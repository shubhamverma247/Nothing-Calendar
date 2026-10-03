package com.dotfield.dotcal.data.shifts

import java.time.LocalDate

enum class ShiftPreviewAction {
    Created,
    Updated,
    Skipped,
    Removed,
}

enum class ShiftSkipReason {
    Unchanged,
    ManuallyEdited,
    Cancelled,
    ProviderBacked,
    Shared,
    Recurring,
    AllDay,
}

data class ShiftEventFingerprint(
    val title: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val isAllDay: Boolean,
    val colorHex: String?,
    val reminderMinutes: Int?,
)

data class ShiftExpectedEvent(
    val date: LocalDate,
    val title: String,
    val shiftTypeId: String,
    val fingerprint: ShiftEventFingerprint,
)

data class ShiftTrackedEvent(
    val date: LocalDate,
    val eventId: String,
    val title: String,
    val fingerprint: ShiftEventFingerprint?,
    val protectedReason: ShiftSkipReason? = null,
)

data class ShiftGenerationPreviewItem(
    val action: ShiftPreviewAction,
    val date: LocalDate,
    val title: String,
    val eventId: String? = null,
    val shiftTypeId: String? = null,
    val skipReason: ShiftSkipReason? = null,
)

data class ShiftGenerationPreview(
    val patternId: String = "",
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
    val accountId: String? = null,
    val items: List<ShiftGenerationPreviewItem>,
) {
    val createdCount: Int get() = items.count { it.action == ShiftPreviewAction.Created }
    val updatedCount: Int get() = items.count { it.action == ShiftPreviewAction.Updated }
    val skippedCount: Int get() = items.count { it.action == ShiftPreviewAction.Skipped }
    val removedCount: Int get() = items.count { it.action == ShiftPreviewAction.Removed }
}

fun buildShiftGenerationPreview(
    expected: List<ShiftExpectedEvent>,
    tracked: List<ShiftTrackedEvent>,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
): ShiftGenerationPreview {
    val expectedByDate = expected.filter { it.date in rangeStart..rangeEnd }.associateBy { it.date }
    val trackedByDate = tracked
        .distinctBy { it.eventId }
        .filter { it.date in rangeStart..rangeEnd }
        .groupBy { it.date }
    val dates = (expectedByDate.keys + trackedByDate.keys).sorted()
    val items = dates.flatMap { date ->
        val wanted = expectedByDate[date]
        val existing = trackedByDate[date].orEmpty()
        buildList {
            if (wanted != null && existing.isEmpty()) {
                add(
                    ShiftGenerationPreviewItem(
                        action = ShiftPreviewAction.Created,
                        date = date,
                        title = wanted.title,
                        shiftTypeId = wanted.shiftTypeId,
                    ),
                )
            } else if (wanted != null) {
                add(classifyTrackedEvent(existing.first(), wanted))
            }
            existing.drop(if (wanted == null) 0 else 1).forEach { add(classifyRemoval(it)) }
        }
    }
    return ShiftGenerationPreview(rangeStart = rangeStart, rangeEnd = rangeEnd, items = items)
}

private fun classifyTrackedEvent(
    tracked: ShiftTrackedEvent,
    expected: ShiftExpectedEvent,
): ShiftGenerationPreviewItem {
    val reason = tracked.protectedReason
    return when {
        reason != null -> skipped(tracked, reason, expected.shiftTypeId)
        tracked.fingerprint == expected.fingerprint -> skipped(tracked, ShiftSkipReason.Unchanged, expected.shiftTypeId)
        else -> ShiftGenerationPreviewItem(
            action = ShiftPreviewAction.Updated,
            date = tracked.date,
            title = expected.title,
            eventId = tracked.eventId,
            shiftTypeId = expected.shiftTypeId,
        )
    }
}

private fun classifyRemoval(tracked: ShiftTrackedEvent): ShiftGenerationPreviewItem =
    tracked.protectedReason?.let { skipped(tracked, it) }
        ?: ShiftGenerationPreviewItem(
            action = ShiftPreviewAction.Removed,
            date = tracked.date,
            title = tracked.title,
            eventId = tracked.eventId,
        )

private fun skipped(
    tracked: ShiftTrackedEvent,
    reason: ShiftSkipReason,
    shiftTypeId: String? = null,
) = ShiftGenerationPreviewItem(
    action = ShiftPreviewAction.Skipped,
    date = tracked.date,
    title = tracked.title,
    eventId = tracked.eventId,
    shiftTypeId = shiftTypeId,
    skipReason = reason,
)
