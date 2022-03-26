package com.example.androidasmt.rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidasmt.R
import com.example.androidasmt.network.response.CustomerBookingResponse
import com.example.androidasmt.network.response.UserResponse
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CustomerBookingAdapter (
    // Callback function
    val fn: (ViewHolder, CustomerBookingResponse) -> Unit = { _, _ ->}
) : ListAdapter<CustomerBookingResponse, CustomerBookingAdapter.ViewHolder>(DiffCallback) {

    // Static member
    companion object DiffCallback : DiffUtil.ItemCallback<CustomerBookingResponse>() {
        override fun areItemsTheSame(a: CustomerBookingResponse, b: CustomerBookingResponse) = a.id == b.id
        override fun areContentsTheSame(a: CustomerBookingResponse, b: CustomerBookingResponse) = a == b
    }

    // Inner class
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val txtVaccine: TextView = view.findViewById(R.id.txtVaccine)
        val txtAddress: TextView = view.findViewById(R.id.txtAddress)
        val txtDateTime: TextView = view.findViewById(R.id.txtDateTime)
        val txtStatus: TextView = view.findViewById(R.id.txtStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.rv_customer_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.txtVaccine.text = item.vaccineName
        holder.txtAddress.text = item.clinicAddress

        val datetime = LocalDateTime.parse(item.actionDateTime, DateTimeFormatter.ISO_DATE_TIME)
        val readableFormat = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")

        holder.txtDateTime.text = datetime.format(readableFormat)

        holder.txtStatus.text = when (item.completed) {
            null -> "Progressing"
            true -> "Completed"
            false -> "Cancelled"
        }

        fn(holder, item)
    }
}