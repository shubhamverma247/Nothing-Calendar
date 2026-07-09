package com.dotfield.dotcal.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.ui.theme.NWhite
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun MonthView(
    month: LocalDate,
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    palette: DotCalPalette,
    weekStart: DayOfWeek,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpToday: () -> Unit,
    selectedBulkDates: Set<LocalDate>,
    onBulkSelectionStart: (LocalDate) -> Unit,
    onBulkApply: () -> Unit,
    onBulkClear: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val days = remember(month, weekStart) { monthGrid(month, weekStart) }
    val weekDayLabels = remember(weekStart) { weekDayLabels(weekStart) }
    var dragTotal by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(month) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragTotal < -50.dp.toPx() -> onNext()
                            dragTotal > 50.dp.toPx() -> onPrevious()
                        }
                        dragTotal = 0f
                    },
                    onHorizontalDrag = { _, amount -> dragTotal += amount },
                )
            },
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(32.dp).background(palette.calendarSurface)) {
            weekDayLabels.forEach {
                Text(
                    it,
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                    fontFamily = mono,
                    fontSize = 11.sp,
                    color = palette.secondaryText,
                    textAlign = TextAlign.Center,
                )
            }
        }

        LazyVerticalGrid(columns = GridCells.Fixed(7), userScrollEnabled = false, modifier = Modifier.fillMaxWidth()) {
            items(days) { day ->
                DayCell(
                    date = day,
                    activeMonth = YearMonth.from(month),
                    isSelected = day == selectedDate,
                    isBulkSelected = day in selectedBulkDates,
                    events = eventsByDate[day].orEmpty(),
                    palette = palette,
                    onClick = { onDateSelected(day) },
                    onLongPress = { onBulkSelectionStart(day) },
                )
            }
        }
        if (selectedBulkDates.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().background(palette.calendarSurface).padding(horizontal = 18.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${selectedBulkDates.size} selected", color = palette.primaryText, fontFamily = mono, fontSize = 13.sp, modifier = Modifier.weight(1f))
                TextButton(onClick = onBulkClear) { Text("Clear", color = palette.secondaryText, fontFamily = mono) }
                Button(
                    onClick = onBulkApply,
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("Apply Template", fontFamily = mono, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    activeMonth: YearMonth,
    isSelected: Boolean,
    isBulkSelected: Boolean,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
) {
    val isToday = date == LocalDate.now()
    val inMonth = YearMonth.from(date) == activeMonth
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(palette.calendarSurface)
            .pointerInput(date) {
                detectTapGestures(
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    },
                )
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
            if (inMonth) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .then(
                            when {
                                isToday -> Modifier.clip(CircleShape).background(palette.accent)
                                isBulkSelected -> Modifier.border(2.dp, palette.accent, CircleShape).background(palette.accent.copy(alpha = 0.12f), CircleShape)
                                isSelected -> Modifier.border(1.5.dp, palette.accent, CircleShape)
                                else -> Modifier
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        date.dayOfMonth.toString().padStart(2, '0'),
                        color = if (isToday) palette.onAccent else palette.primaryText,
                        fontFamily = mono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    events.take(3).forEach { event ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(event.displayColor(palette)),
                        )
                    }
                }
            } else {
                Spacer(
                        modifier = Modifier
                            .size(28.dp),
                )
            }
        }
    }
}

@Composable
internal fun WeekView(
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    palette: DotCalPalette,
    weekStart: DayOfWeek,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onJumpToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val days = remember(selectedDate, weekStart) { weekDays(selectedDate, weekStart) }
    val weekEvents = remember(eventsByDate, days) { days.flatMap { eventsByDate[it].orEmpty() } }
    val timedEvents = remember(weekEvents) { weekEvents.filter { it.isAllDay == 0 } }
    val allDayEvents = remember(weekEvents) { weekEvents.filter { it.isAllDay == 1 } }
    val eventLayouts = remember(timedEvents) { layoutTimedEvents(timedEvents) }
    val timedEventsByDay = remember(timedEvents) { timedEvents.groupBy { it.localDate() } }

    var dragTotal by remember { mutableFloatStateOf(0f) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(selectedDate) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragTotal < -50.dp.toPx() -> onNextWeek()
                            dragTotal > 50.dp.toPx() -> onPreviousWeek()
                        }
                        dragTotal = 0f
                    },
                    onHorizontalDrag = { _, amount -> dragTotal += amount },
                )
            },
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(64.dp).background(palette.calendarSurface)) {
            Spacer(modifier = Modifier.width(32.dp))
            days.forEach { day ->
                WeekDayHeader(
                    date = day,
                    selected = day == selectedDate,
                    palette = palette,
                    onClick = { onDateSelected(day) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (allDayEvents.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth().height(32.dp).background(palette.calendarSurface)) {
                Spacer(modifier = Modifier.width(32.dp))
                days.forEach { day ->
                    val event = allDayEvents.firstOrNull { it.localDate() == day }
                    Box(
                        modifier = Modifier.weight(1f).height(32.dp).padding(2.dp).background(if (event == null) Color.Transparent else event.displayColor(palette).copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (event != null) {
                            Text(event.title, color = NWhite, fontFamily = mono, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.calendarSurface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    WeekTimeColumn(selectedDate = selectedDate, days = days, palette = palette)
                    days.forEach { day ->
                        WeekDayColumn(
                            day = day,
                            selectedDate = selectedDate,
                            events = timedEventsByDay[day].orEmpty(),
                            eventLayouts = eventLayouts,
                            palette = palette,
                            onAddAtDate = onAddAtDate,
                            onEventClick = onEventClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                Spacer(modifier = Modifier.height(TIMELINE_BOTTOM_CLEARANCE_DP.dp))
            }
            TimelineBottomBoundary(palette = palette, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun WeekDayHeader(
    date: LocalDate,
    selected: Boolean,
    palette: DotCalPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = date == LocalDate.now()
    Column(
        modifier = modifier.noRippleClickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(date.dayOfWeek.name.take(3), color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp)
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(28.dp)
                .then(
                    when {
                        today -> Modifier.clip(CircleShape).background(palette.accent)
                        selected -> Modifier.clip(CircleShape).background(palette.dimText.copy(alpha = 0.45f))
                        else -> Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                date.dayOfMonth.toString().padStart(2, '0'),
                color = if (today) palette.onAccent else palette.primaryText,
                fontFamily = mono,
                fontSize = 14.sp,
            )
        }
        if (selected && !today) {
            Spacer(modifier = Modifier.padding(top = 4.dp).size(width = 20.dp, height = 2.dp).background(palette.accent))
        }
    }
}

@Composable
private fun WeekTimeColumn(
    selectedDate: LocalDate,
    days: List<LocalDate>,
    palette: DotCalPalette,
) {
    val now = LocalTime.now()
    val showNow = selectedDate in days && selectedDate == LocalDate.now()
    Box(
        modifier = Modifier
            .width(32.dp)
            .height((24 * WEEK_HOUR_HEIGHT_DP).dp)
            .drawBehind {
                repeat(23) { hour ->
                    val y = (hour + 1) * WEEK_HOUR_HEIGHT_DP.dp.toPx()
                    drawLine(palette.line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
            },
    ) {
        repeat(24) { hour ->
            Text(
                hour.toString().padStart(2, '0'),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (hour * WEEK_HOUR_HEIGHT_DP + 4).dp),
            )
        }
        if (showNow) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = weekEventTopOffset(now))
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(palette.accent),
            )
        }
    }
}

@Composable
private fun WeekDayColumn(
    day: LocalDate,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    eventLayouts: Map<String, WeekEventLayout>,
    palette: DotCalPalette,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val now = LocalTime.now()
    val showNow = day == selectedDate && day == LocalDate.now()
    Box(
        modifier = modifier
            .height((24 * WEEK_HOUR_HEIGHT_DP).dp)
            .background(palette.calendarSurface)
            .drawBehind {
                drawLine(palette.line, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                repeat(23) { hour ->
                    val y = (hour + 1) * WEEK_HOUR_HEIGHT_DP.dp.toPx()
                    drawLine(palette.line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
            },
    ) {
        repeat(24) { hour ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WEEK_HOUR_HEIGHT_DP.dp)
                    .offset(y = (hour * WEEK_HOUR_HEIGHT_DP).dp)
                    .clickable { onAddAtDate(day, LocalTime.of(hour, 0)) },
            )
        }
        if (showNow) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .offset(y = weekEventTopOffset(now))
                    .background(palette.accent),
            )
        }
        events.sortedBy { it.startTimeMs }.forEach { event ->
            val layout = eventLayouts[event.id] ?: WeekEventLayout(column = 0, columnCount = 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(weekEventHeight(event))
                    .offset(y = weekEventTopOffset(event.startLocalTime()))
                    .zIndex(1f)
                    .padding(horizontal = 2.dp),
            ) {
                repeat(layout.columnCount) { column ->
                    if (column == layout.column) {
                        WeekEventBlock(
                            event = event,
                            palette = palette,
                            onClick = { onEventClick(event) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun weekEventTopOffset(time: LocalTime) =
    (((time.hour * 60 + time.minute) / 60f) * WEEK_HOUR_HEIGHT_DP).dp

private fun weekEventHeight(event: CalendarEvent) =
    ((event.durationMinutes() / 60f) * WEEK_HOUR_HEIGHT_DP).coerceAtLeast(22f).dp

@Composable
private fun WeekEventBlock(
    event: CalendarEvent,
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(event.displayColor(palette).copy(alpha = 0.80f))
            .noRippleClickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(event.title, color = NWhite, fontFamily = mono, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun DayView(
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    palette: DotCalPalette,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onJumpToday: () -> Unit,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val dayAll = remember(eventsByDate, selectedDate) { eventsByDate[selectedDate].orEmpty() }
    val dayEvents = remember(dayAll) { dayAll.filter { it.isTask == 0 } }
    val allDayEvents = remember(dayEvents) { dayEvents.filter { it.isAllDay == 1 } }
    val timedEvents = remember(dayEvents) { dayEvents.filter { it.isAllDay == 0 } }
    val eventLayouts = remember(timedEvents) { layoutTimedEvents(timedEvents) }
    val tasks = remember(dayAll) { dayAll.filter { it.isTask == 1 } }
    var dragTotal by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.calendarSurface)
            .pointerInput(selectedDate) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragTotal < -50.dp.toPx() -> onNextDay()
                            dragTotal > 50.dp.toPx() -> onPreviousDay()
                        }
                        dragTotal = 0f
                    },
                    onHorizontalDrag = { _, amount -> dragTotal += amount },
                )
            },
    ) {
        DayHeader(
            selectedDate = selectedDate,
            palette = palette,
            onPreviousDay = onPreviousDay,
            onNextDay = onNextDay,
            onJumpToday = onJumpToday,
        )
        if (allDayEvents.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(44.dp).background(palette.calendarSurface)) {
                items(allDayEvents.size) { index ->
                    Text(
                        allDayEvents[index].title,
                        color = NWhite,
                        fontFamily = mono,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(allDayEvents[index].displayColor(palette).copy(alpha = 0.75f))
                            .clickable { onEventClick(allDayEvents[index]) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f).background(palette.calendarSurface)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    DayTimeColumn(selectedDate = selectedDate, palette = palette)
                    DayTimelineColumn(
                        selectedDate = selectedDate,
                        events = timedEvents,
                        eventLayouts = eventLayouts,
                        palette = palette,
                        onAddAtDate = onAddAtDate,
                        onEventClick = onEventClick,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (tasks.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text("Tasks", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                        tasks.forEach { task ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(18.dp).clip(RoundedCornerShape(3.dp)).background(palette.cell))
                                Text(
                                    task.title,
                                    color = if (task.isCompleted == 1) palette.secondaryText else palette.primaryText,
                                    fontFamily = mono,
                                    fontSize = 14.sp,
                                    textDecoration = if (task.isCompleted == 1) TextDecoration.LineThrough else null,
                                    modifier = Modifier.padding(start = 12.dp),
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(TIMELINE_BOTTOM_CLEARANCE_DP.dp))
            }
            TimelineBottomBoundary(palette = palette, modifier = Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun TimelineBottomBoundary(
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TIMELINE_BOTTOM_CLEARANCE_DP.dp)
            .background(palette.calendarSurface)
            .drawBehind {
                drawLine(
                    palette.line,
                    Offset(0f, 0f),
                    Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            },
    )
}

@Composable
private fun DayHeader(
    selectedDate: LocalDate,
    palette: DotCalPalette,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onJumpToday: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(palette.calendarSurface)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousDay, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day", tint = palette.primaryText)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .noRippleClickable(onClick = onJumpToday),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                selectedDate.format(dayHeaderFormatter).uppercase(Locale.US),
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
            )
            Text(
                selectedDate.year.toString(),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 11.sp,
                maxLines = 1,
            )
        }
        IconButton(onClick = onNextDay, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next day", tint = palette.primaryText)
        }
    }
}

@Composable
private fun DayTimeColumn(
    selectedDate: LocalDate,
    palette: DotCalPalette,
) {
    val now = LocalTime.now()
    val showNow = selectedDate == LocalDate.now()
    Box(
        modifier = Modifier
            .width(32.dp)
            .height((24 * DAY_HOUR_HEIGHT_DP).dp)
            .drawBehind {
                repeat(23) { hour ->
                    val y = (hour + 1) * DAY_HOUR_HEIGHT_DP.dp.toPx()
                    drawLine(palette.line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
            },
    ) {
        repeat(24) { hour ->
            Text(
                hour.toString().padStart(2, '0'),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (hour * DAY_HOUR_HEIGHT_DP + 6).dp),
            )
        }
        if (showNow) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = dayEventTopOffset(now))
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(palette.accent),
            )
        }
    }
}

@Composable
private fun DayTimelineColumn(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    eventLayouts: Map<String, WeekEventLayout>,
    palette: DotCalPalette,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val now = LocalTime.now()
    val showNow = selectedDate == LocalDate.now()
    Box(
        modifier = modifier
            .height((24 * DAY_HOUR_HEIGHT_DP).dp)
            .background(palette.calendarSurface)
            .drawBehind {
                repeat(23) { hour ->
                    val y = (hour + 1) * DAY_HOUR_HEIGHT_DP.dp.toPx()
                    drawLine(palette.line, Offset(0f, y), Offset(size.width, y), strokeWidth = 1.dp.toPx())
                }
            },
    ) {
        repeat(24) { hour ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(DAY_HOUR_HEIGHT_DP.dp)
                    .offset(y = (hour * DAY_HOUR_HEIGHT_DP).dp)
                    .clickable { onAddAtDate(selectedDate, LocalTime.of(hour, 0)) },
            )
        }
        if (showNow) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .offset(y = dayEventTopOffset(now))
                    .background(palette.accent),
            )
        }
        events.sortedBy { it.startTimeMs }.forEach { event ->
            val layout = eventLayouts[event.id] ?: WeekEventLayout(column = 0, columnCount = 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dayEventHeight(event))
                    .offset(y = dayEventTopOffset(event.startLocalTime()))
                    .zIndex(1f)
                    .padding(start = 6.dp, end = 8.dp),
            ) {
                repeat(layout.columnCount) { column ->
                    if (column == layout.column) {
                        WeekEventBlock(
                            event = event,
                            palette = palette,
                            onClick = { onEventClick(event) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun dayEventTopOffset(time: LocalTime) =
    (((time.hour * 60 + time.minute) / 60f) * DAY_HOUR_HEIGHT_DP).dp

private fun dayEventHeight(event: CalendarEvent) =
    ((event.durationMinutes() / 60f) * DAY_HOUR_HEIGHT_DP).coerceAtLeast(24f).dp

@Composable
internal fun ThreeDayView(
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    palette: DotCalPalette,
    onPreviousRange: () -> Unit,
    onNextRange: () -> Unit,
    onJumpToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val days = remember(selectedDate) { List(3) { selectedDate.plusDays(it.toLong()) } }
    val rangeEvents = remember(eventsByDate, days) {
        days.flatMap { eventsByDate[it].orEmpty() }.filter { it.isAllDay == 0 }
    }
    val rangeEventsByDayHour = remember(rangeEvents) {
        rangeEvents.groupBy { event -> event.localDate() to event.startLocalTime().hour }
    }
    var dragTotal by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(selectedDate) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragTotal < -50.dp.toPx() -> onNextRange()
                            dragTotal > 50.dp.toPx() -> onPreviousRange()
                        }
                        dragTotal = 0f
                    },
                    onHorizontalDrag = { _, amount -> dragTotal += amount },
                )
            },
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(64.dp).background(palette.calendarSurface)) {
            days.forEach { day ->
                WeekDayHeader(
                    date = day,
                    selected = day == selectedDate,
                    palette = palette,
                    onClick = { onDateSelected(day) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
            items(24) { hour ->
                ThreeDayHourRow(
                    hour = hour,
                    days = days,
                    selectedDate = selectedDate,
                    eventsByDayHour = rangeEventsByDayHour,
                    palette = palette,
                    onAddAtDate = onAddAtDate,
                    onEventClick = onEventClick,
                )
            }
        }
    }
}

@Composable
private fun ThreeDayHourRow(
    hour: Int,
    days: List<LocalDate>,
    selectedDate: LocalDate,
    eventsByDayHour: Map<Pair<LocalDate, Int>, List<CalendarEvent>>,
    palette: DotCalPalette,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val now = LocalTime.now()
    val showNow = selectedDate == LocalDate.now() && hour == now.hour
    Row(modifier = Modifier.fillMaxWidth().height(68.dp).background(palette.calendarSurface)) {
        Box(modifier = Modifier.width(52.dp).height(68.dp), contentAlignment = Alignment.TopCenter) {
            Text("${hour.toString().padStart(2, '0')}:00", color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp, modifier = Modifier.padding(top = 6.dp))
        }
        days.forEach { day ->
            val dayEvents = eventsByDayHour[day to hour].orEmpty()
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .background(palette.calendarSurface)
                    .drawBehind {
                        drawLine(palette.line, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                        drawLine(palette.line, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                    },
            ) {
                if (dayEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { onAddAtDate(day, LocalTime.of(hour, 0)) },
                    )
                }
                if (showNow && day == selectedDate) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .offset(y = ((now.minute / 60f) * 68).dp)
                            .background(palette.accent),
                    )
                }
                dayEvents.take(2).forEachIndexed { index, event ->
                    WeekEventBlock(
                        event = event,
                        palette = palette,
                        onClick = { onEventClick(event) },
                        modifier = Modifier.zIndex(1f).padding(start = 5.dp, end = 5.dp, top = (5 + index * 29).dp).height(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
internal fun YearView(
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    palette: DotCalPalette,
    weekStart: DayOfWeek,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onJumpToday: () -> Unit,
    onMonthSelected: (LocalDate) -> Unit,
) {
    var dragTotal by remember { mutableFloatStateOf(0f) }
    val months = remember(selectedDate.year) { List(12) { selectedDate.withMonth(it + 1).withDayOfMonth(1) } }
    val eventDates = remember(eventsByDate, selectedDate.year) {
        eventsByDate.entries
            .filter { (date, dayEvents) -> date.year == selectedDate.year && dayEvents.any { it.isTask == 0 } }
            .map { it.key }
            .toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(selectedDate.year) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            dragTotal < -50.dp.toPx() -> onNextYear()
                            dragTotal > 50.dp.toPx() -> onPreviousYear()
                        }
                        dragTotal = 0f
                    },
                    onHorizontalDrag = { _, amount -> dragTotal += amount },
                )
            },
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
            contentPadding = PaddingValues(start = 8.dp, top = 0.dp, end = 8.dp, bottom = 150.dp),
        ) {
            items(months) { month ->
                YearMonthCell(
                    month = month,
                    selected = month.year == selectedDate.year && month.monthValue == selectedDate.monthValue,
                    eventDates = eventDates,
                    palette = palette,
                    weekStart = weekStart,
                    onClick = { onMonthSelected(month) },
                )
            }
        }
    }
}

@Composable
private fun YearMonthCell(
    month: LocalDate,
    selected: Boolean,
    eventDates: Set<LocalDate>,
    palette: DotCalPalette,
    weekStart: DayOfWeek,
    onClick: () -> Unit,
) {
    val days = remember(month, weekStart) { monthGrid(month, weekStart) }
    val today = LocalDate.now()
    val isCurrentMonth = month.year == today.year && month.monthValue == today.monthValue
    Column(
        modifier = Modifier
            .aspectRatio(0.82f)
            .padding(3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) palette.cell else Color.Transparent)
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 7.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(month.month.name.take(3), color = if (isCurrentMonth) palette.accent else palette.yearMonthLabel, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(month.monthValue.toString().padStart(2, '0'), color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(5.dp))
        MiniMonthGridCanvas(
            month = month,
            days = days,
            today = today,
            eventDates = eventDates,
            palette = palette,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

@Composable
private fun MiniMonthGridCanvas(
    month: LocalDate,
    days: List<LocalDate>,
    today: LocalDate,
    eventDates: Set<LocalDate>,
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
) {
    val weekdayColor = palette.yearWeekday.toArgb()
    val secondaryColor = palette.secondaryText.toArgb()
    val dimColor = palette.dimText.copy(alpha = 0.35f).toArgb()
    val accentColor = palette.accent.toArgb()
    val onAccentColor = palette.onAccent.toArgb()
    Canvas(modifier = modifier) {
        val nativeCanvas = drawContext.canvas.nativeCanvas
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = weekdayColor
            textAlign = Paint.Align.CENTER
            textSize = 7.sp.toPx()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            textSize = 7.sp.toPx()
        }
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentColor
            style = Paint.Style.FILL
        }
        val columnWidth = size.width / 7f
        val rowHeight = size.height / 7f
        val labels = listOf("S", "M", "T", "W", "T", "F", "S")
        labels.forEachIndexed { index, label ->
            val x = columnWidth * index + columnWidth / 2f
            val y = rowHeight * 0.58f
            nativeCanvas.drawText(label, x, y, labelPaint)
        }
        days.forEachIndexed { index, day ->
            val row = index / 7 + 1
            val column = index % 7
            val x = columnWidth * column + columnWidth / 2f
            val y = rowHeight * row + rowHeight * 0.62f
            val inMonth = day.monthValue == month.monthValue
            val isToday = day == today
            val hasEvent = day in eventDates
            val isWeekdayDate = inMonth && day.dayOfWeek != DayOfWeek.SATURDAY && day.dayOfWeek != DayOfWeek.SUNDAY
            if (isToday) {
                nativeCanvas.drawCircle(x, rowHeight * row + rowHeight / 2f, 7.5.dp.toPx(), circlePaint)
            }
            datePaint.color = when {
                isToday -> onAccentColor
                !inMonth -> dimColor
                hasEvent -> accentColor
                else -> secondaryColor
            }
            datePaint.typeface = Typeface.create(Typeface.DEFAULT, if (isWeekdayDate) Typeface.BOLD else Typeface.NORMAL)
            nativeCanvas.drawText(day.dayOfMonth.toString(), x, y, datePaint)
        }
    }
}


private data class WeekEventLayout(
    val column: Int,
    val columnCount: Int,
)

private fun layoutTimedEvents(events: List<CalendarEvent>): Map<String, WeekEventLayout> {
    val result = mutableMapOf<String, WeekEventLayout>()
    events.groupBy { it.localDate() }.values.forEach { dayEvents ->
        val sorted = dayEvents.sortedBy { it.startTimeMs }
        val cluster = mutableListOf<CalendarEvent>()
        var clusterEnd = Long.MIN_VALUE

        fun flushCluster() {
            if (cluster.isEmpty()) return
            val columnEnds = mutableListOf<Long>()
            val assignments = mutableListOf<Pair<CalendarEvent, Int>>()
            cluster.sortedBy { it.startTimeMs }.forEach { event ->
                val start = event.startTimeMs
                val end = event.normalizedEndTimeMs()
                val reusableColumn = columnEnds.indexOfFirst { it <= start }
                val column = if (reusableColumn >= 0) reusableColumn else columnEnds.size
                if (reusableColumn >= 0) {
                    columnEnds[column] = end
                } else {
                    columnEnds.add(end)
                }
                assignments += event to column
            }
            val columnCount = columnEnds.size.coerceAtLeast(1)
            assignments.forEach { (event, column) ->
                result[event.id] = WeekEventLayout(column = column, columnCount = columnCount)
            }
            cluster.clear()
        }

        sorted.forEach { event ->
            val end = event.normalizedEndTimeMs()
            if (cluster.isNotEmpty() && event.startTimeMs >= clusterEnd) {
                flushCluster()
                clusterEnd = Long.MIN_VALUE
            }
            cluster += event
            clusterEnd = maxOf(clusterEnd, end)
        }
        flushCluster()
    }
    return result
}

private fun monthGrid(month: LocalDate, weekStart: DayOfWeek): List<LocalDate> {
    val first = month.withDayOfMonth(1)
    val delta = (7 + first.dayOfWeek.value - weekStart.value) % 7
    val start = first.minusDays(delta.toLong())
    return List(42) { start.plusDays(it.toLong()) }
}

private fun weekDays(date: LocalDate, weekStart: DayOfWeek): List<LocalDate> {
    val delta = (7 + date.dayOfWeek.value - weekStart.value) % 7
    val start = date.minusDays(delta.toLong())
    return List(7) { start.plusDays(it.toLong()) }
}

private fun weekDayLabels(weekStart: DayOfWeek): List<String> {
    return List(7) { index ->
        weekStart.plus(index.toLong()).name.take(3)
    }
}
