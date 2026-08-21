package com.dotfield.dotcal.ui

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dotfield.dotcal.data.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

class CalendarLayoutConstraintTest {
    @Test
    fun monthCellMetricsNeverReturnNegativeDimensionsWhenWeekNumberColumnOverflows() {
        val metrics = monthDayCellMetrics(
            maxWidth = 20.dp,
            maxHeight = 600.dp,
            weekNumberWidth = 36.dp,
        )

        assertEquals(0.dp, metrics.width)
        assertEquals(0.dp, metrics.height)
        assertEquals(2, metrics.visibleChipCount)
    }

    @Test
    fun monthCellMetricsHandleUnboundedHeight() {
        val metrics = monthDayCellMetrics(
            maxWidth = 350.dp,
            maxHeight = Dp.Infinity,
            weekNumberWidth = 0.dp,
        )

        assertEquals(50.dp, metrics.width)
        assertEquals(50.dp, metrics.height)
        assertEquals(2, metrics.visibleChipCount)
    }

    @Test
    fun timedEventHeightClampsCorruptLongDurationsToOneDay() {
        val height = safeTimedEventHeight(
            durationMinutes = Long.MAX_VALUE,
            hourHeightDp = 72f,
            minimumHeightDp = 22f,
        )

        assertEquals(1728.dp, height)
    }

    @Test
    fun timedEventHeightFallsBackWhenHourHeightIsInvalid() {
        val height = safeTimedEventHeight(
            durationMinutes = 60,
            hourHeightDp = Float.NaN,
            minimumHeightDp = 22f,
        )

        assertEquals(22.dp, height)
    }

    @Test
    fun minuteOfDayRejectsCorruptStoredValues() {
        assertNull(minuteOfDayToLocalTimeOrNull(-1))
        assertNull(minuteOfDayToLocalTimeOrNull(24 * 60))
        assertNull(minuteOfDayToLocalTimeOrNull(null))
    }

    @Test
    fun minuteOfDayConvertsValidStoredValues() {
        assertEquals(LocalTime.of(0, 0), minuteOfDayToLocalTimeOrNull(0))
        assertEquals(LocalTime.of(23, 59), minuteOfDayToLocalTimeOrNull(23 * 60 + 59))
    }

    @Test
    fun googleAllDayMultiDayEventIsVisibleOnEachCoveredDate() {
        val event = testEvent(
            start = LocalDate.of(2026, 8, 21).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            end = LocalDate.of(2026, 8, 23).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            isAllDay = 1,
            source = "GOOGLE",
        )

        val eventsByDate = eventsByVisibleDate(listOf(event))

        assertEquals(listOf(event), eventsByDate[LocalDate.of(2026, 8, 21)])
        assertEquals(listOf(event), eventsByDate[LocalDate.of(2026, 8, 22)])
        assertNull(eventsByDate[LocalDate.of(2026, 8, 23)])
    }

    @Test
    fun multiDayEventTitleIncludesVisibleDayPosition() {
        val event = testEvent(
            start = LocalDate.of(2026, 8, 21).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            end = LocalDate.of(2026, 8, 23).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            isAllDay = 1,
            source = "GOOGLE",
        )

        assertEquals("Multi-day (Day 1/2)", event.titleForVisibleDate(LocalDate.of(2026, 8, 21)))
        assertEquals("Multi-day (Day 2/2)", event.titleForVisibleDate(LocalDate.of(2026, 8, 22)))
    }

    @Test
    fun weekStripShowsMultiDayTitleOnlyOnFirstVisibleWeekSegment() {
        val weekDays = (0L..6L).map { LocalDate.of(2026, 8, 10).plusDays(it) }
        val event = testEvent(
            start = LocalDate.of(2026, 8, 12).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            end = LocalDate.of(2026, 8, 15).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            isAllDay = 1,
            source = "GOOGLE",
        )

        val titleDays = weekDays.filter { event.shouldShowStripTitle(it, weekDays) }

        assertEquals(listOf(LocalDate.of(2026, 8, 12)), titleDays)
    }

    @Test
    fun weekStripShowsCarryOverMultiDayTitleOnFirstVisibleWeekDay() {
        val weekDays = (0L..6L).map { LocalDate.of(2026, 8, 10).plusDays(it) }
        val event = testEvent(
            start = LocalDate.of(2026, 8, 9).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            end = LocalDate.of(2026, 8, 12).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            isAllDay = 1,
            source = "GOOGLE",
        )

        val titleDays = weekDays.filter { event.shouldShowStripTitle(it, weekDays) }

        assertEquals(listOf(LocalDate.of(2026, 8, 10)), titleDays)
    }

    @Test
    fun monthStripShowsMultiDayTitleOnlyOnceWhenDatesShareRow() {
        val monthRow = (0L..6L).map { LocalDate.of(2026, 8, 10).plusDays(it) }
        val event = testEvent(
            start = LocalDate.of(2026, 8, 12).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            end = LocalDate.of(2026, 8, 15).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            isAllDay = 1,
            source = "GOOGLE",
        )

        val titleDays = monthRow.filter { event.shouldShowStripTitle(it, monthRow) }

        assertEquals(listOf(LocalDate.of(2026, 8, 12)), titleDays)
    }

    @Test
    fun monthStripShowsCarryOverTitleAtStartOfNextRow() {
        val nextMonthRow = (0L..6L).map { LocalDate.of(2026, 8, 17).plusDays(it) }
        val event = testEvent(
            start = LocalDate.of(2026, 8, 15).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            end = LocalDate.of(2026, 8, 19).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli(),
            isAllDay = 1,
            source = "GOOGLE",
        )

        val titleDays = nextMonthRow.filter { event.shouldShowStripTitle(it, nextMonthRow) }

        assertEquals(listOf(LocalDate.of(2026, 8, 17)), titleDays)
    }

    @Test
    fun timedOvernightEventIsVisibleOnStartAndEndDate() {
        val event = testEvent(
            start = LocalDate.of(2026, 8, 21).atTime(23, 0).toInstant(ZoneOffset.UTC).toEpochMilli(),
            end = LocalDate.of(2026, 8, 22).atTime(1, 0).toInstant(ZoneOffset.UTC).toEpochMilli(),
            isAllDay = 0,
            source = "LOCAL",
        )

        val eventsByDate = eventsByVisibleDate(listOf(event))

        assertEquals(listOf(event), eventsByDate[LocalDate.of(2026, 8, 21)])
        assertEquals(listOf(event), eventsByDate[LocalDate.of(2026, 8, 22)])
    }

    private fun testEvent(
        start: Long,
        end: Long,
        isAllDay: Int,
        source: String,
    ): CalendarEvent {
        return CalendarEvent(
            id = "event-$start",
            accountId = "local",
            title = "Multi-day",
            startTimeMs = start,
            endTimeMs = end,
            timeZone = "UTC",
            isAllDay = isAllDay,
            colorHex = "#34A853",
            rrule = null,
            source = source,
            googleEventId = null,
            googleCalendarId = null,
            completedAtMs = null,
            voiceNotePath = null,
            createdAtMs = start,
            updatedAtMs = start,
        )
    }
}
