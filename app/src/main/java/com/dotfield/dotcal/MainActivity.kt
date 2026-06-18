package com.dotfield.dotcal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import com.dotfield.dotcal.ui.DotCalApp
import com.dotfield.dotcal.ui.DotCalViewModel
import com.dotfield.dotcal.ui.theme.DotCalTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: DotCalViewModel by viewModels {
        val app = application as DotCalApplication
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DotCalViewModel(app.repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val bootPrefs = getSharedPreferences(BOOT_PREFS, MODE_PRIVATE)
        val bootTheme = bootPrefs.getString(BOOT_THEME_KEY, null)
        setTheme(if (bootTheme == "Light") R.style.Theme_DotCal_Light else R.style.Theme_DotCal_Dark)
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val storedTheme = runCatching { calendarPreferencesDataStore.data.first()[CalendarPreferences.KEY_THEME_MODE] }.getOrNull()
            if (storedTheme != null && storedTheme != bootTheme) {
                bootPrefs.edit().putString(BOOT_THEME_KEY, storedTheme).apply()
            }
        }
        setContent {
            DotCalTheme {
                DotCalApp(viewModel = viewModel)
            }
        }
    }

    private companion object {
        const val BOOT_PREFS = "dotcal_boot"
        const val BOOT_THEME_KEY = "theme_mode"
    }
}
