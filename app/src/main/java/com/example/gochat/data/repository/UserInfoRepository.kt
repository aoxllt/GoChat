package com.example.gochat.data.repository

import android.content.Context
import android.util.Log
import com.example.gochat.api.Respons
import com.example.gochat.data.ApiService
import com.example.gochat.data.database.dao.UserInfoDao
import com.example.gochat.data.database.entity.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UserInfoRepository(
    private val context: Context,
    private val apiService: ApiService,
    private val userInfoDao: UserInfoDao
) {

    companion object {
        private const val TAG = "UserInfoRepository"
    }

    suspend fun fetchUserProfile(token: String, userId: Int): Result<UserInfo?> {
        return try {
            // Step 1: 获取本地缓存数据
            val cachedUser = userInfoDao.getUserInfoById(userId)
            Log.d(TAG, "Fetched cached user from DB: $cachedUser")

            // Step 2: 异步从后端更新数据
            withContext(Dispatchers.IO) {
                try {
                    Log.d(TAG, "Fetching user profile from backend with token: $token, userId: $userId")
                    val response = apiService.getUserProfile(token)
                    Log.d(TAG, "Response code: ${response.code()}")

                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        if (responseBody != null) {
                            val jsonString = responseBody.string()
                            Log.d(TAG, "Raw JSON response: $jsonString")

                            val jsonObject = JSONObject(jsonString)
                            val userInfo = parseUserInfoFromJson(jsonObject, userId)
                            Log.d(TAG, "Parsed UserInfo: $userInfo")

                            userInfoDao.insertOrUpdate(userInfo)
                            Log.d(TAG, "UserInfo saved to DB")
                        } else {
                            Log.w(TAG, "Response body is null")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string() ?: "No error body"
                        Log.e(TAG, "Failed to fetch profile, code: ${response.code()}, error: $errorBody")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching profile from backend: $e")
                }
            }

            // Step 3: 返回本地数据
            Result.success(cachedUser)
        } catch (e: Exception) {
            Log.e(TAG, "Exception in fetchUserProfile: $e")
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(token: String, userInfo: UserInfo): Result<Respons?> {
        return try {
            Log.d(TAG, "Saving user profile with token: $token, userInfo: $userInfo")
            val response = apiService.updateUserProfile(token, userInfo)

            if (response.isSuccessful) {
                val updatedUserInfo = userInfo.copy(lastLoginTime = Instant.now().toString())
                userInfoDao.insertOrUpdate(updatedUserInfo)
                val responseBody = response.body()
                Log.d(TAG, "Profile saved successfully, response: $responseBody")
                Result.success(responseBody)
            } else {
                val errorBody = response.errorBody()?.string() ?: "{}"
                val jsonObject = JSONObject(errorBody)
                val message = jsonObject.optString("message", "未知错误")
                Log.e(TAG, "Failed to save profile, code: ${response.code()}, error: $message")
                Result.failure(Exception("保存失败: $message"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in saveUserProfile: $e")
            Result.failure(e)
        }
    }

    private fun parseUserInfoFromJson(jsonObject: JSONObject, userId: Int): UserInfo {
        fun String?.toDefault(default: String, invalidValues: List<String> = listOf("unspecified", "null")): String {
            return if (this.isNullOrEmpty() || this in invalidValues) default else this
        }
        fun String?.toNullableDefault(invalidValues: List<String> = listOf("null")): String? {
            return if (this.isNullOrEmpty() || this in invalidValues) null else this
        }

        return UserInfo(
            id = jsonObject.optInt("ID", userId),
            displayName = jsonObject.optString("DisplayName").toDefault("未设置昵称"),
            email = jsonObject.optString("Email").toDefault(""),
            avatarUrl = jsonObject.optString("AvatarURL").toNullableDefault(),
            bio = jsonObject.optString("Bio").toDefault("这个人很懒，什么都没写"),
            gender = jsonObject.optString("Gender").toDefault("未知"),
            birthDate = jsonObject.optString("BirthDate").toNullableDefault()?.let { birthStr ->
                try {
                    LocalDate.parse(birthStr, DateTimeFormatter.ISO_LOCAL_DATE)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to parse BirthDate: $birthStr, error: $e")
                    null
                }
            },
            phoneNumber = jsonObject.optString("PhoneNumber").toNullableDefault(),
            location = jsonObject.optString("Location").toNullableDefault(),
            lastLoginTime = Instant.now().toString()
        )
    }
}