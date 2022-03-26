package com.example.androidasmt.ui.admin

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.androidasmt.network.response.UserResponse

class AccountsFragmentModel: ViewModel() {
    val search = MutableLiveData("")
    val data = MutableLiveData<List<UserResponse>>(listOf())

    var list = MediatorLiveData<List<UserResponse>>()

    init {
        list.addSource(data)   { filter() }
        list.addSource(search) { filter() }
    }

    private fun filter() {
        list.value = data.value!!.filter {
            it.name!!.lowercase().contains(search.value!!.trim().lowercase()) ||
            it.email!!.lowercase().contains(search.value!!.trim().lowercase())
        }
    }

    val isInitializing = MutableLiveData(true)

    fun initData(response: List<UserResponse>) {
        data.value = response
        isInitializing.value = false
    }
}