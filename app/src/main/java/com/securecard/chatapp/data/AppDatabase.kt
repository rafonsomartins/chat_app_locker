package com.securecard.chatapp.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.securecard.chatapp.data.dao.MessageDao
import com.securecard.chatapp.data.dao.UserDao
import com.securecard.chatapp.data.model.Message
import com.securecard.chatapp.data.model.User
import com.securecard.chatapp.data.util.Converters

@Database(entities = [User::class, Message::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
} 