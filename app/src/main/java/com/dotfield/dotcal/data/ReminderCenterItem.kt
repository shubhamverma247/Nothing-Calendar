package com.dotfield.dotcal.data

data class ReminderCenterItem(
    val reminderId: Long,
    val eventId: String,
    val eventTitle: String,
    val eventStartTimeMs: Long,
    val isTask: Int,
    val minutesBefore: Int,
    val triggerAtMs: Long,
    val alarmRequestCode: Int,
    val isDelivered: Int,
) {
    fun isOverdue(nowMs: Long): Boolean = triggerAtMs <= nowMs
}
