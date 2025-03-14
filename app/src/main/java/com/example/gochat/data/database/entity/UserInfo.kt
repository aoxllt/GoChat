package com.example.gochat.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "user_info",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"], // 引用 User 表的 id
            childColumns = ["id"], // UserInfo 表中的外键列改为 id
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id"]), // 索引外键 id
        Index(value = ["email"]),
        Index(value = ["displayName"])
    ]
)
data class UserInfo(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0, // 主键，自动生成
    val id: Long, // 外键，引用 User.id
    val displayName: String? = null,
    val email: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val gender: String = "unspecified",
    val birthDate: LocalDate? = null,
    val phoneNumber: String? = null,
    val location: String? = null,
    val lastLoginTime: String = java.time.Instant.now().toString()
)