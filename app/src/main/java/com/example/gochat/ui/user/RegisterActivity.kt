package com.example.gochat.ui.user

import android.R.attr.button
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gochat.MainActivity
import com.example.gochat.databinding.ActivityRegisterBinding
import com.example.gochat.utils.GetDevId

import com.example.myapp.ui.viewmodel.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化 UI 事件
        initUI()
    }

    /**
     * 初始化 UI 事件
     */
    private fun initUI() {
        // 返回按钮点击事件
        binding.backbtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 注册按钮点击事件
        binding.regbtn.setOnClickListener {
            registerUser()
        }
    }

    /**
     * 处理用户注册逻辑
     */
    private fun registerUser() {
        val email = binding.mailInput.text.toString().trim()

        // 验证邮箱是否为空
        if (email.isEmpty()) {
            Toast.makeText(this, "邮箱不能为空", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, "邮箱格式不正确", Toast.LENGTH_SHORT).show()
            return
        }
        binding.regbtn.isEnabled = false

        // 调用 ViewModel 进行注册
        viewModel.register(email) { result ->
            // 如果注册成功，跳转到验证码页面
            if (result.toString().startsWith("true")) {
                Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show()
                navigateToCaptchaActivity(email)
            } else {
                Toast.makeText(this, "服务器错误，请稍后再尝试", Toast.LENGTH_SHORT).show()
            }
            // 延迟 1 秒后启用按钮
            Handler(Looper.getMainLooper()).postDelayed({
                binding.regbtn.isEnabled = false
            }, 1000) // 1000 毫秒 = 1 秒
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * 跳转到验证码页面
     * @param email 用户邮箱
     */
    private fun navigateToCaptchaActivity(email: String) {
        val intent = Intent(this, CaptchActivity::class.java).apply {
            putExtra("email", email)
        }
        startActivity(intent)
    }

}