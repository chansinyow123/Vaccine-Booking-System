package com.example.androidasmt.network.response

data class BookingResponse (
    val bookingId: Int,
    val file: String?,
    val contentType: String?,
    val name: String,
    val email: String,
    val ic: String,
    val phoneNumber: String,
    val clinicAddress: String,
    val clinicEmail: String,
    val bookingDescription: String,
    val vaccineName: String,
    val vaccineDescription: String,
    val vaccinePricePerDose: Double,
    val dayRange: List<Int>,
    val appointments: List<AppointmentResponse>,
    val completed: Boolean?,
)