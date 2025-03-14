package com.example.myapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gochat.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userRepository: UserRepository,
    private val deviceId: String
) : ViewModel() {
    // 表示是否正在处理注册请求
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> get() = _isProcessing

    // 表示注册结果
    private val _result = MutableStateFlow<Boolean?>(null)
    val result: StateFlow<Boolean?> get() = _result

    fun register(email: String) {
        if (_isProcessing.value) return // 防止重复请求

        viewModelScope.launch {
            _isProcessing.value = true
            val isSuccess = userRepository.register(deviceId, email)
            _result.value = isSuccess
            _isProcessing.value = false
        }
    }
}