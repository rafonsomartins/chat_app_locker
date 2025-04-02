package com.securecard.chatapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.securecard.chatapp.ChatApplication
import com.securecard.chatapp.R
import com.securecard.chatapp.data.repository.MessageRepository
import com.securecard.chatapp.data.repository.UserRepository
import com.securecard.chatapp.databinding.ActivityMainBinding
import com.securecard.chatapp.ui.auth.LoginActivity
import com.securecard.chatapp.ui.chat.ChatActivity
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding
	private lateinit var userRepository: UserRepository
	private lateinit var messageRepository: MessageRepository
	private lateinit var contactsAdapter: ContactsAdapter
	
	private var currentUsername: String? = null

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		setSupportActionBar(binding.toolbar)
		
		// Get repositories from application
		userRepository = (application as ChatApplication).userRepository
		messageRepository = (application as ChatApplication).messageRepository
		
		// Check if user is logged in
		currentUsername = getSharedPreferences("auth_prefs", MODE_PRIVATE)
			.getString("username", null)
		
		if (currentUsername == null) {
			navigateToLogin()
			return
		}
		
		setupContactsList()
		setupListeners()
	}
	
	private fun setupContactsList() {
		contactsAdapter = ContactsAdapter { contactUsername ->
			val intent = Intent(this, ChatActivity::class.java).apply {
				putExtra("CURRENT_USER", currentUsername)
				putExtra("CONTACT_USER", contactUsername)
			}
			startActivity(intent)
		}
		
		binding.rvContacts.apply {
			layoutManager = LinearLayoutManager(this@MainActivity)
			adapter = contactsAdapter
		}
		
		loadContacts()
	}
	
	private fun loadContacts() {
		lifecycleScope.launch {
			currentUsername?.let { username ->
				// Combine user list and contacts list from messages
				val userFlow = userRepository.getAllUsers()
				val contactsFlow = messageRepository.getContactsForUser(username)
				
				combine(userFlow, contactsFlow) { users, contacts ->
					users.filter { it.username != username && (contacts.contains(it.username) || it.hasSecureCard) }
				}.collect { contactsList ->
					contactsAdapter.submitList(contactsList)
				}
			}
		}
	}
	
	private fun setupListeners() {
		binding.fabNewChat.setOnClickListener {
			// Show dialog to select a user to start a new chat with
			NewChatDialogFragment().show(supportFragmentManager, "new_chat_dialog")
		}
		
		binding.etSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(query: String?): Boolean {
				return false
			}
			
			override fun onQueryTextChange(newText: String?): Boolean {
				contactsAdapter.filter(newText ?: "")
				return true
			}
		})
	}
	
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.main_menu, menu)
		return true
	}
	
	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		return when (item.itemId) {
			R.id.action_logout -> {
				logout()
				true
			}
			R.id.action_settings -> {
				// Open settings
				true
			}
			else -> super.onOptionsItemSelected(item)
		}
	}
	
	private fun logout() {
		getSharedPreferences("auth_prefs", MODE_PRIVATE)
			.edit()
			.remove("username")
			.apply()
		
		navigateToLogin()
	}
	
	private fun navigateToLogin() {
		val intent = Intent(this, LoginActivity::class.java)
		intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
		startActivity(intent)
		finish()
	}
} 