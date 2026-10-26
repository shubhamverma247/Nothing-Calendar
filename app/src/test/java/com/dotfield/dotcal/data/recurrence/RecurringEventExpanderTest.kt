package com.dotfield.dotcal.data.recurrence

import com.dotfield.dotcal.data.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class RecurringEventExpanderTest {
    @Test
    fun parameterizedWeeklyRuleIncludesSundayOccurrence() {
        val zoneId = ZoneId.of("UTC")
        val masterStart = LocalDate.of(2026, 10, 4).atTime(14, 0).atZone(zoneId).toInstant().toEpochMilli()
        val durationMs = 60 * 60 * 1_000L
        val event = CalendarEvent(
            id = "weekly-sunday",
            accountId = "local",
            title = "Sunday event",
            startTimeMs = masterStart,
            endTimeMs = masterStart + durationMs,
            timeZone = zoneId.id,
            isAllDay = 0,
            colorHex = null,
            rrule = "FREQ=WEEKLY;BYDAY=SU;WKST=MO",
            source = "LOCAL",
            googleEventId = null,
            googleCalendarId = null,
            completedAtMs = null,
            voiceNotePath = null,
            createdAtMs = masterStart,
            updatedAtMs = masterStart,
        )

        val occurrences = expandRecurringEvents(
            events = listOf(event),
            rangeStart = LocalDate.of(2026, 10, 25),
            rangeEndExclusive = LocalDate.of(2026, 10, 26),
        )

        assertEquals(1, occurrences.size)
        assertEquals(
            LocalDate.of(2026, 10, 25).atTime(14, 0).atZone(zoneId).toInstant().toEpochMilli(),
            occurrences.single().startTimeMs,
        )
    }

    @Test
    fun providerRdateWithoutRuleCreatesVisibleOccurrence() {
        val zoneId = ZoneId.of("UTC")
        val masterStart = LocalDate.of(2026, 10, 4).atTime(14, 0).atZone(zoneId).toInstant().toEpochMilli()
        val expectedStart = LocalDate.of(2026, 10, 25).atTime(14, 0).atZone(zoneId).toInstant().toEpochMilli()
        val event = CalendarEvent(
            id = "rdate-only",
            accountId = "local",
            title = "Extra date",
            startTimeMs = masterStart,
            endTimeMs = masterStart + 3_600_000L,
            timeZone = zoneId.id,
            isAllDay = 0,
            colorHex = null,
            rrule = null,
            source = "LOCAL",
            googleEventId = null,
            googleCalendarId = null,
            completedAtMs = null,
            voiceNotePath = null,
            createdAtMs = masterStart,
            updatedAtMs = masterStart,
        )

        val occurrences = expandRecurringEvents(
            events = listOf(event),
            rangeStart = LocalDate.of(2026, 10, 25),
            rangeEndExclusive = LocalDate.of(2026, 10, 26),
            providerRdates = mapOf(event.id to "20261025T140000Z"),
        )

        assertEquals(true, occurrences.any { it.startTimeMs == expectedStart })
    }
}
