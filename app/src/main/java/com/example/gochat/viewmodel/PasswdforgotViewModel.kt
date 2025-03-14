package com.example.gochat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gochat.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PasswdforgotViewModel(private val userRepository: UserRepository) : ViewModel() {
    // 表示是否正在处理请求
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> get() = _isProcessing

    // 表示操作结果
    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> get() = _result

    fun passwdForgot(username: String, email: String) {
        if (_isProcessing.value) return // 防止重复请求

        viewModelScope.launch {
            _isProcessing.value = true
            val response = userRepository.passwdForgot(username, email)
            _result.value = response
            _isProcessing.value = false
        }
    }
}