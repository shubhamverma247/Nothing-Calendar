package com.dotfield.dotcal.review

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield

class ReviewUsagePolicyTest {
    @Test
    fun requiresFiveSessionsCreatedItemAndMeaningfulAction() {
        assertFalse(ReviewUsagePolicy.isEligible(ReviewUsageState(4, 1, 1), nowMs = 1_000L))
        assertFalse(ReviewUsagePolicy.isEligible(ReviewUsageState(5, 0, 1), nowMs = 1_000L))
        assertFalse(ReviewUsagePolicy.isEligible(ReviewUsageState(5, 1, 0), nowMs = 1_000L))
        assertTrue(ReviewUsagePolicy.isEligible(ReviewUsageState(5, 1, 1), nowMs = 1_000L))
    }

    @Test
    fun cooldownBlocksPromptForNinetyDays() {
        val promptedAt = 1_000L
        val state = ReviewUsageState(5, 1, 1, lastPromptMs = promptedAt)

        assertFalse(ReviewUsagePolicy.isEligible(state, promptedAt + 90L * 24 * 60 * 60 * 1_000 - 1))
        assertTrue(ReviewUsagePolicy.isEligible(state, promptedAt + 90L * 24 * 60 * 60 * 1_000))
    }

    @Test
    fun failedLaunchDoesNotPersistCooldownAndAllowsRetry() = runBlocking {
        val dataSource = FakeReviewUsageDataSource(ReviewUsageState(5, 1, 1))
        val store = ReviewUsageStore(dataSource, this)
        var launchCount = 0

        store.maybeRequestReview(nowMs = 1_000L) { complete ->
            launchCount += 1
            complete(false)
        }
        yield()

        assertEquals(0L, dataSource.currentState.lastPromptMs)

        store.maybeRequestReview(nowMs = 2_000L) { complete ->
            launchCount += 1
            complete(true)
        }
        yield()

        assertEquals(2, launchCount)
        assertEquals(2_000L, dataSource.currentState.lastPromptMs)
    }

    @Test
    fun ignoresConcurrentPromptWhileLaunchInFlight() = runBlocking {
        val dataSource = FakeReviewUsageDataSource(ReviewUsageState(5, 1, 1))
        val store = ReviewUsageStore(dataSource, this)
        var launchCount = 0
        var pendingComplete: ((Boolean) -> Unit)? = null

        store.maybeRequestReview(nowMs = 1_000L) { complete ->
            launchCount += 1
            pendingComplete = complete
        }
        store.maybeRequestReview(nowMs = 1_001L) { complete ->
            launchCount += 1
            complete(true)
        }

        assertEquals(1, launchCount)

        pendingComplete?.invoke(true)
        yield()

        assertEquals(1_000L, dataSource.currentState.lastPromptMs)
    }

    @Test
    fun stateReadFailureIsSilent() = runBlocking {
        val store = ReviewUsageStore(FailingReviewUsageDataSource, this)
        var launchCount = 0

        store.maybeRequestReview(nowMs = 1_000L) { complete ->
            launchCount += 1
            complete(true)
        }

        assertEquals(0, launchCount)
    }

    private class FakeReviewUsageDataSource(
        var currentState: ReviewUsageState,
    ) : ReviewUsageDataSource {
        override suspend fun state(): ReviewUsageState = currentState

        override suspend fun incrementSession() {
            currentState = currentState.copy(sessionCount = currentState.sessionCount + 1)
        }

        override suspend fun incrementCreatedItem() {
            currentState = currentState.copy(createdItemCount = currentState.createdItemCount + 1)
        }

        override suspend fun incrementMeaningfulAction() {
            currentState = currentState.copy(meaningfulActionCount = currentState.meaningfulActionCount + 1)
        }

        override suspend fun markPrompted(nowMs: Long) {
            currentState = currentState.copy(lastPromptMs = nowMs)
        }
    }

    private object FailingReviewUsageDataSource : ReviewUsageDataSource {
        override suspend fun state(): ReviewUsageState = error("DataStore read failed")
        override suspend fun incrementSession() = Unit
        override suspend fun incrementCreatedItem() = Unit
        override suspend fun incrementMeaningfulAction() = Unit
        override suspend fun markPrompted(nowMs: Long) = Unit
    }
}
