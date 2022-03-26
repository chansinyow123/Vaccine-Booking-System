package com.example.androidasmt.ui.admin

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.error.EditClinicError
import com.example.androidasmt.network.response.UserResponse
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import java.io.File

class EditClinicFragmentModel: ViewModel() {
    var id = ""
    val email = MutableLiveData("")
    val address = MutableLiveData("")
    val file = MutableLiveData("")

    // image upload
    var image: File? = null
    var isImageUpload = false

    val isLoading = MutableLiveData(false)
    val isInitializing = MutableLiveData(true)

    val errImage = MutableLiveData("")
    val errAddress = MutableLiveData("")

    fun initData(response: UserResponse) {
        address.value = response.address
        email.value = response.email
        file.value = response.file

        isInitializing.value = false
    }

    suspend fun submitEditForm(): Response<Unit> {

        var file: MultipartBody.Part? = null

        image?.let {

            // get the file requestBody
            val requestFile: RequestBody = RequestBody.create(MediaType.parse("image/${it.extension}"), it)

            // MultipartBody.Part is used to send also the actual file name
            file = MultipartBody.Part.createFormData("file", it.name, requestFile)
        }

        // prepare body data
        val userId = RequestBody.create(MultipartBody.FORM, id)
        val address = RequestBody.create(MultipartBody.FORM, address.value!!)
        val isFileUpload = RequestBody.create(MultipartBody.FORM, isImageUpload.toString())

        // send api request with body data
        return VaccineApi.retrofitService.editClinic(
            userId,
            address,
            file,
            isFileUpload
        )
    }

    suspend fun submitDeleteForm(): Response<Unit> {
        // send api request with body data
        return VaccineApi.retrofitService.deleteClinic(id)
    }

    fun clearError() {
        errImage.value = ""
        errAddress.value = ""
    }

    fun setError(error: EditClinicError) {
        // show all the error
        error.file?.forEachIndexed        { i, e -> errImage.value    += "* $e" + if (i != error.file.lastIndex)        "\n" else ""}
        error.address?.forEachIndexed     { i, e -> errAddress.value  += "* $e" + if (i != error.address.lastIndex)     "\n" else ""}
    }
}