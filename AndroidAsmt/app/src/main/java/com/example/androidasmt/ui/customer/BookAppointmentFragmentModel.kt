package com.example.androidasmt.ui.customer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.body.CreateBookingBody
import com.example.androidasmt.network.body.CreateVaccineBody
import com.example.androidasmt.network.error.CreateBookingError
import com.example.androidasmt.network.error.CreateVaccineError
import com.example.androidasmt.network.response.ClinicWithVaccineResponse
import com.example.androidasmt.network.response.VaccineResponse
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class BookAppointmentFragmentModel: ViewModel() {
    var id = ""
    val address = MutableLiveData("")
    val email = MutableLiveData("")
    val file = MutableLiveData("")
    val date = MutableLiveData<List<String>>(listOf())
    val time = MutableLiveData<List<String>>(listOf())
    val vaccines = MutableLiveData<List<VaccineResponse>>(listOf())
    val dateIndex = MutableLiveData(0)
    val timeIndex = MutableLiveData(0)
    val vaccineIndex = MutableLiveData(0)
    val description = MutableLiveData("")

    val isLoading = MutableLiveData(false)
    val isInitializing = MutableLiveData(true)

    val errDateTime = MutableLiveData("")
    val errDescription = MutableLiveData("")

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")

    fun initData(response: ClinicWithVaccineResponse) {

        val currentDate = LocalDate.now()
        val dateList = mutableListOf<String>()

        // add on 30 days to date list
        for (i in 1..30) dateList.add(
            currentDate
                .plusDays(i.toLong())
                .format(dateFormatter)
        )

        // then assign to date livedata
        date.value = dateList

        // initialize operation time list
        time.value = listOf(
            "10:00",
            "11:00",
            "13:00",
            "14:00",
            "15:00",
            "16:00",
        )

        // init response
        address.value = response.address
        email.value = response.email
        file.value = response.file
        vaccines.value = response.vaccines

        isInitializing.value = false
    }

    suspend fun submitForm(): Response<Unit> {

        // prepare body data -----------------------------------------------------

        // format datetime
        val dateString = date.value!!.get(dateIndex.value!!)
        val timeString = time.value!!.get(timeIndex.value!!)
        val appointmentDateTime = LocalDateTime.parse("$dateString $timeString", dateTimeFormatter)

        val body = CreateBookingBody(
            vaccines.value!!.get(vaccineIndex.value!!).id,
            description.value!!,
            appointmentDateTime.toString()
        )

        // send api request with body data --------------------------------------
        return VaccineApi.retrofitService.createBooking(body)
    }

    fun clearError() {
        errDateTime.value = ""
        errDescription.value = ""
    }

    fun setError(error: CreateBookingError) {
        // show all the error
        error.description?.forEachIndexed          { i, e -> errDescription.value  += "* $e" + if (i != error.description.lastIndex)         "\n" else ""}
        error.appointmentDateTime?.forEachIndexed  { i, e -> errDateTime.value     += "* $e" + if (i != error.appointmentDateTime.lastIndex) "\n" else ""}
    }


}