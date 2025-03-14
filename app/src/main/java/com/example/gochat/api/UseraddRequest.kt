package com.example.gochat.api

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

data class UseraddRequest(
    val email: String,
    val username: String,
    val password: String
) {
    fun toJsonRequestBody(): RequestBody {
        val gson = Gson()
        val json = gson.toJson(this)
        return json.toRequestBody("application/json".toMediaTypeOrNull())
    }
}