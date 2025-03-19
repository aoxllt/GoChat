package com.example.gochat.ui.main

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
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

        supportActionBar?.title = ""


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
        binding.actionFriendListFragmentToAddFriendFragment.setOnClickListener {
            navController.navigate(R.id.action_friendListFragment_to_addFriendFragment)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    // 设置沉浸式状态栏
    private fun setupStatusBar() {
        // 使状态栏透明
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = android.graphics.Color.TRANSPARENT

        // 设置状态栏图标为深色（适用于浅色背景）
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.isAppearanceLightStatusBars = true
    }

    // 获取状态栏高度的辅助函数
    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    // 获取 ActionBar 高度的辅助函数
    private fun getActionBarHeight(): Int {
        var result = 0
        val typedArray = theme.obtainStyledAttributes(intArrayOf(android.R.attr.actionBarSize))
        result = typedArray.getDimensionPixelSize(0, 0)
        typedArray.recycle()
        return result
    }
}