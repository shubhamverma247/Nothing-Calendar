package com.dotfield.dotcal

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import com.dotfield.dotcal.data.DotCalDatabase
import com.dotfield.dotcal.data.DotCalRepository
import com.dotfield.dotcal.data.billing.ProManager
import com.dotfield.dotcal.reminders.ReminderScheduler
import com.dotfield.dotcal.sync.CalendarSyncWorkScheduler
import com.dotfield.dotcal.widget.WidgetUpdateWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DotCalApplication : Application() {
    val database: DotCalDatabase by lazy { DotCalDatabase.create(this) }
    val repository: DotCalRepository by lazy { DotCalRepository(database.calendarDao(), this) }
    val proManager: ProManager by lazy { ProManager(this, repository) }
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        ReminderScheduler(this).ensureChannel()
        proManager.initialize()
        runStartupTask { repository.rescheduleFutureReminders() }
        runStartupTask {
            if (CalendarSyncWorkScheduler.syncFromPreferences(this@DotCalApplication)) {
                CalendarSyncWorkScheduler.enqueueSyncNow(this@DotCalApplication)
            }
        }
        runStartupTask { WidgetUpdateWorker.enqueue(this@DotCalApplication) }
        registerSystemThemeChangeReceiver()
    }

    private fun registerSystemThemeChangeReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                    // Apply the new dark/light palette immediately. CONFIGURATION_CHANGED is not
                    // exempt from implicit-broadcast limits, so this runtime receiver is the only
                    // reliable delivery path and must not wait behind a WorkManager queue.
                    runStartupTask { WidgetUpdateWorker.updateNow(this@DotCalApplication) }
                }
            }
        }
        ContextCompat.registerReceiver(
            this,
            receiver,
            IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    private fun runStartupTask(block: suspend () -> Unit) {
        applicationScope.launch {
            runCatching { block() }
        }
    }
}
