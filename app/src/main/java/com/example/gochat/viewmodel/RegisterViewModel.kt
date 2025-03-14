package com.example.myapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gochat.data.repository.UserRepository
import kotlinx.coroutines.launch

class RegisterViewModel(private val userRepository: UserRepository, private val deviceId: String) : ViewModel() {
    fun register(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isSuccess = userRepository.register(deviceId, email)
            onResult(isSuccess)
        }
    }
}