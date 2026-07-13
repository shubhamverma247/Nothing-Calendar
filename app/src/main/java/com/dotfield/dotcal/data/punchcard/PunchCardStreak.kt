package com.dotfield.dotcal.data.punchcard

import java.time.LocalDate

object PunchCardStreak {
    const val Namespace = "punchcard"

    fun compute(punchedDays: Set<LocalDate>, endingAt: LocalDate): Int {
        var streak = 0
        var cursor = endingAt
        while (cursor in punchedDays) {
            streak += 1
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
