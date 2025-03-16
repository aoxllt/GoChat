package com.example.myapp.di

import android.provider.Settings
import com.example.gochat.api.MessageApi
import com.example.gochat.api.PasswdchangeRequest
import com.example.gochat.config.config
import com.example.gochat.data.ApiService
import com.example.gochat.data.database.AppDatabase
import com.example.gochat.data.repository.UserRepository

import com.example.gochat.viewmodel.CaptchViewModel

import com.example.gochat.viewmodel.LoginViewModel
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
    single { AppDatabase.getDatabase(androidContext()) }

    // DAO
    single { get<AppDatabase>().userDao() }
    single { get<AppDatabase>().userInfoDao() }
    single { get<AppDatabase>().friendDao() }

    // Retrofit 客户端（共享配置）
    single {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(config.BACKEND_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ApiService
    single { get<Retrofit>().create(ApiService::class.java) }



    // MessageApi（新增）
    single { get<Retrofit>().create(MessageApi::class.java) }

    // Repository
    single { UserRepository(get(), get(), get(), androidContext()) }

    // Device ID
    single {
        Settings.Secure.getString(androidContext().contentResolver, Settings.Secure.ANDROID_ID)
    }

    // ViewModel
    viewModel { RegisterViewModel(get(), get()) } // UserRepository, deviceId
    viewModel { CaptchViewModel(get(), get()) }   // UserRepository, deviceId
    viewModel { UseraddViewModel(get()) }
    viewModel { PasswdforgotViewModel(get()) }
    viewModel { PasswdchangeViewModel(get()) }
    viewModel { LoginViewModel(get(), get(), get()) }
//    viewModel { AddFriendViewModel(get()) }       // FriendApi
//    viewModel { FriendListViewModel(get()) }      // FriendRepository
//    viewModel { ChatViewModel(get()) }

}