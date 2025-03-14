package com.example.gochat.data

import com.example.gochat.api.LoginRequest
import com.example.gochat.api.LoginResponse
import com.example.gochat.api.PasswdchangeRequest
import com.example.gochat.api.PasswdforgotRequest
import com.example.gochat.api.RegisterRequest
import com.example.gochat.api.VerifyRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>
    @POST("verify")
    suspend fun  verify(@Body varify: VerifyRequest): Response<ResponseBody>
    @GET("username")
    suspend fun checkUsername(@Query("username") username: String ): Response<ResponseBody>
    @Multipart
    @POST("useradd")
    suspend fun registerUser(@Part("user") userJson: RequestBody,@Part avatar: MultipartBody.Part?): Response<ResponseBody>
    @POST("passwdforgot")
    suspend fun passwdForgot(@Body passwdforgot: PasswdforgotRequest): Response<ResponseBody>
    @POST("passwdchange")
    suspend fun passwdChange(@Body passwdchange: PasswdchangeRequest): Response<ResponseBody>
    @POST("login")
    suspend fun login(@Body login: LoginRequest): Response<LoginResponse>
    @POST("refresh")
    suspend fun refreshToken(@Header("Refresh-Token") refreshToken: String): Response<LoginResponse>
}