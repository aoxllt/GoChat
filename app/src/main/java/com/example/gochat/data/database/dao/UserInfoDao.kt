package com.example.gochat.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.gochat.data.database.entity.UserInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface UserInfoDao {
    @Update
    suspend fun update(userInfo: UserInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userInfo: UserInfo)

    // 获取单个用户信息，使用 id（主键）
    @Query("SELECT * FROM user_info WHERE id = :id LIMIT 1")
    suspend fun getUserInfoById(id: Int): UserInfo?

    // 根据邮箱获取用户信息
    @Query("SELECT * FROM user_info WHERE email = :email LIMIT 1")
    suspend fun getUserInfoByEmail(email: String): UserInfo?

    // 获取多个用户信息（如果需要，按 id 查询）
    @Query("SELECT * FROM user_info WHERE id = :id")
    suspend fun getUserInfosById(id: Int): List<UserInfo>

    // 更新最后登录时间
    @Query("UPDATE user_info SET last_login_time = :time WHERE id = :id")
    suspend fun updateLastLoginTime(id: Int, time: String = java.time.Instant.now().toString())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(userInfo: UserInfo)


    @Query("SELECT * FROM user_info WHERE id = :userId")
    fun getUserInfoFlow(userId: Int): Flow<UserInfo?>
}