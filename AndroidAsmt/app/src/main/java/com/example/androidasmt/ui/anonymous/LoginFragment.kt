package com.example.androidasmt.ui.anonymous

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.androidasmt.AdminActivity
import com.example.androidasmt.CustomerActivity
import com.example.androidasmt.ClinicActivity
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentLoginBinding
import com.example.androidasmt.helper.FileHelper
import com.example.androidasmt.helper.hideKeyboard
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.body.LoginBody
import com.example.androidasmt.network.response.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private val nav by lazy { findNavController() }

    private val vm: LoginFragmentModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        binding.btnLogin.setOnClickListener { submitForm() }

//        binding.btnCustomer.setOnClickListener {
//            vm.email.value = "chansinyow3@gmail.com"
//            vm.password.value = "password"
//            submitForm()
//        }
//
//        binding.btnAdmin.setOnClickListener {
//            vm.email.value = "chansinyow@gmail.com"
//            vm.password.value = "password"
//            submitForm()
//        }
//
//        binding.btnClinic.setOnClickListener {
//            vm.email.value = "chansinyow2@gmail.com"
//            vm.password.value = "password"
//            submitForm()
//        }

        binding.linkForgotPassword.setOnClickListener {
            nav.navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        binding.linkRegister.setOnClickListener {
            nav.navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return binding.root
    }

    private fun submitForm() {

        hideKeyboard()
        vm.isLoading.value = true

        lifecycleScope.launch {
            // send api request ---------------------------------------------------------------
            val response = withContext(Dispatchers.IO) {
                vm.submitForm()
            }

            vm.isLoading.value = false

            // check the statusCode
            when (response.code()) {
                in 200..299 -> status200to299(response.body()!!) // Success Status
                400, 401 -> invalidCredentials()
                404 -> status404() // Email not verified
                else -> statusOther(response) // Other Status Code
            }
        }
    }

    private fun status200to299(response: LoginResponse) {
        // assign activity intent depends on which user role
        val intent = when (response.userRole) {
            "Admin" -> Intent(activity, AdminActivity::class.java)
            "Clinic" ->
                Intent(activity, ClinicActivity::class.java)
                    .putExtra("email", response.email)
                    .putExtra("address", response.address)
                    .putExtra("file", FileHelper.createTempFile(requireContext(), response.file))
            "Customer" ->
                Intent(activity, CustomerActivity::class.java)
                    .putExtra("name", response.name)
                    .putExtra("email", response.email)
                    .putExtra("file", FileHelper.createTempFile(requireContext(), response.file))
            else -> null
        }

        // if intent is none of the above, show unexpected error message
        if (intent == null) {
            AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.ic_error)
                .setTitle("Error")
                .setMessage("Unexpected error occurred!")
                .setPositiveButton("Dismiss", null)
                .show()
            return
        }

        // set the token to VaccineApi
        VaccineApi.token = response.token

        // clear backstack and pass JWT Token to next activity
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        // navigate to next activity
        startActivity(intent)
    }

    private fun status404() {
        // display error alert dialog
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("Email not verified yet!")
            .setPositiveButton("Dismiss", null)
            .show()
    }

    private fun invalidCredentials() {
        // display error alert dialog
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("Invalid email and password!")
            .setPositiveButton("Dismiss", null)
            .show()
    }

    private fun statusOther(response: Response<LoginResponse>) {
        // display error alert dialog
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("Unexpected Error Occurred: " + response.code().toString())
            .setPositiveButton("Dismiss", null)
            .show()
    }
}