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
import com.example.androidasmt.network.response.VaccineResponse

class VaccineAdapter(
    // Callback function
    val fn: (VaccineAdapter.ViewHolder, VaccineResponse) -> Unit = { _, _ ->}
) : ListAdapter<VaccineResponse, VaccineAdapter.ViewHolder>(DiffCallback) {
    // Static member
    companion object DiffCallback : DiffUtil.ItemCallback<VaccineResponse>() {
        override fun areItemsTheSame(a: VaccineResponse, b: VaccineResponse) = a.id == b.id
        override fun areContentsTheSame(a: VaccineResponse, b: VaccineResponse) = a == b
    }

    // Inner class
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val root = view
        val txtName: TextView = view.findViewById(R.id.txtName)
        val txtPrice: TextView = view.findViewById(R.id.txtPrice)
        val txtDayRange: TextView = view.findViewById(R.id.txtDayRange)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.rv_vaccine, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.txtName.text = item.name
        holder.txtPrice.text = "RM %.2f".format(item.pricePerDose)
        holder.txtDayRange.text = item.dayRange.toString()

        fn(holder, item)
    }
}