package com.securecard.chatapp.data.repository

import com.securecard.chatapp.data.dao.UserDao
import com.securecard.chatapp.data.model.User
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class UserRepository(private val userDao: UserDao) {
	
	suspend fun registerUser(username: String, password: String, hasSecureCard: Boolean = false): Boolean {
		val existingUser = userDao.getUserByUsername(username)
		if (existingUser != null) {
			return false
		}
		
		val passwordHash = hashPassword(password)
		val user = User(username, passwordHash, hasSecureCard)
		userDao.insertUser(user)
		return true
	}
	
	suspend fun loginUser(username: String, password: String): User? {
		val passwordHash = hashPassword(password)
		return userDao.validateUser(username, passwordHash)
	}
	
	suspend fun updateUserCardStatus(username: String, hasSecureCard: Boolean) {
		userDao.updateUserCardStatus(username, hasSecureCard)
	}
	
	suspend fun getUserByUsername(username: String): User? {
		return userDao.getUserByUsername(username)
	}
	
	fun getAllUsers(): Flow<List<User>> {
		return userDao.getAllUsers()
	}
	
	private fun hashPassword(password: String): String {
		val bytes = password.toByteArray()
		val md = MessageDigest.getInstance("SHA-256")
		val digest = md.digest(bytes)
		return digest.fold("") { str, it -> str + "%02x".format(it) }
	}
} 