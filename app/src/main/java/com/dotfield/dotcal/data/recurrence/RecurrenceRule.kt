package com.dotfield.dotcal.data.recurrence

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/**
 * Pure-Kotlin RRULE model for DotCal. No dependency, mirrors QuickAddParser / BackupSerializer style.
 *
 * Supports the subset DotCal's expander can actually honor:
 *   FREQ = DAILY | WEEKLY | MONTHLY | YEARLY
 *   INTERVAL = every N periods (>= 1)
 *   BYDAY    = weekly multi-weekday (MO,WE,FR) OR a single ordinal weekday for monthly (2MO, -1FR)
 *   COUNT    = stop after N occurrences
 *   UNTIL    = stop on/after a date (date-only YYYYMMDD, or the date part of a datetime)
 *
 * Backward compatibility invariant: a bare "FREQ=DAILY" (interval 1, no BYDAY/COUNT/UNTIL) parses to
 * interval=1 with empty extras and [toRRule] returns exactly "FREQ=DAILY" again. Existing events keep working.
 */
enum class RecurrenceFreq { DAILY, WEEKLY, MONTHLY, YEARLY }

/**
 * @param ordinal null = every occurrence of [day] (weekly BYDAY). 1..5 = nth weekday of month. -1 = last weekday.
 */
data class ByDay(val ordinal: Int?, val day: DayOfWeek)

data class RecurrenceRule(
    val freq: RecurrenceFreq,
    val interval: Int = 1,
    val byDay: List<ByDay> = emptyList(),
    val count: Int? = null,
    val until: LocalDate? = null,
) {
    val isSimple: Boolean
        get() = interval <= 1 && byDay.isEmpty() && count == null && until == null

    fun toRRule(): String {
        val parts = mutableListOf("FREQ=${freq.name}")
        if (interval > 1) parts += "INTERVAL=$interval"
        if (byDay.isNotEmpty()) parts += "BYDAY=" + byDay.joinToString(",") { it.toToken() }
        count?.let { parts += "COUNT=$it" }
        until?.let { parts += "UNTIL=" + it.format(UNTIL_FORMAT) }
        return parts.joinToString(";")
    }

    /** Short human sentence for pickers / detail rows. e.g. "Every 2 weeks on Mon, Fri · 10 times". */
    fun humanLabel(): String {
        val base = when (freq) {
            RecurrenceFreq.DAILY -> if (interval <= 1) "Daily" else "Every $interval days"
            RecurrenceFreq.WEEKLY -> if (interval <= 1) "Weekly" else "Every $interval weeks"
            RecurrenceFreq.MONTHLY -> if (interval <= 1) "Monthly" else "Every $interval months"
            RecurrenceFreq.YEARLY -> if (interval <= 1) "Yearly" else "Every $interval years"
        }
        val onClause = when {
            freq == RecurrenceFreq.WEEKLY && byDay.isNotEmpty() ->
                " on " + byDay.sortedBy { it.day.value }.joinToString(", ") { it.day.shortName() }
            freq == RecurrenceFreq.MONTHLY && byDay.isNotEmpty() -> {
                val bd = byDay.first()
                " on the " + ordinalWord(bd.ordinal) + " " + bd.day.shortName()
            }
            else -> ""
        }
        val endClause = when {
            count != null -> " · " + if (count == 1) "1 time" else "$count times"
            until != null -> " · until " + until.format(DISPLAY_FORMAT)
            else -> ""
        }
        return base + onClause + endClause
    }

    private fun ByDay.toToken(): String = (ordinal?.toString() ?: "") + day.rruleCode()

    // --- Occurrence-date generation (pure) ---------------------------------------------------------
    // The series is walked in "blocks". Block N is one period (INTERVAL applied N times) from the
    // anchor. Each block yields 0+ dates (>= firstDate), sorted ascending. Callers count/until-bound
    // and window-filter the flattened stream. Anchors are monotonic, enabling fast-forward.

    /** A monotonically increasing representative date for [block], used for range/until termination. */
    fun blockAnchorDate(firstDate: LocalDate, block: Int): LocalDate {
        val step = block.toLong() * interval
        return when (freq) {
            RecurrenceFreq.DAILY -> firstDate.plusDays(step)
            RecurrenceFreq.WEEKLY -> weekMonday(firstDate).plusWeeks(step)
            RecurrenceFreq.MONTHLY -> firstDate.withDayOfMonth(1).plusMonths(step)
            RecurrenceFreq.YEARLY -> LocalDate.of((firstDate.year + step).toInt(), 1, 1)
        }
    }

    /** Concrete occurrence dates produced by [block], sorted ascending, each >= [firstDate]. */
    fun datesForBlock(firstDate: LocalDate, block: Int): List<LocalDate> {
        val step = block.toLong() * interval
        return when (freq) {
            RecurrenceFreq.DAILY ->
                listOf(firstDate.plusDays(step)).filter { it >= firstDate }
            RecurrenceFreq.WEEKLY -> {
                val blockMonday = weekMonday(firstDate).plusWeeks(step)
                val days = if (byDay.isEmpty()) listOf(firstDate.dayOfWeek) else byDay.map { it.day }.distinct()
                days.sortedBy { it.value }
                    .map { blockMonday.plusDays((it.value - 1).toLong()) }
                    .filter { it >= firstDate }
            }
            RecurrenceFreq.MONTHLY -> {
                val monthAnchor = firstDate.withDayOfMonth(1).plusMonths(step)
                val date = if (byDay.isEmpty()) {
                    monthAnchor.dayOrNull(firstDate.dayOfMonth)
                } else {
                    nthWeekdayOfMonth(monthAnchor, byDay.first())
                }
                listOfNotNull(date).filter { it >= firstDate }
            }
            RecurrenceFreq.YEARLY -> {
                val year = (firstDate.year + step).toInt()
                listOfNotNull(monthDayOrNull(year, firstDate.monthValue, firstDate.dayOfMonth))
                    .filter { it >= firstDate }
            }
        }
    }

    /** First block whose anchor is at/just before [rangeStart]; safe to over-shoot backwards by one. */
    fun fastForwardBlock(firstDate: LocalDate, rangeStart: LocalDate): Int {
        if (rangeStart <= firstDate) return 0
        val periods = when (freq) {
            RecurrenceFreq.DAILY -> DAYS_BETWEEN(firstDate, rangeStart)
            RecurrenceFreq.WEEKLY -> DAYS_BETWEEN(weekMonday(firstDate), rangeStart) / 7
            RecurrenceFreq.MONTHLY -> MONTHS_BETWEEN(firstDate.withDayOfMonth(1), rangeStart.withDayOfMonth(1))
            RecurrenceFreq.YEARLY -> (rangeStart.year - firstDate.year).toLong()
        }
        return ((periods / interval) - 1).coerceAtLeast(0).toInt()
    }

    companion object {
        private val UNTIL_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd", Locale.US)
        private val DISPLAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)

        /** Tolerant parse. Returns null for null/blank or a missing/unknown FREQ. Never throws. */
        fun parse(raw: String?): RecurrenceRule? {
            val text = raw?.trim().orEmpty()
            if (text.isEmpty()) return null
            val parts = text.split(';')
                .mapNotNull { part ->
                    val idx = part.indexOf('=')
                    if (idx <= 0) null else part.substring(0, idx).trim().uppercase(Locale.US) to
                        part.substring(idx + 1).trim()
                }
                .toMap()

            val freq = when (parts["FREQ"]?.uppercase(Locale.US)) {
                "DAILY" -> RecurrenceFreq.DAILY
                "WEEKLY" -> RecurrenceFreq.WEEKLY
                "MONTHLY" -> RecurrenceFreq.MONTHLY
                "YEARLY" -> RecurrenceFreq.YEARLY
                else -> return null
            }

            val interval = parts["INTERVAL"]?.toIntOrNull()?.takeIf { it >= 1 } ?: 1
            val byDay = parts["BYDAY"]?.split(',')?.mapNotNull { parseByDay(it.trim()) }.orEmpty()
            val count = parts["COUNT"]?.toIntOrNull()?.takeIf { it >= 1 }
            val until = parts["UNTIL"]?.let { parseUntil(it) }

            return RecurrenceRule(
                freq = freq,
                interval = interval,
                byDay = byDay,
                // COUNT wins if both present (RFC allows only one; be forgiving).
                count = count,
                until = if (count == null) until else null,
            )
        }

        private fun parseByDay(token: String): ByDay? {
            if (token.isEmpty()) return null
            val code = token.takeLast(2).uppercase(Locale.US)
            val day = dayFromCode(code) ?: return null
            val prefix = token.dropLast(2)
            val ordinal = if (prefix.isBlank()) null else prefix.toIntOrNull() ?: return null
            return ByDay(ordinal, day)
        }

        private fun parseUntil(value: String): LocalDate? {
            val digits = value.takeWhile { it.isDigit() }
            if (digits.length < 8) return null
            return runCatching {
                LocalDate.of(
                    digits.substring(0, 4).toInt(),
                    digits.substring(4, 6).toInt(),
                    digits.substring(6, 8).toInt(),
                )
            }.getOrNull()
        }

        private fun dayFromCode(code: String): DayOfWeek? = when (code) {
            "MO" -> DayOfWeek.MONDAY
            "TU" -> DayOfWeek.TUESDAY
            "WE" -> DayOfWeek.WEDNESDAY
            "TH" -> DayOfWeek.THURSDAY
            "FR" -> DayOfWeek.FRIDAY
            "SA" -> DayOfWeek.SATURDAY
            "SU" -> DayOfWeek.SUNDAY
            else -> null
        }

        private fun ordinalWord(ordinal: Int?): String = when (ordinal) {
            null, 1 -> "1st"
            2 -> "2nd"
            3 -> "3rd"
            4 -> "4th"
            5 -> "5th"
            -1 -> "last"
            else -> "${ordinal}th"
        }
    }
}

internal fun DayOfWeek.rruleCode(): String = when (this) {
    DayOfWeek.MONDAY -> "MO"
    DayOfWeek.TUESDAY -> "TU"
    DayOfWeek.WEDNESDAY -> "WE"
    DayOfWeek.THURSDAY -> "TH"
    DayOfWeek.FRIDAY -> "FR"
    DayOfWeek.SATURDAY -> "SA"
    DayOfWeek.SUNDAY -> "SU"
}

private fun DayOfWeek.shortName(): String =
    getDisplayName(TextStyle.SHORT, Locale.US)

private fun weekMonday(date: LocalDate): LocalDate =
    date.minusDays((date.dayOfWeek.value - 1).toLong())

private fun LocalDate.dayOrNull(day: Int): LocalDate? =
    if (day in 1..lengthOfMonth()) withDayOfMonth(day) else null

private fun monthDayOrNull(year: Int, month: Int, day: Int): LocalDate? =
    runCatching { LocalDate.of(year, month, 1).dayOrNull(day) }.getOrNull()

/**
 * The nth (or last, ordinal -1) occurrence of a weekday within the month of [firstOfMonth].
 * Returns null when the nth occurrence does not exist (e.g. a 5th Monday in a short month).
 */
private fun nthWeekdayOfMonth(firstOfMonth: LocalDate, byDay: ByDay): LocalDate? {
    val target = byDay.day
    if (byDay.ordinal == -1) {
        val lastDay = firstOfMonth.withDayOfMonth(firstOfMonth.lengthOfMonth())
        val back = (lastDay.dayOfWeek.value - target.value + 7) % 7
        return lastDay.minusDays(back.toLong())
    }
    val n = (byDay.ordinal ?: 1).coerceAtLeast(1)
    val forward = (target.value - firstOfMonth.dayOfWeek.value + 7) % 7
    val candidate = firstOfMonth.plusDays(forward.toLong()).plusWeeks((n - 1).toLong())
    return if (candidate.monthValue == firstOfMonth.monthValue) candidate else null
}

@Suppress("FunctionName")
private fun DAYS_BETWEEN(a: LocalDate, b: LocalDate): Long =
    java.time.temporal.ChronoUnit.DAYS.between(a, b)

@Suppress("FunctionName")
private fun MONTHS_BETWEEN(a: LocalDate, b: LocalDate): Long =
    java.time.temporal.ChronoUnit.MONTHS.between(a, b)
