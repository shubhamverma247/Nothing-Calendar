package com.dotfield.dotcal.data.shifts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ShiftEventDraftTest {
    private val date = LocalDate.of(2026, 8, 12)

    @Test
    fun timedShiftBuildsSameDayDraft() {
        val type = shiftType(startMinute = 9 * 60, durationMinutes = 8 * 60)

        val draft = buildShiftEventDraft(type, date)

        assertEquals("Day", draft?.title)
        assertEquals(date, draft?.date)
        assertEquals(date, draft?.endDate)
        assertEquals(LocalTime.of(9, 0), draft?.startTime)
        assertEquals(LocalTime.of(17, 0), draft?.endTime)
        assertEquals(false, draft?.isAllDay)
        assertEquals("#FF3B30", draft?.colorHex)
    }

    @Test
    fun overnightShiftBuildsNextDayEndDate() {
        val type = shiftType(startMinute = 22 * 60, durationMinutes = 10 * 60)

        val draft = buildShiftEventDraft(type, date)

        assertEquals(date, draft?.date)
        assertEquals(date.plusDays(1), draft?.endDate)
        assertEquals(LocalTime.of(22, 0), draft?.startTime)
        assertEquals(LocalTime.of(8, 0), draft?.endTime)
    }

    @Test
    fun allDayShiftBuildsSingleAllDayDraft() {
        val type = shiftType(startMinute = null, durationMinutes = null, isAllDay = true)

        val draft = buildShiftEventDraft(type, date)

        assertEquals(date, draft?.date)
        assertEquals(date, draft?.endDate)
        assertEquals(LocalTime.MIDNIGHT, draft?.startTime)
        assertEquals(LocalTime.of(23, 59), draft?.endTime)
        assertEquals(true, draft?.isAllDay)
    }

    @Test
    fun offDayDoesNotBuildDraft() {
        val type = shiftType(startMinute = null, durationMinutes = null)

        assertNull(buildShiftEventDraft(type, date))
    }

    private fun shiftType(
        startMinute: Int?,
        durationMinutes: Int?,
        isAllDay: Boolean = false,
    ) = ShiftType(
        id = "shift-1",
        name = "Day",
        colorHex = "#FF3B30",
        startMinuteOfDay = startMinute,
        durationMinutes = durationMinutes,
        isAllDay = isAllDay,
        reminderMinutes = 30,
        createdAtMs = 1L,
    )
}
