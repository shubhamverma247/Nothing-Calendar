package com.dotfield.dotcal.sync

import com.dotfield.dotcal.data.CalendarDao
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventReminder
import com.dotfield.dotcal.data.SyncMetadata
import com.dotfield.dotcal.data.provider.CalendarProviderDataSource
import com.dotfield.dotcal.data.provider.providerAvailabilityIsNonBlocking
import com.dotfield.dotcal.data.provider.providerReminderAlarmRequestCode
import com.dotfield.dotcal.data.recurrence.planNextReminder
import com.dotfield.dotcal.data.provider.providerStatusIsCancelled
import com.dotfield.dotcal.data.sidestore.EventSideStoreNamespaces
import com.dotfield.dotcal.data.sidestore.SharedSideStore
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
    private val sideStore: SharedSideStore,
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
            val providerEventsWithExceptions = applyProviderRecurringExceptionMetadata(providerEvents)
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
            val staleDuplicateIds = providerByGoogleId.keys
                .takeIf { it.isNotEmpty() }
                ?.let { providerIds ->
                    staleProviderDuplicateIds(
                        providerByGoogleId = providerByGoogleId,
                        existingProviderEvents = dao.getGoogleEventsByGoogleIds(providerIds.toList()),
                        localByGoogleId = localByGoogleId,
                    )
                }
                .orEmpty()
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
                .plus(staleDuplicateIds)
                .distinct()
            val accountDeleted = deleteIds.size
            val remindersByEventId = upserts.associate { event ->
                event.id to providerReminderMinutesByGoogleId[event.googleEventId].orEmpty().toEventReminders(event)
            }
            val replacedReminderEvents = upserts.map { it.id } + deleteIds
            val remindersToCancel = replacedReminderEvents.flatMap { eventId -> dao.getRemindersForEvent(eventId) }
            syncProviderMetadataFlags(providerByGoogleId, localByGoogleId, deletedGoogleIds, deleteIds)
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
        val nowMs = System.currentTimeMillis()
        return distinct().filter { it >= 0 }.sorted().map { minutes ->
            val plan = planNextReminder(event, minutes, nowMs)
            EventReminder(
                eventId = event.id,
                minutesBefore = minutes,
                triggerAtMs = plan?.triggerAtMs ?: event.startTimeMs - minutes * 60_000L,
                alarmRequestCode = providerReminderAlarmRequestCode(event.id, minutes),
            )
        }
    }

    private suspend fun syncProviderMetadataFlags(
        providerByGoogleId: Map<String, CalendarEvent>,
        localByGoogleId: Map<String, CalendarEvent>,
        deletedGoogleIds: Set<String>,
        deletedLocalEventIds: List<String>,
    ) {
        deletedLocalEventIds.forEach { eventId ->
            removeProviderSideMetadata(eventId)
        }
        providerByGoogleId.forEach { (googleEventId, providerEvent) ->
            if (googleEventId in deletedGoogleIds) return@forEach
            val eventId = localByGoogleId[googleEventId]?.id ?: providerEvent.id
            if (providerAvailabilityIsNonBlocking(providerEvent.providerAvailability)) {
                sideStore.write(EventSideStoreNamespaces.GhostFlags, eventId, "1")
            } else {
                sideStore.remove(EventSideStoreNamespaces.GhostFlags, eventId)
            }
            providerEvent.providerStatus?.let { status ->
                sideStore.write(EventSideStoreNamespaces.ProviderStatuses, eventId, status.toString())
            } ?: sideStore.remove(EventSideStoreNamespaces.ProviderStatuses, eventId)
            providerEvent.providerRdate?.let { rdate ->
                sideStore.write(EventSideStoreNamespaces.ProviderRdates, eventId, rdate)
            } ?: sideStore.remove(EventSideStoreNamespaces.ProviderRdates, eventId)
            providerEvent.providerMeetingMetadataJson?.let { metadata ->
                sideStore.write(EventSideStoreNamespaces.ProviderMeetingMetadata, eventId, metadata)
            } ?: sideStore.remove(EventSideStoreNamespaces.ProviderMeetingMetadata, eventId)
        }
    }

    private suspend fun removeProviderSideMetadata(eventId: String) {
        sideStore.remove(EventSideStoreNamespaces.GhostFlags, eventId)
        sideStore.remove(EventSideStoreNamespaces.ProviderStatuses, eventId)
        sideStore.remove(EventSideStoreNamespaces.ProviderRdates, eventId)
        sideStore.remove(EventSideStoreNamespaces.ProviderMeetingMetadata, eventId)
    }

    private fun com.dotfield.dotcal.data.CalendarAccount.googleCalendarId(): Long? {
        return id.substringAfter("provider-calendar-", "").toLongOrNull()
    }

    companion object {
        private const val SYNC_RANGE_DAYS = 365L
        private const val TOMBSTONE_RETENTION_DAYS = 30L
    }
}

internal fun applyProviderRecurringExceptionMetadata(providerEvents: List<CalendarEvent>): List<CalendarEvent> {
    val occurrenceExceptions = providerEvents
        .mapNotNull { event ->
            val originalId = event.providerOriginalGoogleEventId ?: return@mapNotNull null
            val originalStartMs = event.providerOriginalInstanceTimeMs ?: return@mapNotNull null
            originalId to originalStartMs
        }
        .groupBy({ it.first }, { it.second })
    return providerEvents.mapNotNull { event ->
        val externalExceptions = occurrenceExceptions[event.googleEventId].orEmpty()
        if (event.providerOriginalGoogleEventId != null) {
            if (providerStatusIsCancelled(event.providerStatus)) {
                null
            } else {
                event.copy(
                    rrule = null,
                    exceptionDates = "[]",
                    syncVersion = event.syncVersion.withProviderExceptionMetadata(
                        event.providerOriginalGoogleEventId,
                        event.providerOriginalInstanceTimeMs,
                    ),
                )
            }
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
}

private fun mergeExceptionDates(exceptionDates: String, additionalExceptions: List<Long>): String {
    return (exceptionDates.toExceptionStartTimes() + additionalExceptions)
        .distinct()
        .sorted()
        .joinToString(separator = ",", prefix = "[", postfix = "]")
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

internal fun staleProviderDuplicateIds(
    providerByGoogleId: Map<String, CalendarEvent>,
    existingProviderEvents: List<CalendarEvent>,
    localByGoogleId: Map<String, CalendarEvent>,
): List<String> {
    if (providerByGoogleId.isEmpty()) return emptyList()
    return existingProviderEvents.mapNotNull { existing ->
        val googleEventId = existing.googleEventId ?: return@mapNotNull null
        val providerEvent = providerByGoogleId[googleEventId] ?: return@mapNotNull null
        val keepId = localByGoogleId[googleEventId]?.id ?: providerEvent.id
        existing.id.takeIf { it != keepId }
    }.distinct()
}
