package com.dotfield.dotcal.data.recurrence

import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.baseEventId
import com.dotfield.dotcal.data.provider.providerRdateStartTimes
import com.dotfield.dotcal.data.recurrenceOccurrenceId
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal fun expandRecurringEvents(
    events: List<CalendarEvent>,
    rangeStart: LocalDate,
    rangeEndExclusive: LocalDate,
    providerRdates: Map<String, String> = emptyMap(),
): List<CalendarEvent> {
    val rangeStartMs = rangeStart.atStartMs()
    val rangeEndMs = rangeEndExclusive.atStartMs()
    return events.flatMap { event ->
        val rule = RecurrenceRule.parse(event.rrule)
        val ruleEvents = if (rule == null) listOf(event) else event.expandRule(rule, rangeStart, rangeEndExclusive)
        val rdateEvents = event.expandProviderRdates(providerRdates[event.baseEventId()], rangeStartMs, rangeEndMs)
        (ruleEvents + rdateEvents).distinctBy { it.id }
    }.sortedWith(compareBy<CalendarEvent> { it.startTimeMs }.thenBy { it.title })
}

private fun CalendarEvent.expandRule(
    rule: RecurrenceRule,
    rangeStart: LocalDate,
    rangeEndExclusive: LocalDate,
): List<CalendarEvent> {
    val firstDate = recurrenceStartDate()
    val anchorCount = rule.count != null
    var block = if (anchorCount) 0 else rule.fastForwardBlock(firstDate, rangeStart)
    val events = mutableListOf<CalendarEvent>()
    var emittedCount = 0
    var iterations = 0
    while (iterations < MAX_RECURRENCE_BLOCKS) {
        iterations++
        for (date in rule.datesForBlock(firstDate, block)) {
            if (rule.until != null && date > rule.until) return events
            if (anchorCount && emittedCount >= rule.count!!) return events
            emittedCount++
            if (date >= rangeStart && date < rangeEndExclusive) {
                occurrenceOn(date)?.let { events += it }
            }
        }
        if (anchorCount && emittedCount >= rule.count!!) break
        val anchor = rule.blockAnchorDate(firstDate, block)
        if (rule.until != null && anchor > rule.until) break
        if (!anchorCount && anchor >= rangeEndExclusive) break
        block++
    }
    return events
}

private fun CalendarEvent.occurrenceOn(date: LocalDate): CalendarEvent? {
    val zoneId = safeZoneId(timeZone)
    val startTime = Instant.ofEpochMilli(startTimeMs).atZone(zoneId).toLocalTime()
    val occurrenceStart = date.atTime(startTime).atZone(zoneId).toInstant().toEpochMilli()
    if (occurrenceStart in recurrenceExceptionStartTimes()) return null
    return copy(
        id = recurrenceOccurrenceId(id, occurrenceStart),
        startTimeMs = occurrenceStart,
        endTimeMs = occurrenceStart + (endTimeMs - startTimeMs),
    )
}

private fun CalendarEvent.expandProviderRdates(
    rdate: String?,
    rangeStartMs: Long,
    rangeEndMs: Long,
): List<CalendarEvent> {
    if (rdate.isNullOrBlank()) return emptyList()
    val durationMs = endTimeMs - startTimeMs
    val exceptions = recurrenceExceptionStartTimes()
    return providerRdateStartTimes(rdate, isAllDay, timeZone)
        .filterNot { it in exceptions }
        .map { occurrenceStart ->
            copy(
                id = recurrenceOccurrenceId(id, occurrenceStart),
                startTimeMs = occurrenceStart,
                endTimeMs = occurrenceStart + durationMs,
            )
        }
        .filter { occurrence -> occurrence.startTimeMs < rangeEndMs && occurrence.endTimeMs >= rangeStartMs }
}

private fun CalendarEvent.recurrenceStartDate(): LocalDate =
    Instant.ofEpochMilli(startTimeMs).atZone(safeZoneId(timeZone)).toLocalDate()

private fun CalendarEvent.recurrenceExceptionStartTimes(): Set<Long> =
    exceptionDates.removePrefix("[").removeSuffix("]").split(',').mapNotNull { it.trim().toLongOrNull() }.toSet()

private fun LocalDate.atStartMs(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun safeZoneId(id: String): ZoneId = runCatching { ZoneId.of(id) }.getOrDefault(ZoneId.systemDefault())

private const val MAX_RECURRENCE_BLOCKS = 10_000
