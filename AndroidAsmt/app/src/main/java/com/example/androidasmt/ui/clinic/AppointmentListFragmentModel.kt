package com.example.androidasmt.ui.clinic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.response.ClinicAppointmentResponse
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppointmentListFragmentModel: ViewModel() {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    val datetime = MutableLiveData(LocalDateTime.now())
    val formattedDate = datetime.map { it.format(dateFormatter) }

    val list = MutableLiveData<List<ClinicAppointmentResponse>>(listOf())

    val isLoading = MutableLiveData(true)

    suspend fun sendRequest(): Response<List<ClinicAppointmentResponse>> {
        // send api request
        return VaccineApi.retrofitService.getClinicAppointments(datetime.value.toString())
    }

    fun setData(response: List<ClinicAppointmentResponse>) {
        // assign the response to list and set the loading to false
        list.value = response
        isLoading.value = false
    }
}