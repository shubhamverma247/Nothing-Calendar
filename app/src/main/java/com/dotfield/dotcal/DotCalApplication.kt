package com.dotfield.dotcal

import android.app.Application
import com.dotfield.dotcal.data.DotCalDatabase
import com.dotfield.dotcal.data.DotCalRepository
import com.dotfield.dotcal.reminders.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DotCalApplication : Application() {
    val database: DotCalDatabase by lazy { DotCalDatabase.create(this) }
    val repository: DotCalRepository by lazy { DotCalRepository(database.calendarDao(), this) }

    override fun onCreate() {
        super.onCreate()
        ReminderScheduler(this).ensureChannel()
        CoroutineScope(Dispatchers.IO).launch {
            repository.rescheduleFutureReminders()
        }
    }
}
