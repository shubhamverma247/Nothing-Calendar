package com.dotfield.dotcal.data.tasks

import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.scheduling.BusyPeriod
import com.dotfield.dotcal.data.scheduling.FreeSlotEngine
import com.dotfield.dotcal.data.scheduling.FreeSlotRequest
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class RepairMyDayChange(
    val taskId: String,
    val title: String,
    val originalStartTimeMs: Long,
    val originalEndTimeMs: Long,
    val originalUpdatedAtMs: Long,
    val proposedStart: LocalDateTime,
    val proposedEnd: LocalDateTime,
)

data class RepairMyDayPreview(
    val date: LocalDate,
    val zoneId: String,
    val planningStart: LocalDateTime,
    val changes: List<RepairMyDayChange>,
    val unplannedTaskIds: Set<String>,
) {
    val isUnchanged: Boolean
        get() = changes.isEmpty() && unplannedTaskIds.isEmpty()

    val canApply: Boolean
        get() = changes.isNotEmpty() && unplannedTaskIds.isEmpty()
}

fun CalendarEvent.isRepairMyDayEligible(date: LocalDate, zoneId: ZoneId): Boolean =
    isTask == 1 &&
        isCompleted == 0 &&
        isAllDay == 0 &&
        endTimeMs > startTimeMs &&
        Instant.ofEpochMilli(startTimeMs).atZone(zoneId).toLocalDate() == date &&
        rrule.isNullOrBlank() &&
        source == "LOCAL" &&
        googleEventId == null &&
        googleCalendarId == null &&
        providerRdate.isNullOrBlank() &&
        providerOriginalGoogleEventId == null &&
        providerOriginalInstanceTimeMs == null

fun repairMyDayCandidates(
    tasks: List<CalendarEvent>,
    date: LocalDate,
    zoneId: ZoneId = ZoneId.systemDefault(),
): List<CalendarEvent> = tasks.filter { it.isRepairMyDayEligible(date, zoneId) }

object RepairMyDayPlanner {
    private val DayStart = LocalTime.of(9, 0)
    private val DayEnd = LocalTime.of(21, 0)

    fun preview(
        tasks: List<CalendarEvent>,
        selectedTaskIds: Set<String>,
        busyPeriods: List<BusyPeriod>,
        now: LocalDateTime,
        zoneId: ZoneId = ZoneId.systemDefault(),
    ): RepairMyDayPreview {
        val date = now.toLocalDate()
        val candidates = repairMyDayCandidates(tasks, date, zoneId)
            .filter { it.id in selectedTaskIds }
            .sortedBy(CalendarEvent::startTimeMs)
        val unplanned = (selectedTaskIds - candidates.mapTo(mutableSetOf(), CalendarEvent::id)).toMutableSet()
        val workingStart = maxOf(DayStart, now.toPlanningMinute().toLocalTime())
        if (!workingStart.isBefore(DayEnd)) {
            return RepairMyDayPreview(date, zoneId.id, now, emptyList(), selectedTaskIds)
        }
        val slots = FreeSlotEngine.compute(
            request = FreeSlotRequest(
                rangeStart = date,
                rangeEnd = date,
                workingStart = workingStart,
                workingEnd = DayEnd,
                minimumSlotMinutes = 1,
            ),
            busyPeriods = busyPeriods,
        ).single().freeSlots.map { slot ->
            MutableSlot(date.atTime(slot.start), date.atTime(slot.end))
        }
        val changes = buildList {
            candidates.forEach { task ->
                val duration = Duration.ofMillis(task.endTimeMs - task.startTimeMs)
                val slot = slots.firstOrNull { !it.end.isBefore(it.cursor.plus(duration)) }
                if (slot == null) {
                    unplanned += task.id
                } else {
                    val proposedStart = slot.cursor
                    val proposedEnd = proposedStart.plus(duration)
                    val proposedStartMs = proposedStart.atZone(zoneId).toInstant().toEpochMilli()
                    val proposedEndMs = proposedEnd.atZone(zoneId).toInstant().toEpochMilli()
                    if (proposedStartMs != task.startTimeMs || proposedEndMs != task.endTimeMs) {
                        add(
                            RepairMyDayChange(
                                taskId = task.id,
                                title = task.title,
                                originalStartTimeMs = task.startTimeMs,
                                originalEndTimeMs = task.endTimeMs,
                                originalUpdatedAtMs = task.updatedAtMs,
                                proposedStart = proposedStart,
                                proposedEnd = proposedEnd,
                            ),
                        )
                    }
                    slot.cursor = proposedEnd
                }
            }
        }
        return RepairMyDayPreview(date, zoneId.id, now, changes, unplanned)
    }

    private fun LocalDateTime.toPlanningMinute(): LocalDateTime {
        val minute = withSecond(0).withNano(0)
        return if (second == 0 && nano == 0) minute else minute.plusMinutes(1)
    }

    private data class MutableSlot(var cursor: LocalDateTime, val end: LocalDateTime)
}
