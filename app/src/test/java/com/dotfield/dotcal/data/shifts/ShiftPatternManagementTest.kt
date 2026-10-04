package com.dotfield.dotcal.data.shifts

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ShiftPatternManagementTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun editingPatternPreservesIdentityAndGenerationRecords() {
        val store = ShiftPatternStore(temporaryFolder.root)
        val original = pattern(id = "pattern", name = "Old name")
        val generation = generation(patternId = original.id)
        store.savePattern(original)
        store.saveGeneration(generation)

        store.savePattern(
            original.copy(
                name = "Edited name",
                cycleShiftTypeIds = listOf("night", SHIFT_OFF_TYPE_ID),
            ),
        )

        assertEquals("pattern", store.listPatterns().single().id)
        assertEquals("Edited name", store.listPatterns().single().name)
        assertEquals(listOf(generation), store.listGenerations())
    }

    @Test
    fun duplicationCreatesIndependentPatternWithCopyName() {
        val original = pattern(id = "pattern", name = "Rotation")

        val duplicate = duplicateShiftPattern(
            original = original,
            copyName = "Copy of Rotation",
            createdAtMs = 99L,
        )

        assertNotEquals(original.id, duplicate.id)
        assertEquals("Copy of Rotation", duplicate.name)
        assertEquals(original.cycleShiftTypeIds, duplicate.cycleShiftTypeIds)
        assertEquals(original.cycleStartDate, duplicate.cycleStartDate)
        assertEquals(99L, duplicate.createdAtMs)
        assertNull(duplicate.archivedAtMs)
    }

    @Test
    fun archiveRoundTripsWithoutRemovingGenerationRecords() {
        val store = ShiftPatternStore(temporaryFolder.root)
        val original = pattern(id = "pattern")
        val generation = generation(patternId = original.id)
        store.savePattern(original)
        store.saveGeneration(generation)

        store.savePattern(original.copy(archivedAtMs = 77L))

        assertEquals(77L, store.listPatterns().single().archivedAtMs)
        assertEquals(listOf(generation), store.listGenerations())

        store.savePattern(original.copy(archivedAtMs = null))
        assertNull(store.listPatterns().single().archivedAtMs)
        assertEquals(listOf(generation), store.listGenerations())
    }

    @Test
    fun deletingPatternDoesNotRemoveGenerationRecordsWithoutExplicitCleanup() {
        val store = ShiftPatternStore(temporaryFolder.root)
        val original = pattern(id = "pattern")
        val generation = generation(patternId = original.id)
        store.savePattern(original)
        store.saveGeneration(generation)

        store.removePattern(original.id)

        assertEquals(emptyList<ShiftPattern>(), store.listPatterns())
        assertEquals(listOf(generation), store.listGenerations())

        store.removeGenerationsForPattern(original.id)
        assertEquals(emptyList<ShiftGenerationRecord>(), store.listGenerations())
    }

    @Test
    fun rotatingDaysNightsPresetUsesTwoDaysTwoNightsFourOff() {
        assertEquals(
            listOf("day", "day", "night", "night", SHIFT_OFF_TYPE_ID, SHIFT_OFF_TYPE_ID, SHIFT_OFF_TYPE_ID, SHIFT_OFF_TYPE_ID),
            shiftPatternPresetCycle(ShiftPatternPreset.RotatingDaysNights, primaryTypeId = "day", secondaryTypeId = "night"),
        )
    }

    @Test
    fun fourOnFourOffPresetUsesFourWorkDaysAndFourOffDays() {
        assertEquals(
            listOf("day", "day", "day", "day", SHIFT_OFF_TYPE_ID, SHIFT_OFF_TYPE_ID, SHIFT_OFF_TYPE_ID, SHIFT_OFF_TYPE_ID),
            shiftPatternPresetCycle(ShiftPatternPreset.FourOnFourOff, primaryTypeId = "day"),
        )
    }

    @Test
    fun shiftTypeUsageIncludesActiveAndArchivedPatterns() {
        val patterns = listOf(
            pattern(id = "active", cycle = listOf("day", SHIFT_OFF_TYPE_ID)),
            pattern(id = "archived", cycle = listOf("night", "day")).copy(archivedAtMs = 9L),
            pattern(id = "other", cycle = listOf("night")),
        )

        assertEquals(2, shiftTypeUsageCount(patterns, "day"))
        assertEquals(2, shiftTypeUsageCount(patterns, "night"))
        assertEquals(0, shiftTypeUsageCount(patterns, "missing"))
    }

    @Test
    fun presetExpansionWrapsAcrossBothCycleBoundaries() {
        val day = shiftType("day")
        val night = shiftType("night")
        val pattern = pattern(
            cycle = shiftPatternPresetCycle(
                ShiftPatternPreset.RotatingDaysNights,
                primaryTypeId = day.id,
                secondaryTypeId = night.id,
            ),
        )

        val occurrences = expandShiftPattern(
            pattern = pattern,
            shiftTypes = listOf(day, night).associateBy { it.id },
            rangeStart = pattern.cycleStartDate.minusDays(1),
            rangeEnd = pattern.cycleStartDate.plusDays(8),
        )

        assertEquals(
            listOf(
                pattern.cycleStartDate to "day",
                pattern.cycleStartDate.plusDays(1) to "day",
                pattern.cycleStartDate.plusDays(2) to "night",
                pattern.cycleStartDate.plusDays(3) to "night",
                pattern.cycleStartDate.plusDays(8) to "day",
            ),
            occurrences.map { it.date to it.shiftType.id },
        )
    }

    @Test
    fun overnightShiftAtRangeEndProducesOneStartDateOccurrence() {
        val night = shiftType("night").copy(startMinuteOfDay = 22 * 60, durationMinutes = 8 * 60)
        val rangeEnd = LocalDate.of(2026, 10, 3)
        val pattern = pattern(cycle = listOf(night.id))

        val occurrences = expandShiftPattern(
            pattern = pattern,
            shiftTypes = mapOf(night.id to night),
            rangeStart = rangeEnd,
            rangeEnd = rangeEnd,
        )

        assertEquals(listOf(rangeEnd), occurrences.map { it.date })
        assertEquals(rangeEnd.plusDays(1), buildShiftEventDraft(occurrences.single().shiftType, rangeEnd)?.endDate)
    }

    private fun pattern(
        id: String = "pattern",
        name: String = "Rotation",
        cycle: List<String> = listOf("day"),
    ) = ShiftPattern(
        id = id,
        name = name,
        cycleShiftTypeIds = cycle,
        cycleStartDate = LocalDate.of(2026, 10, 3),
        createdAtMs = 1L,
    )

    private fun generation(patternId: String) = ShiftGenerationRecord(
        id = "generation",
        patternId = patternId,
        generatedAtMs = 2L,
        events = listOf(ShiftGenerationEvent("event")),
        rangeStart = LocalDate.of(2026, 10, 3),
        rangeEnd = LocalDate.of(2026, 10, 4),
    )

    private fun shiftType(id: String) = ShiftType(
        id = id,
        name = id,
        colorHex = "#FF0000",
        startMinuteOfDay = 8 * 60,
        durationMinutes = 8 * 60,
        isAllDay = false,
        reminderMinutes = null,
        breakMinutes = null,
        createdAtMs = 1L,
    )
}
