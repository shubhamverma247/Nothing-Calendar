package com.dotfield.dotcal.data.scheduling

import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class BusyPeriod(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val isAllDay: Boolean = false,
    val isGhost: Boolean = false,
)

data class FreeSlot(
    val date: LocalDate,
    val start: LocalTime,
    val end: LocalTime,
)

data class DayAvailability(
    val date: LocalDate,
    val workingStart: LocalTime,
    val workingEnd: LocalTime,
    val freeSlots: List<FreeSlot>,
) {
    val isFreeAllDay: Boolean
        get() = freeSlots.singleOrNull()?.let { it.start == workingStart && it.end == workingEnd } == true

    val isFullyBusy: Boolean get() = freeSlots.isEmpty()
}

data class FreeSlotRequest(
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
    val workingStart: LocalTime = LocalTime.of(9, 0),
    val workingEnd: LocalTime = LocalTime.of(21, 0),
    val minimumSlotMinutes: Int = 30,
    val blockAllDayEvents: Boolean = true,
    val treatGhostsAsBusy: Boolean = true,
)

object FreeSlotEngine {
    fun compute(
        request: FreeSlotRequest,
        busyPeriods: List<BusyPeriod>,
    ): List<DayAvailability> {
        require(!request.rangeEnd.isBefore(request.rangeStart)) { "Range end must not precede range start" }
        require(request.workingEnd.isAfter(request.workingStart)) { "Working hours must end after they start" }
        require(request.minimumSlotMinutes > 0) { "Minimum slot length must be positive" }

        return generateSequence(request.rangeStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(request.rangeEnd) }
            .map { date -> computeDay(date, request, busyPeriods) }
            .toList()
    }

    private fun computeDay(
        date: LocalDate,
        request: FreeSlotRequest,
        busyPeriods: List<BusyPeriod>,
    ): DayAvailability {
        val windowStart = date.atTime(request.workingStart)
        val windowEnd = date.atTime(request.workingEnd)
        val clippedBusy = busyPeriods.asSequence()
            .filter { it.end.isAfter(it.start) }
            .filter { request.treatGhostsAsBusy || !it.isGhost }
            .filter { request.blockAllDayEvents || !it.isAllDay }
            .mapNotNull { period ->
                val start = maxOf(period.start, windowStart)
                val end = minOf(period.end, windowEnd)
                if (end.isAfter(start)) DateTimeInterval(start, end) else null
            }
            .sortedBy { it.start }
            .toList()

        val mergedBusy = mergeIntervals(clippedBusy)
        val freeSlots = buildList {
            var cursor = windowStart
            mergedBusy.forEach { busy ->
                if (busy.start.isAfter(cursor)) {
                    addIfLongEnough(date, cursor, busy.start, request.minimumSlotMinutes)
                }
                if (busy.end.isAfter(cursor)) cursor = busy.end
            }
            if (windowEnd.isAfter(cursor)) {
                addIfLongEnough(date, cursor, windowEnd, request.minimumSlotMinutes)
            }
        }
        return DayAvailability(date, request.workingStart, request.workingEnd, freeSlots)
    }

    private fun MutableList<FreeSlot>.addIfLongEnough(
        date: LocalDate,
        start: LocalDateTime,
        end: LocalDateTime,
        minimumMinutes: Int,
    ) {
        if (Duration.between(start, end).toMinutes() >= minimumMinutes) {
            add(FreeSlot(date, start.toLocalTime(), end.toLocalTime()))
        }
    }

    private fun mergeIntervals(intervals: List<DateTimeInterval>): List<DateTimeInterval> {
        if (intervals.isEmpty()) return emptyList()
        val merged = mutableListOf(intervals.first())
        intervals.drop(1).forEach { next ->
            val previous = merged.last()
            if (!next.start.isAfter(previous.end)) {
                merged[merged.lastIndex] = previous.copy(end = maxOf(previous.end, next.end))
            } else {
                merged += next
            }
        }
        return merged
    }

    private data class DateTimeInterval(
        val start: LocalDateTime,
        val end: LocalDateTime,
    )
}
