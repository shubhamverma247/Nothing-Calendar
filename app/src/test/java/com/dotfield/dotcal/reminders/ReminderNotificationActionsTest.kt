package com.dotfield.dotcal.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ReminderNotificationActionsTest {
    @Test
    fun eventNotificationsUseOneSnoozePickerAction() {
        assertEquals(
            listOf(ReminderNotificationActionType.Snooze),
            ReminderNotificationActions.notificationActionTypes(isTask = false),
        )
    }

    @Test
    fun taskNotificationsIncludeCompleteTaskAction() {
        assertEquals(
            listOf(
                ReminderNotificationActionType.Snooze,
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

    @Test
    fun snoozePickerProvidesUsefulPresets() {
        assertEquals(listOf(5, 10, 15, 30, 60), ReminderNotificationActions.SnoozePickerMinutes)
    }

    @Test
    fun snoozeCannotScheduleInThePastOrAtCurrentTime() {
        assertEquals(false, ReminderNotificationActions.canScheduleAt(1_000L, 1_000L))
        assertEquals(false, ReminderNotificationActions.canScheduleAt(999L, 1_000L))
        assertEquals(true, ReminderNotificationActions.canScheduleAt(1_001L, 1_000L))
    }

    @Test
    fun repeatedSnoozesUseOneStableAlarmRequestCode() {
        val alarmRequestCode = 1234

        assertEquals(
            ReminderNotificationActions.snoozeAlarmRequestCode(alarmRequestCode),
            ReminderNotificationActions.snoozeAlarmRequestCode(alarmRequestCode),
        )
    }
}
