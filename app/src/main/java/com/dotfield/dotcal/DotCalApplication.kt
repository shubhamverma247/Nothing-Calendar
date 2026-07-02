package com.dotfield.dotcal

import android.app.Application
import com.dotfield.dotcal.data.DotCalDatabase
import com.dotfield.dotcal.data.DotCalRepository
import com.dotfield.dotcal.data.billing.ProManager
import com.dotfield.dotcal.reminders.ReminderScheduler
import com.dotfield.dotcal.sync.CalendarSyncWorkScheduler
import com.dotfield.dotcal.widget.WidgetUpdateWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DotCalApplication : Application() {
    val database: DotCalDatabase by lazy { DotCalDatabase.create(this) }
    val repository: DotCalRepository by lazy { DotCalRepository(database.calendarDao(), this) }
    val proManager: ProManager by lazy { ProManager(this, repository) }

    override fun onCreate() {
        super.onCreate()
        ReminderScheduler(this).ensureChannel()
        proManager.initialize()
        CoroutineScope(Dispatchers.IO).launch {
            repository.rescheduleFutureReminders()
            CalendarSyncWorkScheduler.syncFromPreferences(this@DotCalApplication)
            WidgetUpdateWorker.enqueue(this@DotCalApplication)
        }
    }
}
