package com.securecard.chatapp.data.repository

import com.securecard.chatapp.data.dao.MessageDao
import com.securecard.chatapp.data.model.Message
import kotlinx.coroutines.flow.Flow

class MessageRepository(private val messageDao: MessageDao) {
    
    fun getMessagesForChat(userId: String, contactId: String): Flow<List<Message>> {
        return messageDao.getMessagesForChat(userId, contactId)
    }
    
    suspend fun sendMessage(
        senderId: String,
        recipientId: String,
        content: String,
        encryptedContent: String? = null,
        isCardEncrypted: Boolean = false
    ): Long {
        val message = Message(
            senderId = senderId,
            recipientId = recipientId,
            content = content,
            encryptedContent = encryptedContent,
            isCardEncrypted = isCardEncrypted
        )
        return messageDao.insertMessage(message)
    }
    
    suspend fun updateMessage(message: Message) {
        messageDao.updateMessage(message)
    }
    
    suspend fun getMessageById(messageId: Long): Message? {
        return messageDao.getMessageById(messageId)
    }
    
    fun getContactsForUser(userId: String): Flow<List<String>> {
        return messageDao.getContactsForUser(userId)
    }
    
    suspend fun markMessagesAsRead(userId: String, contactId: String) {
        messageDao.markMessagesAsRead(userId, contactId)
    }
} 