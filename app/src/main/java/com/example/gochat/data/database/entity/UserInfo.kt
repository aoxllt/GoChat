package com.example.gochat.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * 用户个人信息实体类，与 User 一对一关联
 * @param id 主键，同时是外键，引用 User.id
 * @param displayName 显示名称
 * @param email 邮箱（可与 User 的 email 不同）
 * @param avatarUrl 头像 URL
 * @param bio 个人简介
 * @param gender 性别
 * @param birthDate 出生日期
 * @param phoneNumber 电话号码
 * @param location 位置
 * @param lastLoginTime 最后登录时间
 */
@Entity(
    tableName = "user_info",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id"]),
        Index(value = ["email"]),
        Index(value = ["display_name"])
    ]
)
data class UserInfo(
    @PrimaryKey
    val id: Int, // 主键，直接作为外键引用 User.id

    @ColumnInfo(name = "display_name")
    val displayName: String? = null,

    @ColumnInfo(name = "email")
    val email: String,

    @ColumnInfo(name = "avatar_url")
    val avatarUrl: String? = null,

    @ColumnInfo(name = "bio")
    val bio: String? = null,

    @ColumnInfo(name = "gender")
    val gender: String = "unspecified",

    @ColumnInfo(name = "birth_date")
    val birthDate: LocalDate? = null,

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String? = null,

    @ColumnInfo(name = "location")
    val location: String? = null,

    @ColumnInfo(name = "last_login_time")
    val lastLoginTime: String = java.time.Instant.now().toString()
)