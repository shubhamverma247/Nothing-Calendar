package com.dotfield.dotcal.data.shifts

import java.io.File
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ShiftPatternStoreTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun generationRecordRoundTripsEventState() {
        val store = ShiftPatternStore(temporaryFolder.root)
        val record = ShiftGenerationRecord(
            id = "generation",
            patternId = "pattern",
            generatedAtMs = 123L,
            events = listOf(
                ShiftGenerationEvent(
                    eventId = "event",
                    date = LocalDate.of(2026, 10, 3),
                    updatedAtMs = 456L,
                    cancelled = true,
                ),
                ShiftGenerationEvent(eventId = "legacy-shaped-event"),
            ),
            rangeStart = LocalDate.of(2026, 10, 1),
            rangeEnd = LocalDate.of(2026, 10, 31),
        )

        assertEquals(true, store.saveGeneration(record))

        assertEquals(listOf(record), store.listGenerations())
    }

    @Test
    fun legacyEventIdsRemainReadableAndCancellationPersists() {
        val generationDir = File(temporaryFolder.root, "shift_generations").apply { mkdirs() }
        File(generationDir, "legacy.json").writeText(
            """{"id":"legacy","patternId":"pattern","generatedAtMs":1,"eventIds":["event"],"rangeStart":"2026-10-01","rangeEnd":"2026-10-31"}""",
        )
        val store = ShiftPatternStore(temporaryFolder.root)

        assertEquals(listOf("event"), store.listGenerations().single().eventIds)

        assertEquals(true, store.markEventCancelled("event", LocalDate.of(2026, 10, 3)))
        assertEquals(
            ShiftGenerationEvent(
                eventId = "event",
                date = LocalDate.of(2026, 10, 3),
                cancelled = true,
            ),
            store.listGenerations().single().events.single(),
        )
    }

    @Test
    fun legacyShiftTypeWithoutBreakRemainsReadable() {
        val typeDir = File(temporaryFolder.root, "shift_types").apply { mkdirs() }
        File(typeDir, "legacy.json").writeText(
            """{"id":"legacy","name":"Night","colorHex":"#FF3B30","startMinuteOfDay":1320,"durationMinutes":480,"isAllDay":false,"reminderMinutes":30,"createdAtMs":1}""",
        )

        val type = ShiftPatternStore(temporaryFolder.root).listTypes().single()

        assertEquals(30, type.reminderMinutes)
        assertEquals(null, type.breakMinutes)
    }

    @Test
    fun reminderAndBreakBoundaryValuesRoundTrip() {
        val store = ShiftPatternStore(temporaryFolder.root)
        val atStart = shiftType(id = "at-start", reminderMinutes = 0, breakMinutes = 1)
        val oneWeekBefore = shiftType(id = "one-week", reminderMinutes = 7 * 24 * 60, breakMinutes = 479)

        store.saveType(atStart)
        store.saveType(oneWeekBefore)

        assertEquals(
            setOf(atStart, oneWeekBefore),
            store.listTypes().toSet(),
        )
    }

    private fun shiftType(
        id: String,
        reminderMinutes: Int?,
        breakMinutes: Int?,
    ) = ShiftType(
        id = id,
        name = "Shift",
        colorHex = "#FF3B30",
        startMinuteOfDay = 9 * 60,
        durationMinutes = 8 * 60,
        isAllDay = false,
        reminderMinutes = reminderMinutes,
        breakMinutes = breakMinutes,
        createdAtMs = 1L,
    )
}
