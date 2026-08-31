package com.dotfield.dotcal.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertArrayEquals
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
    fun alarmClockFallbackCoversTasksAndMissingExactAlarmAccess() {
        assertEquals(true, shouldUseAlarmClock(isTask = true, canScheduleExactAlarms = true))
        assertEquals(true, shouldUseAlarmClock(isTask = false, canScheduleExactAlarms = false))
        assertEquals(false, shouldUseAlarmClock(isTask = false, canScheduleExactAlarms = true))
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

    @Test
    fun repeatAlarmRequestCodeStaysSeparateFromReminderAndSnoozeAlarms() {
        val alarmRequestCode = 1234

        assertNotEquals(alarmRequestCode, ReminderNotificationActions.repeatAlarmRequestCode(alarmRequestCode))
        assertNotEquals(
            ReminderNotificationActions.snoozeAlarmRequestCode(alarmRequestCode),
            ReminderNotificationActions.repeatAlarmRequestCode(alarmRequestCode),
        )
    }

    @Test
    fun multipleRemindersKeepSnoozeRequestCodesSeparate() {
        val first = ReminderNotificationActions.SnoozePickerMinutes.map {
            ReminderNotificationActions.snoozeRequestCode(100, it)
        }
        val second = ReminderNotificationActions.SnoozePickerMinutes.map {
            ReminderNotificationActions.snoozeRequestCode(200, it)
        }

        assertEquals(first.size, first.toSet().size)
        assertEquals(second.size, second.toSet().size)
        assertEquals(emptySet<Int>(), first.toSet().intersect(second.toSet()))
    }

    @Test
    fun reminderDeepLinksTargetEventOrTaskDetail() {
        assertEquals("dotcal://event/event-1", ReminderNotificationActions.reminderDeepLink("event-1", isTask = false))
        assertEquals("dotcal://task/task-1", ReminderNotificationActions.reminderDeepLink("task-1", isTask = true))
    }

    @Test
    fun reminderVibrationPatternUsesTwoAlertPulses() {
        assertArrayEquals(longArrayOf(0, 250, 100, 250), ReminderNotificationActions.vibrationPattern())
    }

    @Test
    fun reminderAndGlyphProgressShareOneNotificationId() {
        val alarmRequestCode = 1234

        assertEquals(alarmRequestCode, ReminderNotificationActions.notificationId(alarmRequestCode))
    }

    @Test
    fun liveProgressCancellationCoversCurrentAndLegacyRequestCodes() {
        val alarmRequestCode = 1234

        assertEquals(
            listOf(alarmRequestCode, alarmRequestCode xor 0x6D6D6D6D),
            ReminderNotificationActions.liveProgressRequestCodes(alarmRequestCode),
        )
    }

    @Test
    fun progressPercentIsClampedToTargetWindow() {
        assertEquals(0, ReminderNotificationActions.progressPercent(1_000L, 2_000L, 500L))
        assertEquals(50, ReminderNotificationActions.progressPercent(1_000L, 2_000L, 1_500L))
        assertEquals(100, ReminderNotificationActions.progressPercent(1_000L, 2_000L, 2_500L))
        assertEquals(100, ReminderNotificationActions.progressPercent(2_000L, 2_000L, 2_000L))
    }
}
