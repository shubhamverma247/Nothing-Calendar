package com.dotfield.dotcal.data.shifts

import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.DotCalRepository
import java.time.LocalDate
import java.time.ZoneId

const val SHIFT_PLAN_QR_EVENT_LIMIT = 14

fun buildShiftPlanShareEvents(
    pattern: ShiftPattern,
    shiftTypes: Map<String, ShiftType>,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
    nowMs: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<CalendarEvent> {
    if (rangeEnd.isBefore(rangeStart)) return emptyList()
    return expandShiftPattern(pattern, shiftTypes, rangeStart, rangeEnd)
        .mapNotNull { occurrence ->
            val draft = buildShiftEventDraft(occurrence.shiftType, occurrence.date) ?: return@mapNotNull null
            val startMs = if (draft.isAllDay) {
                draft.date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            } else {
                draft.date.atTime(draft.startTime).atZone(zoneId).toInstant().toEpochMilli()
            }
            val endMs = if (draft.isAllDay) {
                draft.endDate.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            } else {
                draft.endDate.atTime(draft.endTime).atZone(zoneId).toInstant().toEpochMilli()
            }
            CalendarEvent(
                id = shiftPlanEventId(pattern.id, occurrence.date, occurrence.shiftType.id),
                accountId = DotCalRepository.LOCAL_ACCOUNT_ID,
                title = draft.title,
                description = "",
                location = "",
                startTimeMs = startMs,
                endTimeMs = endMs,
                timeZone = zoneId.id,
                isAllDay = if (draft.isAllDay) 1 else 0,
                colorHex = draft.colorHex,
                rrule = null,
                source = "LOCAL",
                googleEventId = null,
                googleCalendarId = null,
                completedAtMs = null,
                voiceNotePath = null,
                createdAtMs = nowMs,
                updatedAtMs = nowMs,
            )
        }
}

fun shiftPlanEventId(patternId: String, date: LocalDate, shiftTypeId: String): String =
    "shift-plan-${patternId}-${date}-${shiftTypeId}"
