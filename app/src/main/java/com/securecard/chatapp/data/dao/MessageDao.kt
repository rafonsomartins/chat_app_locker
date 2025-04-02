package com.securecard.chatapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.securecard.chatapp.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
	@Query("SELECT * FROM messages WHERE (senderId = :userId AND recipientId = :contactId) OR (senderId = :contactId AND recipientId = :userId) ORDER BY timestamp ASC")
	fun getMessagesForChat(userId: String, contactId: String): Flow<List<Message>>
	
	@Query("SELECT * FROM messages WHERE id = :messageId")
	suspend fun getMessageById(messageId: Long): Message?
	
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertMessage(message: Message): Long
	
	@Update
	suspend fun updateMessage(message: Message)
	
	@Query("SELECT DISTINCT senderId FROM messages WHERE recipientId = :userId " +
		"UNION SELECT DISTINCT recipientId FROM messages WHERE senderId = :userId")
	fun getContactsForUser(userId: String): Flow<List<String>>
	
	@Query("UPDATE messages SET isRead = 1 WHERE recipientId = :userId AND senderId = :contactId")
	suspend fun markMessagesAsRead(userId: String, contactId: String)
} 