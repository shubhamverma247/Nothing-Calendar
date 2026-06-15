package com.dotfield.dotcal.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID
import kotlin.math.absoluteValue

data class EventEditorData(
    val title: String,
    val description: String,
    val location: String,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val isAllDay: Boolean,
    val reminderMinutes: Int?,
)

class DotCalRepository(private val dao: CalendarDao) {
    fun observeEventsForMonth(month: LocalDate): Flow<List<CalendarEvent>> {
        val start = month.withDayOfMonth(1)
        val end = start.plusMonths(1)
        return dao.observeEvents(start.atStartMs(), end.atStartMs())
    }

    fun observeTasks(): Flow<List<CalendarEvent>> = dao.observeTasks()

    suspend fun ensureLocalAccount() {
        dao.upsertAccount(
            CalendarAccount(
                id = LOCAL_ACCOUNT_ID,
                accountName = "LOCAL",
                displayName = "Personal",
                accountType = "LOCAL",
                color = "#FF0000",
                isVisible = 1,
                isPrimary = 1,
                sortOrder = 0,
            ),
        )
    }

    suspend fun saveLocalEvent(existing: CalendarEvent?, data: EventEditorData) {
        require(data.title.isNotBlank()) { "TITLE REQUIRED" }
        val zoneId = ZoneId.systemDefault()
        val start = if (data.isAllDay) {
            data.date.atStartOfDay(zoneId).toInstant().toEpochMilli()
        } else {
            data.date.atTime(data.startTime).atZone(zoneId).toInstant().toEpochMilli()
        }
        val end = if (data.isAllDay) {
            data.date.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        } else {
            data.date.atTime(data.endTime).atZone(zoneId).toInstant().toEpochMilli()
        }
        require(end > start) { "END MUST BE AFTER START" }
        val now = System.currentTimeMillis()
        val eventId = existing?.id ?: UUID.randomUUID().toString()
        val event = existing?.copy(
            title = data.title.trim(),
            description = data.description.trim(),
            location = data.location.trim(),
            startTimeMs = start,
            endTimeMs = end,
            timeZone = zoneId.id,
            isAllDay = if (data.isAllDay) 1 else 0,
            updatedAtMs = now,
        ) ?: CalendarEvent(
                id = eventId,
                accountId = LOCAL_ACCOUNT_ID,
                title = data.title.trim(),
                description = data.description.trim(),
                location = data.location.trim(),
                startTimeMs = start,
                endTimeMs = end,
                timeZone = zoneId.id,
                isAllDay = if (data.isAllDay) 1 else 0,
                colorHex = null,
                rrule = null,
                source = "LOCAL",
                googleEventId = null,
                googleCalendarId = null,
                completedAtMs = null,
                voiceNotePath = null,
                createdAtMs = now,
                updatedAtMs = now,
            )
        dao.upsertEvent(event)
        dao.deleteRemindersForEvent(eventId)
        data.reminderMinutes?.let { minutes ->
            dao.insertReminders(
                listOf(
                    EventReminder(
                        eventId = eventId,
                        minutesBefore = minutes,
                        triggerAtMs = start - minutes * 60_000L,
                        alarmRequestCode = "$eventId-$minutes".hashCode().absoluteValue,
                    ),
                ),
            )
        }
    }

    suspend fun addLocalEvent(title: String, date: LocalDate, startTime: LocalTime = LocalTime.of(9, 0)) {
        saveLocalEvent(
            existing = null,
            data = EventEditorData(
                title = title,
                description = "",
                location = "",
                date = date,
                startTime = startTime,
                endTime = startTime.plusHours(1),
                isAllDay = false,
                reminderMinutes = null,
            ),
        )
    }

    private fun LocalDate.atStartMs(): Long {
        return atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    companion object {
        const val LOCAL_ACCOUNT_ID = "local-primary"
    }
}
