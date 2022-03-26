package com.example.androidasmt.network.error

import com.google.gson.annotations.SerializedName

data class RegisterClinicError (
    @SerializedName("Username")
    val username: List<String>?,
    @SerializedName("Password")
    val password: List<String>?,
    @SerializedName("Address")
    val address: List<String>?,
    @SerializedName("File")
    val file: List<String>?,
)