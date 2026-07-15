package com.dotfield.dotcal.data.scheduling

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class EventDragMathTest {
    private val start = LocalDateTime.of(2026, 7, 15, 14, 0)
    private val end = LocalDateTime.of(2026, 7, 15, 15, 0)

    @Test
    fun snapMinutesUsesFifteenMinuteIncrements() {
        assertEquals(15, EventDragMath.snapMinutes(deltaPixels = 12f, hourHeightPixels = 48f))
        assertEquals(-30, EventDragMath.snapMinutes(deltaPixels = -24f, hourHeightPixels = 48f))
        assertEquals(0, EventDragMath.snapMinutes(deltaPixels = 5f, hourHeightPixels = 48f))
    }

    @Test
    fun moveCanCrossDayColumnsAndPreservesDuration() {
        val result = EventDragMath.move(
            start = start,
            end = end,
            minuteDelta = 90,
            dayDelta = 2,
        )

        assertEquals(LocalDateTime.of(2026, 7, 17, 15, 30), result.start)
        assertEquals(LocalDateTime.of(2026, 7, 17, 16, 30), result.end)
    }

    @Test
    fun resizeStartNeverPassesMinimumDuration() {
        val result = EventDragMath.resizeStart(start, end, minuteDelta = 90)

        assertEquals(LocalDateTime.of(2026, 7, 15, 14, 45), result.start)
        assertEquals(end, result.end)
    }

    @Test
    fun resizeEndNeverPassesMinimumDuration() {
        val result = EventDragMath.resizeEnd(start, end, minuteDelta = -90)

        assertEquals(start, result.start)
        assertEquals(LocalDateTime.of(2026, 7, 15, 14, 15), result.end)
    }

    @Test
    fun wholeSeriesResizeStartAppliesOnlyStartDelta() {
        val master = EventTimeRange(
            start = LocalDateTime.of(2026, 7, 1, 14, 0),
            end = LocalDateTime.of(2026, 7, 1, 15, 0),
        )
        val occurrence = EventTimeRange(start, end)
        val target = EventTimeRange(start.plusMinutes(15), end)

        val result = EventDragMath.applyOccurrenceChangeToSeries(master, occurrence, target)

        assertEquals(LocalDateTime.of(2026, 7, 1, 14, 15), result.start)
        assertEquals(master.end, result.end)
    }

    @Test
    fun wholeSeriesResizeEndAppliesOnlyEndDelta() {
        val master = EventTimeRange(
            start = LocalDateTime.of(2026, 7, 1, 14, 0),
            end = LocalDateTime.of(2026, 7, 1, 15, 0),
        )
        val occurrence = EventTimeRange(start, end)
        val target = EventTimeRange(start, end.plusMinutes(30))

        val result = EventDragMath.applyOccurrenceChangeToSeries(master, occurrence, target)

        assertEquals(master.start, result.start)
        assertEquals(LocalDateTime.of(2026, 7, 1, 15, 30), result.end)
    }
}
