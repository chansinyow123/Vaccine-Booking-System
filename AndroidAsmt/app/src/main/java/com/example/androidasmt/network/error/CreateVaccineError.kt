package com.example.androidasmt.network.error

import com.google.gson.annotations.SerializedName

data class CreateVaccineError (
    @SerializedName("Name")
    val name: List<String>?,
    @SerializedName("Description")
    val description: List<String>?,
    @SerializedName("PricePerDose")
    val pricePerDose: List<String>?,
    @SerializedName("DayRange")
    val dayRange: List<String>?,
)