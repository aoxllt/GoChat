package com.example.gochat.api

data class VerifyRequest (
    val email : String,
    val deviceId: String,
    val captch: String
)