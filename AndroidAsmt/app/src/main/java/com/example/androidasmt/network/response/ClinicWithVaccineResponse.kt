package com.example.androidasmt.network.response

data class ClinicWithVaccineResponse (
    val clinicId: String,
    val address: String,
    val email: String,
    val file: String?,
    val contentType: String?,
    val vaccines: List<VaccineResponse>
)