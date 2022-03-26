package com.example.androidasmt.network.body

data class CreateBookingBody (
    val vaccineId: Int,
    val description: String,
    val appointmentDateTime: String,
)