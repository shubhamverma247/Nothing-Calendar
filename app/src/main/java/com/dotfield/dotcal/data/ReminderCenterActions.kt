package com.dotfield.dotcal.data

interface ReminderCenterStore {
    suspend fun markReminderDelivered(alarmRequestCode: Int)
    suspend fun deleteReminder(reminderId: Long)
    suspend fun rescheduleReminder(reminderId: Long, triggerAtMs: Long)
}

interface ReminderCenterScheduler {
    fun cancelReminder(alarmRequestCode: Int)

    fun scheduleSnooze(
        eventId: String,
        eventTitle: String,
        alarmRequestCode: Int,
        triggerAtMs: Long,
        snoozeMinutes: Int,
        isTask: Boolean,
    )
}

class ReminderCenterActions(
    private val store: ReminderCenterStore,
    private val scheduler: ReminderCenterScheduler,
    private val nowMs: () -> Long = System::currentTimeMillis,
) {
    suspend fun dismiss(item: ReminderCenterItem) {
        scheduler.cancelReminder(item.alarmRequestCode)
        store.markReminderDelivered(item.alarmRequestCode)
    }

    suspend fun cancel(item: ReminderCenterItem) {
        scheduler.cancelReminder(item.alarmRequestCode)
        store.deleteReminder(item.reminderId)
    }

    suspend fun snooze(item: ReminderCenterItem, minutes: Int) {
        val triggerAtMs = nowMs() + minutes * 60_000L
        scheduler.scheduleSnooze(
            eventId = item.eventId,
            eventTitle = item.eventTitle,
            alarmRequestCode = item.alarmRequestCode,
            triggerAtMs = triggerAtMs,
            snoozeMinutes = minutes,
            isTask = item.isTask == 1,
        )
        store.rescheduleReminder(item.reminderId, triggerAtMs)
    }
}
