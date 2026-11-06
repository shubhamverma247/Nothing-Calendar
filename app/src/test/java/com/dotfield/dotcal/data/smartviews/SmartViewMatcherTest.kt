package com.dotfield.dotcal.data.smartviews

import com.dotfield.dotcal.data.CalendarEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SmartViewMatcherTest {
    private val today = LocalDate.of(2026, 10, 26)

    @Test
    fun pendingTasksIncludesUnscheduledOpenTasksOnly() {
        val open = event("open", isTask = 1, isCompleted = 0, start = null)
        val completed = event("completed", isTask = 1, isCompleted = 1, start = null)
        val calendarEvent = event("event", isTask = 0, isCompleted = 0, start = today.atTime(10, 0))

        val result = SmartViewMatcher.filter(
            items = listOf(open, completed, calendarEvent),
            view = SmartView.pendingTasks(),
            today = today,
            weekStart = DayOfWeek.MONDAY,
        )

        assertEquals(listOf("open"), result.map { it.id })
    }

    @Test
    fun upcomingAppointmentsExcludesPastEventsAndTasks() {
        val futureEvent = event("future", start = today.plusDays(1).atTime(9, 0))
        val pastEvent = event("past", start = today.minusDays(1).atTime(9, 0))
        val futureTask = event("task", isTask = 1, start = today.plusDays(1).atTime(9, 0))

        val result = SmartViewMatcher.filter(
            items = listOf(pastEvent, futureTask, futureEvent),
            view = SmartView.upcomingAppointments(),
            today = today,
            weekStart = DayOfWeek.MONDAY,
        )

        assertEquals(listOf("future"), result.map { it.id })
    }

    @Test
    fun upcomingAppointmentsExcludesEventsThatEndedEarlierToday() {
        val zone = ZoneId.systemDefault()
        val nowMs = today.atTime(18, 0).atZone(zone).toInstant().toEpochMilli()
        val finished = event("finished", start = today.atTime(9, 0))
        val ongoing = event("ongoing", start = today.atTime(17, 30))
        val future = event("future", start = today.plusDays(1).atTime(9, 0))

        val result = SmartViewMatcher.filter(
            items = listOf(finished, ongoing, future),
            view = SmartView.upcomingAppointments(),
            today = today,
            weekStart = DayOfWeek.MONDAY,
            nowMs = nowMs,
        )

        assertEquals(listOf("ongoing", "future"), result.map { it.id })
    }

    @Test
    fun thisWeekRespectsConfiguredWeekStartAndCalendar() {
        val sunday = LocalDate.of(2026, 11, 1)
        val workSunday = event("work", accountId = "work", start = sunday.atTime(9, 0))
        val personalSunday = event("personal", accountId = "personal", start = sunday.atTime(9, 0))
        val nextMonday = event("next", accountId = "work", start = sunday.plusDays(1).atTime(9, 0))
        val view = SmartView(
            id = "work-week",
            name = "Work this week",
            type = SmartViewType.Events,
            dateRange = SmartViewDateRange.ThisWeek,
            accountId = "work",
            createdAtMs = 1L,
        )

        val result = SmartViewMatcher.filter(
            items = listOf(nextMonday, personalSunday, workSunday),
            view = view,
            today = today,
            weekStart = DayOfWeek.MONDAY,
        )

        assertEquals(listOf("work"), result.map { it.id })
    }

    @Test
    fun recurringExpansionRangeCoversUpcomingAndCurrentMonthViews() {
        assertEquals(
            today to today.plusYears(1),
            SmartViewMatcher.recurrenceExpansionRange(SmartViewDateRange.Upcoming, today),
        )
        assertEquals(
            today.withDayOfMonth(1) to today.plusMonths(1).withDayOfMonth(1),
            SmartViewMatcher.recurrenceExpansionRange(SmartViewDateRange.ThisMonth, today),
        )
        assertEquals(null, SmartViewMatcher.recurrenceExpansionRange(SmartViewDateRange.AnyTime, today))
    }

    @Test
    fun pastRecurrenceExpansionIncludesOccurrencesEarlierToday() {
        assertEquals(
            today.minusYears(1) to today.plusDays(1),
            SmartViewMatcher.recurrenceExpansionRange(SmartViewDateRange.Past, today),
        )
    }

    @Test
    fun databaseQueryRangeBoundsUpcomingAndCurrentMonthViews() {
        val zone = ZoneId.of("UTC")
        val nowMs = today.atTime(18, 0).atZone(zone).toInstant().toEpochMilli()

        assertEquals(
            SmartViewQueryRange(nowMs, Long.MAX_VALUE),
            SmartViewMatcher.databaseQueryRange(SmartViewDateRange.Upcoming, today, nowMs, zone),
        )
        assertEquals(
            SmartViewQueryRange(
                today.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli(),
                today.plusMonths(1).withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli(),
            ),
            SmartViewMatcher.databaseQueryRange(SmartViewDateRange.ThisMonth, today, nowMs, zone),
        )
        assertNull(SmartViewMatcher.databaseQueryRange(SmartViewDateRange.AnyTime, today, nowMs, zone))
    }

    private fun event(
        id: String,
        accountId: String = "local",
        isTask: Int = 0,
        isCompleted: Int = 0,
        start: LocalDateTime? = today.atTime(LocalTime.NOON),
    ): CalendarEvent {
        val zone = ZoneId.systemDefault()
        val startMs = start?.atZone(zone)?.toInstant()?.toEpochMilli() ?: 0L
        return CalendarEvent(
            id = id,
            accountId = accountId,
            title = id,
            startTimeMs = startMs,
            endTimeMs = if (startMs == 0L) 0L else startMs + 3_600_000L,
            timeZone = zone.id,
            isAllDay = if (startMs == 0L) 1 else 0,
            colorHex = null,
            rrule = null,
            source = "LOCAL",
            googleEventId = null,
            googleCalendarId = null,
            isTask = isTask,
            isCompleted = isCompleted,
            completedAtMs = null,
            voiceNotePath = null,
            createdAtMs = 0L,
            updatedAtMs = 0L,
        )
    }
}
