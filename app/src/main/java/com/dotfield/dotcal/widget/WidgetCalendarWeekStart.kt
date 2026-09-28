package com.dotfield.dotcal.widget

import com.dotfield.dotcal.prefs.CalendarPreferences
import androidx.datastore.preferences.core.Preferences
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

internal fun widgetWeekStartFromPreferences(preferences: Preferences, locale: Locale = Locale.getDefault()): DayOfWeek {
    return widgetWeekStartFromStorage(preferences[CalendarPreferences.KEY_WEEK_START], locale)
}

internal fun widgetWeekStartFromStorage(value: String?, locale: Locale = Locale.getDefault()): DayOfWeek {
    return when (value) {
        "SATURDAY" -> DayOfWeek.SATURDAY
        "SUNDAY" -> DayOfWeek.SUNDAY
        "MONDAY" -> DayOfWeek.MONDAY
        else -> WeekFields.of(locale).firstDayOfWeek
    }
}

internal fun widgetWeekdayLabels(weekStart: DayOfWeek, locale: Locale = Locale.getDefault()): List<String> {
    return List(7) { index ->
        weekStart.plus(index.toLong())
            .getDisplayName(TextStyle.NARROW_STANDALONE, locale)
            .uppercase(locale)
    }
}

internal fun widgetMonthDays(
    displayMonthDate: LocalDate,
    today: LocalDate,
    weekStart: DayOfWeek,
    eventDays: Set<Int> = emptySet(),
): List<WidgetCalendarDay> {
    val month = YearMonth.from(displayMonthDate)
    val monthStart = month.atDay(1)
    val leadingBlanks = (monthStart.dayOfWeek.value - weekStart.value + 7) % 7
    val days = MutableList(leadingBlanks) { WidgetCalendarDay(dayOfMonth = null) }
    days += (1..month.lengthOfMonth()).map { day ->
        val date = month.atDay(day)
        WidgetCalendarDay(dayOfMonth = day, dateIso = date.toString(), isToday = date == today, hasEvents = day in eventDays)
    }
    while (days.size % 7 != 0) days += WidgetCalendarDay(dayOfMonth = null)
    return days
}
