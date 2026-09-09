package com.dotfield.dotcal.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dotfield.dotcal.launcher.ACTION_DAILY_ICON_REFRESH
import com.dotfield.dotcal.launcher.DailyLauncherIconScheduler
import com.dotfield.dotcal.launcher.DynamicLauncherIconManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

internal enum class WidgetMaintenanceAction {
    CONFIGURATION,
    STANDARD,
    DAILY_ICON,
    NONE,
}

internal fun widgetMaintenanceActionFor(action: String?): WidgetMaintenanceAction = when (action) {
    ACTION_DAILY_ICON_REFRESH -> WidgetMaintenanceAction.DAILY_ICON
    Intent.ACTION_CONFIGURATION_CHANGED -> WidgetMaintenanceAction.CONFIGURATION
    Intent.ACTION_BOOT_COMPLETED,
    Intent.ACTION_DATE_CHANGED,
    Intent.ACTION_MY_PACKAGE_REPLACED,
    Intent.ACTION_TIMEZONE_CHANGED,
    Intent.ACTION_TIME_CHANGED -> WidgetMaintenanceAction.STANDARD
    else -> WidgetMaintenanceAction.NONE
}

class WidgetMaintenanceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (widgetMaintenanceActionFor(intent.action)) {
            WidgetMaintenanceAction.CONFIGURATION,
            WidgetMaintenanceAction.STANDARD,
            WidgetMaintenanceAction.DAILY_ICON -> {
                val pendingResult = goAsync()
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    try {
                        DailyLauncherIconScheduler(context).scheduleNextRefresh()
                        DynamicLauncherIconManager(context).updateIconForToday()
                        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                            WidgetUpdateWorker.enqueueConfigurationRefresh(context)
                        } else {
                            WidgetUpdateWorker.enqueue(context)
                        }
                    } catch (error: Exception) {
                        Log.e(TAG, "Maintenance refresh failed", error)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            WidgetMaintenanceAction.NONE -> Unit
        }
    }

    private companion object {
        const val TAG = "WidgetMaintenance"
    }
}
