package com.example.gochat.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey val id: Long,
    val username: String,
    val nickname: String,
    val avatarUrl: String?
)