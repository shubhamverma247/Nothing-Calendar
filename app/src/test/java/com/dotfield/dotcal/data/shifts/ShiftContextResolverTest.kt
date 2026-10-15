package com.dotfield.dotcal.data.shifts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ShiftContextResolverTest {
    private val date = LocalDate.of(2026, 10, 16)

    @Test
    fun multipleActivePatternsRequireExplicitSelection() {
        val result = ShiftContextResolver.resolve(
            request(patterns = listOf(pattern("one"), pattern("two"))),
        )

        assertEquals(
            ShiftContextResolution.AmbiguousPatterns(listOf("one", "two")),
            result,
        )
    }

    @Test
    fun explicitPatternResolvesOffDay() {
        val result = ShiftContextResolver.resolve(
            request(
                patterns = listOf(pattern("one", listOf(SHIFT_OFF_TYPE_ID)), pattern("two")),
                selectedPatternId = "one",
            ),
        ) as ShiftContextResolution.Resolved

        assertEquals(ShiftDayContext.OffDay(date), result.days.single())
    }

    @Test
    fun overnightPatternShiftKeepsNextDayEnd() {
        val night = shiftType("night", startMinute = 22 * 60 + 30, durationMinutes = 7 * 60 + 45)
        val result = ShiftContextResolver.resolve(
            request(patterns = listOf(pattern("night-pattern", listOf(night.id))), shiftTypes = listOf(night)),
        ) as ShiftContextResolution.Resolved

        val context = result.days.single() as ShiftDayContext.Timed
        assertEquals(LocalDateTime.of(date, LocalTime.of(22, 30)), context.intervals.single().start)
        assertEquals(LocalDateTime.of(date.plusDays(1), LocalTime.of(6, 15)), context.intervals.single().end)
    }

    @Test
    fun actualQuickShiftOverridesPatternOffDay() {
        val actual = ShiftEventSignal(
            actualDate = date,
            start = date.atTime(7, 15),
            end = date.atTime(15, 45),
            isAllDay = false,
            generatedBy = ShiftEventGeneratedBy.Manual,
        )
        val result = ShiftContextResolver.resolve(
            request(patterns = listOf(pattern("pattern", listOf(SHIFT_OFF_TYPE_ID))), actualShifts = listOf(actual)),
        ) as ShiftContextResolution.Resolved

        val context = result.days.single() as ShiftDayContext.Timed
        assertEquals(ShiftInterval(actual.start!!, actual.end!!), context.intervals.single())
    }

    @Test
    fun editedGeneratedShiftUsesActualEventTimes() {
        val type = shiftType("day", startMinute = 8 * 60, durationMinutes = 8 * 60)
        val actual = ShiftEventSignal(
            actualDate = date,
            start = date.atTime(9, 10),
            end = date.atTime(17, 40),
            isAllDay = false,
            generatedBy = ShiftEventGeneratedBy.Pattern,
            patternId = "pattern",
        )
        val result = ShiftContextResolver.resolve(
            request(
                patterns = listOf(pattern("pattern", listOf(type.id))),
                shiftTypes = listOf(type),
                actualShifts = listOf(actual),
            ),
        ) as ShiftContextResolution.Resolved

        val context = result.days.single() as ShiftDayContext.Timed
        assertEquals(ShiftInterval(actual.start!!, actual.end!!), context.intervals.single())
    }

    @Test
    fun missingShiftTypeIsUnknownNotOffDay() {
        val result = ShiftContextResolver.resolve(
            request(patterns = listOf(pattern("pattern", listOf("deleted-type")))),
        ) as ShiftContextResolution.Resolved

        assertEquals(ShiftDayContext.Unknown(date), result.days.single())
    }

    @Test
    fun allDayShiftRemainsDistinctFromTimedAndOffDay() {
        val allDay = shiftType("all-day", startMinute = null, durationMinutes = null, isAllDay = true)
        val result = ShiftContextResolver.resolve(
            request(patterns = listOf(pattern("pattern", listOf(allDay.id))), shiftTypes = listOf(allDay)),
        ) as ShiftContextResolution.Resolved

        assertEquals(ShiftDayContext.AllDay(date), result.days.single())
    }

    @Test
    fun routineAnchorsResolveAroundShiftAndOnOffDay() {
        val timed = ShiftDayContext.Timed(
            date,
            listOf(ShiftInterval(date.atTime(22, 30), date.plusDays(1).atTime(6, 15))),
        )
        val offDay = ShiftDayContext.OffDay(date)

        assertEquals(
            date.atTime(22, 0),
            ShiftContextResolver.resolveRoutineTime(timed, ShiftRoutineRule.BeforeShiftStart(30)),
        )
        assertEquals(
            date.plusDays(1).atTime(6, 45),
            ShiftContextResolver.resolveRoutineTime(timed, ShiftRoutineRule.AfterShiftEnd(30)),
        )
        assertEquals(
            date.atTime(10, 0),
            ShiftContextResolver.resolveRoutineTime(offDay, ShiftRoutineRule.OnOffDay(LocalTime.of(10, 0))),
        )
        assertNull(ShiftContextResolver.resolveRoutineTime(offDay, ShiftRoutineRule.BeforeShiftStart(30)))
    }

    @Test
    fun archivedPatternsAreNotAutoSelected() {
        val result = ShiftContextResolver.resolve(
            request(patterns = listOf(pattern("archived").copy(archivedAtMs = 1L))),
        ) as ShiftContextResolution.Resolved

        assertNull(result.patternId)
        assertTrue(result.days.single() is ShiftDayContext.Unknown)
    }

    @Test
    fun generatedShiftFromDifferentPatternDoesNotOverrideSelectedPattern() {
        val otherPatternShift = ShiftEventSignal(
            actualDate = date,
            start = date.atTime(8, 0),
            end = date.atTime(16, 0),
            isAllDay = false,
            generatedBy = ShiftEventGeneratedBy.Pattern,
            patternId = "other",
            originalPatternDate = date,
        )
        val result = ShiftContextResolver.resolve(
            request(
                patterns = listOf(pattern("selected"), pattern("other")),
                actualShifts = listOf(otherPatternShift),
                selectedPatternId = "selected",
            ),
        ) as ShiftContextResolution.Resolved

        assertEquals(ShiftDayContext.OffDay(date), result.days.single())
    }

    @Test
    fun cancelledPatternDateIsSuppressedInsteadOfProjected() {
        val day = shiftType("day", startMinute = 8 * 60, durationMinutes = 8 * 60)
        val result = ShiftContextResolver.resolve(
            request(
                patterns = listOf(pattern("pattern", listOf(day.id))),
                shiftTypes = listOf(day),
                suppressedPatternDates = setOf(ShiftPatternDate("pattern", date)),
            ),
        ) as ShiftContextResolution.Resolved

        assertEquals(ShiftDayContext.Suppressed(date), result.days.single())
    }

    @Test
    fun movedGeneratedShiftSuppressesOriginalDateAndUsesActualDate() {
        val day = shiftType("day", startMinute = 8 * 60, durationMinutes = 8 * 60)
        val movedDate = date.plusDays(1)
        val moved = ShiftEventSignal(
            actualDate = movedDate,
            start = movedDate.atTime(10, 0),
            end = movedDate.atTime(18, 0),
            isAllDay = false,
            generatedBy = ShiftEventGeneratedBy.Pattern,
            patternId = "pattern",
            originalPatternDate = date,
        )
        val result = ShiftContextResolver.resolve(
            request(
                rangeEnd = movedDate,
                patterns = listOf(pattern("pattern", listOf(day.id))),
                shiftTypes = listOf(day),
                actualShifts = listOf(moved),
            ),
        ) as ShiftContextResolution.Resolved

        assertEquals(ShiftDayContext.Suppressed(date), result.days.first())
        assertEquals(
            ShiftDayContext.Timed(movedDate, listOf(ShiftInterval(moved.start!!, moved.end!!))),
            result.days.last(),
        )
    }

    @Test
    fun unavailableExplicitPatternReturnsTypedFailure() {
        val result = ShiftContextResolver.resolve(
            request(patterns = emptyList(), selectedPatternId = "missing"),
        )

        assertEquals(ShiftContextResolution.PatternUnavailable("missing"), result)
    }

    @Test
    fun storedEventSignalUsesActualDateAndOriginalPatternDate() {
        val originalDate = date
        val movedDate = date.plusDays(1)
        val zone = ZoneId.of("UTC")
        val signal = buildShiftEventSignal(
            metadata = ShiftEventMetadata(
                shiftTypeId = "day",
                shiftTypeName = "Day",
                colorHex = "#FF3B30",
                date = originalDate,
                generatedBy = ShiftEventGeneratedBy.Pattern,
                patternId = "pattern",
            ),
            startTimeMs = movedDate.atTime(10, 0).atZone(zone).toInstant().toEpochMilli(),
            endTimeMs = movedDate.atTime(18, 0).atZone(zone).toInstant().toEpochMilli(),
            timeZone = zone.id,
            isAllDay = false,
        )

        assertEquals(movedDate, signal?.actualDate)
        assertEquals(originalDate, signal?.originalPatternDate)
    }

    @Test
    fun cancelledGenerationUsesStoredDateOrMetadataFallback() {
        val record = ShiftGenerationRecord(
            id = "generation",
            patternId = "pattern",
            generatedAtMs = 1L,
            events = listOf(ShiftGenerationEvent("event", date = null, cancelled = true)),
            rangeStart = date,
            rangeEnd = date,
        )
        val metadata = ShiftEventMetadata(
            shiftTypeId = "day",
            shiftTypeName = "Day",
            colorHex = "#FF3B30",
            date = date,
            generatedBy = ShiftEventGeneratedBy.Pattern,
            patternId = "pattern",
        )

        assertEquals(
            setOf(ShiftPatternDate("pattern", date)),
            cancelledShiftPatternDates(listOf(record), mapOf("event" to metadata)),
        )
    }

    private fun request(
        patterns: List<ShiftPattern>,
        shiftTypes: List<ShiftType> = emptyList(),
        actualShifts: List<ShiftEventSignal> = emptyList(),
        selectedPatternId: String? = null,
        suppressedPatternDates: Set<ShiftPatternDate> = emptySet(),
        rangeEnd: LocalDate = date,
    ) = ShiftContextRequest(
        rangeStart = date,
        rangeEnd = rangeEnd,
        patterns = patterns,
        shiftTypes = shiftTypes,
        actualShifts = actualShifts,
        selectedPatternId = selectedPatternId,
        suppressedPatternDates = suppressedPatternDates,
    )

    private fun pattern(id: String, cycle: List<String> = listOf(SHIFT_OFF_TYPE_ID)) = ShiftPattern(
        id = id,
        name = id,
        cycleShiftTypeIds = cycle,
        cycleStartDate = date,
        createdAtMs = 1L,
    )

    private fun shiftType(
        id: String,
        startMinute: Int?,
        durationMinutes: Int?,
        isAllDay: Boolean = false,
    ) = ShiftType(
        id = id,
        name = id,
        colorHex = "#FF3B30",
        startMinuteOfDay = startMinute,
        durationMinutes = durationMinutes,
        isAllDay = isAllDay,
        reminderMinutes = null,
        createdAtMs = 1L,
    )
}
