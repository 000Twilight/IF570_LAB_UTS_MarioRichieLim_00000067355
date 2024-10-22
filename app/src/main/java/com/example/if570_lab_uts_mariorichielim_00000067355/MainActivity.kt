package com.example.if570_lab_uts_mariorichielim_00000067355

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottom_nav)

        // Setup navigation controller
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        // Listen for destination changes to show/hide bottom nav
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registerFragment -> hideBottomNavigation()
                else -> showBottomNavigation()
            }
        }
    }

    public fun hideBottomNavigation() {
        bottomNavigationView.visibility = View.GONE
    }

    public fun showBottomNavigation() {
        bottomNavigationView.visibility = View.VISIBLE
    }
}
