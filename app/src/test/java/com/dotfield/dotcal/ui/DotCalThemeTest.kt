package com.dotfield.dotcal.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class DotCalThemeTest {
    @Test
    fun redPresetUsesNothingPrimaryRed() {
        assertEquals("#C8102E", AccentColor.Preset.RED.hex)
    }

    @Test
    fun accentSwitchContentStaysWhiteForEveryPreset() {
        AccentColor.Preset.entries.forEach { preset ->
            assertEquals(preset.name, androidx.compose.ui.graphics.Color.White, preset.onColor)
        }
    }
}
