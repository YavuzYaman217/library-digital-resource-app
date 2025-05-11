package com.yyaman.libraryapp.main

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.yyaman.libraryapp.R
import com.yyaman.libraryapp.data.AuthRepository
import com.yyaman.libraryapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appBarConfig: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Setup nav controller + drawer
        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        val navController = navHost.navController

        appBarConfig = AppBarConfiguration(
            setOf(
                R.id.searchFragment,
                R.id.digitalListFragment,
                R.id.reservationsFragment,
                R.id.bookmarksFragment,
                R.id.profileFragment,
                R.id.settingsFragment
            ),
            binding.drawerLayout
        )

        NavigationUI.setupWithNavController(binding.toolbar, navController, appBarConfig)
        NavigationUI.setupWithNavController(binding.navigationView, navController)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // 2) Populate nav-header with current user
        val header = binding.navigationView.getHeaderView(0)
        val tvName  = header.findViewById<TextView>(R.id.tvNavName)
        val tvEmail = header.findViewById<TextView>(R.id.tvNavEmail)

        lifecycleScope.launch {
            try {
                // fetch on IO thread
                val user = withContext(Dispatchers.IO) {
                    AuthRepository(this@MainActivity).me()
                }
                // update on UI thread
                tvName.text  = user.name
                tvEmail.text = user.email
            } catch (e: Exception) {
                // failed to fetch (token expired / network); consider logging out here
            }
        }

        // 3) Handle deep-link into reservations if requested
        binding.root.post {
            if (intent?.getStringExtra("dest") == "reservations") {
                navController.navigate(R.id.reservationsFragment)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        return NavigationUI.navigateUp(navHost.navController, appBarConfig)
                || super.onSupportNavigateUp()
    }
}
