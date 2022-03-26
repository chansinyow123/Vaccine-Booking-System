package com.example.androidasmt.network.response

data class CustomerListResponse (
    val bookingId: Int,
    val name: String,
    val email: String,
    val file: String?,
    val contentType: String?,
)