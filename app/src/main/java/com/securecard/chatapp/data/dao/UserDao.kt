package com.securecard.chatapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.securecard.chatapp.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
	@Query("SELECT * FROM users WHERE username = :username")
	suspend fun getUserByUsername(username: String): User?
	
	@Query("SELECT * FROM users WHERE username = :username AND passwordHash = :passwordHash")
	suspend fun validateUser(username: String, passwordHash: String): User?
	
	@Query("SELECT * FROM users")
	fun getAllUsers(): Flow<List<User>>
	
	@Insert(onConflict = OnConflictStrategy.ABORT)
	suspend fun insertUser(user: User)
	
	@Update
	suspend fun updateUser(user: User)
	
	@Query("UPDATE users SET hasSecureCard = :hasSecureCard WHERE username = :username")
	suspend fun updateUserCardStatus(username: String, hasSecureCard: Boolean)
} 