package com.example.gochat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gochat.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PasswdforgotViewModel(private val userRepository: UserRepository) : ViewModel() {
    // 表示是否正在处理请求
    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> get() = _isProcessing

    // 表示发送验证码的结果
    private val _sendCodeResult = MutableStateFlow<String?>(null)
    val sendCodeResult: StateFlow<String?> get() = _sendCodeResult

    // 表示验证结果
    private val _result = MutableStateFlow<Result<String>?>(null)
    val result: StateFlow<Result<String>?> = _result.asStateFlow()

    fun sendVerificationCode(username: String, email: String) {
        if (_isProcessing.value) return

        viewModelScope.launch {
            _isProcessing.value = true
            val result = userRepository.sendVerificationCode(username, email)
            result.onSuccess {
                _sendCodeResult.value = "验证码已发送"
            }.onFailure { exception ->
                _sendCodeResult.value = exception.message
            }
            _isProcessing.value = false
        }
    }

    fun passwdForgot(username: String, email: String, code: String) {
        if (_isProcessing.value) return
        viewModelScope.launch {
            _isProcessing.value = true
            val result = userRepository.passwdForgot(username, email, code)
            _result.value = result // 直接传递 Result 对象
            _isProcessing.value = false
        }
    }
}