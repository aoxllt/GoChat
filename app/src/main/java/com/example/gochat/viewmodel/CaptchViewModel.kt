package com.example.gochat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gochat.data.repository.UserRepository
import kotlinx.coroutines.launch

class CaptchViewModel(
    private val userRepository: UserRepository,
    private val deviceId: String
) : ViewModel() {

    fun verify(email: String, captch: String ,onResult: (String) -> Unit) {
        viewModelScope.launch {
            val isSuccess = userRepository.verify(email,deviceId,captch)
            onResult(isSuccess)
        }
    }
}