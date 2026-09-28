package com.dotfield.dotcal.data.scheduling

import com.dotfield.dotcal.data.CalendarEvent
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

data class FindTimeSuggestion(
    val slot: FreeSlot,
    val unusedMinutes: Long,
)

object FindTimeForThisMatcher {
    private val CommonDurations = listOf(30, 45, 60, 90, 120)

    fun durationMinutes(item: CalendarEvent, fallbackMinutes: Long = 60): Long {
        require(fallbackMinutes > 0) { "Fallback duration must be positive" }
        if (item.isAllDay == 1 || item.endTimeMs <= item.startTimeMs) return fallbackMinutes
        return ((item.endTimeMs - item.startTimeMs) / 60_000L).coerceAtLeast(1L)
    }

    fun durationOptions(item: CalendarEvent): List<Int> {
        val derived = durationMinutes(item).coerceIn(15L, 24L * 60L).toInt()
        return (CommonDurations + derived).distinct().sorted()
    }

    fun initialDateRange(item: CalendarEvent, today: LocalDate): Pair<LocalDate, LocalDate> {
        val zoneId = runCatching { ZoneId.of(item.timeZone) }.getOrDefault(ZoneId.systemDefault())
        val itemDate = if (item.startTimeMs > 0L) {
            Instant.ofEpochMilli(item.startTimeMs).atZone(zoneId).toLocalDate()
        } else {
            null
        }
        val end = itemDate
            ?.takeUnless { it.isBefore(today) }
            ?: today.plusDays(7)
        return today to end
    }

    fun canMove(event: CalendarEvent): Boolean =
        event.isTask == 0 &&
            event.isAllDay == 0 &&
            event.source == "LOCAL" &&
            event.googleEventId == null &&
            event.googleCalendarId == null &&
            event.rrule.isNullOrBlank() &&
            event.providerRdate.isNullOrBlank() &&
            event.providerOriginalGoogleEventId == null &&
            event.providerOriginalInstanceTimeMs == null

    fun find(
        days: List<DayAvailability>,
        durationMinutes: Long,
        now: LocalDateTime,
        limit: Int = 6,
    ): List<FindTimeSuggestion> {
        require(durationMinutes > 0) { "Duration must be positive" }
        require(limit >= 0) { "Limit must not be negative" }

        return days.asSequence()
            .flatMap { it.freeSlots.asSequence() }
            .mapNotNull { slot -> slot.toSuggestion(durationMinutes, now) }
            .sortedWith(compareBy<FindTimeSuggestion> { it.slot.date.atTime(it.slot.start) }
                .thenBy { it.unusedMinutes })
            .take(limit)
            .toList()
    }

    private fun FreeSlot.toSuggestion(
        durationMinutes: Long,
        now: LocalDateTime,
    ): FindTimeSuggestion? {
        val slotStart = date.atTime(start)
        val slotEnd = date.atTime(end)
        val usableStart = maxOf(slotStart, now)
        val scheduledEnd = usableStart.plusMinutes(durationMinutes)
        if (slotEnd.isBefore(scheduledEnd)) return null

        return FindTimeSuggestion(
            slot = copy(start = usableStart.toLocalTime(), end = scheduledEnd.toLocalTime()),
            unusedMinutes = Duration.between(scheduledEnd, slotEnd).toMinutes(),
        )
    }
}
