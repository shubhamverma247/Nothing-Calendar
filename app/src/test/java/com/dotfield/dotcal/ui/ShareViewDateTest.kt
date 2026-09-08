package com.dotfield.dotcal.ui

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ShareViewDateTest {
    @Test
    fun monthShareUsesVisibleMonthAfterNavigation() {
        val visibleMonth = LocalDate.of(2026, 10, 1)
        val selectedDate = LocalDate.of(2026, 9, 6)

        assertEquals(visibleMonth, shareViewDate(CalendarTab.Month, visibleMonth, selectedDate))
    }

    @Test
    fun nonMonthShareUsesSelectedDate() {
        val visibleMonth = LocalDate.of(2026, 10, 1)
        val selectedDate = LocalDate.of(2026, 9, 6)

        assertEquals(selectedDate, shareViewDate(CalendarTab.Week, visibleMonth, selectedDate))
    }
}
