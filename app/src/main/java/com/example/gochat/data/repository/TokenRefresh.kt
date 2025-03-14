package com.example.gochat.data.repository

import android.content.Context
import com.example.gochat.data.ApiService
import com.example.gochat.utils.TokenManager

suspend fun refreshAccessToken(context: Context, apiService: ApiService): Boolean {
    val refreshToken = TokenManager.getRefreshToken(context) ?: return false
    return try {
        val response = apiService.refreshToken(refreshToken)
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.message == "刷新成功") {
                TokenManager.saveTokens(
                    context,
                    TokenManager.getUserId(context) ?: 0,
                    body.accessToken ?: "",
                    body.refreshToken ?: ""
                )
                true
            } else {
                false
            }
        } else {
            false
        }
    } catch (e: Exception) {
        false
    }
}