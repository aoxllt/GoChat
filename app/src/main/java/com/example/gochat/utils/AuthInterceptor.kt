package com.example.gochat.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.gochat.MainActivity
import com.example.gochat.data.ApiService
import com.example.gochat.data.repository.refreshAccessToken
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.Invocation
import java.io.IOException

class AuthInterceptor(private val context: Context, private val apiService: ApiService) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 检查是否需要认证
        val invocation = originalRequest.tag(Invocation::class.java)
        val requiresAuth = invocation?.method()?.getAnnotation(RequiresAuth::class.java) != null
                || originalRequest.header("Authorization") != null

        if (!requiresAuth) {
            return chain.proceed(originalRequest)
        }

        val accessToken = TokenManager.getAccessToken(context)
        if (accessToken == null || !TokenManager.isTokenValid(accessToken)) {
            synchronized(this) {
                // 双重检查
                val newAccessToken = TokenManager.getAccessToken(context)
                if (newAccessToken == null || !TokenManager.isTokenValid(newAccessToken)) {
                    val refreshed = runBlocking { refreshAccessToken(context, apiService) }
                    if (!refreshed) {
                        (context as? Activity)?.runOnUiThread {
                            context.startActivity(Intent(context, MainActivity::class.java))
                            context.finish()
                        }
                        throw IOException("Token refresh failed")
                    }
                }
            }
        }

        val updatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${TokenManager.getAccessToken(context)}")
            .build()
        return chain.proceed(updatedRequest)
    }
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresAuth