package com.dotfield.dotcal.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick as semanticsOnClick
import androidx.compose.ui.semantics.onLongClick as semanticsOnLongClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.dotfield.dotcal.R
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.baseEventId
import com.dotfield.dotcal.data.insights.OnThisDayMemory
import com.dotfield.dotcal.data.scheduling.EventDragMath
import com.dotfield.dotcal.data.scheduling.EventTimeRange
import com.dotfield.dotcal.ui.theme.NBlack
import com.dotfield.dotcal.ui.theme.NWhite
import java.time.Duration
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

internal data class EventDragChange(
    val event: CalendarEvent,
    val targetStart: LocalDateTime,
    val targetEnd: LocalDateTime,
)

private const val WEEK_ALL_DAY_MAX_ROWS = 3
private const val WEEK_ALL_DAY_ROW_HEIGHT_DP = 24
private const val WEEK_ALL_DAY_VERTICAL_PADDING_DP = 4
private const val WEEK_ALL_DAY_ROW_GAP_DP = 3

private enum class EventDragMode {
    Move,
    ResizeStart,
    ResizeEnd,
}

@Composable
internal fun MonthView(
    month: LocalDate,
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    shiftEventIds: Set<String>,
    palette: DotCalPalette,
    weekStart: DayOfWeek,
    showWeekNumbers: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    highlightDate: LocalDate?,
    selectedBulkDates: Set<LocalDate>,
    onBulkSelectionStart: (LocalDate) -> Unit,
    onBulkApply: () -> Unit,
    onBulkClear: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val days = remember(month, weekStart) { monthGrid(month, weekStart) }
    val locale = currentResourceLocale()
    val weekDayLabels = remember(weekStart, locale) { weekDayLabels(weekStart, locale) }
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
        val weekNumberColumnWidth = 36.dp
        Row(modifier = Modifier.fillMaxWidth().height(32.dp).background(palette.calendarSurface)) {
            if (showWeekNumbers) {
                WeekNumberCell(label = "", palette = palette, modifier = Modifier.width(weekNumberColumnWidth).fillMaxHeight())
            }
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

        BoxWithConstraints(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val weekNumberWidth = if (showWeekNumbers) weekNumberColumnWidth else 0.dp
            val dayCellMetrics = monthDayCellMetrics(maxWidth, maxHeight, weekNumberWidth)
            val eventSlotsByDate = remember(eventsByDate, days, dayCellMetrics.visibleChipCount) {
                days.chunked(7).flatMap { week ->
                    monthEventSlotsByDate(
                        weekDates = week,
                        eventsByDate = eventsByDate,
                        visibleSlotCount = dayCellMetrics.visibleChipCount,
                    ).entries
                }.associate { it.key to it.value }
            }
            Column(modifier = Modifier.fillMaxWidth()) {
                days.chunked(7).forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth().height(dayCellMetrics.height)) {
                        if (showWeekNumbers) {
                            WeekNumberCell(
                                label = isoWeekNumberLabel(week.first()),
                                palette = palette,
                                modifier = Modifier.width(weekNumberColumnWidth).fillMaxHeight(),
                                contentAlignment = Alignment.TopCenter,
                                textModifier = Modifier.padding(top = 7.dp),
                            )
                        }
                        week.forEach { day ->
                            DayCell(
                                date = day,
                                weekDates = week,
                                activeMonth = YearMonth.from(month),
                                isSelected = day == selectedDate,
                                isBulkSelected = day in selectedBulkDates,
                                isHighlighted = day == highlightDate,
                                events = eventsByDate[day].orEmpty(),
                                eventSlots = eventSlotsByDate[day].orEmpty(),
                                shiftEventIds = shiftEventIds,
                                visibleChipCount = dayCellMetrics.visibleChipCount,
                                palette = palette,
                                onClick = { onDateSelected(day) },
                                onLongPress = { onBulkSelectionStart(day) },
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                            )
                        }
                    }
                }
            }
        }
        if (selectedBulkDates.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.calendarSurface)
                    .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 102.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    pluralStringResource(R.plurals.count_selected, selectedBulkDates.size, selectedBulkDates.size),
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = onBulkClear) { Text(stringResource(R.string.calendar_clear), color = palette.secondaryText, fontFamily = mono) }
                Button(
                    onClick = onBulkApply,
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(stringResource(R.string.calendar_apply_template), fontFamily = mono, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    weekDates: List<LocalDate>,
    activeMonth: YearMonth,
    isSelected: Boolean,
    isBulkSelected: Boolean,
    isHighlighted: Boolean,
    events: List<CalendarEvent>,
    eventSlots: List<CalendarEvent?>,
    shiftEventIds: Set<String>,
    visibleChipCount: Int,
    palette: DotCalPalette,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isToday = date == LocalDate.now()
    val inMonth = YearMonth.from(date) == activeMonth
    val haptic = LocalHapticFeedback.current
    val todayLabel = stringResource(R.string.a11y_today)
    val outsideCurrentMonthLabel = stringResource(R.string.a11y_outside_current_month)
    val selectedLabel = stringResource(R.string.a11y_selected)
    val bulkSelectedLabel = stringResource(R.string.a11y_bulk_selected)
    val notSelectedLabel = stringResource(R.string.a11y_not_selected)
    val noEventsLabel = stringResource(R.string.a11y_no_events)
    val eventCountLabel = pluralStringResource(R.plurals.a11y_event_count, events.size, events.size)
    val visibleEventCount = eventSlots.count { it != null }
    val hiddenEventCount = (events.size - visibleEventCount).coerceAtLeast(0)
    val moreEventsLabel = pluralStringResource(
        R.plurals.a11y_more_events,
        hiddenEventCount,
        hiddenEventCount,
    )
    val untitledEventLabel = stringResource(R.string.a11y_untitled_event)
    val openDayLabel = stringResource(R.string.a11y_open_day)
    val selectDateLabel = stringResource(R.string.a11y_select_date)
    val accessibilityLabel = remember(
        date,
        events,
        eventSlots,
        visibleChipCount,
        inMonth,
        isToday,
        isSelected,
        isBulkSelected,
        todayLabel,
        outsideCurrentMonthLabel,
        selectedLabel,
        bulkSelectedLabel,
        noEventsLabel,
        eventCountLabel,
        moreEventsLabel,
        untitledEventLabel,
    ) {
        monthDayAccessibilityLabel(
            date = date,
            events = events,
            eventSlots = eventSlots,
            visibleChipCount = visibleChipCount,
            inMonth = inMonth,
            isToday = isToday,
            isSelected = isSelected,
            isBulkSelected = isBulkSelected,
            todayLabel = todayLabel,
            outsideCurrentMonthLabel = outsideCurrentMonthLabel,
            selectedLabel = selectedLabel,
            bulkSelectedLabel = bulkSelectedLabel,
            noEventsLabel = noEventsLabel,
            eventCountLabel = eventCountLabel,
            moreEventsLabel = moreEventsLabel,
            untitledEventLabel = untitledEventLabel,
        )
    }
    val accessibilityState = remember(isSelected, isBulkSelected) {
        when {
            isBulkSelected -> bulkSelectedLabel
            isSelected -> selectedLabel
            else -> notSelectedLabel
        }
    }
    val highlightColor by animateColorAsState(
        targetValue = if (isHighlighted) palette.accent.copy(alpha = 0.28f) else Color.Transparent,
        animationSpec = tween(durationMillis = 500),
        label = "jumpDayHighlight",
    )
    Box(
        modifier = modifier
            .background(palette.calendarSurface)
            .semantics(mergeDescendants = true) {
                contentDescription = accessibilityLabel
                stateDescription = accessibilityState
                this.selected = isSelected || isBulkSelected
                semanticsOnClick(label = openDayLabel) {
                    onClick()
                    true
                }
                semanticsOnLongClick(label = selectDateLabel) {
                    onLongPress()
                    true
                }
            }
            .drawBehind {
                if (inMonth && highlightColor.alpha > 0f) {
                    drawCircle(
                        color = highlightColor,
                        radius = minOf(22.dp.toPx(), size.minDimension * 0.42f),
                        center = Offset(size.width / 2f, 18.dp.toPx()),
                    )
                }
            }
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
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 3.dp),
        ) {
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
                Spacer(modifier = Modifier.height(3.dp))
                eventSlots.take(visibleChipCount).forEach { event ->
                    if (event == null) {
                        Spacer(modifier = Modifier.height(14.dp))
                    } else {
                        MonthEventChip(
                            event = event,
                            date = date,
                            weekDates = weekDates,
                            isShift = event.baseEventId() in shiftEventIds,
                            palette = palette,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(event.visibleSegmentPadding(date, weekDates, 3.dp)),
                        )
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                }
                if (hiddenEventCount > 0) {
                    Text(
                        stringResource(R.string.month_day_more_count, hiddenEventCount),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp),
                        fontFamily = mono,
                        fontSize = 8.sp,
                        color = palette.secondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(includeFontPadding = false),
                            lineHeight = 9.sp,
                            lineHeightStyle = LineHeightStyle(
                                alignment = LineHeightStyle.Alignment.Center,
                                trim = LineHeightStyle.Trim.Both,
                            ),
                        ),
                    )
                }
            } else {
                Box(
                    modifier = Modifier.size(28.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        date.dayOfMonth.toString().padStart(2, '0'),
                        color = palette.disabledText,
                        fontFamily = mono,
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

private fun monthDayAccessibilityLabel(
    date: LocalDate,
    events: List<CalendarEvent>,
    eventSlots: List<CalendarEvent?>,
    visibleChipCount: Int,
    inMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,
    isBulkSelected: Boolean,
    todayLabel: String,
    outsideCurrentMonthLabel: String,
    selectedLabel: String,
    bulkSelectedLabel: String,
    noEventsLabel: String,
    eventCountLabel: String,
    moreEventsLabel: String,
    untitledEventLabel: String,
): String = buildString {
    if (isToday) append(todayLabel).append(", ")
    append(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())))
    if (!inMonth) append(", ").append(outsideCurrentMonthLabel)
    if (isSelected) append(", ").append(selectedLabel)
    if (isBulkSelected) append(", ").append(bulkSelectedLabel)
    append(", ")
    append(if (events.isEmpty()) noEventsLabel else eventCountLabel)
    val visibleTitles = eventSlots.mapNotNull { it?.title?.ifBlank { untitledEventLabel } }
    if (visibleTitles.isNotEmpty()) {
        append(": ")
        append(visibleTitles.joinToString())
    }
    if (events.size > visibleTitles.size) {
        append(", ")
        append(moreEventsLabel)
    }
}

@Composable
private fun MonthEventChip(
    event: CalendarEvent,
    date: LocalDate,
    weekDates: List<LocalDate>,
    isShift: Boolean,
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
) {
    val eventColor = event.displayColor(palette)
    val contentColor = monthEventContentColor(eventColor)
    val title = event.title.ifBlank { stringResource(R.string.event_conflict_untitled) }
    val showTitle = event.shouldShowStripTitle(date, weekDates)
    Row(
        modifier = modifier
            .height(14.dp)
            .clip(event.visibleSegmentShape(date, weekDates, 3.dp))
            .background(eventColor.copy(alpha = if (event.isGhost) 0.32f else 0.75f))
            .padding(horizontal = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showTitle && isShift) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(contentColor),
            )
            Spacer(Modifier.width(3.dp))
        }
        if (showTitle) {
            Text(
                title,
                modifier = Modifier.fillMaxWidth(),
                color = contentColor.copy(alpha = if (event.isGhost) 0.78f else 1f),
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 8.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                    lineHeight = 9.sp,
                    lineHeightStyle = LineHeightStyle(
                        alignment = LineHeightStyle.Alignment.Center,
                        trim = LineHeightStyle.Trim.Both,
                    ),
                ),
            )
        }
    }
}

private fun monthEventContentColor(eventColor: Color): Color {
    return if (eventColor.luminance() > 0.58f) NBlack else NWhite
}

private fun CalendarEvent.visibleSegmentShape(date: LocalDate, segmentDates: List<LocalDate>, radius: Dp): RoundedCornerShape {
    val dates = visibleDates().filter { it in segmentDates }
    if (dates.size <= 1) return RoundedCornerShape(radius)
    val index = dates.indexOf(date).takeIf { it >= 0 } ?: return RoundedCornerShape(radius)
    return RoundedCornerShape(
        topStart = if (index == 0) radius else 0.dp,
        bottomStart = if (index == 0) radius else 0.dp,
        topEnd = if (index == dates.lastIndex) radius else 0.dp,
        bottomEnd = if (index == dates.lastIndex) radius else 0.dp,
    )
}

private fun CalendarEvent.visibleSegmentPadding(date: LocalDate, segmentDates: List<LocalDate>, edgePadding: Dp): PaddingValues {
    val dates = visibleDates().filter { it in segmentDates }
    if (dates.size <= 1) return PaddingValues(horizontal = edgePadding)
    val index = dates.indexOf(date).takeIf { it >= 0 } ?: return PaddingValues(horizontal = edgePadding)
    return PaddingValues(
        start = if (index == 0) edgePadding else 0.dp,
        top = 0.dp,
        end = if (index == dates.lastIndex) edgePadding else 0.dp,
        bottom = 0.dp,
    )
}

internal fun monthEventSlotsByDate(
    weekDates: List<LocalDate>,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    visibleSlotCount: Int,
): Map<LocalDate, List<CalendarEvent?>> {
    if (visibleSlotCount <= 0) return weekDates.associateWith { emptyList() }
    val slotsByDate = weekDates.associateWith { MutableList<CalendarEvent?>(visibleSlotCount) { null } }
    val weekEvents = weekDates
        .flatMap { eventsByDate[it].orEmpty() }
        .distinctBy { it.id }
    val multiDayEvents = weekEvents
        .filter { it.isMultiDayVisible() }
        .sortedWith(compareBy<CalendarEvent>(
            { event -> event.visibleDates().firstOrNull { it in weekDates } ?: LocalDate.MAX },
            { event -> -event.visibleDates().count { it in weekDates } },
            { it.startTimeMs },
            { it.id },
        ))

    multiDayEvents.forEach { event ->
        val dates = event.visibleDates().filter { it in weekDates }
        val lane = (0 until visibleSlotCount).firstOrNull { index ->
            dates.all { date -> slotsByDate[date]?.get(index) == null }
        } ?: return@forEach
        dates.forEach { date -> slotsByDate[date]?.set(lane, event) }
    }

    weekDates.forEach { date ->
        val occupiedEventIds = slotsByDate[date].orEmpty().mapNotNull { it?.id }.toSet()
        val singles = eventsByDate[date].orEmpty()
            .filter { !it.isMultiDayVisible() && it.id !in occupiedEventIds }
        val daySlots = slotsByDate.getValue(date)
        singles.forEach { event ->
            val lane = daySlots.indexOfFirst { it == null }
            if (lane >= 0) daySlots[lane] = event
        }
    }

    return slotsByDate.mapValues { it.value.toList() }
}

@Composable
internal fun WeekView(
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    palette: DotCalPalette,
    weekStart: DayOfWeek,
    showWeekNumbers: Boolean,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onJumpToday: () -> Unit,
    onJumpPickerRequest: () -> Unit,
    highlightDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onEventDrag: (EventDragChange) -> Unit,
    onAvailabilityRequest: (LocalDate) -> Unit,
    use24HourFormat: Boolean,
) {
    val days = remember(selectedDate, weekStart) { weekDays(selectedDate, weekStart) }
    val weekEvents = remember(eventsByDate, days) { days.flatMap { eventsByDate[it].orEmpty() }.distinctBy { it.id } }
    val timedEvents = remember(weekEvents) { weekEvents.filter { it.isAllDay == 0 && !it.isMultiDayVisible() } }
    val allDayEventsByDay = remember(eventsByDate, days) {
        days.associateWith { day ->
            eventsByDate[day].orEmpty().filter { it.isAllDay == 1 || it.isMultiDayVisible() }.distinctBy { it.id }
        }
    }
    val allDayRowCount = remember(allDayEventsByDay) {
        allDayEventsByDay.values.maxOfOrNull { it.size }?.coerceIn(1, WEEK_ALL_DAY_MAX_ROWS) ?: 0
    }
    val allDayEventSlotsByDay = remember(allDayEventsByDay, days, allDayRowCount) {
        monthEventSlotsByDate(days, allDayEventsByDay, allDayRowCount)
    }
    val eventLayouts = remember(timedEvents) { layoutTimedEvents(timedEvents) }
    val timedEventsByDay = remember(eventsByDate, days) {
        days.associateWith { day ->
            eventsByDate[day].orEmpty().filter { it.isAllDay == 0 && !it.isMultiDayVisible() }.distinctBy { it.id }
        }
    }

    var dragTotal by remember { mutableFloatStateOf(0f) }
    var activeDragDay by remember(selectedDate) { mutableStateOf<LocalDate?>(null) }
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
            if (showWeekNumbers) {
                WeekNumberCell(label = isoWeekNumberLabel(days.first()), palette = palette, modifier = Modifier.width(36.dp).fillMaxHeight())
            } else {
                Spacer(modifier = Modifier.width(32.dp))
            }
            days.forEach { day ->
                WeekDayHeader(
                    date = day,
                    selected = day == selectedDate,
                    highlighted = day == highlightDate,
                    palette = palette,
                    onClick = { onDateSelected(day) },
                    onLongClick = { onAvailabilityRequest(day) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (allDayRowCount > 0) {
            AllDayStripRows(
                days = days,
                eventSlotsByDate = allDayEventSlotsByDay,
                leadingWidth = if (showWeekNumbers) 36.dp else 32.dp,
                palette = palette,
                onEventClick = onEventClick,
            )
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
                    WeekTimeColumn(selectedDate = selectedDate, days = days, palette = palette, width = if (showWeekNumbers) 36.dp else 32.dp)
                    days.forEach { day ->
                        WeekDayColumn(
                            day = day,
                            selectedDate = selectedDate,
                            events = timedEventsByDay[day].orEmpty(),
                            eventLayouts = eventLayouts,
                            palette = palette,
                            onAddAtDate = onAddAtDate,
                            onEventClick = onEventClick,
                            onEventDrag = onEventDrag,
                            use24HourFormat = use24HourFormat,
                            allowedDayDelta = -days.indexOf(day)..(days.lastIndex - days.indexOf(day)),
                            onDragActiveChange = { active ->
                                activeDragDay = when {
                                    active -> day
                                    activeDragDay == day -> null
                                    else -> activeDragDay
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .zIndex(if (activeDragDay == day) 10f else 0f),
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
private fun AllDayStripRows(
    days: List<LocalDate>,
    eventSlotsByDate: Map<LocalDate, List<CalendarEvent?>>,
    leadingWidth: Dp,
    palette: DotCalPalette,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val rowCount = eventSlotsByDate.values.maxOfOrNull { it.size } ?: 0
    val stripHeight = (
        WEEK_ALL_DAY_VERTICAL_PADDING_DP * 2 +
            rowCount * WEEK_ALL_DAY_ROW_HEIGHT_DP +
            (rowCount - 1).coerceAtLeast(0) * WEEK_ALL_DAY_ROW_GAP_DP
        ).dp
    Row(modifier = Modifier.fillMaxWidth().height(stripHeight).background(palette.calendarSurface)) {
        if (leadingWidth > 0.dp) {
            Spacer(modifier = Modifier.width(leadingWidth))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(vertical = WEEK_ALL_DAY_VERTICAL_PADDING_DP.dp),
            verticalArrangement = Arrangement.spacedBy(WEEK_ALL_DAY_ROW_GAP_DP.dp),
        ) {
            repeat(rowCount) { rowIndex ->
                Row(modifier = Modifier.fillMaxWidth().height(WEEK_ALL_DAY_ROW_HEIGHT_DP.dp)) {
                    days.forEach { day ->
                        val event = eventSlotsByDate[day].orEmpty().getOrNull(rowIndex)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(event?.visibleSegmentPadding(day, days, 2.dp) ?: PaddingValues(horizontal = 2.dp))
                                .clip(event?.visibleSegmentShape(day, days, 4.dp) ?: RoundedCornerShape(0.dp))
                                .background(if (event == null) Color.Transparent else event.displayColor(palette).copy(alpha = if (event.isGhost) 0.32f else 0.75f))
                                .then(if (event?.isGhost == true) Modifier.ghostDottedBorder(palette, 4f) else Modifier)
                                .then(if (event == null) Modifier else Modifier.clickable { onEventClick(event) }),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (event != null && event.shouldShowStripTitle(day, days)) {
                                Text(
                                    event.title,
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                    color = monthEventContentColor(event.displayColor(palette)),
                                    fontFamily = mono,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekNumberCell(
    label: String,
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    textModifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(palette.calendarSurface),
        contentAlignment = contentAlignment,
    ) {
        Text(
            label,
            modifier = textModifier,
            color = palette.secondaryText.copy(alpha = 0.72f),
            fontFamily = mono,
            fontSize = 9.sp,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun isoWeekNumberLabel(date: LocalDate): String {
    return stringResource(
        R.string.week_number_short,
        date.get(WeekFields.ISO.weekOfWeekBasedYear()).toString().padStart(2, '0'),
    )
}

@Composable
private fun WeekDayHeader(
    date: LocalDate,
    selected: Boolean,
    highlighted: Boolean,
    palette: DotCalPalette,
    onClick: () -> Unit,
    onLongClick: () -> Unit = onClick,
    modifier: Modifier = Modifier,
) {
    val today = date == LocalDate.now()
    val haptic = LocalHapticFeedback.current
    val locale = currentResourceLocale()
    val highlightColor by animateColorAsState(
        targetValue = if (highlighted) palette.accent.copy(alpha = 0.24f) else Color.Transparent,
        animationSpec = tween(durationMillis = 500),
        label = "jumpWeekHeaderHighlight",
    )
    Column(
        modifier = modifier
            .drawBehind {
                if (highlightColor.alpha > 0f) {
                    drawCircle(
                        color = highlightColor,
                        radius = size.minDimension * 0.44f,
                        center = Offset(size.width / 2f, size.height * 0.55f),
                    )
                }
            }
            .pointerInput(date) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    },
                )
            }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, locale), color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp)
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
    width: androidx.compose.ui.unit.Dp = 32.dp,
) {
    val now = LocalTime.now()
    val showNow = selectedDate in days && selectedDate == LocalDate.now()
    Box(
        modifier = Modifier
            .width(width)
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
    onEventDrag: (EventDragChange) -> Unit,
    use24HourFormat: Boolean,
    allowedDayDelta: IntRange,
    onDragActiveChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val now = LocalTime.now()
    val showNow = day == selectedDate && day == LocalDate.now()
    BoxWithConstraints(
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
            DraggableEventRow(
                event = event,
                layout = layout,
                palette = palette,
                baseTop = weekEventTopOffset(event.startLocalTime()),
                hourHeightDp = WEEK_HOUR_HEIGHT_DP,
                minimumHeightDp = 22f,
                dayColumnWidthPx = constraints.maxWidth.toFloat(),
                allowedDayDelta = allowedDayDelta,
                use24HourFormat = use24HourFormat,
                onClick = { onEventClick(event) },
                onEventDrag = onEventDrag,
                onDragActiveChange = onDragActiveChange,
            )
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
    isShift: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val eventColor = event.displayColor(palette)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(eventColor.copy(alpha = if (event.isGhost) 0.34f else if (isShift) 0.90f else 0.80f))
            .then(if (event.isGhost) Modifier.ghostDottedBorder(palette, 4f) else Modifier)
            .noRippleClickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShiftMarker(isShift = isShift, color = NWhite)
        Text(event.title, color = NWhite, fontFamily = mono, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun ShiftMarker(isShift: Boolean, color: Color) {
    if (isShift) {
        Box(
            modifier = Modifier
                .padding(end = 5.dp)
                .size(width = 3.dp, height = 12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color.copy(alpha = 0.95f)),
        )
    }
}

@Composable
private fun DraggableEventRow(
    event: CalendarEvent,
    layout: WeekEventLayout,
    palette: DotCalPalette,
    baseTop: androidx.compose.ui.unit.Dp,
    hourHeightDp: Float,
    minimumHeightDp: Float,
    dayColumnWidthPx: Float,
    allowedDayDelta: IntRange,
    use24HourFormat: Boolean,
    isShift: Boolean = false,
    onClick: () -> Unit,
    onEventDrag: (EventDragChange) -> Unit,
    onDragActiveChange: (Boolean) -> Unit = {},
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val hourHeightPx = with(density) { hourHeightDp.dp.toPx() }
    val originalRange = remember(event.id, event.startTimeMs, event.endTimeMs) {
        val zoneId = ZoneId.systemDefault()
        EventTimeRange(
            start = Instant.ofEpochMilli(event.startTimeMs).atZone(zoneId).toLocalDateTime(),
            end = Instant.ofEpochMilli(event.endTimeMs).atZone(zoneId).toLocalDateTime(),
        )
    }
    var dragMode by remember(event.id) { mutableStateOf<EventDragMode?>(null) }
    var totalX by remember(event.id) { mutableFloatStateOf(0f) }
    var totalY by remember(event.id) { mutableFloatStateOf(0f) }
    var minuteDelta by remember(event.id) { mutableIntStateOf(0) }
    var dayDelta by remember(event.id) { mutableIntStateOf(0) }

    fun resetDrag() {
        val wasDragging = dragMode != null
        dragMode = null
        totalX = 0f
        totalY = 0f
        minuteDelta = 0
        dayDelta = 0
        if (wasDragging) {
            onDragActiveChange(false)
        }
    }

    fun updateDrag(amount: Offset) {
        totalX += amount.x
        totalY += amount.y
        val nextMinutes = EventDragMath.snapMinutes(totalY, hourHeightPx)
        val nextDays = if (dragMode == EventDragMode.Move && dayColumnWidthPx > 0f) {
            kotlin.math.round(totalX / dayColumnWidthPx).toInt().coerceIn(allowedDayDelta)
        } else {
            0
        }
        if (nextMinutes != minuteDelta || nextDays != dayDelta) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
        minuteDelta = nextMinutes
        dayDelta = nextDays
    }

    fun currentRange(): EventTimeRange {
        return when (dragMode) {
            EventDragMode.Move -> EventDragMath.move(originalRange.start, originalRange.end, minuteDelta, dayDelta)
            EventDragMode.ResizeStart -> EventDragMath.resizeStart(originalRange.start, originalRange.end, minuteDelta)
            EventDragMode.ResizeEnd -> EventDragMath.resizeEnd(originalRange.start, originalRange.end, minuteDelta)
            null -> originalRange
        }
    }
    val previewRange = currentRange()
    val previewDurationMinutes = Duration.between(previewRange.start, previewRange.end).toMinutes().coerceAtLeast(15)
    val previewHeight = safeTimedEventHeight(previewDurationMinutes, hourHeightDp, minimumHeightDp)
    val translationY = when (dragMode) {
        EventDragMode.Move, EventDragMode.ResizeStart -> (minuteDelta / 60f) * hourHeightPx
        else -> 0f
    }
    val translationX = if (dragMode == EventDragMode.Move) dayDelta * dayColumnWidthPx else 0f
    val draggable = event.isTask == 0 && event.isAllDay == 0 && event.source != "BIRTHDAY"

    fun finishDrag() {
        val finalRange = currentRange()
        val changed = finalRange != originalRange
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (changed) {
            onEventDrag(EventDragChange(event, finalRange.start, finalRange.end))
        }
        resetDrag()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(previewHeight)
            .offset(y = baseTop)
            .graphicsLayer {
                this.translationX = translationX
                this.translationY = translationY
                shadowElevation = if (dragMode == null) 0f else 12.dp.toPx()
                alpha = if (dragMode == null) 1f else 0.92f
            }
            .zIndex(if (dragMode == null) 1f else 20f)
            .padding(horizontal = 2.dp),
    ) {
        repeat(layout.columnCount) { column ->
            if (column == layout.column) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .pointerInput(event.id, draggable, hourHeightPx, dayColumnWidthPx) {
                            if (!draggable) return@pointerInput
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    dragMode = EventDragMode.Move
                                    onDragActiveChange(true)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDragCancel = ::resetDrag,
                                onDragEnd = ::finishDrag,
                                onDrag = { change, amount ->
                                    change.consume()
                                    updateDrag(amount)
                                },
                            )
                        }
                        .noRippleClickable(onClick = onClick),
                ) {
                    WeekEventBlock(
                        event = event,
                        palette = palette,
                        isShift = isShift,
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (draggable) {
                        DragResizeHandle(
                            modifier = Modifier.align(Alignment.TopCenter),
                            onStart = {
                                dragMode = EventDragMode.ResizeStart
                                onDragActiveChange(true)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { updateDrag(Offset(0f, it)) },
                            onCancel = ::resetDrag,
                            onEnd = ::finishDrag,
                            palette = palette,
                        )
                        DragResizeHandle(
                            modifier = Modifier.align(Alignment.BottomCenter),
                            onStart = {
                                dragMode = EventDragMode.ResizeEnd
                                onDragActiveChange(true)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { updateDrag(Offset(0f, it)) },
                            onCancel = ::resetDrag,
                            onEnd = ::finishDrag,
                            palette = palette,
                        )
                    }
                    if (dragMode != null) {
                        Text(
                            text = dragTimeLabel(previewRange, use24HourFormat),
                            color = palette.primaryText,
                            fontFamily = mono,
                            fontSize = 10.sp,
                            maxLines = 1,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-28).dp)
                                .background(palette.dialogSurface, RoundedCornerShape(4.dp))
                                .border(1.dp, palette.line, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DragResizeHandle(
    modifier: Modifier,
    palette: DotCalPalette,
    onStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onCancel: () -> Unit,
    onEnd: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onStart() },
                    onDragCancel = onCancel,
                    onDragEnd = onEnd,
                    onDrag = { change, amount ->
                        change.consume()
                        onDrag(amount.y)
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 20.dp, height = 2.dp)
                .background(palette.onAccent.copy(alpha = 0.86f), RoundedCornerShape(2.dp)),
        )
    }
}

@Composable
private fun dragTimeLabel(range: EventTimeRange, use24HourFormat: Boolean): String {
    val formatter = localizedFormatter(if (use24HourFormat) "HH:mm" else "h:mm a")
    return stringResource(
        R.string.time_range,
        range.start.toLocalTime().format(formatter),
        range.end.toLocalTime().format(formatter),
    )
}

@Composable
internal fun DayView(
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    shiftEventIds: Set<String>,
    palette: DotCalPalette,
    isDayPunched: Boolean,
    punchStreak: Int,
    onThisDayMemories: List<OnThisDayMemory>,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onJumpToday: () -> Unit,
    onJumpPickerRequest: () -> Unit,
    onPunchDay: () -> Unit,
    onClearPunchDay: () -> Unit,
    onMemoryClick: (String) -> Unit,
    onMemoryDismiss: () -> Unit,
    highlightDate: LocalDate?,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onEventDrag: (EventDragChange) -> Unit,
    use24HourFormat: Boolean,
) {
    val dayAll = remember(eventsByDate, selectedDate) { eventsByDate[selectedDate].orEmpty() }
    val dayEvents = remember(dayAll) { dayAll.filter { it.isTask == 0 } }
    val allDayEvents = remember(dayEvents) { dayEvents.filter { it.isAllDay == 1 || it.isMultiDayVisible() } }
    val timedEvents = remember(dayEvents) { dayEvents.filter { it.isAllDay == 0 && !it.isMultiDayVisible() } }
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
            onJumpPickerRequest = onJumpPickerRequest,
            highlighted = selectedDate == highlightDate,
        )
        PunchCardStrip(
            isPunched = isDayPunched,
            streak = punchStreak,
            palette = palette,
            onPunch = onPunchDay,
            onClear = onClearPunchDay,
        )
        if (onThisDayMemories.isNotEmpty()) {
            OnThisDayCard(
                memories = onThisDayMemories,
                palette = palette,
                onMemoryClick = onMemoryClick,
                onDismiss = onMemoryDismiss,
                modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            )
        }
        if (allDayEvents.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(44.dp).background(palette.calendarSurface)) {
                items(allDayEvents.size, key = { allDayEvents[it].id }) { index ->
                    val event = allDayEvents[index]
                    val isShift = event.baseEventId() in shiftEventIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(event.displayColor(palette).copy(alpha = if (event.isGhost) 0.34f else if (isShift) 0.88f else 0.75f))
                            .then(if (event.isGhost) Modifier.ghostDottedBorder(palette, 2f) else Modifier)
                            .clickable { onEventClick(event) }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ShiftMarker(isShift = isShift, color = NWhite)
                        Text(
                            event.title,
                            modifier = Modifier.weight(1f),
                            color = NWhite,
                            fontFamily = mono,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        event.visibleDayPositionLabel(selectedDate)?.let { label ->
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Day $label",
                                color = NWhite,
                                fontFamily = mono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1,
                            )
                        }
                    }
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
                        shiftEventIds = shiftEventIds,
                        palette = palette,
                        onAddAtDate = onAddAtDate,
                        onEventClick = onEventClick,
                        onEventDrag = onEventDrag,
                        use24HourFormat = use24HourFormat,
                        modifier = Modifier.weight(1f),
                    )
                }
                if (tasks.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(stringResource(R.string.calendar_tasks), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
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

private fun Modifier.ghostDottedBorder(palette: DotCalPalette, radiusDp: Float): Modifier = drawBehind {
    drawRoundRect(
        color = palette.primaryText.copy(alpha = if (palette.isDark) 0.62f else 0.46f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(radiusDp.dp.toPx(), radiusDp.dp.toPx()),
        style = Stroke(
            width = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 3.dp.toPx())),
        ),
    )
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
    onJumpPickerRequest: () -> Unit,
    highlighted: Boolean,
) {
    val haptic = LocalHapticFeedback.current
    val highlightColor by animateColorAsState(
        targetValue = if (highlighted) palette.accent.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = tween(durationMillis = 500),
        label = "jumpDayHeaderHighlight",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(palette.calendarSurface)
            .drawBehind {
                if (highlightColor.alpha > 0f) drawRect(highlightColor)
            }
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPreviousDay, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day", tint = palette.primaryText)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .pointerInput(selectedDate) {
                    detectTapGestures(
                        onTap = { onJumpToday() },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onJumpPickerRequest()
                        },
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                selectedDate.format(dayHeaderFormatter).uppercase(Locale.getDefault()),
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
private fun PunchCardStrip(
    isPunched: Boolean,
    streak: Int,
    palette: DotCalPalette,
    onPunch: () -> Unit,
    onClear: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val progress by animateFloatAsState(
        targetValue = if (isPunched) 1f else 0f,
        animationSpec = tween(durationMillis = 360),
        label = "punchCardStamp",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(palette.calendarSurface)
            .drawBehind {
                drawLine(palette.line, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            }
            .pointerInput(isPunched) {
                detectTapGestures(
                    onTap = {
                        if (!isPunched) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onPunch()
                        }
                    },
                    onLongPress = {
                        if (isPunched) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClear()
                        }
                    },
                )
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(30.dp)) {
            drawPunchDots(progress = progress, palette = palette)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = if (streak > 0) pluralStringResource(R.plurals.punch_streak_days, streak, streak) else stringResource(R.string.calendar_complete_day),
            color = if (isPunched) palette.accent else palette.secondaryText,
            fontFamily = mono,
            fontSize = 12.sp,
            fontWeight = if (isPunched) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

private fun DrawScope.drawPunchDots(
    progress: Float,
    palette: DotCalPalette,
) {
    val dotRadius = size.minDimension / 18f
    val gap = size.minDimension / 5f
    val start = (size.minDimension - gap * 4) / 2f
    val activeCount = (progress * 25).toInt()
    repeat(5) { row ->
        repeat(5) { column ->
            val index = row * 5 + column
            val center = Offset(start + column * gap, start + row * gap)
            val color = when {
                index < activeCount -> palette.accent
                progress > 0f -> palette.accent.copy(alpha = 0.20f)
                else -> palette.secondaryText.copy(alpha = 0.30f)
            }
            drawCircle(color = color, radius = dotRadius * (1f + progress * 0.35f), center = center)
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
    shiftEventIds: Set<String>,
    palette: DotCalPalette,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
    onEventDrag: (EventDragChange) -> Unit,
    use24HourFormat: Boolean,
    modifier: Modifier = Modifier,
) {
    val now = LocalTime.now()
    val showNow = selectedDate == LocalDate.now()
    BoxWithConstraints(
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
            DraggableEventRow(
                event = event,
                layout = layout,
                palette = palette,
                baseTop = dayEventTopOffset(event.startLocalTime()),
                hourHeightDp = DAY_HOUR_HEIGHT_DP,
                minimumHeightDp = 24f,
                dayColumnWidthPx = constraints.maxWidth.toFloat(),
                allowedDayDelta = 0..0,
                use24HourFormat = use24HourFormat,
                isShift = event.baseEventId() in shiftEventIds,
                onClick = { onEventClick(event) },
                onEventDrag = onEventDrag,
            )
        }
    }
}

private fun dayEventTopOffset(time: LocalTime) =
    (((time.hour * 60 + time.minute) / 60f) * DAY_HOUR_HEIGHT_DP).dp

private fun dayEventHeight(event: CalendarEvent) =
    ((event.durationMinutes() / 60f) * DAY_HOUR_HEIGHT_DP).coerceAtLeast(24f).dp

internal data class MonthDayCellMetrics(
    val width: Dp,
    val height: Dp,
    val visibleChipCount: Int,
)

internal fun monthDayCellMetrics(
    maxWidth: Dp,
    maxHeight: Dp,
    weekNumberWidth: Dp,
): MonthDayCellMetrics {
    val availableWidth = (maxWidth - weekNumberWidth).safeNonNegative()
    val dayCellWidth = (availableWidth / 7f).safeNonNegative()
    val rawHeight = if (maxHeight.isPositiveFinite()) {
        minOf(maxHeight / 6f, dayCellWidth * 1.60f)
    } else {
        dayCellWidth
    }
    val dayCellHeight = rawHeight.safeNonNegative()
    return MonthDayCellMetrics(
        width = dayCellWidth,
        height = dayCellHeight,
        visibleChipCount = if (dayCellHeight < 76.dp || weekNumberWidth > 0.dp) 2 else 3,
    )
}

private fun Dp.isPositiveFinite(): Boolean = value.isFinite() && this > 0.dp

private fun Dp.safeNonNegative(): Dp {
    return if (value.isFinite() && this > 0.dp) this else 0.dp
}

internal fun safeTimedEventHeight(
    durationMinutes: Long,
    hourHeightDp: Float,
    minimumHeightDp: Float,
): Dp {
    val safeMin = minimumHeightDp.takeIf { it.isFinite() && it >= 0f } ?: 0f
    val safeHourHeight = hourHeightDp.takeIf { it.isFinite() && it > 0f } ?: return safeMin.dp
    val boundedMinutes = durationMinutes.coerceIn(15L, 24L * 60L)
    val rawHeight = (boundedMinutes / 60f) * safeHourHeight
    val boundedHeight = rawHeight
        .takeIf { it.isFinite() }
        ?.coerceIn(safeMin, 24f * safeHourHeight)
        ?: safeMin
    return boundedHeight.dp
}

@Composable
internal fun ThreeDayView(
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    palette: DotCalPalette,
    onPreviousRange: () -> Unit,
    onNextRange: () -> Unit,
    onJumpToday: () -> Unit,
    onJumpPickerRequest: () -> Unit,
    highlightDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val days = remember(selectedDate) { List(3) { selectedDate.plusDays(it.toLong()) } }
    val allDayEventsByDay = remember(eventsByDate, days) {
        days.associateWith { day ->
            eventsByDate[day].orEmpty().filter { it.isAllDay == 1 || it.isMultiDayVisible() }.distinctBy { it.id }
        }
    }
    val allDayRowCount = remember(allDayEventsByDay) {
        allDayEventsByDay.values.maxOfOrNull { it.size }?.coerceIn(1, WEEK_ALL_DAY_MAX_ROWS) ?: 0
    }
    val allDayEventSlotsByDay = remember(allDayEventsByDay, days, allDayRowCount) {
        monthEventSlotsByDate(days, allDayEventsByDay, allDayRowCount)
    }
    val rangeEvents = remember(eventsByDate, days) {
        days.flatMap { eventsByDate[it].orEmpty() }
            .filter { it.isAllDay == 0 && !it.isMultiDayVisible() }
            .distinctBy { it.id }
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
                    highlighted = day == highlightDate,
                    palette = palette,
                    onClick = { onDateSelected(day) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (allDayRowCount > 0) {
            AllDayStripRows(
                days = days,
                eventSlotsByDate = allDayEventSlotsByDay,
                leadingWidth = 0.dp,
                palette = palette,
                onEventClick = onEventClick,
            )
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
                        isShift = false,
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
    heatmapEnabled: Boolean,
    onHeatmapToggle: (Boolean) -> Unit,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onJumpToday: () -> Unit,
    onJumpPickerRequest: () -> Unit,
    onMonthSelected: (LocalDate) -> Unit,
) {
    var dragTotal by remember { mutableFloatStateOf(0f) }
    val today = LocalDate.now()
    val months = remember(selectedDate.year) { List(12) { selectedDate.withMonth(it + 1).withDayOfMonth(1) } }
    val eventDensity = remember(eventsByDate, selectedDate.year) {
        val density = mutableMapOf<LocalDate, Int>()
        eventsByDate.forEach { entry ->
            val date = entry.key
            if (date.year == selectedDate.year) {
                val count = entry.value.count { event ->
                    event.isTask == 0 && event.isAllDay == 0 && event.source != "BIRTHDAY"
                }
                if (count > 0) density[date] = count
            }
        }
        density
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
        YearHeatmapBar(
            enabled = heatmapEnabled,
            palette = palette,
            onToggle = onHeatmapToggle,
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f).fillMaxWidth().background(palette.calendarSurface),
            contentPadding = PaddingValues(start = 8.dp, top = 0.dp, end = 8.dp, bottom = 150.dp),
        ) {
            items(months) { month ->
                YearMonthCell(
                    month = month,
                    selected = month.year == selectedDate.year && month.monthValue == selectedDate.monthValue,
                    eventDensity = eventDensity,
                    heatmapEnabled = heatmapEnabled,
                    palette = palette,
                    weekStart = weekStart,
                    today = today,
                    onClick = { onMonthSelected(month) },
                )
            }
        }
    }
}

@Composable
private fun YearHeatmapBar(
    enabled: Boolean,
    palette: DotCalPalette,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(palette.calendarSurface)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.calendar_heatmap), color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                listOf(0, 1, 2, 3).forEach { intensity ->
                    Box(
                        modifier = Modifier
                            .size((5 + intensity * 2).dp)
                            .clip(CircleShape)
                            .then(
                                if (intensity == 0) {
                                    Modifier.border(1.dp, palette.secondaryText.copy(alpha = 0.45f), CircleShape)
                                } else {
                                    Modifier.background(yearHeatmapColor(intensity, palette))
                                },
                            ),
                    )
                }
                Text("0 / 1 / 2 / 3+", color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp)
            }
        }
        DotCalSwitch(
            checked = enabled,
            palette = palette,
            onCheckedChange = onToggle,
        )
    }
}

@Composable
private fun YearMonthCell(
    month: LocalDate,
    selected: Boolean,
    eventDensity: Map<LocalDate, Int>,
    heatmapEnabled: Boolean,
    palette: DotCalPalette,
    weekStart: DayOfWeek,
    today: LocalDate,
    onClick: () -> Unit,
) {
    val days = remember(month, weekStart) { monthGrid(month, weekStart) }
    val locale = currentResourceLocale()
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
            Text(month.month.getDisplayName(java.time.format.TextStyle.SHORT, locale), color = if (isCurrentMonth) palette.accent else palette.yearMonthLabel, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(month.monthValue.toString().padStart(2, '0'), color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(5.dp))
        MiniMonthGridCanvas(
            month = month,
            days = days,
            today = today,
            eventDensity = eventDensity,
            heatmapEnabled = heatmapEnabled,
            palette = palette,
            weekStart = weekStart,
            locale = locale,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

@Composable
private fun MiniMonthGridCanvas(
    month: LocalDate,
    days: List<LocalDate>,
    today: LocalDate,
    eventDensity: Map<LocalDate, Int>,
    heatmapEnabled: Boolean,
    palette: DotCalPalette,
    weekStart: DayOfWeek,
    locale: Locale,
    modifier: Modifier = Modifier,
) {
    val labels = remember(weekStart, locale) { weekDayLabels(weekStart, locale) }
    val weekdayColor = palette.yearWeekday.toArgb()
    val secondaryColor = palette.secondaryText.toArgb()
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
        val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = palette.secondaryText.copy(alpha = 0.45f).toArgb()
            style = Paint.Style.STROKE
            strokeWidth = 1.dp.toPx()
        }
        val columnWidth = size.width / 7f
        val rowHeight = size.height / 7f
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
            if (!inMonth) return@forEachIndexed
            val isToday = day == today
            val density = eventDensity[day] ?: 0
            val hasEvent = density > 0
            val isWeekdayDate = day.dayOfWeek != DayOfWeek.SATURDAY && day.dayOfWeek != DayOfWeek.SUNDAY
            if (heatmapEnabled && !isToday) {
                val centerY = rowHeight * row + rowHeight / 2f
                if (density == 0) {
                    nativeCanvas.drawCircle(x, centerY, 5.1.dp.toPx(), outlinePaint)
                } else {
                    circlePaint.color = yearHeatmapColor(density, palette).toArgb()
                    nativeCanvas.drawCircle(x, centerY, (4.8f + density.coerceAtMost(3) * 0.9f).dp.toPx(), circlePaint)
                    circlePaint.color = accentColor
                }
            }
            if (isToday) {
                nativeCanvas.drawCircle(x, rowHeight * row + rowHeight / 2f, 7.5.dp.toPx(), circlePaint)
            }
            datePaint.color = when {
                isToday -> onAccentColor
                heatmapEnabled && density >= 2 -> onAccentColor
                hasEvent -> accentColor
                else -> secondaryColor
            }
            datePaint.typeface = Typeface.create(Typeface.DEFAULT, if (isWeekdayDate) Typeface.BOLD else Typeface.NORMAL)
            nativeCanvas.drawText(day.dayOfMonth.toString(), x, y, datePaint)
        }
    }
}

private fun yearHeatmapColor(density: Int, palette: DotCalPalette): Color {
    return when (density.coerceAtMost(3)) {
        1 -> palette.accent.copy(alpha = 0.34f)
        2 -> palette.accent.copy(alpha = 0.62f)
        else -> palette.accent
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

@Composable
private fun currentResourceLocale(): Locale {
    return LocalContext.current.resources.configuration.locales[0] ?: Locale.getDefault()
}

private fun weekDayLabels(weekStart: DayOfWeek, locale: Locale): List<String> {
    return List(7) { index ->
        val day = weekStart.plus(index.toLong())
        day.getDisplayName(java.time.format.TextStyle.SHORT, locale)
            .trimEnd('.')
            .take(2)
            .ifBlank {
                day.getDisplayName(java.time.format.TextStyle.NARROW, locale)
            }
    }
}
