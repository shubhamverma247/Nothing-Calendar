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
            eventId = event.id,
            alarmRequestCode = reminder.alarmRequestCode,
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setWindow(
                AlarmManager.RTC_WAKEUP,
                reminder.triggerAtMs,
                FALLBACK_WINDOW_MS,
                pendingIntent,
            )
            return
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminder.triggerAtMs, pendingIntent)
    }

    fun scheduleSnooze(eventId: String, alarmRequestCode: Int, triggerAtMs: Long) {
        val requestCode = snoozeRequestCode(alarmRequestCode)
        val pendingIntent = reminderPendingIntent(
            requestCode = requestCode,
            eventId = eventId,
            alarmRequestCode = alarmRequestCode,
        )
        alarmManager.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMs, FALLBACK_WINDOW_MS, pendingIntent)
    }

    fun cancelReminder(alarmRequestCode: Int) {
        alarmManager.cancel(reminderPendingIntent(alarmRequestCode, eventId = null, alarmRequestCode = alarmRequestCode))
        alarmManager.cancel(reminderPendingIntent(snoozeRequestCode(alarmRequestCode), eventId = null, alarmRequestCode = alarmRequestCode))
    }

    fun showReminderNotification(event: CalendarEvent, reminder: EventReminder) {
        ensureChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle(event.title)
            .setContentText(reminderText(reminder.minutesBefore))
            .setContentIntent(viewEventPendingIntent(event.id))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .addAction(0, "VIEW", viewEventPendingIntent(event.id))
            .addAction(0, "SNOOZE 10 MIN", snoozePendingIntent(event.id, reminder.alarmRequestCode))
            .build()
        NotificationManagerCompat.from(appContext).notify(reminder.alarmRequestCode, notification)
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "DotCal reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Event reminder alerts"
        }
        appContext.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun reminderPendingIntent(requestCode: Int, eventId: String?, alarmRequestCode: Int): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SHOW_REMINDER
            eventId?.let { putExtra(ReminderReceiver.EXTRA_EVENT_ID, it) }
            putExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, alarmRequestCode)
        }
        return PendingIntent.getBroadcast(
            appContext,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun viewEventPendingIntent(eventId: String): PendingIntent {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse("dotcal://event/$eventId")
        }
        return PendingIntent.getActivity(
            appContext,
            eventId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun snoozePendingIntent(eventId: String, alarmRequestCode: Int): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SNOOZE_REMINDER
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, alarmRequestCode)
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
        const val CHANNEL_ID = "dotcal_reminders"
        private const val FALLBACK_WINDOW_MS = 5 * 60_000L
        private fun snoozeRequestCode(alarmRequestCode: Int): Int = alarmRequestCode xor 0x5A5A5A5A
    }
}
