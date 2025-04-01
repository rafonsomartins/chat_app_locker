package com.securecard.chatapp.ui.chat

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.securecard.chatapp.ChatApplication
import com.securecard.chatapp.R
import com.securecard.chatapp.data.model.Message
import com.securecard.chatapp.data.model.User
import com.securecard.chatapp.data.repository.MessageRepository
import com.securecard.chatapp.data.repository.UserRepository
import com.securecard.chatapp.data.util.EncryptionUtil
import com.securecard.chatapp.data.util.NfcUtil
import com.securecard.chatapp.databinding.ActivityChatBinding
import com.securecard.chatapp.databinding.DialogNfcPromptBinding
import com.securecard.chatapp.databinding.DialogSecurityWarningBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

class ChatActivity : AppCompatActivity(), MessageAdapter.OnMessageClickListener {
    
    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var userRepository: UserRepository
    private lateinit var messageRepository: MessageRepository
    
    private lateinit var currentUsername: String
    private lateinit var contactUsername: String
    private var contactUser: User? = null
    
    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private var nfcDialog: AlertDialog? = null
    private var currentDecryptingMessageId: Long = -1
    private var decryptionTimer: CountDownTimer? = null
    
    // Standard encryption key for non-NFC messages
    private lateinit var standardEncryptionKey: ByteArray
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Initialize NFC adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_MUTABLE
        )
        
        // Get repositories
        userRepository = (application as ChatApplication).userRepository
        messageRepository = (application as ChatApplication).messageRepository
        
        // Get usernames from intent
        currentUsername = intent.getStringExtra("CURRENT_USER") ?: ""
        contactUsername = intent.getStringExtra("CONTACT_USER") ?: ""
        
        if (currentUsername.isEmpty() || contactUsername.isEmpty()) {
            Toast.makeText(this, "Invalid user information", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setupToolbar()
        setupMessageList()
        setupSendButton()
        loadContactInfo()
        
        // Generate or retrieve standard encryption key
        standardEncryptionKey = getOrCreateEncryptionKey()
    }
    
    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = contactUsername
            setDisplayHomeAsUpEnabled(true)
        }
    }
    
    private fun setupMessageList() {
        messageAdapter = MessageAdapter(currentUsername, this)
        
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true // Messages appear from bottom
            }
            adapter = messageAdapter
        }
        
        loadMessages()
    }
    
    private fun loadMessages() {
        lifecycleScope.launch {
            messageRepository.getMessagesForChat(currentUsername, contactUsername)
                .collectLatest { messages ->
                    messageAdapter.submitList(messages)
                    if (messages.isNotEmpty()) {
                        binding.rvMessages.scrollToPosition(messages.size - 1)
                    }
                    
                    // Mark messages as read
                    messageRepository.markMessagesAsRead(currentUsername, contactUsername)
                }
        }
    }
    
    private fun loadContactInfo() {
        lifecycleScope.launch {
            contactUser = userRepository.getUserByUsername(contactUsername)
            updateSecureCardStatus()
        }
    }
    
    private fun updateSecureCardStatus() {
        contactUser?.let {
            binding.ivSecureCardStatus.visibility = if (it.hasSecureCard) View.VISIBLE else View.GONE
        }
    }
    
    private fun setupSendButton() {
        binding.btnSend.setOnClickListener {
            val messageText = binding.etMessage.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                binding.etMessage.text?.clear()
            }
        }
    }
    
    private fun sendMessage(text: String) {
        contactUser?.let { contact ->
            if (!contact.hasSecureCard) {
                // Show warning if recipient doesn't have a secure card
                val shouldShowWarning = getSharedPreferences("security_prefs", MODE_PRIVATE)
                    .getBoolean("show_security_warning_$contactUsername", true)
                
                if (shouldShowWarning) {
                    showSecurityWarningDialog(text)
                } else {
                    sendStandardEncryptedMessage(text)
                }
            } else {
                // Use both standard and card encryption
                sendDualEncryptedMessage(text)
            }
        } ?: sendStandardEncryptedMessage(text) // Fallback if contact info not loaded
    }
    
    private fun sendStandardEncryptedMessage(text: String) {
        val encryptedContent = EncryptionUtil.encrypt(text, standardEncryptionKey)
        
        lifecycleScope.launch {
            messageRepository.sendMessage(
                senderId = currentUsername,
                recipientId = contactUsername,
                content = text,
                encryptedContent = encryptedContent,
                isCardEncrypted = false
            )
        }
    }
    
    private fun sendDualEncryptedMessage(text: String) {
        // First encrypt with standard encryption
        val standardEncrypted = EncryptionUtil.encrypt(text, standardEncryptionKey)
        
        // Prepare data for card encryption (in real app, this would be done differently)
        val cardEncryptedContent = "CARD_ENCRYPTED:$standardEncrypted"
        
        lifecycleScope.launch {
            messageRepository.sendMessage(
                senderId = currentUsername,
                recipientId = contactUsername,
                content = text,
                encryptedContent = cardEncryptedContent,
                isCardEncrypted = true
            )
        }
    }
    
    private fun showSecurityWarningDialog(messageText: String) {
        val dialogBinding = DialogSecurityWarningBinding.inflate(layoutInflater)
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        
        dialogBinding.btnContinue.setOnClickListener {
            if (dialogBinding.cbRememberChoice.isChecked) {
                getSharedPreferences("security_prefs", MODE_PRIVATE)
                    .edit()
                    .putBoolean("show_security_warning_$contactUsername", false)
                    .apply()
            }
            sendStandardEncryptedMessage(messageText)
            dialog.dismiss()
        }
        
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun getOrCreateEncryptionKey(): ByteArray {
        val prefs = EncryptionUtil.createEncryptedSharedPreferences(
            this,
            "encryption_keys"
        )
        
        val keyStr = prefs.getString("standard_key", null)
        return if (keyStr != null) {
            android.util.Base64.decode(keyStr, android.util.Base64.DEFAULT)
        } else {
            val newKey = EncryptionUtil.generateKey()
            prefs.edit().putString(
                "standard_key",
                android.util.Base64.encodeToString(newKey, android.util.Base64.DEFAULT)
            ).apply()
            newKey
        }
    }
    
    override fun onMessageClick(message: Message) {
        if (message.encryptedContent != null) {
            if (message.isCardEncrypted) {
                // Need NFC card to decrypt
                showNfcDecryptionDialog(message.id)
            } else {
                // Standard decryption
                decryptAndDisplayMessage(message)
            }
        }
    }
    
    private fun decryptAndDisplayMessage(message: Message) {
        try {
            message.encryptedContent?.let { encrypted ->
                if (!message.isCardEncrypted) {
                    // Standard decryption
                    val decryptedText = EncryptionUtil.decrypt(encrypted, standardEncryptionKey)
                    updateMessageWithDecryptedText(message.id, decryptedText)
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to decrypt message", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateMessageWithDecryptedText(messageId: Long, decryptedText: String) {
        val position = messageAdapter.findPositionById(messageId)
        if (position != -1) {
            messageAdapter.updateDecryptedContent(position, decryptedText)
            
            // Cancel any existing timer
            decryptionTimer?.cancel()
            
            // Set current decrypting message
            currentDecryptingMessageId = messageId
            
            // Start countdown timer to hide the message again
            decryptionTimer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    val seconds = millisUntilFinished / 1000
                    messageAdapter.updateDecryptionTimer(position, seconds.toInt())
                }
                
                override fun onFinish() {
                    messageAdapter.hideDecryptedContent(position)
                    currentDecryptingMessageId = -1
                }
            }.start()
        }
    }
    
    private fun showNfcDecryptionDialog(messageId: Long) {
        if (nfcAdapter == null) {
            Toast.makeText(this, R.string.nfc_not_supported, Toast.LENGTH_SHORT).show()
            return
        }
        
        if (!nfcAdapter!!.isEnabled) {
            Toast.makeText(this, R.string.nfc_disabled, Toast.LENGTH_SHORT).show()
            return
        }
        
        currentDecryptingMessageId = messageId
        
        val dialogBinding = DialogNfcPromptBinding.inflate(layoutInflater)
        
        nfcDialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()
        
        dialogBinding.btnCancel.setOnClickListener {
            nfcDialog?.dismiss()
            currentDecryptingMessageId = -1
        }
        
        nfcDialog?.show()
        
        // Enable NFC foreground dispatch
        NfcUtil.enableNfcForegroundDispatch(this, pendingIntent!!)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        
        if (currentDecryptingMessageId != -1L) {
            // Check if this is an NFC intent
            val tag = NfcUtil.processNfcIntent(intent)
            if (tag != null) {
                decryptWithNfcCard(tag)
            }
        }
    }
    
    private fun decryptWithNfcCard(tag: Tag) {
        lifecycleScope.launch {
            val message = messageRepository.getMessageById(currentDecryptingMessageId)
            
            message?.let {
                if (it.isCardEncrypted && it.encryptedContent != null) {
                    // This is a simulation since we don't have actual NFC card decryption
                    // In a real app, you would decrypt with the NFC card
                    
                    // Extract the standard encrypted part (for simulation)
                    val encryptedPart = it.encryptedContent!!.substringAfter("CARD_ENCRYPTED:")
                    
                    // Decrypt with standard key (for simulation)
                    val decryptedText = EncryptionUtil.decrypt(encryptedPart, standardEncryptionKey)
                    
                    // Update the UI
                    updateMessageWithDecryptedText(it.id, decryptedText)
                }
            }
            
            // Close NFC dialog
            nfcDialog?.dismiss()
        }
    }
    
    override fun onResume() {
        super.onResume()
        if (nfcAdapter != null) {
            NfcUtil.enableNfcForegroundDispatch(this, pendingIntent!!)
        }
    }
    
    override fun onPause() {
        super.onPause()
        if (nfcAdapter != null) {
            NfcUtil.disableNfcForegroundDispatch(this)
        }
        decryptionTimer?.cancel()
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 