package com.dotfield.dotcal.data.tasks

import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.scheduling.BusyPeriod
import com.dotfield.dotcal.data.toPlanningBusyPeriod
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RepairMyDayTest {
    private val date = LocalDate.of(2026, 11, 10)
    private val zoneId = ZoneId.of("UTC")

    @Test
    fun candidatesContainOnlyUnfinishedStandaloneLocalTimedTasksForTheDay() {
        val eligible = task("eligible", LocalTime.of(9, 0), 30)
        val completed = task("completed", LocalTime.of(9, 30), 30, isCompleted = 1)
        val recurring = task("recurring", LocalTime.of(10, 0), 30, rrule = "FREQ=DAILY")
        val provider = task("provider", LocalTime.of(10, 30), 30, source = "GOOGLE", googleEventId = "42")
        val allDay = task("all-day", null, 30)
        val tomorrow = task("tomorrow", LocalTime.of(11, 0), 30, taskDate = date.plusDays(1))
        val event = task("event", LocalTime.of(11, 30), 30, isTask = 0)

        assertEquals(
            listOf(eligible),
            repairMyDayCandidates(
                listOf(eligible, completed, recurring, provider, allDay, tomorrow, event),
                date,
                zoneId,
            ),
        )
    }

    @Test
    fun providerRecurrenceMetadataAlsoProtectsTask() {
        val task = task("provider-occurrence", LocalTime.of(9, 0), 30).apply {
            providerOriginalGoogleEventId = "master"
            providerOriginalInstanceTimeMs = 1L
            providerRdate = "20261110T090000Z"
        }

        assertFalse(task.isRepairMyDayEligible(date, zoneId))
    }

    @Test
    fun previewMovesOnlySelectedTasksPreservesDurationAndAvoidsFixedBusyTime() {
        val first = task("first", LocalTime.of(8, 0), 30)
        val second = task("second", LocalTime.of(8, 30), 60)
        val unselected = task("unselected", LocalTime.of(9, 30), 30)
        val busy = BusyPeriod(
            start = date.atTime(10, 30),
            end = date.atTime(11, 0),
        )

        val preview = RepairMyDayPlanner.preview(
            tasks = listOf(first, second, unselected),
            selectedTaskIds = setOf(first.id, second.id),
            busyPeriods = listOf(busy),
            now = date.atTime(10, 0),
            zoneId = zoneId,
        )

        assertEquals(listOf("first", "second"), preview.changes.map { it.taskId })
        assertEquals(date.atTime(10, 0), preview.changes[0].proposedStart)
        assertEquals(date.atTime(10, 30), preview.changes[0].proposedEnd)
        assertEquals(date.atTime(11, 0), preview.changes[1].proposedStart)
        assertEquals(date.atTime(12, 0), preview.changes[1].proposedEnd)
        assertTrue(preview.canApply)
    }

    @Test
    fun protectedOrUnplaceableSelectionsBlockConfirmation() {
        val provider = task("provider", LocalTime.of(8, 0), 30, googleCalendarId = "provider")
        val tooLong = task("too-long", LocalTime.of(8, 30), 120)

        val preview = RepairMyDayPlanner.preview(
            tasks = listOf(provider, tooLong),
            selectedTaskIds = setOf(provider.id, tooLong.id),
            busyPeriods = emptyList(),
            now = date.atTime(20, 0),
            zoneId = zoneId,
        )

        assertEquals(setOf(provider.id, tooLong.id), preview.unplannedTaskIds)
        assertTrue(preview.changes.isEmpty())
        assertFalse(preview.canApply)
    }

    @Test
    fun taskAlreadyInItsProposedSlotIsNotReportedAsAChange() {
        val task = task("already-planned", LocalTime.of(10, 0), 30)

        val preview = RepairMyDayPlanner.preview(
            tasks = listOf(task),
            selectedTaskIds = setOf(task.id),
            busyPeriods = emptyList(),
            now = date.atTime(10, 0),
            zoneId = zoneId,
        )

        assertTrue(preview.changes.isEmpty())
        assertTrue(preview.unplannedTaskIds.isEmpty())
        assertTrue(preview.isUnchanged)
        assertFalse(preview.canApply)
    }

    @Test
    fun allDayBusyPeriodKeepsCalendarDateAcrossPlanningZones() {
        val allDay = task("all-day-blocker", null, 24 * 60).copy(timeZone = zoneId.id)
        val planningZone = ZoneId.of("America/Los_Angeles")

        val period = allDay.toPlanningBusyPeriod(planningZone, isGhost = false)

        assertEquals(date.atStartOfDay(), period.start)
        assertEquals(date.plusDays(1).atStartOfDay(), period.end)
        assertTrue(period.isAllDay)
    }

    private fun task(
        id: String,
        time: LocalTime?,
        durationMinutes: Long,
        taskDate: LocalDate = date,
        isTask: Int = 1,
        isCompleted: Int = 0,
        rrule: String? = null,
        source: String = "LOCAL",
        googleEventId: String? = null,
        googleCalendarId: String? = null,
    ): CalendarEvent {
        val start = time?.let { taskDate.atTime(it).atZone(zoneId).toInstant().toEpochMilli() }
            ?: taskDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        return CalendarEvent(
            id = id,
            accountId = "local",
            title = id,
            startTimeMs = start,
            endTimeMs = start + durationMinutes * 60_000L,
            timeZone = zoneId.id,
            isAllDay = if (time == null) 1 else 0,
            colorHex = null,
            rrule = rrule,
            source = source,
            googleEventId = googleEventId,
            googleCalendarId = googleCalendarId,
            isTask = isTask,
            isCompleted = isCompleted,
            completedAtMs = null,
            voiceNotePath = null,
            createdAtMs = 0L,
            updatedAtMs = 7L,
        )
    }
}
