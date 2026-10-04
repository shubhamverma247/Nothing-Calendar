package com.dotfield.dotcal.data.shifts

import org.junit.Assert.assertEquals
import org.junit.Test

class ShiftTimeMathTest {
    @Test
    fun durationAllowsMinutePrecision() {
        val duration = shiftDurationMinutes(
            startMinuteOfDay = 8 * 60,
            endMinuteOfDay = 16 * 60 + 30,
        )

        assertEquals(8 * 60 + 30, duration)
    }

    @Test
    fun durationAllowsOvernightShift() {
        val duration = shiftDurationMinutes(
            startMinuteOfDay = 22 * 60,
            endMinuteOfDay = 6 * 60,
        )

        assertEquals(8 * 60, duration)
    }

    @Test
    fun endMinuteWrapsAfterMidnight() {
        val endMinute = shiftEndMinuteOfDay(
            startMinuteOfDay = 22 * 60,
            durationMinutes = 10 * 60,
        )

        assertEquals(8 * 60, endMinute)
    }

    @Test
    fun overnightEndUsesNextCalendarDay() {
        assertEquals(1, shiftEndDayOffset(startMinuteOfDay = 22 * 60, durationMinutes = 8 * 60))
        assertEquals(0, shiftEndDayOffset(startMinuteOfDay = 8 * 60, durationMinutes = 8 * 60))
    }

    @Test
    fun breakMustFitInsideTimedShift() {
        assertEquals(true, isValidShiftBreakMinutes(null, durationMinutes = 8 * 60))
        assertEquals(true, isValidShiftBreakMinutes(1, durationMinutes = 8 * 60))
        assertEquals(true, isValidShiftBreakMinutes(479, durationMinutes = 8 * 60))
        assertEquals(false, isValidShiftBreakMinutes(0, durationMinutes = 8 * 60))
        assertEquals(false, isValidShiftBreakMinutes(480, durationMinutes = 8 * 60))
        assertEquals(false, isValidShiftBreakMinutes(-1, durationMinutes = 8 * 60))
    }
}
