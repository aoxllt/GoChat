package com.example.gochat.ui.user

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gochat.databinding.ActivityPasswdforgotBinding
import com.example.gochat.databinding.ActivityRegisterBinding
import com.example.gochat.viewmodel.PasswdforgotViewModel
import com.example.myapp.ui.viewmodel.RegisterViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class PasswdForgotActivity: AppCompatActivity() {
    private lateinit var binding: ActivityPasswdforgotBinding
    private val viewModel: PasswdforgotViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswdforgotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        val username = binding.etUsername.text.toString().trim() // 获取用户名
        val email = binding.email.text.toString().trim()       // 获取邮箱
        binding.btnConfirm.setOnClickListener {
            // 检查用户名长度和邮箱格式
            if (username.length < 4 || !isValidEmail(email)) {
                Toast.makeText(this, "用户名小于四位或邮箱格式错误", Toast.LENGTH_SHORT).show()
            }
            // 调用 ViewModel 的 passwdForgot 方法
            viewModel.passwdForgot(username, email) { result ->
                if (result == "true") {
                    Toast.makeText(this, "验证成功", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, PasswdChangeActivity::class.java)
                    intent.putExtra("email", email)
                    intent.putExtra("username", username)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, result + "请重试", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
