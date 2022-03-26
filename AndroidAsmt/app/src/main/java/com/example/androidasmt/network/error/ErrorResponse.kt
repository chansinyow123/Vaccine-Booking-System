package com.example.androidasmt.network.error

data class ErrorResponse<T> (
    val errors: T,
    val type: String,
    val title: String,
    val status: Int,
    val traceId: String
)