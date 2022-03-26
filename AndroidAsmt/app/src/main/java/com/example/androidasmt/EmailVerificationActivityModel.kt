package com.example.androidasmt

import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.body.VerifyCustomerBody
import retrofit2.Response

class EmailVerificationActivityModel : ViewModel() {

    var uid: String? = null
    var token: String? = null

    suspend fun submitForm(): Response<Unit> {
        val body = VerifyCustomerBody(uid!!, token!!)
        return VaccineApi.retrofitService.verifyCustomerAccount(body)
    }
}