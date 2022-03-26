package com.example.androidasmt.ui.clinic

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.androidasmt.R
import com.example.androidasmt.databinding.FragmentAppointmentDetailsBinding
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.response.AppointmentResponse
import com.example.androidasmt.ui.customer.StatusFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AppointmentDetailsFragment : Fragment() {

    private lateinit var binding: FragmentAppointmentDetailsBinding
    private val nav by lazy { findNavController() }

    private val vm: AppointmentDetailsFragmentModel by viewModels()

    companion object {
        private const val TAG = "CameraXBasic"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            // to show all the permission available ------------------------------------------
            // For Debugging Purposes
            permissions.entries.forEach {
                Log.e(TAG, "${it.key} = ${it.value}")
            }

            // check if is granted -----------------------------------------------------------
            val granted = permissions.entries.all {
                it.value == true
            }

            // if permission granted, open camera --------------------------------------------
            // else navigateUp with error alert dialog
            if (granted) {
                printPDF()
            }
            else {
                // display error alert dialog
                AlertDialog.Builder(requireContext())
                    .setIcon(R.drawable.ic_error)
                    .setTitle("Error")
                    .setMessage("Please permit storage permissions in your setting.")
                    .setPositiveButton("Dismiss", null)
                    .show()
            }
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAppointmentDetailsBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        vm.id = requireArguments().getInt("id", 0)

        initData()

        vm.file.observe(viewLifecycleOwner) {
            // set the default image if it is null, else display image
            if (it.isNullOrEmpty()) {
                binding.image.setImageResource(R.drawable.ic_profile)
            }
            else {
                val byteArray = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                binding.image.load(bitmap) {
                    placeholder(R.drawable.loading_ani)
                }
            }
        }

        vm.completed.observe(viewLifecycleOwner) { c ->
            // set txtStatus text --------------------------------------------
            binding.txtStatus.text = when (c) {
                null -> "Progressing"
                true -> "Completed"
                false -> "Cancelled"
            }

            // set status color ----------------------------------------------
            val color = when (c) {
                null -> R.color.progressing
                true -> R.color.completed
                false -> R.color.cancelled
            }
            binding.txtStatus.setTextColor(ContextCompat.getColor(requireContext(), color));
        }

        vm.appointments.observe(viewLifecycleOwner) { a -> appendAppointmentList(a) }

        binding.txtVaccine.setOnClickListener {
            val args = bundleOf(
                "vaccineName" to vm.vaccineName.value,
                "vaccineDescription" to vm.vaccineDescription.value,
                "pricePerDose" to vm.vaccinePricePerDose.value,
                "clinicAddress" to vm.clinicAddress.value,
                "clinicEmail" to vm.clinicEmail.value,
                "dayRange" to vm.dayRange.value,
            )

            nav.navigate(R.id.vaccineInfoFragment, args)
        }

        // Print PDF ------------------------------------------------------------
        binding.btnPrintPDF.setOnClickListener {
            // if permission granted, print pdf
            // else request permission
            if (allPermissionsGranted()) {
                printPDF()
            } else {
                requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
            }
        }

        return binding.root
    }

    private fun printPDF() {
        // create a new document
        val document = PdfDocument()

        // create a page description
        val pageInfo = PdfDocument.PageInfo.Builder(1000, 4000, 1).create()

        // start a page
        val page = document.startPage(pageInfo)

        // draw something on the page
        val content = binding.root
        content.draw(page.canvas)

        // finish the page
        document.finishPage(page)

        // below line is used to set the name of
        // the PDF file and its path.
        val datetimeFormat = DateTimeFormatter.ofPattern("ddMMMyyyy_HHmm")
        val fileName = LocalDateTime.now().format(datetimeFormat) + ".pdf"
        val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        // add more pages
        // ...

        // write the document content
        try {
            // after creating a file name we will
            // write our PDF file to that location.
            document.writeTo(FileOutputStream(file))

            // below line is to print Toast message
            // on completion of PDF generation.
            Toast.makeText(
                requireContext(),
                "${file.absolutePath}, $fileName generated successfully.",
                Toast.LENGTH_LONG
            ).show()

            // Open File Intent to view the downloaded file
            val target = Intent(Intent.ACTION_VIEW)
            val fileUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().applicationContext.packageName + ".provider",
                file,
            );
            target.setDataAndType(fileUri, "application/pdf")
            target.flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION

            val intent = Intent.createChooser(target, "Open File")
            startActivity(intent)

        } catch (e: IOException) {
            // below line is used
            // to handle error
            e.printStackTrace()
            Toast.makeText(requireContext(),"Error", Toast.LENGTH_LONG).show()
        }

        // close the document
        document.close()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun appendAppointmentList(a: List<AppointmentResponse>) {
        // remove all view in linearlayout first
        binding.layoutAppointments.removeAllViews()

        // set edt size and other param
        val txtParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // ordinal number
        val ordinalNum = listOf("1st", "2nd", "3rd", "4th", "5th")

        // datetime formatter
        val datetimeFormat = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")

        // padding
        val paddingLeft = 0
        val paddingTop = 30
        val paddingRight = 0
        val paddingBottom = 30

        // loop through day range
        for (i in 0..vm.dayRange.value!!.count()) {

            // add appointment txt
            val txtAppointment = TextView(requireContext())
            txtAppointment.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
            txtAppointment.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            txtAppointment.layoutParams = txtParam

            // add attend txt
            val txtAttend = TextView(requireContext())
            txtAttend.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
            txtAttend.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            txtAttend.layoutParams = txtParam

            // prepare the text
            var datetimeObj: LocalDateTime
            var appointmentLogo = "❌"
            var appointmentDateTime = ""
            var attendLogo = "❌"
            var attendDateTime = ""

            // if dayRange is less than appointment, then set the logo to tick, and set datetime
            if (i < a.count()) {
                appointmentLogo = "✅"
                datetimeObj =
                    LocalDateTime.parse(a[i].appointmentDateTime, DateTimeFormatter.ISO_DATE_TIME)
                appointmentDateTime = "(${datetimeObj.format(datetimeFormat)})"

                // if attendDateTime is not null
                // then assign tick logo and set datetime
                if (a[i].attendDateTime != null) {
                    attendLogo = "✅"
                    datetimeObj =
                        LocalDateTime.parse(a[i].attendDateTime, DateTimeFormatter.ISO_DATE_TIME)
                    attendDateTime = "(${datetimeObj.format(datetimeFormat)})"
                }
            }

            // set the text
            txtAppointment.text =
                "$appointmentLogo ${ordinalNum[i]} Dose Appointment $appointmentDateTime"
            txtAttend.text = "$attendLogo ${ordinalNum[i]} Dose Completed $attendDateTime"

            // add txt to linearlayout
            binding.layoutAppointments.addView(txtAppointment)
            binding.layoutAppointments.addView(txtAttend)
        }

        // add fully vaccinated txt -------------------------------------------------
        // add fullVac txt
        val txtFullyVac = TextView(requireContext())
        txtFullyVac.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom)
        txtFullyVac.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        txtFullyVac.layoutParams = txtParam

        // if completed, show message fully vaccinated
        if (vm.completed.value == true) txtFullyVac.text = "✅ Fully Vaccinated"
        else txtFullyVac.text = "❌ Not Fully Vaccinated"

        // add to the linearlayout
        binding.layoutAppointments.addView(txtFullyVac)
    }

    private fun initData() {
        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                VaccineApi.retrofitService.getBookingInfo(vm.id)
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