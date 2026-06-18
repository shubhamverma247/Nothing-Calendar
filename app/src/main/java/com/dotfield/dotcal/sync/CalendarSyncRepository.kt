package com.dotfield.dotcal.sync

import com.dotfield.dotcal.data.CalendarDao
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.SyncMetadata
import com.dotfield.dotcal.data.provider.CalendarProviderDataSource
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
            val localEvents = dao.getGoogleEventsInRange(calendarId.toString(), now, rangeEndMs)
            val providerByGoogleId = providerEvents.mapNotNull { event ->
                event.googleEventId?.let { it to event }
            }.toMap()
            val localByGoogleId = localEvents.mapNotNull { event ->
                event.googleEventId?.let { it to event }
            }.toMap()
            val deletedGoogleIds = providerByGoogleId.keys
                .takeIf { it.isNotEmpty() }
                ?.let { dao.getDeletedGoogleEventIds(it.toList()).toSet() }
                ?: emptySet()
            val upserts = mutableListOf<CalendarEvent>()
            providerByGoogleId.forEach { (googleEventId, providerEvent) ->
                if (googleEventId in deletedGoogleIds) return@forEach
                val localEvent = localByGoogleId[googleEventId]
                when {
                    localEvent == null -> {
                        upserts += providerEvent
                        inserted += 1
                    }
                    localEvent.syncVersion != providerEvent.syncVersion -> {
                        upserts += providerEvent.copy(
                            id = localEvent.id,
                            createdAtMs = localEvent.createdAtMs,
                            updatedAtMs = now,
                        )
                        updated += 1
                    }
                }
            }
            val providerIds = providerByGoogleId.keys
            val deleteIds = localEvents
                .filter { localEvent -> localEvent.googleEventId !in providerIds }
                .map { it.id }
            deleted += deleteIds.size
            dao.applyProviderCalendarSync(
                account = account,
                upserts = upserts,
                deleteIds = deleteIds,
                metadata = SyncMetadata(
                    accountId = account.id,
                    lastSyncMs = now,
                    lastSyncStatus = "SUCCESS",
                    errorMessage = null,
                    eventsInserted = inserted,
                    eventsUpdated = updated,
                    eventsDeleted = deleted,
                ),
                tombstoneCutoffMs = tombstoneCutoffMs,
            )
        }
        return CalendarSyncResult(
            calendarsSynced = calendars.size,
            eventsInserted = inserted,
            eventsUpdated = updated,
            eventsDeleted = deleted,
        )
    }

    private fun com.dotfield.dotcal.data.CalendarAccount.googleCalendarId(): Long? {
        return id.substringAfter("provider-calendar-", "").toLongOrNull()
    }

    companion object {
        private const val SYNC_RANGE_DAYS = 60L
        private const val TOMBSTONE_RETENTION_DAYS = 30L
    }
}
