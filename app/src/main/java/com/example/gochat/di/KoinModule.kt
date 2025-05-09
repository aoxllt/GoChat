package com.example.myapp.di

import android.provider.Settings
import com.example.gochat.api.PasswdchangeRequest
import com.example.gochat.config.config
import com.example.gochat.data.ApiService
import com.example.gochat.data.database.AppDatabase
import com.example.gochat.data.repository.UserInfoRepository
import com.example.gochat.data.repository.UserRepository
import com.example.gochat.viewmodel.CaptchViewModel
import com.example.gochat.viewmodel.LoginViewModel
import com.example.gochat.viewmodel.MyViewModel
import com.example.gochat.viewmodel.PasswdchangeViewModel
import com.example.gochat.viewmodel.PasswdforgotViewModel
import com.example.gochat.viewmodel.UseraddViewModel
import com.example.myapp.ui.viewmodel.RegisterViewModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val appModule = module {
    // 数据库
    single {
        AppDatabase.getDatabase(androidContext())
    }

    // DAO
    single {
        get<AppDatabase>().userDao()
    }

    single { // 新增 UserInfoDao
        get<AppDatabase>().userInfoDao()
    }

    // Retrofit
    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS) // 连接超时：30秒
            .readTimeout(30, TimeUnit.SECONDS)    // 读取超时：30秒
            .writeTimeout(30, TimeUnit.SECONDS)   // 写入超时：30秒
            .build()
        Retrofit.Builder()
            .baseUrl(config.BACKEND_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    // Repository
    single {
        UserRepository(get(), get(),get(),androidContext())
    }
    single {
        UserInfoRepository(androidContext(), get(),get())
    }

    // Device ID（提取为单独的 single 定义，避免重复计算）
    single {
        Settings.Secure.getString(androidContext().contentResolver, Settings.Secure.ANDROID_ID)
    }

    // ViewModel
    viewModel {
        RegisterViewModel(get(), get()) // 注入 UserRepository 和 deviceId
    }

    viewModel {
        CaptchViewModel(get(), get()) // 注入 UserRepository 和 deviceId
    }
    viewModel {
        UseraddViewModel(get())
    }
    viewModel {
        PasswdforgotViewModel(get())
    }
    viewModel {
        PasswdchangeViewModel(get())
    }
    viewModel {
        LoginViewModel(get(),get(),get(),androidContext())
    }
    viewModel { MyViewModel(get(), get(),androidContext()) }
}