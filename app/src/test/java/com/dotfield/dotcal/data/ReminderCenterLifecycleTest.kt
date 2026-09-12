package com.dotfield.dotcal.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderCenterLifecycleTest {
    private val item = ReminderCenterItem(
        reminderId = 7L,
        eventId = "event-7",
        eventTitle = "Focus",
        eventStartTimeMs = 10_000L,
        isTask = 0,
        minutesBefore = 15,
        triggerAtMs = 1_000L,
        alarmRequestCode = 42,
        isDelivered = 0,
    )

    @Test
    fun dismissCancelsAlarmAndMarksReminderDelivered() = kotlinx.coroutines.runBlocking {
        val store = FakeReminderCenterStore()
        val scheduler = FakeReminderCenterScheduler()

        ReminderCenterActions(store, scheduler, nowMs = { 5_000L }).dismiss(item)

        assertEquals(listOf(42), scheduler.cancelledAlarmCodes)
        assertEquals(listOf(42), store.deliveredAlarmCodes)
    }

    @Test
    fun cancelCancelsAlarmAndDeletesReminder() = kotlinx.coroutines.runBlocking {
        val store = FakeReminderCenterStore()
        val scheduler = FakeReminderCenterScheduler()

        ReminderCenterActions(store, scheduler, nowMs = { 5_000L }).cancel(item)

        assertEquals(listOf(42), scheduler.cancelledAlarmCodes)
        assertEquals(listOf(7L), store.deletedReminderIds)
    }

    @Test
    fun snoozeSchedulesAlarmAndUpdatesTriggerTime() = kotlinx.coroutines.runBlocking {
        val store = FakeReminderCenterStore()
        val scheduler = FakeReminderCenterScheduler()

        ReminderCenterActions(store, scheduler, nowMs = { 5_000L }).snooze(item, 15)

        val expectedTriggerAtMs = 905_000L
        assertEquals(
            ScheduledSnooze(
                eventId = "event-7",
                eventTitle = "Focus",
                alarmRequestCode = 42,
                triggerAtMs = expectedTriggerAtMs,
                snoozeMinutes = 15,
                isTask = false,
            ),
            scheduler.snoozes.single(),
        )
        assertEquals(mapOf(7L to expectedTriggerAtMs), store.rescheduledReminderTimes)
    }

    @Test
    fun snoozePreservesTaskReminderType() = kotlinx.coroutines.runBlocking {
        val task = item.copy(isTask = 1)
        val store = FakeReminderCenterStore()
        val scheduler = FakeReminderCenterScheduler()

        ReminderCenterActions(store, scheduler, nowMs = { 5_000L }).snooze(task, 5)

        assertTrue(scheduler.snoozes.single().isTask)
    }

    private class FakeReminderCenterStore : ReminderCenterStore {
        val deliveredAlarmCodes = mutableListOf<Int>()
        val deletedReminderIds = mutableListOf<Long>()
        val rescheduledReminderTimes = mutableMapOf<Long, Long>()

        override suspend fun markReminderDelivered(alarmRequestCode: Int) {
            deliveredAlarmCodes += alarmRequestCode
        }

        override suspend fun deleteReminder(reminderId: Long) {
            deletedReminderIds += reminderId
        }

        override suspend fun rescheduleReminder(reminderId: Long, triggerAtMs: Long) {
            rescheduledReminderTimes[reminderId] = triggerAtMs
        }
    }

    private class FakeReminderCenterScheduler : ReminderCenterScheduler {
        val cancelledAlarmCodes = mutableListOf<Int>()
        val snoozes = mutableListOf<ScheduledSnooze>()

        override fun cancelReminder(alarmRequestCode: Int) {
            cancelledAlarmCodes += alarmRequestCode
        }

        override fun scheduleSnooze(
            eventId: String,
            eventTitle: String,
            alarmRequestCode: Int,
            triggerAtMs: Long,
            snoozeMinutes: Int,
            isTask: Boolean,
        ) {
            snoozes += ScheduledSnooze(eventId, eventTitle, alarmRequestCode, triggerAtMs, snoozeMinutes, isTask)
        }
    }

    private data class ScheduledSnooze(
        val eventId: String,
        val eventTitle: String,
        val alarmRequestCode: Int,
        val triggerAtMs: Long,
        val snoozeMinutes: Int,
        val isTask: Boolean,
    )
}
