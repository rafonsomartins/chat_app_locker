package com.securecard.chatapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
	@PrimaryKey val username: String,
	val passwordHash: String,
	val hasSecureCard: Boolean = false
) 