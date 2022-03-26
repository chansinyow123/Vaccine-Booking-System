package com.example.androidasmt.network.error

import com.google.gson.annotations.SerializedName

data class ResetPasswordError (
    @SerializedName("UserId")
    val userId: List<String>?,
    @SerializedName("Token")
    val token: List<String>?,
    @SerializedName("Password")
    val password: List<String>?,
    @SerializedName("CfmPassword")
    val cfmPassword: List<String>?,
)