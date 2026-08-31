package com.dotfield.dotcal.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.dotfield.dotcal.DotCalApplication
import com.dotfield.dotcal.MainActivity
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.baseEventId
import com.dotfield.dotcal.data.recurrenceOccurrenceId
import com.dotfield.dotcal.glyph.DotCalGlyphBridge
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
        val fallbackIsTask = intent.getBooleanExtra(EXTRA_IS_TASK, false)
        val snoozedUntilMs = intent.getLongExtra(EXTRA_SNOOZED_UNTIL_MS, 0L)
        val eventStartTimeMs = intent.getLongExtra(EXTRA_EVENT_START_TIME_MS, 0L)
        val snoozeMinutes = intent.getIntExtra(EXTRA_SNOOZE_MINUTES, DEFAULT_SNOOZE_MINUTES)
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
                    isTask = fallbackIsTask,
                    eventStartTimeMs = eventStartTimeMs,
                    snoozedUntilMs = snoozedUntilMs,
                )
            }.onFailure { throwable ->
                Log.e(TAG, "Reminder payload notification failed for requestCode=$alarmRequestCode", throwable)
            }
        }

        if (intent.action == ACTION_OPEN_REMINDER) {
            val targetEventId = eventId
            if (targetEventId.isNullOrBlank()) {
                Log.w(TAG, "Open reminder ignored: missing event for requestCode=$alarmRequestCode")
                return
            }
            context.startActivity(Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse(ReminderNotificationActions.reminderDeepLink(targetEventId, fallbackIsTask))
                // Reusing a singleTop activity can leave the already-composed calendar route
                // visible on some Nothing OS builds even after onNewIntent updates the URI.
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            })
        }

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val app = context.applicationContext as DotCalApplication
                val repository = app.repository
                when (intent.action) {
                    ACTION_UPDATE_LIVE_PROGRESS -> scheduler.updateLiveProgress(intent)
                    ACTION_SHOW_REMINDER -> {
                        if (showedFromPayload) {
                            val reminder = repository.getReminderByRequestCode(alarmRequestCode)
                            val event = eventId?.let { repository.getEvent(it) }
                            eventId?.let {
                                DotCalGlyphBridge.reminderExpired(
                                    context,
                                    reminderGlyphEventId(event, it, eventStartTimeMs),
                                )
                            }
                            val rescheduled = if (reminder != null && event != null) {
                                repository.rescheduleRecurringReminder(reminder, event, eventStartTimeMs)
                            } else {
                                false
                            }
                            if (!rescheduled) repository.markReminderDelivered(alarmRequestCode)
                            return@runCatching
                        }
                        val reminder = repository.getReminderByRequestCode(alarmRequestCode)
                        val targetEventId = eventId ?: reminder?.eventId
                        val event = targetEventId?.let { repository.getEvent(it) }
                        if (event != null && reminder != null) {
                            DotCalGlyphBridge.reminderExpired(
                                context,
                                reminderGlyphEventId(
                                    event,
                                    targetEventId,
                                    reminder.triggerAtMs + reminder.minutesBefore * 60_000L,
                                ),
                            )
                            scheduler.showReminderNotification(event, reminder)
                            val rescheduled = repository.rescheduleRecurringReminder(reminder, event, reminder.triggerAtMs + reminder.minutesBefore * 60_000L)
                            if (!rescheduled) repository.markReminderDelivered(alarmRequestCode)
                            return@runCatching
                        }
                        if (targetEventId != null && !fallbackTitle.isNullOrBlank() && fallbackMinutes != Int.MIN_VALUE) {
                            Log.w(TAG, "Reminder using alarm payload fallback for requestCode=$alarmRequestCode")
                            DotCalGlyphBridge.reminderExpired(
                                context,
                                reminderGlyphEventId(event, targetEventId, eventStartTimeMs),
                            )
                            scheduler.showReminderNotification(
                                eventId = targetEventId,
                                eventTitle = fallbackTitle,
                                minutesBefore = fallbackMinutes,
                                alarmRequestCode = alarmRequestCode,
                                isTask = fallbackIsTask,
                                eventStartTimeMs = event?.startTimeMs ?: 0L,
                                snoozedUntilMs = snoozedUntilMs,
                            )
                            if (reminder != null) repository.markReminderDelivered(alarmRequestCode)
                            return@runCatching
                        }
                        Log.w(TAG, "Reminder dropped: missing event/reminder for requestCode=$alarmRequestCode")
                    }
                    ACTION_SNOOZE_REMINDER -> {
                        scheduler.cancelLiveProgress(alarmRequestCode)
                        scheduler.cancelRepeat(alarmRequestCode)
                        NotificationManagerCompat.from(context).cancel(alarmRequestCode)
                        val snoozeAtMs = System.currentTimeMillis() + ReminderNotificationActions.snoozeDelayMs(snoozeMinutes)
                        val reminder = repository.getReminderByRequestCode(alarmRequestCode)
                        val targetEventId = eventId ?: reminder?.eventId ?: return@runCatching
                        val event = repository.getEvent(targetEventId)
                        val eventTitle = event?.title ?: fallbackTitle ?: "Event reminder"
                        scheduler.scheduleSnooze(
                            eventId = targetEventId,
                            eventTitle = eventTitle,
                            alarmRequestCode = alarmRequestCode,
                            triggerAtMs = snoozeAtMs,
                            snoozeMinutes = snoozeMinutes,
                            isTask = event?.isTask == 1 || fallbackIsTask,
                        )
                        DotCalGlyphBridge.eventSnoozed(
                            context,
                            reminderGlyphEventId(
                                event,
                                targetEventId,
                                eventStartTimeMs.takeIf { it > 0L }
                                    ?: reminder?.let { it.triggerAtMs + it.minutesBefore * 60_000L }
                                    ?: 0L,
                            ),
                            snoozeAtMs,
                        )
                    }
                    ACTION_COMPLETE_TASK_REMINDER -> {
                        scheduler.cancelLiveProgress(alarmRequestCode)
                        scheduler.cancelRepeat(alarmRequestCode)
                        NotificationManagerCompat.from(context).cancel(alarmRequestCode)
                        val targetEventId = eventId ?: repository.getReminderByRequestCode(alarmRequestCode)?.eventId ?: return@runCatching
                        val task = repository.getEvent(targetEventId)
                        if (task?.isTask == 1) {
                            repository.setTaskCompleted(task, completed = true)
                            DotCalGlyphBridge.taskCompleted(
                                context,
                                reminderGlyphEventId(
                                    task,
                                    targetEventId,
                                    eventStartTimeMs.takeIf { it > 0L }
                                        ?: repository.getReminderByRequestCode(alarmRequestCode)?.let {
                                            it.triggerAtMs + it.minutesBefore * 60_000L
                                        }
                                        ?: 0L,
                                ),
                            )
                        } else {
                            Log.w(TAG, "Complete task ignored: missing task for requestCode=$alarmRequestCode")
                        }
                    }
                    ACTION_DISMISS_REMINDER -> {
                        scheduler.cancelLiveProgress(alarmRequestCode)
                        scheduler.cancelRepeat(alarmRequestCode)
                        NotificationManagerCompat.from(context).cancel(alarmRequestCode)
                    }
                    ACTION_OPEN_REMINDER -> {
                        scheduler.cancelLiveProgress(alarmRequestCode)
                        scheduler.cancelRepeat(alarmRequestCode)
                        NotificationManagerCompat.from(context).cancel(alarmRequestCode)
                        val reminder = repository.getReminderByRequestCode(alarmRequestCode)
                        val targetEventId = eventId ?: reminder?.eventId
                        if (targetEventId.isNullOrBlank()) {
                            Log.w(TAG, "Open reminder ignored: missing event for requestCode=$alarmRequestCode")
                            return@runCatching
                        }
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
        const val ACTION_UPDATE_LIVE_PROGRESS = "com.dotfield.dotcal.action.UPDATE_LIVE_PROGRESS"
        const val ACTION_SNOOZE_REMINDER = "com.dotfield.dotcal.action.SNOOZE_REMINDER"
        const val ACTION_COMPLETE_TASK_REMINDER = "com.dotfield.dotcal.action.COMPLETE_TASK_REMINDER"
        const val ACTION_DISMISS_REMINDER = "com.dotfield.dotcal.action.DISMISS_REMINDER"
        const val ACTION_OPEN_REMINDER = "com.dotfield.dotcal.action.OPEN_REMINDER"
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_ALARM_REQUEST_CODE = "extra_alarm_request_code"
        const val EXTRA_EVENT_TITLE = "extra_event_title"
        const val EXTRA_MINUTES_BEFORE = "extra_minutes_before"
        const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"
        const val EXTRA_IS_TASK = "extra_is_task"
        const val EXTRA_EVENT_START_TIME_MS = "extra_event_start_time_ms"
        const val EXTRA_SNOOZED_UNTIL_MS = "extra_snoozed_until_ms"
        const val EXTRA_CLEAR_REMINDER_ON_OPEN = "extra_clear_reminder_on_open"
        private const val DEFAULT_SNOOZE_MINUTES = 15
    }
}

internal fun reminderGlyphEventId(event: CalendarEvent?, fallbackEventId: String, occurrenceStartMs: Long): String {
    if (event == null || occurrenceStartMs <= 0L) return fallbackEventId
    val isRecurring = !event.rrule.isNullOrBlank() || !event.providerRdate.isNullOrBlank()
    return if (isRecurring) recurrenceOccurrenceId(event.baseEventId(), occurrenceStartMs) else fallbackEventId
}
