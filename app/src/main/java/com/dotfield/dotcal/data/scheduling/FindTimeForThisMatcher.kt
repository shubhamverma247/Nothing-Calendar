package com.dotfield.dotcal.data.scheduling

import java.time.Duration
import java.time.LocalDateTime

data class FindTimeSuggestion(
    val slot: FreeSlot,
    val unusedMinutes: Long,
)

object FindTimeForThisMatcher {
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
