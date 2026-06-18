package com.dotfield.dotcal.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.math.absoluteValue

data class EventEditorData(
    val title: String,
    val description: String,
    val location: String,
    val date: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isAllDay: Boolean,
    val reminderMinutes: Int?,
    val rrule: String?,
)

enum class RecurringEditScope {
    ThisEvent,
    WholeSeries,
}

class DotCalRepository(private val dao: CalendarDao) {
    fun observeEventsForMonth(month: LocalDate): Flow<List<CalendarEvent>> {
        val start = month.withDayOfMonth(1)
        val end = start.plusMonths(1)
        return dao.observeEvents(start.atStartMs(), end.atStartMs())
            .map { events -> expandRecurringEvents(events, start, end) }
    }

    fun observeTasks(): Flow<List<CalendarEvent>> = dao.observeTasks()

    fun observeReminders(): Flow<List<EventReminder>> = dao.observeReminders()

    suspend fun ensureLocalAccount() {
        dao.upsertAccount(
            CalendarAccount(
                id = LOCAL_ACCOUNT_ID,
                accountName = "LOCAL",
                displayName = "Personal",
                accountType = "LOCAL",
                color = "#FF0000",
                isVisible = 1,
                isPrimary = 1,
                sortOrder = 0,
            ),
        )
    }

    suspend fun saveLocalEvent(
        existing: CalendarEvent?,
        data: EventEditorData,
        recurringEditScope: RecurringEditScope = RecurringEditScope.WholeSeries,
    ) {
        require(data.title.isNotBlank()) { "TITLE REQUIRED" }
        val zoneId = ZoneId.systemDefault()
        if (existing?.isRecurrenceOccurrence() == true && recurringEditScope == RecurringEditScope.ThisEvent) {
            saveDetachedOccurrence(existing, data, zoneId)
            return
        }
        val eventId = existing?.baseEventId() ?: UUID.randomUUID().toString()
        val existingMaster = if (existing?.isRecurrenceOccurrence() == true) {
            dao.getEvent(eventId) ?: existing.copy(id = eventId)
        } else {
            existing
        }
        val startDate = if (existing?.isRecurrenceOccurrence() == true && existingMaster?.rrule != null) {
            existingMaster.startDate()
        } else {
            data.date
        }
        val endDate = if (existing?.isRecurrenceOccurrence() == true && existingMaster?.rrule != null) {
            val selectedSpanDays = ChronoUnit.DAYS.between(data.date, data.endDate).coerceAtLeast(0)
            startDate.plusDays(selectedSpanDays)
        } else {
            data.endDate
        }
        val start = if (data.isAllDay) {
            startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        } else {
            startDate.atTime(data.startTime).atZone(zoneId).toInstant().toEpochMilli()
        }
        val end = if (data.isAllDay) {
            endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        } else {
            endDate.atTime(data.endTime).atZone(zoneId).toInstant().toEpochMilli()
        }
        require(end > start) { "END MUST BE AFTER START" }
        val now = System.currentTimeMillis()
        val event = existingMaster?.copy(
            id = eventId,
            title = data.title.trim(),
            description = data.description.trim(),
            location = data.location.trim(),
            startTimeMs = start,
            endTimeMs = end,
            timeZone = zoneId.id,
            isAllDay = if (data.isAllDay) 1 else 0,
            rrule = data.rrule,
            updatedAtMs = now,
        ) ?: CalendarEvent(
                id = eventId,
                accountId = LOCAL_ACCOUNT_ID,
                title = data.title.trim(),
                description = data.description.trim(),
                location = data.location.trim(),
                startTimeMs = start,
                endTimeMs = end,
                timeZone = zoneId.id,
                isAllDay = if (data.isAllDay) 1 else 0,
                colorHex = null,
                rrule = data.rrule,
                source = "LOCAL",
                googleEventId = null,
                googleCalendarId = null,
                completedAtMs = null,
                voiceNotePath = null,
                createdAtMs = now,
                updatedAtMs = now,
            )
        dao.upsertEvent(event)
        dao.deleteRemindersForEvent(eventId)
        data.reminderMinutes?.let { minutes ->
            dao.insertReminders(
                listOf(
                    EventReminder(
                        eventId = eventId,
                        minutesBefore = minutes,
                        triggerAtMs = start - minutes * 60_000L,
                        alarmRequestCode = "$eventId-$minutes".hashCode().absoluteValue,
                    ),
                ),
            )
        }
    }

    suspend fun deleteLocalEvent(
        event: CalendarEvent,
        recurringEditScope: RecurringEditScope = RecurringEditScope.WholeSeries,
    ) {
        if (event.isRecurrenceOccurrence() && recurringEditScope == RecurringEditScope.ThisEvent) {
            excludeOccurrence(event)
            return
        }
        val eventId = event.baseEventId()
        dao.deleteRemindersForEvent(eventId)
        dao.deleteEvent(eventId)
    }

    suspend fun addLocalEvent(title: String, date: LocalDate, startTime: LocalTime = LocalTime.of(9, 0)) {
        saveLocalEvent(
            existing = null,
            data = EventEditorData(
                title = title,
                description = "",
                location = "",
                date = date,
                endDate = date,
                startTime = startTime,
                endTime = startTime.plusHours(1),
                isAllDay = false,
                reminderMinutes = null,
                rrule = null,
            ),
        )
    }

    private fun LocalDate.atStartMs(): Long {
        return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    private suspend fun saveDetachedOccurrence(existing: CalendarEvent, data: EventEditorData, zoneId: ZoneId) {
        val master = excludeOccurrence(existing) ?: return
        val start = if (data.isAllDay) {
            data.date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        } else {
            data.date.atTime(data.startTime).atZone(zoneId).toInstant().toEpochMilli()
        }
        val end = if (data.isAllDay) {
            data.endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        } else {
            data.endDate.atTime(data.endTime).atZone(zoneId).toInstant().toEpochMilli()
        }
        require(end > start) { "END MUST BE AFTER START" }
        val now = System.currentTimeMillis()
        val detachedId = UUID.randomUUID().toString()
        val detachedEvent = master.copy(
            id = detachedId,
            title = data.title.trim(),
            description = data.description.trim(),
            location = data.location.trim(),
            startTimeMs = start,
            endTimeMs = end,
            timeZone = zoneId.id,
            isAllDay = if (data.isAllDay) 1 else 0,
            rrule = null,
            exceptionDates = "[]",
            googleEventId = null,
            googleCalendarId = null,
            syncVersion = 0,
            createdAtMs = now,
            updatedAtMs = now,
        )
        dao.upsertEvent(detachedEvent)
        data.reminderMinutes?.let { minutes ->
            dao.insertReminders(
                listOf(
                    EventReminder(
                        eventId = detachedId,
                        minutesBefore = minutes,
                        triggerAtMs = start - minutes * 60_000L,
                        alarmRequestCode = "$detachedId-$minutes".hashCode().absoluteValue,
                    ),
                ),
            )
        }
    }

    private suspend fun excludeOccurrence(event: CalendarEvent): CalendarEvent? {
        val occurrenceStartMs = event.occurrenceStartMs() ?: return null
        val masterId = event.baseEventId()
        val master = dao.getEvent(masterId) ?: return null
        val exceptions = master.exceptionStartTimes() + occurrenceStartMs
        val updatedMaster = master.copy(
            exceptionDates = exceptions.sorted().joinToString(prefix = "[", postfix = "]"),
            updatedAtMs = System.currentTimeMillis(),
        )
        dao.upsertEvent(updatedMaster)
        return updatedMaster
    }

    private fun expandRecurringEvents(events: List<CalendarEvent>, rangeStart: LocalDate, rangeEndExclusive: LocalDate): List<CalendarEvent> {
        val expanded = events.flatMap { event ->
            when (event.rrule?.trim()) {
                "FREQ=DAILY" -> event.expandDaily(rangeStart, rangeEndExclusive)
                "FREQ=WEEKLY" -> event.expandWeekly(rangeStart, rangeEndExclusive)
                "FREQ=MONTHLY" -> event.expandMonthly(rangeStart, rangeEndExclusive)
                else -> listOf(event)
            }
        }
        return expanded.sortedWith(compareBy<CalendarEvent> { it.startTimeMs }.thenBy { it.title })
    }

    private fun CalendarEvent.expandDaily(rangeStart: LocalDate, rangeEndExclusive: LocalDate): List<CalendarEvent> {
        val firstDate = startDate()
        val firstVisibleDate = maxOf(firstDate, rangeStart)
        return generateOccurrences(firstVisibleDate, rangeEndExclusive) { it.plusDays(1) }
    }

    private fun CalendarEvent.expandWeekly(rangeStart: LocalDate, rangeEndExclusive: LocalDate): List<CalendarEvent> {
        val firstDate = startDate()
        val daysToRange = ChronoUnit.DAYS.between(firstDate, rangeStart).coerceAtLeast(0)
        val weeksToRange = daysToRange / 7
        val firstVisibleDate = firstDate.plusWeeks(weeksToRange).let { candidate ->
            if (candidate < rangeStart) candidate.plusWeeks(1) else candidate
        }
        return generateOccurrences(firstVisibleDate, rangeEndExclusive) { it.plusWeeks(1) }
    }

    private fun CalendarEvent.expandMonthly(rangeStart: LocalDate, rangeEndExclusive: LocalDate): List<CalendarEvent> {
        val firstDate = startDate()
        val monthsToRange = ChronoUnit.MONTHS.between(firstDate.withDayOfMonth(1), rangeStart.withDayOfMonth(1)).coerceAtLeast(0)
        var cursorMonth = firstDate.withDayOfMonth(1).plusMonths(monthsToRange)
        val occurrences = mutableListOf<CalendarEvent>()
        while (cursorMonth < rangeEndExclusive.withDayOfMonth(1).plusMonths(1)) {
            val date = cursorMonth.dayOrNull(firstDate.dayOfMonth)
            if (date != null && date >= firstDate && date >= rangeStart && date < rangeEndExclusive) {
                occurrenceOn(date)?.let { occurrences += it }
            }
            cursorMonth = cursorMonth.plusMonths(1)
        }
        return occurrences
    }

    private fun CalendarEvent.generateOccurrences(
        firstVisibleDate: LocalDate,
        rangeEndExclusive: LocalDate,
        nextDate: (LocalDate) -> LocalDate,
    ): List<CalendarEvent> {
        val firstDate = startDate()
        var cursor = firstVisibleDate
        val occurrences = mutableListOf<CalendarEvent>()
        while (cursor < rangeEndExclusive) {
            if (cursor >= firstDate) occurrenceOn(cursor)?.let { occurrences += it }
            cursor = nextDate(cursor)
        }
        return occurrences
    }

    private fun CalendarEvent.occurrenceOn(date: LocalDate): CalendarEvent? {
        val zoneId = ZoneId.of(timeZone)
        val startDateTime = java.time.Instant.ofEpochMilli(startTimeMs).atZone(zoneId).toLocalDateTime()
        val durationMs = endTimeMs - startTimeMs
        val occurrenceStart = date.atTime(startDateTime.toLocalTime()).atZone(zoneId).toInstant().toEpochMilli()
        if (occurrenceStart in exceptionStartTimes()) return null
        return copy(
            id = recurrenceOccurrenceId(id, occurrenceStart),
            startTimeMs = occurrenceStart,
            endTimeMs = occurrenceStart + durationMs,
        )
    }

    private fun CalendarEvent.startDate(): LocalDate {
        return java.time.Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.of(timeZone)).toLocalDate()
    }

    private fun LocalDate.dayOrNull(day: Int): LocalDate? {
        val yearMonth = YearMonth.from(this)
        return if (day <= yearMonth.lengthOfMonth()) withDayOfMonth(day) else null
    }

    companion object {
        const val LOCAL_ACCOUNT_ID = "local-primary"
    }
}

private const val RECURRENCE_OCCURRENCE_SEPARATOR = "::occurrence::"

fun recurrenceOccurrenceId(eventId: String, occurrenceStartMs: Long): String {
    return "$eventId$RECURRENCE_OCCURRENCE_SEPARATOR$occurrenceStartMs"
}

fun CalendarEvent.baseEventId(): String {
    return id.substringBefore(RECURRENCE_OCCURRENCE_SEPARATOR)
}

fun CalendarEvent.isRecurrenceOccurrence(): Boolean {
    return id.contains(RECURRENCE_OCCURRENCE_SEPARATOR)
}

fun CalendarEvent.occurrenceStartMs(): Long? {
    return id.substringAfter(RECURRENCE_OCCURRENCE_SEPARATOR, "").toLongOrNull()
}

private fun CalendarEvent.exceptionStartTimes(): Set<Long> {
    return exceptionDates
        .removePrefix("[")
        .removeSuffix("]")
        .split(',')
        .mapNotNull { it.trim().toLongOrNull() }
        .toSet()
}
