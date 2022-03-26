package com.example.androidasmt.ui.anonymous

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentForgotPasswordBinding
import com.example.androidasmt.helper.hideKeyboard
import com.example.androidasmt.network.error.ForgotPasswordError
import com.example.androidasmt.network.error.ErrorResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.reflect.Type

class ForgotPasswordFragment : Fragment() {

    private lateinit var binding: FragmentForgotPasswordBinding
    private val nav by lazy { findNavController() }

    private val vm: ForgotPasswordFragmentModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)

        // data binding
        binding.vm = vm
        binding.lifecycleOwner = this

        binding.btnSendLink.setOnClickListener { submitForm() }

        return binding.root
    }

    private fun submitForm() {

        hideKeyboard()             // Hide keyboard
        vm.isLoading.value = true  // display loading state in ui
        vm.clearError()            // clear the error

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                vm.submitForm()
            }

            // after sent, disable loading state
            vm.isLoading.value = false

            // check status code
            when (response.code()) {
                in 200..299 -> status200to299() // success status
                400 -> status400(response) // BadRequest status
                404 -> status404() // NotFound status
                else -> statusOther(response) // Other status
            }
        }
    }

    private fun status200to299() {
        // display success alert dialog
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_success)
            .setTitle("Success")
            .setMessage("Link has been sent to your email!")
            .setPositiveButton("Dismiss", null)
            .show()
    }

    private fun status400(response: Response<Unit>) {
        // read the error response body
        val reader = response.errorBody()!!.charStream()
        val type: Type = object : TypeToken<ErrorResponse<ForgotPasswordError>>() {}.type
        val errorResponse = Gson().fromJson<ErrorResponse<ForgotPasswordError>>(reader, type)

        // set the error
        vm.setError(errorResponse.errors)

        // show toast error message
        Toast.makeText(requireContext(), "Invalid email field!", Toast.LENGTH_SHORT).show()
    }

    private fun status404() {
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("This email does not exist!")
            .setPositiveButton("Dismiss", null)
            .show()
    }

    private fun statusOther(response: Response<Unit>) {
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("Unexpected error occured: " + response.code().toString())
            .setPositiveButton("Dismiss", null)
            .show()
    }
}