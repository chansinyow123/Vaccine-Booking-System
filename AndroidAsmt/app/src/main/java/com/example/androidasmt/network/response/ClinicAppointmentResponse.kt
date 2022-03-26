package com.example.androidasmt.network.response

data class ClinicAppointmentResponse (
    val appointmentTime: String,
    val customers: List<CustomerListResponse>,
)