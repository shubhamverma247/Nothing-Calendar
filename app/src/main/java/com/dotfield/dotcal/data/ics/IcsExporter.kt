package com.dotfield.dotcal.data.ics

import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventReminder
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Serializes [CalendarEvent] rows into an RFC 5545 iCalendar (`.ics`) document.
 *
 * Design notes:
 *  - Pure Kotlin, no third-party dependency (keeps APK lean; DotCal only supports a small
 *    recurrence subset so a full library is unnecessary).
 *  - Events export as VEVENT, tasks (`isTask == 1`) export as VTODO.
 *  - Only master rows should be passed in; recurrence expansion is intentionally NOT applied so
 *    RRULE round-trips as a single VEVENT with a RRULE line (matching import).
 *  - The event [CalendarEvent.id] is written as UID so re-import can upsert by UID.
 */
object IcsExporter {

    private val UTC_STAMP: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)
    private val LOCAL_STAMP: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
    private val DATE_ONLY: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyyMMdd")

    private const val PRODID = "-//Dotfield Studio//DotCal//EN"

    /** Builds the full VCALENDAR text for [events]. Master rows only. */
    fun export(events: List<CalendarEvent>, remindersByEventId: Map<String, List<EventReminder>> = emptyMap()): String {
        val sb = StringBuilder()
        sb.appendCrlf("BEGIN:VCALENDAR")
        sb.appendCrlf("VERSION:2.0")
        sb.appendCrlf("PRODID:$PRODID")
        sb.appendCrlf("CALSCALE:GREGORIAN")
        events.forEach { event ->
            val reminders = remindersByEventId[event.id].orEmpty()
            if (event.isTask == 1) appendTodo(sb, event, reminders) else appendEvent(sb, event, reminders)
        }
        sb.appendCrlf("END:VCALENDAR")
        return sb.toString()
    }

    private fun appendEvent(sb: StringBuilder, event: CalendarEvent, reminders: List<EventReminder>) {
        val zone = safeZone(event.timeZone)
        val nowStamp = UTC_STAMP.format(Instant.now())
        sb.appendCrlf("BEGIN:VEVENT")
        sb.appendCrlf("UID:${escapeText(event.id)}")
        sb.appendCrlf("DTSTAMP:$nowStamp")
        appendStartEnd(sb, event, zone)
        appendProp(sb, "SUMMARY", event.title)
        if (event.description.isNotBlank()) appendProp(sb, "DESCRIPTION", event.description)
        if (event.location.isNotBlank()) appendProp(sb, "LOCATION", event.location)
        event.rrule?.trim()?.takeIf { it.isNotEmpty() }?.let { sb.appendCrlf("RRULE:$it") }
        appendExdates(sb, event, zone)
        appendReminders(sb, reminders)
        sb.appendCrlf("END:VEVENT")
    }

    private fun appendTodo(sb: StringBuilder, event: CalendarEvent, reminders: List<EventReminder>) {
        val zone = safeZone(event.timeZone)
        val nowStamp = UTC_STAMP.format(Instant.now())
        sb.appendCrlf("BEGIN:VTODO")
        sb.appendCrlf("UID:${escapeText(event.id)}")
        sb.appendCrlf("DTSTAMP:$nowStamp")
        // A task may have no due date (startTimeMs == 0).
        if (event.startTimeMs > 0L) {
            if (event.isAllDay == 1) {
                val date = Instant.ofEpochMilli(event.startTimeMs).atZone(zone).toLocalDate()
                sb.appendCrlf("DUE;VALUE=DATE:${DATE_ONLY.format(date)}")
            } else {
                sb.appendCrlf("DUE:${UTC_STAMP.format(Instant.ofEpochMilli(event.startTimeMs))}")
            }
        }
        appendProp(sb, "SUMMARY", event.title)
        sb.appendCrlf("STATUS:${if (event.isCompleted == 1) "COMPLETED" else "NEEDS-ACTION"}")
        if (event.isCompleted == 1) {
            event.completedAtMs?.let { sb.appendCrlf("COMPLETED:${UTC_STAMP.format(Instant.ofEpochMilli(it))}") }
        }
        event.rrule?.trim()?.takeIf { it.isNotEmpty() }?.let { sb.appendCrlf("RRULE:$it") }
        appendReminders(sb, reminders)
        sb.appendCrlf("END:VTODO")
    }

    private fun appendStartEnd(sb: StringBuilder, event: CalendarEvent, zone: ZoneId) {
        if (event.isAllDay == 1) {
            val startDate = Instant.ofEpochMilli(event.startTimeMs).atZone(zone).toLocalDate()
            // Exclusive end date per RFC 5545 for all-day events.
            val endDate = Instant.ofEpochMilli(event.endTimeMs).atZone(zone).toLocalDate()
            sb.appendCrlf("DTSTART;VALUE=DATE:${DATE_ONLY.format(startDate)}")
            sb.appendCrlf("DTEND;VALUE=DATE:${DATE_ONLY.format(endDate)}")
        } else {
            val tzid = zone.id
            val start = Instant.ofEpochMilli(event.startTimeMs).atZone(zone).toLocalDateTime()
            val end = Instant.ofEpochMilli(event.endTimeMs).atZone(zone).toLocalDateTime()
            sb.appendCrlf("DTSTART;TZID=$tzid:${LOCAL_STAMP.format(start)}")
            sb.appendCrlf("DTEND;TZID=$tzid:${LOCAL_STAMP.format(end)}")
        }
    }

    private fun appendExdates(sb: StringBuilder, event: CalendarEvent, zone: ZoneId) {
        val exceptions = event.exceptionDates
            .removePrefix("[").removeSuffix("]")
            .split(',')
            .mapNotNull { it.trim().toLongOrNull() }
        if (exceptions.isEmpty()) return
        val values = exceptions.joinToString(",") { ms ->
            if (event.isAllDay == 1) {
                DATE_ONLY.format(Instant.ofEpochMilli(ms).atZone(zone).toLocalDate())
            } else {
                UTC_STAMP.format(Instant.ofEpochMilli(ms))
            }
        }
        if (event.isAllDay == 1) {
            sb.appendCrlf("EXDATE;VALUE=DATE:$values")
        } else {
            sb.appendCrlf("EXDATE:$values")
        }
    }

    private fun appendProp(sb: StringBuilder, name: String, value: String) {
        sb.appendCrlf(foldLine("$name:${escapeText(value)}"))
    }

    private fun appendReminders(sb: StringBuilder, reminders: List<EventReminder>) {
        reminders.distinctBy { it.minutesBefore }.sortedBy { it.minutesBefore }.forEach { reminder ->
            sb.appendCrlf("BEGIN:VALARM")
            sb.appendCrlf("ACTION:DISPLAY")
            sb.appendCrlf("TRIGGER:${relativeTrigger(reminder.minutesBefore)}")
            appendProp(sb, "DESCRIPTION", "Reminder")
            sb.appendCrlf("END:VALARM")
        }
    }

    private fun relativeTrigger(minutesBefore: Int): String =
        if (minutesBefore <= 0) "PT0M" else "-PT${minutesBefore}M"

    /** Escapes text per RFC 5545 (backslash, comma, semicolon, newlines). */
    private fun escapeText(value: String): String = value
        .replace("\\", "\\\\")
        .replace("\n", "\\n")
        .replace(",", "\\,")
        .replace(";", "\\;")

    /** Folds long content lines at 75 octets by inserting CRLF + a leading space. */
    private fun foldLine(line: String): String {
        if (line.length <= 75) return line
        val sb = StringBuilder()
        var index = 0
        while (index < line.length) {
            val end = (index + if (index == 0) 75 else 74).coerceAtMost(line.length)
            if (index > 0) sb.append("\r\n ")
            sb.append(line, index, end)
            index = end
        }
        return sb.toString()
    }

    private fun safeZone(id: String): ZoneId = runCatching { ZoneId.of(id) }.getOrDefault(ZoneId.systemDefault())

    private fun StringBuilder.appendCrlf(text: String) {
        append(text).append("\r\n")
    }
}
