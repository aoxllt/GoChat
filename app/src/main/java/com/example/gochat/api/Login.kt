package com.example.gochat.api

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val status: String,
    val message: String?,
    val accessToken: String?,
    val refreshToken: String?,
    val user: UserResponse?,
    val userInfo: UserInfoResponse? // 改为 userInfo，与后端一致
)

data class UserResponse(
    val id: Int,
    val username: String,
    val email: String,
)

data class UserInfoResponse(
    val displayName: String? = null,
    val email: String, // 注意后端始终返回非 null，若可能为 null 改为 String?
    val avatarUrl: String? = null,
    val bio: String? = null,
    val gender: String? = null,
    val birthDate: String? = null,
    val phoneNumber: String? = null,
    val location: String? = null
)