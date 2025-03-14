// AuthToken.kt
package com.example.gochat.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_tokens")
data class AuthToken(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val accessToken: String,    // 短令牌
    val refreshToken: String,   // 长令牌
    val userId: Int,            // 关联用户ID
    val createdAt: Long = System.currentTimeMillis(),  // 创建时间
    val accessTokenExpiresAt: Long? = null  // 短令牌过期时间，可选
)