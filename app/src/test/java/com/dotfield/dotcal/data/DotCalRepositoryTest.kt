package com.dotfield.dotcal.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DotCalRepositoryTest {
    @Test
    fun withProviderSyncResultPreservesProviderColorAndIds() {
        val draft = event(
            id = "local-draft",
            accountId = "provider-calendar-1",
            colorHex = null,
            source = "LOCAL",
            googleEventId = null,
            googleCalendarId = null,
            syncVersion = 3,
        )
        val providerEvent = event(
            id = "provider-calendar-1-event-42",
            accountId = "provider-calendar-1",
            colorHex = "#3366FF",
            source = "GOOGLE",
            googleEventId = "42",
            googleCalendarId = "1",
            syncVersion = 9,
        )

        val merged = draft.withProviderSyncResult(providerEvent)

        assertEquals("#3366FF", merged.colorHex)
        assertEquals("provider-calendar-1-event-42", merged.id)
        assertEquals("GOOGLE", merged.source)
        assertEquals("42", merged.googleEventId)
        assertEquals("1", merged.googleCalendarId)
        assertEquals(9, merged.syncVersion)
    }

    private fun event(
        id: String,
        accountId: String,
        colorHex: String?,
        source: String,
        googleEventId: String?,
        googleCalendarId: String?,
        syncVersion: Int,
    ) = CalendarEvent(
        id = id,
        accountId = accountId,
        title = "Test",
        startTimeMs = 0L,
        endTimeMs = 3_600_000L,
        timeZone = "UTC",
        isAllDay = 0,
        colorHex = colorHex,
        rrule = null,
        source = source,
        googleEventId = googleEventId,
        googleCalendarId = googleCalendarId,
        syncVersion = syncVersion,
        completedAtMs = null,
        createdAtMs = 0L,
        updatedAtMs = 0L,
        voiceNotePath = null,
    )
}
