package com.example.gochat.api

import com.google.gson.annotations.SerializedName

data class UserProfileResponse(
    @SerializedName("id") val Id: String,
    @SerializedName("display_name") val Displayname: String,
    @SerializedName("email") val Email: String,
    @SerializedName("avatar_url") val avatarUrl: String,
    @SerializedName("bio") val bio: String,
    @SerializedName("gender") val sex: String,
    @SerializedName("birth_date") val birth: String,
    @SerializedName("phone_number") val Phonenumber: String,
    @SerializedName("location") val localcity: String,
    @SerializedName("last_logintime") val lastlogintime: String,
)