package com.example.androidasmt.ui.clinic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.body.CreateVaccineBody
import com.example.androidasmt.network.error.CreateVaccineError
import retrofit2.Response

class AddVaccineFragmentModel: ViewModel() {
    // input field
    var name = MutableLiveData("")
    var description = MutableLiveData("")
    var pricePerDose = 0.0
    var dayRange = mutableListOf<Int>()

    var isLoading = MutableLiveData(false)

    // error message
    val errName = MutableLiveData("")
    val errDescription = MutableLiveData("")
    var errPricePerDose = MutableLiveData("")
    var errDayRange = MutableLiveData("")

    suspend fun submitForm(): Response<Unit> {

        // prepare body data -----------------------------------------------------
        val body = CreateVaccineBody (
            name.value!!,
            description.value!!,
            pricePerDose,
            dayRange
        )

        // send api request with body data --------------------------------------
        return VaccineApi.retrofitService.createVaccine(body)
    }

    fun clearError() {
        errName.value = ""
        errDescription.value = ""
        errPricePerDose.value = ""
        errDayRange.value = ""
    }

    fun setError(error: CreateVaccineError) {
        // show all the error
        error.name?.forEachIndexed           { i, e -> errName.value         += "* $e" + if (i != error.name.lastIndex)         "\n" else ""}
        error.description?.forEachIndexed    { i, e -> errDescription.value  += "* $e" + if (i != error.description.lastIndex)  "\n" else ""}
        error.pricePerDose?.forEachIndexed   { i, e -> errPricePerDose.value += "* $e" + if (i != error.pricePerDose.lastIndex) "\n" else ""}
        error.dayRange?.forEachIndexed       { i, e -> errDayRange.value     += "* $e" + if (i != error.dayRange.lastIndex)     "\n" else ""}
    }
}