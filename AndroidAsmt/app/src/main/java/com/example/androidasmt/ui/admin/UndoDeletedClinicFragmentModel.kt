package com.example.androidasmt.ui.admin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.response.UserResponse
import retrofit2.Response

class UndoDeletedClinicFragmentModel: ViewModel() {
    var id = ""
    val email = MutableLiveData("")
    val address = MutableLiveData("")
    var file = MutableLiveData("")

    val isLoading = MutableLiveData(false)
    val isInitializing = MutableLiveData(true)

    fun initData(response: UserResponse) {
        address.value = response.address
        email.value = response.email
        file.value = response.file

        isInitializing.value = false
    }

    suspend fun submitForm(): Response<Unit> {
        // send api request with body data
        return VaccineApi.retrofitService.undoDeleteClinic(id)
    }
}