package com.example.gochat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "user_id")
    val userId: Int,

    @ColumnInfo(name = "friend_id")
    val friendId: Int,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "last_message")
    val lastMessage: String? = null,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long? = null,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null
)