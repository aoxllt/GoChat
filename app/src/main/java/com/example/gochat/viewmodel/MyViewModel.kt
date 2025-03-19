package com.example.gochat.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gochat.config.config
import com.example.gochat.data.database.entity.UserInfo
import com.example.gochat.data.repository.UserInfoRepository
import com.example.gochat.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import com.bumptech.glide.Glide
import com.example.gochat.data.database.dao.UserInfoDao
import kotlinx.coroutines.withContext

class MyViewModel(
    private val repository: UserInfoRepository,
    private val userInfoDao: UserInfoDao,
    private val context: Context
) : ViewModel() {

    val backendUrl: String = config.BACKEND_URL

    private val _userInfo = MutableStateFlow<UserInfo?>(null)
    val userInfo: StateFlow<UserInfo?> = _userInfo

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    init {
        // 监听数据库变化
        viewModelScope.launch {
            userInfoDao.getUserInfoFlow(getUserId()).collect { userInfo ->
                _userInfo.value = userInfo
            }
        }
    }

    fun getUserId(): Int = TokenManager.getUserId(context)
    fun getCurrentAvatarUrl(): String? = _userInfo.value?.avatarUrl

    fun fetchUserProfile() {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            val token = TokenManager.getAccessToken(context)
            val userId = getUserId()

            if (token == null || userId == 0) {
                _error.value = "用户信息获取失败：未登录或 ID 无效"
                _loading.value = false
                return@launch
            }

            val result = repository.fetchUserProfile("$token", userId)
            _loading.value = false
            result.onFailure { exception ->
                _error.value = exception.message ?: "网络错误，请检查连接"
            }
            // 本地数据通过 Flow 自动更新 UI，无需手动设置
        }
    }

    fun saveUserProfile(userInfo: UserInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            _loading.value = true
            val token = TokenManager.getAccessToken(context)
            if (token == null) {
                _error.value = "保存失败：未登录"
                _loading.value = false
                return@launch
            }

            val result = repository.saveUserProfile("$token", userInfo)
            _loading.value = false
            result.onSuccess { response ->
                _saveSuccess.value = true
                _error.value = null
            }.onFailure { exception ->
                _error.value = exception.message ?: "网络错误，请稍后重试"
                _saveSuccess.value = false
            }
        }
    }

    suspend fun downloadAndSaveAvatar(url: String, relativePath: String): File? {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("MyViewModel", "Downloading from: $url")
                val file = Glide.with(context)
                    .asFile()
                    .load(url)
                    .submit()
                    .get()
                val localFile = File(context.filesDir, relativePath.trimStart('/'))
                val parentDir = localFile.parentFile
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs()
                    Log.d("MyViewModel", "Created directory: $parentDir")
                }
                file.copyTo(localFile, overwrite = true)
                file.delete()
                Log.d("MyViewModel", "Saved to: $localFile")
                localFile
            } catch (e: Exception) {
                Log.e("MyViewModel", "Download failed: $e")
                null
            }
        }
    }
}