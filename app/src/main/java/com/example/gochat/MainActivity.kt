package com.example.gochat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gochat.data.ApiService
import com.example.gochat.data.repository.refreshAccessToken
import com.example.gochat.databinding.ActivityMainBinding
import com.example.gochat.ui.main.HomeActivity
import com.example.gochat.ui.user.PasswdForgotActivity
import com.example.gochat.ui.user.RegisterActivity
import com.example.gochat.utils.LoadingUtil // 添加导入
import com.example.gochat.utils.TokenManager
import com.example.gochat.utils.setDebounceClickListener
import com.example.gochat.viewmodel.LoginState
import com.example.gochat.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: LoginViewModel by viewModel()
    private val apiService: ApiService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            val status = checkLoginStatus()
            Log.d("MainActivity", "Login status: $status")
            when (status) {
                LoginStatus.LOGGED_IN -> {
                    Log.d("MainActivity", "Starting HomeActivity")
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    finish()
                    return@launch
                }
                LoginStatus.REFRESH_FAILED -> {
                    Toast.makeText(this@MainActivity, "请重新登录", Toast.LENGTH_SHORT).show()
                }
                LoginStatus.NOT_LOGGED_IN -> {}
            }

            // 设置按钮监听器
            binding.tvRegister.setDebounceClickListener {
                startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
            }
            binding.tvForgotPassword.setDebounceClickListener {
                startActivity(Intent(this@MainActivity, PasswdForgotActivity::class.java))
            }

            // 观察登录状态
            viewModel.loginState.observe(this@MainActivity) { state ->
                when (state) {
                    is LoginState.Success -> {
                        LoadingUtil.hideLoading(this@MainActivity)
                        Toast.makeText(this@MainActivity, "登录成功", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                        finish()
                    }
                    is LoginState.Error -> {
                        LoadingUtil.hideLoading(this@MainActivity)
                        Toast.makeText(this@MainActivity, "登录失败: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                    is LoginState.Loading -> {
                        // 动画已在按钮点击时显示，这里无需重复操作
                    }
                }
            }

            setupListeners()
        }
    }

    private fun setupListeners() {
        binding.btnLogin.setDebounceClickListener {
            val account = binding.etaccount.text.toString().trim()
            val passwd = binding.etpasswd.text.toString().trim()

            if (passwd.length < 6) {
                Toast.makeText(this, "密码长度不能少于6位", Toast.LENGTH_SHORT).show()
                return@setDebounceClickListener
            }

            LoadingUtil.showLoading(this)
            viewModel.login(account, passwd)
        }
    }

    private suspend fun checkLoginStatus(): LoginStatus {
        val accessToken = TokenManager.getAccessToken(this)
        val refreshToken = TokenManager.getRefreshToken(this)
        val userId = TokenManager.getUserId(this)

        Log.d("MainActivity", "Checking tokens: accessToken=$accessToken, refreshToken=$refreshToken, userId=$userId")

        if (accessToken == null && refreshToken == null && userId == null) {
            Log.d("MainActivity", "No tokens saved")
            return LoginStatus.NOT_LOGGED_IN
        }

        if (accessToken != null && TokenManager.isTokenValid(accessToken)) {
            Log.d("MainActivity", "Valid access token found")
            return LoginStatus.LOGGED_IN
        }

        if (refreshToken != null && refreshAccessToken(this, apiService)) {
            Log.d("MainActivity", "Refresh successful")
            return LoginStatus.LOGGED_IN
        }

        return if (refreshToken != null) {
            Log.d("MainActivity", "Refresh failed")
            LoginStatus.REFRESH_FAILED
        } else {
            Log.d("MainActivity", "No refresh token available")
            LoginStatus.NOT_LOGGED_IN
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LoadingUtil.hideLoading(this)
    }
}

enum class LoginStatus {
    LOGGED_IN,
    NOT_LOGGED_IN,
    REFRESH_FAILED
}