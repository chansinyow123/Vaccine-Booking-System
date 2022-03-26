package com.example.androidasmt.rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidasmt.R
import com.example.androidasmt.network.response.ClinicAppointmentResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ClinicAppointmentAdapter (
    // Callback function
    val fn: (ViewHolder, ClinicAppointmentResponse) -> Unit = { _, _ ->}
) : ListAdapter<ClinicAppointmentResponse, ClinicAppointmentAdapter.ViewHolder>(DiffCallback) {

    // Static member
    companion object DiffCallback : DiffUtil.ItemCallback<ClinicAppointmentResponse>() {
        override fun areItemsTheSame(a: ClinicAppointmentResponse, b: ClinicAppointmentResponse) = a.appointmentTime == b.appointmentTime
        override fun areContentsTheSame(a: ClinicAppointmentResponse, b: ClinicAppointmentResponse) = a == b
    }

    // Inner class
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val txtTime: TextView = view.findViewById(R.id.txtTime)
        val rv: RecyclerView = view.findViewById(R.id.rv)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.rv_clinic_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        val datetimeObj = LocalDateTime.parse(item.appointmentTime, DateTimeFormatter.ISO_DATE_TIME)
        holder.txtTime.text  = "${datetimeObj.format(timeFormatter)}"

        fn(holder, item)
    }
}