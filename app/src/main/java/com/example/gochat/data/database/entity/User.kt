package com.example.gochat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.gochat.data.database.entity.enums.UserStatus

/**
 * 用户实体类
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
    var id: Int = 0, // 使用 var

    @ColumnInfo(name = "username")
    var username: String,

    @ColumnInfo(name = "password")
    var password: String,

    @ColumnInfo(name = "email")
    var email: String,

    @ColumnInfo(name = "avatar_url") // 新增头像字段
    var avatarUrl: String? = null,   // 可为空，默认为 null

    @ColumnInfo(name = "created_at")
    var createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "status")
    var status: UserStatus = UserStatus.ACTIVE
)