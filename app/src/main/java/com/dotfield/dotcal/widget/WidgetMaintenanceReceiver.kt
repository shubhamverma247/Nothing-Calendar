package com.dotfield.dotcal.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class WidgetMaintenanceReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        WidgetUpdateWorker.enqueue(context)
    }
}
