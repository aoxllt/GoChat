package com.example.gochat.ui.user

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gochat.MainActivity
import com.example.gochat.databinding.ActivityRegisterBinding
import com.example.gochat.utils.setDebounceClickListener // 假设你已有此扩展函数
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
            }
        }

        lifecycleScope.launch {
            viewModel.result.collect { result ->
                result?.let {
                    if (it) { // Boolean 类型，true 表示成功
                        Toast.makeText(this@RegisterActivity, "验证码已发送", Toast.LENGTH_SHORT).show()
                        navigateToCaptchaActivity(binding.mailInput.text.toString().trim())
                    } else {
                        Toast.makeText(this@RegisterActivity, "服务器错误，请稍后再尝试", Toast.LENGTH_SHORT).show()
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