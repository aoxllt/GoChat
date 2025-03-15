package com.example.gochat.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.gochat.MainActivity
import com.example.gochat.R
import com.example.gochat.databinding.ActivityCaptchBinding
import com.example.gochat.databinding.ActivityHomeBinding
import com.example.gochat.databinding.ActivityMainBinding
import com.example.gochat.utils.TokenManager
import com.example.gochat.utils.setDebounceClickListener

class HomeActivity: AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        binding.btnClearJwt.setDebounceClickListener {
            android.app.AlertDialog.Builder(this)
                .setTitle("确认清除")
                .setMessage("确定要清除 JWT 并退出登录吗？")
                .setPositiveButton("确定") { _, _ ->
                    TokenManager.clearTokens(this)
                    Toast.makeText(this, "JWT 已清除，请重新登录", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }
}