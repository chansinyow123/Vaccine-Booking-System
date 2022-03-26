package com.example.androidasmt.network.error

import com.google.gson.annotations.SerializedName

data class ChangePasswordError (
    @SerializedName("OldPassword")
    val oldPassword: List<String>?,
    @SerializedName("NewPassword")
    val newPassword: List<String>?,
    @SerializedName("CfmPassword")
    val cfmPassword: List<String>?,
)