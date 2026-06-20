package com.dotfield.dotcal.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

enum class DotCalWidgetSize(val maxItems: Int) {
    Small(maxItems = 1),
    Medium(maxItems = 1),
    Large(maxItems = 2),
}

class SmallDotCalWidget : DotCalWidget(DotCalWidgetSize.Small, DpSize(110.dp, 110.dp))
class MediumDotCalWidget : DotCalWidget(DotCalWidgetSize.Medium, DpSize(250.dp, 110.dp))
class LargeDotCalWidget : DotCalWidget(DotCalWidgetSize.Large, DpSize(250.dp, 250.dp))

abstract class DotCalWidget(
    private val widgetSize: DotCalWidgetSize,
    private val minSize: DpSize,
) : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(minSize))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = WidgetDataRepository.create(context).load(widgetSize)
        val palette = dotCalWidgetPalette(context)
        provideContent {
            DotCalGlanceTheme {
                when (widgetSize) {
                    DotCalWidgetSize.Small -> SmallWidget(context, data, palette)
                    DotCalWidgetSize.Medium -> MediumWidget(context, data, palette)
                    DotCalWidgetSize.Large -> LargeWidget(context, data, palette)
                }
            }
        }
    }
}

class SmallDotCalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallDotCalWidget()
}

class MediumDotCalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumDotCalWidget()
}

class LargeDotCalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeDotCalWidget()
}

@Composable
private fun SmallWidget(context: Context, data: WidgetCalendarData, palette: DotCalWidgetPalette) {
    val item = data.nextEvent
    Column(
        modifier = widgetSurface(palette)
            .padding(14.dp)
            .clickable(actionStartActivity(item?.let { itemIntent(context, it) } ?: openAddEventIntent(context))),
    ) {
        CompactTopRow(item?.dayOfMonth ?: data.todayLabel, palette)
        Spacer(GlanceModifier.height(2.dp))
        if (item == null) {
            Box(
                modifier = GlanceModifier.fillMaxWidth().height(40.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                EmptyState("No Events", "Tap to create", palette)
            }
        } else {
            Text(item.dateLabel, maxLines = 1, style = secondaryStyle(palette, 9))
            Spacer(GlanceModifier.height(4.dp))
            Text(item.title, maxLines = 1, style = primaryStyle(palette, 16, FontWeight.Bold))
            Spacer(GlanceModifier.height(6.dp))
            Text(item.detailLine(), maxLines = 1, style = secondaryStyle(palette, 13))
        }
    }
}

@Composable
private fun MediumWidget(context: Context, data: WidgetCalendarData, palette: DotCalWidgetPalette) {
    val item = data.nextEvent
    Row(modifier = widgetSurface(palette).padding(start = 6.dp, top = 16.dp, end = 14.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = GlanceModifier.clickable(actionStartActivity(openCalendarMonthIntent(context)))) {
            DateCircle(item?.dayOfMonth ?: data.todayLabel, palette, 48)
        }
        Spacer(GlanceModifier.width(8.dp))
        if (item == null) {
            Box(
                modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity(openAddEventIntent(context))),
                contentAlignment = Alignment.CenterStart,
            ) {
                EmptyState("No Events", "Tap to create", palette)
            }
        } else {
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .clickable(actionStartActivity(itemIntent(context, item))),
            ) {
                Text(item.dateLabel, maxLines = 1, style = secondaryStyle(palette, 12))
                Spacer(GlanceModifier.height(10.dp))
                Text(item.title, maxLines = 2, style = primaryStyle(palette, 20, FontWeight.Bold))
                Spacer(GlanceModifier.height(6.dp))
                Text(item.detailLine(), maxLines = 1, style = secondaryStyle(palette, 13))
            }
        }
    }
}

@Composable
private fun LargeWidget(context: Context, data: WidgetCalendarData, palette: DotCalWidgetPalette) {
    Column(modifier = widgetSurface(palette).padding(10.dp)) {
        Header(context, data.todayLabel, data.header, palette)
        Spacer(GlanceModifier.height(14.dp))
        MonthCalendar(context, data, palette)
        Spacer(GlanceModifier.height(6.dp))
        if (data.events.isEmpty()) {
            Box(
                modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity(openAddEventIntent(context))),
                contentAlignment = Alignment.Center,
            ) {
                LargeEmptyState(palette)
            }
        } else {
            Text("TODAY", maxLines = 1, style = secondaryStyle(palette, 10))
            Spacer(GlanceModifier.height(3.dp))
            data.events.forEach { item -> AgendaRow(context, item, palette) }
            if (data.moreItemCount > 0) {
                Text(
                    "+${data.moreItemCount} more",
                    modifier = GlanceModifier.clickable(actionStartActivity(openCalendarMonthIntent(context))),
                    style = TextStyle(color = palette.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                )
            }
        }
    }
}

@Composable
private fun CompactTopRow(day: String, palette: DotCalWidgetPalette) {
    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        DateCircle(day, palette, 42)
    }
}

@Composable
private fun Header(context: Context, day: String, date: String, palette: DotCalWidgetPalette) {
    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Top, horizontalAlignment = Alignment.Start) {
        Box(modifier = GlanceModifier.clickable(actionStartActivity(openCalendarMonthIntent(context)))) {
            DateCircle(day, palette, 54)
        }
        Spacer(GlanceModifier.width(12.dp))
        Column {
            Text("DotCal", style = primaryStyle(palette, 18, FontWeight.Bold))
            Text(date, style = secondaryStyle(palette, 12))
        }
    }
}

@Composable
private fun DateCircle(day: String, palette: DotCalWidgetPalette, size: Int) {
    Box(
        modifier = GlanceModifier.size(size.dp).background(palette.accent).cornerRadius((size / 2).dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(day, style = TextStyle(color = ColorProvider(Color.White), fontSize = (size / 2).sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
private fun AgendaRow(context: Context, item: WidgetEventItem, palette: DotCalWidgetPalette) {
    Column(
        modifier = GlanceModifier.fillMaxWidth().padding(bottom = 5.dp).clickable(actionStartActivity(itemIntent(context, item))),
    ) {
        Text(item.timeLabel, maxLines = 1, style = primaryStyle(palette, 11, FontWeight.Normal))
        Spacer(GlanceModifier.height(1.dp))
        Text(item.title, maxLines = 1, style = primaryStyle(palette, 12, FontWeight.Bold))
        if (item.location.isNotBlank()) {
            Spacer(GlanceModifier.height(1.dp))
            Text(item.location, maxLines = 1, style = secondaryStyle(palette, 10))
        }
    }
}

@Composable
private fun MonthCalendar(context: Context, data: WidgetCalendarData, palette: DotCalWidgetPalette) {
    Column(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(data.monthLabel, style = TextStyle(color = palette.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold))
        Spacer(GlanceModifier.height(5.dp))
        MonthGrid(context, data.days, palette)
    }
}

@Composable
private fun MonthGrid(context: Context, days: List<WidgetCalendarDay>, palette: DotCalWidgetPalette) {
    Row(GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach {
            Box(GlanceModifier.width(CalendarCellWidth).height(16.dp), contentAlignment = Alignment.Center) {
                Text(it, style = secondaryStyle(palette, 10))
            }
        }
    }
    days.chunked(7).take(6).forEach { week ->
        Row(GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            week.forEach { day ->
                Box(
                    modifier = GlanceModifier
                        .width(CalendarCellWidth)
                        .height(25.dp)
                        .clickable(actionStartActivity(openCalendarDateIntent(context, day.dateIso))),
                    contentAlignment = Alignment.Center,
                ) {
                    CalendarDayCell(day, palette)
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(day: WidgetCalendarDay, palette: DotCalWidgetPalette) {
    val dayNumber = day.dayOfMonth ?: return
    if (day.isToday) {
        DateCircle(dayNumber.toString(), palette, 22)
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                dayNumber.toString(),
                style = TextStyle(color = palette.primary, fontSize = 13.sp, fontWeight = FontWeight.Normal),
            )
            Text(if (day.hasEvents) "•" else "", style = TextStyle(color = palette.accent, fontSize = 8.sp, fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String, palette: DotCalWidgetPalette) {
    Column {
        Text(title, style = primaryStyle(palette, 18, FontWeight.Bold))
        Spacer(GlanceModifier.height(6.dp))
        Text(subtitle, style = secondaryStyle(palette, 13))
    }
}

@Composable
private fun LargeEmptyState(palette: DotCalWidgetPalette) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("No events today", style = primaryStyle(palette, 13, FontWeight.Bold))
        Spacer(GlanceModifier.height(3.dp))
        Text("Tap to create", style = secondaryStyle(palette, 12))
    }
}

private fun widgetSurface(palette: DotCalWidgetPalette): GlanceModifier {
    return GlanceModifier
        .fillMaxSize()
        .background(palette.background)
        .cornerRadius(24.dp)
}

private fun primaryStyle(palette: DotCalWidgetPalette, size: Int, weight: FontWeight): TextStyle {
    return TextStyle(color = palette.primary, fontSize = size.sp, fontWeight = weight)
}

private fun secondaryStyle(palette: DotCalWidgetPalette, size: Int): TextStyle {
    return TextStyle(color = palette.secondary, fontSize = size.sp)
}

private fun itemIntent(context: Context, item: WidgetEventItem): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse("dotcal://event/${item.id}")).setPackage(context.packageName)
}

private fun openCalendarMonthIntent(context: Context): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse("dotcal://calendar/month")).setPackage(context.packageName)
}

private fun openCalendarDateIntent(context: Context, dateIso: String?): Intent {
    val uri = if (dateIso == null) "dotcal://calendar/month" else "dotcal://calendar/month?date=$dateIso"
    return Intent(Intent.ACTION_VIEW, Uri.parse(uri)).setPackage(context.packageName)
}

private val CalendarCellWidth = 35.dp

private fun openAddEventIntent(context: Context): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse("dotcal://event/new")).setPackage(context.packageName)
}

private fun WidgetEventItem.detailLine(): String {
    return if (location.isBlank()) timeLabel else "$timeLabel • $location"
}
