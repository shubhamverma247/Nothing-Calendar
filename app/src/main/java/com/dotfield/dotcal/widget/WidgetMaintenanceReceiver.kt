package com.dotfield.dotcal.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WidgetMaintenanceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_CONFIGURATION_CHANGED -> WidgetUpdateWorker.enqueueConfigurationRefresh(context)
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIMEZONE_CHANGED -> WidgetUpdateWorker.enqueue(context)
        }
    }
}
