package com.securecard.chatapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val senderId: String,
    val recipientId: String,
    val content: String,
    val encryptedContent: String? = null,
    val isCardEncrypted: Boolean = false,
    val timestamp: Date = Date(),
    val isRead: Boolean = false
) 