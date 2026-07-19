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

    @Test
    fun allDayAndGhostEventsBlockDeadTimeSlots() {
        val result = DeadTimeFinder.find(
            startDate = startDate,
            busyPeriods = listOf(
                busy("2026-07-16T00:00", "2026-07-17T00:00", isAllDay = true),
                busy("2026-07-17T08:00", "2026-07-17T22:00", isGhost = true),
            ),
        )

        assertEquals(emptyList<FreeSlot>(), result.days[0].freeSlots)
        assertEquals(emptyList<FreeSlot>(), result.days[1].freeSlots)
        assertEquals(
            FreeSlot(startDate.plusDays(2), LocalTime.of(8, 0), LocalTime.of(22, 0)),
            result.slots.first(),
        )
    }

    @Test
    fun searchesExactlySevenDaysFromStartDate() {
        val result = DeadTimeFinder.find(
            startDate = startDate,
            busyPeriods = emptyList(),
        )

        assertEquals(7, result.days.size)
        assertEquals(startDate, result.days.first().date)
        assertEquals(startDate.plusDays(6), result.days.last().date)
        assertEquals(7, result.slots.size)
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

    private fun busy(
        start: String,
        end: String,
        isAllDay: Boolean = false,
        isGhost: Boolean = false,
    ) = BusyPeriod(
        start = LocalDateTime.parse(start),
        end = LocalDateTime.parse(end),
        isAllDay = isAllDay,
        isGhost = isGhost,
    )
}
