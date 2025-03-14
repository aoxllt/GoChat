package com.example.gochat.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.gochat.data.database.converter.DateConverter
import com.example.gochat.data.database.dao.AuthTokenDao
import com.example.gochat.data.database.dao.UserDao
import com.example.gochat.data.database.entity.AuthToken
import com.example.gochat.data.database.entity.User
import com.example.gochat.data.database.entity.UserInfo
import android.content.Context
import com.example.gochat.data.database.dao.UserInfoDao


@Database(entities = [User::class, AuthToken::class, UserInfo::class], version = 4, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun authTokenDao(): AuthTokenDao
    abstract fun userInfoDao(): UserInfoDao // 新增 UserInfoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // 开发阶段使用
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}