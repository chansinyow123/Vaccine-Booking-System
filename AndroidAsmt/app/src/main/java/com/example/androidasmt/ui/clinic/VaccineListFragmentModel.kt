package com.example.androidasmt.ui.clinic

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.response.VaccineResponse

class VaccineListFragmentModel: ViewModel() {
    val search = MutableLiveData("")
    val data = MutableLiveData<List<VaccineResponse>>(listOf())

    var list = MediatorLiveData<List<VaccineResponse>>()

    init {
        list.addSource(data)   { filter() }
        list.addSource(search) { filter() }
    }

    private fun filter() {
        list.value = data.value!!.filter {
            it.name.lowercase().contains(search.value!!.trim().lowercase())
        }
    }

    val isInitializing = MutableLiveData(true)

    fun initData(response: List<VaccineResponse>) {
        data.value = response
        isInitializing.value = false
    }
}