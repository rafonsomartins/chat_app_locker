package com.securecard.chatapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.securecard.chatapp.data.model.User
import com.securecard.chatapp.databinding.ItemContactBinding
import java.util.Locale

class ContactsAdapter(private val onContactClick: (String) -> Unit) : 
    ListAdapter<User, ContactsAdapter.ContactViewHolder>(ContactDiffCallback()) {
    
    private var originalList: List<User> = emptyList()
    private var filteredList: List<User> = emptyList()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactViewHolder(binding, onContactClick)
    }
    
    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    fun submitList(list: List<User>) {
        originalList = list
        filteredList = list
        super.submitList(list)
    }
    
    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter { 
                it.username.lowercase(Locale.getDefault())
                    .contains(query.lowercase(Locale.getDefault())) 
            }
        }
        super.submitList(filteredList)
    }
    
    class ContactViewHolder(
        private val binding: ItemContactBinding,
        private val onContactClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(user: User) {
            binding.tvUsername.text = user.username
            
            // Show secure card icon if user has a secure card
            binding.ivSecureCard.visibility = if (user.hasSecureCard) View.VISIBLE else View.GONE
            
            // Set placeholder for the last message (would be updated with real data)
            binding.tvLastMessage.text = "Tap to start chatting"
            binding.tvTimestamp.text = ""
            
            binding.root.setOnClickListener {
                onContactClick(user.username)
            }
        }
    }
    
    private class ContactDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.username == newItem.username
        }
        
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
} 