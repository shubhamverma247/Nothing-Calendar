package com.dotfield.dotcal.reminders

internal object ReminderNotificationActions {
    val SnoozeMinutes = listOf(5, 15, 30)
    val SnoozePickerMinutes = listOf(5, 10, 15, 30, 60)

    fun snoozeDelayMs(minutes: Int): Long = minutes * 60_000L

    fun canScheduleAt(triggerAtMs: Long, nowMs: Long): Boolean = triggerAtMs > nowMs

    fun snoozeRequestCode(alarmRequestCode: Int, minutes: Int): Int {
        return alarmRequestCode xor (0x5A5A0000 or minutes)
    }

    fun notificationActionTypes(isTask: Boolean): List<ReminderNotificationActionType> {
        return if (isTask) {
            listOf(
                ReminderNotificationActionType.Snooze,
                ReminderNotificationActionType.CompleteTask,
            )
        } else {
            listOf(ReminderNotificationActionType.Snooze)
        }
    }
}

internal sealed interface ReminderNotificationActionType {
    data object Snooze : ReminderNotificationActionType
    data object CompleteTask : ReminderNotificationActionType
}
