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

    @Query("SELECT * FROM calendar_accounts ORDER BY sortOrder ASC")
    suspend fun getAccountsForWidgetConfig(): List<CalendarAccount>

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

    @Query("SELECT MAX(sortOrder) FROM calendar_accounts")
    suspend fun getMaxAccountSortOrder(): Int?

    @Query("SELECT id FROM calendar_accounts WHERE id LIKE 'holiday-%' ORDER BY sortOrder ASC")
    fun observeHolidayAccountIds(): Flow<List<String>>

    @Query("SELECT * FROM calendar_events WHERE id = :eventId LIMIT 1")
    suspend fun getEvent(eventId: String): CalendarEvent?

    /**
     * All user-owned master rows (events + tasks) for ICS export. Excludes read-only generated
     * calendars (birthdays, holidays) and Google-synced rows which are re-created by their sources.
     */
    @Query(
        """
        SELECT * FROM calendar_events
        WHERE source NOT IN ('BIRTHDAY', 'HOLIDAY', 'GOOGLE')
        ORDER BY isTask ASC, startTimeMs ASC
        """,
    )
    suspend fun getAllUserEventsForExport(): List<CalendarEvent>

    @Query("SELECT * FROM event_reminders WHERE eventId = :eventId ORDER BY triggerAtMs ASC")
    suspend fun getRemindersForEvent(eventId: String): List<EventReminder>

    @Query(
        """
        SELECT event_reminders.* FROM event_reminders
        INNER JOIN calendar_events ON calendar_events.id = event_reminders.eventId
        WHERE calendar_events.source = 'BIRTHDAY'
        """,
    )
    suspend fun getBirthdayReminders(): List<EventReminder>

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
        SELECT calendar_events.* FROM calendar_events
        INNER JOIN calendar_accounts ON calendar_accounts.id = calendar_events.accountId
        WHERE calendar_accounts.isVisible = 1
        AND (:accountId IS NULL OR calendar_events.accountId = :accountId)
        AND calendar_events.isTask = 0
        AND (
            (calendar_events.startTimeMs < :rangeEndMs AND calendar_events.endTimeMs >= :rangeStartMs)
            OR (calendar_events.rrule IS NOT NULL AND calendar_events.rrule != '' AND calendar_events.startTimeMs < :rangeEndMs)
        )
        ORDER BY calendar_events.startTimeMs ASC
        """,
    )
    suspend fun getVisibleEventsForWidget(rangeStartMs: Long, rangeEndMs: Long, accountId: String?): List<CalendarEvent>

    @Query(
        """
        SELECT * FROM calendar_events
        WHERE isTask = 1
        AND isCompleted = 0
        AND startTimeMs > 0
        AND startTimeMs < :rangeEndMs
        ORDER BY startTimeMs ASC
        """,
    )
    suspend fun getOpenTasksForWidget(rangeEndMs: Long): List<CalendarEvent>

    @Query(
        """
        SELECT * FROM calendar_events
        WHERE isTask = 1
        ORDER BY isCompleted ASC, startTimeMs ASC
        """,
    )
    fun observeTasks(): Flow<List<CalendarEvent>>

    @Query(
        """
        SELECT * FROM calendar_events
        WHERE isTask = 1 AND startTimeMs BETWEEN :dayStartMs AND :dayEndMs
        ORDER BY isCompleted ASC, startTimeMs ASC
        """,
    )
    fun observeTodayTasks(dayStartMs: Long, dayEndMs: Long): Flow<List<CalendarEvent>>

    @Query(
        """
        SELECT * FROM calendar_events
        WHERE isTask = 1 AND isCompleted = 0 AND startTimeMs > :nowMs
        ORDER BY startTimeMs ASC
        """,
    )
    fun observeUpcomingTasks(nowMs: Long): Flow<List<CalendarEvent>>

    @Query(
        """
        SELECT * FROM calendar_events
        WHERE isTask = 1 AND isCompleted = 1
        ORDER BY completedAtMs DESC, startTimeMs ASC
        """,
    )
    fun observeCompletedTasks(): Flow<List<CalendarEvent>>

    @Query(
        """
        UPDATE calendar_events
        SET isCompleted = :isCompleted,
            completedAtMs = :completedAtMs,
            updatedAtMs = :updatedAtMs
        WHERE id = :eventId AND isTask = 1
        """,
    )
    suspend fun updateTaskCompletion(
        eventId: String,
        isCompleted: Int,
        completedAtMs: Long?,
        updatedAtMs: Long,
    )

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

    @Query("DELETE FROM calendar_accounts WHERE id = :accountId")
    suspend fun deleteAccount(accountId: String)

    @Query("DELETE FROM calendar_events WHERE source = 'BIRTHDAY'")
    suspend fun deleteBirthdayEvents()

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

    @Transaction
    suspend fun replaceBirthdayCalendar(
        account: CalendarAccount,
        events: List<CalendarEvent>,
        reminders: List<EventReminder>,
    ) {
        upsertAccountPreservingEvents(account)
        deleteBirthdayEvents()
        if (events.isNotEmpty()) upsertEvents(events)
        if (reminders.isNotEmpty()) insertReminders(reminders)
    }

    @Transaction
    suspend fun upsertHolidayCalendar(
        account: CalendarAccount,
        events: List<CalendarEvent>,
    ) {
        upsertAccountPreservingEvents(account)
        if (events.isNotEmpty()) upsertEvents(events)
    }
}
