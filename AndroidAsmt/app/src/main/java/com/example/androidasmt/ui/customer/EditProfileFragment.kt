package com.example.androidasmt.ui.customer

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentEditProfileBinding
import com.example.androidasmt.helper.FileHelper
import com.example.androidasmt.helper.hideKeyboard
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.error.EditCustomerError
import com.example.androidasmt.network.error.ErrorResponse
import com.example.androidasmt.network.error.RegisterCustomerError
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File
import java.lang.reflect.Type

class EditProfileFragment : Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private val nav by lazy { findNavController() }

    private val vm: EditProfileFragmentModel by viewModels()

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
                    vm.isImageUpload = true
                    // Toast.makeText(requireContext(), vm.image!!.path, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        initData()

        vm.file.observe(viewLifecycleOwner) {
            // set the default image if it is null, else display image
            if (it.isNullOrEmpty()) {
                binding.image.setImageResource(R.drawable.ic_profile)
            }
            else {
                val byteArray = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.image.load(bitmap) {
                    placeholder(R.drawable.loading_ani)
                }
            }
        }

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
                        binding.image.setImageResource(R.drawable.ic_profile)
                        vm.image = null
                        vm.isImageUpload = true
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

    private fun initData() {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                VaccineApi.retrofitService.getProfile()
            }

            when(response.code()) {
                in 200..299 -> vm.initData(response.body()!!)
                else -> statusOther(response.code().toString())
            }
        }
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
                else -> statusOther(response.code().toString()) // Other Status
            }
        }
    }

    private fun status200to299() {
        // display success alert dialog and navigate to login screen
        AlertDialog.Builder(requireContext())
            .setIcon(R.drawable.ic_success)
            .setTitle("Success")
            .setMessage("Profile Edited!")
            .setPositiveButton("Dismiss", null)
            .show()

        // update the header image
        updateHeaderImage()
    }

    private fun updateHeaderImage() {
        // change header imageView if image is upload
        if (vm.isImageUpload) {

            // get the navigationView header from the activity
            val header = requireActivity().findViewById<NavigationView>(R.id.navView).getHeaderView(0)

            // if image is null, then assign default profile image
            // else display the image
            if (vm.image == null) {
                header.findViewById<ImageView>(R.id.image).setImageResource(R.drawable.ic_profile)
            }
            else {
                header.findViewById<ImageView>(R.id.image)
                    .load(vm.image) {
                        placeholder(R.drawable.loading_ani)
                    }
            }
        }
    }

    private fun status400(response: Response<Unit>) {
        // read the error response body
        val reader = response.errorBody()!!.charStream()
        val type: Type = object : TypeToken<ErrorResponse<EditCustomerError>>() {}.type
        val errorResponse = Gson().fromJson<ErrorResponse<EditCustomerError>>(reader, type)

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