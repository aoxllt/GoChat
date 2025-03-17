package com.example.gochat.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gochat.MainActivity
import com.example.gochat.data.database.AppDatabase
import com.example.gochat.data.database.dao.UserDao
import com.example.gochat.data.database.dao.UserWithInfo
import com.example.gochat.data.database.entity.User
import com.example.gochat.data.database.entity.UserInfo
import com.example.gochat.data.database.entity.enums.UserStatus
import com.example.gochat.databinding.ActivityHomeBinding
import com.example.gochat.utils.TokenManager
import com.example.gochat.utils.setDebounceClickListener
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userDao = AppDatabase.getDatabase(this).userDao()

        lifecycleScope.launch {
            initializeTestData()
        }

        setupListeners()
    }

    private suspend fun initializeTestData() {
        if (userDao.getAllUsers().isEmpty()) {
            val user1Id = userDao.insertUser(User(username = "user1", password = "pass1", email = "user1@example.com"))
            val user2Id = userDao.insertUser(User(username = "user2", password = "pass2", email = "user2@example.com"))
            userDao.insertUserInfo(UserInfo(id = user1Id.toInt(), email = "user1@example.com", phoneNumber = "1234567890", bio = "Hello"))
            userDao.insertUserInfo(UserInfo(id = user2Id.toInt(), email = "user2@example.com", phoneNumber = "0987654321", bio = "Hi"))
        }
    }

    private fun setupListeners() {
        binding.btnClearJwt.setDebounceClickListener {
            TokenManager.clearTokens(this)
            Toast.makeText(this, "JWT 已清除，请重新登录", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        binding.btnClearRoom.setDebounceClickListener {
            lifecycleScope.launch {
                userDao.clearAll()
                Toast.makeText(this@HomeActivity, "Room 数据已清除", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnShowRoom.setDebounceClickListener {
            lifecycleScope.launch {
                val usersWithInfo = userDao.getUsersWithInfo()
                if (usersWithInfo.isEmpty()) {
                    Toast.makeText(this@HomeActivity, "暂无数据", Toast.LENGTH_SHORT).show()
                } else {
                    val dataString = usersWithInfo.joinToString("\n") { userWithInfo ->
                        val user = userWithInfo.user
                        val info = userWithInfo.userInfo
                        "ID: ${user.id}, Username: ${user.username}, Email: ${user.email}, Status: ${user.status}, " +
                                "Phone: ${info?.phoneNumber ?: "N/A"}, Bio: ${info?.bio ?: "N/A"}, " +
                                "DisplayName: ${info?.displayName ?: "N/A"}"
                    }
                    Log.d("HomeActivity", "Room 数据:\n$dataString")
                    Toast.makeText(this@HomeActivity, "数据已输出到 Logcat", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}