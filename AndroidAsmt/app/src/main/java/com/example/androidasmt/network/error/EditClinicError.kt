package com.example.androidasmt.network.error

import com.google.gson.annotations.SerializedName

data class EditClinicError (
    @SerializedName("Address")
    val address: List<String>?,
    @SerializedName("File")
    val file: List<String>?,
)