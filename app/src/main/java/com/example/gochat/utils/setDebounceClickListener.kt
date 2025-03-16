package com.example.gochat.utils

import android.view.View

fun View.setDebounceClickListener(debounceTime: Long = 1000L, action: () -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= debounceTime) {
            lastClickTime = currentTime
            action()
        }
    }
}