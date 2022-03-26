package com.example.androidasmt.ui.anonymous

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentRegisterBinding
import com.example.androidasmt.helper.FileHelper
import com.example.androidasmt.helper.hideKeyboard
import com.example.androidasmt.network.error.RegisterCustomerError
import com.example.androidasmt.network.error.ErrorResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File
import java.lang.reflect.Type

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val nav by lazy { findNavController() }

    private val vm: RegisterFragmentModel by viewModels()

    // ActivityResultLauncher
    private val launcher = registerForActivityResult(StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            // get the file uri
            val uri = it.data?.data

            // if uri is not null, then assign image
            if (uri != null) {
                // get the path from uri, if not null, then assign file object
                val pathFromUri = FileHelper.getPath(requireContext(), uri)
                if (pathFromUri != null) {
                    binding.image.setImageURI(uri)
                    vm.image = File(pathFromUri)
                    // Toast.makeText(requireContext(), vm.image!!.path, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        binding.image.setOnClickListener { pickImageUploadMethod() }
        binding.btnRegister.setOnClickListener { submitForm() }

        return binding.root
    }

    private fun pickImageUploadMethod() {
        // display 2 selection dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Pick an image upload method")
            .setItems(R.array.image_selection) { _, index ->
                when (index) {
                    0 -> accessStorage()
                    1 -> {
                        // set back to default
                        binding.image.setImageResource(R.drawable.ic_profile)
                        vm.image = null
                    }
                }
            }
            .show()
    }

    private fun accessStorage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        launcher.launch(intent)
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
        // display success alert dialog and navigate to login screen
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_success)
            .setTitle("Success")
            .setMessage("Account has been registered! Remember to verify your email before login.")
            .setPositiveButton("Go to Login Screen", null)
            .setOnDismissListener {
                // navigate to login screen
                nav.navigateUp()
            }
            .show()
    }

    private fun status400(response: Response<Unit>) {
        // read the error response body
        val reader = response.errorBody()!!.charStream()
        val type: Type = object : TypeToken<ErrorResponse<RegisterCustomerError>>() {}.type
        val errorResponse = Gson().fromJson<ErrorResponse<RegisterCustomerError>>(reader, type)

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