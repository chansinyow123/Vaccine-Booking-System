package com.example.androidasmt.rv

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.androidasmt.R
import com.example.androidasmt.network.response.CustomerListResponse

class ClinicAppointmentCustomerAdapter (
    // Callback function
    val fn: (ViewHolder, CustomerListResponse) -> Unit = { _, _ ->}
) : ListAdapter<CustomerListResponse, ClinicAppointmentCustomerAdapter.ViewHolder>(DiffCallback) {

    // Static member
    companion object DiffCallback : DiffUtil.ItemCallback<CustomerListResponse>() {
        override fun areItemsTheSame(a: CustomerListResponse, b: CustomerListResponse) = a.bookingId == b.bookingId
        override fun areContentsTheSame(a: CustomerListResponse, b: CustomerListResponse) = a == b
    }

    // Inner class
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val img: ImageView = view.findViewById(R.id.img)
        val txtName: TextView = view.findViewById(R.id.txtName)
        val txtEmail: TextView = view.findViewById(R.id.txtEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.rv_clinic_appointment_customer, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        // set the default image if it is null, else display image
        if (item.file.isNullOrEmpty()) {
            holder.img.setImageResource(R.drawable.ic_profile)
        }
        else {
            val byteArray = Base64.decode(item.file, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            holder.img.load(bitmap) {
                placeholder(R.drawable.loading_ani)
            }
        }

        holder.txtName.text = item.name
        holder.txtEmail.text = item.email

        fn(holder, item)
    }
}