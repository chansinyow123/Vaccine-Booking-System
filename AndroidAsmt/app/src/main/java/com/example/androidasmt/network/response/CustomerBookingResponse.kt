package com.example.androidasmt.network.response

import java.time.LocalDateTime

data class CustomerBookingResponse (
    val id: Int,
    val vaccineName: String,
    val clinicAddress: String,
    val completed: Boolean?,
    val actionDateTime: String
)