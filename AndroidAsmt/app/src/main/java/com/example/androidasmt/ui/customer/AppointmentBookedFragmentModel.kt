package com.example.androidasmt.ui.customer

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.response.CustomerBookingResponse

class AppointmentBookedFragmentModel: ViewModel() {
    val search = MutableLiveData("")
    val data = MutableLiveData<List<CustomerBookingResponse>>(listOf())

    var list = MediatorLiveData<List<CustomerBookingResponse>>()

    init {
        list.addSource(data)   { filter() }
        list.addSource(search) { filter() }
    }

    private fun filter() {
        list.value = data.value!!.filter {
            it.clinicAddress.lowercase().contains(search.value!!.trim().lowercase())
            || it.vaccineName.lowercase().contains(search.value!!.trim().lowercase())
        }
    }

    val isInitializing = MutableLiveData(true)

    fun initData(response: List<CustomerBookingResponse>) {
        data.value = response
        isInitializing.value = false
    }
}