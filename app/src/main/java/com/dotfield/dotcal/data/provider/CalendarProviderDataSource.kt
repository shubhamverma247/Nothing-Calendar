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
import kotlin.math.absoluteValue
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
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
        val calendarColor = getCalendarColor(calendarId)
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
            val providerEventIds = buildList {
                while (events.moveToNext()) {
                    add(events.getLong(EVENT_ID_INDEX).toString())
                }
            }
            val reminderMinutesByEventId = getReminderMinutesForEvents(providerEventIds)
            val attendeesByEventId = getAttendeesForEvents(providerEventIds)
            events.moveToPosition(-1)
            buildList {
                while (events.moveToNext()) {
                    val providerEventId = events.getLong(EVENT_ID_INDEX).toString()
                    events.toCalendarEvent(
                        calendarId = calendarId,
                        calendarColor = calendarColor,
                        rangeStartMs = startMs,
                        rangeEndMs = endMs,
                        reminderMinutes = reminderMinutesByEventId[providerEventId].orEmpty(),
                        attendees = attendeesByEventId[providerEventId].orEmpty(),
                    )?.let(::add)
                }
            }
        }
    }

    fun saveEvent(calendarId: Long, event: CalendarEvent, reminderMinutes: List<Int>): CalendarEvent? {
        if (!hasCalendarWritePermission()) return null
        val existingProviderId = event.googleEventId?.toLongOrNull()
        val savedProviderId = if (existingProviderId == null) {
            insertEvent(calendarId, event)
        } else {
            updateEvent(existingProviderId, calendarId, event).takeIf { it }?.let { existingProviderId }
                ?: insertEvent(calendarId, event)
        } ?: return null
        replaceReminders(savedProviderId, reminderMinutes)
        return getEvent(calendarId, savedProviderId.toString())
    }

    /**
     * Cancel one occurrence without updating the recurring master event. Provider-backed
     * calendars need an exception row so Google/Outlook sync does not interpret EXDATE as a
     * series rewrite.
     */
    fun cancelEventOccurrence(
        googleEventId: String,
        occurrenceStartMs: Long,
        occurrenceEndMs: Long,
        isAllDay: Int,
        timeZone: String,
    ): Boolean {
        if (!hasCalendarWritePermission()) return false
        val providerEventId = googleEventId.toLongOrNull() ?: return false
        val exceptionUri = ContentUris.withAppendedId(
            CalendarContract.Events.CONTENT_EXCEPTION_URI,
            providerEventId,
        )
        val providerStartMs = providerCanceledOccurrenceTime(occurrenceStartMs, isAllDay, timeZone)
        val providerTimeZone = providerEventTimeZone(isAllDay, timeZone)
        val values = ContentValues().apply {
            put(CalendarContract.Events.ORIGINAL_INSTANCE_TIME, providerStartMs)
            put(CalendarContract.Events.DTSTART, providerStartMs)
            put(CalendarContract.Events.DURATION, providerCanceledOccurrenceDuration(occurrenceStartMs, occurrenceEndMs, isAllDay))
            put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CANCELED)
            put(CalendarContract.Events.ALL_DAY, isAllDay)
            put(CalendarContract.Events.EVENT_TIMEZONE, providerTimeZone)
        }
        return runCatching { contentResolver.insert(exceptionUri, values) != null }.getOrDefault(false)
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
                events.toCalendarEvent(calendarId, getCalendarColor(calendarId), Long.MIN_VALUE, Long.MAX_VALUE)
            } else {
                null
            }
        }
    }

    fun getReminderMinutes(googleEventId: String): List<Int> {
        if (!hasCalendarReadPermission()) return emptyList()
        val providerEventId = googleEventId.toLongOrNull() ?: return emptyList()
        val cursor = runCatching {
            contentResolver.query(
                CalendarContract.Reminders.CONTENT_URI,
                REMINDER_PROJECTION,
                "${CalendarContract.Reminders.EVENT_ID} = ?",
                arrayOf(providerEventId.toString()),
                "${CalendarContract.Reminders.MINUTES} ASC",
            )
        }.getOrNull() ?: return emptyList()
        return cursor.use { reminders ->
            buildList {
                while (reminders.moveToNext()) {
                    reminders.getIntOrDefault(REMINDER_MINUTES_INDEX, -1).takeIf { it >= 0 }?.let(::add)
                }
            }.normalizedProviderReminderMinutes()
        }
    }

    private fun getReminderMinutesForEvents(googleEventIds: Collection<String>): Map<String, List<Int>> {
        if (!hasCalendarReadPermission()) return emptyMap()
        val result = mutableMapOf<String, MutableList<Int>>()
        providerEventIdChunks(googleEventIds).forEach { ids ->
            val selection = "${CalendarContract.Reminders.EVENT_ID} IN (${ids.joinToString { "?" }})"
            val cursor = runCatching {
                contentResolver.query(
                    CalendarContract.Reminders.CONTENT_URI,
                    arrayOf(CalendarContract.Reminders.EVENT_ID, CalendarContract.Reminders.MINUTES),
                    selection,
                    ids.toTypedArray(),
                    "${CalendarContract.Reminders.EVENT_ID} ASC, ${CalendarContract.Reminders.MINUTES} ASC",
                )
            }.getOrNull() ?: return@forEach
            cursor.use { reminders ->
                while (reminders.moveToNext()) {
                    val eventId = reminders.getLong(0).toString()
                    val minutes = reminders.getIntOrDefault(1, -1)
                    if (minutes >= 0) result.getOrPut(eventId) { mutableListOf() }.add(minutes)
                }
            }
        }
        return result.mapValues { (_, minutes) -> minutes.normalizedProviderReminderMinutes() }
    }

    private fun replaceReminders(providerEventId: Long, reminderMinutes: List<Int>) {
        val normalizedMinutes = reminderMinutes.normalizedProviderReminderMinutes()
        val eventUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, providerEventId)
        runCatching {
            contentResolver.delete(
                CalendarContract.Reminders.CONTENT_URI,
                "${CalendarContract.Reminders.EVENT_ID} = ?",
                arrayOf(providerEventId.toString()),
            )
            normalizedMinutes.forEach { minutes ->
                contentResolver.insert(
                    CalendarContract.Reminders.CONTENT_URI,
                    ContentValues().apply {
                        put(CalendarContract.Reminders.EVENT_ID, providerEventId)
                        put(CalendarContract.Reminders.MINUTES, minutes)
                        put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                    },
                )
            }
            contentResolver.update(
                eventUri,
                ContentValues().apply {
                    put(CalendarContract.Events.HAS_ALARM, if (normalizedMinutes.isEmpty()) 0 else 1)
                },
                null,
                null,
            )
        }
    }

    private fun getCalendarColor(calendarId: Long): String {
        val cursor = runCatching {
            contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                arrayOf(CalendarContract.Calendars.CALENDAR_COLOR),
                "${CalendarContract.Calendars._ID} = ?",
                arrayOf(calendarId.toString()),
                null,
            )
        }.getOrNull() ?: return DEFAULT_PROVIDER_COLOR
        return cursor.use { calendars ->
            if (calendars.moveToFirst()) calendars.getColorHex(0) else DEFAULT_PROVIDER_COLOR
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
            val eventZone = safeProviderZone(event.timeZone)
            val providerStartMs = if (event.isAllDay == 1) {
                convertAllDayBoundary(event.startTimeMs, eventZone, ZoneOffset.UTC)
            } else {
                event.startTimeMs
            }
            put(CalendarContract.Events.DTSTART, providerStartMs)
            put(CalendarContract.Events.EVENT_TIMEZONE, providerEventTimeZone(event.isAllDay, event.timeZone))
            put(CalendarContract.Events.ALL_DAY, event.isAllDay)
            if (event.rrule.isNullOrBlank()) {
                val providerEndMs = if (event.isAllDay == 1) {
                    convertAllDayBoundary(event.endTimeMs, eventZone, ZoneOffset.UTC)
                } else {
                    event.endTimeMs
                }
                put(CalendarContract.Events.DTEND, providerEndMs)
                putNull(CalendarContract.Events.DURATION)
            } else {
                putNull(CalendarContract.Events.DTEND)
                put(CalendarContract.Events.DURATION, event.providerDuration())
            }
            event.colorHex?.toProviderColor()?.let { put(CalendarContract.Events.EVENT_COLOR, it) }
                ?: putNull(CalendarContract.Events.EVENT_COLOR)
            event.rrule?.takeUnless { it.isBlank() }?.let { put(CalendarContract.Events.RRULE, it) }
                ?: putNull(CalendarContract.Events.RRULE)
            providerExdateFromExceptionDates(event.exceptionDates, event.isAllDay, event.timeZone)
                ?.let { put(CalendarContract.Events.EXDATE, it) }
                ?: putNull(CalendarContract.Events.EXDATE)
            put(
                CalendarContract.Events.AVAILABILITY,
                if (event.isGhost) {
                    CalendarContract.Events.AVAILABILITY_FREE
                } else {
                    CalendarContract.Events.AVAILABILITY_BUSY
                },
            )
            put(CalendarContract.Events.STATUS, event.providerStatus ?: CalendarContract.Events.STATUS_CONFIRMED)
            normalizedProviderRdate(event.providerRdate)
                ?.let { put(CalendarContract.Events.RDATE, it) }
                ?: putNull(CalendarContract.Events.RDATE)
        }
    }

    private fun Cursor.toCalendarEvent(
        calendarId: Long,
        calendarColor: String,
        rangeStartMs: Long,
        rangeEndMs: Long,
        reminderMinutes: List<Int> = getReminderMinutes(getLong(EVENT_ID_INDEX).toString()),
        attendees: List<ProviderAttendee> = getAttendees(getLong(EVENT_ID_INDEX).toString()),
    ): CalendarEvent? {
        val providerEventId = getLong(EVENT_ID_INDEX).toString()
        val rawStart = getLongOrNull(EVENT_DTSTART_INDEX) ?: return null
        val rawEnd = providerEventEndTimeMs(
            startTimeMs = rawStart,
            dtEndMs = getLongOrNull(EVENT_DTEND_INDEX),
            duration = getStringOrNull(EVENT_DURATION_INDEX),
            lastDateMs = getLongOrNull(EVENT_LAST_DATE_INDEX),
            rrule = getStringOrNull(EVENT_RRULE_INDEX),
        )
        val timeZone = getStringOrNull(EVENT_TIMEZONE_INDEX).takeUnless { it.isNullOrBlank() } ?: TimeZone.getDefault().id
        val isAllDay = getIntOrDefault(EVENT_ALL_DAY_INDEX, 0)
        val zone = safeProviderZone(timeZone)
        val start = if (isAllDay == 1) convertAllDayBoundary(rawStart, ZoneOffset.UTC, zone) else rawStart
        val end = if (isAllDay == 1) convertAllDayBoundary(rawEnd, ZoneOffset.UTC, zone) else rawEnd
        if (end < rangeStartMs || start >= rangeEndMs) return null
        val now = System.currentTimeMillis()
        val originalGoogleEventId = getLongOrNull(EVENT_ORIGINAL_ID_INDEX)?.toString()
        val originalInstanceTimeMs = getLongOrNull(EVENT_ORIGINAL_INSTANCE_TIME_INDEX)
        val meetingMetadataJson = encodeProviderMeetingMetadata(
            ProviderMeetingMetadata(
                organizer = getStringOrNull(EVENT_ORGANIZER_INDEX),
                accessLevel = getIntOrNull(EVENT_ACCESS_LEVEL_INDEX),
                availability = getIntOrNull(EVENT_AVAILABILITY_INDEX),
                guestsCanModify = getIntOrNull(EVENT_GUESTS_CAN_MODIFY_INDEX)?.toProviderBoolean(),
                guestsCanInviteOthers = getIntOrNull(EVENT_GUESTS_CAN_INVITE_OTHERS_INDEX)?.toProviderBoolean(),
                guestsCanSeeGuests = getIntOrNull(EVENT_GUESTS_CAN_SEE_GUESTS_INDEX)?.toProviderBoolean(),
                attendees = attendees,
            ),
        )
        return CalendarEvent(
            id = providerEventRoomId(calendarId, providerEventId),
            accountId = providerAccountId(calendarId),
            title = getStringOrNull(EVENT_TITLE_INDEX).takeUnless { it.isNullOrBlank() } ?: "Untitled event",
            description = getStringOrNull(EVENT_DESCRIPTION_INDEX).orEmpty(),
            location = getStringOrNull(EVENT_LOCATION_INDEX).orEmpty(),
            startTimeMs = start,
            endTimeMs = end.coerceAtLeast(start + MIN_EVENT_DURATION_MS),
            timeZone = timeZone,
            isAllDay = isAllDay,
            colorHex = getLongOrNull(EVENT_COLOR_INDEX)?.let { colorIntToHex(it.toInt()) } ?: calendarColor,
            rrule = getStringOrNull(EVENT_RRULE_INDEX),
            exceptionDates = exceptionDatesFromProviderExdate(
                exdate = getStringOrNull(EVENT_EXDATE_INDEX),
                isAllDay = getIntOrDefault(EVENT_ALL_DAY_INDEX, 0),
                timeZone = timeZone,
            ),
            source = "GOOGLE",
            googleEventId = providerEventId,
            googleCalendarId = calendarId.toString(),
            syncVersion = providerSyncVersion(reminderMinutes, meetingMetadataJson),
            isTask = 0,
            isCompleted = 0,
            completedAtMs = null,
            imageUris = "[]",
            voiceNotePath = null,
            createdAtMs = now,
            updatedAtMs = now,
        ).apply {
            providerOriginalGoogleEventId = originalGoogleEventId
            providerOriginalInstanceTimeMs = originalInstanceTimeMs
            providerAvailability = getIntOrDefault(EVENT_AVAILABILITY_INDEX, CalendarContract.Events.AVAILABILITY_BUSY)
            providerStatus = getIntOrDefault(EVENT_STATUS_INDEX, CalendarContract.Events.STATUS_CONFIRMED)
            providerRdate = normalizedProviderRdate(getStringOrNull(EVENT_RDATE_INDEX))
            providerMeetingMetadataJson = meetingMetadataJson
            isGhost = providerAvailabilityIsNonBlocking(providerAvailability)
        }
    }

    private fun Cursor.providerSyncVersion(reminderMinutes: List<Int>, meetingMetadataJson: String?): Int {
        var hash = 17
        EVENT_HASH_COLUMNS.forEach { index ->
            hash = 31 * hash + getStringOrNull(index).orEmpty().hashCode()
        }
        hash = 31 * hash + reminderMinutes.hashCode()
        hash = 31 * hash + meetingMetadataJson.orEmpty().hashCode()
        return hash
    }

    private fun Cursor.getStringOrNull(index: Int): String? = if (isNull(index)) null else getString(index)

    private fun Cursor.getLongOrNull(index: Int): Long? = if (isNull(index)) null else getLong(index)

    private fun Cursor.getIntOrNull(index: Int): Int? = if (isNull(index)) null else getInt(index)

    private fun Cursor.getIntOrDefault(index: Int, defaultValue: Int): Int = if (isNull(index)) defaultValue else getInt(index)

    private fun Cursor.getColorHex(index: Int): String {
        return getLongOrNull(index)?.let { colorIntToHex(it.toInt()) } ?: "#FF3B30"
    }

    private fun getAttendees(providerEventId: String): List<ProviderAttendee> {
        val cursor = runCatching {
            contentResolver.query(
                CalendarContract.Attendees.CONTENT_URI,
                ATTENDEE_PROJECTION,
                "${CalendarContract.Attendees.EVENT_ID} = ?",
                arrayOf(providerEventId),
                "${CalendarContract.Attendees.ATTENDEE_NAME} ASC",
            )
        }.getOrNull() ?: return emptyList()
        return cursor.use { attendees ->
            buildList {
                while (attendees.moveToNext()) {
                    val attendee = ProviderAttendee(
                        name = attendees.getStringOrNull(ATTENDEE_NAME_INDEX),
                        email = attendees.getStringOrNull(ATTENDEE_EMAIL_INDEX),
                        status = attendees.getIntOrNull(ATTENDEE_STATUS_INDEX),
                        type = attendees.getIntOrNull(ATTENDEE_TYPE_INDEX),
                        relationship = attendees.getIntOrNull(ATTENDEE_RELATIONSHIP_INDEX),
                    )
                    if (attendee.isGuestAttendee()) add(attendee)
                }
            }
        }
    }

    private fun getAttendeesForEvents(googleEventIds: Collection<String>): Map<String, List<ProviderAttendee>> {
        if (!hasCalendarReadPermission()) return emptyMap()
        val result = mutableMapOf<String, MutableList<ProviderAttendee>>()
        providerEventIdChunks(googleEventIds).forEach { ids ->
            val selection = "${CalendarContract.Attendees.EVENT_ID} IN (${ids.joinToString { "?" }})"
            val cursor = runCatching {
                contentResolver.query(
                    CalendarContract.Attendees.CONTENT_URI,
                    arrayOf(
                        CalendarContract.Attendees.EVENT_ID,
                        CalendarContract.Attendees.ATTENDEE_NAME,
                        CalendarContract.Attendees.ATTENDEE_EMAIL,
                        CalendarContract.Attendees.ATTENDEE_STATUS,
                        CalendarContract.Attendees.ATTENDEE_TYPE,
                        CalendarContract.Attendees.ATTENDEE_RELATIONSHIP,
                    ),
                    selection,
                    ids.toTypedArray(),
                    "${CalendarContract.Attendees.EVENT_ID} ASC, ${CalendarContract.Attendees.ATTENDEE_NAME} ASC",
                )
            }.getOrNull() ?: return@forEach
            cursor.use { attendees ->
                while (attendees.moveToNext()) {
                    val eventId = attendees.getLong(0).toString()
                    val attendee = ProviderAttendee(
                        name = attendees.getStringOrNull(1),
                        email = attendees.getStringOrNull(2),
                        status = attendees.getIntOrNull(3),
                        type = attendees.getIntOrNull(4),
                        relationship = attendees.getIntOrNull(5),
                    )
                    if (attendee.isGuestAttendee()) {
                        result.getOrPut(eventId) { mutableListOf() }.add(attendee)
                    }
                }
            }
        }
        return result
    }

    private fun providerEventIdChunks(googleEventIds: Collection<String>): List<List<String>> {
        return googleEventIds
            .mapNotNull { it.toLongOrNull()?.toString() }
            .distinct()
            .chunked(PROVIDER_QUERY_CHUNK_SIZE)
    }

    companion object {
        private const val DEFAULT_EVENT_DURATION_MS = 60 * 60 * 1000L
        private const val MIN_EVENT_DURATION_MS = 60 * 1000L
        private const val PROVIDER_SORT_OFFSET = 100
        private const val PROVIDER_QUERY_CHUNK_SIZE = 500

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
            CalendarContract.Events.DURATION,
            CalendarContract.Events.EXDATE,
            CalendarContract.Events.ORIGINAL_ID,
            CalendarContract.Events.ORIGINAL_INSTANCE_TIME,
            CalendarContract.Events.AVAILABILITY,
            CalendarContract.Events.STATUS,
            CalendarContract.Events.RDATE,
            CalendarContract.Events.ORGANIZER,
            CalendarContract.Events.ACCESS_LEVEL,
            CalendarContract.Events.GUESTS_CAN_MODIFY,
            CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS,
            CalendarContract.Events.GUESTS_CAN_SEE_GUESTS,
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
        private const val EVENT_DURATION_INDEX = 11
        private const val EVENT_EXDATE_INDEX = 12
        private const val EVENT_ORIGINAL_ID_INDEX = 13
        private const val EVENT_ORIGINAL_INSTANCE_TIME_INDEX = 14
        private const val EVENT_AVAILABILITY_INDEX = 15
        private const val EVENT_STATUS_INDEX = 16
        private const val EVENT_RDATE_INDEX = 17
        private const val EVENT_ORGANIZER_INDEX = 18
        private const val EVENT_ACCESS_LEVEL_INDEX = 19
        private const val EVENT_GUESTS_CAN_MODIFY_INDEX = 20
        private const val EVENT_GUESTS_CAN_INVITE_OTHERS_INDEX = 21
        private const val EVENT_GUESTS_CAN_SEE_GUESTS_INDEX = 22
        private val EVENT_HASH_COLUMNS = EVENT_PROJECTION.indices.toList()

        private val REMINDER_PROJECTION = arrayOf(
            CalendarContract.Reminders.MINUTES,
        )
        private const val REMINDER_MINUTES_INDEX = 0

        private val ATTENDEE_PROJECTION = arrayOf(
            CalendarContract.Attendees.ATTENDEE_NAME,
            CalendarContract.Attendees.ATTENDEE_EMAIL,
            CalendarContract.Attendees.ATTENDEE_STATUS,
            CalendarContract.Attendees.ATTENDEE_TYPE,
            CalendarContract.Attendees.ATTENDEE_RELATIONSHIP,
        )
        private const val ATTENDEE_NAME_INDEX = 0
        private const val ATTENDEE_EMAIL_INDEX = 1
        private const val ATTENDEE_STATUS_INDEX = 2
        private const val ATTENDEE_TYPE_INDEX = 3
        private const val ATTENDEE_RELATIONSHIP_INDEX = 4
    }
}

fun providerAccountId(calendarId: Long): String = "provider-calendar-$calendarId"

fun providerCalendarId(accountId: String): Long? = accountId.substringAfter("provider-calendar-", "").toLongOrNull()

fun providerEventRoomId(calendarId: Long, eventId: String): String = "provider-calendar-$calendarId-event-$eventId"

private const val DEFAULT_PROVIDER_COLOR = "#FF3B30"
private const val PROVIDER_UTC_TIMEZONE = "UTC"

private fun colorIntToHex(color: Int): String = "#%06X".format(0xFFFFFF and color)

private fun String.toProviderColor(): Int? {
    val hex = removePrefix("#")
    return hex.toLongOrNull(16)?.toInt()
}

internal fun CalendarEvent.providerDuration(): String {
    return if (isAllDay == 1) {
        val dayMs = 24L * 60L * 60L * 1000L
        val days = ((endTimeMs - startTimeMs).coerceAtLeast(dayMs) + dayMs - 1L) / dayMs
        "P${days}D"
    } else {
        val seconds = ((endTimeMs - startTimeMs).coerceAtLeast(60_000L)) / 1000L
        "PT${seconds}S"
    }
}

internal fun List<Int>.normalizedProviderReminderMinutes(): List<Int> {
    return distinct().filter { it >= 0 }.sorted()
}

internal fun providerAvailabilityIsNonBlocking(availability: Int?): Boolean {
    return availability == CalendarContract.Events.AVAILABILITY_FREE
}

internal fun providerStatusIsCancelled(status: Int?): Boolean {
    return status == CalendarContract.Events.STATUS_CANCELED
}

private fun Int.toProviderBoolean(): Boolean = this != 0

internal fun normalizedProviderRdate(rdate: String?): String? {
    return rdate?.trim()?.takeIf { it.isNotBlank() }
}

fun providerReminderAlarmRequestCode(eventId: String, minutes: Int): Int {
    return "$eventId-$minutes".hashCode().absoluteValue
}

internal fun providerExdateFromExceptionDates(exceptionDates: String, isAllDay: Int, timeZone: String): String? {
    val zone = safeProviderZone(timeZone)
    val exceptions = exceptionDates
        .removePrefix("[")
        .removeSuffix("]")
        .split(',')
        .mapNotNull { it.trim().toLongOrNull() }
        .distinct()
        .sorted()
    if (exceptions.isEmpty()) return null
    return exceptions.joinToString(",") { ms ->
        val instant = Instant.ofEpochMilli(ms)
        if (isAllDay == 1) {
            PROVIDER_DATE_ONLY.format(instant.atZone(zone).toLocalDate())
        } else {
            PROVIDER_UTC_STAMP.format(instant)
        }
    }
}

internal fun providerCanceledOccurrenceDuration(startTimeMs: Long, endTimeMs: Long, isAllDay: Int): String {
    val durationMs = (endTimeMs - startTimeMs).coerceAtLeast(1_000L)
    return if (isAllDay == 1) {
        val dayMs = 24L * 60L * 60L * 1_000L
        "P${((durationMs + dayMs - 1L) / dayMs).coerceAtLeast(1L)}D"
    } else {
        "P${((durationMs + 999L) / 1_000L).coerceAtLeast(1L)}S"
    }
}

internal fun providerCanceledOccurrenceTime(occurrenceStartMs: Long, isAllDay: Int, timeZone: String): Long {
    return if (isAllDay == 1) {
        convertAllDayBoundary(occurrenceStartMs, safeProviderZone(timeZone), ZoneOffset.UTC)
    } else {
        occurrenceStartMs
    }
}

internal fun exceptionDatesFromProviderExdate(exdate: String?, isAllDay: Int, timeZone: String): String {
    val zone = safeProviderZone(timeZone)
    val values = exdate
        ?.split(',')
        ?.mapNotNull { token -> parseProviderDateToken(token.trim(), isAllDay, zone) }
        ?.distinct()
        ?.sorted()
        .orEmpty()
    return values.joinToString(separator = ",", prefix = "[", postfix = "]")
}

internal fun providerRdateStartTimes(rdate: String?, isAllDay: Int, timeZone: String): List<Long> {
    val zone = safeProviderZone(timeZone)
    return rdate
        ?.split(',')
        ?.mapNotNull { token -> parseProviderDateToken(token.trim(), isAllDay, zone) }
        ?.distinct()
        ?.sorted()
        .orEmpty()
}

internal fun providerEventEndTimeMs(
    startTimeMs: Long,
    dtEndMs: Long?,
    duration: String?,
    lastDateMs: Long?,
    rrule: String?,
): Long {
    dtEndMs?.let { return it }
    providerDurationMillis(duration)?.let { return startTimeMs + it }
    if (rrule.isNullOrBlank()) {
        lastDateMs?.let { return it }
    }
    return startTimeMs + DEFAULT_PROVIDER_EVENT_DURATION_MS
}

internal fun providerDurationMillis(duration: String?): Long? {
    val value = duration?.trim()?.uppercase().takeUnless { it.isNullOrBlank() } ?: return null
    val match = PROVIDER_DURATION_PATTERN.matchEntire(value) ?: return null
    val weeks = match.groupValues[1].toLongOrNull() ?: 0L
    val days = match.groupValues[2].toLongOrNull() ?: 0L
    val hours = match.groupValues[3].toLongOrNull() ?: 0L
    val minutes = match.groupValues[4].toLongOrNull() ?: 0L
    val seconds = match.groupValues[5].toLongOrNull() ?: 0L
    val totalSeconds = weeks * 7L * 24L * 60L * 60L +
        days * 24L * 60L * 60L +
        hours * 60L * 60L +
        minutes * 60L +
        seconds
    return totalSeconds.takeIf { it > 0L }?.times(1000L)
}

private const val DEFAULT_PROVIDER_EVENT_DURATION_MS = 60 * 60 * 1000L

private val PROVIDER_DURATION_PATTERN =
    Regex("""P(?:(\d+)W)?(?:(\d+)D)?(?:T(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?)?""")

private val PROVIDER_UTC_STAMP: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)
private val PROVIDER_LOCAL_STAMP: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
private val PROVIDER_DATE_ONLY: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd")

private fun parseProviderDateToken(token: String, isAllDay: Int, zone: ZoneId): Long? {
    if (token.isBlank()) return null
    if (isAllDay == 1 || (token.length == 8 && !token.contains('T'))) {
        val date = runCatching { LocalDate.parse(token, PROVIDER_DATE_ONLY) }.getOrNull() ?: return null
        return date.atStartOfDay(zone).toInstant().toEpochMilli()
    }
    if (token.endsWith("Z", ignoreCase = true)) {
        val dateTime = runCatching { LocalDateTime.parse(token.uppercase(), PROVIDER_UTC_STAMP) }.getOrNull() ?: return null
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
    }
    val localDateTime = runCatching { LocalDateTime.parse(token, PROVIDER_LOCAL_STAMP) }.getOrNull() ?: return null
    return localDateTime.atZone(zone).toInstant().toEpochMilli()
}

private fun safeProviderZone(id: String): ZoneId {
    return runCatching { ZoneId.of(id) }.getOrDefault(ZoneId.systemDefault())
}

internal fun providerEventTimeZone(isAllDay: Int, timeZone: String): String {
    return if (isAllDay == 1) PROVIDER_UTC_TIMEZONE else timeZone
}

/** Converts an all-day boundary by calendar date, never by elapsed milliseconds. */
internal fun convertAllDayBoundary(timeMs: Long, sourceZone: ZoneId, targetZone: ZoneId): Long {
    val date = Instant.ofEpochMilli(timeMs).atZone(sourceZone).toLocalDate()
    return date.atStartOfDay(targetZone).toInstant().toEpochMilli()
}
