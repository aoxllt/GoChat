package com.example.gochat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gochat.data.database.entity.UserInfo

@Dao
interface UserInfoDao {
    @Update
    suspend fun update(userInfo: UserInfo)

    @Query("SELECT * FROM user_info WHERE userId = :userId")
    suspend fun getProfileByUserId(userId: Long): UserInfo?

    @Query("SELECT * FROM user_info WHERE id = :userId")
    suspend fun getProfilesByUserId(userId: Long): List<UserInfo>

    @Query("SELECT * FROM user_info WHERE email = :email")
    suspend fun getProfileByEmail(email: String): UserInfo?

    @Query("UPDATE user_info SET lastLoginTime = :time WHERE userId = :userId")
    suspend fun updateLastLoginTime(userId: Long, time: String = java.time.Instant.now().toString())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userInfo: UserInfo)

    @Query("SELECT * FROM user_info WHERE id = :userId LIMIT 1")
    suspend fun getUserInfoByUserId(userId: Long): UserInfo?
}