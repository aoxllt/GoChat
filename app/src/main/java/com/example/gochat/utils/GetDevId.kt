package com.example.gochat.utils

import android.app.Application
import android.provider.Settings

class GetDevId : Application() {
    val deviceId: String by lazy {
        Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    override fun onCreate() {
        super.onCreate()
    }
}