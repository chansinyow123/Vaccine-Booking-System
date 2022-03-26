package com.example.androidasmt.helper

import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputLayout

object DataBindingAdapter {
    @BindingAdapter("errorText")
    @JvmStatic
    fun setErrorMessage(view: TextInputLayout, errorMessage: String?) {
        view.error = errorMessage
    }

    @BindingAdapter("items")
    fun setItems(view: Spinner, items: List<Any>) {
        val adapter = ArrayAdapter(view.context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        view.adapter = adapter
    }
}