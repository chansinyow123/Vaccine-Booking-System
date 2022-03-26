package com.example.androidasmt.ui.anonymous

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.body.ForgotPasswordBody
import com.example.androidasmt.network.error.ForgotPasswordError
import retrofit2.Response

class ForgotPasswordFragmentModel : ViewModel() {
    var email = MutableLiveData("")
    var isLoading = MutableLiveData(false)
    var errEmail = MutableLiveData("")

    suspend fun submitForm(): Response<Unit> {

        // prepare body data
        val body = ForgotPasswordBody(email.value!!, "http://www.example.com/reset-password")

        // send forgot password link
        return VaccineApi.retrofitService.sendForgotPasswordLink(body)
    }

    fun clearError() {
        errEmail.value = ""
    }

    fun setError(error: ForgotPasswordError) {
        // show all the error
        error.username?.forEachIndexed { i, e -> errEmail.value    += "* $e" + if (i != error.username.lastIndex)    "\n" else ""}
    }
}