package com.example.gochat.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.gochat.api.UseraddRequest
import com.example.gochat.data.repository.UserRepository

class UseraddViewModel(private val userRepository: UserRepository
) : ViewModel() {

    // 检查用户名是否可用，返回字符串结果
    suspend fun checkUsername(username: String): String {
        return userRepository.checkname(username)
    }
    suspend fun saveUserInfo(
        request: UseraddRequest,
        avatarUri: Uri?,
        contentResolver: ContentResolver
    ): String {
        return userRepository.saveUserInfo(request, avatarUri, contentResolver)
    }
}