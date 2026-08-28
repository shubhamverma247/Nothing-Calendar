package com.dotfield.dotcal.ui

import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.RecurringEditScope
import com.dotfield.dotcal.data.recurrenceOccurrenceId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DetailDeleteScopeTest {
    @Test
    fun recurringOccurrenceOpensScopeChoiceBeforeDetailDelete() {
        val master = event("event-1")
        val occurrence = master.copy(id = recurrenceOccurrenceId(master.id, 604_800_000L))

        assertEquals(RecurringEditScope.ThisEvent, detailDeleteScope(occurrence))
        assertNull(detailDeleteScope(master))
    }

    private fun event(id: String) = CalendarEvent(
        id = id,
        accountId = "provider-calendar-1",
        title = "Test",
        startTimeMs = 0L,
        endTimeMs = 3_600_000L,
        timeZone = "UTC",
        isAllDay = 0,
        colorHex = "#FF0000",
        rrule = "FREQ=WEEKLY",
        source = "GOOGLE",
        googleEventId = "google-event-1",
        googleCalendarId = "provider-calendar-1",
        completedAtMs = null,
        createdAtMs = 0L,
        updatedAtMs = 0L,
        voiceNotePath = null,
    )
}
