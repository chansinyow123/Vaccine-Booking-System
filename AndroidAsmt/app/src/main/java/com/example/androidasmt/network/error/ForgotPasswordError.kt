package com.example.androidasmt.network.error

import com.google.gson.annotations.SerializedName

data class ForgotPasswordError(
    @SerializedName("Username")
    val username: List<String>?
)