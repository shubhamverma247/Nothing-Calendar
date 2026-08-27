package com.dotfield.dotcal.ui

import com.dotfield.dotcal.data.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class UiHelpersTest {
    @Test
    fun allDayProviderEventUsesItsOwnZoneForDateGrouping() {
        val zone = ZoneId.of("Asia/Kolkata")
        val start = LocalDate.of(2026, 9, 5).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = LocalDate.of(2026, 9, 7).atStartOfDay(zone).toInstant().toEpochMilli()
        val event = allDayEvent(start, end, zone.id, "GOOGLE")

        assertEquals(LocalDate.of(2026, 9, 5), event.localDate())
        assertEquals(listOf(LocalDate.of(2026, 9, 5), LocalDate.of(2026, 9, 6)), event.visibleDates())
        assertEquals(LocalDate.of(2026, 9, 6), event.endLocalDateForEditor())
    }

    @Test
    fun providerGhostEventDoesNotShowDottedBorder() {
        val event = allDayEvent(0L, 86_400_000L, "UTC", "GOOGLE").apply { isGhost = true }
        val localGhost = allDayEvent(0L, 86_400_000L, "UTC", "LOCAL").apply { isGhost = true }

        assertEquals(false, event.shouldShowGhostBorder())
        assertEquals(true, localGhost.shouldShowGhostBorder())
    }

    private fun allDayEvent(startTimeMs: Long, endTimeMs: Long, timeZone: String, source: String) = CalendarEvent(
        id = "provider-calendar-1-event-1",
        accountId = "provider-calendar-1",
        title = "All day",
        startTimeMs = startTimeMs,
        endTimeMs = endTimeMs,
        timeZone = timeZone,
        isAllDay = 1,
        colorHex = "#3366FF",
        rrule = null,
        source = source,
        googleEventId = "1",
        googleCalendarId = "1",
        completedAtMs = null,
        createdAtMs = 0L,
        updatedAtMs = 0L,
        voiceNotePath = null,
    )
}
