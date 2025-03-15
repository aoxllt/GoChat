package com.example.gochat.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.gochat.data.database.dao.UserDao
import com.example.gochat.data.database.dao.UserInfoDao
import com.example.gochat.data.database.entity.User
import com.example.gochat.data.database.entity.UserInfo

@Database(entities = [User::class, UserInfo::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun userInfoDao(): UserInfoDao

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