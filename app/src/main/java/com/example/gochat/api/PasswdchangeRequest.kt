package com.example.gochat.api

data class PasswdchangeRequest(
    val email : String,
    val username : String,
    val newpasswd: String
)