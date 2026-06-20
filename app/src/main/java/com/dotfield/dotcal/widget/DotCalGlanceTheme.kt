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
    val border: ColorProvider,
    val inactive: ColorProvider,
    val accent: ColorProvider = ColorProvider(Color(0xFFFF3B30)),
)

@Composable
fun DotCalGlanceTheme(content: @Composable () -> Unit) {
    content()
}

suspend fun dotCalWidgetPalette(context: Context): DotCalWidgetPalette {
    val mode = context.calendarPreferencesDataStore.data.first()[CalendarPreferences.KEY_THEME_MODE] ?: "System"
    if (mode == "System") {
        return DotCalWidgetPalette(
            background = ColorProvider(R.color.widget_background),
            primary = ColorProvider(R.color.widget_primary),
            secondary = ColorProvider(R.color.widget_secondary),
            border = ColorProvider(R.color.widget_border),
            inactive = ColorProvider(R.color.widget_inactive),
        )
    }
    val isDark = when (mode) {
        "Light" -> false
        "Dark" -> true
        else -> (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
    return if (isDark) {
        DotCalWidgetPalette(
            background = ColorProvider(Color(0xFF1E1E1E)),
            primary = ColorProvider(Color(0xFFFFFFFF)),
            secondary = ColorProvider(Color(0xFFB3B3B3)),
            border = ColorProvider(Color(0xFF2A2A2A)),
            inactive = ColorProvider(Color(0xFF666666)),
        )
    } else {
        DotCalWidgetPalette(
            background = ColorProvider(Color(0xFFF5F5F5)),
            primary = ColorProvider(Color(0xFF101010)),
            secondary = ColorProvider(Color(0xFF6B6B6B)),
            border = ColorProvider(Color(0xFFE8E8E8)),
            inactive = ColorProvider(Color(0xFFB0B0B0)),
        )
    }
}
