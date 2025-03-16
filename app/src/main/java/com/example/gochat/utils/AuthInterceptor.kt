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

/**
 * `AuthInterceptor` 是一个用于处理 API 请求认证的拦截器。
 * 它会在请求中检查是否需要添加认证信息，并在必要时刷新访问令牌。
 *
 * @param context 应用的上下文
 * @param apiService API 服务接口，用于执行访问令牌的刷新操作
 */
class AuthInterceptor(private val context: Context, private val apiService: ApiService) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // 检查请求是否需要认证
        val invocation = originalRequest.tag(Invocation::class.java)
        val requiresAuth = invocation?.method()?.getAnnotation(RequiresAuth::class.java) != null
                || originalRequest.header("Authorization") != null

        // 如果请求不需要认证，则直接执行请求
        if (!requiresAuth) {
            return chain.proceed(originalRequest)
        }

        // 获取当前的访问令牌
        val accessToken = TokenManager.getAccessToken(context)

        // 如果令牌无效或为空，则尝试刷新令牌
        if (accessToken == null || !TokenManager.isTokenValid(accessToken)) {
            synchronized(this) {
                // 双重检查以避免多线程问题
                val newAccessToken = TokenManager.getAccessToken(context)
                if (newAccessToken == null || !TokenManager.isTokenValid(newAccessToken)) {
                    val refreshed = runBlocking { refreshAccessToken(context, apiService) }

                    // 如果令牌刷新失败，重定向到登录界面
                    if (!refreshed) {
                        (context as? Activity)?.runOnUiThread {
                            context.startActivity(Intent(context, MainActivity::class.java))
                            context.finish()
                        }
                        throw IOException("Token refresh failed") // 抛出异常以终止请求
                    }
                }
            }
        }

        // 使用新的访问令牌更新请求头
        val updatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer ${TokenManager.getAccessToken(context)}")
            .build()

        return chain.proceed(updatedRequest)
    }
}

/**
 * `RequiresAuth` 是一个自定义注解，用于标记需要认证的 API 方法。
 * 该注解用于 Retrofit API 接口，拦截器会自动检测该注解，以决定是否需要在请求中添加访问令牌。
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresAuth
