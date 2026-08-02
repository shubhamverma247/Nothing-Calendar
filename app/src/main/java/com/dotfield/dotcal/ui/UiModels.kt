package com.dotfield.dotcal.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.dotfield.dotcal.R
import com.dotfield.dotcal.data.recurrence.ByDay
import com.dotfield.dotcal.data.recurrence.RecurrenceFreq
import com.dotfield.dotcal.data.recurrence.RecurrenceRule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Formatters must not be cached in a plain top-level `val`: that binds whatever locale was active
 * at class-load and then renders stale English month/day names after the user switches language
 * without restarting the process. These accessors rebuild per locale and memoize per (pattern,
 * locale) pair so hot list rendering stays allocation-free.
 *
 * Only the locale is localized here, not the field order — the patterns stay fixed so the mono
 * layouts keep their exact column widths.
 */
private val formatterCache = ConcurrentHashMap<Pair<String, Locale>, DateTimeFormatter>()

internal fun localizedFormatter(pattern: String): DateTimeFormatter {
    val locale = Locale.getDefault()
    return formatterCache.getOrPut(pattern to locale) { DateTimeFormatter.ofPattern(pattern, locale) }
}

internal val detailDateFormatter: DateTimeFormatter get() = localizedFormatter("EEE, dd MMM yyyy")
internal val dayHeaderFormatter: DateTimeFormatter get() = localizedFormatter("EEE dd MMM")
internal val compactDateFormatter: DateTimeFormatter get() = localizedFormatter("MMM d")
internal val editorDateFormatter: DateTimeFormatter get() = localizedFormatter("EEE, d MMM, yyyy")
internal val timeFormatter: DateTimeFormatter get() = localizedFormatter("HH:mm")
internal val editorTimeFormatter: DateTimeFormatter get() = localizedFormatter("h:mm a")
internal const val WEEK_HOUR_HEIGHT_DP = 64f
internal const val DAY_HOUR_HEIGHT_DP = 72f
internal const val TIMELINE_BOTTOM_CLEARANCE_DP = 104f
internal val reminderOptions = listOf(null, 5, 10, 30, 60, 1440)
internal val taskReminderOptions = listOf(null, 5, 10, 30, 1440)
internal val defaultEventDurationOptions = listOf(15, 30, 60, 90, 120)

/**
 * [rrule] is the persisted RRULE code and must stay stable; [labelRes] is the display text. Same
 * pattern as the converted enums: the `label` property name is unchanged so call sites still read
 * `option.label`, but it now resolves through resources and is therefore `@Composable`.
 */
internal data class RecurrenceOption(@StringRes val labelRes: Int, val rrule: String?) {
    val label: String
        @Composable get() = stringResource(labelRes)
}

internal val recurrenceOptions = listOf(
    RecurrenceOption(R.string.event_repeat_none, null),
    RecurrenceOption(R.string.repeat_daily, "FREQ=DAILY"),
    RecurrenceOption(R.string.repeat_weekly, "FREQ=WEEKLY"),
    RecurrenceOption(R.string.repeat_monthly, "FREQ=MONTHLY"),
    RecurrenceOption(R.string.repeat_yearly, "FREQ=YEARLY"),
)

internal enum class SettingsScreen {
    Root,
    Theme,
    Sync,
    CalendarPreferences,
    ReminderDefaults,
    Widgets,
    DataRestore,
    CalendarAccounts,
    AddAccount,
    GlobalHolidays,
    AppPrivacy,
    PrivacyPolicy,
}

/**
 * [storageKey] is persisted to DataStore and must stay stable; only [label] is display text.
 */
internal enum class WeekStartOption(
    val storageKey: String,
    @StringRes val labelRes: Int,
    val fixedDay: DayOfWeek?,
) {
    RegionDefault("REGION_DEFAULT", R.string.week_start_region_default, null),
    Saturday("SATURDAY", R.string.week_start_saturday, DayOfWeek.SATURDAY),
    Sunday("SUNDAY", R.string.week_start_sunday, DayOfWeek.SUNDAY),
    Monday("MONDAY", R.string.week_start_monday, DayOfWeek.MONDAY);

    val label: String
        @Composable get() = stringResource(labelRes)
}

@Composable
internal fun repeatRowLabel(rrule: String?): String {
    val noneLabel = stringResource(R.string.event_repeat_none)
    if (rrule.isNullOrBlank()) return noneLabel
    recurrenceOptions.firstOrNull { it.rrule == rrule }?.let { return it.label }
    return recurrenceHumanLabel(rrule) ?: noneLabel
}

/**
 * "1st Monday" / "last Friday" — the ordinal-weekday phrase shared by the picker and the label.
 *
 * [style] exists because the two callers legitimately differ: the Repeat picker row spells the day
 * out ("The 1st Monday"), while the compact recurrence sentence abbreviates it ("on the 1st Mon"),
 * matching what the pre-extraction English produced.
 */
@Composable
internal fun nthWeekdayPhrase(byDay: ByDay, style: TextStyle = TextStyle.FULL): String {
    val ord = stringResource(
        when (byDay.ordinal) {
            -1 -> R.string.recurrence_ordinal_last
            2 -> R.string.recurrence_ordinal_2nd
            3 -> R.string.recurrence_ordinal_3rd
            4 -> R.string.recurrence_ordinal_4th
            5 -> R.string.recurrence_ordinal_5th
            else -> R.string.recurrence_ordinal_1st
        },
    )
    return "$ord ${byDay.day.getDisplayName(style, Locale.getDefault())}"
}

/**
 * The localized replacement for the deleted `RecurrenceRule.humanLabel()`. Builds the same sentence
 * — e.g. "Every 2 weeks on Mon, Fri · 10 times" — but every fragment comes from resources, so the
 * data layer stays Context-free and English-free.
 *
 * Returns null when [rrule] is null/blank or unparseable, letting each call site pick its own
 * fallback (the Repeat row wants "None", a template summary wants "Repeats").
 */
@Composable
internal fun recurrenceHumanLabel(rrule: String?): String? {
    val rule = RecurrenceRule.parse(rrule) ?: return null
    return recurrenceHumanLabel(rule)
}

@Composable
internal fun recurrenceHumanLabel(rule: RecurrenceRule): String {
    val base = if (rule.interval <= 1) {
        stringResource(
            when (rule.freq) {
                RecurrenceFreq.DAILY -> R.string.repeat_daily
                RecurrenceFreq.WEEKLY -> R.string.repeat_weekly
                RecurrenceFreq.MONTHLY -> R.string.repeat_monthly
                RecurrenceFreq.YEARLY -> R.string.repeat_yearly
            },
        )
    } else {
        // "Every %1$d %2$s" — the unit is a plural so Spanish gets "días" not "día".
        stringResource(
            R.string.recurrence_every_n_units,
            rule.interval,
            pluralStringResource(
                when (rule.freq) {
                    RecurrenceFreq.DAILY -> R.plurals.recurrence_unit_day
                    RecurrenceFreq.WEEKLY -> R.plurals.recurrence_unit_week
                    RecurrenceFreq.MONTHLY -> R.plurals.recurrence_unit_month
                    RecurrenceFreq.YEARLY -> R.plurals.recurrence_unit_year
                },
                rule.interval,
            ),
        )
    }
    val onClause = when {
        rule.freq == RecurrenceFreq.WEEKLY && rule.byDay.isNotEmpty() -> stringResource(
            R.string.recurrence_label_on_days,
            rule.byDay.sortedBy { it.day.value }
                .joinToString(", ") { it.day.getDisplayName(TextStyle.SHORT, Locale.getDefault()) },
        )
        rule.freq == RecurrenceFreq.MONTHLY && rule.byDay.isNotEmpty() -> stringResource(
            R.string.recurrence_label_on_the,
            nthWeekdayPhrase(rule.byDay.first(), TextStyle.SHORT),
        )
        else -> ""
    }
    val count = rule.count
    val until = rule.until
    val endClause = when {
        // `recurrence_times` is a bare unit ("time"/"times"), so the count is formatted by the
        // wrapper string, not by the plural itself.
        count != null -> stringResource(
            R.string.recurrence_label_end_count,
            count,
            pluralStringResource(R.plurals.recurrence_times, count),
        )
        until != null ->
            stringResource(R.string.recurrence_label_end_until, until.format(compactDateFormatter))
        else -> ""
    }
    return base + onClause + endClause
}

internal enum class DateTimeField { Start, End }

internal const val MAX_VOICE_NOTE_SECONDS = 300

internal fun allDayReminderTimeLabel(time: LocalTime): String {
    return localizedFormatter("h:mm a").format(time).lowercase(Locale.getDefault())
}
