package com.example.gochat.ui.user

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gochat.MainActivity
import com.example.gochat.databinding.ActivityRegisterBinding
import com.example.gochat.utils.LoadingUtil
import com.example.gochat.utils.setDebounceClickListener
import com.example.myapp.ui.viewmodel.RegisterState
import com.example.myapp.ui.viewmodel.RegisterViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    /**
     * 初始化 UI 事件
     */
    private fun initUI() {
        // 返回按钮点击事件（添加防抖）
        binding.backbtn.setDebounceClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 注册按钮点击事件（添加防抖）
        binding.regbtn.setDebounceClickListener {
            registerUser()
        }

        // 观察 ViewModel 的状态
        lifecycleScope.launch {
            viewModel.isProcessing.collect { isProcessing ->
                binding.regbtn.isEnabled = !isProcessing // 在处理请求时禁用按钮
                if (isProcessing) {
                    LoadingUtil.showLoading(this@RegisterActivity) // 显示加载动画
                } else {
                    LoadingUtil.hideLoading(this@RegisterActivity) // 隐藏加载动画
                }
            }
        }

        lifecycleScope.launch {
            viewModel.registerState.collect { state ->
                when (state) {
                    is RegisterState.Idle -> {
                        // 初始状态，无需特别处理
                    }
                    is RegisterState.Loading -> {
                        // 正在处理，UI 已通过 isProcessing 控制按钮状态
                    }
                    is RegisterState.Success -> {
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_SHORT).show()
                        navigateToCaptchaActivity(binding.mailInput.text.toString().trim())
                    }
                    is RegisterState.Error -> {
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    /**
     * 处理用户注册逻辑
     */
    private fun registerUser() {
        val email = binding.mailInput.text.toString().trim()

        // 验证邮箱
        if (email.isEmpty()) {
            Toast.makeText(this, "邮箱不能为空", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, "邮箱格式不正确", Toast.LENGTH_SHORT).show()
            return
        }

        // 调用 ViewModel 进行注册
        viewModel.register(email)
    }

    /**
     * 验证邮箱格式
     */
    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * 跳转到验证码页面
     */
    private fun navigateToCaptchaActivity(email: String) {
        val intent = Intent(this, CaptchActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(intent)
    }
}