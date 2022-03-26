package com.example.androidasmt.ui.clinic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.response.AppointmentResponse
import com.example.androidasmt.network.response.BookingResponse

class AppointmentDetailsFragmentModel: ViewModel() {
    var id = 0
    val file = MutableLiveData("")
    val name = MutableLiveData("")
    val email = MutableLiveData("")
    val ic = MutableLiveData("")
    val phoneNumber = MutableLiveData("")
    val clinicAddress = MutableLiveData("")
    val clinicEmail = MutableLiveData("")
    val bookingDescription = MutableLiveData("")
    val vaccineName = MutableLiveData("")
    val vaccineDescription = MutableLiveData("")
    val vaccinePricePerDose = MutableLiveData(0.0)
    val dayRange = MutableLiveData<List<Int>>(listOf())
    val appointments = MutableLiveData<List<AppointmentResponse>>(listOf())
    val completed = MutableLiveData<Boolean?>(null)

    val isLoading = MutableLiveData(true)

    fun initData(response: BookingResponse) {
        completed.value = response.completed
        file.value = response.file
        name.value = response.name
        email.value = response.email
        ic.value = response.ic
        phoneNumber.value = response.phoneNumber
        clinicAddress.value = response.clinicAddress
        clinicEmail.value = response.clinicEmail
        bookingDescription.value = response.bookingDescription
        vaccineName.value = response.vaccineName
        vaccineDescription.value = response.vaccineDescription
        vaccinePricePerDose.value = response.vaccinePricePerDose
        dayRange.value = response.dayRange
        appointments.value = response.appointments

        isLoading.value = false
    }
}