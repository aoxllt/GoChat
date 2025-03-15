package com.example.gochat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.gochat.data.database.entity.enums.UserStatus

/**
 * 用户登录实体类
 * @param id 用户 ID，主键，自动生成
 * @param username 用户名，唯一
 * @param password 密码
 * @param email 邮箱
 * @param createdAt 创建时间，默认为当前时间戳
 * @param status 用户状态，默认为 ACTIVE
 */
@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "username")
    val username: String,

    @ColumnInfo(name = "password")
    val password: String,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "status")
    val status: UserStatus = UserStatus.ACTIVE
)