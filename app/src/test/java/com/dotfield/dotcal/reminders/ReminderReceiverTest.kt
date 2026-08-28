package com.dotfield.dotcal.reminders

import com.dotfield.dotcal.data.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderReceiverTest {
    @Test
    fun recurringReminderUsesOccurrenceIdForGlyphLifecycle() {
        val event = testEvent(rrule = "FREQ=DAILY")

        assertEquals(
            "master::occurrence::123456",
            reminderGlyphEventId(event, "master", 123456L),
        )
    }

    @Test
    fun oneTimeReminderKeepsStableEventIdForGlyphLifecycle() {
        val event = testEvent()

        assertEquals(
            "master",
            reminderGlyphEventId(event, "master", 123456L),
        )
    }

    private fun testEvent(rrule: String? = null) = CalendarEvent(
        id = "master",
        accountId = "local",
        title = "Test",
        startTimeMs = 1_000L,
        endTimeMs = 2_000L,
        timeZone = "UTC",
        isAllDay = 0,
        colorHex = null,
        rrule = rrule,
        source = "LOCAL",
        googleEventId = null,
        googleCalendarId = null,
        completedAtMs = null,
        voiceNotePath = null,
        createdAtMs = 0L,
        updatedAtMs = 0L,
    )
}
