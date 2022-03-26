package com.example.androidasmt.ui.clinic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.body.EditVaccineBody
import com.example.androidasmt.network.error.EditVaccineError
import com.example.androidasmt.network.response.VaccineResponse
import retrofit2.Response

class EditVaccineFragmentModel: ViewModel() {
    var id = 0
    val name = MutableLiveData("")
    val description = MutableLiveData("")
    val pricePerDose = MutableLiveData(0.0)
    val dayRange = MutableLiveData<List<Int>>(listOf())

    val isLoading = MutableLiveData(false)
    val isInitializing = MutableLiveData(true)

    val errName = MutableLiveData("")
    val errDescription = MutableLiveData("")
    val errPricePerDose = MutableLiveData("")

    fun initData(response: VaccineResponse) {
        name.value = response.name
        description.value = response.description
        pricePerDose.value = response.pricePerDose
        dayRange.value = response.dayRange

        isInitializing.value = false
    }

    suspend fun submitEditForm(): Response<Unit> {

        // prepare body data
        val body = EditVaccineBody(
            id,
            name.value!!,
            description.value!!,
            pricePerDose.value!!,
        )

        // send api request with body data
        return VaccineApi.retrofitService.editVaccine(body)
    }

    suspend fun submitDeleteForm(): Response<Unit> {
        // send api request with body data
        return VaccineApi.retrofitService.deleteVaccine(id)
    }

    fun clearError() {
        errName.value = ""
        errDescription.value = ""
        errPricePerDose.value = ""
    }

    fun setError(error: EditVaccineError) {
        // show all the error
        error.name?.forEachIndexed          { i, e -> errName.value         += "* $e" + if (i != error.name.lastIndex)         "\n" else ""}
        error.description?.forEachIndexed   { i, e -> errDescription.value  += "* $e" + if (i != error.description.lastIndex)  "\n" else ""}
        error.pricePerDose?.forEachIndexed  { i, e -> errPricePerDose.value += "* $e" + if (i != error.pricePerDose.lastIndex) "\n" else ""}
    }
}