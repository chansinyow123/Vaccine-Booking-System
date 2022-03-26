package com.example.androidasmt.network.error

import com.google.gson.annotations.SerializedName

data class CreateBookingError (
    @SerializedName("Description")
    val description: List<String>?,
    @SerializedName("AppointmentDateTime")
    val appointmentDateTime: List<String>?,
)