package com.example.gochat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gochat.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PasswdchangeViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> get() = _isProcessing

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> get() = _result

    fun passwdChange(email: String, username: String, newPassword: String, token: String) {
        if (_isProcessing.value) return // 防止重复请求

        viewModelScope.launch {
            _isProcessing.value = true
            val result = userRepository.passwdChange(email, username, newPassword, token)
            result.onSuccess { value ->
                _result.value = value // "true" 表示成功
            }.onFailure { exception ->
                _result.value = exception.message // 错误消息
            }
            _isProcessing.value = false
        }
    }
}