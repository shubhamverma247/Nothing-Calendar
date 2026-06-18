package com.dotfield.dotcal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_accounts ORDER BY sortOrder ASC")
    fun observeAccounts(): Flow<List<CalendarAccount>>

    @Query("SELECT * FROM sync_metadata ORDER BY lastSyncMs DESC")
    fun observeSyncMetadata(): Flow<List<SyncMetadata>>

    @Query(
        """
        SELECT calendar_events.* FROM calendar_events
        INNER JOIN calendar_accounts ON calendar_accounts.id = calendar_events.accountId
        WHERE calendar_events.isTask = 0
        AND calendar_accounts.isVisible = 1
        AND (
            (startTimeMs < :rangeEndMs AND endTimeMs >= :rangeStartMs)
            OR (rrule IS NOT NULL AND rrule != '' AND startTimeMs < :rangeEndMs)
        )
        ORDER BY calendar_events.startTimeMs ASC
        """,
    )
    fun observeEvents(rangeStartMs: Long, rangeEndMs: Long): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM calendar_accounts WHERE id = :accountId LIMIT 1")
    suspend fun getAccount(accountId: String): CalendarAccount?

    @Query("SELECT * FROM calendar_events WHERE id = :eventId LIMIT 1")
    suspend fun getEvent(eventId: String): CalendarEvent?

    @Query("SELECT * FROM event_reminders WHERE eventId = :eventId ORDER BY triggerAtMs ASC")
    suspend fun getRemindersForEvent(eventId: String): List<EventReminder>

    @Query("SELECT * FROM event_reminders WHERE alarmRequestCode = :alarmRequestCode LIMIT 1")
    suspend fun getReminderByRequestCode(alarmRequestCode: Int): EventReminder?

    @Query(
        """
        SELECT * FROM event_reminders
        WHERE isDelivered = 0 AND triggerAtMs > :nowMs
        ORDER BY triggerAtMs ASC
        """,
    )
    suspend fun getFutureUndeliveredReminders(nowMs: Long): List<EventReminder>

    @Query(
        """
        SELECT * FROM calendar_events
        WHERE isTask = 1
        ORDER BY isCompleted ASC, startTimeMs ASC
        """,
    )
    fun observeTasks(): Flow<List<CalendarEvent>>

    @Query("SELECT * FROM event_reminders ORDER BY triggerAtMs ASC")
    fun observeReminders(): Flow<List<EventReminder>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccountIfAbsent(account: CalendarAccount)

    @Query(
        """
        UPDATE calendar_accounts
        SET accountName = :accountName,
            displayName = :displayName,
            accountType = :accountType,
            color = :color,
            isVisible = :isVisible,
            isPrimary = :isPrimary,
            sortOrder = :sortOrder
        WHERE id = :id
        """,
    )
    suspend fun updateAccount(
        id: String,
        accountName: String,
        displayName: String,
        accountType: String,
        color: String,
        isVisible: Int,
        isPrimary: Int,
        sortOrder: Int,
    )

    @Query("UPDATE calendar_accounts SET isVisible = :isVisible WHERE id = :accountId")
    suspend fun updateAccountVisibility(accountId: String, isVisible: Int)

    @Transaction
    suspend fun upsertAccountPreservingEvents(account: CalendarAccount) {
        insertAccountIfAbsent(account)
        updateAccount(
            id = account.id,
            accountName = account.accountName,
            displayName = account.displayName,
            accountType = account.accountType,
            color = account.color,
            isVisible = account.isVisible,
            isPrimary = account.isPrimary,
            sortOrder = account.sortOrder,
        )
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvent(event: CalendarEvent)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvents(events: List<CalendarEvent>)

    @Query(
        """
        SELECT * FROM calendar_events
        WHERE source = 'GOOGLE'
        AND googleCalendarId = :googleCalendarId
        AND startTimeMs < :rangeEndMs
        AND endTimeMs >= :rangeStartMs
        """,
    )
    suspend fun getGoogleEventsInRange(
        googleCalendarId: String,
        rangeStartMs: Long,
        rangeEndMs: Long,
    ): List<CalendarEvent>

    @Query("DELETE FROM event_reminders WHERE eventId = :eventId")
    suspend fun deleteRemindersForEvent(eventId: String)

    @Query("DELETE FROM calendar_events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Query("DELETE FROM calendar_events WHERE id IN (:eventIds)")
    suspend fun deleteEvents(eventIds: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<EventReminder>)

    @Query("UPDATE event_reminders SET isDelivered = 1 WHERE alarmRequestCode = :alarmRequestCode")
    suspend fun markReminderDelivered(alarmRequestCode: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeletedEventLog(deletedEventLog: DeletedEventLog)

    @Query("SELECT googleEventId FROM deleted_event_log WHERE googleEventId IN (:googleEventIds)")
    suspend fun getDeletedGoogleEventIds(googleEventIds: List<String>): List<String>

    @Query("DELETE FROM deleted_event_log WHERE deletedAtMs < :cutoffMs")
    suspend fun deleteOldDeletedEventLogs(cutoffMs: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSyncMetadata(syncMetadata: SyncMetadata)

    @Transaction
    suspend fun applyProviderCalendarSync(
        account: CalendarAccount,
        upserts: List<CalendarEvent>,
        deleteIds: List<String>,
        metadata: SyncMetadata,
        tombstoneCutoffMs: Long,
    ) {
        upsertAccountPreservingEvents(account)
        if (upserts.isNotEmpty()) upsertEvents(upserts)
        if (deleteIds.isNotEmpty()) deleteEvents(deleteIds)
        deleteOldDeletedEventLogs(tombstoneCutoffMs)
        upsertSyncMetadata(metadata)
    }
}
