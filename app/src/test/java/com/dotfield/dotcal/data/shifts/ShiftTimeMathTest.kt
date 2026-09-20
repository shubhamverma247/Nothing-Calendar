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
}
