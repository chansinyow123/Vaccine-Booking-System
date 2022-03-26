package com.example.androidasmt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.androidasmt.databinding.ActivityAdminBinding
import com.example.androidasmt.helper.hideKeyboard

class ActivityAdminViewModel : ViewModel() {
    var token = MutableLiveData("")
}

class AdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminBinding
    private val nav by lazy { supportFragmentManager.findFragmentById(R.id.host)!!.findNavController() }

    private val vm: ActivityAdminViewModel by viewModels()

    // AppBarConfiguration
    private lateinit var abc: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // view binding
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize token pass from LoginActivity --------------------------------------------------------------------
        val token = intent.getStringExtra("token")

        // if there is no token, back to Login Activity
        if (token.isNullOrEmpty()) {
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        // initialize token value
        vm.token.value = token

        // set the fragment that needs navigation drawer ---------------------------------------------------------------
        abc = AppBarConfiguration(
            setOf(R.id.clinicFragment, R.id.accountsFragment, R.id.changePasswordFragment, R.id.deletedClinicFragment),
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
//        val header = binding.navView.getHeaderView(0)
//        header.findViewById<ImageView>(R.id.photo).setImageResource(R.drawable.blah)
//        header.findViewById<TextView>(R.id.text).setText("cool")
    }

    // Setup navigation up
    override fun onSupportNavigateUp(): Boolean {
        // apply hamburger logo using abc config
        hideKeyboard()
        return nav.navigateUp(abc) || super.onSupportNavigateUp()
    }
}