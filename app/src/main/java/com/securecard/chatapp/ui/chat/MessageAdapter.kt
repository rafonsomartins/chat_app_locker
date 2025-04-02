package com.securecard.chatapp.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.securecard.chatapp.R
import com.securecard.chatapp.data.model.Message
import com.securecard.chatapp.databinding.ItemMessageBinding
import java.text.SimpleDateFormat
import java.util.Locale

class MessageAdapter(
	private val currentUsername: String,
	private val messageClickListener: OnMessageClickListener
) : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {
	
	// Map to store decrypted content temporarily
	private val decryptedMessages = mutableMapOf<Long, String>()
	private val decryptionTimers = mutableMapOf<Long, Int>()
	
	interface OnMessageClickListener {
		fun onMessageClick(message: Message)
	}
	
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
		val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return MessageViewHolder(binding, messageClickListener)
	}
	
	override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
		val message = getItem(position)
		
		// Check if this message has a decrypted content to display
		val decryptedContent = decryptedMessages[message.id]
		val decryptionTime = decryptionTimers[message.id]
		
		holder.bind(message, currentUsername, decryptedContent, decryptionTime)
	}
	
	fun updateDecryptedContent(position: Int, decryptedContent: String) {
		val message = getItem(position)
		decryptedMessages[message.id] = decryptedContent
		notifyItemChanged(position)
	}
	
	fun hideDecryptedContent(position: Int) {
		val message = getItem(position)
		decryptedMessages.remove(message.id)
		decryptionTimers.remove(message.id)
		notifyItemChanged(position)
	}
	
	fun updateDecryptionTimer(position: Int, seconds: Int) {
		val message = getItem(position)
		decryptionTimers[message.id] = seconds
		notifyItemChanged(position)
	}
	
	fun findPositionById(messageId: Long): Int {
		return currentList.indexOfFirst { it.id == messageId }
	}
	
	class MessageViewHolder(
		private val binding: ItemMessageBinding,
		private val messageClickListener: OnMessageClickListener
	) : RecyclerView.ViewHolder(binding.root) {
		
		private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
		
		fun bind(message: Message, currentUsername: String, decryptedContent: String?, decryptionTime: Int?) {
			// Set message alignment based on sender
			val isMyMessage = message.senderId == currentUsername
			
			// Adjust layout constraints based on sender
			(binding.cvMessage.layoutParams as ViewGroup.MarginLayoutParams).apply {
				if (isMyMessage) {
					marginStart = 64
					marginEnd = 8
				} else {
					marginStart = 8
					marginEnd = 64
				}
			}
			
			// Set card background color based on sender
			binding.cvMessage.setCardBackgroundColor(
				if (isMyMessage) 0xFFE1FFC7.toInt() else 0xFFEAEAEA.toInt()
			)
			
			// Set message content
			if (message.encryptedContent != null && decryptedContent == null) {
				// Show encrypted message placeholder
				binding.tvMessageContent.text = itemView.context.getString(R.string.message_encrypted)
				binding.ivLock.visibility = View.VISIBLE
				
				// Make message clickable for decryption
				binding.cvMessage.setOnClickListener {
					messageClickListener.onMessageClick(message)
				}
			} else {
				// Show decrypted content or original text
				binding.tvMessageContent.text = decryptedContent ?: message.content
				binding.ivLock.visibility = if (message.encryptedContent != null) View.VISIBLE else View.GONE
				
				// Only make encrypted messages clickable
				binding.cvMessage.setOnClickListener(null)
			}
			
			// Set timestamp
			binding.tvTimestamp.text = timeFormat.format(message.timestamp)
			
			// Show/hide decryption timer
			if (decryptionTime != null && decryptedContent != null) {
				binding.tvDecryptionTimer.apply {
					visibility = View.VISIBLE
					text = itemView.context.getString(
						R.string.message_decryption_timeout,
						decryptionTime
					)
				}
			} else {
				binding.tvDecryptionTimer.visibility = View.GONE
			}
		}
	}
	
	private class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
		override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
			return oldItem.id == newItem.id
		}
		
		override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
			return oldItem == newItem
		}
	}
} 