package com.example.gochat.api

data class UserProfileResponse(
    val displayname: String,
    val email: String,
    val bio: String,
    val sex: String,
    val birth: String,
    val Phonenumber: String,
    val localcity: String,
    val avatarUrl: String,
)