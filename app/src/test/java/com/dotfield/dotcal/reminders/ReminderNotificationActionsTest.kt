package com.dotfield.dotcal.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ReminderNotificationActionsTest {
    @Test
    fun eventNotificationsIncludeOpenAndSnoozeActions() {
        assertEquals(
            listOf(
                ReminderNotificationActionType.Open,
                ReminderNotificationActionType.Snooze(5),
                ReminderNotificationActionType.Snooze(15),
                ReminderNotificationActionType.Snooze(30),
            ),
            ReminderNotificationActions.notificationActionTypes(isTask = false),
        )
    }

    @Test
    fun taskNotificationsIncludeCompleteTaskAction() {
        assertEquals(
            listOf(
                ReminderNotificationActionType.Open,
                ReminderNotificationActionType.Snooze(5),
                ReminderNotificationActionType.Snooze(15),
                ReminderNotificationActionType.Snooze(30),
                ReminderNotificationActionType.CompleteTask,
            ),
            ReminderNotificationActions.notificationActionTypes(isTask = true),
        )
    }

    @Test
    fun snoozeActionsHaveUniqueRequestCodesPerDelay() {
        val alarmRequestCode = 42
        val five = ReminderNotificationActions.snoozeRequestCode(alarmRequestCode, 5)
        val fifteen = ReminderNotificationActions.snoozeRequestCode(alarmRequestCode, 15)
        val thirty = ReminderNotificationActions.snoozeRequestCode(alarmRequestCode, 30)

        assertNotEquals(five, fifteen)
        assertNotEquals(fifteen, thirty)
        assertNotEquals(five, thirty)
    }

    @Test
    fun snoozeDelayUsesSelectedMinutes() {
        assertEquals(5 * 60_000L, ReminderNotificationActions.snoozeDelayMs(5))
        assertEquals(15 * 60_000L, ReminderNotificationActions.snoozeDelayMs(15))
        assertEquals(30 * 60_000L, ReminderNotificationActions.snoozeDelayMs(30))
    }
}
