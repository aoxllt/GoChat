package com.example.gochat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.gochat.data.database.entity.enums.UserStatus

@Entity(tableName = "friends")
data class Friend(
    @PrimaryKey
    val id: Int,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "status")
    val status: UserStatus,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,

    @ColumnInfo(name = "owner_id")
    val ownerId: Int // 当前登录用户 ID，用于区分不同用户的好友列表
)