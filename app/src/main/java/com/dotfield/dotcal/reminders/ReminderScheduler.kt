package com.dotfield.dotcal.reminders

import android.Manifest
import android.app.AlarmManager
import android.app.Notification
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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dotfield.dotcal.MainActivity
import com.dotfield.dotcal.R
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventReminder
import com.dotfield.dotcal.data.recurrence.planNextReminder
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import com.nothing.ketchum.Common
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import java.text.DateFormat
import java.util.Date

class ReminderScheduler(private val context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = appContext.getSystemService(AlarmManager::class.java)

    fun scheduleReminder(
        reminder: EventReminder,
        event: CalendarEvent,
        occurrenceStartTimeMs: Long = event.startTimeMs,
    ) {
        val nowMs = System.currentTimeMillis()
        val plan = planNextReminder(event, reminder.minutesBefore, nowMs)
        val triggerAtMs = plan?.triggerAtMs ?: reminder.triggerAtMs
        val effectiveOccurrenceStartMs = plan?.occurrenceStartMs ?: occurrenceStartTimeMs
        if (triggerAtMs <= nowMs) return
        val pendingIntent = reminderPendingIntent(
            requestCode = reminder.alarmRequestCode,
            payload = ReminderAlarmPayload(
                eventId = event.id,
                alarmRequestCode = reminder.alarmRequestCode,
                eventTitle = event.title,
                minutesBefore = reminder.minutesBefore,
                isTask = event.isTask == 1,
                eventStartTimeMs = effectiveOccurrenceStartMs,
            ),
        )
        if (shouldUseAlarmClock(event.isTask == 1, canScheduleExactAlarms())) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMs, viewReminderPendingIntent(event.id, isTask = event.isTask == 1)),
                pendingIntent,
            )
            return
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
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
        if (shouldUseAlarmClock(isTask, canScheduleExactAlarms())) {
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
        scheduleRepeatAlarm: Boolean = true,
    ) {
        val settings = notificationSettings()
        ensureChannel(settings)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "Reminder notification blocked: POST_NOTIFICATIONS not granted")
            return
        }
        val useLiveProgress = useNothingLiveProgress()
        val targetTimeMs = if (snoozedUntilMs > 0L) snoozedUntilMs else eventStartTimeMs
        if (scheduleRepeatAlarm && settings.repeatEnabled) {
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
        val notification = if (useLiveProgress && Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
            buildNothingProgressNotification(
                settings = settings,
                eventId = eventId,
                eventTitle = eventTitle,
                minutesBefore = minutesBefore,
                alarmRequestCode = alarmRequestCode,
                isTask = isTask,
                eventStartTimeMs = eventStartTimeMs,
                snoozedUntilMs = snoozedUntilMs,
                progressOriginMs = progressOriginMs,
                targetTimeMs = targetTimeMs,
            )
        } else {
            buildStandardNotification(
                settings = settings,
                eventId = eventId,
                eventTitle = eventTitle,
                minutesBefore = minutesBefore,
                alarmRequestCode = alarmRequestCode,
                isTask = isTask,
                eventStartTimeMs = eventStartTimeMs,
                snoozedUntilMs = snoozedUntilMs,
            )
        }
        NotificationManagerCompat.from(appContext).notify(ReminderNotificationActions.notificationId(alarmRequestCode), notification)
        if (useLiveProgress) {
            // Cancel alarms created by both the current and the pre-ProgressStyle
            // implementations before scheduling the single active updater.
            cancelLiveProgress(alarmRequestCode)
            scheduleLiveProgress(
                eventId = eventId,
                eventTitle = eventTitle,
                alarmRequestCode = alarmRequestCode,
                isTask = isTask,
                minutesBefore = minutesBefore,
                eventStartTimeMs = eventStartTimeMs,
                snoozedUntilMs = snoozedUntilMs,
                targetTimeMs = targetTimeMs,
                progressOriginMs = progressOriginMs,
            )
        }
    }

    private fun buildStandardNotification(
        settings: ReminderNotificationSettings,
        eventId: String,
        eventTitle: String,
        minutesBefore: Int,
        alarmRequestCode: Int,
        isTask: Boolean,
        eventStartTimeMs: Long,
        snoozedUntilMs: Long,
    ): Notification {
        val builder = NotificationCompat.Builder(appContext, settings.channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(eventTitle)
            .setContentText(reminderText(minutesBefore, snoozedUntilMs))
            .setContentIntent(openReminderPendingIntent(eventId, isTask, alarmRequestCode))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setDeleteIntent(dismissPendingIntent(alarmRequestCode))
            .setVibrate(if (settings.vibrationEnabled) ReminderNotificationActions.vibrationPattern() else longArrayOf(0))
            .setSound(settings.soundUri)
        if (settings.fullScreenEnabled) {
            builder.setFullScreenIntent(viewReminderPendingIntent(eventId, isTask), true)
        }
        addNotificationActions(
            builder = builder,
            eventId = eventId,
            eventTitle = eventTitle,
            alarmRequestCode = alarmRequestCode,
            isTask = isTask,
            eventStartTimeMs = eventStartTimeMs,
        )
        return builder.build()
    }

    @android.annotation.TargetApi(Build.VERSION_CODES.BAKLAVA)
    private fun buildNothingProgressNotification(
        settings: ReminderNotificationSettings,
        eventId: String,
        eventTitle: String,
        minutesBefore: Int,
        alarmRequestCode: Int,
        isTask: Boolean,
        eventStartTimeMs: Long,
        snoozedUntilMs: Long,
        progressOriginMs: Long,
        targetTimeMs: Long,
    ): Notification {
        val progress = ReminderNotificationActions.progressPercent(
            originMs = progressOriginMs,
            targetMs = targetTimeMs,
            nowMs = System.currentTimeMillis(),
        )
        val builder = Notification.Builder(appContext, settings.channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(eventTitle)
            .setContentText(reminderText(minutesBefore, snoozedUntilMs))
            .setContentIntent(openReminderPendingIntent(eventId, isTask, alarmRequestCode))
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setPriority(Notification.PRIORITY_HIGH)
            .setCategory(Notification.CATEGORY_EVENT)
            .setDeleteIntent(dismissPendingIntent(alarmRequestCode))
            .setVibrate(if (settings.vibrationEnabled) ReminderNotificationActions.vibrationPattern() else longArrayOf(0))
            .setSound(settings.soundUri)
            .setStyle(
                Notification.ProgressStyle()
                    .setStyledByProgress(true)
                    .setProgress(progress),
            )
        if (settings.fullScreenEnabled) {
            builder.setFullScreenIntent(viewReminderPendingIntent(eventId, isTask), true)
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
        return builder.build()
    }

    private fun addNotificationActions(
        builder: NotificationCompat.Builder,
        eventId: String,
        eventTitle: String,
        alarmRequestCode: Int,
        isTask: Boolean,
        eventStartTimeMs: Long,
    ) {
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
    }

    fun updateLiveProgress(intent: Intent) {
        val eventId = intent.getStringExtra(ReminderReceiver.EXTRA_EVENT_ID) ?: return
        val alarmRequestCode = intent.getIntExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, Int.MIN_VALUE)
        if (alarmRequestCode == Int.MIN_VALUE) return
        val eventTitle = intent.getStringExtra(ReminderReceiver.EXTRA_EVENT_TITLE) ?: return
        val minutesBefore = intent.getIntExtra(ReminderReceiver.EXTRA_MINUTES_BEFORE, 0)
        val isTask = intent.getBooleanExtra(ReminderReceiver.EXTRA_IS_TASK, false)
        val eventStartTimeMs = intent.getLongExtra(ReminderReceiver.EXTRA_EVENT_START_TIME_MS, 0L)
        val snoozedUntilMs = intent.getLongExtra(ReminderReceiver.EXTRA_SNOOZED_UNTIL_MS, 0L)
        val progressOriginMs = intent.getLongExtra(EXTRA_PROGRESS_ORIGIN_MS, System.currentTimeMillis())
        showReminderNotification(
            eventId = eventId,
            eventTitle = eventTitle,
            minutesBefore = minutesBefore,
            alarmRequestCode = alarmRequestCode,
            isTask = isTask,
            eventStartTimeMs = eventStartTimeMs,
            snoozedUntilMs = snoozedUntilMs,
            progressOriginMs = progressOriginMs,
            scheduleRepeatAlarm = false,
        )
    }

    fun cancelLiveProgress(alarmRequestCode: Int) {
        ReminderNotificationActions.liveProgressRequestCodes(alarmRequestCode).forEach { requestCode ->
            alarmManager.cancel(liveProgressPendingIntent(requestCode, ReminderAlarmPayload.EMPTY))
        }
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
            ReminderNotificationActions.notificationId(payload.alarmRequestCode),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun liveProgressPendingIntent(requestCode: Int, payload: ReminderAlarmPayload): PendingIntent =
        liveProgressIntent(payload.copy(alarmRequestCode = requestCode), 0L)

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
                .setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build())
            enableVibration(settings.vibrationEnabled)
            if (settings.vibrationEnabled) vibrationPattern = ReminderNotificationActions.vibrationPattern()
        }
        appContext.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun notificationSettings(): ReminderNotificationSettings = runBlocking {
        val preferences = appContext.calendarPreferencesDataStore.data.first()
        ReminderNotificationSettings.from(preferences, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
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
        val triggerAtMs = System.currentTimeMillis() + repeatMinutes * 60_000L
        if (shouldUseAlarmClock(isTask, canScheduleExactAlarms())) {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMs, viewReminderPendingIntent(eventId, isTask)),
                pendingIntent,
            )
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
        }
    }

    private fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

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
            data = Uri.parse(ReminderNotificationActions.reminderDeepLink(eventId, isTask))
            flags = ReminderNotificationActions.fullScreenIntentFlags()
            putExtra(ReminderNotificationActions.EXTRA_FULL_SCREEN_REMINDER, true)
        }
        return PendingIntent.getActivity(
            appContext,
            31 * eventId.hashCode() + if (isTask) 1 else 0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun openReminderPendingIntent(eventId: String, isTask: Boolean, alarmRequestCode: Int): PendingIntent {
        val intent = Intent(appContext, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            data = Uri.parse(ReminderNotificationActions.reminderDeepLink(eventId, isTask))
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(ReminderReceiver.EXTRA_EVENT_ID, eventId)
            putExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, alarmRequestCode)
            putExtra(ReminderReceiver.EXTRA_IS_TASK, isTask)
            putExtra(ReminderReceiver.EXTRA_CLEAR_REMINDER_ON_OPEN, true)
        }
        return PendingIntent.getActivity(
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
            0 -> appContext.getString(R.string.notification_starting_now)
            1 -> appContext.getString(R.string.notification_starts_in_minute)
            60 -> appContext.getString(R.string.notification_starts_in_hour)
            1440 -> appContext.getString(R.string.notification_starts_in_day)
            else -> appContext.getString(R.string.notification_starts_in_minutes, minutesBefore)
        }
    }

    companion object {
        private const val TAG = "ReminderScheduler"
        private const val EXTRA_PROGRESS_ORIGIN_MS = "extra_progress_origin_ms"
        private const val LIVE_PROGRESS_INTERVAL_MS = 5_000L
        const val CHANNEL_ID = "dotcal_reminders"
        private fun completeTaskRequestCode(alarmRequestCode: Int): Int = alarmRequestCode xor 0x7C7C7C7C
    }
}

internal fun shouldUseAlarmClock(isTask: Boolean, canScheduleExactAlarms: Boolean): Boolean =
    isTask || !canScheduleExactAlarms

internal data class ReminderNotificationSettings(
    val soundUri: Uri?,
    val repeatEnabled: Boolean,
    val repeatMinutes: Int,
    val vibrationEnabled: Boolean,
    val fullScreenEnabled: Boolean,
) {
    val channelId: String
        get() = "dotcal_reminders_v2_${listOf(soundUri?.toString().orEmpty(), vibrationEnabled).hashCode()}"

    companion object {
        internal fun from(
            preferences: androidx.datastore.preferences.core.Preferences,
            defaultSoundUri: Uri? = null,
        ): ReminderNotificationSettings {
            val isPro = preferences[CalendarPreferences.KEY_IS_PRO] ?: false
            val storedSound = preferences[CalendarPreferences.KEY_REMINDER_SOUND_URI]
            val sound = when {
                !isPro -> defaultSoundUri
                storedSound == ReminderNotificationActions.SILENT_SOUND_VALUE -> null
                storedSound.isNullOrBlank() -> defaultSoundUri
                else -> Uri.parse(storedSound)
            }
            return ReminderNotificationSettings(
                soundUri = sound,
                repeatEnabled = isPro && (preferences[CalendarPreferences.KEY_REMINDER_REPEAT_ENABLED] ?: false),
                repeatMinutes = (preferences[CalendarPreferences.KEY_REMINDER_REPEAT_MINUTES] ?: 5).coerceIn(5, 60),
                vibrationEnabled = if (isPro) preferences[CalendarPreferences.KEY_REMINDER_VIBRATION_ENABLED] ?: true else true,
                fullScreenEnabled = isPro && (preferences[CalendarPreferences.KEY_REMINDER_FULL_SCREEN_ENABLED] ?: false),
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
