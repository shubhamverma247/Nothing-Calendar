package com.dotfield.dotcal.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.R
import com.dotfield.dotcal.ui.theme.NBlack

internal data class DotCalPalette(
    val background: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val dimText: Color,
    val line: Color,
    val cell: Color,
    val calendarSurface: Color,
    val topBarSurface: Color,
    val bottomNavSurface: Color,
    val dialogSurface: Color,
    val cancelSurface: Color,
    val cancelBorder: Color,
    val dragHandle: Color,
    val eventCardSurface: Color,
    val eventCardBorder: Color,
    val eventCardChevron: Color,
    val textFieldBorder: Color,
    val segmentSelected: Color,
    val disabledText: Color,
    val switchOffTrack: Color,
    val dot: Color,
    val yearWeekday: Color,
    val yearMonthLabel: Color,
    val accent: Color,
    val onAccent: Color,
    val isDark: Boolean,
)

internal enum class DotCalThemeMode(val label: String) {
    Light("Light"),
    Dark("Dark"),
    System("System");

    companion object {
        fun fromStorage(value: String?): DotCalThemeMode {
            return entries.firstOrNull { it.name == value } ?: System
        }
    }
}

internal enum class AppFont(val id: String, val label: String, val tagline: String) {
    NDot("ndot", "Ndot", "Monospaced. Technical. Precise."),
    NType("ntype", "NType 82", "Editorial. Redefined."),
    System("system", "System", "Your device's own font.");

    companion object {
        fun fromId(id: String?): AppFont = entries.firstOrNull { it.id == id } ?: NDot
    }
}

internal val LocalHeadingFont = staticCompositionLocalOf<FontFamily> { FontFamily.Default }

@Composable
internal fun rememberAppFontFamily(font: AppFont): FontFamily = remember(font) {
    when (font) {
        AppFont.NDot -> FontFamily(Font(R.font.ndot))
        AppFont.NType -> FontFamily(Font(R.font.ntype82))
        AppFont.System -> FontFamily.Default
    }
}

/**
 * Accent color model. Free tier uses the 5 [Preset] swatches; Pro unlocks the extra curated
 * [proPresets] and a full [Custom] hex color picker.
 *
 * Storage format in [CalendarPreferences.KEY_ACCENT_COLOR] (and the boot SharedPreferences mirror):
 *  - a preset enum name, e.g. "BLUE" (backward compatible with existing installs), or
 *  - a "#RRGGBB" hex string for a Pro custom color.
 */
internal sealed interface AccentColor {
    val color: Color
    val label: String

    /** Text/icon color that stays legible on top of [color]. */
    val onColor: Color
        get() = if (color.luminanceApprox() > 0.5f) Color(0xFF101010) else Color(0xFFFFFFFF)

    /** Value persisted to DataStore + boot prefs. */
    val storageValue: String

    enum class Preset(val hex: String, override val label: String) : AccentColor {
        // Free presets. Order/names are storage-stable; do not rename.
        RED("#FF3B30", "Red"),
        BLUE("#0A84FF", "Blue"),
        GREEN("#30D158", "Green"),
        PURPLE("#BF5AF2", "Purple"),
        AMBER("#FF9F0A", "Amber"),
        // Pro presets (extra curated palette).
        TEAL("#2AB8B0", "Teal"),
        PINK("#FF375F", "Pink"),
        ORANGE("#FF6B00", "Orange"),
        CYAN("#32ADE6", "Cyan"),
        INDIGO("#5E5CE6", "Indigo"),
        MINT("#66D4A0", "Mint"),
        ROSE("#F06292", "Rose"),
        LIME("#B0C948", "Lime");

        override val color: Color get() = Color(android.graphics.Color.parseColor(hex))
        override val storageValue: String get() = name
        val isPro: Boolean get() = this in proPresets
    }

    /** Pro-only arbitrary hex color chosen from the color picker. */
    data class Custom(val hex: String) : AccentColor {
        override val color: Color get() = Color(android.graphics.Color.parseColor(hex))
        override val label: String get() = hex.uppercase()
        override val storageValue: String get() = hex.uppercase()
    }

    companion object {
        val Default: Preset = Preset.RED

        /** Free presets shown to everyone. */
        val freePresets: List<Preset> = listOf(
            Preset.RED, Preset.BLUE, Preset.GREEN, Preset.PURPLE, Preset.AMBER,
        )

        /** Extra presets unlocked by Pro. */
        val proPresets: List<Preset> = listOf(
            Preset.TEAL, Preset.PINK, Preset.ORANGE, Preset.CYAN,
            Preset.INDIGO, Preset.MINT, Preset.ROSE, Preset.LIME,
        )

        fun fromStorage(value: String?): AccentColor {
            if (value == null) return Default
            Preset.entries.firstOrNull { it.name == value }?.let { return it }
            return normalizeHex(value)?.let { Custom(it) } ?: Default
        }

        /** Returns a canonical "#RRGGBB" string if [raw] is a valid hex color, else null. */
        fun normalizeHex(raw: String?): String? {
            if (raw.isNullOrBlank()) return null
            val trimmed = raw.trim().removePrefix("#")
            val hex = when (trimmed.length) {
                3 -> trimmed.map { "$it$it" }.joinToString("")
                6 -> trimmed
                8 -> trimmed.substring(2) // drop alpha, force opaque
                else -> return null
            }
            if (!hex.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) return null
            return "#${hex.uppercase()}"
        }
    }
}

/** Cheap perceptual-ish luminance used to pick a legible on-accent text color. */
internal fun Color.luminanceApprox(): Float = 0.299f * red + 0.587f * green + 0.114f * blue

internal fun dotCalPalette(
    mode: DotCalThemeMode,
    accentColor: AccentColor = AccentColor.Default,
    systemDark: Boolean = false,
): DotCalPalette {
    val resolved = if (mode == DotCalThemeMode.System) {
        if (systemDark) DotCalThemeMode.Dark else DotCalThemeMode.Light
    } else {
        mode
    }
    val accent = accentColor.color
    val onAccent = accentColor.onColor
    return when (resolved) {
        DotCalThemeMode.Dark -> DotCalPalette(
            background = Color(0xFF000000),
            primaryText = Color(0xFFFFFFFF),
            secondaryText = Color(0xFFB3B3B3),
            dimText = Color(0xFF6E6E6E),
            line = Color(0xFF2A2A2A),
            cell = NBlack,
            calendarSurface = NBlack,
            topBarSurface = Color(0xFF000000),
            bottomNavSurface = Color(0xFF000000),
            dialogSurface = Color(0xFF1E1E1E),
            cancelSurface = Color(0xFF121212),
            cancelBorder = Color(0xFF2A2A2A),
            dragHandle = Color(0xFF707070),
            eventCardSurface = Color(0xFF121212),
            eventCardBorder = Color(0xFF2A2A2A),
            eventCardChevron = Color(0xFF6E6E6E),
            textFieldBorder = Color(0xFF4A4A4A),
            segmentSelected = Color(0xFF1E1E1E),
            disabledText = Color(0xFF6E6E6E),
            switchOffTrack = Color(0xFF3A3A3A),
            dot = Color(0xFFFFFFFF),
            yearWeekday = Color(0xFFFFFFFF),
            yearMonthLabel = Color(0xFFFFFFFF),
            accent = accent,
            onAccent = onAccent,
            isDark = true,
        )
        DotCalThemeMode.Light -> DotCalPalette(
            background = Color(0xFFFFFFFF),
            primaryText = Color(0xFF101010),
            secondaryText = Color(0xFF6B6B6B),
            dimText = Color(0xFFBDBDBD),
            line = Color(0xFFE8E8E8),
            cell = Color(0xFFFFFFFF),
            calendarSurface = Color(0xFFFFFFFF),
            topBarSurface = Color(0xFFFFFFFF),
            bottomNavSurface = Color(0xFFFFFFFF),
            dialogSurface = Color(0xFFFFFFFF),
            cancelSurface = Color(0xFFEFEFEF),
            cancelBorder = Color(0xFFE0E0E0),
            dragHandle = Color(0xFFC8C8C8),
            eventCardSurface = Color(0xFFFFFFFF),
            eventCardBorder = Color(0xFFE8E8E8),
            eventCardChevron = Color(0xFFBDBDBD),
            textFieldBorder = Color(0xFFDADADA),
            segmentSelected = Color(0xFFEFEFEF),
            disabledText = Color(0xFFBDBDBD),
            switchOffTrack = Color(0xFFDADADA),
            dot = Color(0xFF101010),
            yearWeekday = Color(0xFF101010),
            yearMonthLabel = Color(0xFF101010),
            accent = accent,
            onAccent = onAccent,
            isDark = false,
        )
        DotCalThemeMode.System -> error("System must be resolved before palette creation")
    }
}

internal fun dotCalBootPalette(accentColor: AccentColor = AccentColor.Default): DotCalPalette {
    return DotCalPalette(
        background = Color(0xFF000000),
        primaryText = Color(0xFFFFFFFF),
        secondaryText = Color(0xFFB3B3B3),
        dimText = Color(0xFF6E6E6E),
        line = Color(0xFF2A2A2A),
        cell = NBlack,
        calendarSurface = NBlack,
        topBarSurface = Color(0xFF000000),
        bottomNavSurface = Color(0xFF000000),
        dialogSurface = Color(0xFF1E1E1E),
        cancelSurface = Color(0xFF121212),
        cancelBorder = Color(0xFF2A2A2A),
        dragHandle = Color(0xFF707070),
        eventCardSurface = Color(0xFF121212),
        eventCardBorder = Color(0xFF2A2A2A),
        eventCardChevron = Color(0xFF6E6E6E),
        textFieldBorder = Color(0xFF4A4A4A),
        segmentSelected = Color(0xFF1E1E1E),
        disabledText = Color(0xFF6E6E6E),
        switchOffTrack = Color(0xFF3A3A3A),
        dot = Color(0xFFFFFFFF),
        yearWeekday = Color(0xFFFFFFFF),
        yearMonthLabel = Color(0xFFFFFFFF),
        accent = accentColor.color,
        onAccent = accentColor.onColor,
        isDark = true,
    )
}
