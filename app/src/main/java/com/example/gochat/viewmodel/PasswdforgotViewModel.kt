package com.example.gochat.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gochat.data.repository.UserRepository
import kotlinx.coroutines.launch

class PasswdforgotViewModel(private val userRepository: UserRepository,): ViewModel() {
   fun passwdForgot(username: String,email: String,onResult:(String)-> Unit){
        viewModelScope.launch {
            var result=userRepository.passwdForgot(username,email)
            onResult(result)
        }
    }
}