package com.dotfield.dotcal.data.provider

import android.Manifest
import android.content.ContentUris
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.CalendarEvent
import java.util.TimeZone

class CalendarProviderDataSource(private val context: Context) {
    private val contentResolver: ContentResolver = context.contentResolver

    fun hasCalendarReadPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }

    fun hasCalendarWritePermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }

    fun getDeviceCalendars(): List<CalendarAccount> {
        if (!hasCalendarReadPermission()) return emptyList()
        val cursor = runCatching {
            contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                CALENDAR_PROJECTION,
                "${CalendarContract.Calendars.VISIBLE} != 0",
                null,
                "${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} ASC",
            )
        }.getOrNull() ?: return emptyList()
        return cursor.use { calendars ->
            buildList {
                while (calendars.moveToNext()) {
                    val calendarId = calendars.getLong(CALENDAR_ID_INDEX)
                    val accountType = calendars.getStringOrNull(CALENDAR_ACCOUNT_TYPE_INDEX).orEmpty()
                    val providerType = if (accountType.contains("google", ignoreCase = true)) "GOOGLE" else "DEVICE"
                    add(
                        CalendarAccount(
                            id = providerAccountId(calendarId),
                            accountName = calendars.getStringOrNull(CALENDAR_ACCOUNT_NAME_INDEX).orEmpty(),
                            displayName = calendars.getStringOrNull(CALENDAR_DISPLAY_NAME_INDEX).takeUnless { it.isNullOrBlank() }
                                ?: calendars.getStringOrNull(CALENDAR_ACCOUNT_NAME_INDEX).takeUnless { it.isNullOrBlank() }
                                ?: "Calendar",
                            accountType = providerType,
                            color = calendars.getColorHex(CALENDAR_COLOR_INDEX),
                            isVisible = calendars.getIntOrDefault(CALENDAR_VISIBLE_INDEX, 1),
                            isPrimary = 0,
                            sortOrder = calendars.position + PROVIDER_SORT_OFFSET,
                        ),
                    )
                }
            }
        }
    }

    fun getEventsInRange(calendarId: Long, startMs: Long, endMs: Long): List<CalendarEvent> {
        if (!hasCalendarReadPermission()) return emptyList()
        val cursor = runCatching {
            contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                EVENT_PROJECTION,
                "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DELETED} = 0 AND ${CalendarContract.Events.DTSTART} < ?",
                arrayOf(calendarId.toString(), endMs.toString()),
                "${CalendarContract.Events.DTSTART} ASC",
            )
        }.getOrNull() ?: return emptyList()
        return cursor.use { events ->
            buildList {
                while (events.moveToNext()) {
                    events.toCalendarEvent(calendarId, startMs, endMs)?.let(::add)
                }
            }
        }
    }

    fun saveEvent(calendarId: Long, event: CalendarEvent): CalendarEvent? {
        if (!hasCalendarWritePermission()) return null
        val existingProviderId = event.googleEventId?.toLongOrNull()
        val savedProviderId = if (existingProviderId == null) {
            insertEvent(calendarId, event)
        } else {
            updateEvent(existingProviderId, calendarId, event).takeIf { it }?.let { existingProviderId }
                ?: insertEvent(calendarId, event)
        } ?: return null
        return getEvent(calendarId, savedProviderId.toString())
    }

    fun deleteEvent(googleEventId: String): Boolean {
        if (!hasCalendarWritePermission()) return false
        val providerEventId = googleEventId.toLongOrNull() ?: return false
        val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, providerEventId)
        return runCatching { contentResolver.delete(eventUri, null, null) > 0 }.getOrDefault(false)
    }

    private fun insertEvent(calendarId: Long, event: CalendarEvent): Long? {
        val uri = runCatching {
            contentResolver.insert(CalendarContract.Events.CONTENT_URI, eventValues(calendarId, event, includeCalendarId = true))
        }.getOrNull() ?: return null
        return ContentUris.parseId(uri).takeIf { it >= 0L }
    }

    private fun updateEvent(providerEventId: Long, calendarId: Long, event: CalendarEvent): Boolean {
        val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, providerEventId)
        return runCatching {
            contentResolver.update(eventUri, eventValues(calendarId, event, includeCalendarId = false), null, null) > 0
        }.getOrDefault(false)
    }

    private fun getEvent(calendarId: Long, googleEventId: String): CalendarEvent? {
        if (!hasCalendarReadPermission()) return null
        val cursor = runCatching {
            contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                EVENT_PROJECTION,
                "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events._ID} = ? AND ${CalendarContract.Events.DELETED} = 0",
                arrayOf(calendarId.toString(), googleEventId),
                null,
            )
        }.getOrNull() ?: return null
        return cursor.use { events ->
            if (events.moveToFirst()) {
                events.toCalendarEvent(calendarId, Long.MIN_VALUE, Long.MAX_VALUE)
            } else {
                null
            }
        }
    }

    private fun eventValues(calendarId: Long, event: CalendarEvent, includeCalendarId: Boolean): ContentValues {
        return ContentValues().apply {
            if (includeCalendarId) {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
            }
            put(CalendarContract.Events.TITLE, event.title)
            put(CalendarContract.Events.DESCRIPTION, event.description)
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.DTSTART, event.startTimeMs)
            put(CalendarContract.Events.EVENT_TIMEZONE, event.timeZone)
            put(CalendarContract.Events.ALL_DAY, event.isAllDay)
            if (event.rrule.isNullOrBlank()) {
                put(CalendarContract.Events.DTEND, event.endTimeMs)
                putNull(CalendarContract.Events.DURATION)
            } else {
                putNull(CalendarContract.Events.DTEND)
                put(CalendarContract.Events.DURATION, event.providerDuration())
            }
            event.colorHex?.toProviderColor()?.let { put(CalendarContract.Events.EVENT_COLOR, it) }
                ?: putNull(CalendarContract.Events.EVENT_COLOR)
            event.rrule?.takeUnless { it.isBlank() }?.let { put(CalendarContract.Events.RRULE, it) }
                ?: putNull(CalendarContract.Events.RRULE)
        }
    }

    private fun Cursor.toCalendarEvent(calendarId: Long, rangeStartMs: Long, rangeEndMs: Long): CalendarEvent? {
        val providerEventId = getLong(EVENT_ID_INDEX).toString()
        val start = getLongOrNull(EVENT_DTSTART_INDEX) ?: return null
        val end = getLongOrNull(EVENT_DTEND_INDEX) ?: getLongOrNull(EVENT_LAST_DATE_INDEX) ?: start + DEFAULT_EVENT_DURATION_MS
        if (end < rangeStartMs || start >= rangeEndMs) return null
        val timeZone = getStringOrNull(EVENT_TIMEZONE_INDEX).takeUnless { it.isNullOrBlank() } ?: TimeZone.getDefault().id
        val now = System.currentTimeMillis()
        return CalendarEvent(
            id = providerEventRoomId(calendarId, providerEventId),
            accountId = providerAccountId(calendarId),
            title = getStringOrNull(EVENT_TITLE_INDEX).takeUnless { it.isNullOrBlank() } ?: "Untitled event",
            description = getStringOrNull(EVENT_DESCRIPTION_INDEX).orEmpty(),
            location = getStringOrNull(EVENT_LOCATION_INDEX).orEmpty(),
            startTimeMs = start,
            endTimeMs = end.coerceAtLeast(start + MIN_EVENT_DURATION_MS),
            timeZone = timeZone,
            isAllDay = getIntOrDefault(EVENT_ALL_DAY_INDEX, 0),
            colorHex = getLongOrNull(EVENT_COLOR_INDEX)?.let { colorIntToHex(it.toInt()) },
            rrule = getStringOrNull(EVENT_RRULE_INDEX),
            exceptionDates = "[]",
            source = "GOOGLE",
            googleEventId = providerEventId,
            googleCalendarId = calendarId.toString(),
            syncVersion = providerSyncVersion(),
            isTask = 0,
            isCompleted = 0,
            completedAtMs = null,
            imageUris = "[]",
            voiceNotePath = null,
            createdAtMs = now,
            updatedAtMs = now,
        )
    }

    private fun Cursor.providerSyncVersion(): Int {
        var hash = 17
        EVENT_HASH_COLUMNS.forEach { index ->
            hash = 31 * hash + getStringOrNull(index).orEmpty().hashCode()
        }
        return hash
    }

    private fun Cursor.getStringOrNull(index: Int): String? = if (isNull(index)) null else getString(index)

    private fun Cursor.getLongOrNull(index: Int): Long? = if (isNull(index)) null else getLong(index)

    private fun Cursor.getIntOrDefault(index: Int, defaultValue: Int): Int = if (isNull(index)) defaultValue else getInt(index)

    private fun Cursor.getColorHex(index: Int): String {
        return getLongOrNull(index)?.let { colorIntToHex(it.toInt()) } ?: "#FF3B30"
    }

    companion object {
        private const val DEFAULT_EVENT_DURATION_MS = 60 * 60 * 1000L
        private const val MIN_EVENT_DURATION_MS = 60 * 1000L
        private const val PROVIDER_SORT_OFFSET = 100

        private val CALENDAR_PROJECTION = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.VISIBLE,
        )
        private const val CALENDAR_ID_INDEX = 0
        private const val CALENDAR_ACCOUNT_NAME_INDEX = 1
        private const val CALENDAR_DISPLAY_NAME_INDEX = 2
        private const val CALENDAR_ACCOUNT_TYPE_INDEX = 3
        private const val CALENDAR_COLOR_INDEX = 4
        private const val CALENDAR_VISIBLE_INDEX = 5

        private val EVENT_PROJECTION = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_TIMEZONE,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.EVENT_COLOR,
            CalendarContract.Events.RRULE,
            CalendarContract.Events.LAST_DATE,
        )
        private const val EVENT_ID_INDEX = 0
        private const val EVENT_TITLE_INDEX = 1
        private const val EVENT_DESCRIPTION_INDEX = 2
        private const val EVENT_LOCATION_INDEX = 3
        private const val EVENT_DTSTART_INDEX = 4
        private const val EVENT_DTEND_INDEX = 5
        private const val EVENT_TIMEZONE_INDEX = 6
        private const val EVENT_ALL_DAY_INDEX = 7
        private const val EVENT_COLOR_INDEX = 8
        private const val EVENT_RRULE_INDEX = 9
        private const val EVENT_LAST_DATE_INDEX = 10
        private val EVENT_HASH_COLUMNS = EVENT_PROJECTION.indices.toList()
    }
}

fun providerAccountId(calendarId: Long): String = "provider-calendar-$calendarId"

fun providerCalendarId(accountId: String): Long? = accountId.substringAfter("provider-calendar-", "").toLongOrNull()

fun providerEventRoomId(calendarId: Long, eventId: String): String = "provider-calendar-$calendarId-event-$eventId"

private fun colorIntToHex(color: Int): String = "#%06X".format(0xFFFFFF and color)

private fun String.toProviderColor(): Int? {
    val hex = removePrefix("#")
    return hex.toLongOrNull(16)?.toInt()
}

private fun CalendarEvent.providerDuration(): String {
    val seconds = ((endTimeMs - startTimeMs).coerceAtLeast(60_000L)) / 1000L
    return "P${seconds}S"
}
