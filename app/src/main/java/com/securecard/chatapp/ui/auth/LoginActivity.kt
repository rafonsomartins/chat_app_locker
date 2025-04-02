package com.securecard.chatapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.securecard.chatapp.ChatApplication
import com.securecard.chatapp.databinding.ActivityLoginBinding
import com.securecard.chatapp.ui.MainActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
	
	private lateinit var binding: ActivityLoginBinding
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityLoginBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		setupListeners()
	}
	
	private fun setupListeners() {
		binding.btnLogin.setOnClickListener {
			val username = binding.etUsername.text.toString().trim()
			val password = binding.etPassword.text.toString().trim()
			
			if (username.isEmpty() || password.isEmpty()) {
				Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
				return@setOnClickListener
			}
			
			loginUser(username, password)
		}
		
		binding.btnRegister.setOnClickListener {
			startActivity(Intent(this, RegisterActivity::class.java))
		}
	}
	
	private fun loginUser(username: String, password: String) {
		val userRepository = (application as ChatApplication).userRepository
		
		lifecycleScope.launch {
			val user = userRepository.loginUser(username, password)
			
			if (user != null) {
				// Save login state
				getSharedPreferences("auth_prefs", MODE_PRIVATE)
					.edit()
					.putString("username", username)
					.apply()
				
				// Navigate to main screen
				val intent = Intent(this@LoginActivity, MainActivity::class.java)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
				startActivity(intent)
				finish()
			} else {
				runOnUiThread {
					Toast.makeText(
						this@LoginActivity,
						"Invalid username or password",
						Toast.LENGTH_SHORT
					).show()
				}
			}
		}
	}
} 