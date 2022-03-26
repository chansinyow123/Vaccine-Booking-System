package com.example.androidasmt.ui.anonymous

import android.database.Cursor
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.error.RegisterCustomerError
import okhttp3.MediaType
import retrofit2.Response
import okhttp3.RequestBody

import okhttp3.MultipartBody
import java.io.File
import java.io.InputStream
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream


class RegisterFragmentModel : ViewModel() {

    // input field
    var image: File? = null
    var name = MutableLiveData("")
    var ic = MutableLiveData("")
    var phone = MutableLiveData("")
    var address = MutableLiveData("")
    var email = MutableLiveData("")
    var password = MutableLiveData("")

    var isLoading = MutableLiveData(false)

    // error message
    val errImage = MutableLiveData("")
    val errName = MutableLiveData("")
    val errIC = MutableLiveData("")
    val errPhone = MutableLiveData("")
    val errAddress = MutableLiveData("")
    val errEmail = MutableLiveData("")
    val errPassword = MutableLiveData("")


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
        val name         = RequestBody.create(MultipartBody.FORM, name.value!!)
        val ic           = RequestBody.create(MultipartBody.FORM, ic.value!!)
        val phoneNumber  = RequestBody.create(MultipartBody.FORM, phone.value!!)
        val address      = RequestBody.create(MultipartBody.FORM, address.value!!)
        val deepLink     = RequestBody.create(MultipartBody.FORM, "http://www.example.com/email-verification")

        // send api request with body data --------------------------------------
        return VaccineApi.retrofitService.registerCustomerAccount(
            username,
            password,
            name,
            ic,
            phoneNumber,
            address,
            deepLink,
            file
        )
    }

    fun clearError() {
        errImage.value = ""
        errName.value = ""
        errIC.value = ""
        errPhone.value = ""
        errAddress.value = ""
        errEmail.value = ""
        errPassword.value = ""
    }

    fun setError(error: RegisterCustomerError) {
        // show all the error
        error.username?.forEachIndexed    { i, e -> errEmail.value    += "* $e" + if (i != error.username.lastIndex)    "\n" else ""}
        error.password?.forEachIndexed    { i, e -> errPassword.value += "* $e" + if (i != error.password.lastIndex)    "\n" else ""}
        error.name?.forEachIndexed        { i, e -> errName.value     += "* $e" + if (i != error.name.lastIndex)        "\n" else ""}
        error.ic?.forEachIndexed          { i, e -> errIC.value       += "* $e" + if (i != error.ic.lastIndex)          "\n" else ""}
        error.phoneNumber?.forEachIndexed { i, e -> errPhone.value    += "* $e" + if (i != error.phoneNumber.lastIndex) "\n" else ""}
        error.address?.forEachIndexed     { i, e -> errAddress.value  += "* $e" + if (i != error.address.lastIndex)     "\n" else ""}
        error.file?.forEachIndexed        { i, e -> errImage.value    += "* $e" + if (i != error.file.lastIndex)        "\n" else ""}
    }
}