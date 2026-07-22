package com.dotfield.dotcal.data.insights

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OnThisDayFinderTest {
    @Test
    fun surfacesSameMonthDayFromPreviousYears() {
        val target = LocalDate.of(2026, 7, 19)
        val memories = OnThisDayFinder.find(
            target,
            listOf(
                candidate("a", "Trip to Goa", LocalDate.of(2024, 7, 19)),
                candidate("b", "Wrong day", LocalDate.of(2023, 7, 18)),
                candidate("c", "Wrong month", LocalDate.of(2022, 8, 19)),
            ),
        )

        assertEquals(listOf("Trip to Goa"), memories.map { it.title })
        assertEquals(2, memories.first().yearsAgo)
        assertEquals("2 years ago today", memories.first().subtitle)
    }

    @Test
    fun singularYearCopy() {
        val memories = OnThisDayFinder.find(
            LocalDate.of(2026, 3, 1),
            listOf(candidate("a", "New job", LocalDate.of(2025, 3, 1))),
        )

        assertEquals("1 year ago today", memories.single().subtitle)
    }

    @Test
    fun ignoresCurrentYearAndFutureEntries() {
        val memories = OnThisDayFinder.find(
            LocalDate.of(2026, 7, 19),
            listOf(
                candidate("today", "Same year", LocalDate.of(2026, 7, 19)),
                candidate("future", "Next year", LocalDate.of(2027, 7, 19)),
            ),
        )

        assertTrue(memories.isEmpty())
    }

    @Test
    fun birthdayWithKnownYearShowsAgeTurning() {
        val memories = OnThisDayFinder.find(
            LocalDate.of(2026, 5, 10),
            listOf(candidate("bday", "Asha's Birthday", LocalDate.of(1999, 5, 10), isBirthday = true, birthYearKnown = true)),
        )

        assertEquals(27, memories.single().ageTurning)
        assertEquals("turns 27", memories.single().subtitle)
    }

    @Test
    fun birthdayWithUnknownYearHidesAge() {
        val memories = OnThisDayFinder.find(
            LocalDate.of(2026, 5, 10),
            listOf(candidate("bday", "Asha's Birthday", LocalDate.of(2000, 5, 10), isBirthday = true, birthYearKnown = false)),
        )

        assertNull(memories.single().ageTurning)
        assertEquals("Birthday today", memories.single().subtitle)
    }

    @Test
    fun leapDayMemorySurfacesOnFeb28InNonLeapYear() {
        val memories = OnThisDayFinder.find(
            LocalDate.of(2026, 2, 28),
            listOf(candidate("leap", "Leap party", LocalDate.of(2024, 2, 29))),
        )

        assertEquals(listOf("Leap party"), memories.map { it.title })
        assertEquals(2, memories.single().yearsAgo)
    }

    @Test
    fun leapDayMemoryUsesExactMatchInLeapYear() {
        val onFeb28 = OnThisDayFinder.find(
            LocalDate.of(2028, 2, 28),
            listOf(candidate("leap", "Leap party", LocalDate.of(2024, 2, 29))),
        )
        val onFeb29 = OnThisDayFinder.find(
            LocalDate.of(2028, 2, 29),
            listOf(candidate("leap", "Leap party", LocalDate.of(2024, 2, 29))),
        )

        // In a leap year the memory belongs on 29 Feb only, not on 28 Feb.
        assertTrue(onFeb28.isEmpty())
        assertEquals(listOf("Leap party"), onFeb29.map { it.title })
    }

    @Test
    fun birthdaysSortBeforeRegularMemoriesThenByRecency() {
        val memories = OnThisDayFinder.find(
            LocalDate.of(2026, 6, 15),
            listOf(
                candidate("old", "Old memory", LocalDate.of(2020, 6, 15)),
                candidate("recent", "Recent memory", LocalDate.of(2025, 6, 15)),
                candidate("bday", "Ravi's Birthday", LocalDate.of(1990, 6, 15), isBirthday = true, birthYearKnown = true),
            ),
        )

        assertEquals(listOf("Ravi's Birthday", "Recent memory", "Old memory"), memories.map { it.title })
    }

    private fun candidate(
        id: String,
        title: String,
        originalDate: LocalDate,
        isBirthday: Boolean = false,
        birthYearKnown: Boolean = false,
    ) = OnThisDayCandidate(
        eventId = id,
        title = title,
        originalDate = originalDate,
        isBirthday = isBirthday,
        birthYearKnown = birthYearKnown,
    )
}
