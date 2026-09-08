package com.dotfield.dotcal.ui

import java.time.LocalDate
import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test

class MonthTransitionTest {
    @Test
    fun nextMonthSlidesInFromRight() {
        assertEquals(
            1,
            monthTransitionDirection(
                initialMonth = LocalDate.of(2026, 8, 1),
                targetMonth = LocalDate.of(2026, 9, 1),
            ),
        )
    }

    @Test
    fun previousMonthSlidesInFromLeft() {
        assertEquals(
            -1,
            monthTransitionDirection(
                initialMonth = LocalDate.of(2026, 8, 1),
                targetMonth = LocalDate.of(2026, 7, 1),
            ),
        )
    }

    @Test
    fun sameMonthHasNoDirection() {
        assertEquals(
            0,
            monthTransitionDirection(
                initialMonth = LocalDate.of(2026, 8, 1),
                targetMonth = LocalDate.of(2026, 8, 1),
            ),
        )
    }

    @Test
    fun nextPeriodSlidesInFromRight() {
        assertEquals(
            1,
            periodTransitionDirection(
                initialPeriod = LocalDate.of(2026, 8, 10),
                targetPeriod = LocalDate.of(2026, 8, 17),
            ),
        )
    }

    @Test
    fun previousPeriodSlidesInFromLeft() {
        assertEquals(
            -1,
            periodTransitionDirection(
                initialPeriod = LocalDate.of(2026, 8, 17),
                targetPeriod = LocalDate.of(2026, 8, 10),
            ),
        )
    }

    @Test
    fun unchangedPeriodHasNoDirection() {
        assertEquals(
            0,
            periodTransitionDirection(
                initialPeriod = LocalDate.of(2026, 8, 10),
                targetPeriod = LocalDate.of(2026, 8, 10),
            ),
        )
    }

    @Test
    fun datesInsideSameWeekShareTransitionKey() {
        assertEquals(
            LocalDate.of(2026, 8, 10),
            weekTransitionKey(LocalDate.of(2026, 8, 13), DayOfWeek.MONDAY),
        )
    }

    @Test
    fun weekTransitionKeyUsesConfiguredWeekStart() {
        assertEquals(
            LocalDate.of(2026, 8, 9),
            weekTransitionKey(LocalDate.of(2026, 8, 13), DayOfWeek.SUNDAY),
        )
    }
}
