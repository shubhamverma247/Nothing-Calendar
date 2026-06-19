package com.dotfield.dotcal.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dotfield.dotcal.MainActivity
import com.dotfield.dotcal.R
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventReminder

class ReminderScheduler(private val context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    fun scheduleReminder(reminder: EventReminder, event: CalendarEvent) {
        if (reminder.triggerAtMs <= System.currentTimeMillis()) return
        val pendingIntent = reminderPendingIntent(
            requestCode = reminder.alarmRequestCode,
            payload = ReminderAlarmPayload(
                eventId = event.id,
                alarmRequestCode = reminder.alarmRequestCode,
                eventTitle = event.title,
                minutesBefore = reminder.minutesBefore,
                isTask = event.isTask == 1,
            ),
        )
        if (event.isTask == 1 || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms())) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(reminder.triggerAtMs, viewReminderPendingIntent(event.id, isTask = event.isTask == 1)),
                pendingIntent,
            )
            return
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.triggerAtMs, pendingIntent)
    }

    fun scheduleSnooze(eventId: String, eventTitle: String, alarmRequestCode: Int, triggerAtMs: Long, isTask: Boolean = false) {
        val requestCode = snoozeRequestCode(alarmRequestCode)
        val pendingIntent = reminderPendingIntent(
            requestCode = requestCode,
            payload = ReminderAlarmPayload(
                eventId = eventId,
                alarmRequestCode = alarmRequestCode,
                eventTitle = eventTitle,
                minutesBefore = 10,
                isTask = isTask,
            ),
        )
        if (isTask || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms())) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMs, viewReminderPendingIntent(eventId, isTask)),
                pendingIntent,
            )
            return
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
    }

    fun cancelReminder(alarmRequestCode: Int) {
        alarmManager.cancel(reminderPendingIntent(alarmRequestCode, payload = ReminderAlarmPayload.EMPTY.copy(alarmRequestCode = alarmRequestCode)))
        alarmManager.cancel(reminderPendingIntent(snoozeRequestCode(alarmRequestCode), payload = ReminderAlarmPayload.EMPTY.copy(alarmRequestCode = alarmRequestCode)))
    }

    fun showReminderNotification(event: CalendarEvent, reminder: EventReminder) {
        showReminderNotification(
            eventId = event.id,
            eventTitle = event.title,
            minutesBefore = reminder.minutesBefore,
            alarmRequestCode = reminder.alarmRequestCode,
            isTask = event.isTask == 1,
        )
    }

    fun showReminderNotification(eventId: String, eventTitle: String, minutesBefore: Int, alarmRequestCode: Int, isTask: Boolean = false) {
        ensureChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Reminder notification blocked: POST_NOTIFICATIONS not granted")
            return
        }
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(eventTitle)
            .setContentText(reminderText(minutesBefore))
            .setContentIntent(viewReminderPendingIntent(eventId, isTask))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(R.drawable.ic_notification, if (isTask) "View Task" else "View", viewReminderPendingIntent(eventId, isTask))
            .addAction(R.drawable.ic_notification, "Snooze 10 Min", snoozePendingIntent(eventId, eventTitle, alarmRequestCode, isTask))
            .build()
        NotificationManagerCompat.from(appContext).notify(alarmRequestCode, notification)
    }

    fun ensureChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "DotCal reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Event reminder alerts"
        }
        appContext.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun reminderPendingIntent(requestCode: Int, payload: ReminderAlarmPayload): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SHOW_REMINDER
            if (payload.eventId.isNotBlank()) putExtra(ReminderReceiver.EXTRA_EVENT_ID, payload.eventId)
            putExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, payload.alarmRequestCode)
            payload.eventTitle?.let { putExtra(ReminderReceiver.EXTRA_EVENT_TITLE, it) }
            putExtra(ReminderReceiver.EXTRA_MINUTES_BEFORE, payload.minutesBefore)
            putExtra(ReminderReceiver.EXTRA_IS_TASK, payload.isTask)
        }
        return PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun viewReminderPendingIntent(eventId: String, isTask: Boolean): PendingIntent {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(if (isTask) "dotcal://task/$eventId" else "dotcal://event/$eventId")
        }
        return PendingIntent.getActivity(
            appContext,
            31 * eventId.hashCode() + if (isTask) 1 else 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun snoozePendingIntent(eventId: String, eventTitle: String, alarmRequestCode: Int, isTask: Boolean): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SNOOZE_REMINDER
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, alarmRequestCode)
            putExtra(ReminderReceiver.EXTRA_EVENT_TITLE, eventTitle)
            putExtra(ReminderReceiver.EXTRA_IS_TASK, isTask)
        }
        return PendingIntent.getBroadcast(
            appContext,
            snoozeRequestCode(alarmRequestCode),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun reminderText(minutesBefore: Int): String {
        return when (minutesBefore) {
            0 -> "Starting now"
            1 -> "Starts in 1 minute"
            60 -> "Starts in 1 hour"
            1440 -> "Starts in 1 day"
            else -> "Starts in $minutesBefore minutes"
        }
    }

    companion object {
        private const val TAG = "ReminderScheduler"
        const val CHANNEL_ID = "dotcal_reminders"
        private fun snoozeRequestCode(alarmRequestCode: Int): Int = alarmRequestCode xor 0x5A5A5A5A
    }
}

private data class ReminderAlarmPayload(
    val eventId: String,
    val alarmRequestCode: Int,
    val eventTitle: String?,
    val minutesBefore: Int,
    val isTask: Boolean,
) {
    companion object {
        val EMPTY = ReminderAlarmPayload(
            eventId = "",
            alarmRequestCode = Int.MIN_VALUE,
            eventTitle = null,
            minutesBefore = 0,
            isTask = false,
        )
    }
}
