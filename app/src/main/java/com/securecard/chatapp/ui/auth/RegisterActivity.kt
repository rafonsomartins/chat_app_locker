package com.securecard.chatapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.securecard.chatapp.ChatApplication
import com.securecard.chatapp.databinding.ActivityRegisterBinding
import com.securecard.chatapp.ui.MainActivity
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRegisterBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupListeners()
    }
    
    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val hasSecureCard = binding.cbHasSecureCard.isChecked
            
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            registerUser(username, password, hasSecureCard)
        }
        
        binding.btnLogin.setOnClickListener {
            finish()
        }
    }
    
    private fun registerUser(username: String, password: String, hasSecureCard: Boolean) {
        val userRepository = (application as ChatApplication).userRepository
        
        lifecycleScope.launch {
            val success = userRepository.registerUser(username, password, hasSecureCard)
            
            if (success) {
                // Save login state
                getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    .edit()
                    .putString("username", username)
                    .apply()
                
                // Navigate to main screen
                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Username already exists",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
} 