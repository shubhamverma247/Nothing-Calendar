package com.dotfield.dotcal.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class ReminderPresetTest {
    @Test
    fun eventReminderPresetsMatchCreationOptions() {
        assertEquals(listOf(1440, 120, 60, 30, 15, 10, 5), eventReminderPresets)
    }

    @Test
    fun defaultReminderOptionsIncludeCommonShortAndLongOffsets() {
        assertEquals(listOf(null, 5, 10, 15, 30, 60, 120, 1440), reminderOptions)
        assertEquals(listOf(null, 5, 10, 15, 30, 60, 120, 1440), taskReminderOptions)
    }

    @Test
    fun reminderMinutesBeforeEventReturnsMinutesBetweenReminderAndStart() {
        val eventStart = LocalDateTime.of(2026, 8, 27, 10, 0)
        val reminderTime = LocalDateTime.of(2026, 8, 27, 9, 45)

        assertEquals(15, reminderMinutesBeforeEvent(reminderTime, eventStart))
    }

    @Test
    fun reminderMinutesBeforeEventRejectsSameOrLaterTime() {
        val eventStart = LocalDateTime.of(2026, 8, 27, 10, 0)

        assertNull(reminderMinutesBeforeEvent(eventStart, eventStart))
        assertNull(reminderMinutesBeforeEvent(eventStart.plusMinutes(1), eventStart))
    }

    @Test
    fun reminderOffsetDisplayUsesMinutesOnlyBelowOneHour() {
        assertEquals(ReminderOffsetDisplay("15", ReminderOffsetUnit.Minute), reminderOffsetDisplay(15))
        assertEquals(ReminderOffsetDisplay("1.5", ReminderOffsetUnit.Hour), reminderOffsetDisplay(90))
        assertEquals(ReminderOffsetDisplay("2", ReminderOffsetUnit.Day), reminderOffsetDisplay(2_880))
        assertEquals(ReminderOffsetDisplay("2", ReminderOffsetUnit.Week), reminderOffsetDisplay(20_160))
    }
}
