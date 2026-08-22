package com.dotfield.dotcal.sync

import com.dotfield.dotcal.data.CalendarDao
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventReminder
import com.dotfield.dotcal.data.SyncMetadata
import com.dotfield.dotcal.data.provider.CalendarProviderDataSource
import com.dotfield.dotcal.data.provider.providerReminderAlarmRequestCode
import com.dotfield.dotcal.reminders.ReminderScheduler
import java.util.concurrent.TimeUnit

data class CalendarSyncResult(
    val permissionDenied: Boolean = false,
    val calendarsSynced: Int = 0,
    val eventsInserted: Int = 0,
    val eventsUpdated: Int = 0,
    val eventsDeleted: Int = 0,
)

class CalendarSyncRepository(
    private val dao: CalendarDao,
    private val providerDataSource: CalendarProviderDataSource,
    private val reminderScheduler: ReminderScheduler,
) {
    suspend fun sync(): CalendarSyncResult {
        if (!providerDataSource.hasCalendarReadPermission()) {
            return CalendarSyncResult(permissionDenied = true)
        }
        val now = System.currentTimeMillis()
        val rangeEndMs = now + TimeUnit.DAYS.toMillis(SYNC_RANGE_DAYS)
        val tombstoneCutoffMs = now - TimeUnit.DAYS.toMillis(TOMBSTONE_RETENTION_DAYS)
        var inserted = 0
        var updated = 0
        var deleted = 0
        val calendars = providerDataSource.getDeviceCalendars()
        calendars.forEach { providerAccount ->
            val existingAccount = dao.getAccount(providerAccount.id)
            val account = providerAccount.copy(isVisible = existingAccount?.isVisible ?: providerAccount.isVisible)
            dao.upsertAccountPreservingEvents(account)
            if (account.isVisible == 0) return@forEach
            val calendarId = account.googleCalendarId() ?: return@forEach
            val providerEvents = providerDataSource.getEventsInRange(calendarId, now, rangeEndMs)
            val modifiedOccurrenceExceptions = providerEvents
                .mapNotNull { event ->
                    val originalId = event.providerOriginalGoogleEventId ?: return@mapNotNull null
                    val originalStartMs = event.providerOriginalInstanceTimeMs ?: return@mapNotNull null
                    originalId to originalStartMs
                }
                .groupBy({ it.first }, { it.second })
            val providerEventsWithExceptions = providerEvents.map { event ->
                val externalExceptions = modifiedOccurrenceExceptions[event.googleEventId].orEmpty()
                if (event.providerOriginalGoogleEventId != null) {
                    event.copy(
                        rrule = null,
                        exceptionDates = "[]",
                        syncVersion = event.syncVersion.withProviderExceptionMetadata(
                            event.providerOriginalGoogleEventId,
                            event.providerOriginalInstanceTimeMs,
                        ),
                    )
                } else if (externalExceptions.isNotEmpty()) {
                    val exceptionDates = mergeExceptionDates(event.exceptionDates, externalExceptions)
                    event.copy(
                        exceptionDates = exceptionDates,
                        syncVersion = 31 * event.syncVersion + exceptionDates.hashCode(),
                    )
                } else {
                    event
                }
            }
            val localEvents = dao.getGoogleEventsInRange(calendarId.toString(), now, rangeEndMs)
            val providerByGoogleId = providerEventsWithExceptions.mapNotNull { event ->
                event.googleEventId?.let { it to event }
            }.toMap()
            val providerReminderMinutesByGoogleId = providerByGoogleId.keys.associateWith { googleEventId ->
                providerDataSource.getReminderMinutes(googleEventId)
            }
            val localByGoogleId = localEvents.mapNotNull { event ->
                event.googleEventId?.let { it to event }
            }.toMap()
            val deletedGoogleIds = providerByGoogleId.keys
                .takeIf { it.isNotEmpty() }
                ?.let { dao.getDeletedGoogleEventIds(it.toList()).toSet() }
                ?: emptySet()
            val upserts = mutableListOf<CalendarEvent>()
            var accountInserted = 0
            var accountUpdated = 0
            providerByGoogleId.forEach { (googleEventId, providerEvent) ->
                if (googleEventId in deletedGoogleIds) return@forEach
                val localEvent = localByGoogleId[googleEventId]
                when {
                    localEvent == null -> {
                        upserts += providerEvent
                        accountInserted += 1
                    }
                    localEvent.syncVersion != providerEvent.syncVersion -> {
                        upserts += providerEvent.copy(
                            id = localEvent.id,
                            createdAtMs = localEvent.createdAtMs,
                            updatedAtMs = now,
                        )
                        accountUpdated += 1
                    }
                }
            }
            val providerIds = providerByGoogleId.keys
            val deleteIds = localEvents
                .filter { localEvent -> localEvent.googleEventId !in providerIds }
                .map { it.id }
            val accountDeleted = deleteIds.size
            val remindersByEventId = upserts.associate { event ->
                event.id to providerReminderMinutesByGoogleId[event.googleEventId].orEmpty().toEventReminders(event)
            }
            val replacedReminderEvents = upserts.map { it.id } + deleteIds
            val remindersToCancel = replacedReminderEvents.flatMap { eventId -> dao.getRemindersForEvent(eventId) }
            inserted += accountInserted
            updated += accountUpdated
            deleted += accountDeleted
            dao.applyProviderCalendarSync(
                account = account,
                upserts = upserts,
                remindersByEventId = remindersByEventId,
                deleteIds = deleteIds,
                metadata = SyncMetadata(
                    accountId = account.id,
                    lastSyncMs = now,
                    lastSyncStatus = "SUCCESS",
                    errorMessage = null,
                    eventsInserted = accountInserted,
                    eventsUpdated = accountUpdated,
                    eventsDeleted = accountDeleted,
                ),
                tombstoneCutoffMs = tombstoneCutoffMs,
            )
            remindersToCancel.forEach { reminder -> reminderScheduler.cancelReminder(reminder.alarmRequestCode) }
            upserts.forEach { event ->
                remindersByEventId[event.id].orEmpty().forEach { reminder ->
                    reminderScheduler.scheduleReminder(reminder, event)
                }
            }
        }
        return CalendarSyncResult(
            calendarsSynced = calendars.size,
            eventsInserted = inserted,
            eventsUpdated = updated,
            eventsDeleted = deleted,
        )
    }

    private fun List<Int>.toEventReminders(event: CalendarEvent): List<EventReminder> {
        return distinct().filter { it >= 0 }.sorted().map { minutes ->
            EventReminder(
                eventId = event.id,
                minutesBefore = minutes,
                triggerAtMs = event.startTimeMs - minutes * 60_000L,
                alarmRequestCode = providerReminderAlarmRequestCode(event.id, minutes),
            )
        }
    }

    private fun mergeExceptionDates(exceptionDates: String, additionalExceptions: List<Long>): String {
        return (exceptionDates.toExceptionStartTimes() + additionalExceptions)
            .distinct()
            .sorted()
            .joinToString(prefix = "[", postfix = "]")
    }

    private fun String.toExceptionStartTimes(): List<Long> {
        return removePrefix("[")
            .removeSuffix("]")
            .split(',')
            .mapNotNull { it.trim().toLongOrNull() }
    }

    private fun Int.withProviderExceptionMetadata(originalGoogleEventId: String?, originalInstanceTimeMs: Long?): Int {
        var hash = this
        hash = 31 * hash + originalGoogleEventId.orEmpty().hashCode()
        hash = 31 * hash + (originalInstanceTimeMs?.hashCode() ?: 0)
        return hash
    }

    private fun com.dotfield.dotcal.data.CalendarAccount.googleCalendarId(): Long? {
        return id.substringAfter("provider-calendar-", "").toLongOrNull()
    }

    companion object {
        private const val SYNC_RANGE_DAYS = 365L
        private const val TOMBSTONE_RETENTION_DAYS = 30L
    }
}
