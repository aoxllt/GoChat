package com.example.gochat.ui.user

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gochat.MainActivity
import com.example.gochat.databinding.ActivityPasswdchangeBinding
import com.example.gochat.ui.user.RegisterActivity
import com.example.gochat.utils.LoadingUtil
import com.example.gochat.utils.setDebounceClickListener
import com.example.gochat.viewmodel.PasswdchangeViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PasswdChangeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPasswdchangeBinding
    private val viewModel: PasswdchangeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPasswdchangeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
    }

    private fun initUI() {
        // 从 Intent 中获取 username、email 和 token
        val username = intent.getStringExtra("username") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val token = intent.getStringExtra("token") ?: "" // 从 Passwdforgot 获取

        lifecycleScope.launch {
            viewModel.isProcessing.collect { isProcessing ->
                binding.btnConfirm.isEnabled = !isProcessing
                if (isProcessing) {
                    LoadingUtil.showLoading(this@PasswdChangeActivity) // 显示加载动画
                } else {
                    LoadingUtil.hideLoading(this@PasswdChangeActivity) // 隐藏加载动画
                }
            }
        }

        // 观察结果
        lifecycleScope.launch {
            viewModel.result.collect { result ->
                result?.let {
                    if (it == "true") {
                        Toast.makeText(this@PasswdChangeActivity, "密码修改成功", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@PasswdChangeActivity, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@PasswdChangeActivity, "修改失败：$it", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // 设置确认按钮的点击事件
        binding.btnConfirm.setDebounceClickListener {
            val newPassword = binding.renewpasswd.text.toString().trim()
            val confirmPassword = binding.confirmPasswd.text.toString().trim()

            // 检查密码是否有效
            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show()
                return@setDebounceClickListener
            }
            if (newPassword.length < 6) {
                Toast.makeText(this, "密码长度不能少于6位", Toast.LENGTH_SHORT).show()
                return@setDebounceClickListener
            }
            if (newPassword != confirmPassword) {
                Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show()
                return@setDebounceClickListener
            }

            // 调用 ViewModel 修改密码，传递 token
            viewModel.passwdChange(email, username, newPassword, token)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        LoadingUtil.hideLoading(this)
    }
}