package com.dotfield.dotcal.launcher

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.dotfield.dotcal.widget.WidgetMaintenanceReceiver
import java.time.ZonedDateTime

internal const val ACTION_DAILY_ICON_REFRESH = "com.dotfield.dotcal.action.DAILY_ICON_REFRESH"

private const val DAILY_ICON_REQUEST_CODE = 42031

class DailyLauncherIconScheduler(context: Context) {
    private val appContext = context.applicationContext
    private val alarmManager = requireNotNull(appContext.getSystemService(AlarmManager::class.java))

    fun scheduleNextRefresh(now: ZonedDateTime = ZonedDateTime.now()): Boolean {
        val operation = dailyRefreshPendingIntent()
        val triggerAtMillis = nextLauncherIconRefreshAt(now).toInstant().toEpochMilli()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
            return false
        }
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, operation)
        return true
    }

    fun cancelNextRefresh() {
        alarmManager.cancel(dailyRefreshPendingIntent())
    }

    private fun dailyRefreshPendingIntent(): PendingIntent = PendingIntent.getBroadcast(
            appContext,
            DAILY_ICON_REQUEST_CODE,
            Intent(appContext, WidgetMaintenanceReceiver::class.java).setAction(ACTION_DAILY_ICON_REFRESH),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}
