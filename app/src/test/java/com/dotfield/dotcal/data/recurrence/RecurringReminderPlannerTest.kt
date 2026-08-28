package com.dotfield.dotcal.data.recurrence

import com.dotfield.dotcal.data.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class RecurringReminderPlannerTest {
    private val zone = ZoneId.of("UTC")

    @Test
    fun plansTheNextDailyOccurrenceAfterTheMasterHasPassed() {
        val event = event("20260827T0900", "FREQ=DAILY")
        val now = LocalDateTime.of(2026, 8, 28, 8, 0).atZone(zone).toInstant().toEpochMilli()

        val plan = planNextReminder(event, minutesBefore = 15, nowMs = now)

        assertEquals(
            LocalDateTime.of(2026, 8, 28, 8, 45).atZone(zone).toInstant().toEpochMilli(),
            plan?.triggerAtMs,
        )
    }

    @Test
    fun skipsExceptionAndStopsAtCount() {
        val first = LocalDateTime.of(2026, 8, 27, 9, 0).atZone(zone).toInstant().toEpochMilli()
        val excluded = LocalDateTime.of(2026, 8, 28, 9, 0).atZone(zone).toInstant().toEpochMilli()
        val event = event("20260827T0900", "FREQ=DAILY;COUNT=2").copy(exceptionDates = "[$excluded]")
        val now = first - 60_000L

        assertNull(planNextReminder(event, minutesBefore = 15, nowMs = now))
    }

    @Test
    fun includesProviderRdateAsNextOccurrence() {
        val event = event("20260827T0900", null).apply {
            providerRdate = "20260830T090000Z"
        }
        val now = LocalDateTime.of(2026, 8, 28, 9, 0).atZone(zone).toInstant().toEpochMilli()

        val plan = planNextReminder(event, minutesBefore = 30, nowMs = now)

        assertEquals(
            LocalDateTime.of(2026, 8, 30, 8, 30).atZone(zone).toInstant().toEpochMilli(),
            plan?.triggerAtMs,
        )
    }

    private fun event(start: String, rrule: String?): CalendarEvent {
        val startTime = LocalDateTime.parse(start, java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm"))
            .atZone(zone).toInstant().toEpochMilli()
        return CalendarEvent(
            id = "recurring",
            accountId = "local",
            title = "Recurring",
            startTimeMs = startTime,
            endTimeMs = startTime + 60 * 60_000L,
            timeZone = zone.id,
            isAllDay = 0,
            colorHex = null,
            rrule = rrule,
            source = "LOCAL",
            googleEventId = null,
            googleCalendarId = null,
            completedAtMs = null,
            voiceNotePath = null,
            createdAtMs = 0,
            updatedAtMs = 0,
        )
    }
}
