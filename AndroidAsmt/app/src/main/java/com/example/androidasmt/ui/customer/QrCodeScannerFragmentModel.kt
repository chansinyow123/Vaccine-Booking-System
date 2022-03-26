package com.example.androidasmt.ui.customer

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QrCodeScannerFragmentModel: ViewModel() {
    var id = 0

    val isLoading = MutableLiveData(false)
}