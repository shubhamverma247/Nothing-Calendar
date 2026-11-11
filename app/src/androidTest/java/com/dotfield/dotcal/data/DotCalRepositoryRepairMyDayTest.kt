package com.dotfield.dotcal.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dotfield.dotcal.data.provider.ProviderAttendee
import com.dotfield.dotcal.data.provider.ProviderMeetingMetadata
import com.dotfield.dotcal.data.provider.encodeProviderMeetingMetadata
import com.dotfield.dotcal.data.sidestore.EventSideStoreNamespaces
import com.dotfield.dotcal.data.sidestore.SharedSideStore
import com.dotfield.dotcal.reminders.EventReminderScheduler
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DotCalRepositoryRepairMyDayTest {
    private lateinit var database: DotCalDatabase
    private lateinit var repository: DotCalRepository
    private lateinit var dao: CalendarDao
    private lateinit var reminderScheduler: FakeEventReminderScheduler
    private val zoneId = ZoneId.systemDefault()
    private val date = LocalDate.now()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DotCalDatabase::class.java).build()
        dao = database.calendarDao()
        reminderScheduler = FakeEventReminderScheduler()
        repository = DotCalRepository(dao, context, reminderScheduler)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun previewDoesNotWriteAndConfirmedApplyCanBeUndone() = runBlocking {
        val task = task("repair", LocalTime.of(8, 0), 30)
        insert(task)
        val reminder = reminder(task.id, task.startTimeMs - 15 * 60_000L)
        dao.insertReminders(listOf(reminder))

        val preview = repository.previewRepairMyDay(
            selectedTaskIds = setOf(task.id),
            now = date.atTime(10, 0),
            zoneId = zoneId,
        )

        assertEquals(task.startTimeMs, dao.getEvent(task.id)?.startTimeMs)
        val undoToken = repository.applyRepairMyDay(preview).getOrThrow()
        val moved = requireNotNull(dao.getEvent(task.id))
        assertEquals(date.atTime(10, 0), moved.localStart())
        assertEquals(date.atTime(10, 30), moved.localEnd())
        assertEquals(reminder.triggerAtMs + 2 * 60 * 60_000L, dao.getRemindersForEvent(task.id).single().triggerAtMs)

        reminderScheduler.cancelledRequestCodes.clear()
        assertTrue(repository.undoRepairMyDay(undoToken))
        assertEquals(task.startTimeMs, dao.getEvent(task.id)?.startTimeMs)
        assertEquals(reminder.triggerAtMs, dao.getRemindersForEvent(task.id).single().triggerAtMs)
        assertTrue(reminder.alarmRequestCode in reminderScheduler.cancelledRequestCodes)
    }

    @Test
    fun stalePreviewIsRejectedBeforeAnyWrite() = runBlocking {
        val task = task("stale", LocalTime.of(8, 0), 30)
        insert(task)
        val preview = repository.previewRepairMyDay(setOf(task.id), date.atTime(10, 0), zoneId)
        dao.upsertEvent(task.copy(title = "changed", updatedAtMs = task.updatedAtMs + 1))

        val result = repository.applyRepairMyDay(preview)

        assertTrue(result.isFailure)
        assertEquals(task.startTimeMs, dao.getEvent(task.id)?.startTimeMs)
    }

    @Test
    fun atomicRepairWriteRejectsTaskCompletedAfterValidation() = runBlocking {
        val task = task("completed-during-apply", LocalTime.of(8, 0), 30)
        insert(task)
        val moved = task.copy(
            startTimeMs = task.startTimeMs + 2 * 60 * 60_000L,
            endTimeMs = task.endTimeMs + 2 * 60 * 60_000L,
            updatedAtMs = task.updatedAtMs + 1,
        )
        dao.updateTaskCompletion(task.id, 1, task.startTimeMs, task.updatedAtMs + 2)

        val applied = dao.replaceEventsAndRemindersIfCurrent(
            expectedEvents = listOf(task),
            expectedReminders = emptyList(),
            replacementEvents = listOf(moved),
            replacementReminders = emptyList(),
        )

        assertFalse(applied)
        assertEquals(1, dao.getEvent(task.id)?.isCompleted)
    }

    @Test
    fun undoDoesNotOverwriteTaskCompletedAfterRepair() = runBlocking {
        val task = task("completed-before-undo", LocalTime.of(8, 0), 30)
        insert(task)
        val preview = repository.previewRepairMyDay(setOf(task.id), date.atTime(10, 0), zoneId)
        val undoToken = repository.applyRepairMyDay(preview).getOrThrow()
        val moved = requireNotNull(dao.getEvent(task.id))
        repository.setTaskCompleted(moved, completed = true)

        val undone = repository.undoRepairMyDay(undoToken)

        assertFalse(undone)
        val current = requireNotNull(dao.getEvent(task.id))
        assertEquals(1, current.isCompleted)
        assertEquals(moved.startTimeMs, current.startTimeMs)
    }

    @Test
    fun sharedLocalTaskIsExcludedAndCannotBePreviewed() = runBlocking {
        val task = task("shared", LocalTime.of(8, 0), 30)
        insert(task)
        SharedSideStore(ApplicationProvider.getApplicationContext()).write(
            EventSideStoreNamespaces.ProviderMeetingMetadata,
            task.id,
            requireNotNull(
                encodeProviderMeetingMetadata(
                    ProviderMeetingMetadata(attendees = listOf(ProviderAttendee(email = "guest@example.com"))),
                ),
            ),
        )

        assertTrue(repository.listRepairMyDayCandidates(listOf(task), date, zoneId).isEmpty())
        val preview = repository.previewRepairMyDay(setOf(task.id), date.atTime(10, 0), zoneId)
        assertTrue(preview.changes.isEmpty())
        assertEquals(setOf(task.id), preview.unplannedTaskIds)
    }

    @Test
    fun previewNormalizesFixedEventsIntoThePlannerZone() = runBlocking {
        val plannerZone = ZoneId.of("UTC")
        val task = task("repair-zone", LocalTime.of(8, 0), 60)
        val fixedStart = date.atTime(10, 30).atZone(plannerZone).toInstant().toEpochMilli()
        insert(task)
        insert(
            task("fixed-zone", LocalTime.of(10, 30), 30).copy(
                startTimeMs = fixedStart,
                endTimeMs = fixedStart + 30 * 60_000L,
                timeZone = "Asia/Kolkata",
                isTask = 0,
            ),
        )

        val preview = repository.previewRepairMyDay(setOf(task.id), date.atTime(10, 0), plannerZone)

        assertEquals(date.atTime(11, 0), preview.changes.single().proposedStart)
    }

    @Test
    fun privateFixedEventStillBlocksARepairSlot() = runBlocking {
        val task = task("repair-private", LocalTime.of(8, 0), 60)
        val fixed = task("fixed-private", LocalTime.of(10, 30), 30).copy(isTask = 0)
        insert(task)
        insert(fixed)
        repository.moveToPrivateVault(fixed)

        val preview = repository.previewRepairMyDay(setOf(task.id), date.atTime(10, 0), zoneId)

        assertEquals(date.atTime(11, 0), preview.changes.single().proposedStart)
    }

    private suspend fun insert(event: CalendarEvent) {
        dao.insertAccountIfAbsent(account)
        dao.upsertEvent(event)
    }

    private fun task(id: String, time: LocalTime, durationMinutes: Long): CalendarEvent {
        val start = date.atTime(time).atZone(zoneId).toInstant().toEpochMilli()
        return CalendarEvent(
            id = id,
            accountId = account.id,
            title = id,
            startTimeMs = start,
            endTimeMs = start + durationMinutes * 60_000L,
            timeZone = zoneId.id,
            isAllDay = 0,
            colorHex = null,
            rrule = null,
            source = "LOCAL",
            googleEventId = null,
            googleCalendarId = null,
            isTask = 1,
            isCompleted = 0,
            completedAtMs = null,
            voiceNotePath = null,
            createdAtMs = 1L,
            updatedAtMs = 1L,
        )
    }

    private fun reminder(eventId: String, triggerAtMs: Long) = EventReminder(
        eventId = eventId,
        minutesBefore = 15,
        triggerAtMs = triggerAtMs,
        alarmRequestCode = eventId.hashCode(),
    )

    private fun CalendarEvent.localStart(): LocalDateTime =
        java.time.Instant.ofEpochMilli(startTimeMs).atZone(zoneId).toLocalDateTime()

    private fun CalendarEvent.localEnd(): LocalDateTime =
        java.time.Instant.ofEpochMilli(endTimeMs).atZone(zoneId).toLocalDateTime()

    private val account = CalendarAccount(
        id = DotCalRepository.LOCAL_ACCOUNT_ID,
        accountName = "Local",
        displayName = "Local",
        accountType = "LOCAL",
        color = "#FF3B30",
        isVisible = 1,
        isPrimary = 1,
        sortOrder = 0,
    )

    private class FakeEventReminderScheduler : EventReminderScheduler {
        val cancelledRequestCodes = mutableListOf<Int>()

        override fun scheduleReminder(
            reminder: EventReminder,
            event: CalendarEvent,
            occurrenceStartTimeMs: Long,
        ) = Unit

        override fun cancelReminder(alarmRequestCode: Int) {
            cancelledRequestCodes += alarmRequestCode
        }

        override fun scheduleSnooze(
            eventId: String,
            eventTitle: String,
            alarmRequestCode: Int,
            triggerAtMs: Long,
            snoozeMinutes: Int,
            isTask: Boolean,
        ) = Unit
    }
}
