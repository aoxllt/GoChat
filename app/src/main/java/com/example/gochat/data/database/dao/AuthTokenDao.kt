// AuthTokenDao.kt
package com.example.gochat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.gochat.data.database.entity.AuthToken

@Dao
interface AuthTokenDao {
    @Insert
    suspend fun insert(token: AuthToken)

    @Query("SELECT * FROM auth_tokens WHERE userId = :userId LIMIT 1")
    suspend fun getTokenByUserId(userId: Int): AuthToken?

    @Query("DELETE FROM auth_tokens WHERE userId = :userId")
    suspend fun deleteTokenByUserId(userId: Int)

    @Query("SELECT * FROM auth_tokens LIMIT 1")
    suspend fun getLatestToken(): AuthToken? // 获取最近的令牌
}