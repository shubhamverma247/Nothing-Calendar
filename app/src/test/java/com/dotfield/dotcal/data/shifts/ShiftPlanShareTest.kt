package com.dotfield.dotcal.data.shifts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class ShiftPlanShareTest {
    private val zone = ZoneId.of("Asia/Kolkata")
    private val start = LocalDate.of(2026, 8, 10)
    private val day = shiftType("day", "Day", 7 * 60, 12 * 60)
    private val night = shiftType("night", "Night", 22 * 60, 10 * 60)
    private val off = shiftType("off", "Off", null, null)
    private val allDay = shiftType("training", "Training", null, null, isAllDay = true)

    @Test
    fun shareEventsSkipOffDaysAndUseStableIds() {
        val pattern = pattern(listOf(day.id, off.id, night.id))

        val events = buildShiftPlanShareEvents(
            pattern = pattern,
            shiftTypes = listOf(day, off, night).associateBy { it.id },
            rangeStart = start,
            rangeEnd = start.plusDays(2),
            nowMs = 1L,
            zoneId = zone,
        )

        assertEquals(2, events.size)
        assertEquals("shift-plan-pattern-1-2026-08-10-day", events[0].id)
        assertEquals("Day", events[0].title)
        assertEquals("shift-plan-pattern-1-2026-08-12-night", events[1].id)
        assertEquals("Night", events[1].title)
    }

    @Test
    fun overnightShareEventEndsOnNextDay() {
        val pattern = pattern(listOf(night.id))

        val event = buildShiftPlanShareEvents(
            pattern = pattern,
            shiftTypes = mapOf(night.id to night),
            rangeStart = start,
            rangeEnd = start,
            zoneId = zone,
        ).single()

        assertEquals(ms(LocalDateTime.of(2026, 8, 10, 22, 0)), event.startTimeMs)
        assertEquals(ms(LocalDateTime.of(2026, 8, 11, 8, 0)), event.endTimeMs)
        assertEquals(0, event.isAllDay)
    }

    @Test
    fun allDayShareEventUsesExclusiveIcsStyleEnd() {
        val pattern = pattern(listOf(allDay.id))

        val event = buildShiftPlanShareEvents(
            pattern = pattern,
            shiftTypes = mapOf(allDay.id to allDay),
            rangeStart = start,
            rangeEnd = start,
            zoneId = zone,
        ).single()

        assertEquals(ms(LocalDateTime.of(2026, 8, 10, 0, 0)), event.startTimeMs)
        assertEquals(ms(LocalDateTime.of(2026, 8, 11, 0, 0)), event.endTimeMs)
        assertEquals(1, event.isAllDay)
    }

    @Test
    fun reversedRangeReturnsNoShareEvents() {
        val events = buildShiftPlanShareEvents(
            pattern = pattern(listOf(day.id)),
            shiftTypes = mapOf(day.id to day),
            rangeStart = start.plusDays(1),
            rangeEnd = start,
            zoneId = zone,
        )

        assertTrue(events.isEmpty())
    }

    private fun ms(value: LocalDateTime): Long =
        value.atZone(zone).toInstant().toEpochMilli()

    private fun pattern(cycle: List<String>) = ShiftPattern(
        id = "pattern-1",
        name = "Pattern",
        cycleShiftTypeIds = cycle,
        cycleStartDate = start,
        createdAtMs = 1L,
    )

    private fun shiftType(
        id: String,
        name: String,
        startMinute: Int?,
        durationMinutes: Int?,
        isAllDay: Boolean = false,
    ) = ShiftType(
        id = id,
        name = name,
        colorHex = "#FF3B30",
        startMinuteOfDay = startMinute,
        durationMinutes = durationMinutes,
        isAllDay = isAllDay,
        reminderMinutes = null,
        createdAtMs = 1L,
    )
}
