package com.securecard.chatapp

import android.app.Application
import androidx.room.Room
import com.securecard.chatapp.data.AppDatabase
import com.securecard.chatapp.data.repository.MessageRepository
import com.securecard.chatapp.data.repository.UserRepository

class ChatApplication : Application() {
	
	// Database instance
	private lateinit var database: AppDatabase
	
	// Repositories
	lateinit var userRepository: UserRepository
	lateinit var messageRepository: MessageRepository
	
	override fun onCreate() {
		super.onCreate()
		
		// Initialize database
		database = Room.databaseBuilder(
			applicationContext,
			AppDatabase::class.java,
			"securecard-chat-db"
		).build()
		
		// Initialize repositories
		userRepository = UserRepository(database.userDao())
		messageRepository = MessageRepository(database.messageDao())
	}
} 