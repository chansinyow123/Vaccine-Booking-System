package com.example.androidasmt.ui.customer

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentBookAppointmentBinding
import com.example.androidasmt.helper.hideKeyboard
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.error.CreateBookingError
import com.example.androidasmt.network.error.CreateVaccineError
import com.example.androidasmt.network.error.ErrorResponse
import com.example.androidasmt.network.response.ClinicWithVaccineResponse
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.reflect.Type
import java.time.LocalDateTime

class BookAppointmentFragment : Fragment() {

    private lateinit var binding: FragmentBookAppointmentBinding
    private val nav by lazy { findNavController() }

    private val vm: BookAppointmentFragmentModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentBookAppointmentBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        // get the id from homeFragment
        vm.id = requireArguments().getString("id", "")

        // init data
        initData()

        // set the spinner date data
        vm.date.observe(viewLifecycleOwner) {
            val adpDate = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, it)
            adpDate.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spnDate.adapter = adpDate
        }
        // set the spinner time data
        vm.time.observe(viewLifecycleOwner) {
            val adpTime = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, it)
            adpTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spnTime.adapter = adpTime
        }
        // set the spinner vaccines data
        vm.vaccines.observe(viewLifecycleOwner) {
            val adpVaccines = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, it)
            adpVaccines.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spnVaccine.adapter = adpVaccines
        }

        // set the vaccine dialog whenever the index changed
        vm.vaccineIndex.observe(viewLifecycleOwner) {
            // if have not initialized, return
            if (vm.isInitializing.value == true) return@observe

            val args = bundleOf(
                "vaccineName" to vm.vaccines.value!!.get(it).name,
                "vaccineDescription" to vm.vaccines.value!!.get(it).description,
                "pricePerDose" to vm.vaccines.value!!.get(it).pricePerDose,
                "clinicAddress" to vm.address.value,
                "clinicEmail" to vm.email.value,
                "dayRange" to vm.vaccines.value!!.get(it).dayRange
            )

            binding.vaccineDialog.setOnClickListener {
                nav.navigate(R.id.vaccineInfoFragment, args)
            }
        }

        vm.file.observe(viewLifecycleOwner) {
            // set the default image if it is null, else display image
            if (it.isNullOrEmpty()) {
                binding.imgClinic.setImageResource(R.drawable.ic_image)
            }
            else {
                val byteArray = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.imgClinic.load(bitmap) {
                    placeholder(R.drawable.loading_ani)
                }
            }
        }

        binding.btnSubmit.setOnClickListener{ submitForm() }

        return binding.root
    }

    private fun submitForm() {
        hideKeyboard()             // Hide keyboard
        vm.isLoading.value = true  // display loading state in ui
        vm.clearError()            // clear the error

        lifecycleScope.launch {
            // send api request
            val response = withContext(Dispatchers.IO) {
                vm.submitForm()
            }

            vm.isLoading.value = false

            // check the statusCode
            when (response.code()) {
                in 200..299 -> status200to299() // success status
                400 -> status400(response) // BadRequest status
                else -> statusOther(response.code().toString()) // Other Status
            }
        }
    }

    private fun status200to299() {
        Snackbar.make(binding.root, "Booking Successfully!", Snackbar.LENGTH_SHORT).show()
        nav.navigateUp()
    }

    private fun status400(response: Response<Unit>) {
        // read the error response body
        val reader = response.errorBody()!!.charStream()
        val type: Type = object : TypeToken<ErrorResponse<CreateBookingError>>() {}.type
        val errorResponse = Gson().fromJson<ErrorResponse<CreateBookingError>>(reader, type)

        // set the error
        vm.setError(errorResponse.errors)

        // show toast error message
        Toast.makeText(requireContext(), "Invalid field detected!", Toast.LENGTH_SHORT).show()
    }

    private fun initData() {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                VaccineApi.retrofitService.getClinicWithVaccines(vm.id)
            }

            when(response.code()) {
                in 200..299 -> initStatus200to299(response.body()!!)
                else -> statusOther(response.code().toString())
            }
        }
    }

    private fun initStatus200to299(response: ClinicWithVaccineResponse) {
        // if there are no vaccine in this clinic, does not allow user to book
        if (response.vaccines.count() <= 0) {
            AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_error)
                .setTitle("Error")
                .setMessage("There are no vaccine in this clinic yet!")
                .setPositiveButton("Ok", null)
                .setOnDismissListener {
                    // navigate back
                    nav.navigateUp()
                }
                .show()
            return
        }

        vm.initData(response)
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