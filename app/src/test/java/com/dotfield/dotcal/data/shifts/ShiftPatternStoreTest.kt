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
}
