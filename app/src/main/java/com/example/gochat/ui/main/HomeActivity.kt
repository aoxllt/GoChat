package com.example.gochat.ui.main

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.gochat.R
import com.example.gochat.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置 Toolbar
        setSupportActionBar(binding.toolbar)

        // 设置导航
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 将 BottomNavigationView 与 NavController 绑定
        binding.bottomNavigation.setupWithNavController(navController)

        // 监听导航变化，控制 AppBarLayout 可见性
        navController.addOnDestinationChangedListener { _: NavController, destination: NavDestination, _: Bundle? ->
            when (destination.id) {
                R.id.myFragment -> {
                    binding.appBarLayout.visibility = View.GONE  // 在 "我的" 页面隐藏 AppBar
                }
                else -> {
                    binding.appBarLayout.visibility = View.VISIBLE  // 其他页面显示 AppBar
                }
            }
        }

        // 添加好友按钮点击事件
        binding.addFriendButton.setOnClickListener {
            // TODO: 实现添加好友逻辑，例如导航到添加好友页面
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}