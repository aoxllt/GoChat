package com.example.gochat.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gochat.data.database.dao.UserDao
import com.example.gochat.data.database.dao.UserInfoDao
import com.example.gochat.data.repository.UserRepository
import com.example.gochat.utils.TokenManager
import kotlinx.coroutines.launch

class LoginViewModel(
    private val userRepository: UserRepository,
    private val userDao: UserDao,
    private val userInfoDao: UserInfoDao,
    private val context: Context
) : ViewModel() {

    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> get() = _loginState

    fun login(account: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            // 本地验证
            val localUser = userDao.getUserByUsername(account)
            val token = TokenManager.getAccessToken(context)
            if (token!=null&&localUser != null && localUser.password == password) {
                _loginState.value = LoginState.Success(localUser.id)
                return@launch
            }

            // 后端验证
            val result = userRepository.login(account, password)
            when {
                result.isSuccess -> {
                    val response = result.getOrNull()!!
                    if (response.status == "true") {
                        _loginState.value = LoginState.Success(response.user!!.id)
                    } else {
                        _loginState.value = LoginState.Error(response.message ?: "登录失败")
                    }
                }
                result.isFailure -> {
                    val errorMsg = result.exceptionOrNull()?.message ?: "未知错误"
                    _loginState.value = LoginState.Error(errorMsg)
                }
            }
        }
    }
}

sealed class LoginState {
    object Loading : LoginState()
    data class Success(val userId: Int) : LoginState()
    data class Error(val message: String) : LoginState()
}