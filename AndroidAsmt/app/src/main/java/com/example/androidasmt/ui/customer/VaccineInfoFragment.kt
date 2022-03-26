package com.example.androidasmt.ui.customer

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentVaccineInfoBinding

class VaccineInfoFragment : DialogFragment() {

    private lateinit var binding: FragmentVaccineInfoBinding
    private val nav by lazy { findNavController() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentVaccineInfoBinding.inflate(inflater, container, false)

        val vaccineName = requireArguments().getString("vaccineName", "")
        val vaccineDescription = requireArguments().getString("vaccineDescription", "")
        val pricePerDose = requireArguments().getDouble("pricePerDose", 0.0)
        val clinicAddress = requireArguments().getString("clinicAddress", "")
        val clinicEmail = requireArguments().getString("clinicEmail", "")
        val dayRange = requireArguments().getIntegerArrayList("dayRange")!!.toList()

        binding.txtVaccineName.text = vaccineName
        binding.txtVaccineDescription.text = vaccineDescription
        binding.txtPricePerDose.text = "RM %.2f".format(pricePerDose)
        binding.txtClinicAddress.text = clinicAddress
        binding.txtClinicEmail.text = clinicEmail

        displayDoseInterval(dayRange)

        binding.btnDismiss.setOnClickListener { dismiss() }

        return binding.root
    }

    private fun displayDoseInterval(dayRange: List<Int>) {
        // remove all view in linearlayout first
        binding.layoutDayRange.removeAllViews()

        // set edt size and other param
        val txtParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // ordinal number
        val ordinalNum = listOf("1st", "2nd", "3rd", "4th", "5th")

        // padding
        val paddingLeft = 0
        val paddingTop = 30
        val paddingRight = 0
        val paddingBottom = 0

        // add DoseRequired txt
        val txtDoseRequired = TextView(requireContext())
        txtDoseRequired.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        txtDoseRequired.layoutParams = txtParam

        // set DoseRequired Text
        txtDoseRequired.text = "Minimum ${1 + dayRange.count()} Dose"

        // add to the linearlayout
        binding.layoutDayRange.addView(txtDoseRequired)

        // loop through dayRange count
        for (i in 1..dayRange.count()) {
            // add DoseInterval txt
            val txtDoseInterval = TextView(requireContext())
            txtDoseInterval.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
            txtDoseInterval.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            txtDoseInterval.layoutParams = txtParam

            // set DoseInterval txt
            txtDoseInterval.text = "${ ordinalNum.get(i) } Dose Interval: ${ dayRange.get(i - 1) } days"

            // add to the linearlayout
            binding.layoutDayRange.addView(txtDoseInterval)
        }

    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}