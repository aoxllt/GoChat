package com.example.gochat.api

data class ChatSession(
    val id: Int, // 会话 ID
    val friendId: Int, // 好友 ID
    val friendName: String, // 好友昵称
    val friendAvatarUrl: String?, // 好友头像 URL
    val lastMessage: String, // 最后一条消息
    val lastMessageTime: String // 最后消息时间
)