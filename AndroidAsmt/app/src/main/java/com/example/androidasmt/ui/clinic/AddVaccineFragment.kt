package com.example.androidasmt.ui.clinic

import android.R
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.androidasmt.databinding.FragmentAddVaccineBinding
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import com.example.androidasmt.helper.hideKeyboard
import com.example.androidasmt.network.error.CreateVaccineError
import com.example.androidasmt.network.error.ErrorResponse
import com.example.androidasmt.network.error.RegisterClinicError
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.reflect.Type


class AddVaccineFragment : Fragment() {

    private lateinit var binding: FragmentAddVaccineBinding
    private val nav by lazy { findNavController() }

    private val vm: AddVaccineFragmentModel by viewModels()
    private val imgList: List<ImageView> = mutableListOf()
    private val edtList: List<EditText> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentAddVaccineBinding.inflate(inflater, container, false)

        binding.vm = vm
        binding.lifecycleOwner = this

        addDoseInput()

        binding.btnSubmit.setOnClickListener { submitForm() }
        binding.edtName.requestFocus()

        return binding.root
    }

    private fun submitForm() {
        hideKeyboard()             // Hide keyboard
        vm.isLoading.value = true  // display loading state in ui
        vm.clearError()            // clear the error

        lifecycleScope.launch {
            // send api request
            val response = withContext(Dispatchers.IO) {
                vm.submitForm()
            }

            vm.isLoading.value = false

            // check the statusCode
            when (response.code()) {
                in 200..299 -> status200to299() // success status
                400 -> status400(response) // BadRequest status
                403 -> status403() // Forbid Status
                else -> statusOther(response.code().toString()) // Other Status
            }
        }
    }

    private fun status200to299() {
        Snackbar.make(binding.root, "Vaccine Added!", Snackbar.LENGTH_SHORT).show()
        nav.navigateUp()
    }

    private fun status400(response: Response<Unit>) {
        // read the error response body
        val reader = response.errorBody()!!.charStream()
        val type: Type = object : TypeToken<ErrorResponse<CreateVaccineError>>() {}.type
        val errorResponse = Gson().fromJson<ErrorResponse<CreateVaccineError>>(reader, type)

        // set the error
        vm.setError(errorResponse.errors)

        // show toast error message
        Toast.makeText(requireContext(), "Invalid field detected!", Toast.LENGTH_SHORT).show()
    }

    private fun status403() {
        // display error alert dialog
        AlertDialog.Builder(requireContext())
            .setIcon(com.example.androidasmt.R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("This account has been deleted by admin.")
            .setPositiveButton("Got it!", null)
            .show()
    }

    private fun statusOther(code: String) {
        // display error alert dialog
        AlertDialog.Builder(requireContext())
            .setIcon(com.example.androidasmt.R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("Unexpected Error Occurred: $code")
            .setPositiveButton("Try again later", null)
            .show()
    }

    private fun addDoseInput() {

        // Clear Error
        vm.errDayRange.value = ""

        // set new linearlayout
        val linearLayout = LinearLayout(context)

        // set img size and other param
        val imgParam = LinearLayout.LayoutParams(120, 120)

        // set edt size and other param
        val edtParam = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        // add edt
        val edt = EditText(requireContext())
        edt.hint = "Dose ${vm.dayRange.count() + 2}"
        edt.layoutParams = edtParam
        edt.inputType = InputType.TYPE_CLASS_NUMBER

        // cache imgView for ic_add
        val imgAdd = ImageView(requireContext())
        imgAdd.layoutParams = imgParam
        imgAdd.setImageResource(com.example.androidasmt.R.drawable.ic_add)
        imgAdd.setOnClickListener {

            // if dayRange has more than 3 value, show error message
            if (vm.dayRange.count() >= 3) {
                Toast.makeText(requireContext(), "Dose Interval only allow 3 count", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val input = edt.text.toString().toIntOrNull() ?: 0

            // day validation
            if (input <= 0 || input > 1825) {
                vm.errDayRange.value = "Day must between 1 to 1825"
                return@setOnClickListener
            }

            vm.dayRange.add(input)           // add input to dayRange
            edt.isEnabled = false            // do not allow user to type previous field
            addDoseInput()                   // then add another layout
        }

        // if count is not empty, set the previous img to cross
        if (vm.dayRange.count() != 0) {

            // get the previous layout
            val previousId = vm.dayRange.count() - 1
            val previousLayout = binding.layoutDayRange.getChildAt(previousId) as LinearLayout

            val previousImg = previousLayout.getChildAt(0) as ImageView
            previousImg.setImageResource(com.example.androidasmt.R.drawable.ic_close)

            previousImg.setOnClickListener {

                // get the index of view first
                val indexOfView = binding.layoutDayRange.indexOfChild(previousLayout)
                vm.dayRange.removeAt(indexOfView)                      // remove value from list
                binding.layoutDayRange.removeView(previousLayout)      // remove previous view

                // get the current layout
                val currentLayout = binding.layoutDayRange.getChildAt(vm.dayRange.count()) as LinearLayout
                val currentEdt = currentLayout.getChildAt(1) as EditText
                currentEdt.hint = "Dose ${vm.dayRange.count() + 2}"    // change edt hint
            }
        }

        // add to view
        linearLayout.addView(imgAdd)                  // add img to linearlayout
        linearLayout.addView(edt)                     // add edt to linearlayout
        binding.layoutDayRange.addView(linearLayout)  // add linearlayout to parent
        edt.requestFocus()                            // request focus for next field
    }
}