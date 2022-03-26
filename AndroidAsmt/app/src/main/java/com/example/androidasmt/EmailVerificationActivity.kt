package com.example.androidasmt

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.androidasmt.databinding.ActivityEmailVerificationBinding
import com.example.androidasmt.databinding.ActivityResetPasswordBinding
import com.example.androidasmt.network.VaccineApi
import com.example.androidasmt.network.body.VerifyCustomerBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmailVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEmailVerificationBinding
    private val nav by lazy { supportFragmentManager.findFragmentById(R.id.host)!!.findNavController() }

    private val vm: EmailVerificationActivityModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // view binding
        binding = ActivityEmailVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get the token and uid from query parameter -------------------------------------------
        val data: Uri? = intent?.data

        vm.uid = data?.getQueryParameter("uid")
        vm.token = data?.getQueryParameter("token")?.replace(' ', '+')

        // if token or uid is not exist on url, show error dialog ------------------------------
        // else send api request
        if (vm.token.isNullOrEmpty() || vm.uid.isNullOrEmpty()) errorDialog()
        else submitForm()
    }

    private fun submitForm() {
        lifecycleScope.launch {
            // send the verification email with token and uid ------------------------------
            val response = withContext(Dispatchers.IO) {
                vm.submitForm()
            }

            when (response.code()) {
                in 200..299 -> successDialog()
                else -> errorDialog()
            }
        }
    }

    private fun successDialog() {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_success)
            .setTitle("Success")
            .setMessage("Email Verified")
            .setPositiveButton("Go to Login Screen", null)
            .setOnDismissListener {
                navigateToMainActivity()
            }
            .show()
    }

    private fun errorDialog() {
        AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_error)
            .setTitle("Error")
            .setMessage("Invalid Link")
            .setPositiveButton("Go to Login Screen", null)
            .setOnDismissListener {
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