package com.dotfield.dotcal.data.smartviews

import com.dotfield.dotcal.data.CalendarEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.util.UUID

enum class SmartViewType { All, Events, Tasks }

enum class SmartViewDateRange { AnyTime, Upcoming, Past, ThisWeek, ThisMonth }

enum class SmartViewTaskStatus { Any, Pending, Completed }

data class SmartView(
    val id: String,
    val name: String,
    val query: String = "",
    val type: SmartViewType = SmartViewType.All,
    val dateRange: SmartViewDateRange = SmartViewDateRange.AnyTime,
    val accountId: String? = null,
    val taskStatus: SmartViewTaskStatus = SmartViewTaskStatus.Any,
    val createdAtMs: Long,
) {
    companion object {
        fun newId(): String = UUID.randomUUID().toString()

        fun pendingTasks() = SmartView(
            id = "builtin-pending-tasks",
            name = "Pending tasks",
            type = SmartViewType.Tasks,
            taskStatus = SmartViewTaskStatus.Pending,
            createdAtMs = 0L,
        )

        fun upcomingAppointments() = SmartView(
            id = "builtin-upcoming-appointments",
            name = "Upcoming appointments",
            type = SmartViewType.Events,
            dateRange = SmartViewDateRange.Upcoming,
            createdAtMs = 0L,
        )
    }
}

data class SmartViewQueryRange(
    val startMs: Long,
    val endMs: Long,
)

object SmartViewMatcher {
    // shortcut: past/upcoming recurring views use a one-year horizon, add paging before widening it.
    fun recurrenceExpansionRange(range: SmartViewDateRange, today: LocalDate): Pair<LocalDate, LocalDate>? =
        when (range) {
            SmartViewDateRange.Upcoming -> today to today.plusYears(1)
            SmartViewDateRange.Past -> today.minusYears(1) to today.plusDays(1)
            SmartViewDateRange.ThisWeek -> today.minusDays(6) to today.plusDays(8)
            SmartViewDateRange.ThisMonth -> today.withDayOfMonth(1) to today.plusMonths(1).withDayOfMonth(1)
            SmartViewDateRange.AnyTime -> null
        }

    fun databaseQueryRange(
        range: SmartViewDateRange,
        today: LocalDate,
        nowMs: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): SmartViewQueryRange? = when (range) {
        SmartViewDateRange.AnyTime -> null
        SmartViewDateRange.Upcoming -> SmartViewQueryRange(nowMs, Long.MAX_VALUE)
        SmartViewDateRange.Past -> SmartViewQueryRange(1L, nowMs)
        SmartViewDateRange.ThisWeek -> SmartViewQueryRange(
            today.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            today.plusDays(8).atStartOfDay(zoneId).toInstant().toEpochMilli(),
        )
        SmartViewDateRange.ThisMonth -> SmartViewQueryRange(
            today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
            today.plusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli(),
        )
    }

    fun filter(
        items: List<CalendarEvent>,
        view: SmartView,
        today: LocalDate,
        weekStart: DayOfWeek,
        zoneId: ZoneId = ZoneId.systemDefault(),
        nowMs: Long = today.atStartOfDay(zoneId).toInstant().toEpochMilli(),
    ): List<CalendarEvent> {
        val weekStartDate = today.with(TemporalAdjusters.previousOrSame(weekStart))
        val weekEnd = weekStartDate.plusWeeks(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val weekStartMs = weekStartDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val monthStart = today.withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val monthEnd = today.plusMonths(1).withDayOfMonth(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        return items.filter { item ->
            matchesType(item, view.type) &&
                matchesDate(item, view.dateRange, nowMs, weekStartMs, weekEnd, monthStart, monthEnd) &&
                (view.accountId == null || item.accountId == view.accountId) &&
                matchesTaskStatus(item, view.taskStatus)
        }
    }

    private fun matchesType(item: CalendarEvent, type: SmartViewType): Boolean = when (type) {
        SmartViewType.All -> true
        SmartViewType.Events -> item.isTask == 0
        SmartViewType.Tasks -> item.isTask == 1
    }

    private fun matchesDate(
        item: CalendarEvent,
        range: SmartViewDateRange,
        nowMs: Long,
        weekStart: Long,
        weekEnd: Long,
        monthStart: Long,
        monthEnd: Long,
    ): Boolean = when (range) {
        SmartViewDateRange.AnyTime -> true
        SmartViewDateRange.Upcoming -> item.startTimeMs > 0L && item.endTimeMs > nowMs
        SmartViewDateRange.Past -> item.startTimeMs > 0L && item.endTimeMs <= nowMs
        SmartViewDateRange.ThisWeek -> item.startTimeMs in weekStart until weekEnd
        SmartViewDateRange.ThisMonth -> item.startTimeMs in monthStart until monthEnd
    }

    private fun matchesTaskStatus(item: CalendarEvent, status: SmartViewTaskStatus): Boolean = when (status) {
        SmartViewTaskStatus.Any -> true
        SmartViewTaskStatus.Pending -> item.isTask == 1 && item.isCompleted == 0
        SmartViewTaskStatus.Completed -> item.isTask == 1 && item.isCompleted == 1
    }
}
