package com.example.gochat.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gochat.data.database.dao.ChatDao
import com.example.gochat.data.database.dao.FriendDao
import com.example.gochat.data.database.dao.UserDao
import com.example.gochat.data.database.dao.UserInfoDao
import com.example.gochat.data.database.entity.Chat
import com.example.gochat.data.database.entity.Friend
import com.example.gochat.data.database.entity.User
import com.example.gochat.data.database.entity.UserInfo

@Database(
    entities = [User::class, UserInfo::class, Friend::class,Chat::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userInfoDao(): UserInfoDao
    abstract fun friendDao(): FriendDao
    abstract fun chatDao(): ChatDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gochat_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}