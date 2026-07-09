package com.dotfield.dotcal.ui

import com.dotfield.dotcal.data.recurrence.RecurrenceRule
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val detailDateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.US)
internal val dayHeaderFormatter = DateTimeFormatter.ofPattern("EEE dd MMM", Locale.US)
internal val compactDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
internal val editorDateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM, yyyy", Locale.US)
internal val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
internal val editorTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
internal const val WEEK_HOUR_HEIGHT_DP = 64f
internal const val DAY_HOUR_HEIGHT_DP = 72f
internal const val TIMELINE_BOTTOM_CLEARANCE_DP = 104f
internal val reminderOptions = listOf(null, 5, 10, 30, 60, 1440)
internal val taskReminderOptions = listOf(null, 5, 10, 30, 1440)

internal data class RecurrenceOption(val label: String, val rrule: String?)

internal val recurrenceOptions = listOf(
    RecurrenceOption("None", null),
    RecurrenceOption("Daily", "FREQ=DAILY"),
    RecurrenceOption("Weekly", "FREQ=WEEKLY"),
    RecurrenceOption("Monthly", "FREQ=MONTHLY"),
    RecurrenceOption("Yearly", "FREQ=YEARLY"),
)

internal enum class SettingsScreen {
    Root,
    Theme,
    CalendarAccounts,
    AddAccount,
    GlobalHolidays,
    AppPrivacy,
    PrivacyPolicy,
}

internal enum class WeekStartOption(val storageKey: String, val label: String, val fixedDay: DayOfWeek?) {
    RegionDefault("REGION_DEFAULT", "Region default", null),
    Saturday("SATURDAY", "Saturday", DayOfWeek.SATURDAY),
    Sunday("SUNDAY", "Sunday", DayOfWeek.SUNDAY),
    Monday("MONDAY", "Monday", DayOfWeek.MONDAY),
}

internal fun repeatRowLabel(rrule: String?): String {
    if (rrule.isNullOrBlank()) return "None"
    recurrenceOptions.firstOrNull { it.rrule == rrule }?.let { return it.label }
    return RecurrenceRule.parse(rrule)?.humanLabel() ?: "None"
}

internal enum class DateTimeField { Start, End }

internal const val MAX_VOICE_NOTE_SECONDS = 300

internal fun allDayReminderTimeLabel(time: LocalTime): String {
    return DateTimeFormatter.ofPattern("h:mm a", Locale.US).format(time).lowercase(Locale.US)
}
