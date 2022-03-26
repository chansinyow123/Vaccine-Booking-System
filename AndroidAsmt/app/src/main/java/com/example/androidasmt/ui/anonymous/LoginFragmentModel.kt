package com.example.androidasmt.ui.anonymous

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.body.LoginBody
import com.example.androidasmt.network.response.LoginResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class LoginFragmentModel : ViewModel() {
    var email = MutableLiveData("")
    var password = MutableLiveData("")

    var isLoading = MutableLiveData(false)

    suspend fun submitForm(): Response<LoginResponse> {

        // prepare body data
        val body = LoginBody(email.value!!, password.value!!)

        // login and return response
        return VaccineApi.retrofitService.login(body)
    }
}