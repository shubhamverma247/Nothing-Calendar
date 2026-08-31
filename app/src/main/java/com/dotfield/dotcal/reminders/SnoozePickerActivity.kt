package com.dotfield.dotcal.reminders

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import com.dotfield.dotcal.R
import com.dotfield.dotcal.ui.DotCalPalette
import com.dotfield.dotcal.ui.AccentColor
import com.dotfield.dotcal.ui.DotCalThemeMode
import com.dotfield.dotcal.ui.DateTimeChoiceSheet
import com.dotfield.dotcal.ui.dotCalPalette
import com.dotfield.dotcal.ui.theme.DotCalTheme
import com.dotfield.dotcal.ui.secondaryActionBorder
import com.dotfield.dotcal.ui.secondaryActionContainer
import com.dotfield.dotcal.ui.secondaryActionContent
import java.text.DateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar

internal const val SNOOZE_PICKER_OPTIONS_PANE_HEIGHT_DP = 480

class SnoozePickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val eventId = intent.getStringExtra(ReminderReceiver.EXTRA_EVENT_ID) ?: return finish()
        val title = intent.getStringExtra(ReminderReceiver.EXTRA_EVENT_TITLE).orEmpty()
        val alarmRequestCode = intent.getIntExtra(ReminderReceiver.EXTRA_ALARM_REQUEST_CODE, Int.MIN_VALUE)
        val isTask = intent.getBooleanExtra(ReminderReceiver.EXTRA_IS_TASK, false)
        val eventStartTimeMs = intent.getLongExtra(ReminderReceiver.EXTRA_EVENT_START_TIME_MS, 0L)
        if (alarmRequestCode == Int.MIN_VALUE) return finish()
        setContent {
            DotCalTheme {
                SnoozePickerScreen(
                    eventTitle = title,
                    eventStartTimeMs = eventStartTimeMs,
                    onDismiss = ::closePicker,
                    onSchedule = { triggerAtMs, minutes ->
                        NotificationManagerCompat.from(this).cancel(alarmRequestCode)
                        ReminderScheduler(this).scheduleSnooze(eventId, title, alarmRequestCode, triggerAtMs, minutes, isTask)
                        closePicker()
                    },
                )
            }
        }
    }

    private fun closePicker() {
        finishAndRemoveTask()
    }
}

@Composable
private fun SnoozePickerScreen(
    eventTitle: String,
    eventStartTimeMs: Long,
    onDismiss: () -> Unit,
    onSchedule: (Long, Int) -> Unit,
) {
    val context = LocalContext.current
    val palette = remember {
        val prefs = context.getSharedPreferences("dotcal_boot", android.content.Context.MODE_PRIVATE)
        val mode = DotCalThemeMode.fromStorage(prefs.getString("theme_mode", null))
        val accent = AccentColor.fromStorage(prefs.getString("accent_color", null))
        val systemDark = (context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        dotCalPalette(mode, accent, systemDark)
    }
    var tab by remember { mutableIntStateOf(0) }
    var selectedForMinutes by remember { mutableStateOf<Int?>(null) }
    var customDurationMinutes by remember { mutableStateOf<Int?>(null) }
    var customDurationTime by remember { mutableStateOf(Calendar.getInstance().apply { add(Calendar.MINUTE, 15) }) }
    var untilMode by remember { mutableIntStateOf(0) }
    var customTime by remember { mutableStateOf(Calendar.getInstance().apply { add(Calendar.MINUTE, 15) }) }
    var customChosen by remember { mutableStateOf(false) }
    var showCustomDuration by remember { mutableStateOf(false) }
    var showCustomDateTime by remember { mutableStateOf(false) }
    var invalidTime by remember { mutableStateOf(false) }
    val eventStartAvailable = ReminderNotificationActions.canScheduleAt(eventStartTimeMs, System.currentTimeMillis())
    val canSave = if (tab == 0) selectedForMinutes != null || customDurationMinutes != null else untilMode == 0 && eventStartAvailable || untilMode == 1 && customChosen

    fun save() {
        val triggerAtMs = if (tab == 0) {
            selectedForMinutes?.let { System.currentTimeMillis() + ReminderNotificationActions.snoozeDelayMs(it) }
                ?: customDurationTime.timeInMillis.takeIf { customDurationMinutes != null }
                ?: return
        } else if (untilMode == 0) {
            eventStartTimeMs
        } else {
            customTime.timeInMillis
        }
        if (!ReminderNotificationActions.canScheduleAt(triggerAtMs, System.currentTimeMillis())) {
            invalidTime = true
            return
        }
        onSchedule(triggerAtMs, ((triggerAtMs - System.currentTimeMillis()) / 60_000L).toInt().coerceAtLeast(1))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 4.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 430.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(palette.background)
                .border(1.dp, palette.line, RoundedCornerShape(28.dp)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(palette.topBarSurface).padding(start = 20.dp, end = 10.dp, top = 12.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.snooze_picker_title), color = palette.primaryText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss, modifier = Modifier.clip(CircleShape)) {
                    Icon(Icons.Default.Close, null, tint = palette.primaryText)
                }
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
                Text(eventTitle, color = palette.primaryText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 2)
                Spacer(Modifier.height(14.dp))
                TabRow(selectedTabIndex = tab, containerColor = palette.background, contentColor = palette.accent) {
                    Tab(selected = tab == 0, onClick = { tab = 0; invalidTime = false }, text = { Text(stringResource(R.string.snooze_picker_for), color = if (tab == 0) palette.primaryText else palette.secondaryText) })
                    Tab(selected = tab == 1, onClick = { tab = 1; invalidTime = false }, text = { Text(stringResource(R.string.snooze_picker_until), color = if (tab == 1) palette.primaryText else palette.secondaryText) })
                }
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(SNOOZE_PICKER_OPTIONS_PANE_HEIGHT_DP.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    if (tab == 0) {
                        ReminderNotificationActions.SnoozePickerMinutes.forEach { minutes ->
                            val label = if (minutes < 60) context.getString(R.string.snooze_picker_minutes, minutes) else context.getString(R.string.snooze_picker_one_hour)
                            SnoozeRadioRow(label, context.getString(R.string.snooze_picker_after_this_time), Icons.Default.AccessTime, selectedForMinutes == minutes, palette) { selectedForMinutes = minutes; customDurationMinutes = null }
                        }
                        SnoozeRadioRow(
                            title = if (customDurationMinutes == null) stringResource(R.string.snooze_picker_custom) else stringResource(R.string.snooze_picker_custom_minutes, customDurationMinutes!!),
                            subtitle = stringResource(R.string.snooze_picker_custom_duration_subtitle),
                            icon = Icons.Default.AccessTime,
                            selected = customDurationMinutes != null,
                            palette = palette,
                            onClick = {
                                showCustomDuration = true
                            },
                        )
                        if (invalidTime) {
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.snooze_picker_invalid_time), color = palette.accent, fontSize = 13.sp)
                        }
                    } else {
                        SnoozeRadioRow(
                            stringResource(R.string.snooze_picker_event_start),
                            if (eventStartAvailable) stringResource(R.string.snooze_picker_event_start_subtitle) else stringResource(R.string.snooze_picker_invalid_time),
                            Icons.Default.CalendarMonth,
                            untilMode == 0,
                            palette,
                            enabled = eventStartAvailable,
                        ) { untilMode = 0; invalidTime = false }
                        SnoozeRadioRow(
                            stringResource(R.string.snooze_picker_custom),
                            if (customChosen) DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(customTime.time) else stringResource(R.string.snooze_picker_custom_subtitle),
                            Icons.Default.AccessTime,
                            untilMode == 1,
                            palette,
                        ) {
                            untilMode = 1
                            showCustomDateTime = true
                        }
                        if (invalidTime) {
                            Spacer(Modifier.height(8.dp))
                            Text(stringResource(R.string.snooze_picker_invalid_time), color = palette.accent, fontSize = 13.sp)
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().background(palette.topBarSurface).padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(54.dp),
                    border = secondaryActionBorder(palette),
                    colors = ButtonDefaults.buttonColors(containerColor = secondaryActionContainer(palette), contentColor = secondaryActionContent(palette)),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(0.dp),
                ) { Text(stringResource(R.string.action_cancel), fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                Button(
                    onClick = ::save,
                    enabled = canSave,
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent, disabledContainerColor = palette.accent, disabledContentColor = palette.onAccent.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(0.dp),
                ) { Text(stringResource(R.string.action_save), fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
    if (showCustomDateTime) {
        val zone = ZoneId.systemDefault()
        val value = Instant.ofEpochMilli(customTime.timeInMillis).atZone(zone).toLocalDateTime()
        DateTimeChoiceSheet(
            title = stringResource(R.string.snooze_picker_custom),
            selectedDate = value.toLocalDate(),
            selectedTime = value.toLocalTime(),
            minDate = LocalDate.now(zone),
            includeTime = true,
            palette = palette,
            onDismiss = { showCustomDateTime = false },
            dialogPresentation = true,
            onSelected = { date, time ->
                customTime = Calendar.getInstance().apply {
                    set(date.year, date.monthValue - 1, date.dayOfMonth, time.hour, time.minute, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                customChosen = true
                untilMode = 1
                invalidTime = false
                showCustomDateTime = false
            },
        )
    }
    if (showCustomDuration) {
        val zone = ZoneId.systemDefault()
        val value = Instant.ofEpochMilli(customDurationTime.timeInMillis).atZone(zone).toLocalDateTime()
        DateTimeChoiceSheet(
            title = stringResource(R.string.snooze_picker_custom_duration),
            selectedDate = value.toLocalDate(),
            selectedTime = value.toLocalTime(),
            minDate = LocalDate.now(zone),
            includeTime = true,
            palette = palette,
            onDismiss = { showCustomDuration = false },
            dialogPresentation = true,
            onSelected = { date, time ->
                customDurationTime = Calendar.getInstance().apply {
                    set(date.year, date.monthValue - 1, date.dayOfMonth, time.hour, time.minute, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val minutes = ((customDurationTime.timeInMillis - System.currentTimeMillis()) / 60_000L).toInt().coerceAtLeast(1)
                customDurationMinutes = minutes
                selectedForMinutes = null
                invalidTime = false
                showCustomDuration = false
            },
        )
    }
}

@Composable
private fun SnoozeRadioRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    palette: DotCalPalette,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(44.dp).height(48.dp), contentAlignment = Alignment.CenterStart) {
            Icon(icon, contentDescription = null, tint = if (enabled) palette.secondaryText else palette.disabledText, modifier = Modifier.size(21.dp))
        }
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(
                title,
                color = if (enabled) palette.primaryText else palette.disabledText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
                color = if (enabled) palette.secondaryText else palette.disabledText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(modifier = Modifier.width(48.dp).height(48.dp), contentAlignment = Alignment.Center) {
            RadioButton(
                selected = selected,
                onClick = if (enabled) onClick else null,
                modifier = Modifier.size(48.dp),
                enabled = enabled,
                colors = RadioButtonDefaults.colors(selectedColor = palette.accent, unselectedColor = palette.secondaryText, disabledSelectedColor = palette.disabledText, disabledUnselectedColor = palette.disabledText),
            )
        }
    }
}
