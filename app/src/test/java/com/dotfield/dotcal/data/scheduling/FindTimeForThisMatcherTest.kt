package com.dotfield.dotcal.data.scheduling

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class FindTimeForThisMatcherTest {
    private val monday = LocalDate.of(2026, 7, 13)
    private val now = LocalDateTime.of(monday, LocalTime.of(10, 10))

    @Test
    fun skipsPastAndShortSlots() {
        val suggestions = FindTimeForThisMatcher.find(
            days = listOf(
                day(
                    slot("09:00", "10:00"),
                    slot("10:30", "10:50"),
                    slot("11:00", "12:00"),
                ),
            ),
            durationMinutes = 45,
            now = now,
        )

        assertEquals(listOf(suggestion("11:00", "11:45", unusedMinutes = 15)), suggestions)
    }

    @Test
    fun trimsActiveSlotToNowWhenEnoughTimeRemains() {
        val suggestions = FindTimeForThisMatcher.find(
            days = listOf(day(slot("10:00", "11:30"))),
            durationMinutes = 45,
            now = now,
        )

        assertEquals(listOf(suggestion("10:10", "10:55", unusedMinutes = 35)), suggestions)
    }

    @Test
    fun ranksSoonerSlotsBeforeTighterLaterFits() {
        val suggestions = FindTimeForThisMatcher.find(
            days = listOf(
                day(
                    slot("13:00", "15:00"),
                    slot("16:00", "16:50"),
                ),
                day(slot("09:00", "09:45", monday.plusDays(1))),
            ),
            durationMinutes = 45,
            now = now,
        )

        assertEquals(
            listOf(
                suggestion("13:00", "13:45", unusedMinutes = 75),
                suggestion("16:00", "16:45", unusedMinutes = 5),
                suggestion("09:00", "09:45", monday.plusDays(1), unusedMinutes = 0),
            ),
            suggestions,
        )
    }

    @Test
    fun respectsLimitAfterRanking() {
        val suggestions = FindTimeForThisMatcher.find(
            days = listOf(
                day(
                    slot("11:00", "12:00"),
                    slot("13:00", "14:00"),
                    slot("15:00", "16:00"),
                ),
            ),
            durationMinutes = 30,
            now = now,
            limit = 2,
        )

        assertEquals(
            listOf(
                suggestion("11:00", "11:30", unusedMinutes = 30),
                suggestion("13:00", "13:30", unusedMinutes = 30),
            ),
            suggestions,
        )
    }

    private fun day(vararg slots: FreeSlot) = DayAvailability(
        date = slots.firstOrNull()?.date ?: monday,
        workingStart = LocalTime.of(9, 0),
        workingEnd = LocalTime.of(21, 0),
        freeSlots = slots.toList(),
    )

    private fun slot(start: String, end: String, date: LocalDate = monday) = FreeSlot(
        date = date,
        start = LocalTime.parse(start),
        end = LocalTime.parse(end),
    )

    private fun suggestion(
        start: String,
        end: String,
        date: LocalDate = monday,
        unusedMinutes: Long,
    ) = FindTimeSuggestion(
        slot = FreeSlot(date, LocalTime.parse(start), LocalTime.parse(end)),
        unusedMinutes = unusedMinutes,
    )
}
