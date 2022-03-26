package com.example.androidasmt.ui.admin

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentAddClinicBinding
import com.example.androidasmt.helper.FileHelper
import com.example.androidasmt.helper.hideKeyboard
import com.example.androidasmt.network.error.ErrorResponse
import com.example.androidasmt.network.error.RegisterClinicError
import com.example.androidasmt.network.error.RegisterCustomerError
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File
import java.lang.reflect.Type

class AddClinicFragment : Fragment() {

    private lateinit var binding: FragmentAddClinicBinding
    private val nav by lazy { findNavController() }

    private val vm: AddClinicFragmentModel by viewModels()

    // ActivityResultLauncher
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
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
        binding = FragmentAddClinicBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        binding.image.setOnClickListener { pickImageUploadMethod() }
        binding.btnSubmit.setOnClickListener { submitForm() }

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
                        binding.image.setImageResource(R.drawable.ic_image)
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
            // send api request
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
        Snackbar.make(binding.root, "Clinic Added!", Snackbar.LENGTH_SHORT).show()
        Toast.makeText(requireContext(), "Clinic added!", Toast.LENGTH_SHORT).show()
        nav.navigateUp()
    }

    private fun status400(response: Response<Unit>) {
        // read the error response body
        val reader = response.errorBody()!!.charStream()
        val type: Type = object : TypeToken<ErrorResponse<RegisterClinicError>>() {}.type
        val errorResponse = Gson().fromJson<ErrorResponse<RegisterClinicError>>(reader, type)

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