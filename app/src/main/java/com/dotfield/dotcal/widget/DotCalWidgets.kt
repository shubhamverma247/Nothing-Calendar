package com.dotfield.dotcal.widget

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.GlanceId
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.layout.ContentScale
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
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
import com.dotfield.dotcal.prefs.CalendarPreferences
import java.time.Duration
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class DotCalWidgetSize(val maxItems: Int) {
    DateOnly(maxItems = 0),
    MonthCompact(maxItems = 0),
    Small(maxItems = 1),
    ShiftWide(maxItems = 2),
    Countdown(maxItems = 1),
    Medium(maxItems = 3),
    Large(maxItems = 4),
}

class DateOnlyDotCalWidget : DotCalWidget(LegacyWidgetKind.DateOnly, DotCalWidgetSize.DateOnly, DpSize(56.dp, 56.dp))
class CompactMonthDotCalWidget : DotCalWidget(
    LegacyWidgetKind.MonthCompact,
    DotCalWidgetSize.MonthCompact,
    DpSize(110.dp, 110.dp),
    setOf(DpSize(110.dp, 110.dp), DpSize(140.dp, 140.dp), DpSize(180.dp, 180.dp), DpSize(220.dp, 220.dp)),
)
class SmallDotCalWidget : DotCalWidget(LegacyWidgetKind.Small, DotCalWidgetSize.Small, DpSize(110.dp, 110.dp))
class ShiftWideDotCalWidget : DotCalWidget(
    LegacyWidgetKind.ShiftWide,
    DotCalWidgetSize.ShiftWide,
    DpSize(250.dp, 56.dp),
    setOf(DpSize(110.dp, 110.dp), DpSize(250.dp, 56.dp), DpSize(250.dp, 140.dp)),
)
class MediumDotCalWidget : DotCalWidget(LegacyWidgetKind.Medium, DotCalWidgetSize.Medium, DpSize(250.dp, 140.dp))
class LargeDotCalWidget : DotCalWidget(LegacyWidgetKind.Large, DotCalWidgetSize.Large, DpSize(250.dp, 250.dp))
class EventCountdownDotCalWidget : DotCalWidget(LegacyWidgetKind.Countdown, DotCalWidgetSize.Countdown, DpSize(110.dp, 110.dp))
class AgendaListDotCalWidget : DotCalWidget(LegacyWidgetKind.Agenda, DotCalWidgetSize.Medium, DpSize(250.dp, 140.dp))
class MonthGridDotCalWidget : DotCalWidget(LegacyWidgetKind.MonthGrid, DotCalWidgetSize.Large, DpSize(250.dp, 250.dp))

abstract class DotCalWidget(
    private val legacyKind: LegacyWidgetKind,
    private val widgetSize: DotCalWidgetSize,
    private val minSize: DpSize,
    private val supportedSizes: Set<DpSize> = setOf(minSize),
) : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(supportedSizes)
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settings = syncDotCalWidgetState(context, id, legacyKind)
        val config = settings.instanceConfig
        val data = WidgetDataRepository.create(context).load(
            config = config,
            defaultMaxItems = widgetSize.maxItems,
            monthOffset = if (widgetSize == DotCalWidgetSize.MonthCompact) settings.monthOffset else 0,
        )
        provideContent {
            val palette = dotCalWidgetPalette(context, currentDotCalWidgetSettings())
            DotCalGlanceTheme {
                ConfiguredWidget(context, config, widgetSize, data, palette)
            }
        }
    }
}

private fun compactMonthDays(displayMonthDate: LocalDate, today: LocalDate): List<WidgetCalendarDay> {
    val month = YearMonth.from(displayMonthDate)
    val monthStart = month.atDay(1)
    val leadingBlanks = monthStart.dayOfWeek.value % 7
    val days = MutableList(leadingBlanks) { WidgetCalendarDay(dayOfMonth = null) }
    days += (1..month.lengthOfMonth()).map { day ->
        val date = month.atDay(day)
        WidgetCalendarDay(dayOfMonth = day, dateIso = date.toString(), isToday = date == today)
    }
    while (days.size % 7 != 0) days += WidgetCalendarDay(dayOfMonth = null)
    return days
}

@Composable
private fun ConfiguredWidget(
    context: Context,
    config: WidgetInstanceConfig,
    widgetSize: DotCalWidgetSize,
    data: WidgetCalendarData,
    palette: DotCalWidgetPalette,
) {
    if (widgetSize == DotCalWidgetSize.DateOnly) {
        DateOnlyWidget(context, data, palette)
        return
    }
    if (widgetSize == DotCalWidgetSize.MonthCompact) {
        CompactMonthWidget(context, palette)
        return
    }
    when (config.category) {
        WidgetCategory.Calendar -> if (widgetSize == DotCalWidgetSize.Small) {
            TodayWidget(context, config, data, palette, widgetSize)
        } else {
            LargeWidget(context, config, data, palette)
        }
        WidgetCategory.Schedule -> when (config.viewType) {
            WidgetViewType.NextEvent -> SmallWidget(context, config, data, palette)
            else -> when (widgetSize) {
                DotCalWidgetSize.Small -> SmallWidget(context, config, data, palette)
                DotCalWidgetSize.Large -> LargeAgendaWidget(context, config, data, palette)
                else -> MediumWidget(context, config, data, palette)
            }
        }
        WidgetCategory.Today -> TodayWidget(context, config, data, palette, widgetSize)
        WidgetCategory.Tasks -> TasksWidget(context, config, data, palette, widgetSize)
        WidgetCategory.Countdown -> CountdownWidget(context, data, palette)
        WidgetCategory.Shift -> ShiftWideWidget(context, config, data, palette)
        WidgetCategory.QuickActions -> QuickActionWidget(context, config, palette)
    }
}

class DateOnlyDotCalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DateOnlyDotCalWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            WidgetUpdateWorker.enqueue(context)
        }
    }
}

class CompactMonthDotCalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CompactMonthDotCalWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            WidgetUpdateWorker.enqueue(context)
        }
    }
}

class SmallDotCalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallDotCalWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            WidgetUpdateWorker.enqueue(context)
        }
    }
}

class MediumDotCalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumDotCalWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            WidgetUpdateWorker.enqueue(context)
        }
    }
}

class LargeDotCalWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = LargeDotCalWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            WidgetUpdateWorker.enqueue(context)
        }
    }
}

class ShiftWideWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ShiftWideDotCalWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            WidgetUpdateWorker.enqueue(context)
        }
    }
}

@Composable
private fun DateOnlyWidget(context: Context, data: WidgetCalendarData, palette: DotCalWidgetPalette) {
    WidgetSurfaceBox(palette) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(6.dp)
                .clickable(actionStartActivity(openCalendarTodayIntent(context))),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(data.todayLabel, maxLines = 1, style = monoStyle(palette.accent, 28, FontWeight.Bold, TextAlign.Center))
                Spacer(GlanceModifier.height(2.dp))
                Text(todayDayAbbrev(), maxLines = 1, style = monoStyle(palette.secondary, 9, FontWeight.Bold, TextAlign.Center))
            }
        }
    }
}

@Composable
private fun CompactMonthWidget(context: Context, palette: DotCalWidgetPalette) {
    val metrics = compactMonthMetrics(LocalSize.current)
    val settings = currentDotCalWidgetSettings()
    val monthDate = LocalDate.now().plusMonths(settings.monthOffset.toLong())
    val monthLabel = monthDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))
    val days = compactMonthDays(monthDate, LocalDate.now())
    WidgetSurfaceBox(palette) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(
                    start = metrics.horizontalPadding.dp,
                    top = metrics.topPadding.dp,
                    end = metrics.horizontalPadding.dp,
                    bottom = metrics.bottomPadding.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CompactMonthHeader(monthLabel, palette, metrics)
            Spacer(GlanceModifier.height(metrics.headerBottomGap.dp))
            CompactMonthGrid(context, days, palette, metrics)
        }
    }
}

@Composable
private fun SmallWidget(
    context: Context,
    config: WidgetInstanceConfig,
    data: WidgetCalendarData,
    palette: DotCalWidgetPalette,
) {
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
                Text(countdownLabel(item), maxLines = 1, style = monoStyle(palette.accent, 18, FontWeight.Bold))
                Spacer(GlanceModifier.height(2.dp))
                Text(item.displayTitle(config), maxLines = 1, style = primaryStyle(palette, 14, FontWeight.Bold))
                if (config.content.showTime || config.content.showLocation) {
                    Spacer(GlanceModifier.height(3.dp))
                    Text(item.detailLine(config), maxLines = 1, style = monoStyle(palette.secondary, 9, FontWeight.Normal))
                }
            }
        }
    }
}

@Composable
private fun ShiftWideWidget(
    context: Context,
    config: WidgetInstanceConfig,
    data: WidgetCalendarData,
    palette: DotCalWidgetPalette,
) {
    val size = LocalSize.current
    WidgetSurfaceBox(palette) {
        when {
            size.height >= 100.dp && size.width < 180.dp -> ShiftSquareLayout(context, config, data, palette)
            size.height >= 100.dp -> ShiftMediumLayout(context, config, data, palette)
            else -> ShiftWideLayout(context, config, data, palette)
        }
    }
}

@Composable
private fun ShiftWideLayout(
    context: Context,
    config: WidgetInstanceConfig,
    data: WidgetCalendarData,
    palette: DotCalWidgetPalette,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(start = 16.dp, end = 12.dp)
            .clickable(actionStartActivity(openCalendarAgendaIntent(context))),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShiftCountBlock(data, palette, width = 58)
        Spacer(GlanceModifier.width(12.dp))
        DashedDivider(vertical = true, length = 40, palette = palette)
        Spacer(GlanceModifier.width(12.dp))
        ShiftEventList(config, data, palette, maxRows = 2, titleSize = 13)
    }
}

@Composable
private fun ShiftSquareLayout(
    context: Context,
    config: WidgetInstanceConfig,
    data: WidgetCalendarData,
    palette: DotCalWidgetPalette,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(start = 12.dp, top = 12.dp, end = 10.dp, bottom = 10.dp)
            .clickable(actionStartActivity(openCalendarAgendaIntent(context))),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("SHIFT", maxLines = 1, modifier = GlanceModifier.width(56.dp), style = monoStyle(palette.secondary, 10, FontWeight.Bold))
            Text(
                (data.events.size + data.moreItemCount).toString(),
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth(),
                style = monoStyle(palette.accent, 20, FontWeight.Bold, TextAlign.End),
            )
        }
        Spacer(GlanceModifier.height(8.dp))
        DashedDivider(length = 1, palette = palette)
        Spacer(GlanceModifier.height(8.dp))
        ShiftEventList(config, data, palette, maxRows = 2, titleSize = 12)
    }
}

@Composable
private fun ShiftMediumLayout(
    context: Context,
    config: WidgetInstanceConfig,
    data: WidgetCalendarData,
    palette: DotCalWidgetPalette,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(start = 18.dp, top = 16.dp, end = 16.dp, bottom = 14.dp)
            .clickable(actionStartActivity(openCalendarAgendaIntent(context))),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShiftCountBlock(data, palette, width = 70)
        Spacer(GlanceModifier.width(14.dp))
        DashedDivider(vertical = true, length = 90, palette = palette)
        Spacer(GlanceModifier.width(14.dp))
        ShiftEventList(config, data, palette, maxRows = 3, titleSize = 14)
    }
}

@Composable
private fun ShiftCountBlock(data: WidgetCalendarData, palette: DotCalWidgetPalette, width: Int) {
    Column(modifier = GlanceModifier.width(width.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("SHIFT", maxLines = 1, style = monoStyle(palette.secondary, 10, FontWeight.Bold, TextAlign.Center))
        Spacer(GlanceModifier.height(4.dp))
        Text((data.events.size + data.moreItemCount).toString(), maxLines = 1, style = monoStyle(palette.accent, 24, FontWeight.Bold, TextAlign.Center))
    }
}

@Composable
private fun ShiftEventList(
    config: WidgetInstanceConfig,
    data: WidgetCalendarData,
    palette: DotCalWidgetPalette,
    maxRows: Int,
    titleSize: Int,
) {
    if (data.events.isEmpty()) {
        Text("NO UPCOMING SHIFTS", maxLines = 2, style = monoStyle(palette.secondary, 12, FontWeight.Bold))
    } else {
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            data.events.take(maxRows).forEach { item ->
                Text(item.displayTitle(config), maxLines = 1, style = primaryStyle(palette, titleSize, FontWeight.Bold))
                Text(item.dateLabel.uppercase(Locale.getDefault()) + " - " + item.timeLabel.uppercase(Locale.getDefault()), maxLines = 1, style = monoStyle(palette.secondary, 9, FontWeight.Normal))
                Spacer(GlanceModifier.height(3.dp))
            }
        }
    }
}

@Composable
private fun CountdownWidget(context: Context, data: WidgetCalendarData, palette: DotCalWidgetPalette) {
    val item = data.nextEvent
    val settings = currentDotCalWidgetSettings()
    WidgetSurfaceBox(palette) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .clickable(actionStartActivity(item?.let { itemIntent(context, it) } ?: openAddEventIntent(context))),
            contentAlignment = Alignment.Center,
        ) {
            if (item == null) {
                CompactAddPrompt("ADD EVENT", 30, palette)
            } else {
                Column(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text("D-DAY", maxLines = 1, style = monoStyle(palette.secondary, 10, FontWeight.Bold, TextAlign.Center))
                    Spacer(GlanceModifier.height(2.dp))
                    Image(
                        provider = ImageProvider(buildCountdownNumberGraphic(item.countdownDays.take(3), widgetAccentArgb(settings))),
                        contentDescription = null,
                        modifier = GlanceModifier
                            .width(84.dp)
                            .height(38.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Spacer(GlanceModifier.height(2.dp))
                    Text("DAYS UNTIL", maxLines = 1, style = monoStyle(palette.secondary, 9, FontWeight.Normal, TextAlign.Center))
                    Spacer(GlanceModifier.height(2.dp))
                    Text(item.title, maxLines = 1, style = primaryStyle(palette, 13, FontWeight.Bold))
                }
            }
        }
    }
}

@Composable
private fun TodayWidget(
    context: Context,
    config: WidgetInstanceConfig,
    data: WidgetCalendarData,
    palette: DotCalWidgetPalette,
    widgetSize: DotCalWidgetSize,
) {
    WidgetSurfaceBox(palette) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 14.dp, end = 14.dp, bottom = 14.dp)
                .clickable(actionStartActivity(openCalendarTodayIntent(context))),
        ) {
            Text(todayDayAbbrev(), maxLines = 1, style = monoStyle(palette.secondary, 12, FontWeight.Bold))
            Spacer(GlanceModifier.height(3.dp))
            Text(data.todayLabel, maxLines = 1, style = monoStyle(palette.accent, if (widgetSize == DotCalWidgetSize.Small) 34 else 42, FontWeight.Bold))
            Spacer(GlanceModifier.height(4.dp))
            Text(todaySummary(data, config), maxLines = 1, style = monoStyle(palette.primary, 12, FontWeight.Bold))
            if (widgetSize != DotCalWidgetSize.Small) {
                Spacer(GlanceModifier.height(10.dp))
                data.nextEvent?.let { item ->
                    if (config.content.showTime) {
                        Text(item.timeLabel, maxLines = 1, style = monoStyle(palette.accent, 10, FontWeight.Bold))
                    }
                    Text(item.displayTitle(config), maxLines = 1, style = primaryStyle(palette, 14, FontWeight.Bold))
                } ?: Text("NOTHING SCHEDULED", maxLines = 1, style = monoStyle(palette.secondary, 11, FontWeight.Bold))
            }
        }
    }
}

@Composable
private fun TasksWidget(
    context: Context,
    config: WidgetInstanceConfig,
    data: WidgetCalendarData,
    palette: DotCalWidgetPalette,
    widgetSize: DotCalWidgetSize,
) {
    WidgetSurfaceBox(palette) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 14.dp, end = 14.dp, bottom = 14.dp)
                .clickable(actionStartActivity(openCalendarTasksIntent(context))),
        ) {
            Text("TASKS", maxLines = 1, style = monoStyle(palette.secondary, 12, FontWeight.Bold))
            Spacer(GlanceModifier.height(6.dp))
            if (data.tasks.isEmpty()) {
                Text("ALL CAUGHT UP", maxLines = 1, style = monoStyle(palette.primary, 15, FontWeight.Bold))
                Spacer(GlanceModifier.height(4.dp))
                Text("No open tasks", maxLines = 1, style = monoStyle(palette.secondary, 11, FontWeight.Normal))
            } else if (widgetSize == DotCalWidgetSize.Small) {
                Text(data.tasks.size.toString(), maxLines = 1, style = monoStyle(palette.accent, 34, FontWeight.Bold))
                Text("OPEN", maxLines = 1, style = monoStyle(palette.secondary, 11, FontWeight.Bold))
            } else {
                data.tasks.take(3).forEach { task ->
                    Text(task.displayTitle(config), maxLines = 1, style = primaryStyle(palette, 14, FontWeight.Bold))
                    if (config.content.showTime) {
                        Text(task.dateLabel.uppercase(Locale.getDefault()), maxLines = 1, style = monoStyle(palette.secondary, 9, FontWeight.Normal))
                    }
                    Spacer(GlanceModifier.height(5.dp))
                }
            }
        }
    }
}

@Composable
private fun QuickActionWidget(
    context: Context,
    config: WidgetInstanceConfig,
    palette: DotCalWidgetPalette,
) {
    val action = config.interaction.tapAction
    WidgetSurfaceBox(palette) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp)
                .clickable(actionStartActivity(widgetTapIntent(context, action))),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                RingBadge(quickActionGlyph(action), 42, palette, textSize = 22)
                Spacer(GlanceModifier.height(8.dp))
                Text(quickActionLabel(action), maxLines = 1, style = monoStyle(palette.secondary, 10, FontWeight.Bold, TextAlign.Center))
            }
        }
    }
}

@Composable
private fun MediumWidget(
    context: Context,
    config: WidgetInstanceConfig,
    data: WidgetCalendarData,
    palette: DotCalWidgetPalette,
) {
    WidgetSurfaceBox(palette) {
        Row(
            modifier = GlanceModifier.fillMaxSize().padding(start = 18.dp, end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = GlanceModifier.width(54.dp).clickable(actionStartActivity(openCalendarMonthIntent(context))),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                RingBadge(data.todayLabel, 46, palette, textSize = 20, textColor = palette.primary)
                Spacer(GlanceModifier.height(7.dp))
                Text(todayDayAbbrev(), maxLines = 1, style = monoStyle(palette.secondary, 11, FontWeight.Bold))
            }
            Spacer(GlanceModifier.width(16.dp))
            MediumVerticalDivider()
            Spacer(GlanceModifier.width(16.dp))
            if (data.events.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity(openAddEventIntent(context))),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text("NO EVENTS - TAP TO ADD", maxLines = 1, style = monoStyle(palette.secondary, 12, FontWeight.Bold))
                }
            } else {
                Column(modifier = GlanceModifier.fillMaxWidth()) {
                    Text(config.timeRange.shortLabel(), maxLines = 1, style = monoStyle(palette.secondary, 10, FontWeight.Bold))
                    Spacer(GlanceModifier.height(6.dp))
                    data.events.take(1).forEach { item ->
                        MediumAgendaHero(context, item, config, palette)
                    }
                    data.events.drop(1).take(2).forEach { item ->
                        MediumAgendaRow(context, item, config, palette)
                    }
                    if (data.moreItemCount > 0) {
                        Text(
                            "+${data.moreItemCount} MORE",
                            modifier = GlanceModifier.padding(start = 58.dp).clickable(actionStartActivity(openCalendarAgendaIntent(context))),
                            style = monoStyle(palette.secondary, 10, FontWeight.Bold),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LargeWidget(context: Context, config: WidgetInstanceConfig, data: WidgetCalendarData, palette: DotCalWidgetPalette) {
    val eventCount = data.events.size + data.moreItemCount
    WidgetSurfaceBox(palette) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 12.dp),
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
            Spacer(GlanceModifier.height(10.dp))
            MonthCalendar(context, data, palette)
            Spacer(GlanceModifier.height(8.dp))
            if (data.events.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth().height(46.dp).clickable(actionStartActivity(openAddEventIntent(context))),
                    contentAlignment = Alignment.Center,
                ) {
                    CompactAddPrompt("NO EVENTS - TAP TO ADD", 26, palette)
                }
            } else {
                data.events.forEach { item -> AgendaRow(context, item, config, palette) }
                if (data.moreItemCount > 0) {
                    Text(
                        "+${data.moreItemCount} MORE",
                        modifier = GlanceModifier
                            .padding(start = 68.dp)
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
            modifier = GlanceModifier.size((size - 4).dp).background(palette.solidSurface).cornerRadius(((size - 4) / 2).dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(value, maxLines = 1, style = monoStyle(textColor, textSize, FontWeight.Bold))
        }
    }
}

@Composable
private fun AgendaRow(
    context: Context,
    item: WidgetEventItem,
    config: WidgetInstanceConfig,
    palette: DotCalWidgetPalette,
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(start = 12.dp, bottom = 2.dp)
            .clickable(actionStartActivity(itemIntent(context, item))),
        verticalAlignment = Alignment.Top,
    ) {
        Text(if (config.content.showTime) item.timeLabel else item.dayOfMonth, maxLines = 1, modifier = GlanceModifier.width(50.dp).padding(top = 2.dp), style = monoStyle(palette.accent, 9, FontWeight.Bold))
        Spacer(GlanceModifier.width(6.dp))
        Column(modifier = GlanceModifier.width(168.dp)) {
            Text(item.displayTitle(config), maxLines = 1, style = primaryStyle(palette, 14, FontWeight.Bold))
            if (config.content.showLocation && item.location.isNotBlank()) {
                Text(item.location.uppercase(Locale.getDefault()), maxLines = 1, style = monoStyle(palette.secondary, 9, FontWeight.Normal))
            }
        }
    }
}

class EventCountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = EventCountdownDotCalWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            WidgetUpdateWorker.enqueue(context)
        }
    }
}

class AgendaListWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AgendaListDotCalWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            WidgetUpdateWorker.enqueue(context)
        }
    }
}

class MonthGridWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MonthGridDotCalWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
            WidgetUpdateWorker.enqueue(context)
        }
    }
}

@Composable
private fun MediumAgendaRow(context: Context, item: WidgetEventItem, config: WidgetInstanceConfig, palette: DotCalWidgetPalette) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(bottom = 7.dp)
            .clickable(actionStartActivity(itemIntent(context, item))),
        verticalAlignment = Alignment.Top,
    ) {
        Text(if (config.content.showTime) item.timeLabel else item.dayOfMonth, maxLines = 1, modifier = GlanceModifier.width(52.dp).padding(top = 2.dp), style = monoStyle(palette.accent, 10, FontWeight.Bold))
        Spacer(GlanceModifier.width(6.dp))
        Column(modifier = GlanceModifier.width(134.dp)) {
            Text(item.displayTitle(config), maxLines = 1, style = primaryStyle(palette, 14, FontWeight.Bold))
            if (config.content.showTime) {
                Text(item.dateLabel.uppercase(Locale.getDefault()), maxLines = 1, style = monoStyle(palette.secondary, 9, FontWeight.Normal))
            }
        }
    }
}

@Composable
private fun MediumAgendaHero(context: Context, item: WidgetEventItem, config: WidgetInstanceConfig, palette: DotCalWidgetPalette) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable(actionStartActivity(itemIntent(context, item))),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = GlanceModifier.width(52.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(item.dayOfMonth, maxLines = 1, style = monoStyle(palette.accent, 20, FontWeight.Bold, TextAlign.Center))
            Text(item.dateLabel.dayAbbrev(), maxLines = 1, style = monoStyle(palette.secondary, 9, FontWeight.Bold, TextAlign.Center))
        }
        Spacer(GlanceModifier.width(6.dp))
        Column(modifier = GlanceModifier.width(134.dp)) {
            Text(item.displayTitle(config), maxLines = 2, style = primaryStyle(palette, 14, FontWeight.Bold))
            if (config.content.showTime || config.content.showLocation) {
                Text(item.detailLine(config), maxLines = 1, style = monoStyle(palette.secondary, 9, FontWeight.Normal))
            }
        }
    }
}

@Composable
private fun LargeAgendaRow(context: Context, item: WidgetEventItem, config: WidgetInstanceConfig, palette: DotCalWidgetPalette, highlight: Boolean) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(bottom = if (highlight) 10.dp else 7.dp)
            .clickable(actionStartActivity(itemIntent(context, item))),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = GlanceModifier.width(52.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(item.dayOfMonth, maxLines = 1, style = monoStyle(palette.accent, if (highlight) 22 else 16, FontWeight.Bold, TextAlign.Center))
            Text(item.dateLabel.dayAbbrev(), maxLines = 1, style = monoStyle(palette.secondary, 9, FontWeight.Bold, TextAlign.Center))
        }
        Spacer(GlanceModifier.width(10.dp))
        Column(modifier = GlanceModifier.width(164.dp)) {
            Text(item.displayTitle(config), maxLines = if (highlight) 2 else 1, style = primaryStyle(palette, if (highlight) 15 else 13, FontWeight.Bold))
            if (config.content.showTime || config.content.showLocation) {
                Text(item.largeDetailLine(config), maxLines = 1, style = monoStyle(palette.secondary, 9, FontWeight.Normal))
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
            Box(GlanceModifier.width(CalendarCellWidth).height(15.dp), contentAlignment = Alignment.Center) {
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
                        .height(23.dp)
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
private fun CompactMonthHeader(monthLabel: String, palette: DotCalWidgetPalette, metrics: CompactMonthMetrics) {
    Row(
        modifier = GlanceModifier.fillMaxWidth().height(metrics.headerHeight.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompactMonthChevron("<", -1, palette, metrics)
        Box(
            GlanceModifier
                .width(metrics.monthTitleWidth.dp)
                .height(metrics.headerHeight.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                monthLabel.substringBefore(' '),
                maxLines = 1,
                modifier = GlanceModifier.fillMaxWidth(),
                style = monoStyle(palette.primary, metrics.monthTitleSize, FontWeight.Bold, TextAlign.Center),
            )
        }
        CompactMonthChevron(">", 1, palette, metrics)
    }
}

@Composable
private fun CompactMonthChevron(label: String, delta: Int, palette: DotCalWidgetPalette, metrics: CompactMonthMetrics) {
    Box(
        modifier = GlanceModifier
            .width(metrics.chevronWidth.dp)
            .height(metrics.headerHeight.dp)
            .clickable(
                actionRunCallback<CompactMonthOffsetAction>(
                    actionParametersOf(CompactMonthOffsetAction.OffsetDeltaKey to delta),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, maxLines = 1, style = monoStyle(palette.secondary, metrics.chevronTextSize, FontWeight.Bold, TextAlign.Center))
    }
}

@Composable
private fun CompactMonthGrid(context: Context, days: List<WidgetCalendarDay>, palette: DotCalWidgetPalette, metrics: CompactMonthMetrics) {
    val settings = currentDotCalWidgetSettings()
    Box(
        GlanceModifier
            .fillMaxWidth()
            .clickable(actionStartActivity(openCalendarMonthIntent(context))),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            provider = ImageProvider(buildCompactMonthGridGraphic(context, days, settings, metrics)),
            contentDescription = null,
            modifier = GlanceModifier
                .width(metrics.gridWidth.dp)
                .height(metrics.gridHeight.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

class CompactMonthOffsetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val delta = parameters[OffsetDeltaKey] ?: 0
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { preferences ->
            val nextOffset = ((preferences[CalendarPreferences.KEY_WIDGET_MONTH_OFFSET] ?: 0) + delta).coerceIn(-12, 12)
            mutablePreferencesOf().apply {
                plusAssign(preferences)
                this[CalendarPreferences.KEY_WIDGET_MONTH_OFFSET] = nextOffset
            }
        }
        WidgetUpdateWorker.updateCompactMonthNow(context)
    }

    companion object {
        val OffsetDeltaKey = ActionParameters.Key<Int>("month_offset_delta")
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
            modifier = GlanceModifier.background(palette.solidSurface).cornerRadius(9.dp).padding(horizontal = 6.dp, vertical = verticalPadding.dp),
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
            .cornerRadius(18.dp),
    ) {
        // Dot texture: a tiled BitmapDrawable applied as a view background.
        // Glance routes background(ImageProvider(resId)) to setViewBackgroundResource,
        // so the drawable's tileMode="repeat" is honored (no stretching).
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(palette.dotTile))
                .cornerRadius(18.dp),
        ) {}
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

private fun openCalendarTodayIntent(context: Context): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse("dotcal://calendar/day?date=${LocalDate.now()}")).setPackage(context.packageName)
}

private fun openCalendarAgendaIntent(context: Context): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse("dotcal://calendar/agenda")).setPackage(context.packageName)
}

private fun openCalendarTasksIntent(context: Context): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse("dotcal://tasks")).setPackage(context.packageName)
}

private fun openCalendarDateIntent(context: Context, dateIso: String?): Intent {
    val uri = if (dateIso == null) "dotcal://calendar/month" else "dotcal://calendar/month?date=$dateIso"
    return Intent(Intent.ACTION_VIEW, Uri.parse(uri)).setPackage(context.packageName)
}

private val CalendarCellWidth = 35.dp
private data class CompactMonthMetrics(
    val horizontalPadding: Int,
    val topPadding: Int,
    val bottomPadding: Int,
    val headerHeight: Int,
    val headerBottomGap: Int,
    val monthTitleWidth: Int,
    val monthTitleSize: Int,
    val chevronWidth: Int,
    val chevronTextSize: Int,
    val cellWidth: Int,
    val columnGap: Int,
    val weekdayHeight: Int,
    val weekdayTextSize: Int,
    val weekdayBottomGap: Int,
    val dayHeight: Int,
    val dayTextSize: Int,
    val rowGap: Int,
    val todayRingSize: Int,
    val todayTextSize: Int,
) {
    val gridWidth: Int = (cellWidth * 7) + (columnGap * 6)
    val gridHeight: Int = weekdayHeight + weekdayBottomGap + (dayHeight * 6) + (rowGap * 5)
}

private fun compactMonthMetrics(size: DpSize): CompactMonthMetrics {
    return when {
        size.width >= 220.dp && size.height >= 220.dp -> CompactMonthMetrics(
            horizontalPadding = 14,
            topPadding = 12,
            bottomPadding = 10,
            headerHeight = 28,
            headerBottomGap = 7,
            monthTitleWidth = 118,
            monthTitleSize = 20,
            chevronWidth = 32,
            chevronTextSize = 20,
            cellWidth = 22,
            columnGap = 6,
            weekdayHeight = 16,
            weekdayTextSize = 11,
            weekdayBottomGap = 4,
            dayHeight = 20,
            dayTextSize = 12,
            rowGap = 4,
            todayRingSize = 20,
            todayTextSize = 10,
        )
        size.width >= 180.dp && size.height >= 180.dp -> CompactMonthMetrics(
            horizontalPadding = 10,
            topPadding = 9,
            bottomPadding = 8,
            headerHeight = 26,
            headerBottomGap = 5,
            monthTitleWidth = 104,
            monthTitleSize = 18,
            chevronWidth = 30,
            chevronTextSize = 18,
            cellWidth = 18,
            columnGap = 5,
            weekdayHeight = 14,
            weekdayTextSize = 10,
            weekdayBottomGap = 3,
            dayHeight = 17,
            dayTextSize = 11,
            rowGap = 3,
            todayRingSize = 18,
            todayTextSize = 9,
        )
        size.width >= 140.dp && size.height >= 140.dp -> CompactMonthMetrics(
            horizontalPadding = 8,
            topPadding = 8,
            bottomPadding = 7,
            headerHeight = 24,
            headerBottomGap = 8,
            monthTitleWidth = 86,
            monthTitleSize = 16,
            chevronWidth = 28,
            chevronTextSize = 16,
            cellWidth = 14,
            columnGap = 6,
            weekdayHeight = 13,
            weekdayTextSize = 9,
            weekdayBottomGap = 4,
            dayHeight = 14,
            dayTextSize = 9,
            rowGap = 4,
            todayRingSize = 16,
            todayTextSize = 8,
        )
        else -> CompactMonthMetrics(
            horizontalPadding = 4,
            topPadding = 3,
            bottomPadding = 2,
            headerHeight = 16,
            headerBottomGap = 5,
            monthTitleWidth = 58,
            monthTitleSize = 13,
            chevronWidth = 22,
            chevronTextSize = 12,
            cellWidth = 11,
            columnGap = 3,
            weekdayHeight = 10,
            weekdayTextSize = 8,
            weekdayBottomGap = 3,
            dayHeight = 12,
            dayTextSize = 8,
            rowGap = 3,
            todayRingSize = 13,
            todayTextSize = 7,
        )
    }
}

private fun todayDayAbbrev(): String {
    return LocalDate.now().format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault())).uppercase(Locale.getDefault())
}

private fun countdownLabel(item: WidgetEventItem): String {
    val millis = item.startTimeMs - System.currentTimeMillis()
    if (millis <= 0L) return "NOW"
    val duration = Duration.ofMillis(millis)
    val days = duration.toDays()
    if (days > 0) return "${days}D"
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    if (hours > 0) return if (minutes > 0) "${hours}H ${minutes}M" else "${hours}H"
    return "${duration.toMinutes().coerceAtLeast(1)}M"
}

private fun openAddEventIntent(context: Context): Intent {
    val today = LocalDate.now().toString()
    return Intent(Intent.ACTION_VIEW, Uri.parse("dotcal://event/new?date=$today")).setPackage(context.packageName)
}

private fun widgetTapIntent(context: Context, action: WidgetTapAction): Intent {
    return when (action) {
        WidgetTapAction.OpenCalendar -> openCalendarMonthIntent(context)
        WidgetTapAction.OpenToday -> openCalendarTodayIntent(context)
        WidgetTapAction.OpenAgenda -> openCalendarAgendaIntent(context)
        WidgetTapAction.QuickAdd -> openQuickAddIntent(context)
        WidgetTapAction.CreateEvent -> openAddEventIntent(context)
        WidgetTapAction.CreateTask -> openAddTaskIntent(context)
        WidgetTapAction.Search -> openSearchIntent(context)
    }
}

private fun openQuickAddIntent(context: Context): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse("dotcal://quick-add")).setPackage(context.packageName)
}

private fun openAddTaskIntent(context: Context): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse("dotcal://task/new")).setPackage(context.packageName)
}

private fun openSearchIntent(context: Context): Intent {
    return Intent(Intent.ACTION_VIEW, Uri.parse("dotcal://search")).setPackage(context.packageName)
}

@Composable
private fun LargeAgendaWidget(
    context: Context,
    config: WidgetInstanceConfig,
    data: WidgetCalendarData,
    palette: DotCalWidgetPalette,
) {
    val visibleRows = 4
    val hiddenRows = (data.events.size - visibleRows).coerceAtLeast(0) + data.moreItemCount
    WidgetSurfaceBox(palette) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 14.dp)
                .clickable(actionStartActivity(openCalendarAgendaIntent(context))),
        ) {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = GlanceModifier.width(116.dp)) {
                    Text("SCHEDULE", maxLines = 1, style = monoStyle(palette.primary, 14, FontWeight.Bold))
                    Spacer(GlanceModifier.height(2.dp))
                    Text(config.timeRange.longLabel(), maxLines = 1, style = monoStyle(palette.secondary, 10, FontWeight.Bold))
                }
                Text(
                    "${data.events.size + data.moreItemCount} ITEMS",
                    maxLines = 1,
                    modifier = GlanceModifier.fillMaxWidth(),
                    style = monoStyle(palette.accent, 13, FontWeight.Bold, TextAlign.End),
                )
            }
            Spacer(GlanceModifier.height(12.dp))
            if (data.events.isEmpty()) {
                Box(
                    modifier = GlanceModifier.fillMaxWidth().height(170.dp).clickable(actionStartActivity(openAddEventIntent(context))),
                    contentAlignment = Alignment.Center,
                ) {
                    CompactAddPrompt("NO EVENTS - TAP TO ADD", 30, palette)
                }
            } else {
                data.events.take(visibleRows).forEachIndexed { index, item ->
                    LargeAgendaRow(context, item, config, palette, highlight = index == 0)
                }
                if (hiddenRows > 0) {
                    Text(
                        "+$hiddenRows MORE",
                        maxLines = 1,
                        modifier = GlanceModifier
                            .padding(start = 68.dp)
                            .clickable(actionStartActivity(openCalendarAgendaIntent(context))),
                        style = monoStyle(palette.secondary, 11, FontWeight.Bold),
                    )
                }
            }
        }
    }
}

private fun quickActionGlyph(action: WidgetTapAction): String {
    return when (action) {
        WidgetTapAction.OpenCalendar -> "DC"
        WidgetTapAction.OpenToday -> LocalDate.now().dayOfMonth.toString()
        WidgetTapAction.OpenAgenda -> "AG"
        WidgetTapAction.QuickAdd,
        WidgetTapAction.CreateEvent,
        WidgetTapAction.CreateTask -> "+"
        WidgetTapAction.Search -> "?"
    }
}

private fun quickActionLabel(action: WidgetTapAction): String {
    return when (action) {
        WidgetTapAction.OpenCalendar -> "CALENDAR"
        WidgetTapAction.OpenToday -> "TODAY"
        WidgetTapAction.OpenAgenda -> "AGENDA"
        WidgetTapAction.QuickAdd -> "QUICK ADD"
        WidgetTapAction.CreateEvent -> "ADD EVENT"
        WidgetTapAction.CreateTask -> "ADD TASK"
        WidgetTapAction.Search -> "SEARCH"
    }
}

private fun todaySummary(data: WidgetCalendarData, config: WidgetInstanceConfig): String {
    return when {
        data.remainingEventCount > 0 && config.content.showTasks && data.tasks.isNotEmpty() -> "${data.remainingEventCount} EVENTS - ${data.tasks.size} TASKS"
        data.remainingEventCount > 0 -> "${data.remainingEventCount} EVENTS LEFT"
        config.content.showTasks && data.tasks.isNotEmpty() -> "${data.tasks.size} TASKS"
        else -> "OPEN DAY"
    }
}

private fun WidgetEventItem.displayTitle(config: WidgetInstanceConfig): String {
    return if (config.content.showEventTitle) title else "BUSY"
}

private fun WidgetEventItem.detailLine(config: WidgetInstanceConfig): String {
    val parts = buildList {
        if (config.content.showTime) add(timeLabel)
        if (config.content.showLocation && location.isNotBlank()) add(location.uppercase(Locale.getDefault()))
    }
    return parts.joinToString(" - ")
}


private fun WidgetTimeRange.shortLabel(): String {
    return when (this) {
        WidgetTimeRange.Today -> "TODAY"
        WidgetTimeRange.Next24Hours -> "24 HOURS"
        WidgetTimeRange.Next3Days -> "3 DAYS"
        WidgetTimeRange.Next7Days -> "7 DAYS"
        WidgetTimeRange.Next14Days -> "14 DAYS"
    }
}

private fun WidgetTimeRange.longLabel(): String {
    return when (this) {
        WidgetTimeRange.Today -> "TODAY"
        WidgetTimeRange.Next24Hours -> "NEXT 24 HOURS"
        WidgetTimeRange.Next3Days -> "NEXT 3 DAYS"
        WidgetTimeRange.Next7Days -> "NEXT 7 DAYS"
        WidgetTimeRange.Next14Days -> "NEXT 14 DAYS"
    }
}

private fun String.dayAbbrev(): String {
    return substringBefore(',').uppercase(Locale.getDefault())
}

private fun WidgetEventItem.largeDetailLine(config: WidgetInstanceConfig): String {
    val parts = buildList {
        add(dateLabel.uppercase(Locale.getDefault()))
        if (config.content.showTime) add(timeLabel.uppercase(Locale.getDefault()))
        if (config.content.showLocation && location.isNotBlank()) add(location.uppercase(Locale.getDefault()))
    }
    return parts.joinToString(" - ")
}

private val WidgetDigitPatterns = mapOf(
    '0' to listOf("111", "101", "101", "101", "101", "101", "111"),
    '1' to listOf("010", "110", "010", "010", "010", "010", "111"),
    '2' to listOf("111", "001", "001", "111", "100", "100", "111"),
    '3' to listOf("111", "001", "001", "111", "001", "001", "111"),
    '4' to listOf("101", "101", "101", "111", "001", "001", "001"),
    '5' to listOf("111", "100", "100", "111", "001", "001", "111"),
    '6' to listOf("111", "100", "100", "111", "101", "101", "111"),
    '7' to listOf("111", "001", "001", "010", "010", "010", "010"),
    '8' to listOf("111", "101", "101", "111", "101", "101", "111"),
    '9' to listOf("111", "101", "101", "111", "001", "001", "111"),
)

private fun buildCountdownNumberGraphic(text: String, accentColor: Int): Bitmap {
    val width = 228
    val height = 74
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    val patterns = text.map { WidgetDigitPatterns[it] ?: WidgetDigitPatterns.getValue('0') }
    val columns = patterns.sumOf { it.first().length } + (patterns.size - 1).coerceAtLeast(0)
    val rows = 7
    val gap = 4f
    val dot = 5.5f
    val totalWidth = columns * dot + (columns - 1) * gap
    val totalHeight = rows * dot + (rows - 1) * gap
    var xCursor = (width - totalWidth) / 2f
    val yStart = (height - totalHeight) / 2f
    paint.color = accentColor
    patterns.forEach { pattern ->
        pattern.forEachIndexed { row, line ->
            line.forEachIndexed { column, mark ->
                if (mark == '1') {
                    canvas.drawCircle(
                        xCursor + column * (dot + gap) + dot / 2f,
                        yStart + row * (dot + gap) + dot / 2f,
                        dot / 2f,
                        paint,
                    )
                }
            }
        }
        xCursor += pattern.first().length * (dot + gap)
    }
    return bitmap
}

private data class CompactMonthGridColors(
    val primary: Int,
    val secondary: Int,
    val surface: Int,
    val accent: Int,
)

private fun buildCompactMonthGridGraphic(
    context: Context,
    days: List<WidgetCalendarDay>,
    settings: DotCalWidgetSettings,
    metrics: CompactMonthMetrics,
): Bitmap {
    val scale = 3
    val width = metrics.gridWidth * scale
    val height = metrics.gridHeight * scale
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val colors = compactMonthGridColors(context, settings)

    fun drawCenteredText(text: String, x: Float, centerY: Float, textSize: Int, color: Int, bold: Boolean) {
        paint.reset()
        paint.isAntiAlias = true
        paint.color = color
        paint.textAlign = Paint.Align.CENTER
        paint.textSize = textSize * scale.toFloat()
        paint.typeface = Typeface.create(Typeface.MONOSPACE, if (bold) Typeface.BOLD else Typeface.NORMAL)
        val baseline = centerY - (paint.descent() + paint.ascent()) / 2f
        canvas.drawText(text, x, baseline, paint)
    }

    listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { index, label ->
        val x = ((index * (metrics.cellWidth + metrics.columnGap)) + (metrics.cellWidth / 2f)) * scale
        drawCenteredText(label, x, metrics.weekdayHeight * scale / 2f, metrics.weekdayTextSize, colors.secondary, bold = true)
    }

    val weeks = days.chunked(7).take(6)
    val daysTop = (metrics.weekdayHeight + metrics.weekdayBottomGap) * scale
    repeat(6) { weekIndex ->
        val week = weeks.getOrNull(weekIndex).orEmpty()
        repeat(7) { dayIndex ->
            val day = week.getOrNull(dayIndex) ?: return@repeat
            val dayNumber = day.dayOfMonth ?: return@repeat
            val cellLeft = (dayIndex * (metrics.cellWidth + metrics.columnGap)) * scale
            val cellTop = daysTop + (weekIndex * (metrics.dayHeight + metrics.rowGap) * scale)
            val centerX = cellLeft + (metrics.cellWidth * scale / 2f)
            val centerY = cellTop + (metrics.dayHeight * scale / 2f)
            if (day.isToday) {
                paint.reset()
                paint.isAntiAlias = true
                paint.color = colors.accent
                canvas.drawCircle(centerX, centerY, metrics.todayRingSize * scale / 2f, paint)
                paint.color = colors.surface
                canvas.drawCircle(centerX, centerY, (metrics.todayRingSize - 3).coerceAtLeast(1) * scale / 2f, paint)
                drawCenteredText(dayNumber.toString(), centerX, centerY, metrics.todayTextSize, colors.accent, bold = true)
            } else {
                drawCenteredText(
                    dayNumber.toString(),
                    centerX,
                    centerY,
                    metrics.dayTextSize,
                    if (day.hasEvents) colors.accent else colors.primary,
                    bold = false,
                )
            }
        }
    }
    return bitmap
}

private fun compactMonthGridColors(context: Context, settings: DotCalWidgetSettings): CompactMonthGridColors {
    val accentSetting = settings.instanceConfig.appearance.accentColor ?: settings.accentColor
    val accent = widgetAccentArgb(settings.copy(accentColor = accentSetting))
    val mode = settings.instanceConfig.appearance.themeMode ?: settings.themeMode
    val systemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    val dark = when (mode) {
        "Light" -> false
        "Dark" -> true
        else -> systemDark
    }
    return if (mode == "System") {
        CompactMonthGridColors(
            primary = ContextCompat.getColor(context, R.color.widget_primary),
            secondary = ContextCompat.getColor(context, R.color.widget_secondary),
            surface = if (systemDark) 0xFF1A1A1A.toInt() else android.graphics.Color.WHITE,
            accent = accent,
        )
    } else {
        CompactMonthGridColors(
            primary = if (dark) android.graphics.Color.WHITE else 0xFF101010.toInt(),
            secondary = if (dark) 0xFF7A7A7A.toInt() else 0xFF6B6B6B.toInt(),
            surface = if (dark) 0xFF1A1A1A.toInt() else android.graphics.Color.WHITE,
            accent = accent,
        )
    }
}
