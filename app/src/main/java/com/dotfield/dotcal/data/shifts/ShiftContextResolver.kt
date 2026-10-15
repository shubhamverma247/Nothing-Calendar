package com.dotfield.dotcal.data.shifts

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

data class ShiftContextRequest(
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
    val patterns: List<ShiftPattern>,
    val shiftTypes: List<ShiftType>,
    val actualShifts: List<ShiftEventSignal> = emptyList(),
    val selectedPatternId: String? = null,
    val suppressedPatternDates: Set<ShiftPatternDate> = emptySet(),
)

data class ShiftEventSignal(
    val actualDate: LocalDate,
    val start: LocalDateTime?,
    val end: LocalDateTime?,
    val isAllDay: Boolean,
    val generatedBy: ShiftEventGeneratedBy,
    val patternId: String? = null,
    val originalPatternDate: LocalDate? = null,
)

data class ShiftPatternDate(
    val patternId: String,
    val date: LocalDate,
)

fun buildShiftEventSignal(
    metadata: ShiftEventMetadata,
    startTimeMs: Long,
    endTimeMs: Long,
    timeZone: String,
    isAllDay: Boolean,
): ShiftEventSignal? {
    if (endTimeMs <= startTimeMs) return null
    val zoneId = runCatching { ZoneId.of(timeZone) }.getOrDefault(ZoneId.systemDefault())
    val actualStart = Instant.ofEpochMilli(startTimeMs).atZone(zoneId).toLocalDateTime()
    val actualEnd = Instant.ofEpochMilli(endTimeMs).atZone(zoneId).toLocalDateTime()
    return ShiftEventSignal(
        actualDate = actualStart.toLocalDate(),
        start = actualStart.takeUnless { isAllDay },
        end = actualEnd.takeUnless { isAllDay },
        isAllDay = isAllDay,
        generatedBy = metadata.generatedBy,
        patternId = metadata.patternId,
        originalPatternDate = metadata.date.takeIf { metadata.generatedBy == ShiftEventGeneratedBy.Pattern },
    )
}

fun cancelledShiftPatternDates(
    records: List<ShiftGenerationRecord>,
    metadataByEventId: Map<String, ShiftEventMetadata>,
): Set<ShiftPatternDate> = records.flatMap { record ->
    record.events.mapNotNull { event ->
        if (!event.cancelled) return@mapNotNull null
        val date = event.date ?: metadataByEventId[event.eventId]?.date ?: return@mapNotNull null
        ShiftPatternDate(record.patternId, date)
    }
}.toSet()

data class ShiftInterval(
    val start: LocalDateTime,
    val end: LocalDateTime,
)

sealed interface ShiftDayContext {
    val date: LocalDate

    data class Timed(
        override val date: LocalDate,
        val intervals: List<ShiftInterval>,
    ) : ShiftDayContext

    data class AllDay(override val date: LocalDate) : ShiftDayContext
    data class OffDay(override val date: LocalDate) : ShiftDayContext
    data class Suppressed(override val date: LocalDate) : ShiftDayContext
    data class Unknown(override val date: LocalDate) : ShiftDayContext
}

sealed interface ShiftContextResolution {
    data class Resolved(
        val patternId: String?,
        val days: List<ShiftDayContext>,
    ) : ShiftContextResolution

    data class AmbiguousPatterns(val patternIds: List<String>) : ShiftContextResolution
    data class PatternUnavailable(val patternId: String) : ShiftContextResolution
}

sealed interface ShiftRoutineRule {
    data class BeforeShiftStart(val minutes: Long) : ShiftRoutineRule {
        init {
            require(minutes >= 0) { "Routine offset must not be negative" }
        }
    }

    data class AfterShiftEnd(val minutes: Long) : ShiftRoutineRule {
        init {
            require(minutes >= 0) { "Routine offset must not be negative" }
        }
    }

    data class OnOffDay(val time: LocalTime) : ShiftRoutineRule
}

object ShiftContextResolver {
    fun resolve(request: ShiftContextRequest): ShiftContextResolution {
        require(!request.rangeEnd.isBefore(request.rangeStart)) {
            "Range end must not precede range start"
        }
        val patternResolution = selectPattern(request)
        if (patternResolution is PatternSelection.Ambiguous) {
            return ShiftContextResolution.AmbiguousPatterns(patternResolution.patternIds)
        }
        if (patternResolution is PatternSelection.Unavailable) {
            return ShiftContextResolution.PatternUnavailable(patternResolution.patternId)
        }
        val pattern = (patternResolution as PatternSelection.Selected).pattern
        val typesById = request.shiftTypes.associateBy { it.id }
        val actualShifts = request.actualShifts.filter { signal ->
            signal.generatedBy != ShiftEventGeneratedBy.Pattern ||
                pattern == null ||
                signal.patternId == pattern.id
        }
        val actualByDate = actualShifts.groupBy { it.actualDate }
        val suppressedDates = request.suppressedPatternDates + actualShifts.mapNotNull { signal ->
            val patternId = signal.patternId?.takeIf { signal.generatedBy == ShiftEventGeneratedBy.Pattern }
                ?: return@mapNotNull null
            val originalDate = signal.originalPatternDate ?: return@mapNotNull null
            ShiftPatternDate(patternId, originalDate).takeIf { originalDate != signal.actualDate }
        }
        val days = generateSequence(request.rangeStart) { it.plusDays(1) }
            .takeWhile { !it.isAfter(request.rangeEnd) }
            .map { date ->
                resolveDay(
                    date = date,
                    actualShifts = actualByDate[date].orEmpty(),
                    pattern = pattern,
                    typesById = typesById,
                    isPatternDateSuppressed = pattern?.let { ShiftPatternDate(it.id, date) in suppressedDates } == true,
                )
            }
            .toList()
        return ShiftContextResolution.Resolved(pattern?.id, days)
    }

    fun resolveRoutineTime(
        context: ShiftDayContext,
        rule: ShiftRoutineRule,
    ): LocalDateTime? = when (rule) {
        is ShiftRoutineRule.BeforeShiftStart -> (context as? ShiftDayContext.Timed)
            ?.intervals
            ?.minOfOrNull { it.start }
            ?.minusMinutes(rule.minutes)
        is ShiftRoutineRule.AfterShiftEnd -> (context as? ShiftDayContext.Timed)
            ?.intervals
            ?.maxOfOrNull { it.end }
            ?.plusMinutes(rule.minutes)
        is ShiftRoutineRule.OnOffDay -> (context as? ShiftDayContext.OffDay)
            ?.date
            ?.atTime(rule.time)
    }

    private fun selectPattern(request: ShiftContextRequest): PatternSelection {
        val active = request.patterns.filter { it.archivedAtMs == null }
        request.selectedPatternId?.let { selectedId ->
            val selected = active.firstOrNull { it.id == selectedId }
                ?: return PatternSelection.Unavailable(selectedId)
            return PatternSelection.Selected(selected)
        }
        return when (active.size) {
            0 -> PatternSelection.Selected(null)
            1 -> PatternSelection.Selected(active.single())
            else -> PatternSelection.Ambiguous(active.map { it.id }.sorted())
        }
    }

    private fun resolveDay(
        date: LocalDate,
        actualShifts: List<ShiftEventSignal>,
        pattern: ShiftPattern?,
        typesById: Map<String, ShiftType>,
        isPatternDateSuppressed: Boolean,
    ): ShiftDayContext {
        resolveActual(date, actualShifts)?.let { return it }
        if (isPatternDateSuppressed) return ShiftDayContext.Suppressed(date)
        if (pattern == null || pattern.cycleShiftTypeIds.isEmpty()) return ShiftDayContext.Unknown(date)
        val daysFromStart = ChronoUnit.DAYS.between(pattern.cycleStartDate, date)
        val cycleIndex = Math.floorMod(daysFromStart, pattern.cycleShiftTypeIds.size.toLong()).toInt()
        val shiftTypeId = pattern.cycleShiftTypeIds[cycleIndex]
        if (shiftTypeId == SHIFT_OFF_TYPE_ID) return ShiftDayContext.OffDay(date)
        val shiftType = typesById[shiftTypeId] ?: return ShiftDayContext.Unknown(date)
        return contextForType(date, shiftType)
    }

    private fun resolveActual(
        date: LocalDate,
        signals: List<ShiftEventSignal>,
    ): ShiftDayContext? {
        if (signals.any { it.isAllDay }) return ShiftDayContext.AllDay(date)
        val intervals = signals.mapNotNull { signal ->
            val start = signal.start ?: return@mapNotNull null
            val end = signal.end ?: return@mapNotNull null
            ShiftInterval(start, end).takeIf { end.isAfter(start) }
        }.sortedBy { it.start }
        return intervals.takeIf { it.isNotEmpty() }?.let { ShiftDayContext.Timed(date, it) }
    }

    private fun contextForType(date: LocalDate, type: ShiftType): ShiftDayContext = when (type.mode()) {
        ShiftTypeMode.AllDay -> ShiftDayContext.AllDay(date)
        ShiftTypeMode.OffDay -> ShiftDayContext.OffDay(date)
        ShiftTypeMode.Timed -> {
            val startMinute = Math.floorMod(requireNotNull(type.startMinuteOfDay), MINUTES_PER_DAY)
            val start = date.atStartOfDay().plusMinutes(startMinute.toLong())
            val end = start.plusMinutes(requireNotNull(type.durationMinutes).toLong())
            ShiftDayContext.Timed(date, listOf(ShiftInterval(start, end)))
        }
    }

    private sealed interface PatternSelection {
        data class Selected(val pattern: ShiftPattern?) : PatternSelection
        data class Ambiguous(val patternIds: List<String>) : PatternSelection
        data class Unavailable(val patternId: String) : PatternSelection
    }

    private const val MINUTES_PER_DAY = 24 * 60
}
