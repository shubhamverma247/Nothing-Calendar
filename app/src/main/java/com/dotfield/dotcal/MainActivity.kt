package com.dotfield.dotcal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
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
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private val deepLinkTarget = mutableStateOf<DotCalDeepLinkTarget?>(null)
    private var deepLinkSequence = 0L
    private val viewModel: DotCalViewModel by viewModels {
        val app = application as DotCalApplication
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DotCalViewModel(app.repository, app.proManager) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val bootPrefs = getSharedPreferences(BOOT_PREFS, MODE_PRIVATE)
        val bootTheme = bootPrefs.getString(BOOT_THEME_KEY, null)
        val systemDark = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
            android.content.res.Configuration.UI_MODE_NIGHT_YES
        setTheme(
            if (bootTheme == "Light" || ((bootTheme == null || bootTheme == "System") && !systemDark)) {
                R.style.DotCalLight
            } else {
                R.style.DotCalDark
            },
        )
        super.onCreate(savedInstanceState)
        deepLinkTarget.value = intent.dotCalDeepLinkTarget()
        lifecycleScope.launch {
            val storedTheme = runCatching { calendarPreferencesDataStore.data.first()[CalendarPreferences.KEY_THEME_MODE] }.getOrNull()
            if (storedTheme != null && storedTheme != bootTheme) {
                bootPrefs.edit().putString(BOOT_THEME_KEY, storedTheme).apply()
            }
        }
        setContent {
            DotCalTheme {
                val target = deepLinkTarget.value
                DotCalApp(
                    viewModel = viewModel,
                    initialEventId = target?.eventId,
                    initialTaskId = target?.taskId,
                    initialCalendarTab = target?.calendarTab,
                    initialCalendarDate = target?.calendarDate,
                    initialAddEvent = target?.addEvent == true,
                    initialAddEventDate = target?.addEventDate,
                    initialPaywall = target?.paywall == true,
                    initialRouteToken = target?.routeToken,
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLinkTarget.value = intent.dotCalDeepLinkTarget()
    }

    private companion object {
        const val BOOT_PREFS = "dotcal_boot"
        const val BOOT_THEME_KEY = "theme_mode"
    }

    private fun android.content.Intent.dotCalDeepLinkTarget(): DotCalDeepLinkTarget? {
        val uri = data ?: return null
        val token = ++deepLinkSequence
        return when {
            uri.scheme == "dotcal" && uri.host == "event" && uri.lastPathSegment == "new" -> DotCalDeepLinkTarget(
                addEvent = true,
                addEventDate = uri.getQueryParameter("date") ?: LocalDate.now().toString(),
                routeToken = token,
            )
            uri.scheme == "dotcal" && uri.pathSegments.firstOrNull() == "event" && uri.pathSegments.getOrNull(1) == "new" -> DotCalDeepLinkTarget(
                addEvent = true,
                addEventDate = uri.getQueryParameter("date") ?: LocalDate.now().toString(),
                routeToken = token,
            )
            uri.scheme == "dotcal" && uri.host == "event" -> DotCalDeepLinkTarget(eventId = uri.lastPathSegment, routeToken = token)
            uri.scheme == "dotcal" && uri.pathSegments.firstOrNull() == "event" -> DotCalDeepLinkTarget(eventId = uri.pathSegments.getOrNull(1), routeToken = token)
            uri.scheme == "dotcal" && uri.host == "task" -> DotCalDeepLinkTarget(taskId = uri.lastPathSegment, routeToken = token)
            uri.scheme == "dotcal" && uri.pathSegments.firstOrNull() == "task" -> DotCalDeepLinkTarget(taskId = uri.pathSegments.getOrNull(1), routeToken = token)
            uri.scheme == "dotcal" && uri.host == "paywall" -> DotCalDeepLinkTarget(paywall = true, routeToken = token)
            uri.scheme == "dotcal" && uri.pathSegments.firstOrNull() == "paywall" -> DotCalDeepLinkTarget(paywall = true, routeToken = token)
            uri.scheme == "dotcal" && uri.host == "calendar" -> DotCalDeepLinkTarget(calendarTab = uri.lastPathSegment, calendarDate = uri.getQueryParameter("date"), routeToken = token)
            uri.scheme == "dotcal" && uri.pathSegments.firstOrNull() == "calendar" -> DotCalDeepLinkTarget(calendarTab = uri.pathSegments.getOrNull(1), calendarDate = uri.getQueryParameter("date"), routeToken = token)
            else -> null
        }
    }
}

private data class DotCalDeepLinkTarget(
    val eventId: String? = null,
    val taskId: String? = null,
    val calendarTab: String? = null,
    val calendarDate: String? = null,
    val addEvent: Boolean = false,
    val addEventDate: String? = null,
    val paywall: Boolean = false,
    val routeToken: Long,
)
