package com.dotfield.dotcal.data.ics

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * A single parsed VEVENT/VTODO component. Times are already resolved to epoch millis so the
 * repository can map straight onto CalendarEvent without further timezone work.
 */
data class ParsedIcsItem(
    val uid: String?,
    val isTask: Boolean,
    val title: String,
    val description: String,
    val location: String,
    val startTimeMs: Long,
    val endTimeMs: Long,
    val timeZone: String,
    val isAllDay: Boolean,
    val rrule: String?,
    val exceptionMs: List<Long>,
    val isCompleted: Boolean,
    val completedAtMs: Long?,
    val reminderMinutes: List<Int>,
)

/**
 * Minimal RFC 5545 reader for the subset DotCal produces/consumes: VEVENT + VTODO with
 * DTSTART/DTEND/DUE, SUMMARY/DESCRIPTION/LOCATION, RRULE, EXDATE, STATUS, VALARM.
 *
 * Unsupported properties are ignored rather than failing, so importing a foreign `.ics` degrades
 * gracefully instead of crashing. Unfoldable/garbage components are skipped.
 */
object IcsParser {

    private val DT_UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
    private val DT_LOCAL = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
    private val DATE_ONLY = DateTimeFormatter.ofPattern("yyyyMMdd")

    fun parse(text: String): List<ParsedIcsItem> {
        val lines = unfold(text)
        val items = mutableListOf<ParsedIcsItem>()
        var inComponent: String? = null
        var props = mutableListOf<Pair<String, String>>()
        var inAlarm = false
        var alarmProps = mutableListOf<Pair<String, String>>()
        var alarms = mutableListOf<List<Pair<String, String>>>()
        for (line in lines) {
            val upper = line.trimEnd()
            when {
                upper == "BEGIN:VEVENT" -> {
                    inComponent = "VEVENT"
                    props = mutableListOf()
                    alarms = mutableListOf()
                    inAlarm = false
                }
                upper == "BEGIN:VTODO" -> {
                    inComponent = "VTODO"
                    props = mutableListOf()
                    alarms = mutableListOf()
                    inAlarm = false
                }
                upper == "BEGIN:VALARM" && inComponent != null -> {
                    inAlarm = true
                    alarmProps = mutableListOf()
                }
                upper == "END:VALARM" && inComponent != null && inAlarm -> {
                    alarms += alarmProps
                    inAlarm = false
                }
                upper == "END:VEVENT" || upper == "END:VTODO" -> {
                    val component = inComponent
                    if (component != null) {
                        buildItem(component, props, alarms)?.let { items += it }
                    }
                    inComponent = null
                    inAlarm = false
                }
                inComponent != null -> {
                    val colon = line.indexOf(':')
                    if (colon > 0) {
                        val prop = line.substring(0, colon) to line.substring(colon + 1)
                        if (inAlarm) alarmProps += prop else props += prop
                    }
                }
            }
        }
        return items
    }

    private fun buildItem(
        component: String,
        props: List<Pair<String, String>>,
        alarms: List<List<Pair<String, String>>>,
    ): ParsedIcsItem? {
        var uid: String? = null
        var summary = ""
        var description = ""
        var location = ""
        var rrule: String? = null
        var status: String? = null
        var completedAtMs: Long? = null
        val exceptions = mutableListOf<Long>()

        var startMs: Long? = null
        var endMs: Long? = null
        var dueMs: Long? = null
        var isAllDay = false
        var zoneId = ZoneId.systemDefault()

        for ((rawKey, rawValue) in props) {
            val key = rawKey.substringBefore(';').uppercase()
            val params = parseParams(rawKey)
            val value = unescape(rawValue.trim())
            when (key) {
                "UID" -> uid = value
                "SUMMARY" -> summary = value
                "DESCRIPTION" -> description = value
                "LOCATION" -> location = value
                "RRULE" -> rrule = value.trim().takeIf { it.isNotEmpty() }
                "STATUS" -> status = value.uppercase()
                "COMPLETED" -> completedAtMs = parseInstant(value, ZoneId.systemDefault())?.first
                "DTSTART" -> {
                    val parsed = parseDateOrDateTime(value, params)
                    if (parsed != null) {
                        startMs = parsed.epochMs
                        isAllDay = parsed.dateOnly
                        parsed.zone?.let { zoneId = it }
                    }
                }
                "DTEND" -> parseDateOrDateTime(value, params)?.let { endMs = it.epochMs }
                "DUE" -> {
                    val parsed = parseDateOrDateTime(value, params)
                    if (parsed != null) {
                        dueMs = parsed.epochMs
                        isAllDay = parsed.dateOnly
                        parsed.zone?.let { zoneId = it }
                    }
                }
                "EXDATE" -> {
                    value.split(',').forEach { token ->
                        parseDateOrDateTime(token.trim(), params)?.let { exceptions += it.epochMs }
                    }
                }
            }
        }

        val isTask = component == "VTODO"
        if (summary.isBlank() && uid == null) return null

        val resolvedStart: Long
        val resolvedEnd: Long
        if (isTask) {
            resolvedStart = dueMs ?: startMs ?: 0L
            resolvedEnd = if (resolvedStart > 0L) resolvedStart else 0L
        } else {
            val s = startMs ?: return null
            resolvedStart = s
            resolvedEnd = when {
                endMs != null && endMs > s -> endMs
                isAllDay -> plusDaysMs(s, zoneId, 1)
                else -> s + 60 * 60_000L // default 1h
            }
        }

        return ParsedIcsItem(
            uid = uid,
            isTask = isTask,
            title = summary.ifBlank { "(untitled)" },
            description = description,
            location = location,
            startTimeMs = resolvedStart,
            endTimeMs = resolvedEnd,
            timeZone = zoneId.id,
            isAllDay = isAllDay,
            rrule = normalizeRrule(rrule),
            exceptionMs = exceptions,
            isCompleted = status == "COMPLETED",
            completedAtMs = if (status == "COMPLETED") (completedAtMs ?: System.currentTimeMillis()) else null,
            reminderMinutes = alarms.mapNotNull(::parseAlarmMinutes).distinct().sorted(),
        )
    }

    private fun parseAlarmMinutes(props: List<Pair<String, String>>): Int? {
        val action = props.firstOrNull { it.first.substringBefore(';').equals("ACTION", ignoreCase = true) }?.second?.trim()
        if (action != null && !action.equals("DISPLAY", ignoreCase = true)) return null
        val trigger = props.firstOrNull { it.first.substringBefore(';').equals("TRIGGER", ignoreCase = true) }?.second?.trim()
            ?: return null
        return parseRelativeTriggerMinutes(trigger)
    }

    private fun parseRelativeTriggerMinutes(value: String): Int? {
        val upper = value.uppercase()
        val negative = upper.startsWith("-")
        val duration = upper.removePrefix("-").removePrefix("+")
        if (!duration.startsWith("P")) return null
        val weeks = Regex("""(\d+)W""").find(duration)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        val days = Regex("""(\d+)D""").find(duration)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        val hours = Regex("""(\d+)H""").find(duration)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        val minutes = Regex("""(\d+)M""").find(duration.substringAfter('T', ""))?.groupValues?.get(1)?.toLongOrNull() ?: 0L
        val totalMinutes = weeks * 7 * 24 * 60 + days * 24 * 60 + hours * 60 + minutes
        if (totalMinutes > Int.MAX_VALUE) return null
        return when {
            negative -> totalMinutes.toInt()
            totalMinutes == 0L -> 0
            else -> null
        }
    }

    /**
     * DotCal only supports simple FREQ=DAILY/WEEKLY/MONTHLY/YEARLY. Keep just the FREQ token so an
     * imported complex RRULE still produces a supported repeat instead of being dropped or breaking
     * the recurrence expander.
     */
    private fun normalizeRrule(rrule: String?): String? {
        if (rrule == null) return null
        val freq = rrule.split(';')
            .firstOrNull { it.uppercase().startsWith("FREQ=") }
            ?.uppercase()
            ?: return null
        return when (freq.removePrefix("FREQ=")) {
            "DAILY" -> "FREQ=DAILY"
            "WEEKLY" -> "FREQ=WEEKLY"
            "MONTHLY" -> "FREQ=MONTHLY"
            "YEARLY" -> "FREQ=YEARLY"
            else -> null
        }
    }

    private data class ParsedDate(val epochMs: Long, val dateOnly: Boolean, val zone: ZoneId?)

    private fun parseDateOrDateTime(value: String, params: Map<String, String>): ParsedDate? {
        val tzid = params["TZID"]
        val isDate = params["VALUE"].equals("DATE", ignoreCase = true) || (value.length == 8 && !value.contains('T'))
        if (isDate) {
            val date = runCatching { LocalDate.parse(value, DATE_ONLY) }.getOrNull() ?: return null
            val zone = tzid?.let { safeZone(it) } ?: ZoneId.systemDefault()
            return ParsedDate(date.atStartOfDay(zone).toInstant().toEpochMilli(), true, zone)
        }
        val zone = tzid?.let { safeZone(it) } ?: ZoneId.systemDefault()
        return parseInstant(value, zone)?.let { (ms, usedZone) -> ParsedDate(ms, false, usedZone) }
    }

    private fun parseInstant(value: String, fallbackZone: ZoneId): Pair<Long, ZoneId>? {
        if (value.endsWith("Z")) {
            val dt = runCatching { LocalDateTime.parse(value, DT_UTC) }.getOrNull() ?: return null
            return dt.toInstant(ZoneOffset.UTC).toEpochMilli() to fallbackZone
        }
        val dt = runCatching { LocalDateTime.parse(value, DT_LOCAL) }.getOrNull() ?: return null
        return dt.atZone(fallbackZone).toInstant().toEpochMilli() to fallbackZone
    }

    private fun plusDaysMs(ms: Long, zone: ZoneId, days: Long): Long =
        Instant.ofEpochMilli(ms).atZone(zone).plusDays(days).toInstant().toEpochMilli()

    private fun parseParams(rawKey: String): Map<String, String> {
        val parts = rawKey.split(';')
        if (parts.size <= 1) return emptyMap()
        return parts.drop(1).mapNotNull { part ->
            val eq = part.indexOf('=')
            if (eq <= 0) null else part.substring(0, eq).uppercase() to part.substring(eq + 1)
        }.toMap()
    }

    private fun unescape(value: String): String = value
        .replace("\\n", "\n")
        .replace("\\N", "\n")
        .replace("\\,", ",")
        .replace("\\;", ";")
        .replace("\\\\", "\\")

    /** Reverses RFC 5545 line folding: a line starting with space/tab continues the previous one. */
    private fun unfold(text: String): List<String> {
        val raw = text.replace("\r\n", "\n").replace("\r", "\n").split('\n')
        val result = mutableListOf<String>()
        for (line in raw) {
            if ((line.startsWith(" ") || line.startsWith("\t")) && result.isNotEmpty()) {
                result[result.lastIndex] = result.last() + line.substring(1)
            } else {
                result += line
            }
        }
        return result
    }

    private fun safeZone(id: String): ZoneId = runCatching { ZoneId.of(id) }.getOrDefault(ZoneId.systemDefault())
}
