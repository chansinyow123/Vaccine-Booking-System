package com.example.androidasmt.network

import com.example.androidasmt.CustomerActivity
import com.example.androidasmt.network.body.*
import com.example.androidasmt.network.response.*
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

//private val retrofit = Retrofit.Builder()
//    .baseUrl("https://localhost:5001/api/")
//    .addConverterFactory(GsonConverterFactory.create())
//    .build()

interface VaccineApiService {

    @POST("account/login")
    suspend fun login(@Body body: LoginBody): Response<LoginResponse>

    @POST("password/forgot-password")
    suspend fun sendForgotPasswordLink(@Body body: ForgotPasswordBody): Response<Unit>

    @PUT("password/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordBody): Response<Unit>

    @Multipart
    @POST("customer")
    suspend fun registerCustomerAccount(
        @Part("username")    username    : RequestBody,
        @Part("password")    password    : RequestBody,
        @Part("name")        name        : RequestBody,
        @Part("ic")          ic          : RequestBody,
        @Part("phoneNumber") phoneNumber : RequestBody,
        @Part("address")     address     : RequestBody,
        @Part("deepLink")    deepLink    : RequestBody,
        @Part file : MultipartBody.Part?,
    ): Response<Unit>

    @POST("customer/verify")
    suspend fun verifyCustomerAccount(@Body body: VerifyCustomerBody): Response<Unit>

    @GET("account/get-profile")
    suspend fun getProfile(): Response<UserResponse>

    @PUT("password/change-password")
    suspend fun changePassword(@Body body: ChangePasswordBody): Response<Unit>

    @Multipart
    @PUT("customer")
    suspend fun editCustomer(
        @Part("name")         username     : RequestBody,
        @Part("address")      password     : RequestBody,
        @Part("ic")           address      : RequestBody,
        @Part("phoneNumber")  phoneNumber  : RequestBody,
        @Part file: MultipartBody.Part?,
        @Part("isFileUpload") isFileUpload : RequestBody,
    ): Response<Unit>

    @GET("account/clinics")
    suspend fun getClinicList(): Response<List<UserResponse>>

    @GET("account/customers")
    suspend fun getCustomerList(): Response<List<UserResponse>>

    @Multipart
    @POST("clinic")
    suspend fun registerClinic(
        @Part("username")    username : RequestBody,
        @Part("password")    password : RequestBody,
        @Part("address")     address  : RequestBody,
        @Part file: MultipartBody.Part?,
    ): Response<Unit>

    @Multipart
    @PUT("clinic")
    suspend fun editClinic(
        @Part("userId")       userId       : RequestBody,
        @Part("address")      address      : RequestBody,
        @Part file: MultipartBody.Part?,
        @Part("isFileUpload") isFileUpload : RequestBody,
    ): Response<Unit>

    @DELETE("clinic/{id}")
    suspend fun deleteClinic(@Path("id") id: String): Response<Unit>

    @GET("account/{id}")
    suspend fun getProfileById(@Path("id") id: String): Response<UserResponse>

    @GET("account/clinics/deleted")
    suspend fun getDeletedClinicList(): Response<List<UserResponse>>

    @POST("clinic/undo-delete/{id}")
    suspend fun undoDeleteClinic(@Path("id") id: String): Response<Unit>

    @GET("vaccine/clinic")
    suspend fun getAllVaccineByClinic(): Response<List<VaccineResponse>>

    @POST("vaccine")
    suspend fun createVaccine(@Body body: CreateVaccineBody): Response<Unit>

    @GET("vaccine/{id}")
    suspend fun getVaccineById(@Path("id") id: Int): Response<VaccineResponse>

    @PUT("vaccine")
    suspend fun editVaccine(@Body body: EditVaccineBody): Response<Unit>

    @DELETE("vaccine/{id}")
    suspend fun deleteVaccine(@Path("id") id: Int): Response<Unit>

    @GET("clinic/{id}/vaccines")
    suspend fun getClinicWithVaccines(@Path("id") id: String): Response<ClinicWithVaccineResponse>

    @POST("booking")
    suspend fun createBooking(@Body body: CreateBookingBody): Response<Unit>

    @GET("booking/customer")
    suspend fun getCustomerBooking(): Response<List<CustomerBookingResponse>>

    @GET("booking/{id}")
    suspend fun getBookingInfo(@Path("id") id: Int): Response<BookingResponse>

    @DELETE("booking/{id}")
    suspend fun cancelBooking(@Path("id") id: Int): Response<Unit>

    @POST("booking/proceed")
    suspend fun proceedBooking(@Body body: ProceedBookingBody): Response<Unit>

    @GET("booking/appointments")
    suspend fun getClinicAppointments(@Query("datetime") datetime: String): Response<List<ClinicAppointmentResponse>>

}

object VaccineApi {

    var token = ""

    private var interceptor: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            // Global Request ------------------------------------------------------
            // this is for adding headers in request interceptor
            val request = chain.request()
                .newBuilder()
                .header("Authorization", "Bearer $token")
                .build()

            chain.proceed(request)

//            // Global Response -----------------------------------------------------
//            val response = chain.proceed(request)
//
//            when (response.code()) {
//                401 -> {}
//            }
//
//            response
        }
        .build()

    private var okHttpClient = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .build()

    var retrofitService : VaccineApiService = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:5000/api/")
//        .baseUrl("http://192.168.0.126:5000/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .client(interceptor)
//        .client(okHttpClient)
        .build()
        .create(VaccineApiService::class.java)
}



















