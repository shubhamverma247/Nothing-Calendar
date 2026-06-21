package com.dotfield.dotcal.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.dotfield.dotcal.data.DotCalDatabase
import com.dotfield.dotcal.data.provider.CalendarProviderDataSource
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import com.dotfield.dotcal.widget.WidgetUpdateWorker
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class CalendarSyncWorker(
    appContext: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        return try {
            val dao = DotCalDatabase.create(applicationContext).calendarDao()
            val syncRepository = CalendarSyncRepository(
                dao = dao,
                providerDataSource = CalendarProviderDataSource(applicationContext),
            )
            val result = syncRepository.sync()
            if (!result.permissionDenied) {
                applicationContext.calendarPreferencesDataStore.edit { preferences ->
                    preferences[CalendarPreferences.KEY_LAST_SYNC_MS] = System.currentTimeMillis()
                }
                WidgetUpdateWorker.enqueue(applicationContext)
            }
            Result.success()
        } catch (_: SecurityException) {
            Result.failure()
        } catch (_: Exception) {
            if (runAttemptCount < MAX_RETRY_COUNT) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val UNIQUE_PERIODIC_WORK = "dotcal_calendar_sync_periodic"
        const val UNIQUE_ONE_TIME_WORK = "dotcal_calendar_sync_once"
        private const val MAX_RETRY_COUNT = 3
    }
}

object CalendarSyncWorkScheduler {
    suspend fun syncFromPreferences(context: Context) {
        val preferences = context.calendarPreferencesDataStore.data.first()
        val interval = preferences[CalendarPreferences.KEY_SYNC_INTERVAL_MINS] ?: DEFAULT_SYNC_INTERVAL_MINS
        if (preferences[CalendarPreferences.KEY_SYNC_ENABLED] == true && interval > 0) {
            schedulePeriodic(
                context = context,
                intervalMinutes = interval,
            )
        } else {
            cancelPeriodic(context)
        }
    }

    fun enqueueSyncNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<CalendarSyncWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            CalendarSyncWorker.UNIQUE_ONE_TIME_WORK,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    fun schedulePeriodic(context: Context, intervalMinutes: Int) {
        if (intervalMinutes <= 0) {
            cancelPeriodic(context)
            return
        }
        val interval = intervalMinutes.coerceAtLeast(MIN_SYNC_INTERVAL_MINS)
        val request = PeriodicWorkRequestBuilder<CalendarSyncWorker>(interval.toLong(), TimeUnit.MINUTES).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CalendarSyncWorker.UNIQUE_PERIODIC_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun cancelPeriodic(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(CalendarSyncWorker.UNIQUE_PERIODIC_WORK)
    }

    const val DEFAULT_SYNC_INTERVAL_MINS = 15
    const val MIN_SYNC_INTERVAL_MINS = 15
}
