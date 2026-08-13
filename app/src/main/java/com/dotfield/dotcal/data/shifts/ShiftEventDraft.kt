package com.dotfield.dotcal.data.shifts

import java.time.LocalDate
import java.time.LocalTime

data class ShiftEventDraft(
    val title: String,
    val date: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isAllDay: Boolean,
    val reminderMinutes: Int?,
    val colorHex: String,
)

fun buildShiftEventDraft(type: ShiftType, date: LocalDate): ShiftEventDraft? {
    if (!type.generatesEvent) return null
    if (type.isAllDay) {
        return ShiftEventDraft(
            title = type.name,
            date = date,
            endDate = date,
            startTime = LocalTime.MIDNIGHT,
            endTime = LocalTime.of(23, 59),
            isAllDay = true,
            reminderMinutes = type.reminderMinutes,
            colorHex = type.colorHex,
        )
    }
    val startMinute = type.startMinuteOfDay?.coerceIn(0, 23 * 60 + 59) ?: return null
    val duration = type.durationMinutes?.coerceAtLeast(1) ?: return null
    val startTime = LocalTime.of(startMinute / 60, startMinute % 60)
    val endDateTime = date.atTime(startTime).plusMinutes(duration.toLong())
    return ShiftEventDraft(
        title = type.name,
        date = date,
        endDate = endDateTime.toLocalDate(),
        startTime = startTime,
        endTime = endDateTime.toLocalTime(),
        isAllDay = false,
        reminderMinutes = type.reminderMinutes,
        colorHex = type.colorHex,
    )
}
