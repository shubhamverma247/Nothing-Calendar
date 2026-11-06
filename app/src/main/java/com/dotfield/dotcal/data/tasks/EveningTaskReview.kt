package com.dotfield.dotcal.data.tasks

import com.dotfield.dotcal.data.CalendarEvent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

sealed interface EveningTaskReviewAction {
    data object Complete : EveningTaskReviewAction
    data object Tomorrow : EveningTaskReviewAction
    data class ChooseDate(val date: LocalDate) : EveningTaskReviewAction
    data object Unscheduled : EveningTaskReviewAction
    data object Delete : EveningTaskReviewAction
    data object Skip : EveningTaskReviewAction
}

data class EveningTaskReviewSchedule(
    val date: LocalDate?,
    val time: LocalTime?,
    val reminderMinutes: Int?,
)

data class EveningTaskReviewProgress(val completed: Int, val total: Int)

fun CalendarEvent.isEveningTaskReviewEligible(): Boolean =
    isTask == 1 &&
        isCompleted == 0 &&
        rrule.isNullOrBlank() &&
        source == "LOCAL" &&
        googleEventId == null &&
        googleCalendarId == null

fun eveningTaskReviewCandidates(tasks: List<CalendarEvent>): List<CalendarEvent> =
    tasks.filter(CalendarEvent::isEveningTaskReviewEligible)

fun eveningTaskReviewProgress(
    tasks: List<CalendarEvent>,
    processedIds: Set<String>,
): EveningTaskReviewProgress {
    val candidateIds = eveningTaskReviewCandidates(tasks)
        .mapTo(processedIds.toMutableSet(), CalendarEvent::id)
    return EveningTaskReviewProgress(completed = processedIds.size, total = candidateIds.size)
}

internal suspend fun <T> runEveningTaskReviewMutation(
    undoToken: T,
    mutate: suspend () -> Unit,
    rollback: suspend (T) -> Unit,
): Result<T> {
    return try {
        mutate()
        Result.success(undoToken)
    } catch (cancelled: CancellationException) {
        rollbackEveningTaskReviewMutation(undoToken, cancelled, rollback)
        throw cancelled
    } catch (error: Exception) {
        rollbackEveningTaskReviewMutation(undoToken, error, rollback)
        Result.failure(error)
    }
}

private suspend fun <T> rollbackEveningTaskReviewMutation(
    undoToken: T,
    error: Throwable,
    rollback: suspend (T) -> Unit,
) {
    try {
        withContext(NonCancellable) { rollback(undoToken) }
    } catch (rollbackError: Exception) {
        error.addSuppressed(rollbackError)
    }
}

fun CalendarEvent.eveningReviewSchedule(
    action: EveningTaskReviewAction,
    today: LocalDate,
    reminderMinutes: Int?,
    zoneId: ZoneId = ZoneId.systemDefault(),
): EveningTaskReviewSchedule? {
    val targetDate = when (action) {
        EveningTaskReviewAction.Tomorrow -> today.plusDays(1)
        is EveningTaskReviewAction.ChooseDate -> action.date
        EveningTaskReviewAction.Unscheduled -> null
        else -> return null
    }
    val existingTime = if (targetDate != null && startTimeMs > 0L && isAllDay == 0) {
        Instant.ofEpochMilli(startTimeMs).atZone(zoneId).toLocalTime()
    } else {
        null
    }
    return EveningTaskReviewSchedule(
        date = targetDate,
        time = existingTime,
        reminderMinutes = reminderMinutes.takeIf { targetDate != null },
    )
}
