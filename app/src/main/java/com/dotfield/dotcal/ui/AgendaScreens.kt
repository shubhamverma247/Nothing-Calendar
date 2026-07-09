package com.dotfield.dotcal.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotfield.dotcal.data.CalendarEvent
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val sheetDateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM", Locale.US)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventListSheet(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (CalendarEvent) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        contentColor = palette.primaryText,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(palette.dialogSurface).padding(horizontal = 20.dp).padding(bottom = 12.dp)) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(selectedDate.format(sheetDateFormatter), fontFamily = mono, fontSize = 16.sp, color = palette.primaryText)
            Spacer(modifier = Modifier.height(16.dp))
            if (events.isEmpty()) {
                Text("No events", modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp), fontFamily = mono, fontSize = 14.sp, color = palette.dimText, textAlign = TextAlign.Center)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    lazyItems(events, key = { it.id }) { event ->
                        EventRow(event = event, palette = palette, onClick = { onEdit(event) }, modifier = Modifier.animateItem())
                    }
                }
            }
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
            ) { Text("+ Add Event", fontFamily = mono) }
        }
    }
}

@Composable
internal fun EventRow(event: CalendarEvent, palette: DotCalPalette, onClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.eventCardSurface)
            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(16.dp))
            .noRippleClickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(event.timeRange(), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(event.title, color = palette.primaryText, fontFamily = mono, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (event.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = palette.secondaryText,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        event.location,
                        color = palette.secondaryText,
                        fontFamily = mono,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (onClick != null) {
            Spacer(modifier = Modifier.width(12.dp))
            EventCardChevron(tint = palette.eventCardChevron)
        }
    }
}

@Composable
internal fun EventCardChevron(tint: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val startX = 9.dp.toPx()
        val midX = 15.dp.toPx()
        val topY = 7.dp.toPx()
        val midY = 12.dp.toPx()
        val bottomY = 17.dp.toPx()
        drawLine(tint, Offset(startX, topY), Offset(midX, midY), strokeWidth = strokeWidth)
        drawLine(tint, Offset(midX, midY), Offset(startX, bottomY), strokeWidth = strokeWidth)
    }
}

@Composable
internal fun AgendaPreview(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onAdd: () -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val agendaStartDate = LocalDate.now()
    val upcomingEvents = remember(events, agendaStartDate) {
        events
            .filter { it.isTask == 0 && !it.localDate().isBefore(agendaStartDate) }
            .sortedBy { it.startTimeMs }
    }
    val eventsByDate = remember(upcomingEvents) { upcomingEvents.groupBy { it.localDate() } }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.calendarSurface),
        contentPadding = PaddingValues(start = 13.dp, end = 13.dp, top = 0.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (upcomingEvents.isEmpty()) {
            item {
                AgendaEndOfDayState(
                    palette = palette,
                    modifier = Modifier.fillParentMaxHeight(0.82f),
                    onAdd = onAdd,
                )
            }
        } else {
            eventsByDate.forEach { (date, dateEvents) ->
                item(key = "agenda-header-$date") {
                    AgendaDateHeader(date = date, isFirst = date == agendaStartDate, palette = palette)
                }
                lazyItems(dateEvents, key = { it.id }) { event ->
                    AgendaEventCard(event = event, palette = palette, onClick = { onEventClick(event) }, modifier = Modifier.animateItem())
                }
            }
            item {
                AgendaEndOfDayState(
                    palette = palette,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    onAdd = onAdd,
                )
            }
        }
    }
}

@Composable
private fun AgendaEventCard(
    event: CalendarEvent,
    palette: DotCalPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardBg = if (palette.isDark) palette.dialogSurface else palette.eventCardSurface
    val accentStrip = event.displayColor(palette)
    val cardShape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .drawBehind {
                drawRect(cardBg)
                drawRect(accentStrip, topLeft = Offset.Zero, size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height))
            }
            .border(1.dp, palette.eventCardBorder, cardShape)
            .noRippleClickable(onClick = onClick)
            .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                event.agendaTimeRange(),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
                letterSpacing = 0.2.sp,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                event.title,
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (event.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(palette.secondaryText),
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        event.location,
                        color = palette.secondaryText,
                        fontFamily = mono,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AgendaDateHeader(date: LocalDate, isFirst: Boolean, palette: DotCalPalette) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isFirst) 0.dp else 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            date.dayOfMonth.toString().padStart(2, '0'),
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 26.sp,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                date.dayOfWeek.name.take(3),
                color = palette.accent,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
            Text(
                date.month.name.take(3),
                color = palette.secondaryText,
                fontFamily = mono,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(palette.line),
        )
    }
}

@Composable
private fun AgendaEndOfDayState(
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
    onAdd: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AgendaCalendarOutlineIcon(tint = palette.dimText)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            "You're all caught up",
            color = palette.dimText,
            fontFamily = mono,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            "+ Add Event",
            color = palette.accent,
            fontFamily = mono,
            fontSize = 16.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAdd)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun AgendaCalendarOutlineIcon(tint: Color) {
    Canvas(modifier = Modifier.size(38.dp)) {
        val stroke = 1.6.dp.toPx()
        val radius = 5.dp.toPx()
        val left = 3.dp.toPx()
        val top = 6.dp.toPx()
        val right = size.width - 3.dp.toPx()
        val bottom = size.height - 3.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            style = Stroke(width = stroke),
        )
        drawLine(tint, Offset(left, 15.dp.toPx()), Offset(right, 15.dp.toPx()), strokeWidth = stroke)
        drawLine(tint, Offset(12.dp.toPx(), 3.dp.toPx()), Offset(12.dp.toPx(), 9.dp.toPx()), strokeWidth = stroke)
        drawLine(tint, Offset(26.dp.toPx(), 3.dp.toPx()), Offset(26.dp.toPx(), 9.dp.toPx()), strokeWidth = stroke)
        val dotRadius = 1.4.dp.toPx()
        listOf(12.dp, 19.dp, 26.dp).forEach { x ->
            drawCircle(tint, radius = dotRadius, center = Offset(x.toPx(), 23.dp.toPx()))
        }
    }
}

private fun CalendarEvent.timeRange(): String {
    if (isAllDay == 1) return "All-day"
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
    val end = Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
    return "${start.format(timeFormatter)} - ${end.format(timeFormatter)}"
}

private fun CalendarEvent.agendaTimeRange(): String {
    if (isAllDay == 1) return "All-day"
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
    val end = Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
    return "${start.format(timeFormatter)} - ${end.format(timeFormatter)}"
}
