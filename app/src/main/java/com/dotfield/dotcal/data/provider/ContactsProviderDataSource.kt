package com.dotfield.dotcal.data.provider

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.dotfield.dotcal.data.CalendarEvent
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class ContactsProviderDataSource(private val context: Context) {
    private val contentResolver: ContentResolver = context.contentResolver

    fun hasContactsReadPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    fun getBirthdays(accountId: String): List<CalendarEvent> {
        if (!hasContactsReadPermission()) return emptyList()
        val cursor = runCatching {
            contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                BIRTHDAY_PROJECTION,
                "${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.Event.TYPE} = ?",
                arrayOf(
                    ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE,
                    ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString(),
                ),
                "${ContactsContract.Data.DISPLAY_NAME_PRIMARY} ASC",
            )
        }.getOrNull() ?: return emptyList()
        return cursor.use { birthdays ->
            buildList {
                while (birthdays.moveToNext()) {
                    birthdays.toBirthdayEvent(accountId)?.let(::add)
                }
            }
        }
    }

    private fun Cursor.toBirthdayEvent(accountId: String): CalendarEvent? {
        val contactId = getStringOrNull(CONTACT_ID_INDEX) ?: return null
        val rawDate = getStringOrNull(START_DATE_INDEX)?.trim().orEmpty()
        val birthday = rawDate.toBirthdayDate() ?: return null
        val name = getStringOrNull(DISPLAY_NAME_INDEX).takeUnless { it.isNullOrBlank() } ?: "Contact"
        val zoneId = ZoneId.systemDefault()
        val start = birthday.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = birthday.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()
        return CalendarEvent(
            id = birthdayEventId(contactId, rawDate),
            accountId = accountId,
            title = "${name.trim()}'s Birthday",
            description = "",
            location = "",
            startTimeMs = start,
            endTimeMs = end,
            timeZone = zoneId.id,
            isAllDay = 1,
            colorHex = BIRTHDAY_COLOR,
            rrule = "FREQ=YEARLY",
            exceptionDates = "[]",
            source = "BIRTHDAY",
            googleEventId = null,
            googleCalendarId = null,
            syncVersion = rawDate.hashCode(),
            isTask = 0,
            isCompleted = 0,
            completedAtMs = null,
            imageUris = "[]",
            voiceNotePath = null,
            createdAtMs = now,
            updatedAtMs = now,
        )
    }

    private fun String.toBirthdayDate(): LocalDate? {
        return when {
            startsWith("--") -> runCatching {
                val parts = removePrefix("--").split("-")
                LocalDate.of(BIRTHDAY_BASE_YEAR, parts[0].toInt(), parts[1].toInt())
            }.getOrNull()
            length == 5 && this[2] == '-' -> runCatching {
                val parts = split("-")
                LocalDate.of(BIRTHDAY_BASE_YEAR, parts[0].toInt(), parts[1].toInt())
            }.getOrNull()
            length == 8 && all(Char::isDigit) -> runCatching {
                LocalDate.parse(this, COMPACT_DATE_FORMAT)
            }.getOrNull()
            else -> runCatching { LocalDate.parse(this) }.getOrNull()
        }
    }

    private fun Cursor.getStringOrNull(index: Int): String? = if (isNull(index)) null else getString(index)

    companion object {
        const val BIRTHDAY_ACCOUNT_ID = "birthday-calendar"
        const val BIRTHDAY_COLOR = "#FF3B30"
        // Contact birthdays with no year are stored at this placeholder year; "On This Day"
        // uses it to decide whether an age ("turns 27") can be computed.
        const val BIRTHDAY_BASE_YEAR = 2000
        private val COMPACT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.US)
        private val BIRTHDAY_PROJECTION = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.DISPLAY_NAME_PRIMARY,
            ContactsContract.CommonDataKinds.Event.START_DATE,
        )
        private const val CONTACT_ID_INDEX = 0
        private const val DISPLAY_NAME_INDEX = 1
        private const val START_DATE_INDEX = 2
    }
}

private fun birthdayEventId(contactId: String, rawDate: String): String {
    return "birthday-contact-$contactId-${rawDate.hashCode()}"
}
