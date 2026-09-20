package com.dotfield.dotcal.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CalendarDaoReminderCenterTest {
    private lateinit var database: DotCalDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DotCalDatabase::class.java,
        ).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun reminderCenterReturnsOnlyUndeliveredJoinedReminders() = runBlocking {
        val dao = database.calendarDao()
        dao.insertAccountIfAbsent(account)
        dao.upsertEvents(listOf(pendingEvent, deliveredEvent))
        dao.insertReminders(
            listOf(
                EventReminder(
                    id = 1L,
                    eventId = pendingEvent.id,
                    minutesBefore = 15,
                    triggerAtMs = 2_000L,
                    alarmRequestCode = 11,
                    isDelivered = 0,
                ),
                EventReminder(
                    id = 2L,
                    eventId = deliveredEvent.id,
                    minutesBefore = 30,
                    triggerAtMs = 1_000L,
                    alarmRequestCode = 22,
                    isDelivered = 1,
                ),
            ),
        )

        val items = dao.observeReminderCenterItems().first()

        assertEquals(1, items.size)
        assertEquals(pendingEvent.id, items.single().eventId)
        assertEquals("Pending event", items.single().eventTitle)
        assertEquals(0, items.single().isDelivered)
        assertTrue(items.single().triggerAtMs > 0L)
    }

    private val account = CalendarAccount(
        id = "local",
        accountName = "Local",
        displayName = "Local",
        accountType = "LOCAL",
        color = "#FF3B30",
        isVisible = 1,
        isPrimary = 1,
        sortOrder = 0,
    )

    private val pendingEvent = event("pending", "Pending event")
    private val deliveredEvent = event("delivered", "Delivered event")

    private fun event(id: String, title: String) = CalendarEvent(
        id = id,
        accountId = account.id,
        title = title,
        startTimeMs = TimeUnit.HOURS.toMillis(1),
        endTimeMs = TimeUnit.HOURS.toMillis(2),
        timeZone = "UTC",
        isAllDay = 0,
        colorHex = null,
        rrule = null,
        source = "LOCAL",
        googleEventId = null,
        googleCalendarId = null,
        completedAtMs = null,
        createdAtMs = 0L,
        updatedAtMs = 0L,
        voiceNotePath = null,
    )
}
