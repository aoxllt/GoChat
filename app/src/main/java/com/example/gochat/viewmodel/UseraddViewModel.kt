package com.example.gochat.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gochat.api.LoginResponse
import com.example.gochat.api.UseraddRequest
import com.example.gochat.data.repository.UserRepository
import kotlinx.coroutines.launch

class UseraddViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> get() = _registerState

    suspend fun checkUsername(username: String): String {
        return userRepository.checkUsername(username)
    }

    fun saveUserInfo(
        request: UseraddRequest,
        avatarUri: Uri?,
        contentResolver: ContentResolver
    ) {
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            val result = userRepository.saveUserInfo(request, avatarUri, contentResolver)
            when {
                result.isSuccess -> {
                    val response = result.getOrNull()
                    _registerState.value = RegisterState.Success(response?.user?.username ?: request.username)
                }
                result.isFailure -> {
                    val exception = result.exceptionOrNull()
                    _registerState.value = RegisterState.Error(exception?.message ?: "未知错误")
                }
            }
        }
    }
}

sealed class RegisterState {
    object Loading : RegisterState()
    data class Success(val username: String) : RegisterState()
    data class Error(val message: String) : RegisterState()
}