package com.dotfield.dotcal.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotfield.dotcal.R
import com.dotfield.dotcal.data.scheduling.FreeSlotRequest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

private enum class AvailabilityPreset(@StringRes val labelRes: Int) {
    NextThreeDays(R.string.availability_preset_next_three_days),
    ThisWeek(R.string.availability_preset_this_week),
    NextWeek(R.string.availability_preset_next_week),
    ;

    val label: String
        @Composable get() = stringResource(labelRes)
}

internal data class AvailabilityScrollbarThumb(
    val height: Dp,
    val offset: Dp,
)

internal fun availabilityScrollbarThumb(
    trackHeight: Dp,
    scrollValue: Int,
    maxScroll: Int,
): AvailabilityScrollbarThumb? {
    if (!trackHeight.value.isFinite() || trackHeight <= 0.dp || maxScroll <= 0) return null
    val height = minOf(trackHeight, 32.dp)
    val travel = (trackHeight - height).coerceAtLeast(0.dp)
    val progress = (scrollValue.toFloat() / maxScroll).coerceIn(0f, 1f)
    return AvailabilityScrollbarThumb(
        height = height,
        offset = travel * progress,
    )
}

@Composable
internal fun AvailabilityScreen(
    palette: DotCalPalette,
    initialDate: LocalDate,
    initialEndDate: LocalDate = initialDate.plusDays(2),
    weekStart: DayOfWeek,
    use24HourFormat: Boolean,
    state: AvailabilityUiState,
    onBack: () -> Unit,
    onRefresh: (FreeSlotRequest) -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
) {
    var rangeStart by remember(initialDate) { mutableStateOf(initialDate) }
    var rangeEnd by remember(initialDate, initialEndDate) { mutableStateOf(initialEndDate) }
    var selectedPreset by remember(initialDate, initialEndDate) {
        mutableStateOf<AvailabilityPreset?>(
            AvailabilityPreset.NextThreeDays.takeIf { initialEndDate == initialDate.plusDays(2) },
        )
    }
    var workingHours by remember { mutableStateOf(9f..21f) }
    var minimumMinutes by remember { mutableStateOf(30) }
    var blockAllDayEvents by remember { mutableStateOf(true) }
    var treatGhostsAsBusy by remember { mutableStateOf(true) }
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }
    val formScrollState = rememberScrollState()
    val previewScrollState = rememberScrollState()

    val request = remember(
        rangeStart,
        rangeEnd,
        workingHours,
        minimumMinutes,
        blockAllDayEvents,
        treatGhostsAsBusy,
    ) {
        FreeSlotRequest(
            rangeStart = minOf(rangeStart, rangeEnd),
            rangeEnd = maxOf(rangeStart, rangeEnd),
            workingStart = LocalTime.of(workingHours.start.roundToInt().coerceIn(0, 23), 0),
            workingEnd = LocalTime.of(workingHours.endInclusive.roundToInt().coerceIn(1, 24) % 24, 0)
                .let { if (workingHours.endInclusive.roundToInt() == 24) LocalTime.MAX else it },
            minimumSlotMinutes = minimumMinutes,
            blockAllDayEvents = blockAllDayEvents,
            treatGhostsAsBusy = treatGhostsAsBusy,
        )
    }
    LaunchedEffect(request, use24HourFormat) {
        onRefresh(request)
    }

    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text(
                stringResource(R.string.availability_title),
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center),
            )
            HorizontalDivider(
                color = palette.line.copy(alpha = 0.55f),
                thickness = 1.dp,
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(formScrollState)
                .padding(start = 18.dp, top = 12.dp, end = 18.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 156.dp, max = 244.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(palette.eventCardSurface)
                        .border(1.dp, palette.eventCardBorder, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.availability_preview),
                            color = palette.secondaryText,
                            fontFamily = mono,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            stringResource(
                                if (state.isLoading) {
                                    R.string.availability_status_updating
                                } else {
                                    R.string.availability_status_ready
                                },
                            ),
                            color = palette.accent,
                            fontFamily = mono,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = if (state.isLoading && state.text.isBlank()) Alignment.Center else Alignment.TopStart,
                    ) {
                        when {
                            state.isLoading && state.text.isBlank() -> CircularProgressIndicator(
                                color = palette.accent,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp),
                            )
                            state.error != null -> Text(
                                state.error,
                                color = palette.accent,
                                fontFamily = mono,
                                fontSize = 13.sp,
                            )
                            else -> Text(
                                state.text,
                                color = palette.primaryText,
                                fontFamily = mono,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(previewScrollState)
                                    .padding(end = 10.dp),
                            )
                        }
                        if (previewScrollState.maxValue > 0) {
                            BoxWithConstraints(modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()) {
                                availabilityScrollbarThumb(
                                    trackHeight = maxHeight,
                                    scrollValue = previewScrollState.value,
                                    maxScroll = previewScrollState.maxValue,
                                )?.let { thumb ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(3.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(palette.line.copy(alpha = 0.35f)),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(thumb.height)
                                                .offset(y = thumb.offset)
                                                .clip(RoundedCornerShape(2.dp))
                                                .background(palette.accent),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                AvailabilityPreset.entries.forEach { preset ->
                    AvailabilityChoiceChip(
                        label = preset.label,
                        selected = selectedPreset == preset,
                        palette = palette,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            selectedPreset = preset
                            val weekAnchor = initialDate.with(TemporalAdjusters.previousOrSame(weekStart))
                            when (preset) {
                                AvailabilityPreset.NextThreeDays -> {
                                    rangeStart = initialDate
                                    rangeEnd = initialDate.plusDays(2)
                                }
                                AvailabilityPreset.ThisWeek -> {
                                    rangeStart = weekAnchor
                                    rangeEnd = weekAnchor.plusDays(6)
                                }
                                AvailabilityPreset.NextWeek -> {
                                    rangeStart = weekAnchor.plusWeeks(1)
                                    rangeEnd = weekAnchor.plusWeeks(1).plusDays(6)
                                }
                            }
                        },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                AvailabilityDateRow(stringResource(R.string.availability_from), rangeStart, palette, Modifier.weight(1f)) {
                    selectedPreset = null
                    pickingStart = true
                }
                AvailabilityDateRow(stringResource(R.string.availability_to), rangeEnd, palette, Modifier.weight(1f)) {
                    selectedPreset = null
                    pickingEnd = true
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.availability_working_hours),
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    stringResource(
                        R.string.time_range,
                        formatAvailabilityTime(request.workingStart, use24HourFormat),
                        formatAvailabilityTime(request.workingEnd, use24HourFormat),
                    ),
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AvailabilityHourStepper(
                    label = stringResource(R.string.availability_start),
                    value = request.workingStart,
                    use24HourFormat = use24HourFormat,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    onMinus = {
                        val start = (workingHours.start.roundToInt() - 1).coerceIn(0, workingHours.endInclusive.roundToInt() - 1)
                        workingHours = start.toFloat()..workingHours.endInclusive
                    },
                    onPlus = {
                        val start = (workingHours.start.roundToInt() + 1).coerceIn(0, workingHours.endInclusive.roundToInt() - 1)
                        workingHours = start.toFloat()..workingHours.endInclusive
                    },
                )
                AvailabilityHourStepper(
                    label = stringResource(R.string.availability_end),
                    value = request.workingEnd,
                    use24HourFormat = use24HourFormat,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    onMinus = {
                        val end = (workingHours.endInclusive.roundToInt() - 1).coerceIn(workingHours.start.roundToInt() + 1, 24)
                        workingHours = workingHours.start..end.toFloat()
                    },
                    onPlus = {
                        val end = (workingHours.endInclusive.roundToInt() + 1).coerceIn(workingHours.start.roundToInt() + 1, 24)
                        workingHours = workingHours.start..end.toFloat()
                    },
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(15, 30, 45, 60).forEach { minutes ->
                    AvailabilityChoiceChip(
                        label = stringResource(R.string.availability_minutes_chip, minutes),
                        selected = minimumMinutes == minutes,
                        palette = palette,
                        modifier = Modifier.weight(1f),
                        onClick = { minimumMinutes = minutes },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AvailabilityCompactToggle(
                    title = stringResource(R.string.availability_all_day),
                    subtitle = stringResource(
                        if (blockAllDayEvents) {
                            R.string.availability_all_day_block
                        } else {
                            R.string.availability_all_day_ignore
                        },
                    ),
                    checked = blockAllDayEvents,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    onCheckedChange = { blockAllDayEvents = it },
                )
                AvailabilityCompactToggle(
                    title = stringResource(R.string.availability_ghosts),
                    subtitle = stringResource(
                        if (treatGhostsAsBusy) {
                            R.string.availability_ghosts_busy
                        } else {
                            R.string.availability_ghosts_free
                        },
                    ),
                    checked = treatGhostsAsBusy,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    onCheckedChange = { treatGhostsAsBusy = it },
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.bottomNavSurface)
                .navigationBarsPadding()
                .padding(start = 20.dp, top = 14.dp, end = 20.dp, bottom = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = { onCopy(state.text) },
                enabled = state.text.isNotBlank(),
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(8.dp),
                border = secondaryActionBorder(palette),
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondaryActionContainer(palette),
                    contentColor = secondaryActionContent(palette),
                    disabledContainerColor = secondaryActionContainer(palette).copy(alpha = 0.55f),
                    disabledContentColor = palette.disabledText,
                ),
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.availability_copy), fontWeight = FontWeight.SemiBold)
            }
            Button(
                onClick = { onShare(state.text) },
                enabled = state.text.isNotBlank(),
                modifier = Modifier.weight(1f).height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.accent,
                    contentColor = palette.onAccent,
                    disabledContainerColor = palette.accent.copy(alpha = 0.45f),
                ),
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.availability_share_button), fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (pickingStart) {
        DateTimeChoiceSheet(
            title = stringResource(R.string.availability_start_date),
            selectedDate = rangeStart,
            selectedTime = LocalTime.NOON,
            minDate = null,
            includeTime = false,
            palette = palette,
            onDismiss = { pickingStart = false },
            onSelected = { date, _ ->
                rangeStart = date
                if (rangeEnd.isBefore(date)) rangeEnd = date
                pickingStart = false
            },
        )
    }
    if (pickingEnd) {
        DateTimeChoiceSheet(
            title = stringResource(R.string.availability_end_date),
            selectedDate = rangeEnd,
            selectedTime = LocalTime.NOON,
            minDate = rangeStart,
            includeTime = false,
            palette = palette,
            onDismiss = { pickingEnd = false },
            onSelected = { date, _ ->
                rangeEnd = date
                pickingEnd = false
            },
        )
    }
}

@Composable
private fun AvailabilityChoiceChip(
    label: String,
    selected: Boolean,
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) palette.accent else palette.cell)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (selected) palette.onAccent else palette.secondaryText,
            fontFamily = mono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AvailabilityDateRow(
    label: String,
    date: LocalDate,
    palette: DotCalPalette,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(76.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.cell)
            .border(1.dp, palette.line.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(label, color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp, lineHeight = 14.sp)
        Spacer(Modifier.height(5.dp))
        Text(
            date.format(localizedFormatter("d MMM")),
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AvailabilityHourStepper(
    label: String,
    value: LocalTime,
    use24HourFormat: Boolean,
    palette: DotCalPalette,
    modifier: Modifier,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.calendarSurface)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp)
            Text(
                formatAvailabilityTime(value, use24HourFormat),
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            )
        }
        AvailabilityStepButton("-", palette, onMinus)
        Spacer(Modifier.width(4.dp))
        AvailabilityStepButton("+", palette, onPlus)
    }
}

@Composable
private fun AvailabilityStepButton(
    label: String,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.cell)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = palette.primaryText,
            fontFamily = mono,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun AvailabilityToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    palette: DotCalPalette,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(70.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = palette.primaryText, fontFamily = mono, fontSize = 15.sp)
            Text(subtitle, color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
        }
        DotCalSwitch(
            checked = checked,
            palette = palette,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun AvailabilityCompactToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    palette: DotCalPalette,
    modifier: Modifier,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .height(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(palette.calendarSurface)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = palette.primaryText, fontFamily = mono, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp)
        }
        DotCalSwitch(
            checked = checked,
            palette = palette,
            onCheckedChange = onCheckedChange,
        )
    }
}

private fun formatAvailabilityTime(time: LocalTime, use24HourFormat: Boolean): String {
    if (time == LocalTime.MAX) return "24:00"
    val pattern = if (use24HourFormat) "HH:mm" else "h:mm a"
    return time.format(localizedFormatter(pattern))
}
