package com.dotfield.dotcal.data.nlp

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Locale

/**
 * Offline natural-language quick-add parser. Pure Kotlin, deterministic, no network.
 *
 * Output is intentionally shaped like the existing event editor draft so Quick Add can keep using
 * the normal save path, default handling, conflict warnings, and calendar-set logic.
 */
data class QuickAddResult(
    val title: String,
    val date: LocalDate,
    val endDate: LocalDate,
    /** null == all-day. */
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val isAllDay: Boolean,
    val rrule: String?,
)

object QuickAddParser {

    private const val DEFAULT_TIMED_HOUR = 9

    private val FILLER = setOf(
        "on", "at", "every", "each", "the", "a", "an", "this", "next",
        "in", "from", "for", "of", "to", "my", "har", "agle", "agli",
    )

    private val WEEKDAYS: List<WeekdayToken> = listOf(
        WeekdayToken(DayOfWeek.MONDAY, "mon", "monday", "mondays", "somvar", "somwaar"),
        WeekdayToken(DayOfWeek.TUESDAY, "tue", "tues", "tuesday", "tuesdays", "mangalvar", "mangalwaar"),
        WeekdayToken(DayOfWeek.WEDNESDAY, "wed", "weds", "wednesday", "wednesdays", "budhvar", "budhwaar"),
        WeekdayToken(DayOfWeek.THURSDAY, "thu", "thur", "thurs", "thursday", "thursdays", "guruvar", "guruwar"),
        WeekdayToken(DayOfWeek.FRIDAY, "fri", "friday", "fridays", "shukravar", "shukrawar"),
        WeekdayToken(DayOfWeek.SATURDAY, "sat", "saturday", "saturdays", "shanivar", "shaniwar"),
        WeekdayToken(DayOfWeek.SUNDAY, "sun", "sunday", "sundays", "ravivar", "raviwar"),
    )

    private val MONTHS: List<MonthToken> = listOf(
        MonthToken(1, "jan", "january"),
        MonthToken(2, "feb", "february"),
        MonthToken(3, "mar", "march"),
        MonthToken(4, "apr", "april"),
        MonthToken(5, "may"),
        MonthToken(6, "jun", "june"),
        MonthToken(7, "jul", "july"),
        MonthToken(8, "aug", "august"),
        MonthToken(9, "sep", "sept", "september"),
        MonthToken(10, "oct", "october"),
        MonthToken(11, "nov", "november"),
        MonthToken(12, "dec", "december"),
    )

    fun parse(input: String, now: LocalDateTime = LocalDateTime.now()): QuickAddResult {
        val today = now.toLocalDate()
        val scanner = Scanner(input)

        val recurrence = parseRecurrence(scanner)
        val allDayKeyword = scanner.take(Regex("""\ball[-\s]?day\b""", RegexOption.IGNORE_CASE)) != null
        val durationMinutes = parseDuration(scanner)
        val timeRange = parseTimeRange(scanner) ?: parseSingleTime(scanner)?.let { ParsedTimeRange(it, null) }

        var startTime = timeRange?.start
        var endTime = timeRange?.end

        var date: LocalDate? = null
        var dateExplicit = false
        when {
            scanner.take(Regex("""\btoday\b""", RegexOption.IGNORE_CASE)) != null -> {
                date = today
                dateExplicit = true
            }
            scanner.take(Regex("""\btonight\b""", RegexOption.IGNORE_CASE)) != null -> {
                date = today
                dateExplicit = true
                if (startTime == null) startTime = LocalTime.of(20, 0)
            }
            scanner.take(Regex("""\b(?:tomorrow|tmrw|tmr|tom|kal)\b""", RegexOption.IGNORE_CASE)) != null -> {
                date = today.plusDays(1)
                dateExplicit = true
            }
            scanner.take(Regex("""\bparso\b""", RegexOption.IGNORE_CASE)) != null -> {
                date = today.plusDays(2)
                dateExplicit = true
            }
        }

        if (date == null) {
            date = parseRelativeDate(scanner, today)?.also { dateExplicit = true }
        }
        if (date == null) {
            date = parseWeekday(scanner, today)?.also { dateExplicit = true }
        }
        if (date == null) {
            date = parseMonthAndDay(scanner, today)?.also { dateExplicit = true }
        }
        if (date == null) {
            date = parseNumericDate(scanner, today)?.also { dateExplicit = true }
        }
        if (date == null) {
            date = parseOrdinalDayOfMonth(scanner, today)?.also { dateExplicit = true }
        }

        if (startTime == null) {
            startTime = parseBareAtTime(scanner)
        }

        if (recurrence.anchorWeekday != null && !dateExplicit) {
            date = nextOrSame(today, recurrence.anchorWeekday)
            dateExplicit = true
        }

        var resolvedDate = date ?: today
        if (!dateExplicit && startTime != null && !resolvedDate.atTime(startTime).isAfter(now)) {
            resolvedDate = resolvedDate.plusDays(1)
        }

        val isAllDay = allDayKeyword || (dateExplicit && startTime == null)
        if (startTime != null && endTime == null) {
            endTime = if (durationMinutes != null) startTime.plusMinutes(durationMinutes) else startTime.plusHours(1)
        }

        val finalStart = if (isAllDay) null else startTime ?: LocalTime.of(DEFAULT_TIMED_HOUR, 0)
        val finalEnd = if (isAllDay || finalStart == null) null else endTime ?: finalStart.plusHours(1)

        return QuickAddResult(
            title = cleanTitle(scanner.work),
            date = resolvedDate,
            endDate = resolvedDate,
            startTime = finalStart,
            endTime = finalEnd,
            isAllDay = isAllDay,
            rrule = recurrence.rrule,
        )
    }

    private fun parseRecurrence(scanner: Scanner): ParsedRecurrence {
        val weekdayRecurrence = scanner.take(
            Regex(
                """\b(?:every|each|har)\s+((?:(?:mon|monday|mondays|somvar|somwaar|tue|tues|tuesday|tuesdays|mangalvar|mangalwaar|wed|weds|wednesday|wednesdays|budhvar|budhwaar|thu|thur|thurs|thursday|thursdays|guruvar|guruwar|fri|friday|fridays|shukravar|shukrawar|sat|saturday|saturdays|shanivar|shaniwar|sun|sunday|sundays|ravivar|raviwar)(?:\s*,?\s+|$))+)\b""",
                RegexOption.IGNORE_CASE,
            ),
        )
        if (weekdayRecurrence != null) {
            val days = weekdaysIn(weekdayRecurrence.groupValues[1])
            if (days.isNotEmpty()) {
                val byDay = days.distinct().sortedBy { it.value }.joinToString(",") { it.rruleCode() }
                return ParsedRecurrence("FREQ=WEEKLY;BYDAY=$byDay", days.first())
            }
        }

        val rrule = when {
            scanner.take(Regex("""\b(?:every\s*day|everyday|daily|roz)\b""", RegexOption.IGNORE_CASE)) != null -> "FREQ=DAILY"
            scanner.take(Regex("""\b(?:every\s*week|weekly)\b""", RegexOption.IGNORE_CASE)) != null -> "FREQ=WEEKLY"
            scanner.take(Regex("""\b(?:every\s*month|monthly)\b""", RegexOption.IGNORE_CASE)) != null -> "FREQ=MONTHLY"
            scanner.take(Regex("""\b(?:every\s*year|yearly|annually)\b""", RegexOption.IGNORE_CASE)) != null -> "FREQ=YEARLY"
            else -> null
        }
        return ParsedRecurrence(rrule, null)
    }

    private fun parseDuration(scanner: Scanner): Long? {
        val match = scanner.take(
            Regex(
                """\b(?:for\s+)?(\d+(?:\.\d+)?)\s*(hours?|hrs?|hr|ghante|ghanta|minutes?|mins?|min)\b""",
                RegexOption.IGNORE_CASE,
            ),
        ) ?: return null
        val amount = match.groupValues[1].toDoubleOrNull() ?: return null
        val unit = match.groupValues[2].lowercase(Locale.US)
        return if (unit.startsWith("h") || unit.startsWith("g")) {
            (amount * 60).toLong()
        } else {
            amount.toLong()
        }.takeIf { it > 0 }
    }

    private fun parseTimeRange(scanner: Scanner): ParsedTimeRange? {
        scanner.take(
            Regex(
                """\b(subah|morning|dopahar|afternoon|shaam|sham|evening|raat|night)?\s*(\d{1,2})(?::(\d{2}))?\s*(am|pm)?\s*(?:-|to|se)\s*(subah|morning|dopahar|afternoon|shaam|sham|evening|raat|night)?\s*(\d{1,2})(?::(\d{2}))?\s*(am|pm)?\b(?![/-]\d)""",
                RegexOption.IGNORE_CASE,
            ),
        )?.let { m ->
            val firstHint = meridiemHint(m.groupValues[1])
            val secondHint = meridiemHint(m.groupValues[5])
            val explicitSecondMeridiem = m.groupValues[8].lowercase(Locale.US)
            val firstMeridiem = m.groupValues[4].lowercase(Locale.US).ifBlank { firstHint.ifBlank { explicitSecondMeridiem } }
            val secondMeridiem = m.groupValues[8].lowercase(Locale.US).ifBlank { secondHint.ifBlank { firstMeridiem } }
            val start = timeOf(
                m.groupValues[2].toIntOrNull() ?: return@let,
                m.groupValues[3].toIntOrNull() ?: 0,
                firstMeridiem,
            ) ?: return@let
            val end = timeOf(
                m.groupValues[6].toIntOrNull() ?: return@let,
                m.groupValues[7].toIntOrNull() ?: 0,
                secondMeridiem,
            ) ?: return@let
            return ParsedTimeRange(start, if (end <= start) end.plusHours(12) else end)
        }
        return null
    }

    private fun parseSingleTime(scanner: Scanner): LocalTime? {
        scanner.take(Regex("""\b(?:noon|midday)\b""", RegexOption.IGNORE_CASE))?.let { return LocalTime.of(12, 0) }
        scanner.take(Regex("""\bmidnight\b""", RegexOption.IGNORE_CASE))?.let { return LocalTime.of(0, 0) }

        scanner.take(
            Regex(
                """\b(subah|morning|dopahar|afternoon|shaam|sham|evening|raat|night)\s+(\d{1,2})(?::(\d{2}))?(?:\s*baje)?\b""",
                RegexOption.IGNORE_CASE,
            ),
        )?.let { m ->
            val hint = meridiemHint(m.groupValues[1])
            return timeOf(m.groupValues[2].toIntOrNull() ?: return@let, m.groupValues[3].toIntOrNull() ?: 0, hint)
                ?: return@let
        }

        scanner.take(Regex("""\b(\d{1,2})[:.](\d{2})\s*(am|pm)?\b""", RegexOption.IGNORE_CASE))?.let { m ->
            return timeOf(
                m.groupValues[1].toIntOrNull() ?: return@let,
                m.groupValues[2].toIntOrNull() ?: return@let,
                m.groupValues[3].lowercase(Locale.US),
            ) ?: return@let
        }
        scanner.take(Regex("""\b(\d{1,2})\s*(am|pm)\b""", RegexOption.IGNORE_CASE))?.let { m ->
            return timeOf(m.groupValues[1].toIntOrNull() ?: return@let, 0, m.groupValues[2].lowercase(Locale.US))
                ?: return@let
        }
        scanner.take(Regex("""\b(\d{1,2})(?:\s*baje)\b""", RegexOption.IGNORE_CASE))?.let { m ->
            val hour = m.groupValues[1].toIntOrNull() ?: return@let
            return timeOf(hour, 0, "")
        }
        return null
    }

    private fun parseBareAtTime(scanner: Scanner): LocalTime? {
        val bareAt = scanner.take(Regex("""\bat\s+(\d{1,2})\b""", RegexOption.IGNORE_CASE)) ?: return null
        val hour = bareAt.groupValues[1].toIntOrNull() ?: return null
        return if (hour in 0..23) LocalTime.of(hour, 0) else null
    }

    private fun timeOf(hour: Int, minute: Int, meridiem: String): LocalTime? {
        if (minute !in 0..59) return null
        val normalized = meridiem.lowercase(Locale.US)
        val h = when (normalized) {
            "am" -> if (hour == 12) 0 else hour
            "pm" -> if (hour == 12) 12 else hour + 12
            else -> hour
        }
        if (h !in 0..23) return null
        return LocalTime.of(h, minute)
    }

    private fun meridiemHint(raw: String): String = when (raw.lowercase(Locale.US)) {
        "subah", "morning" -> "am"
        "dopahar", "afternoon", "shaam", "sham", "evening", "raat", "night" -> "pm"
        else -> ""
    }

    private fun parseRelativeDate(scanner: Scanner, today: LocalDate): LocalDate? {
        val match = scanner.take(Regex("""\bin\s+(\d+)\s+(day|days|week|weeks|month|months|year|years)\b""", RegexOption.IGNORE_CASE))
            ?: return null
        val n = match.groupValues[1].toLongOrNull() ?: return null
        return when (match.groupValues[2].lowercase(Locale.US).trimEnd('s')) {
            "day" -> today.plusDays(n)
            "week" -> today.plusWeeks(n)
            "month" -> today.plusMonths(n)
            "year" -> today.plusYears(n)
            else -> null
        }
    }

    private fun parseWeekday(scanner: Scanner, today: LocalDate): LocalDate? {
        for (token in WEEKDAYS) {
            if (scanner.take(Regex("""\b(?:next|agle|agli)\s+${token.pattern}\b""", RegexOption.IGNORE_CASE)) != null) {
                return nextOrSame(today.plusDays(1), token.day)
            }
        }
        for (token in WEEKDAYS) {
            if (scanner.take(Regex("""(?:\bon\s+)?${token.pattern}\b""", RegexOption.IGNORE_CASE)) != null) {
                return nextOrSame(today, token.day)
            }
        }
        return null
    }

    private fun parseMonthAndDay(scanner: Scanner, today: LocalDate): LocalDate? {
        for (token in MONTHS) {
            scanner.take(Regex("""${token.pattern}\s+(\d{1,2})(?:st|nd|rd|th)?""", RegexOption.IGNORE_CASE))?.let { m ->
                val day = m.groupValues.last().toIntOrNull()
                if (day != null) return dateForMonthDay(token.month, day, today)
            }
            scanner.take(Regex("""(\d{1,2})(?:st|nd|rd|th)?\s+(?:of\s+)?${token.pattern}""", RegexOption.IGNORE_CASE))?.let { m ->
                val day = m.groupValues[1].toIntOrNull()
                if (day != null) return dateForMonthDay(token.month, day, today)
            }
        }
        return null
    }

    private fun parseNumericDate(scanner: Scanner, today: LocalDate): LocalDate? {
        val match = scanner.take(Regex("""\b(\d{1,2})[/-](\d{1,2})(?:[/-](\d{2,4}))?\b""")) ?: return null
        val first = match.groupValues[1].toIntOrNull() ?: return null
        val second = match.groupValues[2].toIntOrNull() ?: return null
        val yearRaw = match.groupValues[3].toIntOrNull()
        val (day, month) = when {
            first > 12 -> first to second
            second > 12 -> second to first
            else -> first to second
        }
        if (month !in 1..12) return null
        val year = when {
            yearRaw == null -> null
            yearRaw < 100 -> 2000 + yearRaw
            else -> yearRaw
        }
        return if (year != null) {
            runCatching { LocalDate.of(year, month, day) }.getOrNull()
        } else {
            dateForMonthDay(month, day, today)
        }
    }

    private fun parseOrdinalDayOfMonth(scanner: Scanner, today: LocalDate): LocalDate? {
        val match = scanner.take(Regex("""\b(\d{1,2})(?:st|nd|rd|th)\b""", RegexOption.IGNORE_CASE)) ?: return null
        val day = match.groupValues[1].toIntOrNull() ?: return null
        if (day !in 1..31) return null
        val thisMonth = runCatching { today.withDayOfMonth(day) }.getOrNull()
        if (thisMonth != null && !thisMonth.isBefore(today)) return thisMonth
        return runCatching { today.plusMonths(1).withDayOfMonth(day) }.getOrNull()
    }

    private fun dateForMonthDay(month: Int, day: Int, today: LocalDate): LocalDate? {
        val thisYear = runCatching { LocalDate.of(today.year, month, day) }.getOrNull() ?: return null
        return if (thisYear.isBefore(today)) {
            runCatching { LocalDate.of(today.year + 1, month, day) }.getOrNull() ?: thisYear
        } else {
            thisYear
        }
    }

    private fun nextOrSame(from: LocalDate, dow: DayOfWeek): LocalDate {
        val delta = ((dow.value - from.dayOfWeek.value) + 7) % 7
        return from.plusDays(delta.toLong())
    }

    private fun cleanTitle(raw: String): String {
        val tokens = raw.split(Regex("""\s+"""))
            .filter { it.isNotBlank() }
            .toMutableList()
        while (tokens.isNotEmpty() && tokens.first().lowercase(Locale.US).trim(',', '.') in FILLER) tokens.removeAt(0)
        while (tokens.isNotEmpty() && tokens.last().lowercase(Locale.US).trim(',', '.') in FILLER) tokens.removeAt(tokens.size - 1)
        return tokens.joinToString(" ").trim().trim(',', '-', ' ')
    }

    private fun weekdaysIn(text: String): List<DayOfWeek> =
        WEEKDAYS.mapNotNull { token ->
            if (Regex("""\b(?:${token.forms.joinToString("|")})\b""", RegexOption.IGNORE_CASE).containsMatchIn(text)) token.day else null
        }

    private fun DayOfWeek.rruleCode(): String = when (this) {
        DayOfWeek.MONDAY -> "MO"
        DayOfWeek.TUESDAY -> "TU"
        DayOfWeek.WEDNESDAY -> "WE"
        DayOfWeek.THURSDAY -> "TH"
        DayOfWeek.FRIDAY -> "FR"
        DayOfWeek.SATURDAY -> "SA"
        DayOfWeek.SUNDAY -> "SU"
    }

    private data class WeekdayToken(val day: DayOfWeek, val forms: List<String>) {
        constructor(day: DayOfWeek, vararg forms: String) : this(day, forms.toList())
        val pattern: String = """\b(?:${forms.sortedByDescending { it.length }.joinToString("|")})\b"""
    }

    private data class MonthToken(val month: Int, val forms: List<String>) {
        constructor(month: Int, vararg forms: String) : this(month, forms.toList())
        val pattern: String = """\b(?:${forms.sortedByDescending { it.length }.joinToString("|")})\b"""
    }

    private data class ParsedTimeRange(val start: LocalTime, val end: LocalTime?)
    private data class ParsedRecurrence(val rrule: String?, val anchorWeekday: DayOfWeek?)

    private class Scanner(text: String) {
        var work: String = text
            private set

        fun take(regex: Regex): MatchResult? {
            val match = regex.find(work) ?: return null
            work = work.substring(0, match.range.first) + " " + work.substring(match.range.last + 1)
            return match
        }
    }
}
