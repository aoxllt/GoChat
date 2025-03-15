package com.example.gochat.api

data class VerifyResponse(
    val code: Int,
    val message: String,
    val token: String? = null
)