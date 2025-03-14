package com.example.gochat.ui.user

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.gochat.R
import com.example.gochat.api.UseraddRequest
import com.example.gochat.databinding.ActivityUseraddBinding
import com.example.gochat.ui.main.HomeActivity
import com.example.gochat.viewmodel.UseraddViewModel
import kotlinx.coroutines.*
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

        binding.btnConfirm.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (!validateInputs(username, password, confirmPassword)) {
                return@setOnClickListener
            }

            binding.btnConfirm.isEnabled = false
            coroutineScope.launch {
                val request = UseraddRequest(email, username, password)
                val saveResult = viewModel.saveUserInfo(request, avatarUri, contentResolver)
                if (saveResult == "true") {
                    Toast.makeText(this@UserinfoaddActivity, "用户信息已保存", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this@UserinfoaddActivity, HomeActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@UserinfoaddActivity, saveResult, Toast.LENGTH_SHORT).show()
                }
                binding.btnConfirm.isEnabled = true
            }
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
                        "true" -> binding.etUsername.error = null
                        "false" -> binding.etUsername.error = "用户名已被占用"
                        else -> Toast.makeText(this@UserinfoaddActivity, result, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun validateInputs(username: String, password: String, confirmPassword: String): Boolean {
        if (username.isEmpty()) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show()
            return false
        }
        if (username.length <= 4) {  // 添加长度检查
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
    }
}