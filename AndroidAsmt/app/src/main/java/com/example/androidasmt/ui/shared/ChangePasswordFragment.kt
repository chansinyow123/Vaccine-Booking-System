package com.example.androidasmt.ui.shared

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentChangePasswordBinding
import com.example.androidasmt.helper.hideKeyboard
import com.example.androidasmt.network.error.ChangePasswordError
import com.example.androidasmt.network.error.ErrorResponse
import com.example.androidasmt.network.error.RegisterCustomerError
import com.example.androidasmt.ui.anonymous.RegisterFragmentModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.reflect.Type

class ChangePasswordFragment : Fragment() {

    private lateinit var binding: FragmentChangePasswordBinding
    private val nav by lazy { findNavController() }

    private val vm: ChangePasswordFragmentModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        binding.btnSubmit.setOnClickListener { submitForm() }

        return binding.root
    }

    private fun submitForm() {

        hideKeyboard()             // Hide keyboard
        vm.isLoading.value = true  // display loading state in ui
        vm.clearError()            // clear the error

        lifecycleScope.launch {
            // send api request ---------------------------------------------------------------
            val response = withContext(Dispatchers.IO) {
                vm.submitForm()
            }

            vm.isLoading.value = false

            // check the statusCode
            when (response.code()) {
                in 200..299 -> status200to299() // success status
                400 -> status400(response) // BadRequest status
                else -> statusOther(response) // Other Status
            }
        }
    }

    private fun status200to299() {
        // clear the error
        vm.clearError()

        // display success alert dialog and navigate to login screen
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_success)
            .setTitle("Success")
            .setMessage("Password Changed!")
            .setPositiveButton("Dismiss", null)
            .show()
    }

    private fun status400(response: Response<Unit>) {
        // read the error response body
        val reader = response.errorBody()!!.charStream()
        val type: Type = object : TypeToken<ErrorResponse<ChangePasswordError>>() {}.type
        val errorResponse = Gson().fromJson<ErrorResponse<ChangePasswordError>>(reader, type)

        // set the error
        vm.setError(errorResponse.errors)

        // show toast error message
        Toast.makeText(requireContext(), "Invalid field detected!", Toast.LENGTH_SHORT).show()
    }

    private fun statusOther(response: Response<Unit>) {
        // display error alert dialog
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("Unexpected Error Occurred: " + response.code().toString())
            .setPositiveButton("Try again later", null)
            .show()
    }
}