package com.dotfield.dotcal.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
    val dotTile: Int,
    val accent: ColorProvider = ColorProvider(Color(0xFFFF3B30)),
)

@Composable
fun DotCalGlanceTheme(content: @Composable () -> Unit) {
    content()
}

suspend fun dotCalWidgetPalette(context: Context): DotCalWidgetPalette {
    val preferences = context.calendarPreferencesDataStore.data.first()
    val mode = preferences[CalendarPreferences.KEY_THEME_MODE] ?: "System"
    val accent = widgetAccentColor(preferences[CalendarPreferences.KEY_ACCENT_COLOR])
    val systemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    if (mode == "System") {
        return DotCalWidgetPalette(
            background = ColorProvider(R.color.widget_background),
            primary = ColorProvider(R.color.widget_primary),
            secondary = ColorProvider(R.color.widget_secondary),
            dim = ColorProvider(R.color.widget_dim),
            border = ColorProvider(R.color.widget_border),
            inactive = ColorProvider(R.color.widget_inactive),
            dot = ColorProvider(R.color.widget_dot),
            dotTile = if (systemDark) R.drawable.widget_dot_pattern_dark else R.drawable.widget_dot_pattern_light,
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
            background = ColorProvider(Color(0xFF1A1A1A)),
            primary = ColorProvider(Color(0xFFFFFFFF)),
            secondary = ColorProvider(Color(0xFF7A7A7A)),
            dim = ColorProvider(Color(0xFF4A4A4A)),
            border = ColorProvider(Color(0xFF2A2A2A)),
            inactive = ColorProvider(Color(0xFF4A4A4A)),
            dot = ColorProvider(Color(0xFF242424)),
            dotTile = R.drawable.widget_dot_pattern_dark,
            accent = ColorProvider(accent),
        )
    } else {
        DotCalWidgetPalette(
            background = ColorProvider(Color(0xFFFFFFFF)),
            primary = ColorProvider(Color(0xFF101010)),
            secondary = ColorProvider(Color(0xFF6B6B6B)),
            dim = ColorProvider(Color(0xFFB5B5B5)),
            border = ColorProvider(Color(0xFFECECEC)),
            inactive = ColorProvider(Color(0xFFB5B5B5)),
            dot = ColorProvider(Color(0xFFEDEDED)),
            dotTile = R.drawable.widget_dot_pattern_light,
            accent = ColorProvider(accent),
        )
    }
}

private fun widgetAccentColor(value: String?): Color {
    return when (value) {
        "BLUE" -> Color(0xFF0A84FF)
        "GREEN" -> Color(0xFF30D158)
        "PURPLE" -> Color(0xFFBF5AF2)
        "AMBER" -> Color(0xFFFF9F0A)
        else -> Color(0xFFFF3B30)
    }
}
