package com.example.gochat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gochat.data.ApiService
import com.example.gochat.data.repository.refreshAccessToken
import com.example.gochat.databinding.ActivityMainBinding
import com.example.gochat.ui.main.HomeActivity
import com.example.gochat.ui.user.CaptchActivity
import com.example.gochat.ui.user.PasswdForgotActivity
import com.example.gochat.ui.user.RegisterActivity
import com.example.gochat.ui.user.UserinfoaddActivity
import com.example.gochat.utils.TokenManager
import com.example.gochat.utils.setDebounceClickListener
import com.example.gochat.viewmodel.LoginState
import com.example.gochat.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

import kotlin.jvm.java

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: LoginViewModel by viewModel()
    private val apiService: ApiService by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvRegister.setDebounceClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        binding.tvForgotPassword.setDebounceClickListener {
            startActivity(Intent(this, PasswdForgotActivity::class.java))
        }

        lifecycleScope.launch {
            val status = checkLoginStatus()
            Log.d("MainActivity", "Login status: $status")
            when (status) {
                LoginStatus.LOGGED_IN -> {
                    Log.d("MainActivity", "Starting HomeActivity")
                    startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                    Log.d("MainActivity", "Finishing MainActivity")
                    finish()
                    return@launch
                }
                LoginStatus.REFRESH_FAILED -> {
                    Toast.makeText(this@MainActivity, "请重新登录", Toast.LENGTH_SHORT).show()
                }
                LoginStatus.NOT_LOGGED_IN -> {}
            }

            binding.tvRegister.setDebounceClickListener {
                startActivity(Intent(this@MainActivity, RegisterActivity::class.java))
            }
            binding.tvForgotPassword.setDebounceClickListener {
                startActivity(Intent(this@MainActivity, PasswdForgotActivity::class.java))
            }

            viewModel.loginState.observe(this@MainActivity) { state ->
                when (state) {
                    is LoginState.Success -> {
                        Toast.makeText(this@MainActivity, "登录成功", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                        finish()
                    }
                    is LoginState.Error -> {
                        Toast.makeText(this@MainActivity, "登录失败: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                    is LoginState.Loading -> {}
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
                Toast.makeText(this, "密码小于六位", Toast.LENGTH_SHORT).show()
                return@setDebounceClickListener
            }

            // 正确传入 apiService
            viewModel.login(account, passwd)
        }
    }

    private suspend fun checkLoginStatus(): LoginStatus {
        val accessToken = TokenManager.getAccessToken(this)
        val refreshToken = TokenManager.getRefreshToken(this)
        val userId = TokenManager.getUserId(this)

        // 日志检查
        Log.d("MainActivity", "Checking tokens: accessToken=$accessToken, refreshToken=$refreshToken, userId=$userId")

        // 检查令牌是否保存
        if (accessToken == null && refreshToken == null && userId == null) {
            Log.d("MainActivity", "No tokens saved in package")
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
}


enum class LoginStatus {
    LOGGED_IN,
    NOT_LOGGED_IN,
    REFRESH_FAILED
}