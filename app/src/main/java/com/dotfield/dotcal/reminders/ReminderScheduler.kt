package com.dotfield.dotcal.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dotfield.dotcal.MainActivity
import com.dotfield.dotcal.R
import com.dotfield.dotcal.data.CalendarEvent
import com.nothing.ketchum.Common
import com.dotfield.dotcal.data.EventReminder
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import java.text.DateFormat
import java.util.Date

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
                eventStartTimeMs = event.startTimeMs,
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

    fun scheduleSnooze(eventId: String, eventTitle: String, alarmRequestCode: Int, triggerAtMs: Long, snoozeMinutes: Int, isTask: Boolean = false) {
        cancelSnoozeAlarms(alarmRequestCode)
        val requestCode = ReminderNotificationActions.snoozeAlarmRequestCode(alarmRequestCode)
        val pendingIntent = reminderPendingIntent(
            requestCode = requestCode,
            payload = ReminderAlarmPayload(
                eventId = eventId,
                alarmRequestCode = alarmRequestCode,
                eventTitle = eventTitle,
                minutesBefore = snoozeMinutes,
                isTask = isTask,
                snoozedUntilMs = triggerAtMs,
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
        cancelLiveProgress(alarmRequestCode)
        cancelSnoozeAlarms(alarmRequestCode)
        cancelRepeat(alarmRequestCode)
    }

    fun showReminderNotification(event: CalendarEvent, reminder: EventReminder) {
        showReminderNotification(
            eventId = event.id,
            eventTitle = event.title,
            minutesBefore = reminder.minutesBefore,
            alarmRequestCode = reminder.alarmRequestCode,
            isTask = event.isTask == 1,
            eventStartTimeMs = event.startTimeMs,
        )
    }

    fun showReminderNotification(
        eventId: String,
        eventTitle: String,
        minutesBefore: Int,
        alarmRequestCode: Int,
        isTask: Boolean = false,
        eventStartTimeMs: Long = 0L,
        snoozedUntilMs: Long = 0L,
        progressOriginMs: Long = System.currentTimeMillis(),
    ) {
        val settings = notificationSettings()
        ensureChannel(settings)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Reminder notification blocked: POST_NOTIFICATIONS not granted")
            return
        }
        val builder = NotificationCompat.Builder(appContext, settings.channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(eventTitle)
            .setContentText(reminderText(minutesBefore, snoozedUntilMs))
            .setContentIntent(openReminderPendingIntent(eventId, isTask, alarmRequestCode))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setDeleteIntent(dismissPendingIntent(alarmRequestCode))
            .setVibrate(if (settings.vibrationEnabled) longArrayOf(0, 250, 100, 250) else longArrayOf(0))
            .setSound(settings.soundUri)
        if (settings.fullScreenEnabled) {
            builder.setFullScreenIntent(viewReminderPendingIntent(eventId, isTask), true)
        }
        if (useNothingLiveProgress()) {
            val progressAtMs = if (snoozedUntilMs > 0L) snoozedUntilMs else eventStartTimeMs
            val progress = progressPercent(progressOriginMs, progressAtMs)
            val views = RemoteViews(appContext.packageName, R.layout.notification_ongoing).apply {
                setTextViewText(R.id.notification_event_title, eventTitle)
                setTextViewText(R.id.notification_event_time, reminderText(minutesBefore, snoozedUntilMs))
                setProgressBar(R.id.notification_progress, 100, progress, false)
            }
            builder
                .setOngoing(true)
                .setAutoCancel(false)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setCustomContentView(views)
                .setCustomBigContentView(views)
        }
        if (settings.repeatEnabled) {
            scheduleRepeat(
                eventId = eventId,
                eventTitle = eventTitle,
                alarmRequestCode = alarmRequestCode,
                isTask = isTask,
                eventStartTimeMs = eventStartTimeMs,
                minutesBefore = minutesBefore,
                repeatMinutes = settings.repeatMinutes,
            )
        }
        ReminderNotificationActions.notificationActionTypes(isTask).forEach { action ->
            when (action) {
                ReminderNotificationActionType.Snooze -> builder.addAction(
                    R.drawable.ic_notification,
                    appContext.getString(R.string.notification_action_snooze),
                    snoozePickerPendingIntent(eventId, eventTitle, alarmRequestCode, isTask, eventStartTimeMs),
                )
                ReminderNotificationActionType.CompleteTask -> builder.addAction(
                    R.drawable.ic_notification,
                    appContext.getString(R.string.notification_action_complete_task),
                    completeTaskPendingIntent(eventId, alarmRequestCode),
                )
            }
        }
        val notification = builder.build()
        NotificationManagerCompat.from(appContext).notify(alarmRequestCode, notification)
        if (useNothingLiveProgress()) {
            scheduleLiveProgress(
                eventId = eventId,
                eventTitle = eventTitle,
                alarmRequestCode = alarmRequestCode,
                isTask = isTask,
                minutesBefore = minutesBefore,
                eventStartTimeMs = eventStartTimeMs,
                snoozedUntilMs = snoozedUntilMs,
                targetTimeMs = if (snoozedUntilMs > 0L) snoozedUntilMs else eventStartTimeMs,
                progressOriginMs = progressOriginMs,
            )
        }
    }

    fun updateLiveProgress(intent: Intent) {
        showReminderNotification(
            eventId = intent.getStringExtra(ReminderReceiver.EXTRA_EVENT_ID) ?: return,
            eventTitle = intent.getStringExtra(ReminderReceiver.EXTRA_EVENT_TITLE) ?: return,
            minutesBefore = intent.getIntExtra(ReminderReceiver.EXTRA_MINUTES_BEFORE, 0),
            alarmRequestCode = intent.getIntExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, Int.MIN_VALUE),
            isTask = intent.getBooleanExtra(ReminderReceiver.EXTRA_IS_TASK, false),
            eventStartTimeMs = intent.getLongExtra(ReminderReceiver.EXTRA_EVENT_START_TIME_MS, 0L),
            snoozedUntilMs = intent.getLongExtra(ReminderReceiver.EXTRA_SNOOZED_UNTIL_MS, 0L),
            progressOriginMs = intent.getLongExtra(EXTRA_PROGRESS_ORIGIN_MS, System.currentTimeMillis()),
        )
    }

    fun cancelLiveProgress(alarmRequestCode: Int) {
        alarmManager.cancel(liveProgressPendingIntent(alarmRequestCode, ReminderAlarmPayload.EMPTY))
    }

    private fun scheduleLiveProgress(
        eventId: String,
        eventTitle: String,
        alarmRequestCode: Int,
        isTask: Boolean,
        minutesBefore: Int,
        eventStartTimeMs: Long,
        snoozedUntilMs: Long,
        targetTimeMs: Long,
        progressOriginMs: Long,
    ) {
        if (targetTimeMs <= System.currentTimeMillis()) return
        val nextUpdateAtMs = (System.currentTimeMillis() + LIVE_PROGRESS_INTERVAL_MS).coerceAtMost(targetTimeMs)
        val payload = ReminderAlarmPayload(
            eventId = eventId,
            alarmRequestCode = alarmRequestCode,
            eventTitle = eventTitle,
            minutesBefore = minutesBefore,
            isTask = isTask,
            eventStartTimeMs = eventStartTimeMs,
            snoozedUntilMs = snoozedUntilMs,
        )
        val intent = liveProgressIntent(payload, progressOriginMs)
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextUpdateAtMs, intent)
    }

    private fun liveProgressIntent(payload: ReminderAlarmPayload, progressOriginMs: Long): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_UPDATE_LIVE_PROGRESS
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, payload.eventId)
            putExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, payload.alarmRequestCode)
            putExtra(ReminderReceiver.EXTRA_EVENT_TITLE, payload.eventTitle)
            putExtra(ReminderReceiver.EXTRA_MINUTES_BEFORE, payload.minutesBefore)
            putExtra(ReminderReceiver.EXTRA_IS_TASK, payload.isTask)
            putExtra(ReminderReceiver.EXTRA_EVENT_START_TIME_MS, payload.eventStartTimeMs)
            if (payload.snoozedUntilMs > 0L) putExtra(ReminderReceiver.EXTRA_SNOOZED_UNTIL_MS, payload.snoozedUntilMs)
            putExtra(EXTRA_PROGRESS_ORIGIN_MS, progressOriginMs)
        }
        return PendingIntent.getBroadcast(
            appContext,
            liveProgressRequestCode(payload.alarmRequestCode),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun liveProgressPendingIntent(requestCode: Int, payload: ReminderAlarmPayload): PendingIntent =
        liveProgressIntent(payload.copy(alarmRequestCode = requestCode), 0L)

    private fun progressPercent(originMs: Long, targetMs: Long): Int {
        if (targetMs <= originMs) return 100
        val elapsed = (System.currentTimeMillis() - originMs).coerceAtLeast(0L)
        return ((elapsed * 100L) / (targetMs - originMs)).toInt().coerceIn(0, 100)
    }

    private fun useNothingLiveProgress(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA &&
            runCatching { Common.is23112() }.getOrDefault(false)

    fun ensureChannel() = ensureChannel(notificationSettings())

    private fun ensureChannel(settings: ReminderNotificationSettings) {
        val channel = NotificationChannel(
            settings.channelId,
            "DotCal reminders",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Event reminder alerts"
            setSound(settings.soundUri, android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                .build())
            enableVibration(settings.vibrationEnabled)
            if (settings.vibrationEnabled) vibrationPattern = longArrayOf(0, 250, 100, 250)
        }
        appContext.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun notificationSettings(): ReminderNotificationSettings = runBlocking {
        val preferences = appContext.calendarPreferencesDataStore.data.first()
        ReminderNotificationSettings.from(preferences)
    }

    private fun scheduleRepeat(
        eventId: String,
        eventTitle: String,
        alarmRequestCode: Int,
        isTask: Boolean,
        eventStartTimeMs: Long,
        minutesBefore: Int,
        repeatMinutes: Int,
    ) {
        val repeatCode = ReminderNotificationActions.repeatAlarmRequestCode(alarmRequestCode)
        val pendingIntent = reminderPendingIntent(
            requestCode = repeatCode,
            payload = ReminderAlarmPayload(
                eventId = eventId,
                alarmRequestCode = alarmRequestCode,
                eventTitle = eventTitle,
                minutesBefore = minutesBefore,
                isTask = isTask,
                eventStartTimeMs = eventStartTimeMs,
            ),
        )
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + repeatMinutes * 60_000L,
            pendingIntent,
        )
    }

    fun cancelRepeat(alarmRequestCode: Int) {
        val requestCode = ReminderNotificationActions.repeatAlarmRequestCode(alarmRequestCode)
        alarmManager.cancel(reminderPendingIntent(requestCode, ReminderAlarmPayload.EMPTY.copy(alarmRequestCode = alarmRequestCode)))
    }

    private fun reminderPendingIntent(requestCode: Int, payload: ReminderAlarmPayload): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SHOW_REMINDER
            if (payload.eventId.isNotBlank()) putExtra(ReminderReceiver.EXTRA_EVENT_ID, payload.eventId)
            putExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, payload.alarmRequestCode)
            payload.eventTitle?.let { putExtra(ReminderReceiver.EXTRA_EVENT_TITLE, it) }
            putExtra(ReminderReceiver.EXTRA_MINUTES_BEFORE, payload.minutesBefore)
            putExtra(ReminderReceiver.EXTRA_IS_TASK, payload.isTask)
            if (payload.snoozedUntilMs > 0L) {
                putExtra(ReminderReceiver.EXTRA_SNOOZED_UNTIL_MS, payload.snoozedUntilMs)
            }
            if (payload.eventStartTimeMs > 0L) {
                putExtra(ReminderReceiver.EXTRA_EVENT_START_TIME_MS, payload.eventStartTimeMs)
            }
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

    private fun openReminderPendingIntent(eventId: String, isTask: Boolean, alarmRequestCode: Int): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_OPEN_REMINDER
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, alarmRequestCode)
            putExtra(ReminderReceiver.EXTRA_IS_TASK, isTask)
        }
        return PendingIntent.getBroadcast(
            appContext,
            alarmRequestCode xor 0x1E1E1E1E,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun snoozePickerPendingIntent(eventId: String, eventTitle: String, alarmRequestCode: Int, isTask: Boolean, eventStartTimeMs: Long): PendingIntent {
        val intent = Intent(appContext, SnoozePickerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, alarmRequestCode)
            putExtra(ReminderReceiver.EXTRA_EVENT_TITLE, eventTitle)
            putExtra(ReminderReceiver.EXTRA_IS_TASK, isTask)
            putExtra(ReminderReceiver.EXTRA_EVENT_START_TIME_MS, eventStartTimeMs)
        }
        return PendingIntent.getActivity(
            appContext, alarmRequestCode xor 0x4B4B4B4B, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun completeTaskPendingIntent(eventId: String, alarmRequestCode: Int): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_COMPLETE_TASK_REMINDER
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, alarmRequestCode)
        }
        return PendingIntent.getBroadcast(
            appContext,
            completeTaskRequestCode(alarmRequestCode),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun dismissPendingIntent(alarmRequestCode: Int): PendingIntent {
        val intent = Intent(appContext, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_DISMISS_REMINDER
            putExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, alarmRequestCode)
        }
        return PendingIntent.getBroadcast(
            appContext,
            alarmRequestCode xor 0x2D2D2D2D,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun cancelSnoozeAlarms(alarmRequestCode: Int) {
        val requestCodes = buildList {
            add(ReminderNotificationActions.snoozeAlarmRequestCode(alarmRequestCode))
            addAll(ReminderNotificationActions.SnoozeMinutes.map { minutes ->
                ReminderNotificationActions.snoozeRequestCode(alarmRequestCode, minutes)
            })
            addAll(ReminderNotificationActions.SnoozePickerMinutes.map { minutes ->
                ReminderNotificationActions.snoozeRequestCode(alarmRequestCode, minutes)
            })
        }.distinct()
        requestCodes.forEach { requestCode ->
            alarmManager.cancel(
                reminderPendingIntent(
                    requestCode,
                    payload = ReminderAlarmPayload.EMPTY.copy(alarmRequestCode = alarmRequestCode),
                ),
            )
        }
    }

    private fun reminderText(minutesBefore: Int, snoozedUntilMs: Long): String {
        if (snoozedUntilMs > 0L) {
            val formattedTime = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
                .format(Date(snoozedUntilMs))
        return appContext.getString(R.string.notification_snoozed_until, formattedTime)
        }
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
        private const val EXTRA_PROGRESS_ORIGIN_MS = "extra_progress_origin_ms"
        private const val LIVE_PROGRESS_INTERVAL_MS = 60_000L
        private fun liveProgressRequestCode(alarmRequestCode: Int): Int = alarmRequestCode xor 0x6D6D6D6D
        const val CHANNEL_ID = "dotcal_reminders"
        private fun completeTaskRequestCode(alarmRequestCode: Int): Int = alarmRequestCode xor 0x7C7C7C7C
    }
}

private data class ReminderNotificationSettings(
    val soundUri: Uri?,
    val repeatEnabled: Boolean,
    val repeatMinutes: Int,
    val vibrationEnabled: Boolean,
    val fullScreenEnabled: Boolean,
) {
    val channelId: String
        get() = "dotcal_reminders_${listOf(soundUri?.toString().orEmpty(), vibrationEnabled).hashCode()}"

    companion object {
        fun from(preferences: androidx.datastore.preferences.core.Preferences): ReminderNotificationSettings {
            val sound = preferences[CalendarPreferences.KEY_REMINDER_SOUND_URI]
                ?.takeIf(String::isNotBlank)
                ?.let(Uri::parse)
            return ReminderNotificationSettings(
                soundUri = sound ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                repeatEnabled = preferences[CalendarPreferences.KEY_REMINDER_REPEAT_ENABLED] ?: false,
                repeatMinutes = (preferences[CalendarPreferences.KEY_REMINDER_REPEAT_MINUTES] ?: 5).coerceIn(5, 60),
                vibrationEnabled = preferences[CalendarPreferences.KEY_REMINDER_VIBRATION_ENABLED] ?: true,
                fullScreenEnabled = preferences[CalendarPreferences.KEY_REMINDER_FULL_SCREEN_ENABLED] ?: false,
            )
        }
    }
}

private data class ReminderAlarmPayload(
    val eventId: String,
    val alarmRequestCode: Int,
    val eventTitle: String?,
    val minutesBefore: Int,
    val isTask: Boolean,
    val snoozedUntilMs: Long = 0L,
    val eventStartTimeMs: Long = 0L,
) {
    companion object {
        val EMPTY = ReminderAlarmPayload(
            eventId = "",
            alarmRequestCode = Int.MIN_VALUE,
            eventTitle = null,
            minutesBefore = 0,
            isTask = false,
            snoozedUntilMs = 0L,
            eventStartTimeMs = 0L,
        )
    }
}
