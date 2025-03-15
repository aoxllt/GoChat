package com.example.myapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gochat.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.Result // 导入 Kotlin Result

class RegisterViewModel(
    private val userRepository: UserRepository,
    private val deviceId: String
) : ViewModel() {
    // 表示是否正在处理注册请求
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> get() = _isProcessing

    // 表示注册结果，包含消息
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> get() = _registerState

    fun register(email: String) {
        if (_isProcessing.value) return // 防止重复请求

        viewModelScope.launch {
            _isProcessing.value = true
            _registerState.value = RegisterState.Loading

            val result = userRepository.register(deviceId, email)
            when {
                result.isSuccess -> {
                    // 假设 register 返回 Result<Boolean>，成功时返回具体消息
                    _registerState.value = RegisterState.Success("注册成功")
                }
                result.isFailure -> {
                    val errorMessage = result.exceptionOrNull()?.message ?: "注册失败，请稍后重试"
                    _registerState.value = RegisterState.Error(errorMessage)
                }
            }

            _isProcessing.value = false
        }
    }
}

// 注册状态的密封类，带消息
sealed class RegisterState {
    object Idle : RegisterState()              // 初始状态
    object Loading : RegisterState()           // 正在注册
    data class Success(val message: String) : RegisterState() // 注册成功，带消息
    data class Error(val message: String) : RegisterState()   // 注册失败，带消息
}