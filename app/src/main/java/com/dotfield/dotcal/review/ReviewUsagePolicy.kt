package com.dotfield.dotcal.review

import kotlin.math.max

internal data class ReviewUsageState(
    val sessionCount: Int = 0,
    val createdItemCount: Int = 0,
    val meaningfulActionCount: Int = 0,
    val lastPromptMs: Long = 0L,
)

internal object ReviewUsagePolicy {
    const val COOLDOWN_MS = 90L * 24 * 60 * 60 * 1_000

    fun isEligible(state: ReviewUsageState, nowMs: Long): Boolean {
        val cooldownExpired = state.lastPromptMs <= 0L || nowMs - state.lastPromptMs >= COOLDOWN_MS
        return state.sessionCount >= 5 &&
            state.createdItemCount >= 1 &&
            state.meaningfulActionCount >= 1 &&
            cooldownExpired
    }

    fun increment(value: Int): Int = max(0, value) + 1
}
