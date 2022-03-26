package com.example.androidasmt

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidasmt.databinding.ActivityResetPasswordBinding
import com.example.androidasmt.helper.hideKeyboard
import com.example.androidasmt.network.error.ResetPasswordError
import com.example.androidasmt.network.error.ErrorResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.lang.reflect.Type

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private val nav by lazy { supportFragmentManager.findFragmentById(R.id.host)!!.findNavController() }

    private val vm: ResetPasswordActivityModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // view binding
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.vm = vm
        binding.lifecycleOwner = this

        // get the token and uid from query parameter -------------------------------------------
        val data: Uri? = intent?.data

        vm.uid = data?.getQueryParameter("uid")
        vm.token = data?.getQueryParameter("token")?.replace(' ', '+')

        // if token or uid is not exist on url, navigate user to login screen -------------------
        if (vm.token.isNullOrEmpty() || vm.uid.isNullOrEmpty()) {
            AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_error)
                .setTitle("Error")
                .setMessage("Invalid Link!")
                .setPositiveButton("Go to Login Screen", null)
                .setOnDismissListener {
                    // navigate to main activity
                    navigateToMainActivity()
                }
                .show()
        }

        // listener
        binding.btnResetPassword.setOnClickListener { submitForm() }
        binding.linkLogin.setOnClickListener { navigateToMainActivity() }
    }

    private fun submitForm() {

        hideKeyboard()             // Hide keyboard
        vm.isLoading.value = true  // display loading state in ui
        vm.clearError()            // clear the error

        lifecycleScope.launch {
            val response = withContext(Dispatchers.IO) {
                vm.submitForm()
            }

            // after sent, disable loading state
            vm.isLoading.value = false

            when (response.code()) {
                in 200..299 -> status200to299()
                400 -> status400(response)
                else -> statusOther()
            }

        }
    }

    private fun status400(response: Response<Unit>) {
        // read the error response body
        val reader = response.errorBody()!!.charStream()
        val type: Type = object : TypeToken<ErrorResponse<ResetPasswordError>>() {}.type
        val errorResponse = Gson().fromJson<ErrorResponse<ResetPasswordError>>(reader, type)

        // set the error
        vm.setError(errorResponse.errors)

        // show toast error message
        Toast.makeText(this, "Invalid field detected!", Toast.LENGTH_SHORT).show()
    }


    private fun status200to299() {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_success)
            .setTitle("Success")
            .setMessage("Password has been reset!")
            .setPositiveButton("Go to Login Screen", null)
            .setOnDismissListener {
                // navigate to main activity
                navigateToMainActivity()
            }
            .show()
    }

    private fun statusOther() {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("Token expired, please request the link again.")
            .setPositiveButton("Go to Login Screen", null)
            .setOnDismissListener {
                // navigate to main activity
                navigateToMainActivity()
            }
            .show()
    }

    private fun navigateToMainActivity() {
        // navigate to login screen
        val mainIntent = Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        // navigate to next activity
        startActivity(mainIntent)
    }

}