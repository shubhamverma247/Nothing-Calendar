package com.dotfield.dotcal.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.dotfield.dotcal.DotCalApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduler = ReminderScheduler(context)
        val eventId = intent.getStringExtra(EXTRA_EVENT_ID)
        val alarmRequestCode = intent.getIntExtra(EXTRA_ALARM_REQUEST_CODE, Int.MIN_VALUE)
        if (alarmRequestCode == Int.MIN_VALUE) {
            Log.w(TAG, "Reminder broadcast ignored: missing alarm request code")
            return
        }
        val fallbackTitle = intent.getStringExtra(EXTRA_EVENT_TITLE)
        val fallbackMinutes = intent.getIntExtra(EXTRA_MINUTES_BEFORE, Int.MIN_VALUE)
        val showedFromPayload = intent.action == ACTION_SHOW_REMINDER &&
            eventId != null &&
            !fallbackTitle.isNullOrBlank() &&
            fallbackMinutes != Int.MIN_VALUE
        if (showedFromPayload) {
            runCatching {
                scheduler.showReminderNotification(
                    eventId = eventId,
                    eventTitle = fallbackTitle,
                    minutesBefore = fallbackMinutes,
                    alarmRequestCode = alarmRequestCode,
                )
            }.onFailure { throwable ->
                Log.e(TAG, "Reminder payload notification failed for requestCode=$alarmRequestCode", throwable)
            }
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val app = context.applicationContext as DotCalApplication
                val repository = app.repository
                when (intent.action) {
                    ACTION_SHOW_REMINDER -> {
                        if (showedFromPayload) {
                            repository.markReminderDelivered(alarmRequestCode)
                            return@runCatching
                        }
                        val reminder = repository.getReminderByRequestCode(alarmRequestCode)
                        val targetEventId = eventId ?: reminder?.eventId
                        val event = targetEventId?.let { repository.getEvent(it) }
                        if (event != null && reminder != null) {
                            scheduler.showReminderNotification(event, reminder)
                            repository.markReminderDelivered(alarmRequestCode)
                            return@runCatching
                        }
                        if (targetEventId != null && !fallbackTitle.isNullOrBlank() && fallbackMinutes != Int.MIN_VALUE) {
                            Log.w(TAG, "Reminder using alarm payload fallback for requestCode=$alarmRequestCode")
                            scheduler.showReminderNotification(
                                eventId = targetEventId,
                                eventTitle = fallbackTitle,
                                minutesBefore = fallbackMinutes,
                                alarmRequestCode = alarmRequestCode,
                            )
                            if (reminder != null) repository.markReminderDelivered(alarmRequestCode)
                            return@runCatching
                        }
                        Log.w(TAG, "Reminder dropped: missing event/reminder for requestCode=$alarmRequestCode")
                    }
                    ACTION_SNOOZE_REMINDER -> {
                        NotificationManagerCompat.from(context).cancel(alarmRequestCode)
                        val snoozeAtMs = System.currentTimeMillis() + SNOOZE_DELAY_MS
                        val reminder = repository.getReminderByRequestCode(alarmRequestCode)
                        val targetEventId = eventId ?: reminder?.eventId ?: return@runCatching
                        val event = repository.getEvent(targetEventId)
                        val eventTitle = event?.title ?: fallbackTitle ?: "Event reminder"
                        scheduler.scheduleSnooze(targetEventId, eventTitle, alarmRequestCode, snoozeAtMs)
                    }
                }
            }.onFailure { throwable ->
                Log.e(TAG, "Reminder async handling failed for requestCode=$alarmRequestCode", throwable)
            }
            pendingResult.finish()
        }
    }

    companion object {
        private const val TAG = "ReminderReceiver"
        const val ACTION_SHOW_REMINDER = "com.dotfield.dotcal.action.SHOW_REMINDER"
        const val ACTION_SNOOZE_REMINDER = "com.dotfield.dotcal.action.SNOOZE_REMINDER"
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_ALARM_REQUEST_CODE = "extra_alarm_request_code"
        const val EXTRA_EVENT_TITLE = "extra_event_title"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
        private const val SNOOZE_DELAY_MS = 10 * 60_000L
    }
}
