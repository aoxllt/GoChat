package com.example.gochat.data

import com.example.gochat.api.LoginRequest
import com.example.gochat.api.LoginResponse
import com.example.gochat.api.PasswdchangeRequest
import com.example.gochat.api.PasswdforgotRequest
import com.example.gochat.api.PasswdforgotSendcodeRequest
import com.example.gochat.api.RegisterRequest
import com.example.gochat.api.Respons
import com.example.gochat.api.UserProfileResponse
import com.example.gochat.api.VerifyRequest
import com.example.gochat.api.VerifyResponse
import com.example.gochat.data.database.entity.Chat
import com.example.gochat.data.database.entity.Friend
import com.example.gochat.data.database.entity.User
import com.example.gochat.data.database.entity.UserInfo
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
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<Respons>
    @POST("verify")
    suspend fun  verify(@Body varify: VerifyRequest): Response<ResponseBody>
    @GET("username")
    suspend fun checkUsername(@Query("username") username: String ): Response<Respons>
    @Multipart
    @POST("/useradd")
    suspend fun registerUser(@Part("user") userJson: RequestBody, @Part avatar: MultipartBody.Part?): Response<LoginResponse>
    @POST("passwdforgot")
    suspend fun passwdForgot(@Body passwdforgot: PasswdforgotRequest): Response<VerifyResponse>
    @POST("passwdchange")
    suspend fun passwdChange(@Body passwdchange: PasswdchangeRequest): Response<Respons>
    @POST("login")
    suspend fun login(@Body login: LoginRequest): Response<LoginResponse>
    @POST("refresh")
    suspend fun refreshToken(@Header("Refresh-Token") refreshToken: String): Response<LoginResponse>
    @POST("passwdfgcode")
    suspend fun sendVerificationCode(@Body passwdchange: PasswdforgotSendcodeRequest): Response<Respons>
    @POST("auth/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<ResponseBody>

    // 新增更新用户资料的接口
    @POST("auth/profile/update")
    suspend fun updateUserProfile(
        @Header("Authorization") token: String,
        @Body userInfo: UserInfo
    ): Response<Respons>
    @GET("chats/list")
    suspend fun getChatList(@Header("Authorization") token: String): Response<List<Chat>>
    @POST("friends/list")
    suspend fun getFriendList(@Header("Authorization") token: String): Response<List<Friend>>

    @POST("friends/add/{friendId}")
    suspend fun addFriend(@Header("Authorization") token: String, @Path("friendId") friendId: Int): Response<Friend>

}