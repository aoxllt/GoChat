package com.example.gochat.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.Settings.Global.putString
import android.util.Log
import com.example.gochat.api.LoginRequest
import com.example.gochat.api.LoginResponse
import com.example.gochat.api.PasswdchangeRequest
import com.example.gochat.api.PasswdforgotRequest
import com.example.gochat.api.RegisterRequest
import com.example.gochat.api.UseraddRequest
import com.example.gochat.api.VerifyRequest
import com.example.gochat.data.ApiService
import com.example.gochat.data.database.dao.AuthTokenDao
import com.example.gochat.data.database.dao.UserDao
import com.example.gochat.data.database.dao.UserInfoDao
import com.example.gochat.data.database.entity.AuthToken
import com.example.gochat.data.database.entity.User
import com.example.gochat.data.database.entity.UserInfo
import com.example.gochat.utils.TokenManager
import io.jsonwebtoken.Jwts
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Response
import java.time.Instant
import kotlin.math.log

class UserRepository(
    private val userDao: UserDao, // 数据库操作
    private val authToken: AuthTokenDao,
    private val userInfoDao: UserInfoDao,
    private val apiService: ApiService, // 网络请求
    private val context: Context
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

    suspend fun passwdForgot(username: String, email: String): String {
        val req = PasswdforgotRequest(username, email)
        val rep = apiService.passwdForgot(req)
        val bodyString = if (rep.isSuccessful) {
            return "true"
        } else {
            rep.errorBody()?.string() ?: ""
        }
        val jsonObject = JSONObject(bodyString)
        val outcome = jsonObject.optString("message", "")
        if (outcome == "") {
            return "服务器错误"
        }
        return outcome
    }

    suspend fun passwdChange(email: String, usernam: String, newpasswd: String): String {
        val request = PasswdchangeRequest(email, usernam, newpasswd)
        val rep = apiService.passwdChange(request)
        val bodyString = if (rep.isSuccessful) {
            return "true"
        } else {
            rep.errorBody()?.string() ?: ""
        }
        val jsonObject = JSONObject(bodyString)
        val outcome = jsonObject.optString("message", "")
        if (outcome == "") {
            return "服务器错误"
        }
        return outcome
    }

    /**
     * 用户登录
     */
    suspend fun login(account: String, password: String): Result<LoginResponse> {
        return try {
            val request = LoginRequest(account, password)
            val response = apiService.login(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val user = User(
                        username = account,
                        password = password,
                        email = body.user?.email ?: "",
                        avatarUrl = body.user?.avatarUrl
                    )
                    userDao.insert(user)
                    val userId = userDao.getUserByUsername(account)?.id
                        ?: return Result.failure(Exception("无法获取用户 ID，请稍后重试"))

                    val userInfo = UserInfo(
                        id = userId.toLong(),
                        displayName = body.userInfo?.displayName ?: body.user?.username,
                        email = body.userInfo?.email ?: body.user?.email ?: "",
                        avatarUrl = body.user?.avatarUrl,
                        bio = body.userInfo?.bio,
                        gender = body.userInfo?.gender ?: "unspecified",
                        birthDate = body.userInfo?.birthDate?.let {
                            java.time.LocalDate.parse(it.substring(0, 10))
                        },
                        phoneNumber = body.userInfo?.phoneNumber,
                        location = body.userInfo?.location,
                        lastLoginTime = Instant.now().toString()
                    )
                    userInfoDao.insert(userInfo)
                    saveTokens(userId, body.accessToken ?: "", body.refreshToken ?: "")
                    Result.success(body)
                } else {
                    Result.failure(Exception("服务器错误"))
                }
            } else {
                val bodyString = response.errorBody()?.string() ?: "{}"
                val jsonObject = JSONObject(bodyString)
                val message = jsonObject.optString("message", "未知错误")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Result.failure(Exception("网络错误：${e.localizedMessage ?: "未知错误"}"))
        }
    }

    private suspend fun saveTokens(userId: Int, accessToken: String, refreshToken: String) {
        // 使用 TokenManager 保存到 SharedPreferences
        TokenManager.saveTokens(context, userId, accessToken, refreshToken)
        Log.d("UserRepository", "Tokens saved for userId: $userId, accessToken: $accessToken, refreshToken: $refreshToken")
        val savedAccessToken = TokenManager.getAccessToken(context)
        val savedRefreshToken = TokenManager.getRefreshToken(context)
        val savedUserId = TokenManager.getUserId(context)
        Log.d("UserRepository", "Verified saved tokens: accessToken=$savedAccessToken, refreshToken=$savedRefreshToken, userId=$savedUserId")
    }

    // 计算过期时间（可选）
    private fun calculateExpirationTime(accessToken: String): Long? {
        return try {
            val claims = Jwts.parser()
                .setSigningKey("your-secret-key-here".toByteArray()) // 与后端一致
                .parseClaimsJws(accessToken)
                .body
            claims.expiration?.time
        } catch (e: Exception) {
            Log.e("UserRepository", "Failed to parse token expiration: $e")
            null
        }
    }


    private fun isValidLoginResponse(response: LoginResponse): Boolean {
        return response.status == "true" &&
                !response.accessToken.isNullOrBlank() &&
                !response.refreshToken.isNullOrBlank()
    }
    suspend fun getLatestToken(): AuthToken? {
        return authToken.getLatestToken()
    }
}