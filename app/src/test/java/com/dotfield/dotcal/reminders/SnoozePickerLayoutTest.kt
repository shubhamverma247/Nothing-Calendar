package com.dotfield.dotcal.reminders

import org.junit.Assert.assertEquals
import org.junit.Test

class SnoozePickerLayoutTest {
    @Test
    fun optionsPaneHeightStaysFixedAcrossTabs() {
        assertEquals(480, SNOOZE_PICKER_OPTIONS_PANE_HEIGHT_DP)
    }
}
