package com.dotfield.dotcal.widget

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

class WidgetCalendarWeekStartTest {
    @Test
    fun mondayPreferenceStartsWidgetMonthGridOnMonday() {
        val days = widgetMonthDays(
            displayMonthDate = LocalDate.of(2026, 2, 1),
            today = LocalDate.of(2026, 2, 12),
            weekStart = DayOfWeek.MONDAY,
        )

        assertEquals(null, days[0].dayOfMonth)
        assertEquals(null, days[5].dayOfMonth)
        assertEquals(1, days[6].dayOfMonth)
    }

    @Test
    fun sundayPreferenceKeepsWidgetMonthGridOnSunday() {
        val days = widgetMonthDays(
            displayMonthDate = LocalDate.of(2026, 2, 1),
            today = LocalDate.of(2026, 2, 12),
            weekStart = DayOfWeek.SUNDAY,
        )

        assertEquals(1, days[0].dayOfMonth)
    }

    @Test
    fun weekdayLabelsFollowResolvedWeekStart() {
        assertEquals(
            listOf("M", "T", "W", "T", "F", "S", "S"),
            widgetWeekdayLabels(DayOfWeek.MONDAY, Locale.US),
        )
        assertEquals(
            listOf("S", "M", "T", "W", "T", "F", "S"),
            widgetWeekdayLabels(DayOfWeek.SUNDAY, Locale.US),
        )
    }
}
