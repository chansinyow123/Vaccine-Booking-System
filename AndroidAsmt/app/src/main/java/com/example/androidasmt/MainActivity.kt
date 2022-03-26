package com.example.androidasmt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.*
import com.example.androidasmt.databinding.ActivityMainBinding
import com.example.androidasmt.helper.hideKeyboard

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val nav by lazy { supportFragmentManager.findFragmentById(R.id.host)!!.findNavController() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup action bar
        setupActionBarWithNavController(nav)
    }
    
    // Setup navigation up
    override fun onSupportNavigateUp(): Boolean {
        hideKeyboard()
        return nav.navigateUp() || super.onSupportNavigateUp()
    }

    // Setup navigation drawer / menu
//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        MenuCompat.setGroupDividerEnabled(menu, true)
//        menuInflater.inflate(R.menu.drawer_customer, menu)
//        return super.onCreateOptionsMenu(menu)
//    }
//
//    // When menu is clicked
//    // return true: navigation is handled, else navigation handler is passed to fragment
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//
//        // access the menuItem id
//        when (item.itemId) {
//            R.id.loginFragment -> {
//                // if logout is click, logout the user
//                Snackbar.make(binding.root, "Logout", Snackbar.LENGTH_SHORT).show()
//            }
//        }
//
//        // if the id of the menuItem match the fragment id declared in nav.xml file
//        // then it will navigate to that destination
//        // NOTES: "onNavDestinationSelected()" will clear the backstack
//        return item.onNavDestinationSelected(nav) || super.onOptionsItemSelected(item)
//    }
}