package com.dotfield.dotcal.data.recurrence

import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.provider.providerRdateStartTimes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal data class PlannedReminder(
    val triggerAtMs: Long,
    val occurrenceStartMs: Long,
)

/** Finds the next alarmable occurrence without changing the persisted recurrence model. */
internal fun planNextReminder(
    event: CalendarEvent,
    minutesBefore: Int,
    nowMs: Long,
    providerRdate: String? = event.providerRdate,
): PlannedReminder? {
    val offsetMs = minutesBefore.toLong().coerceAtLeast(0L) * 60_000L
    val nextStartMs = nextOccurrenceStartMs(event, nowMs + offsetMs - 1L, providerRdate) ?: return null
    return PlannedReminder(
        triggerAtMs = nextStartMs - offsetMs,
        occurrenceStartMs = nextStartMs,
    ).takeIf { it.triggerAtMs > nowMs }
}

internal fun nextOccurrenceStartMs(
    event: CalendarEvent,
    afterMs: Long,
    providerRdate: String? = event.providerRdate,
): Long? {
    val zone = runCatching { ZoneId.of(event.timeZone) }.getOrDefault(ZoneId.systemDefault())
    val firstDateTime = Instant.ofEpochMilli(event.startTimeMs).atZone(zone).toLocalDateTime()
    val firstDate = firstDateTime.toLocalDate()
    val exceptionStarts = event.exceptionStartTimesForReminder()
    val candidates = buildList {
        if (event.rrule.isNullOrBlank() && event.startTimeMs > afterMs && event.startTimeMs !in exceptionStarts) {
            add(event.startTimeMs)
        }
        providerRdateStartTimes(providerRdate, event.isAllDay, event.timeZone)
            .filter { it > afterMs && it !in exceptionStarts }
            .forEach(::add)
        val rule = RecurrenceRule.parse(event.rrule) ?: return@buildList
        var block = if (rule.count != null) 0 else {
            val afterDate = Instant.ofEpochMilli(afterMs).atZone(zone).toLocalDate()
            rule.fastForwardBlock(firstDate, minOf(afterDate, firstDate.plusYears(MAX_LOOKAHEAD_YEARS.toLong())))
        }
        var emittedCount = 0
        repeat(MAX_RECURRENCE_BLOCKS) {
            rule.datesForBlock(firstDate, block).forEach { date ->
                if (rule.until != null && date > rule.until) return@buildList
                if (rule.count != null && emittedCount >= rule.count) return@buildList
                emittedCount++
                val startMs = date.atTime(firstDateTime.toLocalTime()).atZone(zone).toInstant().toEpochMilli()
                if (startMs > afterMs && startMs !in exceptionStarts) add(startMs)
            }
            if (rule.count != null && emittedCount >= rule.count) return@buildList
            val anchor = rule.blockAnchorDate(firstDate, block)
            if (rule.until != null && anchor > rule.until) return@buildList
            if (rule.count == null && anchor.isAfter(firstDate.plusYears(MAX_LOOKAHEAD_YEARS.toLong()))) return@buildList
            block++
        }
    }
    return candidates.minOrNull()
}

private const val MAX_RECURRENCE_BLOCKS = 4_000
private const val MAX_LOOKAHEAD_YEARS = 8

/** Kept local to reminder planning so the existing repository expander remains unchanged. */
private fun CalendarEvent.exceptionStartTimesForReminder(): Set<Long> =
    exceptionDates
        .removePrefix("[")
        .removeSuffix("]")
        .split(',')
        .mapNotNull { it.trim().toLongOrNull() }
        .toSet()
