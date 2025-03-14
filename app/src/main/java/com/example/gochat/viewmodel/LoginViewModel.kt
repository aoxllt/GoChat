package com.example.gochat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gochat.api.LoginResponse
import com.example.gochat.data.database.dao.UserDao
import com.example.gochat.data.database.entity.User
import com.example.gochat.data.database.entity.enums.UserStatus
import com.example.gochat.data.repository.UserRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginViewModel(
    private val userRepository: UserRepository,
    private val userDao: UserDao
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState

    fun login(account: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            // 本地验证（明文比较）
            val localUser = userDao.getUserByUsername(account)
            if (localUser != null && localUser.password == password) {
                _loginState.value = LoginState.Success(localUser)
                return@launch
            }

            // 后端验证
            val result = userRepository.login(account, password)
            when {
                result.isSuccess -> {
                    val response = result.getOrNull()!!
                    if (response.status == "true") {
                        val user = User(
                            id = response.user?.id ?: 0,
                            username = account,
                            password = password,
                            email = response.user?.email ?: "",
                            avatarUrl = response.user?.avatarUrl,
                            createdAt = System.currentTimeMillis(),
                            status = UserStatus.ACTIVE
                        )
                        userDao.insert(user)
                        _loginState.value = LoginState.Success(user)
                    } else {
                        _loginState.value = LoginState.Error(response.message ?: "Invalid response")
                    }
                }
                result.isFailure -> {
                    val errorMsg = result.exceptionOrNull()?.message ?: "Unknown error"
                    _loginState.value = LoginState.Error(errorMsg)
                }
            }
        }
    }

    fun isLoggedIn(): Boolean {
        val latestToken = runBlocking { userRepository.getLatestToken() }
        return latestToken?.accessToken?.isNotBlank() == true &&
                latestToken.accessTokenExpiresAt?.let { it > System.currentTimeMillis() } ?: false
    }
}

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}