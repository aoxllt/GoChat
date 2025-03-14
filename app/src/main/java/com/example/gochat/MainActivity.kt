package com.example.gochat

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.gochat.databinding.ActivityMainBinding
import com.example.gochat.ui.user.PasswdForgotActivity
import com.example.gochat.ui.user.RegisterActivity
import com.example.gochat.ui.user.UserinfoaddActivity

import kotlin.jvm.java

class MainActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            //val intent = Intent(this, UserinfoaddActivity::class.java)
            startActivity(intent)
        }
        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, PasswdForgotActivity::class.java)
            startActivity(intent)
        }
    }
}