package com.dotfield.dotcal.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.glance.GlanceId
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.unit.ColorProvider
import com.dotfield.dotcal.R
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import kotlinx.coroutines.flow.first

data class DotCalWidgetPalette(
    val background: ColorProvider,
    val primary: ColorProvider,
    val secondary: ColorProvider,
    val dim: ColorProvider,
    val border: ColorProvider,
    val inactive: ColorProvider,
    val dot: ColorProvider,
    val surfaceDrawable: Int,
    val dotTile: Int,
    val solidSurface: ColorProvider,
    val accent: ColorProvider = ColorProvider(Color(0xFFFF3B30)),
)

data class DotCalWidgetSettings(
    val themeMode: String = "System",
    val accentColor: String? = null,
    val transparent: Boolean = false,
    val showDotTexture: Boolean = true,
    val accountId: String? = null,
)

@Composable
fun DotCalGlanceTheme(content: @Composable () -> Unit) {
    content()
}

suspend fun dotCalWidgetPalette(context: Context): DotCalWidgetPalette {
    return dotCalWidgetPalette(context, readDotCalWidgetSettings(context))
}

suspend fun syncDotCalWidgetState(context: Context, glanceId: GlanceId): DotCalWidgetSettings {
    val settings = readDotCalWidgetSettings(context)
    var accountId: String? = null
    updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { preferences ->
        accountId = preferences[CalendarPreferences.KEY_WIDGET_ACCOUNT_ID]
        mutablePreferencesOf().apply {
            plusAssign(preferences)
            this[CalendarPreferences.KEY_THEME_MODE] = settings.themeMode
            settings.accentColor?.let { this[CalendarPreferences.KEY_ACCENT_COLOR] = it } ?: remove(CalendarPreferences.KEY_ACCENT_COLOR)
            this[CalendarPreferences.KEY_WIDGET_TRANSPARENT] = settings.transparent
            this[CalendarPreferences.KEY_WIDGET_DOT_TEXTURE] = settings.showDotTexture
        }
    }
    return settings.copy(accountId = accountId)
}

@Composable
fun currentDotCalWidgetSettings(): DotCalWidgetSettings {
    return currentState<Preferences>().toDotCalWidgetSettings()
}

fun dotCalWidgetPalette(context: Context, settings: DotCalWidgetSettings): DotCalWidgetPalette {
    val mode = settings.themeMode
    val accent = widgetAccentColor(settings.accentColor)
    val transparent = settings.transparent
    val showDotTexture = settings.showDotTexture
    val systemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    if (mode == "System") {
        val solidSurface = if (systemDark) ColorProvider(Color(0xFF1A1A1A)) else ColorProvider(Color(0xFFFFFFFF))
        return DotCalWidgetPalette(
            background = if (transparent) ColorProvider(Color.Transparent) else ColorProvider(R.color.widget_background),
            primary = ColorProvider(R.color.widget_primary),
            secondary = ColorProvider(R.color.widget_secondary),
            dim = ColorProvider(R.color.widget_dim),
            border = ColorProvider(R.color.widget_border),
            inactive = ColorProvider(R.color.widget_inactive),
            dot = ColorProvider(R.color.widget_dot),
            surfaceDrawable = when {
                transparent -> R.drawable.widget_surface_transparent
                systemDark -> R.drawable.widget_surface_dark
                else -> R.drawable.widget_surface_light
            },
            dotTile = if (showDotTexture && !transparent) {
                if (systemDark) R.drawable.widget_dot_pattern_dark else R.drawable.widget_dot_pattern_light
            } else {
                R.drawable.widget_dot_pattern_transparent
            },
            solidSurface = solidSurface,
            accent = ColorProvider(accent),
        )
    }
    val isDark = when (mode) {
        "Light" -> false
        "Dark" -> true
        else -> systemDark
    }
    return if (isDark) {
        DotCalWidgetPalette(
            background = ColorProvider(if (transparent) Color.Transparent else Color(0xFF1A1A1A)),
            primary = ColorProvider(Color(0xFFFFFFFF)),
            secondary = ColorProvider(Color(0xFF7A7A7A)),
            dim = ColorProvider(Color(0xFF4A4A4A)),
            border = ColorProvider(Color(0xFF2A2A2A)),
            inactive = ColorProvider(Color(0xFF4A4A4A)),
            dot = ColorProvider(Color(0xFF242424)),
            surfaceDrawable = if (transparent) R.drawable.widget_surface_transparent else R.drawable.widget_surface_dark,
            dotTile = if (showDotTexture && !transparent) R.drawable.widget_dot_pattern_dark else R.drawable.widget_dot_pattern_transparent,
            solidSurface = ColorProvider(Color(0xFF1A1A1A)),
            accent = ColorProvider(accent),
        )
    } else {
        DotCalWidgetPalette(
            background = ColorProvider(if (transparent) Color.Transparent else Color(0xFFFFFFFF)),
            primary = ColorProvider(Color(0xFF101010)),
            secondary = ColorProvider(Color(0xFF6B6B6B)),
            dim = ColorProvider(Color(0xFFB5B5B5)),
            border = ColorProvider(Color(0xFFECECEC)),
            inactive = ColorProvider(Color(0xFFB5B5B5)),
            dot = ColorProvider(Color(0xFFEDEDED)),
            surfaceDrawable = if (transparent) R.drawable.widget_surface_transparent else R.drawable.widget_surface_light,
            dotTile = if (showDotTexture && !transparent) R.drawable.widget_dot_pattern_light else R.drawable.widget_dot_pattern_transparent,
            solidSurface = ColorProvider(Color(0xFFFFFFFF)),
            accent = ColorProvider(accent),
        )
    }
}

private suspend fun readDotCalWidgetSettings(context: Context): DotCalWidgetSettings {
    val preferences = context.calendarPreferencesDataStore.data.first()
    return preferences.toDotCalWidgetSettings()
}

private fun Preferences.toDotCalWidgetSettings(): DotCalWidgetSettings {
    return DotCalWidgetSettings(
        themeMode = this[CalendarPreferences.KEY_THEME_MODE] ?: "System",
        accentColor = this[CalendarPreferences.KEY_ACCENT_COLOR],
        transparent = this[CalendarPreferences.KEY_WIDGET_TRANSPARENT] ?: false,
        showDotTexture = this[CalendarPreferences.KEY_WIDGET_DOT_TEXTURE] ?: true,
        accountId = this[CalendarPreferences.KEY_WIDGET_ACCOUNT_ID],
    )
}

private val DEFAULT_ACCENT = Color(0xFFFF3B30)

/**
 * Resolves the stored accent value into a color. Accepts legacy preset enum names, the Pro extra
 * presets, and Pro custom "#RRGGBB" hex strings. Falls back to red on any unknown/invalid value.
 */
private fun widgetAccentColor(value: String?): Color {
    if (value == null) return DEFAULT_ACCENT
    when (value) {
        "RED" -> return DEFAULT_ACCENT
        "BLUE" -> return Color(0xFF0A84FF)
        "GREEN" -> return Color(0xFF30D158)
        "PURPLE" -> return Color(0xFFBF5AF2)
        "AMBER" -> return Color(0xFFFF9F0A)
        "TEAL" -> return Color(0xFF2AB8B0)
        "PINK" -> return Color(0xFFFF375F)
        "ORANGE" -> return Color(0xFFFF6B00)
        "CYAN" -> return Color(0xFF32ADE6)
        "INDIGO" -> return Color(0xFF5E5CE6)
        "MINT" -> return Color(0xFF66D4A0)
        "ROSE" -> return Color(0xFFF06292)
        "LIME" -> return Color(0xFFB0C948)
    }
    if (value.startsWith("#")) {
        runCatching { return Color(android.graphics.Color.parseColor(value)) }
    }
    return DEFAULT_ACCENT
}

internal fun widgetAccentArgb(settings: DotCalWidgetSettings): Int {
    return widgetAccentColor(settings.accentColor).toArgb()
}
