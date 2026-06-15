package com.dotfield.dotcal.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "calendar_events",
    foreignKeys = [
        ForeignKey(
            entity = CalendarAccount::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["startTimeMs"]),
        Index(value = ["endTimeMs"]),
        Index(value = ["accountId"]),
        Index(value = ["source", "googleEventId"]),
        Index(value = ["isTask", "startTimeMs"]),
        Index(value = ["isCompleted"]),
    ],
)
data class CalendarEvent(
    @PrimaryKey val id: String,
    val accountId: String,
    val title: String,
    val description: String = "",
    val location: String = "",
    val startTimeMs: Long,
    val endTimeMs: Long,
    val timeZone: String,
    val isAllDay: Int,
    val colorHex: String?,
    val rrule: String?,
    val exceptionDates: String = "[]",
    val source: String,
    val googleEventId: String?,
    val googleCalendarId: String?,
    val syncVersion: Int = 0,
    val isTask: Int = 0,
    val isCompleted: Int = 0,
    val completedAtMs: Long?,
    val imageUris: String = "[]",
    val voiceNotePath: String?,
    val createdAtMs: Long,
    val updatedAtMs: Long,
)
