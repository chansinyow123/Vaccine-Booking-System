package com.example.androidasmt.ui.admin

import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentViewAccountBinding

class ViewAccountFragment : DialogFragment() {

    private lateinit var binding: FragmentViewAccountBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentViewAccountBinding.inflate(inflater, container, false)

        val email = requireArguments().getString("email", "")
        val name = requireArguments().getString("name", "")
        val address = requireArguments().getString("address", "")
        val ic = requireArguments().getString("ic", "")
        val phoneNumber = requireArguments().getString("phoneNumber", "")
        val file = requireArguments().getString("file", "")

        // set the default image if it is null, else display image
        if (file.isNullOrEmpty()) {
            binding.imgCustomer.setImageResource(R.drawable.ic_profile)
        }
        else {
            // convert to bitmap given the temp file location
            val bitmap = BitmapFactory.decodeFile(file)
            binding.imgCustomer.load(bitmap) {
                placeholder(R.drawable.loading_ani)
            }
        }

        binding.txtEmail.text = email
        binding.txtName.text = name
        binding.txtAddress.text = address
        binding.txtIC.text = ic
        binding.txtPhoneNumber.text = phoneNumber

        binding.btnDismiss.setOnClickListener { dismiss() }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}