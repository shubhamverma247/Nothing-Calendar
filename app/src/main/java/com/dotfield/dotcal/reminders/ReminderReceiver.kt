package com.dotfield.dotcal.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dotfield.dotcal.DotCalApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val app = context.applicationContext as DotCalApplication
                val scheduler = ReminderScheduler(context)
                val repository = app.repository
                val eventId = intent.getStringExtra(EXTRA_EVENT_ID)
                val alarmRequestCode = intent.getIntExtra(EXTRA_ALARM_REQUEST_CODE, Int.MIN_VALUE)
                if (alarmRequestCode == Int.MIN_VALUE) return@runCatching
                when (intent.action) {
                    ACTION_SHOW_REMINDER -> {
                        val reminder = repository.getReminderByRequestCode(alarmRequestCode) ?: return@runCatching
                        val event = repository.getEvent(eventId ?: reminder.eventId) ?: return@runCatching
                        scheduler.showReminderNotification(event, reminder)
                        repository.markReminderDelivered(alarmRequestCode)
                    }
                    ACTION_SNOOZE_REMINDER -> {
                        val snoozeAtMs = System.currentTimeMillis() + SNOOZE_DELAY_MS
                        val targetEventId = eventId ?: repository.getReminderByRequestCode(alarmRequestCode)?.eventId ?: return@runCatching
                        scheduler.scheduleSnooze(targetEventId, alarmRequestCode, snoozeAtMs)
                    }
                }
            }
            pendingResult.finish()
        }
    }

    companion object {
        const val ACTION_SHOW_REMINDER = "com.dotfield.dotcal.action.SHOW_REMINDER"
        const val ACTION_SNOOZE_REMINDER = "com.dotfield.dotcal.action.SNOOZE_REMINDER"
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_ALARM_REQUEST_CODE = "extra_alarm_request_code"
        private const val SNOOZE_DELAY_MS = 10 * 60_000L
    }
}
