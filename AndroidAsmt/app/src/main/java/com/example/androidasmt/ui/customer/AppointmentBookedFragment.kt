package com.example.androidasmt.ui.customer

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentAppointmentBookedBinding
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.rv.ClinicAdapter
import com.example.androidasmt.rv.CustomerBookingAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppointmentBookedFragment : Fragment() {

    private lateinit var binding: FragmentAppointmentBookedBinding
    private val nav by lazy { findNavController() }

    private val vm: AppointmentBookedFragmentModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAppointmentBookedBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        val adapter = CustomerBookingAdapter { holder, item ->

            // set status color ----------------------------------------------
            val color = when (item.completed) {
                null -> R.color.progressing
                true -> R.color.completed
                false -> R.color.cancelled
            }
            holder.txtStatus.setTextColor(ContextCompat.getColor(requireContext(), color));

            // If click on entire item, navigate user to edit clinic --------
            holder.root.setOnClickListener {
                nav.navigate(
                    R.id.action_appointmentBookedFragment_to_statusFragment,
                    bundleOf("id" to item.id)
                )
            }
        }
        binding.rv.adapter = adapter
        binding.rv.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vm.list.observe(viewLifecycleOwner) { l -> adapter.submitList(l) }

        initData()

        return binding.root
    }

    private fun initData() {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                VaccineApi.retrofitService.getCustomerBooking()
            }

            when(response.code()) {
                in 200..299 -> vm.initData(response.body()!!)
                else -> statusOther(response.code().toString())
            }
        }
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