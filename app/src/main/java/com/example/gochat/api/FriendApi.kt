package com.example.gochat.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * 好友数据模型
 */
data class Friend(
    val id: Long,           // 用户ID
    val username: String,   // 用户名
    val nickname: String,   // 昵称
    val avatarUrl: String?  // 头像URL，可为空
)

/**
 * 好友请求数据模型
 */
data class FriendRequest(
    val targetId: Long      // 目标用户ID
)

/**
 * 消息数据模型
 */
data class Messages(
    val id: Long,           // 消息ID
    val senderId: Long,     // 发送者ID
    val receiverId: Long,   // 接收者ID
    val content: String,    // 消息内容
    val timestamp: Long     // 发送时间戳（毫秒）
)