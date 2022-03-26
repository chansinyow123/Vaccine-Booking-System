package com.example.androidasmt.ui.customer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.error.EditCustomerError
import com.example.androidasmt.network.error.RegisterCustomerError
import com.example.androidasmt.network.response.UserResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File

class EditProfileFragmentModel: ViewModel() {

    // input field
    var name = MutableLiveData("")
    var ic = MutableLiveData("")
    var phone = MutableLiveData("")
    var address = MutableLiveData("")
    var file = MutableLiveData("")

    // image upload
    var image: File? = null
    var isImageUpload = false
    // false = did not upload
    // true  = uploaded an image

    // disabled field
    var email = MutableLiveData("")

    var isInitializing = MutableLiveData(true)
    var isLoading = MutableLiveData(false)

    // error message
    val errImage = MutableLiveData("")
    val errName = MutableLiveData("")
    val errIC = MutableLiveData("")
    val errPhone = MutableLiveData("")
    val errAddress = MutableLiveData("")


    fun initData(response: UserResponse) {
        name.value = response.name
        ic.value = response.ic
        phone.value = response.phoneNumber
        address.value = response.address
        file.value = response.file
        email.value = response.email

        isInitializing.value = false
    }

    suspend fun submitForm(): Response<Unit> {

        var file: MultipartBody.Part? = null

        image?.let {

            // get the file requestBody
            val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/${it.extension}"), it)

            // MultipartBody.Part is used to send also the actual file name
            file = MultipartBody.Part.createFormData("file", it.name, requestFile)
        }

        // prepare body data
        val name     = RequestBody.create(MultipartBody.FORM, name.value!!)
        val address     = RequestBody.create(MultipartBody.FORM, address.value!!)
        val ic           = RequestBody.create(MultipartBody.FORM, ic.value!!)
        val phoneNumber  = RequestBody.create(MultipartBody.FORM, phone.value!!)
        val isFileUpload  = RequestBody.create(MultipartBody.FORM, isImageUpload.toString())

        // send api request with body data --------------------------------------
        return VaccineApi.retrofitService.editCustomer(
            name,
            address,
            ic,
            phoneNumber,
            file,
            isFileUpload
        )
    }

    fun clearError() {
        errImage.value = ""
        errName.value = ""
        errIC.value = ""
        errPhone.value = ""
        errAddress.value = ""
    }

    fun setError(error: EditCustomerError) {
        // show all the error
        error.file?.forEachIndexed        { i, e -> errImage.value    += "* $e" + if (i != error.file.lastIndex)        "\n" else ""}
        error.name?.forEachIndexed        { i, e -> errName.value     += "* $e" + if (i != error.name.lastIndex)        "\n" else ""}
        error.ic?.forEachIndexed          { i, e -> errIC.value       += "* $e" + if (i != error.ic.lastIndex)          "\n" else ""}
        error.phoneNumber?.forEachIndexed { i, e -> errPhone.value    += "* $e" + if (i != error.phoneNumber.lastIndex) "\n" else ""}
        error.address?.forEachIndexed     { i, e -> errAddress.value  += "* $e" + if (i != error.address.lastIndex)     "\n" else ""}
    }
}