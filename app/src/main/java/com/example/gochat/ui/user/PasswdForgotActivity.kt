package com.example.gochat.ui.user

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gochat.databinding.ActivityPasswdforgotBinding
import com.example.gochat.utils.setDebounceClickListener
import com.example.gochat.viewmodel.PasswdforgotViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PasswdForgotActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswdforgotBinding
    private val viewModel: PasswdforgotViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswdforgotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI() // 初始化UI，不需要提前传入username和email
    }

    private fun initUI() {
        lifecycleScope.launch {
            viewModel.isProcessing.collect { isProcessing ->
                binding.btnConfirm.isEnabled = !isProcessing // 可选：禁用按钮
            }
        }

        // 观察结果
        lifecycleScope.launch {
            viewModel.result.collect { result ->
                result?.let {
                    if (it == "true") {
                        Toast.makeText(this@PasswdForgotActivity, "验证成功", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@PasswdForgotActivity, PasswdChangeActivity::class.java)
                        intent.putExtra("email", binding.email.text.toString().trim())
                        intent.putExtra("username", binding.etUsername.text.toString().trim())
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@PasswdForgotActivity, "$it 请重试", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding.btnConfirm.setDebounceClickListener {
            // 在点击按钮时获取最新的输入值
            val username = binding.etUsername.text.toString().trim()
            val email = binding.email.text.toString().trim()

            // 检查用户名长度和邮箱格式
            if (username.length < 4 || !isValidEmail(email)) {
                Toast.makeText(this, "用户名小于四位或邮箱格式错误", Toast.LENGTH_SHORT).show()
                return@setDebounceClickListener
            }

            // 调用 ViewModel 的 passwdForgot 方法
            viewModel.passwdForgot(username, email)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}