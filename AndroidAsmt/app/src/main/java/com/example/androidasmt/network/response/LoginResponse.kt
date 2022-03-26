package com.example.androidasmt.network.response

data class LoginResponse (
    val token: String,
    val userRole: String,
    val name: String?,
    val email: String,
    val address: String?,
    val file: String?,
    var contentType: String?
)