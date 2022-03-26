package com.example.androidasmt.network.body

data class EditVaccineBody (
    val id: Int,
    val name: String,
    val description: String,
    val pricePerDose: Double,
)