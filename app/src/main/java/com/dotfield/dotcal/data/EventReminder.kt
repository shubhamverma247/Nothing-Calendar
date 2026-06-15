package com.dotfield.dotcal.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "event_reminders",
    foreignKeys = [
        ForeignKey(
            entity = CalendarEvent::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["triggerAtMs"]),
        Index(value = ["isDelivered"]),
        Index(value = ["alarmRequestCode"], unique = true),
    ],
)
data class EventReminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventId: String,
    val minutesBefore: Int,
    val triggerAtMs: Long,
    val alarmRequestCode: Int,
    val isDelivered: Int = 0,
)
