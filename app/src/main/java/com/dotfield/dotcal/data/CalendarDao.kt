package com.dotfield.dotcal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarDao {
    @Query("SELECT * FROM calendar_accounts ORDER BY sortOrder ASC")
    fun observeAccounts(): Flow<List<CalendarAccount>>

    @Query(
        """
        SELECT * FROM calendar_events
        WHERE isTask = 0
        AND (
            (startTimeMs < :rangeEndMs AND endTimeMs >= :rangeStartMs)
            OR (rrule IS NOT NULL AND rrule != '' AND startTimeMs < :rangeEndMs)
        )
        ORDER BY startTimeMs ASC
        """,
    )
    fun observeEvents(rangeStartMs: Long, rangeEndMs: Long): Flow<List<CalendarEvent>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEvent(event: CalendarEvent)

    @Query("DELETE FROM event_reminders WHERE eventId = :eventId")
    suspend fun deleteRemindersForEvent(eventId: String)

    @Query("DELETE FROM calendar_events WHERE id = :eventId")
    suspend fun deleteEvent(eventId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<EventReminder>)

    @Query("UPDATE event_reminders SET isDelivered = 1 WHERE alarmRequestCode = :alarmRequestCode")
    suspend fun markReminderDelivered(alarmRequestCode: Int)
}
