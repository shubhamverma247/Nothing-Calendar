package com.dotfield.dotcal.data.punchcard

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PunchCardStreakTest {
    @Test
    fun computesStreakAcrossMonthBoundary() {
        val punched = setOf(
            LocalDate.of(2026, 6, 29),
            LocalDate.of(2026, 6, 30),
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 2),
        )

        assertEquals(4, PunchCardStreak.compute(punched, LocalDate.of(2026, 7, 2)))
    }

    @Test
    fun returnsZeroWhenEndingDateIsNotPunched() {
        val punched = setOf(LocalDate.of(2026, 7, 1))

        assertEquals(0, PunchCardStreak.compute(punched, LocalDate.of(2026, 7, 2)))
    }
}
