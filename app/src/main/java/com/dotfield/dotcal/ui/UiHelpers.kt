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
import androidx.compose.runtime.Composable
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

internal fun CalendarEvent.localDate(): LocalDate {
    return Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault()).toLocalDate()
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
        .format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.US))
}

internal fun CalendarEvent.taskDueTimeLine(): String {
    return if (isAllDay == 1) "All-day" else startLocalTime().format(timeFormatter)
}

internal fun taskDateHeaderFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.US)
}

internal fun CalendarEvent.startLocalTime(): LocalTime {
    return Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
}

internal fun CalendarEvent.endLocalDateForEditor(): LocalDate {
    val endInstant = if (isAllDay == 1) endTimeMs - 1 else endTimeMs
    return Instant.ofEpochMilli(endInstant).atZone(ZoneId.systemDefault()).toLocalDate()
}

internal fun CalendarEvent.endLocalTime(): LocalTime {
    return Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
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

internal fun coerceEndAfterStart(start: LocalTime, end: LocalTime): LocalTime {
    if (end.isAfter(start)) return end
    return when {
        start < LocalTime.of(22, 45) -> start.plusHours(1)
        start < LocalTime.of(23, 45) -> LocalTime.of(23, 45)
        else -> LocalTime.of(23, 59)
    }
}

internal fun reminderLabel(minutes: Int?): String {
    return when (minutes) {
        null -> "None"
        60 -> "1 hour before"
        1440 -> "1 day before"
        else -> "$minutes minutes before"
    }
}

internal fun RecurringEditScope.label(): String {
    return when (this) {
        RecurringEditScope.ThisEvent -> "This event"
        RecurringEditScope.WholeSeries -> "Whole series"
    }
}

internal fun dateTimeLabel(date: LocalDate, time: LocalTime): String {
    return "${date.format(editorDateFormatter)} ${time.format(editorTimeFormatter).lowercase(Locale.US)}"
}

internal fun syncIntervalLabel(minutes: Int): String {
    return when (minutes) {
        0 -> "Manual"
        60 -> "1 hour"
        120 -> "2 hours"
        else -> "$minutes min"
    }
}

internal fun calendarAccountsLabel(accounts: List<CalendarAccount>, hasCalendarPermission: Boolean): String {
    if (!hasCalendarPermission) return "Local only"
    val providerCount = accounts.count { it.id != "local-primary" }
    if (providerCount == 0) return "Connected"
    val selectedCount = accounts.count { it.id != "local-primary" && it.isVisible == 1 }
    return "$selectedCount/$providerCount selected"
}

internal fun selectedHolidayCountriesLabel(countries: List<HolidayCountryUiItem>): String {
    val count = countries.count { it.isSelected }
    return when (count) {
        0 -> "None selected"
        1 -> "1 country selected"
        else -> "$count countries selected"
    }
}

internal fun List<SyncMetadata>.lastSyncedSubtitle(): String {
    return "Last synced ${lastSyncedRelativeLabel()}"
}

private fun List<SyncMetadata>.lastSyncedRelativeLabel(): String {
    val lastSyncMs = maxOfOrNull { it.lastSyncMs } ?: 0L
    if (lastSyncMs <= 0L) return "never"
    val elapsedMinutes = ((System.currentTimeMillis() - lastSyncMs) / 60_000L).coerceAtLeast(0L)
    return when {
        elapsedMinutes < 1L -> "just now"
        elapsedMinutes < 60L -> "$elapsedMinutes min ago"
        elapsedMinutes < 24L * 60L -> "${elapsedMinutes / 60L} hr ago"
        elapsedMinutes < 48L * 60L -> "yesterday"
        else -> "${elapsedMinutes / (24L * 60L)} d ago"
    }
}

internal fun String.readableCalendarLabel(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) return "Calendar"
    if (trimmed.contains("@")) return trimmed
    if (trimmed.any { it.isLowerCase() }) return trimmed
    return trimmed.lowercase(Locale.US).replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
    }
}

internal fun CalendarAccount.secondaryCalendarLabel(): String {
    val raw = accountName.ifBlank { accountType }.trim()
    if (raw.isBlank()) return "Local"
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
    return start.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.US))
}

internal fun CalendarEvent.detailTimeLine(): String {
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault())
    val end = Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault())
    return "${start.toLocalTime().format(timeFormatter)} - ${end.toLocalTime().format(timeFormatter)}"
}

internal fun CalendarEvent.recurrenceDetailLabel(): String? {
    val rule = RecurrenceRule.parse(rrule) ?: return null
    return "REPEATS / " + rule.humanLabel().uppercase()
}

internal fun EventReminder.detailLabel(): String {
    return when (minutesBefore) {
        1 -> "1 MINUTE BEFORE"
        60 -> "1 HOUR BEFORE"
        1440 -> "1 DAY BEFORE"
        else -> "$minutesBefore MINUTES BEFORE"
    }
}

internal fun String.toSentenceCase(): String {
    val lower = lowercase(Locale.US)
    return lower.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString() }
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
