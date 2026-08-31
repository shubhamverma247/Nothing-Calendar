package com.dotfield.dotcal.reminders

internal object ReminderNotificationActions {
    const val SILENT_SOUND_VALUE = "__silent__"
    val SnoozeMinutes = listOf(5, 15, 30)
    val SnoozePickerMinutes = listOf(5, 10, 15, 30, 60)

    fun snoozeDelayMs(minutes: Int): Long = minutes * 60_000L

    fun snoozeAlarmRequestCode(alarmRequestCode: Int): Int = alarmRequestCode xor 0x6B6B6B6B

    fun repeatAlarmRequestCode(alarmRequestCode: Int): Int = alarmRequestCode xor 0x3C3C3C3C

    fun canScheduleAt(triggerAtMs: Long, nowMs: Long): Boolean = triggerAtMs > nowMs

    fun reminderDeepLink(eventId: String, isTask: Boolean): String =
        "dotcal://${if (isTask) "task" else "event"}/$eventId"

    fun vibrationPattern(): LongArray = longArrayOf(0, 250, 100, 250)

    fun notificationId(alarmRequestCode: Int): Int = alarmRequestCode

    fun liveProgressRequestCodes(alarmRequestCode: Int): List<Int> {
        return listOf(
            alarmRequestCode,
            alarmRequestCode xor 0x6D6D6D6D,
        ).distinct()
    }

    fun progressPercent(originMs: Long, targetMs: Long, nowMs: Long): Int {
        if (targetMs <= originMs) return 100
        val elapsed = (nowMs - originMs).coerceAtLeast(0L)
        return ((elapsed * 100L) / (targetMs - originMs)).toInt().coerceIn(0, 100)
    }

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
