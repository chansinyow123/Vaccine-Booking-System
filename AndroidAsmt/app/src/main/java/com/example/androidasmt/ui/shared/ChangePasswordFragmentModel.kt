package com.example.androidasmt.ui.shared

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.body.ChangePasswordBody
import com.example.androidasmt.network.error.ChangePasswordError
import retrofit2.Response

class ChangePasswordFragmentModel: ViewModel() {

    var oldPassword = MutableLiveData("")
    var newPassword = MutableLiveData("")
    var cfmPassword = MutableLiveData("")

    var isLoading = MutableLiveData(false)

    // error message
    val errOldPassword = MutableLiveData("")
    val errNewPassword = MutableLiveData("")
    val errCfmPassword = MutableLiveData("")

    suspend fun submitForm(): Response<Unit> {

        // prepare body data
        val body = ChangePasswordBody(
            oldPassword.value!!,
            newPassword.value!!,
            cfmPassword.value!!
        )

        return VaccineApi.retrofitService.changePassword(body)
    }

    fun clearError() {
        errOldPassword.value = ""
        errNewPassword.value = ""
        errCfmPassword.value = ""
    }

    fun setError(error: ChangePasswordError) {
        // show all the error
        error.oldPassword?.forEachIndexed    { i, e -> errOldPassword.value += "* $e" + if (i != error.oldPassword.lastIndex)  "\n" else ""}
        error.newPassword?.forEachIndexed    { i, e -> errNewPassword.value += "* $e" + if (i != error.newPassword.lastIndex)  "\n" else ""}
        error.cfmPassword?.forEachIndexed    { i, e -> errCfmPassword.value += "* $e" + if (i != error.cfmPassword.lastIndex)  "\n" else ""}
    }
}


















