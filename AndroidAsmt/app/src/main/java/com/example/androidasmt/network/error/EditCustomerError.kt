package com.example.androidasmt.network.error

import com.google.gson.annotations.SerializedName

data class EditCustomerError (
    @SerializedName("Name")
    val name: List<String>?,
    @SerializedName("Address")
    val address: List<String>?,
    @SerializedName("IC")
    val ic: List<String>?,
    @SerializedName("PhoneNumber")
    val phoneNumber: List<String>?,
    @SerializedName("File")
    val file: List<String>?,
)