package com.example.gochat.api

import com.example.gochat.api.ChatSession
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageApi {

}

data class Message(
    val id: Long,
    val senderId: Long,
    val receiverId: Long,
    val content: String,
    val timestamp: Long
)