package com.dotfield.dotcal.widget

import android.content.Context
import android.text.format.DateFormat
import com.dotfield.dotcal.data.CalendarDao
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.DotCalDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

data class WidgetEventItem(
    val id: String,
    val title: String,
    val timeLabel: String,
    val location: String,
    val dateLabel: String,
    val dayOfMonth: String,
    val startTimeMs: Long,
)

data class WidgetCalendarDay(
    val dayOfMonth: Int?,
    val dateIso: String? = null,
    val isToday: Boolean = false,
    val hasEvents: Boolean = false,
)

data class WidgetCalendarData(
    val header: String,
    val monthLabel: String,
    val todayLabel: String,
    val nextEvent: WidgetEventItem?,
    val events: List<WidgetEventItem>,
    val moreItemCount: Int,
    val days: List<WidgetCalendarDay> = emptyList(),
)

class WidgetDataRepository(
    private val context: Context,
    private val dao: CalendarDao,
) {
    suspend fun load(size: DotCalWidgetSize, accountId: String? = null, nowMs: Long = System.currentTimeMillis()): WidgetCalendarData = withContext(Dispatchers.IO) {
        val zoneId = ZoneId.systemDefault()
        val now = Instant.ofEpochMilli(nowMs).atZone(zoneId)
        val today = now.toLocalDate()
        val rangeEnd = today.plusDays(WIDGET_RANGE_DAYS)
        val visibleItems = dao.getVisibleEventsForWidget(today.atStartMs(zoneId), rangeEnd.atStartMs(zoneId), accountId)
            .expandRecurring(today, rangeEnd)
            .filter { it.endTimeMs >= nowMs }
            .sortedForWidget(zoneId)
        val use24Hour = DateFormat.is24HourFormat(context)
        val items = visibleItems.asWidgetItems(zoneId, use24Hour)
        WidgetCalendarData(
            header = today.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
            monthLabel = today.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            todayLabel = today.dayOfMonth.toString(),
            nextEvent = items.firstOrNull(),
            events = items.take(size.maxItems),
            moreItemCount = (items.size - size.maxItems).coerceAtLeast(0),
            days = if (size == DotCalWidgetSize.Large) monthDays(today, visibleItems, zoneId) else emptyList(),
        )
    }

    private fun List<CalendarEvent>.sortedForWidget(zoneId: ZoneId): List<CalendarEvent> {
        return sortedWith(
            compareBy<CalendarEvent> { it.widgetDayStart(zoneId) }
                .thenBy { it.widgetPriority() }
                .thenBy { it.startTimeMs }
                .thenBy { it.title },
        )
    }

    private fun CalendarEvent.widgetDayStart(zoneId: ZoneId): Long {
        return Instant.ofEpochMilli(startTimeMs).atZone(zoneId).toLocalDate().atStartMs(zoneId)
    }

    private fun CalendarEvent.widgetPriority(): Int {
        return if (isAllDay == 0) 0 else 1
    }

    private fun List<CalendarEvent>.asWidgetItems(zoneId: ZoneId, use24Hour: Boolean): List<WidgetEventItem> {
        return map { event ->
            val dateTime = Instant.ofEpochMilli(event.startTimeMs).atZone(zoneId)
            WidgetEventItem(
                id = event.id.substringBefore(RECURRENCE_SEPARATOR),
                title = event.title.toWidgetTitleCase(),
                timeLabel = event.widgetTimeLabel(zoneId, use24Hour),
                location = event.location.trim(),
                dateLabel = dateTime.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                dayOfMonth = dateTime.dayOfMonth.toString(),
                startTimeMs = event.startTimeMs,
            )
        }
    }

    private fun CalendarEvent.widgetTimeLabel(zoneId: ZoneId, use24Hour: Boolean): String {
        if (isAllDay == 0) {
            val pattern = if (use24Hour) "HH:mm" else "h:mm a"
            return Instant.ofEpochMilli(startTimeMs).atZone(zoneId).format(DateTimeFormatter.ofPattern(pattern)).uppercase(Locale.getDefault())
        }
        return "All day"
    }

    private fun String.toWidgetTitleCase(): String {
        return trim().split(Regex("\\s+")).joinToString(" ") { word ->
            if (word.length > 1 && word.all { it.isUpperCase() || !it.isLetter() }) {
                word
            } else {
                word.lowercase(Locale.getDefault()).replaceFirstChar { first ->
                    if (first.isLowerCase()) first.titlecase(Locale.getDefault()) else first.toString()
                }
            }
        }
    }

    private fun List<CalendarEvent>.expandRecurring(rangeStart: LocalDate, rangeEndExclusive: LocalDate): List<CalendarEvent> {
        return flatMap { event ->
            when (event.rrule?.trim()) {
                "FREQ=DAILY" -> event.generateOccurrences(rangeStart, rangeEndExclusive) { it.plusDays(1) }
                "FREQ=WEEKLY" -> event.generateOccurrences(event.firstWeeklyDate(rangeStart), rangeEndExclusive) { it.plusWeeks(1) }
                "FREQ=MONTHLY" -> event.expandMonthly(rangeStart, rangeEndExclusive)
                "FREQ=YEARLY" -> event.expandYearly(rangeStart, rangeEndExclusive)
                else -> listOf(event)
            }
        }
    }

    private fun CalendarEvent.generateOccurrences(
        firstVisibleDate: LocalDate,
        rangeEndExclusive: LocalDate,
        nextDate: (LocalDate) -> LocalDate,
    ): List<CalendarEvent> {
        val firstDate = startDate()
        var cursor = maxOf(firstVisibleDate, firstDate)
        val events = mutableListOf<CalendarEvent>()
        while (cursor < rangeEndExclusive) {
            occurrenceOn(cursor)?.let { events += it }
            cursor = nextDate(cursor)
        }
        return events
    }

    private fun CalendarEvent.expandMonthly(rangeStart: LocalDate, rangeEndExclusive: LocalDate): List<CalendarEvent> {
        val firstDate = startDate()
        var cursorMonth = firstDate.withDayOfMonth(1).plusMonths(
            ChronoUnit.MONTHS.between(firstDate.withDayOfMonth(1), rangeStart.withDayOfMonth(1)).coerceAtLeast(0),
        )
        val events = mutableListOf<CalendarEvent>()
        while (cursorMonth < rangeEndExclusive.withDayOfMonth(1).plusMonths(1)) {
            val date = YearMonth.from(cursorMonth).takeIf { firstDate.dayOfMonth <= it.lengthOfMonth() }?.atDay(firstDate.dayOfMonth)
            if (date != null && date >= rangeStart && date < rangeEndExclusive) occurrenceOn(date)?.let { events += it }
            cursorMonth = cursorMonth.plusMonths(1)
        }
        return events
    }

    private fun CalendarEvent.expandYearly(rangeStart: LocalDate, rangeEndExclusive: LocalDate): List<CalendarEvent> {
        val firstDate = startDate()
        val events = mutableListOf<CalendarEvent>()
        for (year in maxOf(firstDate.year, rangeStart.year)..rangeEndExclusive.year) {
            val date = runCatching { LocalDate.of(year, firstDate.monthValue, firstDate.dayOfMonth) }.getOrNull()
            if (date != null && date >= rangeStart && date < rangeEndExclusive) occurrenceOn(date)?.let { events += it }
        }
        return events
    }

    private fun CalendarEvent.firstWeeklyDate(rangeStart: LocalDate): LocalDate {
        val firstDate = startDate()
        val weeksToRange = ChronoUnit.DAYS.between(firstDate, rangeStart).coerceAtLeast(0) / 7
        return firstDate.plusWeeks(weeksToRange).let { if (it < rangeStart) it.plusWeeks(1) else it }
    }

    private fun CalendarEvent.occurrenceOn(date: LocalDate): CalendarEvent? {
        val zoneId = ZoneId.of(timeZone)
        val startDateTime = Instant.ofEpochMilli(startTimeMs).atZone(zoneId).toLocalDateTime()
        val occurrenceStart = date.atTime(startDateTime.toLocalTime()).atZone(zoneId).toInstant().toEpochMilli()
        if (occurrenceStart in exceptionStartTimes()) return null
        return copy(
            id = "$id$RECURRENCE_SEPARATOR$occurrenceStart",
            startTimeMs = occurrenceStart,
            endTimeMs = occurrenceStart + (endTimeMs - startTimeMs),
        )
    }

    private fun CalendarEvent.startDate(): LocalDate {
        return Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.of(timeZone)).toLocalDate()
    }

    private fun CalendarEvent.exceptionStartTimes(): Set<Long> {
        return exceptionDates.removePrefix("[").removeSuffix("]").split(',').mapNotNull { it.trim().toLongOrNull() }.toSet()
    }

    private fun monthDays(today: LocalDate, events: List<CalendarEvent>, zoneId: ZoneId): List<WidgetCalendarDay> {
        val month = YearMonth.from(today)
        val monthStart = month.atDay(1)
        val leadingBlanks = monthStart.dayOfWeek.value % 7
        val eventDays = events
            .filter { Instant.ofEpochMilli(it.startTimeMs).atZone(zoneId).month == today.month }
            .map { Instant.ofEpochMilli(it.startTimeMs).atZone(zoneId).dayOfMonth }
            .toSet()
        val days = MutableList(leadingBlanks) { WidgetCalendarDay(dayOfMonth = null) }
        days += (1..month.lengthOfMonth()).map { day ->
            val date = month.atDay(day)
            WidgetCalendarDay(dayOfMonth = day, dateIso = date.toString(), isToday = day == today.dayOfMonth, hasEvents = day in eventDays)
        }
        while (days.size % 7 != 0) days += WidgetCalendarDay(dayOfMonth = null)
        return days
    }

    private fun LocalDate.atStartMs(zoneId: ZoneId): Long = atStartOfDay(zoneId).toInstant().toEpochMilli()

    companion object {
        private const val WIDGET_RANGE_DAYS = 45L
        private const val RECURRENCE_SEPARATOR = "::occurrence::"

        fun create(context: Context): WidgetDataRepository {
            return WidgetDataRepository(context.applicationContext, DotCalDatabase.create(context).calendarDao())
        }
    }
}
