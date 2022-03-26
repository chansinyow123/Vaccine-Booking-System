package com.example.androidasmt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import coil.load
import com.example.androidasmt.databinding.ActivityCustomerBinding
import com.example.androidasmt.helper.hideKeyboard
import com.google.android.material.snackbar.Snackbar
import android.graphics.BitmapFactory

import android.graphics.Bitmap




class ActivityCustomerViewModel : ViewModel() {

}

class CustomerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerBinding
    private val nav by lazy { supportFragmentManager.findFragmentById(R.id.host)!!.findNavController() }

    private val vm: ActivityCustomerViewModel by viewModels()

    // AppBarConfiguration
    private lateinit var abc: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // view binding
        binding = ActivityCustomerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set the fragment that needs navigation drawer
        abc = AppBarConfiguration(
            setOf(R.id.homeFragment, R.id.appointmentBookedFragment, R.id.changePasswordFragment, R.id.editProfileFragment),
            binding.drawerLayout
        )

        // setup action bar
        setupActionBarWithNavController(nav, abc)
        // allow drawer to handle navigation
        binding.navView.setupWithNavController(nav)

        // custom action for navigation drawer
        binding.navView.menu.findItem(R.id.logout).setOnMenuItemClickListener {

            // go to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)

            // close the drawer, opposite is: binding.drawerLayout.open()
            // binding.drawerLayout.close()

            // return true
            true
        }

        // get the header view
        // get the info passed from LoginFragment and put into header -----------------------------
        val name = intent.getStringExtra("name") ?: ""
        val email = intent.getStringExtra("email") ?: ""
        val file = intent.getStringExtra("file") ?: ""
        val header = binding.navView.getHeaderView(0)

        // set header image
        if (file.isNotEmpty()) {
            // convert to bitmap given the temp file location
            val bitmap = BitmapFactory.decodeFile(file)
            header.findViewById<ImageView>(R.id.image)
                .load(bitmap) {
                    placeholder(R.drawable.loading_ani)
                }
        }
        else {
            header.findViewById<ImageView>(R.id.image).setImageResource(R.drawable.ic_profile)
        }

        // set header details
        header.findViewById<TextView>(R.id.txtName).text = name
        header.findViewById<TextView>(R.id.txtEmail).text = email
    }

    // Setup navigation up
    override fun onSupportNavigateUp(): Boolean {
        // apply hamburger logo using abc config
        hideKeyboard()
        return nav.navigateUp(abc) || super.onSupportNavigateUp()
    }
}