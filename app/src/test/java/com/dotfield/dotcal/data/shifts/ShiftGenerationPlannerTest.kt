package com.dotfield.dotcal.data.shifts

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ShiftGenerationPlannerTest {

    @Test
    fun previewClassifiesEveryAffectedOccurrence() {
        val start = LocalDate.of(2026, 10, 1)
        val expected = listOf(
            expected(start, "Day"),
            expected(start.plusDays(1), "Night"),
            expected(start.plusDays(2), "Day"),
        )
        val tracked = listOf(
            tracked(start, "same", expected[0].fingerprint),
            tracked(start.plusDays(1), "changed", fingerprint("Old")),
            tracked(start.plusDays(3), "removed", fingerprint("Old")),
        )

        val preview = buildShiftGenerationPreview(expected, tracked, start, start.plusDays(3))

        assertEquals(1, preview.createdCount)
        assertEquals(1, preview.updatedCount)
        assertEquals(1, preview.skippedCount)
        assertEquals(1, preview.removedCount)
        assertEquals(
            listOf(
                ShiftPreviewAction.Skipped,
                ShiftPreviewAction.Updated,
                ShiftPreviewAction.Created,
                ShiftPreviewAction.Removed,
            ),
            preview.items.map { it.action },
        )
    }

    @Test
    fun previewPreservesEditedCancelledAndProtectedEvents() {
        val start = LocalDate.of(2026, 10, 1)
        val reasons = listOf(
            ShiftSkipReason.ManuallyEdited,
            ShiftSkipReason.Cancelled,
            ShiftSkipReason.ProviderBacked,
            ShiftSkipReason.Shared,
            ShiftSkipReason.Recurring,
            ShiftSkipReason.AllDay,
        )
        val tracked = reasons.mapIndexed { index, reason ->
            tracked(start.plusDays(index.toLong()), reason.name, fingerprint("Old"), reason)
        }
        val expected = reasons.indices.map { index -> expected(start.plusDays(index.toLong()), "New") }

        val preview = buildShiftGenerationPreview(expected, tracked, start, start.plusDays(5))

        assertEquals(reasons, preview.items.map { it.skipReason })
        assertEquals(List(reasons.size) { ShiftPreviewAction.Skipped }, preview.items.map { it.action })
    }

    @Test
    fun previewNeverIncludesTrackedEventsOutsideRequestedRange() {
        val start = LocalDate.of(2026, 10, 10)
        val tracked = listOf(
            tracked(start.minusDays(1), "before", fingerprint("Old")),
            tracked(start.plusDays(1), "inside", fingerprint("Old")),
            tracked(start.plusDays(3), "after", fingerprint("Old")),
        )

        val preview = buildShiftGenerationPreview(emptyList(), tracked, start, start.plusDays(2))

        assertEquals(listOf("inside"), preview.items.map { it.eventId })
        assertEquals(1, preview.removedCount)
    }

    @Test
    fun previewDeduplicatesLegacyRecordsForSameEvent() {
        val date = LocalDate.of(2026, 10, 10)
        val expected = expected(date, "Day")
        val duplicate = tracked(date, "same-event", fingerprint("Old"))

        val preview = buildShiftGenerationPreview(
            expected = listOf(expected),
            tracked = listOf(duplicate, duplicate),
            rangeStart = date,
            rangeEnd = date,
        )

        assertEquals(listOf(ShiftPreviewAction.Updated), preview.items.map { it.action })
    }

    @Test
    fun previewKeepsOvernightTimingForDisplay() {
        val date = LocalDate.of(2026, 10, 10)
        val timing = ShiftDisplayTime(startMinuteOfDay = 22 * 60, durationMinutes = 8 * 60)
        val expected = expected(date, "Night").copy(displayTime = timing)

        val preview = buildShiftGenerationPreview(
            expected = listOf(expected),
            tracked = emptyList(),
            rangeStart = date,
            rangeEnd = date,
        )

        assertEquals(timing, preview.items.single().displayTime)
        assertEquals(1, preview.items.single().displayTime?.endDayOffset)
    }

    @Test
    fun patternCleanupRemovesOnlyUnprotectedGeneratedEvents() {
        val date = LocalDate.of(2026, 10, 10)
        val tracked = listOf(
            tracked(date, "safe", fingerprint("Safe")),
            tracked(date, "edited", fingerprint("Edited"), ShiftSkipReason.ManuallyEdited),
            tracked(date, "cancelled", null, ShiftSkipReason.Cancelled),
            tracked(date, "provider", fingerprint("Provider"), ShiftSkipReason.ProviderBacked),
            tracked(date, "shared", fingerprint("Shared"), ShiftSkipReason.Shared),
            tracked(date, "recurring", fingerprint("Recurring"), ShiftSkipReason.Recurring),
            tracked(date, "all-day", fingerprint("All day"), ShiftSkipReason.AllDay),
        )

        assertEquals(listOf("safe"), removableGeneratedShiftEventIds(tracked))
    }

    private fun expected(date: LocalDate, title: String) = ShiftExpectedEvent(
        date = date,
        title = title,
        shiftTypeId = title.lowercase(),
        fingerprint = fingerprint(title),
    )

    private fun tracked(
        date: LocalDate,
        id: String,
        fingerprint: ShiftEventFingerprint?,
        protectedReason: ShiftSkipReason? = null,
    ) = ShiftTrackedEvent(
        date = date,
        eventId = id,
        title = id,
        fingerprint = fingerprint,
        protectedReason = protectedReason,
    )

    private fun fingerprint(title: String) = ShiftEventFingerprint(
        title = title,
        startTimeMs = 1L,
        endTimeMs = 2L,
        isAllDay = false,
        colorHex = "#FF3B30",
        reminderMinutes = null,
    )
}
