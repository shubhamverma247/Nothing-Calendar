package com.dotfield.dotcal.data.insights

import java.time.LocalDate

/**
 * A past calendar entry considered for "On This Day". [originalDate] is the local date the
 * event first happened (master-row start, in the device zone). [birthYearKnown] is only
 * meaningful for birthday-type entries — contact birthdays with an unknown year use a base
 * placeholder year, so they should not surface a bogus age.
 */
data class OnThisDayCandidate(
    val eventId: String,
    val title: String,
    val originalDate: LocalDate,
    val isBirthday: Boolean,
    val birthYearKnown: Boolean,
)

data class OnThisDayMemory(
    val eventId: String,
    val title: String,
    val originalDate: LocalDate,
    val yearsAgo: Int,
    /** Age the person turns this year, or null when it cannot be computed. */
    val ageTurning: Int?,
    val isBirthday: Boolean,
) {
    val subtitle: String = OnThisDayFinder.describe(yearsAgo, ageTurning, isBirthday)
}

/**
 * Pure, UI-free "On This Day" matcher. Surfaces entries from previous years that fall on the
 * same month/day as the target date. Handles the 29 Feb edge (a leap-day memory appears on
 * 28 Feb in non-leap years) and birthday anniversary math ("turns 27") when the birth year
 * is known.
 */
object OnThisDayFinder {
    fun find(targetDate: LocalDate, candidates: List<OnThisDayCandidate>): List<OnThisDayMemory> {
        return candidates
            .asSequence()
            .filter { it.matchesDay(targetDate) }
            .mapNotNull { candidate ->
                val yearsAgo = targetDate.year - candidate.originalDate.year
                if (yearsAgo < 1) return@mapNotNull null
                val ageTurning = if (candidate.isBirthday && candidate.birthYearKnown) yearsAgo else null
                OnThisDayMemory(
                    eventId = candidate.eventId,
                    title = candidate.title,
                    originalDate = candidate.originalDate,
                    yearsAgo = yearsAgo,
                    ageTurning = ageTurning,
                    isBirthday = candidate.isBirthday,
                )
            }
            .sortedWith(
                compareByDescending<OnThisDayMemory> { it.isBirthday }
                    .thenBy { it.yearsAgo }
                    .thenBy { it.title.lowercase() },
            )
            .toList()
    }

    fun describe(yearsAgo: Int, ageTurning: Int?, isBirthday: Boolean): String {
        return when {
            ageTurning != null -> "turns $ageTurning"
            isBirthday -> "Birthday today"
            yearsAgo == 1 -> "1 year ago today"
            else -> "$yearsAgo years ago today"
        }
    }

    private fun OnThisDayCandidate.matchesDay(target: LocalDate): Boolean {
        val date = originalDate
        if (date.monthValue == target.monthValue && date.dayOfMonth == target.dayOfMonth) return true
        // A 29 Feb memory has no exact match in a non-leap year — surface it on 28 Feb instead.
        return date.monthValue == 2 && date.dayOfMonth == 29 &&
            target.monthValue == 2 && target.dayOfMonth == 28 && !target.isLeapYear
    }
}
