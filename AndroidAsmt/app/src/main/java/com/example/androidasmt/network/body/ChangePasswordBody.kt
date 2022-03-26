package com.example.androidasmt.network.body

data class ChangePasswordBody(
    val oldPassword: String,
    val newPassword: String,
    val cfmPassword: String
)