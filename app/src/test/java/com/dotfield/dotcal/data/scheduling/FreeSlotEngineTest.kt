package com.dotfield.dotcal.data.scheduling

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FreeSlotEngineTest {
    private val monday = LocalDate.of(2026, 7, 13)

    @Test
    fun overlappingEventsAreMergedIntoFreeGaps() {
        val result = compute(
            busy(
                "2026-07-13T10:00",
                "2026-07-13T12:00",
            ),
            busy(
                "2026-07-13T11:30",
                "2026-07-13T14:00",
            ),
        ).single()

        assertEquals(
            listOf(slot("09:00", "10:00"), slot("14:00", "21:00")),
            result.freeSlots,
        )
    }

    @Test
    fun adjacentEventsAreMergedWithoutCreatingAFalseGap() {
        val result = compute(
            busy("2026-07-13T10:00", "2026-07-13T12:00"),
            busy("2026-07-13T12:00", "2026-07-13T14:00"),
        ).single()

        assertEquals(
            listOf(slot("09:00", "10:00"), slot("14:00", "21:00")),
            result.freeSlots,
        )
    }

    @Test
    fun allDayEventsCanBlockOrBeIgnored() {
        val allDay = BusyPeriod(
            start = monday.atStartOfDay(),
            end = monday.plusDays(1).atStartOfDay(),
            isAllDay = true,
        )

        assertTrue(compute(allDay).single().isFullyBusy)
        assertTrue(compute(allDay, blockAllDay = false).single().isFreeAllDay)
    }

    @Test
    fun eventCrossingMidnightIsClippedToEachWorkingWindow() {
        val request = request(rangeEnd = monday.plusDays(1))
        val result = FreeSlotEngine.compute(
            request,
            listOf(busy("2026-07-13T20:00", "2026-07-14T10:00")),
        )

        assertEquals(listOf(slot("09:00", "20:00", monday)), result[0].freeSlots)
        assertEquals(listOf(slot("10:00", "21:00", monday.plusDays(1))), result[1].freeSlots)
    }

    @Test
    fun emptyCalendarReturnsFreeAllDayForEveryDate() {
        val result = FreeSlotEngine.compute(request(rangeEnd = monday.plusDays(2)), emptyList())

        assertEquals(3, result.size)
        assertTrue(result.all { it.isFreeAllDay })
    }

    @Test
    fun fullWorkingDayBusyReturnsNoSlots() {
        val result = compute(busy("2026-07-13T08:00", "2026-07-13T22:00")).single()

        assertTrue(result.isFullyBusy)
    }

    @Test
    fun gapsShorterThanMinimumAreRemoved() {
        val result = FreeSlotEngine.compute(
            request(minimumMinutes = 60),
            listOf(
                busy("2026-07-13T09:30", "2026-07-13T12:00"),
                busy("2026-07-13T12:45", "2026-07-13T21:00"),
            ),
        ).single()

        assertTrue(result.freeSlots.isEmpty())
    }

    @Test
    fun ghostEventsCanBeTreatedAsFree() {
        val ghost = busy("2026-07-13T12:00", "2026-07-13T13:00", isGhost = true)

        assertEquals(2, compute(ghost).single().freeSlots.size)
        assertTrue(
            FreeSlotEngine.compute(request(treatGhostsAsBusy = false), listOf(ghost))
                .single()
                .isFreeAllDay,
        )
    }

    @Test
    fun textFormatterCoversCompactRulesAndBusyDays() {
        val request = request(rangeEnd = monday.plusDays(3))
        val days = FreeSlotEngine.compute(
            request,
            listOf(
                busy("2026-07-13T09:00", "2026-07-13T14:00"),
                busy("2026-07-13T17:30", "2026-07-13T21:00"),
                busy("2026-07-15T11:00", "2026-07-15T16:00"),
                busy("2026-07-16T08:00", "2026-07-16T22:00"),
            ),
        )

        assertEquals(
            """
            My availability (Mon 13 - Thu 16 Jul):
            Mon: 2:00 pm-5:30 pm
            Tue: free all day
            Wed: before 11 am, after 4 pm
            Thu: fully booked
            """.trimIndent(),
            AvailabilityTextFormatter.format(days, use24HourFormat = false, locale = Locale.US),
        )
    }

    @Test
    fun textFormatterUses24HourTime() {
        val days = compute(busy("2026-07-13T11:00", "2026-07-13T16:00"))

        assertTrue(
            AvailabilityTextFormatter.format(days, use24HourFormat = true, locale = Locale.US)
                .contains("before 11:00, after 16:00"),
        )
    }

    private fun compute(
        vararg periods: BusyPeriod,
        blockAllDay: Boolean = true,
    ): List<DayAvailability> = FreeSlotEngine.compute(
        request(blockAllDay = blockAllDay),
        periods.toList(),
    )

    private fun request(
        rangeEnd: LocalDate = monday,
        minimumMinutes: Int = 30,
        blockAllDay: Boolean = true,
        treatGhostsAsBusy: Boolean = true,
    ) = FreeSlotRequest(
        rangeStart = monday,
        rangeEnd = rangeEnd,
        minimumSlotMinutes = minimumMinutes,
        blockAllDayEvents = blockAllDay,
        treatGhostsAsBusy = treatGhostsAsBusy,
    )

    private fun busy(start: String, end: String, isGhost: Boolean = false) = BusyPeriod(
        LocalDateTime.parse(start),
        LocalDateTime.parse(end),
        isGhost = isGhost,
    )

    private fun slot(
        start: String,
        end: String,
        date: LocalDate = monday,
    ) = FreeSlot(date, LocalTime.parse(start), LocalTime.parse(end))
}
