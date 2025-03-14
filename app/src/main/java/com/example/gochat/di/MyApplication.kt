package com.example.gochat.di

import android.app.Application
import com.example.myapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // 启动 Koin
        startKoin {
            androidContext(this@MyApplication) // 绑定 Application 上下文
            modules(appModule) // 加载 Koin 模块
        }
    }
}