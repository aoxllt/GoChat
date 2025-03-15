package com.example.gochat.data.repository

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.gochat.api.LoginRequest
import com.example.gochat.api.LoginResponse
import com.example.gochat.api.PasswdchangeRequest
import com.example.gochat.api.PasswdforgotRequest
import com.example.gochat.api.PasswdforgotSendcodeRequest
import com.example.gochat.api.RegisterRequest
import com.example.gochat.api.UseraddRequest
import com.example.gochat.api.VerifyRequest
import com.example.gochat.data.ApiService
import com.example.gochat.data.database.dao.UserDao
import com.example.gochat.data.database.dao.UserInfoDao
import com.example.gochat.data.database.entity.User
import com.example.gochat.data.database.entity.UserInfo
import com.example.gochat.data.database.entity.enums.UserStatus
import com.example.gochat.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.time.Instant
import java.time.LocalDate

class UserRepository(
    private val userDao: UserDao, // 数据库操作
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
    suspend fun register(deviceId: String, email: String): Result<String> {
        return try {
            val request = RegisterRequest(deviceId, email)
            val response = apiService.register(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 200) {
                    Result.success("注册成功") // 成功时固定提示
                } else {
                    Result.failure(Exception(body?.message ?: "注册失败")) // 注册失败使用 body.message
                }
            } else {
                if (response.code() == 400) {
                    val errorBody = response.errorBody()?.string()
                    // 假设 errorBody 是 JSON 格式，尝试解析 message 字段
                    val message = errorBody?.let {
                        try {
                            val json = JSONObject(it) // 需要 import org.json.JSONObject
                            json.optString("message", "注册失败")
                        } catch (e: Exception) {
                            it // 如果解析失败，直接使用原始 errorBody 字符串
                        }
                    } ?: "注册失败"
                    Result.failure(Exception(message))
                } else {
                    val errorBody = response.errorBody()?.string()
                    Result.failure(Exception(errorBody ?: "服务器错误")) // 其他非成功响应使用 errorBody
                }
            }
        } catch (e: IOException) {
            Result.failure(Exception("网络错误"))
        } catch (e: HttpException) {
            Result.failure(Exception("服务器错误"))
        } catch (e: Exception) {
            Result.failure(Exception("注册失败"))
        }
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

    suspend fun checkUsername(username: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.checkUsername(username)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.code == 200) {
                        "可用"
                    } else {
                        body?.message ?: "用户名不可用"
                    }
                } else {
                    val bodyString = response.errorBody()?.string() ?: "{}"
                    val jsonObject = JSONObject(bodyString)
                    jsonObject.optString("message", "未知错误")
                }
            } catch (e: Exception) {
                Log.e("CheckUsername", "异常", e)
                "检查失败"
            }
        }
    }

    suspend fun saveUserInfo(
        request: UseraddRequest,
        avatarUri: Uri?,
        contentResolver: ContentResolver
    ): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val userJsonRequestBody = request.toJsonRequestBody()
                val avatarPart = avatarUri?.let { uri ->
                    val inputStream = contentResolver.openInputStream(uri)
                    val byteArray = inputStream?.use { it.readBytes() } ?: byteArrayOf()
                    val requestFile = byteArray.toRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestFile)
                }

                val response: Response<LoginResponse> = apiService.registerUser(
                    userJson = userJsonRequestBody,
                    avatar = avatarPart
                )

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "true") {
                        // 创建新 User 对象
                        val user = User(
                            id = body.user?.id ?: 0, // Int 类型，与后端一致
                            username = request.username,
                            password = request.password, // 注意：应加密存储
                            email = body.user?.email ?: request.email,
                            createdAt = System.currentTimeMillis(),
                            status = UserStatus.ACTIVE
                        )

                        // 直接插入新用户
                        userDao.insert(user)
                        Log.d("SaveUserInfo", "新用户已插入 User 表: $user")

                        // 获取用户 ID
                        val userId = body.user?.id
                            ?: return@withContext Result.failure(Exception("无法获取用户 ID"))

                        // 创建新 UserInfo 对象
                        val userInfo = UserInfo(
                            id = userId,
                            displayName = body.userInfo?.displayName ?: body.user?.username ?: request.username,
                            email = body.userInfo?.email ?: body.user?.email ?: request.email,
                            avatarUrl = body.userInfo?.avatarUrl, // 从 userInfo 获取 avatarUrl
                            bio = body.userInfo?.bio,
                            gender = body.userInfo?.gender ?: "unspecified",
                            birthDate = body.userInfo?.birthDate?.let {
                                LocalDate.parse(it.substring(0, 10))
                            },
                            phoneNumber = body.userInfo?.phoneNumber,
                            location = body.userInfo?.location,
                            lastLoginTime = Instant.now().toString()
                        )

                        // 直接插入新 UserInfo
                        userInfoDao.insert(userInfo)
                        Log.d("SaveUserInfo", "新用户信息已插入 UserInfo 表: $userInfo")

                        // 保存令牌
                        saveTokens(userId, body.accessToken ?: "", body.refreshToken ?: "")
                        Result.success(body)
                    } else {
                        Result.failure(Exception("服务器返回无效数据"))
                    }
                } else {
                    val bodyString = response.errorBody()?.string() ?: "{}"
                    val jsonObject = JSONObject(bodyString)
                    val message = jsonObject.optString("message", "未知错误")
                    Result.failure(Exception(message))
                }
            } catch (e: Exception) {
                Log.e("SaveUserInfo", "Register failed")
                Result.failure(Exception("网络错误"))
            }
        }
    }

    suspend fun sendVerificationCode(username: String, email: String): Result<Boolean> {
        return try {
            val request = PasswdforgotSendcodeRequest(username, email)
            val response = apiService.sendVerificationCode(request)

            if (response.isSuccessful) {
                // 假设 code 200 表示成功
                val body = response.body()
                if (body != null && body.code == 200) {
                    Result.success(true)
                } else {
                    Result.failure(Exception(body?.message ?: "未知错误"))
                }
            } else {
                Result.failure(HttpException(response))
            }
        } catch (e: IOException) {
            // 网络错误
            Result.failure(Exception("网络错误"))
        } catch (e: HttpException) {
            // HTTP 状态码错误
            Result.failure(Exception("服务器错误"))
        } catch (e: Exception) {
            // 其他异常
            Result.failure(Exception("发送验证码失败"))
        }
    }

    suspend fun passwdForgot(username: String, email: String, code: String): Result<String> {
        return try {
            val request= PasswdforgotRequest(email, username,code)
            val response = apiService.passwdForgot(request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 200) {
                    val token = body.token
                    if (!token.isNullOrEmpty()) {
                        Result.success(token)
                    } else {
                        Result.failure(Exception(body.message ?: "验证失败"))
                    }
                } else {
                    Result.failure(Exception(body?.message ?: "未知错误"))
                }
            } else {
                val bodyString = response.errorBody()?.string() ?: "{}"
                val jsonObject = JSONObject(bodyString)
                val message = jsonObject.optString("message", "未知错误")
                Result.failure(Exception(message))
            }
        } catch (e: IOException) {
            Result.failure(Exception("网络错误"))
        } catch (e: Exception) {
            Result.failure(Exception("未知错误"))
        }
    }

    suspend fun passwdChange(email: String, username: String, newPassword: String, token: String): Result<String> {
        return try {
            val request = PasswdchangeRequest(email, username, newPassword, token)
            val response = apiService.passwdChange(request)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.code == 200) {
                    Result.success("true") // 成功时返回 "true"
                } else {
                    Result.failure(Exception(body?.message ?: "未知错误"))
                }
            } else {
                val bodyString = response.errorBody()?.string() ?: "{}"
                val jsonObject = JSONObject(bodyString)
                val message = jsonObject.optString("message", "未知错误")
                Result.failure(Exception(message))
            }
        } catch (e: IOException) {
            Result.failure(Exception("网络错误"))
        } catch (e: HttpException) {
            Result.failure(Exception("服务器错误"))
        } catch (e: Exception) {
            Result.failure(Exception("密码重置失败"))
        }
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
                if (body != null && body.status == "true") {
                    // 创建或更新 User 对象
                    val user = User(
                        id = body.user?.id ?: 0, // Int 类型，与后端一致
                        username = account,
                        password = password,
                        email = body.user?.email ?: ""
                        // 注意：User 没有 avatarUrl 字段，后端返回的 avatarUrl 将用于 UserInfo
                    )

                    // 更新或插入 User
                    val existingUser = userDao.getUserByUsername(account)
                    if (existingUser != null) {
                        val updatedUser = user.copy(id = existingUser.id)
                        userDao.update(updatedUser)
                    } else {
                        userDao.insert(user)
                    }

                    // 获取用户 ID
                    val userId = userDao.getUserByUsername(account)?.id
                        ?: return Result.failure(Exception("无法获取用户 ID"))

                    // 创建或更新 UserInfo 对象
                    val userInfo = UserInfo(
                        id = userId, // Int 类型，无需转换
                        displayName = body.userInfo?.displayName ?: body.user?.username,
                        email = body.userInfo?.email ?: body.user?.email ?: "",
                        avatarUrl = body.userInfo?.avatarUrl, // 从 user 中获取 avatarUrl
                        bio = body.userInfo?.bio,
                        gender = body.userInfo?.gender ?: "unspecified",
                        birthDate = body.userInfo?.birthDate?.let {
                            LocalDate.parse(it.substring(0, 10))
                        },
                        phoneNumber = body.userInfo?.phoneNumber,
                        location = body.userInfo?.location,
                        lastLoginTime = Instant.now().toString()
                    )

                    // 更新或插入 UserInfo
                    val existingUserInfo = userInfoDao.getUserInfoById(userId)
                    if (existingUserInfo != null) {
                        userInfoDao.update(userInfo)
                    } else {
                        userInfoDao.insert(userInfo)
                    }

                    // 保存令牌
                    saveTokens(userId, body.accessToken ?: "", body.refreshToken ?: "")
                    Result.success(body)
                } else {
                    Result.failure(Exception("服务器返回无效数据"))
                }
            } else {
                val bodyString = response.errorBody()?.string() ?: "{}"
                val jsonObject = JSONObject(bodyString)
                val message = jsonObject.optString("message", "未知错误")
                Result.failure(Exception(message))
            }
        } catch (e: Exception) {
            Log.e("UserRepository", "Login failed")
            Result.failure(Exception("网络错误"))
        }
    }

    private fun saveTokens(userId: Int, accessToken: String, refreshToken: String) {
        TokenManager.saveTokens(context, userId, accessToken, refreshToken)
        Log.d("UserRepository", "Tokens saved: userId=$userId, accessToken=$accessToken, refreshToken=$refreshToken")
    }


    private fun isValidLoginResponse(response: LoginResponse): Boolean {
        return response.status == "true" &&
                !response.accessToken.isNullOrBlank() &&
                !response.refreshToken.isNullOrBlank()
    }

}