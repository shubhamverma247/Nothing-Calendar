package com.dotfield.dotcal.review

import android.app.Activity
import android.content.Context
import androidx.datastore.preferences.core.edit
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

internal class ReviewUsageStore(
    private val dataSource: ReviewUsageDataSource,
    private val scope: CoroutineScope,
) {
    constructor(context: Context, scope: CoroutineScope) : this(
        dataSource = DataStoreReviewUsageDataSource(context.applicationContext),
        scope = scope,
    )

    private val requestInFlight = AtomicBoolean(false)

    suspend fun recordSession() = dataSource.incrementSession()

    suspend fun recordCreatedItem() = dataSource.incrementCreatedItem()

    suspend fun recordMeaningfulAction() = dataSource.incrementMeaningfulAction()

    suspend fun maybeRequestReview(activity: Activity, nowMs: Long = System.currentTimeMillis()) {
        maybeRequestReview(nowMs) { complete ->
            launchPlayReview(activity, complete)
        }
    }

    internal suspend fun maybeRequestReview(
        nowMs: Long,
        requestReview: (complete: (Boolean) -> Unit) -> Unit,
    ) {
        if (!requestInFlight.compareAndSet(false, true)) return
        runCatching {
            val state = dataSource.state()
            if (!ReviewUsagePolicy.isEligible(state, nowMs)) {
                requestInFlight.set(false)
                return
            }
            requestReview { launched ->
                scope.launch {
                    runCatching {
                        if (launched) dataSource.markPrompted(nowMs)
                    }
                    requestInFlight.set(false)
                }
            }
        }.onFailure {
            requestInFlight.set(false)
        }
    }

    private fun launchPlayReview(activity: Activity, complete: (Boolean) -> Unit) {
        runCatching {
            val manager = ReviewManagerFactory.create(activity)
            manager.requestReviewFlow().addOnCompleteListener { request ->
                if (!request.isSuccessful) {
                    complete(false)
                    return@addOnCompleteListener
                }
                val reviewInfo = request.result ?: run {
                    complete(false)
                    return@addOnCompleteListener
                }
                manager.launchReviewFlow(activity, reviewInfo).addOnCompleteListener { launch ->
                    complete(launch.isSuccessful)
                }
            }
        }.onFailure {
            complete(false)
        }
    }
}

internal interface ReviewUsageDataSource {
    suspend fun state(): ReviewUsageState
    suspend fun incrementSession()
    suspend fun incrementCreatedItem()
    suspend fun incrementMeaningfulAction()
    suspend fun markPrompted(nowMs: Long)
}

private class DataStoreReviewUsageDataSource(
    private val context: Context,
) : ReviewUsageDataSource {
    override suspend fun state(): ReviewUsageState {
        val preferences = context.calendarPreferencesDataStore.data.first()
        return ReviewUsageState(
            sessionCount = preferences[CalendarPreferences.KEY_REVIEW_SESSION_COUNT] ?: 0,
            createdItemCount = preferences[CalendarPreferences.KEY_REVIEW_CREATED_ITEM_COUNT] ?: 0,
            meaningfulActionCount = preferences[CalendarPreferences.KEY_REVIEW_MEANINGFUL_ACTION_COUNT] ?: 0,
            lastPromptMs = preferences[CalendarPreferences.KEY_REVIEW_LAST_PROMPT_MS] ?: 0L,
        )
    }

    override suspend fun incrementSession() {
        increment(CalendarPreferences.KEY_REVIEW_SESSION_COUNT)
    }

    override suspend fun incrementCreatedItem() {
        increment(CalendarPreferences.KEY_REVIEW_CREATED_ITEM_COUNT)
    }

    override suspend fun incrementMeaningfulAction() {
        increment(CalendarPreferences.KEY_REVIEW_MEANINGFUL_ACTION_COUNT)
    }

    override suspend fun markPrompted(nowMs: Long) {
        context.calendarPreferencesDataStore.edit { preferences ->
            preferences[CalendarPreferences.KEY_REVIEW_LAST_PROMPT_MS] = nowMs
        }
    }

    private suspend fun increment(key: androidx.datastore.preferences.core.Preferences.Key<Int>) {
        context.calendarPreferencesDataStore.edit { preferences ->
            preferences[key] = ReviewUsagePolicy.increment(preferences[key] ?: 0)
        }
    }
}
