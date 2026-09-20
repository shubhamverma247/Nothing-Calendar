package com.dotfield.dotcal.ui

import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.CalendarAccount
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class UiHelpersTest {
    @Test
    fun selectedCalendarCountExcludesLocalAndHiddenAccounts() {
        val accounts = listOf(
            CalendarAccount("local-primary", "", "", "LOCAL", "", 1, 1, 0),
            CalendarAccount("visible", "", "", "GOOGLE", "", 1, 0, 1),
            CalendarAccount("hidden", "", "", "GOOGLE", "", 0, 0, 2),
        )

        assertEquals(1, selectedCalendarAccountCount(accounts))
    }

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

    @Test
    fun startChangePreservesCurrentEventDurationWhenEndWasNotManuallyEdited() {
        val date = LocalDate.of(2026, 9, 20)

        val adjusted = adjustEventEndForStartChange(
            previousStartDate = date,
            previousStartTime = LocalTime.of(9, 0),
            previousEndDate = date,
            previousEndTime = LocalTime.of(10, 30),
            newStartDate = date,
            newStartTime = LocalTime.of(11, 15),
            defaultDurationMinutes = 60,
            endManuallyEdited = false,
        )

        assertEquals(EventEditorEnd(date, LocalTime.of(12, 45)), adjusted)
    }

    @Test
    fun startChangeCanCarryDurationAcrossMidnight() {
        val date = LocalDate.of(2026, 9, 20)

        val adjusted = adjustEventEndForStartChange(
            previousStartDate = date,
            previousStartTime = LocalTime.of(22, 30),
            previousEndDate = date.plusDays(1),
            previousEndTime = LocalTime.of(0, 30),
            newStartDate = date,
            newStartTime = LocalTime.of(23, 15),
            defaultDurationMinutes = 60,
            endManuallyEdited = false,
        )

        assertEquals(EventEditorEnd(date.plusDays(1), LocalTime.of(1, 15)), adjusted)
    }

    @Test
    fun manuallyEditedEndStaysPutWhenStillAfterNewStart() {
        val date = LocalDate.of(2026, 9, 20)

        val adjusted = adjustEventEndForStartChange(
            previousStartDate = date,
            previousStartTime = LocalTime.of(9, 0),
            previousEndDate = date,
            previousEndTime = LocalTime.of(13, 0),
            newStartDate = date,
            newStartTime = LocalTime.of(11, 0),
            defaultDurationMinutes = 60,
            endManuallyEdited = true,
        )

        assertEquals(EventEditorEnd(date, LocalTime.of(13, 0)), adjusted)
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
