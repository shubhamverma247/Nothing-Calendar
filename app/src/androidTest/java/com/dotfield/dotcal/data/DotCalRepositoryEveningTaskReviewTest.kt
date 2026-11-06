package com.dotfield.dotcal.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dotfield.dotcal.data.tasks.EveningTaskReviewAction
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DotCalRepositoryEveningTaskReviewTest {
    private lateinit var database: DotCalDatabase
    private lateinit var repository: DotCalRepository
    private lateinit var dao: CalendarDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, DotCalDatabase::class.java).build()
        dao = database.calendarDao()
        repository = DotCalRepository(dao, context)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun completeThenUndoRestoresOpenTask() = runBlocking {
        val task = task("complete")
        insert(task)

        val token = repository.applyEveningTaskReviewAction(task, EveningTaskReviewAction.Complete).getOrThrow()
        assertEquals(1, dao.getEvent(task.id)?.isCompleted)

        repository.undoBulkEdit(requireNotNull(token))
        assertEquals(0, dao.getEvent(task.id)?.isCompleted)
    }

    @Test
    fun deleteThenUndoRestoresTaskAndReminder() = runBlocking {
        val task = task("delete")
        insert(task)
        dao.insertReminders(listOf(reminder(task.id)))

        val token = repository.applyEveningTaskReviewAction(task, EveningTaskReviewAction.Delete).getOrThrow()
        assertNull(dao.getEvent(task.id))

        repository.undoBulkEdit(requireNotNull(token))
        assertNotNull(dao.getEvent(task.id))
        assertEquals(15, dao.getRemindersForEvent(task.id).single().minutesBefore)
    }

    @Test
    fun tomorrowThenUndoRestoresOriginalSchedule() = runBlocking {
        val task = task("tomorrow")
        insert(task)

        val token = repository.applyEveningTaskReviewAction(task, EveningTaskReviewAction.Tomorrow).getOrThrow()
        val movedStart = requireNotNull(dao.getEvent(task.id)).startTimeMs
        assertEquals(LocalDate.now().plusDays(1), dateOf(movedStart))

        repository.undoBulkEdit(requireNotNull(token))
        assertEquals(task.startTimeMs, dao.getEvent(task.id)?.startTimeMs)
    }

    private suspend fun insert(task: CalendarEvent) {
        dao.insertAccountIfAbsent(account)
        dao.upsertEvent(task)
    }

    private fun task(id: String): CalendarEvent {
        val start = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return CalendarEvent(
            id = id,
            accountId = account.id,
            title = id,
            startTimeMs = start,
            endTimeMs = start + 86_400_000L,
            timeZone = ZoneId.systemDefault().id,
            isAllDay = 1,
            colorHex = null,
            rrule = null,
            source = "LOCAL",
            googleEventId = null,
            googleCalendarId = null,
            isTask = 1,
            isCompleted = 0,
            completedAtMs = null,
            voiceNotePath = null,
            createdAtMs = 0L,
            updatedAtMs = 0L,
        )
    }

    private fun reminder(eventId: String) = EventReminder(
        eventId = eventId,
        minutesBefore = 15,
        triggerAtMs = 1L,
        alarmRequestCode = eventId.hashCode(),
    )

    private fun dateOf(timeMs: Long): LocalDate =
        java.time.Instant.ofEpochMilli(timeMs).atZone(ZoneId.systemDefault()).toLocalDate()

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
}
