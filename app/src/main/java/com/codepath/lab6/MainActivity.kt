package com.codepath.lab6

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.codepath.lab6.databinding.ActivityMainBinding
import kotlinx.serialization.json.Json

// Helper function for JSON parsing (used by both fragments, so keep it here)
fun createJson() = Json {
    isLenient = true
    ignoreUnknownKeys = true
    useAlternativeNames = false
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment = HomeFragment()
    private val parksFragment = ParksFragment()
    private val campgroundFragment = CampgroundFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply the saved dark mode setting before any views are created
        applyDarkMode(this)
        super.onCreate(savedInstanceState)

        // Set up ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Swap fragments when a bottom nav item is tapped
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> homeFragment
                R.id.nav_parks -> parksFragment
                R.id.nav_campgrounds -> campgroundFragment
                R.id.nav_settings -> settingsFragment
                else -> return@setOnItemSelectedListener false
            }
            replaceFragment(fragment)
            true
        }

        // Default tab so the screen isn't blank on launch. After a rotation (or a
        // dark mode switch) the FragmentManager restores the current fragment and
        // the bottom nav restores its selected tab, so we leave both alone.
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_frame_layout, fragment)
            .commit()
    }
}
