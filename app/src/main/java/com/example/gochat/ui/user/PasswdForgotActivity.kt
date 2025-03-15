package com.example.gochat.ui.user

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gochat.databinding.ActivityPasswdforgotBinding
import com.example.gochat.utils.LoadingUtil
import com.example.gochat.utils.setDebounceClickListener
import com.example.gochat.viewmodel.PasswdforgotViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PasswdForgotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswdforgotBinding
    private val viewModel: PasswdforgotViewModel by viewModel()
    private var countDownTimer: CountDownTimer? = null // 跟踪倒计时对象

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswdforgotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        // 观察处理状态
        lifecycleScope.launch {
            viewModel.isProcessing.collect { isProcessing ->
                // 只有在倒计时未运行时才根据 isProcessing 更新 btnSendCode 的状态
                if (countDownTimer == null) {
                    binding.btnSendCode.isEnabled = !isProcessing
                }
                binding.btnConfirm.isEnabled = !isProcessing
                if (isProcessing) {
                    LoadingUtil.showLoading(this@PasswdForgotActivity)
                } else {
                    LoadingUtil.hideLoading(this@PasswdForgotActivity)
                }
            }
        }

        // 观察发送验证码结果
        lifecycleScope.launch {
            viewModel.sendCodeResult.collect { result ->
                result?.let {
                    if (it == "验证码已发送") {
                        Toast.makeText(this@PasswdForgotActivity, it, Toast.LENGTH_SHORT).show()
                        startCountdown()
                    } else {
                        Toast.makeText(this@PasswdForgotActivity, "$it 请重试", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.result.collect { result ->
                result?.let {
                    it.onSuccess { token ->
                        Toast.makeText(this@PasswdForgotActivity, "验证成功", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@PasswdForgotActivity, PasswdChangeActivity::class.java)
                        intent.putExtra("email", binding.email.text.toString().trim())
                        intent.putExtra("username", binding.etUsername.text.toString().trim())
                        intent.putExtra("token", token)
                        startActivity(intent)
                        finish()
                    }.onFailure { exception ->
                        Toast.makeText(this@PasswdForgotActivity, "${exception.message} 请重试", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 确认按钮点击事件
        binding.btnConfirm.setDebounceClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val code = binding.etVerificationCode.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || code.isEmpty()) {
                Toast.makeText(this, "用户名、邮箱和验证码不能为空", Toast.LENGTH_SHORT).show()
                return@setDebounceClickListener
            }
            viewModel.passwdForgot(username, email, code)
        }

        // 发送验证码按钮点击事件
        binding.btnSendCode.setDebounceClickListener {
            val email = binding.email.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()

            if (username.length < 4 || !isValidEmail(email)) {
                Toast.makeText(this, "用户名小于四位或邮箱格式错误", Toast.LENGTH_SHORT).show()
                return@setDebounceClickListener
            }

            viewModel.sendVerificationCode(username, email)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun startCountdown() {
        binding.btnSendCode.isEnabled = false // 禁用按钮
        countDownTimer?.cancel() // 取消之前的倒计时（如果存在）
        countDownTimer = object : CountDownTimer(60000, 1000) { // 60秒倒计时
            override fun onTick(millisUntilFinished: Long) {
                binding.btnSendCode.text = "重新发送 (${millisUntilFinished / 1000}s)"
            }

            override fun onFinish() {
                binding.btnSendCode.text = "发送验证码"
                binding.btnSendCode.isEnabled = true // 倒计时结束后启用按钮
                countDownTimer = null // 重置倒计时对象
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel() // 取消倒计时
        countDownTimer = null
        LoadingUtil.hideLoading(this)
    }
}