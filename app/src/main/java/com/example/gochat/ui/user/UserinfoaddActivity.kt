package com.example.gochat.ui.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gochat.HomeActivity
import com.example.gochat.R
import com.example.gochat.api.UseraddRequest
import com.example.gochat.databinding.ActivityUseraddBinding

import com.example.gochat.utils.LoadingUtil
import com.example.gochat.utils.setDebounceClickListener
import com.example.gochat.viewmodel.RegisterState
import com.example.gochat.viewmodel.UseraddViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class UserinfoaddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUseraddBinding
    private var avatarUri: Uri? = null
    private val viewModel: UseraddViewModel by viewModel()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            avatarUri = it
            binding.ivAvatar.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUseraddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val email = intent.getStringExtra("email") ?: ""
        binding.ivAvatar.setImageResource(R.drawable.gopher)

        binding.ivAvatar.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        setupUsernameWatcher()
        observeRegisterState()

        binding.btnConfirm.setDebounceClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (!validateInputs(username, password, confirmPassword)) {
                return@setDebounceClickListener
            }

            val request = UseraddRequest(email, username, password)
            viewModel.saveUserInfo(request, avatarUri, contentResolver)
        }
    }

    private fun setupUsernameWatcher() {
        binding.etUsername.addTextChangedListener(object : TextWatcher {
            private var debounceJob: Job? = null

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val username = s.toString().trim()
                if (username.isEmpty()) {
                    binding.etUsername.error = null
                    return
                }

                debounceJob?.cancel()

                if (username.length <= 4) {
                    binding.etUsername.error = "用户名要大于四位"
                    return
                }

                debounceJob = coroutineScope.launch {
                    delay(500)
                    val result = viewModel.checkUsername(username)
                    when (result) {
                        "可用" -> binding.etUsername.error = null
                        else -> binding.etUsername.error = result
                    }
                }
            }
        })
    }

    private fun observeRegisterState() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterState.Loading -> {
                    binding.btnConfirm.isEnabled = false
                    LoadingUtil.showLoading(this) // 显示加载动画
                    Toast.makeText(this, "注册中...", Toast.LENGTH_SHORT).show()
                }
                is RegisterState.Success -> {
                    LoadingUtil.hideLoading(this) // 隐藏加载动画
                    binding.btnConfirm.isEnabled = true
                    Toast.makeText(this, "注册成功，欢迎来到GoChat!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is RegisterState.Error -> {
                    LoadingUtil.hideLoading(this) // 隐藏加载动画
                    binding.btnConfirm.isEnabled = true
                    Toast.makeText(this, state.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateInputs(username: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty()) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show()
            return false
        }
        if (username.length <= 4) {
            Toast.makeText(this, "用户名要大于四位", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "密码不能为空", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, "密码长度至少为 6 位", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        LoadingUtil.hideLoading(this)
    }
}