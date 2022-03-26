package com.example.androidasmt.ui.admin

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentDeletedClinicBinding
import com.example.androidasmt.databinding.FragmentUndoDeletedClinicBinding
import com.example.androidasmt.helper.hideKeyboard
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.rv.ClinicAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UndoDeletedClinicFragment : Fragment() {

    private lateinit var binding: FragmentUndoDeletedClinicBinding
    private val nav by lazy { findNavController() }

    private val vm: UndoDeletedClinicFragmentModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentUndoDeletedClinicBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        vm.id = arguments?.getString("id") ?: ""

        initData()

        vm.file.observe(viewLifecycleOwner) {
            // set the default image if it is null, else display image
            if (it.isNullOrEmpty()) {
                binding.image.setImageResource(R.drawable.ic_image)
            }
            else {
                val byteArray = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.image.load(bitmap) {
                    placeholder(R.drawable.loading_ani)
                }
            }
        }

        binding.btnSubmit.setOnClickListener { submitForm() }

        return binding.root
    }

    private fun initData() {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                VaccineApi.retrofitService.getProfileById(vm.id)
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

        lifecycleScope.launch {
            // send api request ---------------------------------------------------------------
            val response = withContext(Dispatchers.IO) {
                vm.submitForm()
            }

            vm.isLoading.value = false

            // check the statusCode
            when (response.code()) {
                in 200..299 -> status200to299() // success status
                else -> statusOther(response.code().toString()) // Other Status
            }
        }
    }

    private fun status200to299() {
        // display success snackbar and navigate to clinic screen
        Snackbar.make(binding.root, "Undo Deleted Clinic Successful.", Snackbar.LENGTH_SHORT).show()
        nav.navigateUp()
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