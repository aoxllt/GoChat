package com.example.gochat.utils

import android.content.Context
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date

object TokenManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_ACCESS_TOKEN = "accessToken"
    private const val KEY_REFRESH_TOKEN = "refreshToken"
    private const val KEY_USER_ID = "userId"

    // 假设你有 JWT 签名密钥（从后端获取或配置）
    private const val SECRET_KEY = "aoxllt2004" // 替换为实际密钥

    @Synchronized
    fun saveTokens(context: Context, userId: Int, accessToken: String, refreshToken: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putInt(KEY_USER_ID, userId)
            apply()
        }
    }

    @Synchronized
    fun getAccessToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    @Synchronized
    fun getRefreshToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    @Synchronized
    fun getUserId(context: Context): Int? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return if (prefs.contains(KEY_USER_ID)) prefs.getInt(KEY_USER_ID, 0) else null
    }

    @Synchronized
    fun clearTokens(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            clear()
            apply()
        }
    }

    fun isTokenValid(token: String): Boolean {
        return try {
            val key = Keys.hmacShaKeyFor(SECRET_KEY.toByteArray()) // 使用 HMAC-SHA 密钥
            val claims = Jwts.parser()
                .verifyWith(key) // 验证签名
                .build()
                .parseSignedClaims(token) // 解析签名后的 claims
                .payload

            val expiration = claims.expiration
            expiration?.after(Date()) == true
        } catch (e: Exception) {
            // 捕获解析失败、签名无效或过期等异常
            false
        }
    }
}