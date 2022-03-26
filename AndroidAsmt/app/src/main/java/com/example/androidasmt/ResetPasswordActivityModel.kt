package com.example.androidasmt

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.body.ResetPasswordBody
import com.example.androidasmt.network.error.ResetPasswordError
import retrofit2.Response

class ResetPasswordActivityModel : ViewModel() {
    // input field
    val password = MutableLiveData("")
    val cfmPassword = MutableLiveData("")

    // error message
    val errPassword = MutableLiveData("")
    val errCfmPassword = MutableLiveData("")

    // query parameter
    var token : String? = null
    var uid : String? = null

    // loading screen
    var isLoading = MutableLiveData(false)

    suspend fun submitForm() : Response<Unit> {

        // prepare body data
        val body = ResetPasswordBody(uid!!, token!!, password.value!!, cfmPassword.value!!)

        // reset password
        return VaccineApi.retrofitService.resetPassword(body)
    }

    fun clearError() {
        errPassword.value = ""
        errCfmPassword.value = ""
    }

    fun setError(error: ResetPasswordError) {
        // show all the error
        error.password?.forEachIndexed    { i, e -> errPassword.value    += "* $e" + if (i != error.password.lastIndex)    "\n" else "" }
        error.cfmPassword?.forEachIndexed { i, e -> errCfmPassword.value += "* $e" + if (i != error.cfmPassword.lastIndex) "\n" else "" }
    }
}