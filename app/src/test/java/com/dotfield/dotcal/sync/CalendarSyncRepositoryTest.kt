package com.dotfield.dotcal.sync

import com.dotfield.dotcal.data.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CalendarSyncRepositoryTest {
    @Test
    fun applyProviderRecurringExceptionMetadataKeepsModifiedOccurrencesAsStandalone() {
        val parent = providerEvent("10", rrule = "FREQ=WEEKLY")
        val modifiedStart = 1_000L
        val modified = providerEvent("11").apply {
            providerOriginalGoogleEventId = "10"
            providerOriginalInstanceTimeMs = modifiedStart
        }

        val result = applyProviderRecurringExceptionMetadata(listOf(parent, modified))

        assertEquals(2, result.size)
        assertEquals("[$modifiedStart]", result.first { it.googleEventId == "10" }.exceptionDates)
        assertNull(result.first { it.googleEventId == "11" }.rrule)
    }

    @Test
    fun applyProviderRecurringExceptionMetadataDropsCancelledOccurrencesButKeepsException() {
        val parent = providerEvent("20", rrule = "FREQ=WEEKLY")
        val cancelledStart = 2_000L
        val cancelled = providerEvent("21").apply {
            providerOriginalGoogleEventId = "20"
            providerOriginalInstanceTimeMs = cancelledStart
            providerStatus = 2
        }

        val result = applyProviderRecurringExceptionMetadata(listOf(parent, cancelled))

        assertEquals(listOf("20"), result.mapNotNull { it.googleEventId })
        assertEquals("[$cancelledStart]", result.single().exceptionDates)
    }

    @Test
    fun staleProviderDuplicateIdsDeletesMovedCalendarCopyOnly() {
        val provider = providerEvent("30").copy(
            id = "provider-calendar-2-event-30",
            accountId = "provider-calendar-2",
            googleCalendarId = "2",
        )
        val stale = providerEvent("30").copy(
            id = "provider-calendar-1-event-30",
            accountId = "provider-calendar-1",
            googleCalendarId = "1",
        )
        val current = provider.copy(id = "local-existing-current")

        val staleIds = staleProviderDuplicateIds(
            providerByGoogleId = mapOf("30" to provider),
            existingProviderEvents = listOf(stale, current),
            localByGoogleId = mapOf("30" to current),
        )

        assertEquals(listOf(stale.id), staleIds)
    }

    private fun providerEvent(
        googleEventId: String,
        rrule: String? = null,
    ): CalendarEvent {
        return CalendarEvent(
            id = "provider-calendar-1-event-$googleEventId",
            accountId = "provider-calendar-1",
            title = "Event $googleEventId",
            startTimeMs = 0L,
            endTimeMs = 3_600_000L,
            timeZone = "UTC",
            isAllDay = 0,
            colorHex = "#FF3B30",
            rrule = rrule,
            source = "GOOGLE",
            googleEventId = googleEventId,
            googleCalendarId = "1",
            completedAtMs = null,
            createdAtMs = 0L,
            updatedAtMs = 0L,
            voiceNotePath = null,
        )
    }
}
