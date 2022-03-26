package com.example.androidasmt.network.body

data class CreateVaccineBody(
    val name: String,
    val description: String,
    val pricePerDose: Double,
    val dayRange: List<Int>
)