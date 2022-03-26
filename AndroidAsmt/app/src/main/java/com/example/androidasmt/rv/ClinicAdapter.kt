package com.example.androidasmt.rv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.androidasmt.R
import com.example.androidasmt.network.response.UserResponse
import android.graphics.BitmapFactory

import android.graphics.Bitmap
import android.util.Base64
import coil.load


class ClinicAdapter(
    // Callback function
    val fn: (ViewHolder, UserResponse) -> Unit = { _, _ ->}
) : ListAdapter<UserResponse, ClinicAdapter.ViewHolder>(DiffCallback) {

    // Static member
    companion object DiffCallback : DiffUtil.ItemCallback<UserResponse>() {
        override fun areItemsTheSame(a: UserResponse, b: UserResponse) = a.id == b.id
        override fun areContentsTheSame(a: UserResponse, b: UserResponse) = a == b
    }

    // Inner class
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val imgClinic: ImageView = view.findViewById(R.id.imgClinic)
        val txtAddress: TextView = view.findViewById(R.id.txtAddress)
        val txtEmail: TextView = view.findViewById(R.id.txtEmail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.rv_clinic, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)

        // set the default image if it is null, else display image
        if (item.file.isNullOrEmpty()) {
            holder.imgClinic.setImageResource(R.drawable.ic_image)
        }
        else {
            val byteArray = Base64.decode(item.file, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            holder.imgClinic.load(bitmap) {
                placeholder(R.drawable.loading_ani)
            }
        }
        holder.txtAddress.text = item.address
        holder.txtEmail.text = item.email

        fn(holder, item)
    }
}