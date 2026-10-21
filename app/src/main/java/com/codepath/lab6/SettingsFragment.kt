package com.codepath.lab6

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial

const val SETTINGS_PREFS = "settings"
const val DARK_MODE_KEY = "dark_mode"

// Applies the saved dark mode setting to the whole app
fun applyDarkMode(context: Context) {
    val darkMode = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .getBoolean(DARK_MODE_KEY, false)
    AppCompatDelegate.setDefaultNightMode(
        if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    )
}

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = requireContext().getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)

        val darkModeSwitch: SwitchMaterial = view.findViewById(R.id.dark_mode_switch)
        darkModeSwitch.isChecked = prefs.getBoolean(DARK_MODE_KEY, false)
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean(DARK_MODE_KEY, isChecked).apply()
            applyDarkMode(requireContext())
        }
    }

    companion object {
        fun newInstance(): SettingsFragment {
            return SettingsFragment()
        }
    }
}
