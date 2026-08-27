package com.dotfield.dotcal.reminders

internal object ReminderNotificationActions {
    val SnoozeMinutes = listOf(5, 15, 30)

    fun snoozeDelayMs(minutes: Int): Long = minutes * 60_000L

    fun snoozeRequestCode(alarmRequestCode: Int, minutes: Int): Int {
        return alarmRequestCode xor (0x5A5A0000 or minutes)
    }

    fun notificationActionTypes(isTask: Boolean): List<ReminderNotificationActionType> {
        return buildList {
            add(ReminderNotificationActionType.Open)
            SnoozeMinutes.forEach { add(ReminderNotificationActionType.Snooze(it)) }
            if (isTask) add(ReminderNotificationActionType.CompleteTask)
        }
    }
}

internal sealed interface ReminderNotificationActionType {
    data object Open : ReminderNotificationActionType
    data class Snooze(val minutes: Int) : ReminderNotificationActionType
    data object CompleteTask : ReminderNotificationActionType
}
