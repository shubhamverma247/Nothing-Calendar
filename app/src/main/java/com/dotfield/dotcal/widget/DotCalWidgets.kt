package com.dotfield.dotcal.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.layout.ContentScale
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
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dotfield.dotcal.R
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class DotCalWidgetSize(val maxItems: Int) {
    Small(maxItems = 1),
    Medium(maxItems = 1),
    Large(maxItems = 2),
}

class SmallDotCalWidget : DotCalWidget(DotCalWidgetSize.Small, DpSize(110.dp, 110.dp))
class MediumDotCalWidget : DotCalWidget(DotCalWidgetSize.Medium, DpSize(250.dp, 140.dp))
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
    val today = LocalDate.now()
    WidgetSurfaceBox(palette) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(start = 14.dp, top = 14.dp, end = 10.dp, bottom = 14.dp)
                .clickable(actionStartActivity(item?.let { itemIntent(context, it) } ?: openAddEventIntent(context))),
        ) {
            Box(modifier = GlanceModifier.fillMaxWidth().height(18.dp), contentAlignment = Alignment.CenterStart) {
                Text(
                    today.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault())).uppercase(Locale.getDefault()),
                    maxLines = 1,
                    modifier = GlanceModifier.width(88.dp),
                    style = monoStyle(palette.primary, 12, FontWeight.Bold),
                )
                Box(modifier = GlanceModifier.fillMaxWidth().padding(end = 8.dp), contentAlignment = Alignment.CenterEnd) {
                    StatusDot(item != null, palette)
                }
            }
            Spacer(GlanceModifier.height(4.dp))
            Text(
                today.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())).uppercase(Locale.getDefault()),
                maxLines = 1,
                style = monoStyle(palette.secondary, 11, FontWeight.Normal),
            )
            Spacer(GlanceModifier.height(29.dp))
            SmallHorizontalDivider()
            Spacer(GlanceModifier.height(8.dp))
            if (item == null) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth().height(38.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CompactAddPrompt("ADD EVENT", 22, palette)
                }
            } else {
                Text(item.title, maxLines = 1, style = primaryStyle(palette, 16, FontWeight.Bold))
                Spacer(GlanceModifier.height(3.dp))
                Text(item.detailLine(), maxLines = 1, style = monoStyle(palette.secondary, 9, FontWeight.Normal))
            }
        }
    }
}

@Composable
private fun MediumWidget(context: Context, data: WidgetCalendarData, palette: DotCalWidgetPalette) {
    val item = data.nextEvent
    WidgetSurfaceBox(palette) {
        Row(
            modifier = GlanceModifier.fillMaxSize().padding(start = 18.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = GlanceModifier.width(54.dp).clickable(actionStartActivity(openCalendarMonthIntent(context))),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RingBadge(item?.dayOfMonth ?: data.todayLabel, 46, palette, textSize = 20, textColor = palette.primary)
                Spacer(GlanceModifier.height(7.dp))
                Text(todayDayAbbrev(), maxLines = 1, style = monoStyle(palette.secondary, 11, FontWeight.Bold))
            }
            Spacer(GlanceModifier.width(16.dp))
            MediumVerticalDivider()
            Spacer(GlanceModifier.width(16.dp))
            if (item == null) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity(openAddEventIntent(context))),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("NO EVENTS - TAP TO ADD", maxLines = 1, style = monoStyle(palette.secondary, 12, FontWeight.Bold))
                }
            } else {
                Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = GlanceModifier
                            .width(180.dp)
                            .clickable(actionStartActivity(itemIntent(context, item))),
                    ) {
                        Text(item.title, maxLines = 2, style = primaryStyle(palette, 20, FontWeight.Bold))
                        Spacer(GlanceModifier.height(6.dp))
                        Text(item.detailLine(), maxLines = 1, style = monoStyle(palette.secondary, 12, FontWeight.Normal))
                    }
                    if (data.moreItemCount > 0) {
                        Spacer(GlanceModifier.width(4.dp))
                        Column(modifier = GlanceModifier.height(132.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            OutlinePill("+${data.moreItemCount}", palette, verticalPadding = 1)
                            Spacer(GlanceModifier.height(92.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LargeWidget(context: Context, data: WidgetCalendarData, palette: DotCalWidgetPalette) {
    val eventCount = data.events.size + data.moreItemCount
    WidgetSurfaceBox(palette) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(start = 18.dp, top = 26.dp, end = 18.dp, bottom = 18.dp),
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MonthTitle(data.monthLabel, palette)
                if (eventCount > 0) {
                    Text(
                        "$eventCount EVENTS",
                        maxLines = 1,
                        modifier = GlanceModifier.width(96.dp),
                        style = monoStyle(palette.accent, 14, FontWeight.Bold, TextAlign.End),
                    )
                }
            }
            Spacer(GlanceModifier.height(17.dp))
            MonthCalendar(context, data, palette)
            Spacer(GlanceModifier.height(15.dp))
            if (data.events.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth().height(46.dp).clickable(actionStartActivity(openAddEventIntent(context))),
                    contentAlignment = Alignment.Center,
                ) {
                    CompactAddPrompt("NO EVENTS - TAP TO ADD", 26, palette)
                }
            } else {
                data.events.forEach { item -> AgendaRow(context, item, palette) }
                if (data.moreItemCount > 0) {
                    Text(
                        "+${data.moreItemCount} MORE",
                        modifier = GlanceModifier
                            .padding(start = 78.dp)
                            .clickable(actionStartActivity(openCalendarMonthIntent(context))),
                        style = monoStyle(palette.secondary, 12, FontWeight.Bold),
                    )
                }
            }
        }
    }
}

@Composable
private fun MediumVerticalDivider() {
    Image(
        provider = ImageProvider(R.drawable.widget_medium_vertical_divider),
        contentDescription = null,
        modifier = GlanceModifier.width(1.dp).height(108.dp),
        contentScale = ContentScale.FillBounds,
    )
}

@Composable
private fun SmallHorizontalDivider() {
    Image(
        provider = ImageProvider(R.drawable.widget_small_horizontal_divider),
        contentDescription = null,
        modifier = GlanceModifier.fillMaxWidth().height(1.dp),
        contentScale = ContentScale.FillBounds,
    )
}

@Composable
private fun MonthTitle(monthLabel: String, palette: DotCalWidgetPalette) {
    val parts = monthLabel.uppercase(Locale.getDefault()).split(" ")
    Row(modifier = GlanceModifier.width(184.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(parts.firstOrNull().orEmpty(), maxLines = 1, style = monoStyle(palette.primary, 14, FontWeight.Bold))
        Spacer(GlanceModifier.width(10.dp))
        Text(parts.getOrNull(1).orEmpty(), maxLines = 1, style = monoStyle(palette.secondary, 14, FontWeight.Normal))
    }
}

@Composable
private fun RingBadge(
    value: String,
    size: Int,
    palette: DotCalWidgetPalette,
    textSize: Int = size / 2,
    textColor: ColorProvider = palette.accent,
) {
    Box(
        modifier = GlanceModifier.size(size.dp).background(palette.accent).cornerRadius((size / 2).dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = GlanceModifier.size((size - 4).dp).background(palette.background).cornerRadius(((size - 4) / 2).dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(value, maxLines = 1, style = monoStyle(textColor, textSize, FontWeight.Bold))
        }
    }
}

@Composable
private fun AgendaRow(context: Context, item: WidgetEventItem, palette: DotCalWidgetPalette) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(start = 12.dp, bottom = 5.dp)
            .clickable(actionStartActivity(itemIntent(context, item))),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(item.timeLabel, maxLines = 1, modifier = GlanceModifier.width(50.dp), style = monoStyle(palette.accent, 10, FontWeight.Bold))
        Spacer(GlanceModifier.width(6.dp))
        Column(modifier = GlanceModifier.width(168.dp)) {
            Text(item.title, maxLines = 1, style = primaryStyle(palette, 16, FontWeight.Bold))
            if (item.location.isNotBlank()) {
                Spacer(GlanceModifier.height(1.dp))
                Text(item.location.uppercase(Locale.getDefault()), maxLines = 1, style = monoStyle(palette.secondary, 10, FontWeight.Normal))
            }
        }
    }
}

@Composable
private fun MonthCalendar(context: Context, data: WidgetCalendarData, palette: DotCalWidgetPalette) {
    Column(modifier = GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        MonthGrid(context, data.days, palette)
    }
}

@Composable
private fun MonthGrid(context: Context, days: List<WidgetCalendarDay>, palette: DotCalWidgetPalette) {
    Row(GlanceModifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        listOf("S", "M", "T", "W", "T", "F", "S").forEach {
            Box(GlanceModifier.width(CalendarCellWidth).height(16.dp), contentAlignment = Alignment.Center) {
                Text(it, style = monoStyle(palette.secondary, 10, FontWeight.Bold))
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
        RingBadge(dayNumber.toString(), 22, palette, textSize = 11)
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(dayNumber.toString(), style = monoStyle(palette.primary, 12, FontWeight.Normal))
            if (day.hasEvents) {
                StatusDot(true, palette, 4)
            } else {
                Spacer(GlanceModifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun StatusDot(visible: Boolean, palette: DotCalWidgetPalette, size: Int = 5) {
    if (visible) {
        Box(GlanceModifier.size(size.dp).background(palette.accent).cornerRadius((size / 2).dp)) {}
    }
}

@Composable
private fun CompactAddPrompt(label: String, ringSize: Int, palette: DotCalWidgetPalette) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        RingBadge("+", ringSize, palette, textSize = ringSize / 2)
        Spacer(GlanceModifier.height(5.dp))
        Text(label, maxLines = 1, style = monoStyle(palette.secondary, 10, FontWeight.Bold, TextAlign.Center))
    }
}

@Composable
private fun DashedDivider(vertical: Boolean = false, length: Int, palette: DotCalWidgetPalette) {
    DashedDivider(
        vertical = vertical,
        length = length,
        palette = palette,
        color = palette.border,
        dashLength = if (vertical) 4 else 1,
        dashGap = if (vertical) 4 else 3,
    )
}

@Composable
private fun DashedDivider(
    vertical: Boolean,
    length: Int,
    palette: DotCalWidgetPalette,
    color: ColorProvider,
    dashLength: Int,
    dashGap: Int,
) {
    val dashCount = if (vertical) {
        ((length + dashGap) / (dashLength + dashGap)).coerceAtLeast(1)
    } else {
        (length / 5).coerceAtLeast(1)
    }
    if (vertical) {
        val paintedLength = (dashCount * dashLength) + ((dashCount - 1) * dashGap)
        Box(
            modifier = GlanceModifier.width(2.dp).height(length.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(modifier = GlanceModifier.width(2.dp).height(paintedLength.dp)) {
                repeat(dashCount) { index ->
                    Box(GlanceModifier.width(2.dp).height(dashLength.dp).background(color)) {}
                    if (index < dashCount - 1) {
                        Spacer(GlanceModifier.height(dashGap.dp))
                    }
                }
            }
        }
    } else {
        Row(modifier = GlanceModifier.fillMaxWidth().height(1.dp)) {
            repeat(dashCount) {
                Box(GlanceModifier.width(3.dp).height(1.dp).background(palette.border)) {}
                Spacer(GlanceModifier.width(3.dp))
            }
        }
    }
}

@Composable
private fun OutlinePill(text: String, palette: DotCalWidgetPalette, verticalPadding: Int = 2) {
    Box(
        modifier = GlanceModifier.background(palette.accent).cornerRadius(10.dp).padding(1.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = GlanceModifier.background(palette.background).cornerRadius(9.dp).padding(horizontal = 6.dp, vertical = verticalPadding.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text, maxLines = 1, style = monoStyle(palette.accent, 10, FontWeight.Bold, TextAlign.Center))
        }
    }
}

@Composable
private fun WidgetSurfaceBox(palette: DotCalWidgetPalette, content: @Composable () -> Unit) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(palette.background)
            .cornerRadius(24.dp),
    ) {
        content()
    }
}

private fun primaryStyle(palette: DotCalWidgetPalette, size: Int, weight: FontWeight): TextStyle {
    return TextStyle(color = palette.primary, fontSize = size.sp, fontWeight = weight)
}

private fun monoStyle(
    color: ColorProvider,
    size: Int,
    weight: FontWeight,
    textAlign: TextAlign? = null,
): TextStyle {
    return TextStyle(color = color, fontSize = size.sp, fontWeight = weight, fontFamily = FontFamily.Monospace, textAlign = textAlign)
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

private fun todayDayAbbrev(): String {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault())).uppercase(Locale.getDefault())
}

private fun openAddEventIntent(context: Context): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse("dotcal://event/new")).setPackage(context.packageName)
}

private fun WidgetEventItem.detailLine(): String {
    return if (location.isBlank()) timeLabel else "$timeLabel • ${location.uppercase(Locale.getDefault())}"
}
