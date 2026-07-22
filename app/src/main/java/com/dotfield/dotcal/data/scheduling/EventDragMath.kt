package com.dotfield.dotcal.data.scheduling

import java.time.Duration
import java.time.LocalDateTime
import kotlin.math.roundToInt

data class EventTimeRange(
    val start: LocalDateTime,
    val end: LocalDateTime,
)

object EventDragMath {
    private const val SNAP_MINUTES = 15L

    fun snapMinutes(deltaPixels: Float, hourHeightPixels: Float): Int {
        if (hourHeightPixels <= 0f) return 0
        val snapPixels = hourHeightPixels / (60f / SNAP_MINUTES)
        return ((deltaPixels / snapPixels).roundToInt() * SNAP_MINUTES).toInt()
    }

    fun move(
        start: LocalDateTime,
        end: LocalDateTime,
        minuteDelta: Int,
        dayDelta: Int,
    ): EventTimeRange {
        val minutes = minuteDelta.toLong()
        val days = dayDelta.toLong()
        return EventTimeRange(
            start = start.plusDays(days).plusMinutes(minutes),
            end = end.plusDays(days).plusMinutes(minutes),
        )
    }

    fun resizeStart(start: LocalDateTime, end: LocalDateTime, minuteDelta: Int): EventTimeRange {
        val latestStart = end.minusMinutes(SNAP_MINUTES)
        return EventTimeRange(start.plusMinutes(minuteDelta.toLong()).coerceAtMost(latestStart), end)
    }

    fun resizeEnd(start: LocalDateTime, end: LocalDateTime, minuteDelta: Int): EventTimeRange {
        val earliestEnd = start.plusMinutes(SNAP_MINUTES)
        return EventTimeRange(start, end.plusMinutes(minuteDelta.toLong()).coerceAtLeast(earliestEnd))
    }

    fun applyOccurrenceChangeToSeries(
        master: EventTimeRange,
        occurrence: EventTimeRange,
        target: EventTimeRange,
    ): EventTimeRange {
        val startDelta = Duration.between(occurrence.start, target.start)
        val endDelta = Duration.between(occurrence.end, target.end)
        return EventTimeRange(
            start = master.start.plus(startDelta),
            end = master.end.plus(endDelta),
        )
    }
}
