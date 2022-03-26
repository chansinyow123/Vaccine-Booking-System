package com.example.androidasmt.network.response

data class VaccineResponse (
    val id: Int,
    val name: String,
    val description: String,
    val pricePerDose: Double,
    val dayRange: List<Int>,
) {
    override fun toString() = "$name (RM %.2f)".format(pricePerDose)
}