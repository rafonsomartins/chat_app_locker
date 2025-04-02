package com.securecard.chatapp.ui

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.securecard.chatapp.ChatApplication
import com.securecard.chatapp.databinding.DialogNewChatBinding
import com.securecard.chatapp.ui.chat.ChatActivity
import kotlinx.coroutines.launch

class NewChatDialogFragment : DialogFragment() {
	
	private var _binding: DialogNewChatBinding? = null
	private val binding get() = _binding!!
	
	private lateinit var contactsAdapter: ContactsAdapter
	private lateinit var currentUsername: String
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		currentUsername = requireContext().getSharedPreferences("auth_prefs", 0)
			.getString("username", "") ?: ""
		
		return MaterialAlertDialogBuilder(requireContext())
			.setTitle("New Chat")
			.setView(createView())
			.setNegativeButton("Cancel") { _, _ -> dismiss() }
			.create()
	}
	
	private fun createView(): View {
		_binding = DialogNewChatBinding.inflate(LayoutInflater.from(context))
		
		setupContactsList()
		setupSearchListener()
		
		return binding.root
	}
	
	private fun setupContactsList() {
		contactsAdapter = ContactsAdapter { contactUsername ->
			val intent = Intent(requireContext(), ChatActivity::class.java).apply {
				putExtra("CURRENT_USER", currentUsername)
				putExtra("CONTACT_USER", contactUsername)
			}
			startActivity(intent)
			dismiss()
		}
		
		binding.rvUsers.apply {
			layoutManager = LinearLayoutManager(context)
			adapter = contactsAdapter
		}
		
		loadUsers()
	}
	
	private fun loadUsers() {
		val userRepository = (requireActivity().application as ChatApplication).userRepository
		
		lifecycleScope.launch {
			userRepository.getAllUsers().collect { users ->
				// Filter out current user
				val filteredUsers = users.filter { it.username != currentUsername }
				contactsAdapter.submitList(filteredUsers)
			}
		}
	}
	
	private fun setupSearchListener() {
		binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
			
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
				contactsAdapter.filter(s.toString())
			}
			
			override fun afterTextChanged(s: android.text.Editable?) {}
		})
	}
	
	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
} 