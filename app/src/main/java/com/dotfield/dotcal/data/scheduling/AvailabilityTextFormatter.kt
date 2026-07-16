package com.dotfield.dotcal.data.scheduling

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object AvailabilityTextFormatter {
    fun format(
        days: List<DayAvailability>,
        use24HourFormat: Boolean,
        locale: Locale = Locale.getDefault(),
    ): String {
        if (days.isEmpty()) return "No availability in this range."
        val visibleDays = days.filterNot { it.isFullyBusy }
        val lines = mutableListOf("My availability (${formatRange(days.first().date, days.last().date, locale)}):")
        visibleDays.forEach { day ->
            lines += "${day.date.format(DateTimeFormatter.ofPattern("EEE", locale))}: ${formatDay(day, use24HourFormat, locale)}"
        }
        if (visibleDays.size < days.size) lines += "Other days are fully booked."
        return lines.joinToString("\n")
    }

    private fun formatDay(
        day: DayAvailability,
        use24HourFormat: Boolean,
        locale: Locale,
    ): String {
        if (day.isFreeAllDay) return "free all day"
        val slots = day.freeSlots
        if (slots.size == 2 &&
            slots.first().start == day.workingStart &&
            slots.last().end == day.workingEnd
        ) {
            return "before ${formatBoundary(slots.first().end, use24HourFormat, locale)}, " +
                "after ${formatBoundary(slots.last().start, use24HourFormat, locale)}"
        }
        return slots.joinToString(", ") { slot ->
            when {
                slot.start == day.workingStart ->
                    "before ${formatBoundary(slot.end, use24HourFormat, locale)}"
                slot.end == day.workingEnd ->
                    "after ${formatBoundary(slot.start, use24HourFormat, locale)}"
                else ->
                    "${formatRangeTime(slot.start, use24HourFormat, locale)}-" +
                        formatRangeTime(slot.end, use24HourFormat, locale)
            }
        }
    }

    private fun formatRange(start: LocalDate, end: LocalDate, locale: Locale): String {
        val startPattern = when {
            start.year != end.year -> "EEE d MMM yyyy"
            start.month != end.month -> "EEE d MMM"
            else -> "EEE d"
        }
        val endPattern = if (start.year != end.year) "EEE d MMM yyyy" else "EEE d MMM"
        return "${start.format(DateTimeFormatter.ofPattern(startPattern, locale))} - " +
            end.format(DateTimeFormatter.ofPattern(endPattern, locale))
    }

    private fun formatBoundary(time: LocalTime, use24HourFormat: Boolean, locale: Locale): String {
        if (time == LocalTime.MAX) return if (use24HourFormat) "24:00" else "12 am"
        val pattern = if (use24HourFormat) "HH:mm" else if (time.minute == 0) "h a" else "h:mm a"
        return time.format(DateTimeFormatter.ofPattern(pattern, locale)).lowercase(locale)
    }

    private fun formatRangeTime(time: LocalTime, use24HourFormat: Boolean, locale: Locale): String {
        if (time == LocalTime.MAX) return if (use24HourFormat) "24:00" else "12:00 am"
        val pattern = if (use24HourFormat) "HH:mm" else "h:mm a"
        return time.format(DateTimeFormatter.ofPattern(pattern, locale)).lowercase(locale)
    }
}
