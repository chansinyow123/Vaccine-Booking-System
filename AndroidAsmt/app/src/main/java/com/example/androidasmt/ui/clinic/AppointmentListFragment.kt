package com.example.androidasmt.ui.clinic

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentAppointmentListBinding
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.rv.ClinicAppointmentAdapter
import com.example.androidasmt.rv.ClinicAppointmentCustomerAdapter
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class AppointmentListFragment : Fragment() {

    private lateinit var binding: FragmentAppointmentListBinding
    private val nav by lazy { findNavController() }

    private val vm: AppointmentListFragmentModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAppointmentListBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        vm.datetime.observe(viewLifecycleOwner) { updateData(it) }

        binding.btnDatePicker.setOnClickListener { showDatePicker() }

        // outer recycler view
        val adapter = ClinicAppointmentAdapter { holder, item ->

            // inner recycler view
            val innerAdapter = ClinicAppointmentCustomerAdapter { innerHolder, innerItem ->
                // If click on entire item, navigate user to edit clinic
                innerHolder.root.setOnClickListener {
                    nav.navigate(
                        R.id.action_appointmentListFragment_to_appointmentDetailsFragment,
                        bundleOf("id" to innerItem.bookingId)
                    )
                }
            }

            holder.rv.adapter = innerAdapter
            holder.rv.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL))

            innerAdapter.submitList(item.customers)
        }

        binding.rv.adapter = adapter
        binding.rv.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))

        vm.list.observe(viewLifecycleOwner) { l -> adapter.submitList(l) }

        return binding.root
    }

    private fun showDatePicker() {

        val dateSelection = vm.datetime.value!!.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val picker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(dateSelection)
                .build()

        picker.addOnPositiveButtonClickListener {
            // Respond to positive button click.
            vm.datetime.value = LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault());
            //Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDateTime()
            //LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC)
        }

        picker.show(parentFragmentManager, "datePicker");
    }

    private fun updateData(datetime: LocalDateTime) {
        lifecycleScope.launch {

            // set loading to true
            vm.isLoading.value = true

            val response = withContext(Dispatchers.IO) {
                vm.sendRequest()
            }

            when(response.code()) {
                in 200..299 -> vm.setData(response.body()!!)
                else -> statusOther(vm.datetime.value.toString())
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