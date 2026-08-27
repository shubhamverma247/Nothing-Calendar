package com.dotfield.dotcal.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Size
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.dotfield.dotcal.R
import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventReminder
import com.dotfield.dotcal.data.RecurringEditScope
import com.dotfield.dotcal.data.SyncMetadata
import com.dotfield.dotcal.data.recurrence.RecurrenceRule
import java.io.File
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.WeekFields
import java.util.Locale

internal fun showDotCalToast(
    context: Context,
    palette: DotCalPalette,
    message: String,
    duration: Int = Toast.LENGTH_SHORT,
) {
    val toast = Toast.makeText(context.applicationContext, message, duration)
    runCatching {
        val density = context.resources.displayMetrics.density
        val background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 18f * density
            setColor(if (palette.isDark) palette.dialogSurface.toArgb() else Color.White.toArgb())
            setStroke(
                (1f * density).toInt().coerceAtLeast(1),
                if (palette.isDark) palette.line.toArgb() else Color(0xFFE4E4E4).toArgb(),
            )
        }
        val horizontalPadding = (18f * density).toInt()
        val verticalPadding = (12f * density).toInt()
        toast.view = TextView(context.applicationContext).apply {
            text = message
            setTextColor(palette.primaryText.toArgb())
            textSize = 14f
            gravity = Gravity.CENTER
            maxLines = 2
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
            setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
            this.background = background
        }
    }
    toast.show()
}

/**
 * Resource-id overload. Most toasts fire from non-composable lambdas (coroutines, callbacks,
 * `onFailure` blocks) where [stringResource] is unavailable; resolving through [Context] keeps
 * those call sites a one-liner instead of forcing a hoisted `val` per message.
 */
internal fun showDotCalToast(
    context: Context,
    palette: DotCalPalette,
    @StringRes messageRes: Int,
    vararg formatArgs: Any,
    duration: Int = Toast.LENGTH_SHORT,
) {
    val message = if (formatArgs.isEmpty()) {
        context.getString(messageRes)
    } else {
        context.getString(messageRes, *formatArgs)
    }
    showDotCalToast(context, palette, message, duration)
}

/** Plural counterpart of the [showDotCalToast] resource overload. */
internal fun showDotCalToastPlural(
    context: Context,
    palette: DotCalPalette,
    @PluralsRes pluralRes: Int,
    count: Int,
    duration: Int = Toast.LENGTH_SHORT,
) {
    showDotCalToast(
        context,
        palette,
        context.resources.getQuantityString(pluralRes, count, count),
        duration,
    )
}

@Composable
internal fun secondaryActionContainer(palette: DotCalPalette) =
    if (palette.isDark) palette.bottomNavSurface else palette.calendarSurface

@Composable
internal fun secondaryActionBorder(palette: DotCalPalette) =
    BorderStroke(
        1.dp,
        if (palette.isDark) palette.line.copy(alpha = 0.78f) else palette.accent.copy(alpha = 0.72f),
    )

@Composable
internal fun secondaryActionContent(palette: DotCalPalette) =
    if (palette.isDark) palette.primaryText else palette.accent

@Composable
internal fun DotCalSwitch(
    checked: Boolean,
    palette: DotCalPalette,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = palette.onAccent,
            checkedTrackColor = palette.accent,
            uncheckedThumbColor = palette.secondaryText,
            uncheckedTrackColor = palette.cell,
            disabledCheckedThumbColor = palette.onAccent.copy(alpha = 0.8f),
            disabledCheckedTrackColor = palette.accent.copy(alpha = 0.45f),
            disabledUncheckedThumbColor = palette.secondaryText.copy(alpha = 0.55f),
            disabledUncheckedTrackColor = palette.cell.copy(alpha = 0.55f),
        ),
    )
}

internal fun parseWeekStartOption(value: String?): WeekStartOption {
    return WeekStartOption.entries.firstOrNull { it.storageKey == value || it.name == value } ?: WeekStartOption.RegionDefault
}

internal fun resolveWeekStartDay(option: WeekStartOption): DayOfWeek {
    return option.fixedDay ?: WeekFields.of(Locale.getDefault()).firstDayOfWeek
}

private const val MAX_VISIBLE_EVENT_DAYS = 370L

internal fun CalendarEvent.localDate(): LocalDate {
    return Instant.ofEpochMilli(startTimeMs).atZone(displayZone()).toLocalDate()
}

internal fun eventsByVisibleDate(events: List<CalendarEvent>): Map<LocalDate, List<CalendarEvent>> {
    val grouped = mutableMapOf<LocalDate, MutableList<CalendarEvent>>()
    events.forEach { event ->
        event.visibleDates().forEach { date ->
            grouped.getOrPut(date) { mutableListOf() }.add(event)
        }
    }
    return grouped
}

internal fun CalendarEvent.visibleDates(): List<LocalDate> {
    val zone = displayZone()
    val startDate = Instant.ofEpochMilli(startTimeMs).atZone(zone).toLocalDate()
    val inclusiveEndInstant = (endTimeMs - 1L).coerceAtLeast(startTimeMs)
    val endDate = Instant.ofEpochMilli(inclusiveEndInstant).atZone(zone).toLocalDate()
    val dayCount = ChronoUnit.DAYS.between(startDate, endDate).coerceIn(0L, MAX_VISIBLE_EVENT_DAYS - 1L)
    return List(dayCount.toInt() + 1) { startDate.plusDays(it.toLong()) }
}

internal fun CalendarEvent.titleForVisibleDate(date: LocalDate): String {
    val dates = visibleDates()
    if (dates.size <= 1) return title
    val dayIndex = dates.indexOf(date).takeIf { it >= 0 } ?: return title
    return "$title (Day ${dayIndex + 1}/${dates.size})"
}

internal fun CalendarEvent.visibleDayPositionLabel(date: LocalDate): String? {
    val dates = visibleDates()
    if (dates.size <= 1) return null
    val dayIndex = dates.indexOf(date).takeIf { it >= 0 } ?: return null
    return "${dayIndex + 1}/${dates.size}"
}

internal fun CalendarEvent.isMultiDayVisible(): Boolean = visibleDates().size > 1

internal fun CalendarEvent.shouldShowStripTitle(date: LocalDate, segmentDates: List<LocalDate>): Boolean {
    val visibleInSegment = visibleDates().filter { it in segmentDates }
    return visibleInSegment.firstOrNull() == date
}

private fun CalendarEvent.displayZone(): ZoneId {
    return runCatching { ZoneId.of(timeZone) }.getOrDefault(ZoneId.systemDefault())
}

internal fun CalendarEvent.hasTaskDate(): Boolean {
    return startTimeMs > 0L
}

internal fun CalendarEvent.taskDueDetailLabel(): String {
    val date = localDate().format(editorDateFormatter)
    return if (isAllDay == 1) date else "$date, ${startLocalTime().format(timeFormatter)}"
}

internal fun CalendarEvent.taskDueDateLine(): String {
    return Instant.ofEpochMilli(startTimeMs)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.getDefault()))
}

internal fun CalendarEvent.taskDueTimeLine(): String {
    return if (isAllDay == 1) "All-day" else startLocalTime().format(timeFormatter)
}

internal fun taskDateHeaderFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.getDefault())
}

internal fun CalendarEvent.startLocalTime(): LocalTime {
    return Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
}

internal fun CalendarEvent.endLocalDateForEditor(): LocalDate {
    val endInstant = if (isAllDay == 1) endTimeMs - 1 else endTimeMs
    val zone = if (isAllDay == 1) displayZone() else ZoneId.systemDefault()
    return Instant.ofEpochMilli(endInstant).atZone(zone).toLocalDate()
}

internal fun CalendarEvent.endLocalTime(): LocalTime {
    return Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
}

internal fun CalendarEvent.shouldShowGhostBorder(): Boolean {
    return isGhost && source != "GOOGLE"
}

internal fun LocalTime.toHour12(): Int {
    val h = hour % 12
    return if (h == 0) 12 else h
}

internal fun toHour24(hour12: Int, period: String): Int {
    return if (period.uppercase(Locale.US) == "PM") {
        if (hour12 == 12) 12 else hour12 + 12
    } else {
        if (hour12 == 12) 0 else hour12
    }
}

internal fun parseStoredTime(value: String?): LocalTime? {
    if (value.isNullOrBlank()) return null
    return runCatching { LocalTime.parse(value) }.getOrNull()
}

internal fun minuteOfDayToLocalTimeOrNull(minuteOfDay: Int?): LocalTime? {
    val minute = minuteOfDay?.takeIf { it in 0..(23 * 60 + 59) } ?: return null
    return LocalTime.of(minute / 60, minute % 60)
}

internal fun coerceEndAfterStart(start: LocalTime, end: LocalTime): LocalTime {
    if (end.isAfter(start)) return end
    return when {
        start < LocalTime.of(22, 45) -> start.plusHours(1)
        start < LocalTime.of(23, 45) -> LocalTime.of(23, 45)
        else -> LocalTime.of(23, 59)
    }
}

@Composable
internal fun reminderLabel(minutes: Int?): String {
    return when (minutes) {
        null -> stringResource(R.string.reminder_none)
        60 -> stringResource(R.string.reminder_1_hour_before)
        1440 -> stringResource(R.string.reminder_1_day_before)
        else -> stringResource(R.string.reminder_minutes_before, minutes)
    }
}

internal fun RecurringEditScope.label(): String {
    return when (this) {
        RecurringEditScope.ThisEvent -> "This event"
        RecurringEditScope.ThisAndFollowing -> "This and following"
        RecurringEditScope.WholeSeries -> "Whole series"
    }
}

internal fun dateTimeLabel(date: LocalDate, time: LocalTime): String {
    return "${date.format(editorDateFormatter)} ${time.format(editorTimeFormatter).lowercase(Locale.getDefault())}"
}

@Composable
internal fun syncIntervalLabel(minutes: Int): String {
    return when (minutes) {
        0 -> stringResource(R.string.sync_interval_manual)
        60 -> stringResource(R.string.duration_1_hour)
        120 -> stringResource(R.string.duration_2_hours)
        else -> stringResource(R.string.duration_minutes, minutes)
    }
}

@Composable
internal fun calendarAccountsLabel(accounts: List<CalendarAccount>, hasCalendarPermission: Boolean): String {
    if (!hasCalendarPermission) return stringResource(R.string.accounts_local_only)
    val providerCount = accounts.count { it.id != "local-primary" }
    if (providerCount == 0) return stringResource(R.string.accounts_connected)
    val selectedCount = accounts.count { it.id != "local-primary" && it.isVisible == 1 }
    return stringResource(R.string.accounts_selected_ratio, selectedCount, providerCount)
}

@Composable
internal fun selectedHolidayCountriesLabel(countries: List<HolidayCountryUiItem>): String {
    val count = countries.count { it.isSelected }
    return if (count == 0) {
        stringResource(R.string.holidays_none_selected)
    } else {
        pluralStringResource(R.plurals.holiday_countries_selected, count, count)
    }
}

@Composable
internal fun calendarMenuSummary(hiddenActions: Set<CalendarOverflowAction>): String {
    val visibleCount = CalendarOverflowAction.entries.size - hiddenActions.size
    return stringResource(
        R.string.settings_calendar_menu_visible_count,
        visibleCount.coerceAtLeast(0),
        CalendarOverflowAction.entries.size,
    )
}

@Composable
internal fun List<SyncMetadata>.lastSyncedSubtitle(): String {
    return stringResource(R.string.sync_last_synced, lastSyncedRelativeLabel())
}

@Composable
private fun List<SyncMetadata>.lastSyncedRelativeLabel(): String {
    val lastSyncMs = maxOfOrNull { it.lastSyncMs } ?: 0L
    if (lastSyncMs <= 0L) return stringResource(R.string.sync_never)
    val elapsedMinutes = ((System.currentTimeMillis() - lastSyncMs) / 60_000L).coerceAtLeast(0L)
    return when {
        elapsedMinutes < 1L -> stringResource(R.string.sync_just_now)
        elapsedMinutes < 60L -> pluralStringResource(R.plurals.sync_minutes_ago, elapsedMinutes.toInt(), elapsedMinutes.toInt())
        elapsedMinutes < 24L * 60L -> {
            val hours = (elapsedMinutes / 60L).toInt()
            pluralStringResource(R.plurals.sync_hours_ago, hours, hours)
        }
        elapsedMinutes < 48L * 60L -> stringResource(R.string.sync_yesterday)
        else -> {
            val days = (elapsedMinutes / (24L * 60L)).toInt()
            pluralStringResource(R.plurals.sync_days_ago, days, days)
        }
    }
}

internal fun String.readableCalendarLabel(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) return "Calendar"
    if (trimmed.contains("@")) return trimmed
    if (trimmed.any { it.isLowerCase() }) return trimmed
    // Account names come from CalendarProvider and are proper nouns, not translated copy. Stays on
    // Locale.US on purpose: Turkish casing would render "GMAIL" as "gmaıl" with a dotless i.
    return trimmed.lowercase(Locale.US).replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
    }
}

@Composable
internal fun CalendarAccount.secondaryCalendarLabel(): String {
    val raw = accountName.ifBlank { accountType }.trim()
    if (raw.isBlank()) return stringResource(R.string.settings_value_local)
    return raw.readableCalendarLabel()
}

internal fun nearestCircularIndex(currentIndex: Int, targetItemIndex: Int, itemCount: Int): Int {
    if (itemCount <= 0) return currentIndex
    val currentItemIndex = currentIndex % itemCount
    val forward = (targetItemIndex - currentItemIndex + itemCount) % itemCount
    val backward = forward - itemCount
    val delta = if (kotlin.math.abs(backward) < forward) backward else forward
    return currentIndex + delta
}

internal fun CalendarEvent.durationMinutes(): Int {
    return ((normalizedEndTimeMs() - startTimeMs) / 60_000L).toInt().coerceAtLeast(15).coerceAtMost(24 * 60)
}

internal fun CalendarEvent.normalizedEndTimeMs(): Long {
    return endTimeMs.coerceAtLeast(startTimeMs + 15 * 60 * 1000L)
}

internal fun CalendarEvent.detailDateLine(): String {
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault())
    return start.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.getDefault()))
}

internal fun CalendarEvent.detailTimeLine(): String {
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault())
    val end = Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault())
    return "${start.toLocalTime().format(timeFormatter)} - ${end.toLocalTime().format(timeFormatter)}"
}

/**
 * The event-detail "Repeats / …" row.
 *
 * This used to be `"REPEATS / " + rule.humanLabel().uppercase()` and was then pushed through a
 * `toSentenceCase()` at both call sites, so the caps never reached the screen — the visible text was
 * always "Repeats / daily". The resource is therefore written in the case it displays in, and both
 * the `uppercase()` and the sentence-case round-trip are gone. That also removes a latent Turkish
 * casing bug: `uppercase()` with no locale turns "i" into a dotless "ı".
 */
@Composable
internal fun CalendarEvent.recurrenceDetailLabel(): String? {
    val label = recurrenceHumanLabel(rrule) ?: return null
    return stringResource(R.string.event_detail_repeats, label)
}

/**
 * Reminder offsets for the detail rows. Same story as above: these were hardcoded ALL-CAPS and
 * sentence-cased at every call site, so the resources now carry the displayed casing directly.
 */
@Composable
internal fun EventReminder.detailLabel(): String = when (minutesBefore) {
    1 -> stringResource(R.string.reminder_detail_1_minute_before)
    60 -> stringResource(R.string.reminder_detail_1_hour_before)
    1440 -> stringResource(R.string.reminder_detail_1_day_before)
    else -> pluralStringResource(R.plurals.reminder_detail_minutes_before, minutesBefore, minutesBefore)
}

/**
 * Comma-joined reminder offsets for the event-detail row. A `@Composable` [detailLabel] cannot be
 * called from `joinToString`/`map` (their lambdas are not composable), so the labels are resolved in
 * a composable `for` loop and joined afterwards.
 */
@Composable
internal fun remindersDetailLine(reminders: List<EventReminder>): String {
    val labels = ArrayList<String>(reminders.size)
    for (reminder in reminders.sortedBy { it.minutesBefore }) {
        labels += reminder.detailLabel()
    }
    return labels.joinToString()
}

internal fun parseJsonStringArray(value: String): List<String> {
    val trimmed = value.trim()
    if (trimmed.length < 2 || trimmed.first() != '[' || trimmed.last() != ']') return emptyList()
    return trimmed
        .removePrefix("[")
        .removeSuffix("]")
        .split(',')
        .mapNotNull { raw ->
            raw.trim()
                .removeSurrounding("\"")
                .replace("\\\"", "\"")
                .takeIf { it.isNotBlank() }
        }
}

internal fun List<String>.toJsonStringArray(): String {
    return joinToString(prefix = "[", postfix = "]") { value ->
        "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }
}

internal fun loadImageThumbnail(context: Context, uriValue: String): Bitmap? {
    val uri = runCatching { Uri.parse(uriValue) }.getOrNull() ?: return null
    return runCatching {
        context.contentResolver.loadThumbnail(uri, Size(180, 180), null)
    }.getOrNull()
}

internal fun loadImagePreview(context: Context, uriValue: String): Bitmap? {
    val uri = runCatching { Uri.parse(uriValue) }.getOrNull() ?: return null
    return runCatching {
        context.contentResolver.loadThumbnail(uri, Size(1280, 1280), null)
    }.getOrNull()
}

internal fun voiceNoteFile(context: Context, eventId: String): File {
    val directory = File(context.filesDir, "voice_notes").apply { mkdirs() }
    return File(directory, "$eventId.m4a")
}

internal fun startVoiceRecording(context: Context, eventId: String): MediaRecorder? {
    val outputFile = voiceNoteFile(context, eventId)
    runCatching { if (outputFile.exists()) outputFile.delete() }
    return runCatching {
        mediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setMaxDuration(MAX_VOICE_NOTE_SECONDS * 1000)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
    }.getOrNull()
}

private fun mediaRecorder(context: Context): MediaRecorder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
    }
}

internal fun formatVoiceDuration(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    return "${safeSeconds / 60}:${(safeSeconds % 60).toString().padStart(2, '0')}"
}

internal fun parseColor(hex: String): Int {
    return try {
        android.graphics.Color.parseColor(hex)
    } catch (_: IllegalArgumentException) {
        android.graphics.Color.RED
    }
}

internal fun CalendarEvent.displayColor(palette: DotCalPalette): Color {
    return colorHex?.let { Color(parseColor(it)) } ?: palette.accent
}

internal fun android.content.Context.findActivity(): android.app.Activity? {
    var ctx: android.content.Context? = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is android.app.Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
