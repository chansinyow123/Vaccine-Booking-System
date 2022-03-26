package com.example.androidasmt.network.body

data class ResetPasswordBody(
    val userId: String,
    val token: String,
    val password: String,
    val cfmPassword: String
)