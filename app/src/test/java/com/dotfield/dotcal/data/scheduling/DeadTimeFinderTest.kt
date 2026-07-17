package com.dotfield.dotcal.data.scheduling

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Test

class DeadTimeFinderTest {
    private val startDate = LocalDate.of(2026, 7, 16)

    @Test
    fun findsAtLeastSixtyMinuteSlotsAcrossNextSevenDays() {
        val result = DeadTimeFinder.find(
            startDate = startDate,
            busyPeriods = listOf(
                busy("2026-07-16T08:00", "2026-07-16T09:30"),
                busy("2026-07-16T10:15", "2026-07-16T22:00"),
            ),
        )

        assertEquals(7, result.days.size)
        assertEquals(emptyList<FreeSlot>(), result.days.first().freeSlots)
        assertEquals(
            FreeSlot(startDate.plusDays(1), LocalTime.of(8, 0), LocalTime.of(22, 0)),
            result.slots.first(),
        )
        assertEquals(6, result.slots.size)
    }

    @Test
    fun customBoundsArePassedToSharedEngine() {
        val result = DeadTimeFinder.find(
            startDate = startDate,
            busyPeriods = listOf(busy("2026-07-16T10:00", "2026-07-16T12:00")),
            startHour = 9,
            endHour = 18,
        )

        assertEquals(
            listOf(
                FreeSlot(startDate, LocalTime.of(9, 0), LocalTime.of(10, 0)),
                FreeSlot(startDate, LocalTime.of(12, 0), LocalTime.of(18, 0)),
            ),
            result.days.first().freeSlots,
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsInvalidBounds() {
        DeadTimeFinder.find(
            startDate = startDate,
            busyPeriods = emptyList(),
            startHour = 18,
            endHour = 9,
        )
    }

    private fun busy(start: String, end: String) = BusyPeriod(
        start = LocalDateTime.parse(start),
        end = LocalDateTime.parse(end),
    )
}
