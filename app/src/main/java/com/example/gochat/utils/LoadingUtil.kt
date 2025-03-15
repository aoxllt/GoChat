package com.example.gochat.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import com.example.gochat.R
import kotlin.random.Random

object LoadingUtil {
    private var loadingView: View? = null
    private val currentColors = mutableSetOf<String>() // 跟踪当前使用的颜色

    fun showLoading(context: Context) {
        if (loadingView != null) return
        val parent = (context as? Activity)?.window?.decorView as? ViewGroup
            ?: throw IllegalStateException("Context must be an Activity")
        loadingView = LayoutInflater.from(context).inflate(R.layout.loading_dialog, parent, false)
        val layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        parent.addView(loadingView, layoutParams)
        loadingView?.visibility = View.VISIBLE

        // 获取三个球的 ImageView
        val ball1 = loadingView?.findViewById<ImageView>(R.id.ball1)
        val ball2 = loadingView?.findViewById<ImageView>(R.id.ball2)
        val ball3 = loadingView?.findViewById<ImageView>(R.id.ball3)

        // 定义浅色池（天蓝色系为主）
        val colorPool = listOf(
            "#81D4FA", // 天蓝色
            "#B3E5FC", // 浅天蓝
            "#C5CAE9", // 浅紫蓝
            "#B2EBF2", // 浅青色
            "#F8BBD0", // 浅粉色
            "#DCEDC8"  // 浅绿色
        )

        // 初始化三个不同颜色
        currentColors.clear()
        val initialColors = colorPool.shuffled().take(3) // 随机选择 3 个不重复颜色
        ball1?.setImageDrawable(createBallDrawable(initialColors[0]))
        ball2?.setImageDrawable(createBallDrawable(initialColors[1]))
        ball3?.setImageDrawable(createBallDrawable(initialColors[2]))
        currentColors.addAll(initialColors)

        // 添加随机颜色切换效果（保持颜色不同）
        startColorTransition(ball1, colorPool)
        startColorTransition(ball2, colorPool)
        startColorTransition(ball3, colorPool)

        // 启动跳跃动画
        val bounce1 = AnimationUtils.loadAnimation(context, R.anim.bounce1)
        val bounce2 = AnimationUtils.loadAnimation(context, R.anim.bounce2)
        val bounce3 = AnimationUtils.loadAnimation(context, R.anim.bounce3)

        ball1?.startAnimation(bounce1)
        ball2?.startAnimation(bounce2)
        ball3?.startAnimation(bounce3)
    }

    fun hideLoading(context: Context) {
        val parent = (context as? Activity)?.window?.decorView as? ViewGroup
            ?: return
        loadingView?.let {
            parent.removeView(it)
            loadingView = null
        }
        currentColors.clear() // 清空颜色集合
    }

    // 创建圆形彩球 drawable
    private fun createBallDrawable(color: String): GradientDrawable {
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(android.graphics.Color.parseColor(color))
            setSize(20, 20)
        }
        return drawable
    }

    // 随机颜色切换效果（保证颜色不同）
    private fun startColorTransition(ball: ImageView?, colorPool: List<String>) {
        ball?.post(object : Runnable {
            override fun run() {
                val currentColor = (ball.drawable as? GradientDrawable)?.color?.defaultColor?.let {
                    "#${Integer.toHexString(it).substring(2)}"
                }
                val availableColors = colorPool.filter { it !in currentColors || it == currentColor }
                if (availableColors.isNotEmpty()) {
                    val newColor = availableColors[Random.nextInt(availableColors.size)]
                    if (currentColor != null && currentColor != newColor) {
                        currentColors.remove(currentColor)
                        currentColors.add(newColor)
                        ball.setImageDrawable(createBallDrawable(newColor))
                    }
                }
                ball.postDelayed(this, Random.nextLong(1000, 2000)) // 每 1-2 秒切换一次
            }
        })
    }
}