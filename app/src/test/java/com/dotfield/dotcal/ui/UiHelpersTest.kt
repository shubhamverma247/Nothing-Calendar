package com.dotfield.dotcal.ui

import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.CALENDAR_ACCESS_READ
import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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
    fun temporaryCalendarTabSelectionDoesNotPersistDefaultView() {
        assertFalse(CalendarTabSelectionSource.TemporaryNavigation.shouldPersistDefaultView())
        assertTrue(CalendarTabSelectionSource.SettingsDefault.shouldPersistDefaultView())
    }

    @Test
    fun visibleCalendarTabsIgnoreHiddenEntries() {
        val hidden = setOf(CalendarTab.Week, CalendarTab.Day)

        assertEquals(
            listOf(CalendarTab.Year, CalendarTab.Month, CalendarTab.Agenda),
            CalendarTab.visiblePickerEntries(hidden),
        )
    }

    @Test
    fun visibleCalendarTabsFallbackWhenAllViewsHidden() {
        assertEquals(
            CalendarTab.pickerEntries,
            CalendarTab.visiblePickerEntries(CalendarTab.pickerEntries.toSet()),
        )
    }

    @Test
    fun calendarTabVisibilityDoesNotHideLastVisibleView() {
        val hidden = CalendarTab.pickerEntries.filterNot { it == CalendarTab.Month }.toSet()

        assertEquals(hidden, updateHiddenCalendarViews(hidden, CalendarTab.Month, visible = false))
        assertEquals(hidden - CalendarTab.Week, updateHiddenCalendarViews(hidden, CalendarTab.Week, visible = true))
    }

    @Test
    fun calendarTabStorageRoundTripsPublicTabsOnly() {
        val hidden = setOf(CalendarTab.Week, CalendarTab.Agenda, CalendarTab.ThreeDay)
        val stored = CalendarTab.hiddenToStorage(hidden)

        assertEquals(setOf(CalendarTab.Agenda, CalendarTab.Week), CalendarTab.hiddenFromStorage(stored))
    }

    @Test
    fun datePickerMillisRoundTripsThroughUtcDate() {
        val date = LocalDate.of(2036, 2, 29)

        assertEquals(date, datePickerDateFromMillis(date.toDatePickerUtcMillis()))
    }

    @Test
    fun eventRowMarkerUsesCalendarColorOnlyWhenEnabled() {
        val event = allDayEvent(0L, 86_400_000L, "UTC", "GOOGLE").copy(colorHex = null)
        val palette = testPalette()

        assertEquals(palette.accent, eventRowCalendarMarkerColor(event, palette, showCalendarColor = true))
        assertNull(eventRowCalendarMarkerColor(event, palette, showCalendarColor = false))
    }

    @Test
    fun defaultEventAccountUsesSavedWritableAccount() {
        val accounts = listOf(
            CalendarAccount("local-primary", "", "Local", "LOCAL", "", 1, 1, 0),
            CalendarAccount("work", "me@example.com", "Work", "GOOGLE", "", 1, 0, 1),
        )

        assertEquals("work", resolveDefaultEventAccountId("work", accounts))
    }

    @Test
    fun defaultEventAccountFallsBackWhenSavedAccountIsReadOnlyOrMissing() {
        val accounts = listOf(
            CalendarAccount("readonly", "me@example.com", "Read only", "GOOGLE", "", 1, 1, 0, accessLevel = CALENDAR_ACCESS_READ),
            CalendarAccount("work", "me@example.com", "Work", "GOOGLE", "", 1, 0, 1),
            CalendarAccount("local-primary", "", "Local", "LOCAL", "", 1, 0, 2),
        )

        assertEquals("work", resolveDefaultEventAccountId("readonly", accounts))
        assertEquals("work", resolveDefaultEventAccountId("missing", accounts))
    }

    @Test
    fun defaultEventAccountFallsBackToLocalWhenNoWritableAccountLoaded() {
        assertEquals("local-primary", resolveDefaultEventAccountId("missing", emptyList()))
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

    private fun testPalette() = DotCalPalette(
        background = Color.White,
        primaryText = Color.Black,
        secondaryText = Color.Gray,
        dimText = Color.LightGray,
        line = Color.LightGray,
        cell = Color.White,
        calendarSurface = Color.White,
        topBarSurface = Color.White,
        bottomNavSurface = Color.White,
        dialogSurface = Color.White,
        cancelSurface = Color.White,
        cancelBorder = Color.LightGray,
        dragHandle = Color.LightGray,
        eventCardSurface = Color.White,
        eventCardBorder = Color.LightGray,
        eventCardChevron = Color.Gray,
        textFieldBorder = Color.LightGray,
        segmentSelected = Color.LightGray,
        disabledText = Color.LightGray,
        switchOffTrack = Color.LightGray,
        dot = Color.Gray,
        yearWeekday = Color.Gray,
        yearMonthLabel = Color.Black,
        accent = Color.Red,
        onAccent = Color.White,
        isDark = false,
    )
}
