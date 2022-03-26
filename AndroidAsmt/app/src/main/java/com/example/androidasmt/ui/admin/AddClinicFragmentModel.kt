package com.example.androidasmt.ui.admin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.error.RegisterClinicError
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File

class AddClinicFragmentModel: ViewModel() {

    // input field
    var image: File? = null
    var email = MutableLiveData("")
    var password = MutableLiveData("")
    var address = MutableLiveData("")

    var isLoading = MutableLiveData(false)

    // error message
    val errImage = MutableLiveData("")
    val errEmail = MutableLiveData("")
    var errPassword = MutableLiveData("")
    var errAddress = MutableLiveData("")

    suspend fun submitForm(): Response<Unit> {

        var file: MultipartBody.Part? = null

        image?.let {

            // get the file requestbody
            val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/${it.extension}"), it)

            // MultipartBody.Part is used to send also the actual file name
            file = MultipartBody.Part.createFormData("file", it.name, requestFile)
        }

        // prepare body data
        val username     = RequestBody.create(MultipartBody.FORM, email.value!!)
        val password     = RequestBody.create(MultipartBody.FORM, password.value!!)
        val address      = RequestBody.create(MultipartBody.FORM, address.value!!)

        // send api request with body data --------------------------------------
        return VaccineApi.retrofitService.registerClinic(
            username,
            password,
            address,
            file
        )
    }

    fun clearError() {
        errImage.value = ""
        errAddress.value = ""
        errEmail.value = ""
        errPassword.value = ""
    }

    fun setError(error: RegisterClinicError) {
        // show all the error
        error.username?.forEachIndexed    { i, e -> errEmail.value    += "* $e" + if (i != error.username.lastIndex)    "\n" else ""}
        error.password?.forEachIndexed    { i, e -> errPassword.value += "* $e" + if (i != error.password.lastIndex)    "\n" else ""}
        error.address?.forEachIndexed     { i, e -> errAddress.value  += "* $e" + if (i != error.address.lastIndex)     "\n" else ""}
        error.file?.forEachIndexed        { i, e -> errImage.value    += "* $e" + if (i != error.file.lastIndex)        "\n" else ""}
    }

}