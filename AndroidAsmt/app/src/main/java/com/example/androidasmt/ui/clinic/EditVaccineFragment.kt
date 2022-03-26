package com.example.androidasmt.ui.clinic

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentEditVaccineBinding
import com.example.androidasmt.helper.hideKeyboard
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.error.EditClinicError
import com.example.androidasmt.network.error.EditVaccineError
import com.example.androidasmt.network.error.ErrorResponse
import com.example.androidasmt.network.response.VaccineResponse
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.reflect.Type

class EditVaccineFragment : Fragment() {

    private lateinit var binding: FragmentEditVaccineBinding
    private val nav by lazy { findNavController() }

    private val vm: EditVaccineFragmentModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditVaccineBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        vm.id = requireArguments().getInt("id", 0)

        initData()

        binding.btnEdit.setOnClickListener { submitEditForm() }
        binding.btnDelete.setOnClickListener { submitDeleteForm() }

        return binding.root
    }

    private fun initData() {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                VaccineApi.retrofitService.getVaccineById(vm.id)
            }

            when(response.code()) {
                in 200..299 -> setData(response.body()!!)
                else -> statusOther(response.code().toString())
            }
        }
    }

    private fun setData(body: VaccineResponse) {
        vm.initData(body)
        displayDoseInterval(vm.dayRange.value!!)
    }

    private fun displayDoseInterval(dayRange: List<Int>) {
        // remove all view in linearlayout first
        binding.layoutDayRange.removeAllViews()

        // set edt size and other param
        val txtParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // ordinal number
        val ordinalNum = listOf("1st", "2nd", "3rd", "4th", "5th")

        // padding
        val paddingLeft = 0
        val paddingTop = 30
        val paddingRight = 0
        val paddingBottom = 0

        // add DoseRequired txt
        val txtDoseRequired = TextView(requireContext())
        txtDoseRequired.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        txtDoseRequired.layoutParams = txtParam

        // set DoseRequired Text
        txtDoseRequired.text = "Minimum ${1 + dayRange.count()} Dose"

        // add to the linearlayout
        binding.layoutDayRange.addView(txtDoseRequired)

        // loop through dayRange count
        for (i in 1..dayRange.count()) {
            // add DoseInterval txt
            val txtDoseInterval = TextView(requireContext())
            txtDoseInterval.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
            txtDoseInterval.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            txtDoseInterval.layoutParams = txtParam

            // set DoseInterval txt
            txtDoseInterval.text = "${ ordinalNum.get(i) } Dose Interval: ${ dayRange.get(i - 1) } days"

            // add to the linearlayout
            binding.layoutDayRange.addView(txtDoseInterval)
        }
    }

    private fun submitEditForm() {
        hideKeyboard()             // Hide keyboard
        vm.isLoading.value = true  // display loading state in ui
        vm.clearError()            // clear the error

        lifecycleScope.launch {
            // send api request ---------------------------------------------------------------
            val response = withContext(Dispatchers.IO) {
                vm.submitEditForm()
            }

            vm.isLoading.value = false

            // check the statusCode
            when (response.code()) {
                in 200..299 -> editStatus200to299() // success status
                400 -> status400(response) // BadRequest status
                else -> statusOther(response.code().toString()) // Other Status
            }
        }
    }

    private fun editStatus200to299() {
        // display success snackbar and navigate to clinic screen
        Snackbar.make(binding.root, "Vaccine Edited!", Snackbar.LENGTH_SHORT).show()
        nav.navigateUp()
    }

    private fun submitDeleteForm() {
        hideKeyboard()             // Hide keyboard
        vm.isLoading.value = true  // display loading state in ui
        vm.clearError()            // clear the error

        lifecycleScope.launch {
            // send api request ---------------------------------------------------------------
            val response = withContext(Dispatchers.IO) {
                vm.submitDeleteForm()
            }

            vm.isLoading.value = false

            // check the statusCode
            when (response.code()) {
                in 200..299 -> deleteStatus200to299() // success status
                else -> statusOther(response.code().toString()) // Other Status
            }
        }
    }

    private fun deleteStatus200to299() {
        Snackbar.make(binding.root, "Vaccine Removed!", Snackbar.LENGTH_SHORT).show()
        nav.navigateUp()
    }

    private fun status400(response: Response<Unit>) {
        // read the error response body
        val reader = response.errorBody()!!.charStream()
        val type: Type = object : TypeToken<ErrorResponse<EditVaccineError>>() {}.type
        val errorResponse = Gson().fromJson<ErrorResponse<EditVaccineError>>(reader, type)

        // set the error
        vm.setError(errorResponse.errors)

        // show toast error message
        Toast.makeText(requireContext(), "Invalid field detected!", Toast.LENGTH_SHORT).show()
    }

    private fun statusOther(statusCode: String) {
        // display error alert dialog
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("Unexpected Error Occurred: $statusCode")
            .setPositiveButton("Try again later", null)
            .show()
    }
}