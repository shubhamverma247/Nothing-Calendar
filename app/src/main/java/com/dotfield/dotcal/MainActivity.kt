package com.dotfield.dotcal

import android.app.Activity
import android.app.LocaleManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import com.dotfield.dotcal.prefs.AppLanguage
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import com.dotfield.dotcal.ui.DotCalApp
import com.dotfield.dotcal.ui.DotCalViewModel
import com.dotfield.dotcal.ui.theme.DotCalTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val BOOT_PREFS_NAME = "dotcal_boot"
internal const val BOOT_LANGUAGE_KEY = "app_language"

/**
 * Wraps [base] in the selected per-app locale. `attachBaseContext` runs long before DataStore can
 * be read, so the chosen tag is mirrored into the existing `dotcal_boot` SharedPreferences the same
 * way the boot theme already is.
 */
internal fun localizedDotCalContext(base: Context): Context {
    val stored = runCatching {
        base.getSharedPreferences(BOOT_PREFS_NAME, Context.MODE_PRIVATE).getString(BOOT_LANGUAGE_KEY, "")
    }.getOrNull()
    val language = AppLanguage.fromTag(stored)
    if (language.tag.isEmpty()) return base
    val config = Configuration(base.resources.configuration)
    config.setLocales(LocaleList.forLanguageTags(language.tag))
    return base.createConfigurationContext(config)
}

/**
 * Applies a language choice as a per-app locale. On API 33+ the framework [LocaleManager] stores it
 * and recreates activities itself. Below 33 there is no framework support, so the mirrored boot
 * preference is picked up by an explicit [Activity.recreate] — never call both.
 */
internal fun applyAppLanguage(context: Context, language: AppLanguage) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        runCatching {
            val manager = context.getSystemService(LocaleManager::class.java)
            manager?.applicationLocales = if (language.tag.isEmpty()) {
                LocaleList.getEmptyLocaleList()
            } else {
                LocaleList.forLanguageTags(language.tag)
            }
        }
    } else {
        context.findActivity()?.recreate()
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

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

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(localizedDotCalContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val bootPrefs = getSharedPreferences(BOOT_PREFS_NAME, MODE_PRIVATE)
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
            reconcileAppLanguage(bootPrefs)
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

    /**
     * Aligns the stored language, the boot mirror, and the framework per-app locale on startup.
     * The OS locale can be changed outside the app (Android's own per-app language screen), and a
     * stale tag we no longer offer would otherwise leave DotCal rendering in an unlisted language.
     * API 33+ only — below that there is no framework locale to reconcile against.
     */
    private suspend fun reconcileAppLanguage(bootPrefs: android.content.SharedPreferences) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        runCatching {
            val manager = getSystemService(LocaleManager::class.java) ?: return
            val osTag = manager.applicationLocales.takeIf { !it.isEmpty }?.get(0)?.language.orEmpty()
            val known = AppLanguage.entries.firstOrNull { it.tag.isNotEmpty() && it.tag == osTag }
            if (osTag.isNotEmpty() && known == null) {
                // OS is on a language we do not offer — clear it back to the device default.
                manager.applicationLocales = LocaleList.getEmptyLocaleList()
            }
            val resolved = known ?: AppLanguage.System
            bootPrefs.edit().putString(BOOT_LANGUAGE_KEY, resolved.tag).apply()
            calendarPreferencesDataStore.edit { preferences ->
                preferences[CalendarPreferences.KEY_APP_LANGUAGE] = resolved.tag
            }
        }
    }

    private companion object {
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
