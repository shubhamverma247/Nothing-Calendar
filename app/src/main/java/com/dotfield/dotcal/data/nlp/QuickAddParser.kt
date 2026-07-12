package com.dotfield.dotcal.data.nlp

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

/**
 * Offline natural-language quick-add parser. Pure Kotlin, no network, no new dependency.
 *
 * Turns a single line like `gym every mon 7am` into the same fields the event editor collects,
 * so the result can pre-fill [com.dotfield.dotcal.data.EventEditorData] and flow through the normal
 * save path. Recurrence is intentionally limited to the app's real engine: bare `FREQ=...` anchored
 * off the event's own start date (no BYDAY/INTERVAL). `every <weekday>` is expressed as
 * `FREQ=WEEKLY` with the start date pinned to that weekday — which the anchor-based expander then
 * repeats correctly.
 *
 * Everything the parser recognizes is stripped from the title. Nothing throws: unrecognized input
 * simply falls back to a timed event today at 9am (matching the legacy quick-add default).
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

    /** Words safe to trim from the leading/trailing edge of the leftover title. */
    private val FILLER = setOf(
        "on", "at", "every", "each", "the", "a", "an", "this", "next",
        "in", "from", "for", "of", "to", "my",
    )

    private val WEEKDAYS: List<Pair<Regex, DayOfWeek>> = listOf(
        weekdayRegex("mon", "monday", "mondays") to DayOfWeek.MONDAY,
        weekdayRegex("tue", "tues", "tuesday", "tuesdays") to DayOfWeek.TUESDAY,
        weekdayRegex("wed", "weds", "wednesday", "wednesdays") to DayOfWeek.WEDNESDAY,
        weekdayRegex("thu", "thur", "thurs", "thursday", "thursdays") to DayOfWeek.THURSDAY,
        weekdayRegex("fri", "friday", "fridays") to DayOfWeek.FRIDAY,
        weekdayRegex("sat", "saturday", "saturdays") to DayOfWeek.SATURDAY,
        weekdayRegex("sun", "sunday", "sundays") to DayOfWeek.SUNDAY,
    )

    private val MONTHS: List<Pair<Regex, Int>> = listOf(
        monthRegex("jan", "january") to 1,
        monthRegex("feb", "february") to 2,
        monthRegex("mar", "march") to 3,
        monthRegex("apr", "april") to 4,
        monthRegex("may") to 5,
        monthRegex("jun", "june") to 6,
        monthRegex("jul", "july") to 7,
        monthRegex("aug", "august") to 8,
        monthRegex("sep", "sept", "september") to 9,
        monthRegex("oct", "october") to 10,
        monthRegex("nov", "november") to 11,
        monthRegex("dec", "december") to 12,
    )

    fun parse(input: String, now: LocalDateTime = LocalDateTime.now()): QuickAddResult {
        val today = now.toLocalDate()
        val scanner = Scanner(input)

        // 1. Recurrence — weekday-scoped first ("every monday"), then plain periods.
        var rrule: String? = null
        var recurrenceWeekday: DayOfWeek? = null
        for ((regex, dow) in WEEKDAYS) {
            val everyWeekday = Regex("""\b(?:every|each)\s+${regex.pattern}""", RegexOption.IGNORE_CASE)
            if (scanner.take(everyWeekday) != null) {
                rrule = "FREQ=WEEKLY"
                recurrenceWeekday = dow
                break
            }
        }
        if (rrule == null) {
            rrule = when {
                scanner.take(Regex("""\b(?:every\s*day|everyday|daily)\b""", RegexOption.IGNORE_CASE)) != null -> "FREQ=DAILY"
                scanner.take(Regex("""\b(?:every\s*week|weekly)\b""", RegexOption.IGNORE_CASE)) != null -> "FREQ=WEEKLY"
                scanner.take(Regex("""\b(?:every\s*month|monthly)\b""", RegexOption.IGNORE_CASE)) != null -> "FREQ=MONTHLY"
                scanner.take(Regex("""\b(?:every\s*year|yearly|annually)\b""", RegexOption.IGNORE_CASE)) != null -> "FREQ=YEARLY"
                else -> null
            }
        }

        // 2. Explicit all-day request.
        val allDayKeyword = scanner.take(Regex("""\ball[-\s]?day\b""", RegexOption.IGNORE_CASE)) != null

        // 3. Time (unambiguous forms first).
        var startTime: LocalTime? = parseTime(scanner)

        // 4. Date.
        var date: LocalDate? = null
        var dateExplicit = false

        when {
            scanner.take(Regex("""\btoday\b""", RegexOption.IGNORE_CASE)) != null -> {
                date = today; dateExplicit = true
            }
            scanner.take(Regex("""\btonight\b""", RegexOption.IGNORE_CASE)) != null -> {
                date = today; dateExplicit = true
                if (startTime == null) startTime = LocalTime.of(20, 0)
            }
            scanner.take(Regex("""\b(?:tomorrow|tmrw|tmr|tom)\b""", RegexOption.IGNORE_CASE)) != null -> {
                date = today.plusDays(1); dateExplicit = true
            }
        }

        if (date == null) {
            val inN = scanner.take(Regex("""\bin\s+(\d+)\s+(day|days|week|weeks|month|months|year|years)\b""", RegexOption.IGNORE_CASE))
            if (inN != null) {
                val n = inN.groupValues[1].toLongOrNull() ?: 0L
                date = when (inN.groupValues[2].lowercase().trimEnd('s')) {
                    "day" -> today.plusDays(n)
                    "week" -> today.plusWeeks(n)
                    "month" -> today.plusMonths(n)
                    "year" -> today.plusYears(n)
                    else -> today
                }
                dateExplicit = true
            }
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

        // 5. Bare "at N" fallback once date numbers are consumed.
        if (startTime == null) {
            val bareAt = scanner.take(Regex("""\bat\s+(\d{1,2})\b""", RegexOption.IGNORE_CASE))
            if (bareAt != null) {
                val h = bareAt.groupValues[1].toIntOrNull()
                if (h != null && h in 0..23) startTime = LocalTime.of(h, 0)
            }
        }

        // 6. A weekday-scoped recurrence pins the start date to that weekday when none was given.
        if (recurrenceWeekday != null && !dateExplicit) {
            date = nextOrSame(today, recurrenceWeekday)
            dateExplicit = true
        }

        var resolvedDate = date ?: today
        // Time given but no explicit date, and it already passed today -> roll to tomorrow.
        if (!dateExplicit && startTime != null && resolvedDate.atTime(startTime).isBefore(now)) {
            resolvedDate = resolvedDate.plusDays(1)
        }
        val isAllDay = allDayKeyword || (dateExplicit && startTime == null)

        val finalStart: LocalTime?
        val finalEnd: LocalTime?
        if (isAllDay) {
            finalStart = null
            finalEnd = null
        } else {
            val s = startTime ?: LocalTime.of(DEFAULT_TIMED_HOUR, 0)
            finalStart = s
            finalEnd = s.plusHours(1)
        }

        return QuickAddResult(
            title = cleanTitle(scanner.work),
            date = resolvedDate,
            endDate = resolvedDate,
            startTime = finalStart,
            endTime = finalEnd,
            isAllDay = isAllDay,
            rrule = rrule,
        )
    }

    // ----- time -----

    private fun parseTime(scanner: Scanner): LocalTime? {
        scanner.take(Regex("""\b(?:noon|midday)\b""", RegexOption.IGNORE_CASE))?.let { return LocalTime.of(12, 0) }
        scanner.take(Regex("""\bmidnight\b""", RegexOption.IGNORE_CASE))?.let { return LocalTime.of(0, 0) }

        // 7:30pm / 7.30 pm / 19:00
        scanner.take(Regex("""\b(\d{1,2})[:.](\d{2})\s*(am|pm)?\b""", RegexOption.IGNORE_CASE))?.let { m ->
            val hour = m.groupValues[1].toIntOrNull() ?: return@let
            val minute = m.groupValues[2].toIntOrNull() ?: return@let
            val ampm = m.groupValues[3].lowercase()
            timeOf(hour, minute, ampm)?.let { return it }
        }
        // 7am / 7 pm
        scanner.take(Regex("""\b(\d{1,2})\s*(am|pm)\b""", RegexOption.IGNORE_CASE))?.let { m ->
            val hour = m.groupValues[1].toIntOrNull() ?: return@let
            timeOf(hour, 0, m.groupValues[2].lowercase())?.let { return it }
        }
        return null
    }

    private fun timeOf(hour: Int, minute: Int, ampm: String): LocalTime? {
        if (minute !in 0..59) return null
        val h = when {
            ampm == "am" -> if (hour == 12) 0 else hour
            ampm == "pm" -> if (hour == 12) 12 else hour + 12
            else -> hour
        }
        if (h !in 0..23) return null
        return LocalTime.of(h, minute)
    }

    // ----- date -----

    private fun parseWeekday(scanner: Scanner, today: LocalDate): LocalDate? {
        for ((regex, dow) in WEEKDAYS) {
            val next = Regex("""\bnext\s+${regex.pattern}""", RegexOption.IGNORE_CASE)
            if (scanner.take(next) != null) {
                // "next monday" == the soonest occurrence strictly after today.
                return nextOrSame(today.plusDays(1), dow)
            }
        }
        for ((regex, dow) in WEEKDAYS) {
            val bare = Regex("""(?:\bon\s+)?${regex.pattern}""", RegexOption.IGNORE_CASE)
            if (scanner.take(bare) != null) {
                return nextOrSame(today, dow)
            }
        }
        return null
    }

    private fun parseMonthAndDay(scanner: Scanner, today: LocalDate): LocalDate? {
        for ((regex, month) in MONTHS) {
            // "jul 4", "july 4th"
            val monthDay = Regex("""${regex.pattern}\s+(\d{1,2})(?:st|nd|rd|th)?""", RegexOption.IGNORE_CASE)
            scanner.take(monthDay)?.let { m ->
                val day = m.groupValues.last().toIntOrNull()
                if (day != null) return dateForMonthDay(month, day, today)
            }
            // "4 july", "4th of jul"
            val dayMonth = Regex("""(\d{1,2})(?:st|nd|rd|th)?\s+(?:of\s+)?${regex.pattern}""", RegexOption.IGNORE_CASE)
            scanner.take(dayMonth)?.let { m ->
                val day = m.groupValues[1].toIntOrNull()
                if (day != null) return dateForMonthDay(month, day, today)
            }
        }
        return null
    }

    private fun parseNumericDate(scanner: Scanner, today: LocalDate): LocalDate? {
        val m = scanner.take(Regex("""\b(\d{1,2})/(\d{1,2})(?:/(\d{2,4}))?\b""")) ?: return null
        val a = m.groupValues[1].toIntOrNull() ?: return null
        val b = m.groupValues[2].toIntOrNull() ?: return null
        val yearRaw = m.groupValues[3].toIntOrNull()
        // Day-first only when the first field can't be a month.
        val (month, day) = if (a > 12) b to a else a to b
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
        val m = scanner.take(Regex("""\b(\d{1,2})(?:st|nd|rd|th)\b""", RegexOption.IGNORE_CASE)) ?: return null
        val day = m.groupValues[1].toIntOrNull() ?: return null
        if (day !in 1..31) return null
        val thisMonth = runCatching { today.withDayOfMonth(day) }.getOrNull()
        if (thisMonth != null && !thisMonth.isBefore(today)) return thisMonth
        return runCatching { today.plusMonths(1).withDayOfMonth(day) }.getOrNull()
    }

    /** Builds the given month/day, rolling to next year if it has already passed this year. */
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

    // ----- title -----

    private fun cleanTitle(raw: String): String {
        var tokens = raw.split(Regex("""\s+""")).filter { it.isNotBlank() }.toMutableList()
        while (tokens.isNotEmpty() && tokens.first().lowercase().trim(',', '.') in FILLER) tokens.removeAt(0)
        while (tokens.isNotEmpty() && tokens.last().lowercase().trim(',', '.') in FILLER) tokens.removeAt(tokens.size - 1)
        return tokens.joinToString(" ").trim().trim(',', '-', ' ')
    }

    // ----- helpers -----

    private fun weekdayRegex(vararg forms: String): Regex =
        Regex("""\b(?:${forms.sortedByDescending { it.length }.joinToString("|")})\b""", RegexOption.IGNORE_CASE)

    private fun monthRegex(vararg forms: String): Regex =
        Regex("""\b(?:${forms.sortedByDescending { it.length }.joinToString("|")})\b""", RegexOption.IGNORE_CASE)

    /** Consumes the first match of a regex from the working string, replacing it with a space. */
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
