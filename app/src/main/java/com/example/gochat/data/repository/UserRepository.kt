package com.example.gochat.data.repository

import android.R
import android.content.ContentResolver
import android.net.Uri
import android.util.Log
import com.example.gochat.api.PasswdforgotRequest
import com.example.gochat.api.RegisterRequest
import com.example.gochat.api.UseraddRequest
import com.example.gochat.api.VerifyRequest
import com.example.gochat.data.ApiService
import com.example.gochat.data.database.dao.UserDao
import com.example.gochat.data.database.entity.User
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class UserRepository(
    private val userDao: UserDao, // 数据库操作
    private val apiService: ApiService // 网络请求
) {

    /**
     * 注册用户
     * @param deviceId 设备 ID
     * @param email 用户邮箱
     * @return 是否注册成功
     */
    suspend fun register(deviceId: String, email: String): Boolean {
        // 发送注册请求到后端
        val request = RegisterRequest(deviceId, email)
        val response = apiService.register(request)

        // 如果请求成功，保存用户到数据库
        if (response.isSuccessful) {
            return true
        }
        return false
    }

    /**
     * 验证验证码
     * @param deviceId 设备 ID
     * @param email 用户邮箱
     * @param captch 验证码
     * @return 是否验证成功
     */
    suspend fun verify(deviceId: String, email: String, captch: String): String {
        val request = VerifyRequest(deviceId, email, captch)
        val rep = apiService.verify(request)
        val bodyString = if (rep.isSuccessful) {
            rep.body()?.string() ?: ""
        } else {
            rep.errorBody()?.string() ?: ""
        }
        val jsonObject = JSONObject(bodyString)
        var outcome = jsonObject.optString("mes", "")
        if (rep.isSuccessful) {
            return "true"
        }
        return outcome
    }

    suspend fun checkname(username: String): String {
        val rep = apiService.checkUsername(username)
        val bodyString = if (rep.isSuccessful) {
            rep.body()?.string() ?: ""
        } else {
            rep.errorBody()?.string() ?: ""
        }
        val jsonObject = JSONObject(bodyString)
        var outcome = jsonObject.optString("mes", "")
        if (outcome == "") {
            return "服务器错误"
        } else if (outcome == "用户名已存在") {
            return "false"
        } else if (outcome == "用户名可用") {
            return "true"
        }
        return outcome
    }

    suspend fun saveUserInfo(
        request: UseraddRequest,
        avatarUri: Uri?,
        contentResolver: ContentResolver,
    ): String {
        try {
            val userJsonRequestBody = request.toJsonRequestBody()
            val avatarPart = avatarUri?.let { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                val byteArray = inputStream?.use { it.readBytes() } ?: byteArrayOf()
                val requestFile = byteArray.toRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestFile)
            }

            val rep = apiService.registerUser(
                userJson = userJsonRequestBody,
                avatar = avatarPart
            )
            val bodyString = if (rep.isSuccessful) {
                rep.body()?.string() ?: ""
            } else {
                rep.errorBody()?.string() ?: ""
            }
            Log.d("SaveUserInfo", "HTTP ${rep.code()}: $bodyString")

            val jsonObject = JSONObject(bodyString)
            val outcome = jsonObject.optString("message", "")
            return when {
                rep.isSuccessful && outcome == "注册成功" -> {
                    // 解析 data 对象
                    val data = jsonObject.optJSONObject("data")
                    if (data != null) {
                        val user = User(
                            id = data.optInt("id", 0),
                            username = request.username,
                            email = request.email,
                            password = request.password, // 注意：实际应加密存储
                            avatarUrl = data.optString("avatar_url", null)
                        )
                        userDao.insert(user) // 插入本地数据库
                        Log.d("SaveUserInfo", "用户已插入本地数据库: $user")
                    }
                    "true"
                }

                outcome == "" -> "服务器错误"
                else -> outcome
            }
        } catch (e: Exception) {
            Log.e("SaveUserInfo", "异常: ${e.message}", e)
            return "保存失败: ${e.message}"
        }
    }
    suspend fun passwdForgot(username: String,email: String): String{
        val req= PasswdforgotRequest(username,email)
        val rep =apiService.passwdForgot(req)
        val bodyString = if (rep.isSuccessful) {
            return "true"
        } else {
            rep.errorBody()?.string() ?: ""
        }
        val jsonObject = JSONObject(bodyString)
        val outcome = jsonObject.optString("message", "")
        if(outcome==""){
            return "服务器错误"
        }
        return outcome
    }
}