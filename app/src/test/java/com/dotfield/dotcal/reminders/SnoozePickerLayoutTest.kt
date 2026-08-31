package com.dotfield.dotcal.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SnoozePickerLayoutTest {
    @Test
    fun optionsPaneHeightStaysFixedAcrossTabs() {
        assertEquals(480, SNOOZE_PICKER_OPTIONS_PANE_HEIGHT_DP)
    }

    @Test
    fun tabContentIsExclusive() {
        assertEquals(SnoozePickerTab.FOR, snoozePickerTab(0))
        assertEquals(SnoozePickerTab.UNTIL, snoozePickerTab(1))
        assertNotEquals(snoozePickerTab(0), snoozePickerTab(1))
    }
}
