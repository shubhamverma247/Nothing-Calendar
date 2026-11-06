package com.dotfield.dotcal.data.tasks

import com.dotfield.dotcal.data.CalendarEvent
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EveningTaskReviewTest {
    private val zoneId = ZoneId.of("UTC")
    private val today = LocalDate.of(2026, 11, 6)

    @Test
    fun candidatesContainOnlyUnfinishedNonRecurringLocalTasks() {
        val eligible = task("eligible")
        val completed = task("completed", isCompleted = 1)
        val recurring = task("recurring", rrule = "FREQ=DAILY")
        val provider = task("provider", source = "GOOGLE", googleEventId = "provider-id")
        val event = task("event", isTask = 0)

        assertEquals(
            listOf(eligible),
            eveningTaskReviewCandidates(listOf(eligible, completed, recurring, provider, event)),
        )
    }

    @Test
    fun providerCalendarIdAlsoProtectsTask() {
        assertFalse(task("provider", googleCalendarId = "42").isEveningTaskReviewEligible())
    }

    @Test
    fun tomorrowPreservesTimedTaskTimeAndReminder() {
        val task = task("timed", date = today, time = LocalTime.of(18, 45))

        val schedule = task.eveningReviewSchedule(
            action = EveningTaskReviewAction.Tomorrow,
            today = today,
            reminderMinutes = 30,
            zoneId = zoneId,
        )

        assertEquals(today.plusDays(1), schedule?.date)
        assertEquals(LocalTime.of(18, 45), schedule?.time)
        assertEquals(30, schedule?.reminderMinutes)
    }

    @Test
    fun chooseDateKeepsUntimedTaskAllDay() {
        val chosenDate = today.plusDays(5)
        val task = task("all-day", date = today, time = null)

        val schedule = task.eveningReviewSchedule(
            action = EveningTaskReviewAction.ChooseDate(chosenDate),
            today = today,
            reminderMinutes = 10,
            zoneId = zoneId,
        )

        assertEquals(chosenDate, schedule?.date)
        assertNull(schedule?.time)
        assertEquals(10, schedule?.reminderMinutes)
    }

    @Test
    fun unscheduledClearsDateTimeAndReminder() {
        val task = task("scheduled", date = today, time = LocalTime.of(9, 0))

        val schedule = task.eveningReviewSchedule(
            action = EveningTaskReviewAction.Unscheduled,
            today = today,
            reminderMinutes = 15,
            zoneId = zoneId,
        )

        assertNull(schedule?.date)
        assertNull(schedule?.time)
        assertNull(schedule?.reminderMinutes)
    }

    @Test
    fun nonScheduleActionsDoNotCreateScheduleEdits() {
        val task = task("task")

        assertNull(task.eveningReviewSchedule(EveningTaskReviewAction.Complete, today, null, zoneId))
        assertNull(task.eveningReviewSchedule(EveningTaskReviewAction.Delete, today, null, zoneId))
        assertNull(task.eveningReviewSchedule(EveningTaskReviewAction.Skip, today, null, zoneId))
        assertTrue(task.isEveningTaskReviewEligible())
    }

    @Test
    fun progressKeepsProcessedTasksAfterTheyLeaveTheTaskFlow() {
        assertEquals(
            EveningTaskReviewProgress(completed = 1, total = 2),
            eveningTaskReviewProgress(
                tasks = listOf(task("remaining")),
                processedIds = setOf("completed"),
            ),
        )
    }

    @Test
    fun progressDoesNotDoubleCountProcessedTasksStillInTheTaskFlow() {
        assertEquals(
            EveningTaskReviewProgress(completed = 1, total = 1),
            eveningTaskReviewProgress(
                tasks = listOf(task("moved")),
                processedIds = setOf("moved"),
            ),
        )
    }

    @Test
    fun failedMutationRollsBackBeforeReturningFailure() = runBlocking {
        var rolledBackToken: String? = null

        val result = runEveningTaskReviewMutation(
            undoToken = "original",
            mutate = { error("widget refresh failed") },
            rollback = { rolledBackToken = it },
        )

        assertTrue(result.isFailure)
        assertEquals("original", rolledBackToken)
    }

    @Test
    fun rollbackFailureIsAttachedToOriginalMutationFailure() = runBlocking {
        val result = runEveningTaskReviewMutation(
            undoToken = "original",
            mutate = { error("mutation failed") },
            rollback = { error("rollback failed") },
        )

        assertEquals("mutation failed", result.exceptionOrNull()?.message)
        assertEquals("rollback failed", result.exceptionOrNull()?.suppressed?.single()?.message)
    }

    private fun task(
        id: String,
        date: LocalDate? = null,
        time: LocalTime? = null,
        isTask: Int = 1,
        isCompleted: Int = 0,
        rrule: String? = null,
        source: String = "LOCAL",
        googleEventId: String? = null,
        googleCalendarId: String? = null,
    ): CalendarEvent {
        val start = when {
            date == null -> 0L
            time == null -> date.atStartOfDay(zoneId).toInstant().toEpochMilli()
            else -> date.atTime(time).atZone(zoneId).toInstant().toEpochMilli()
        }
        return CalendarEvent(
            id = id,
            accountId = "local",
            title = id,
            startTimeMs = start,
            endTimeMs = if (start == 0L) 0L else start + 30 * 60_000L,
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
            updatedAtMs = 0L,
        )
    }
}
