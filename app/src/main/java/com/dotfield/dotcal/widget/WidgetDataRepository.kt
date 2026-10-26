package com.dotfield.dotcal.widget

import android.content.Context
import android.text.format.DateFormat
import com.dotfield.dotcal.data.CalendarDao
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.DotCalDatabase
import com.dotfield.dotcal.data.baseEventId
import com.dotfield.dotcal.data.countdown.CountdownPinStore
import com.dotfield.dotcal.data.privacy.AppPrivacyManager
import com.dotfield.dotcal.data.recurrence.expandRecurringEvents
import com.dotfield.dotcal.data.sidestore.EventSideStoreNamespaces
import com.dotfield.dotcal.data.sidestore.SharedSideStore
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class WidgetEventItem(
    val id: String,
    val title: String,
    val timeLabel: String,
    val location: String,
    val dateLabel: String,
    val dayOfMonth: String,
    val startTimeMs: Long,
    val countdownDays: String,
)

data class WidgetCalendarDay(
    val dayOfMonth: Int?,
    val dateIso: String? = null,
    val isToday: Boolean = false,
    val hasEvents: Boolean = false,
)

data class WidgetCalendarData(
    val header: String,
    val monthLabel: String,
    val todayLabel: String,
    val nextEvent: WidgetEventItem?,
    val events: List<WidgetEventItem>,
    val tasks: List<WidgetEventItem> = emptyList(),
    val todayEventCount: Int = 0,
    val remainingEventCount: Int = 0,
    val moreItemCount: Int,
    val days: List<WidgetCalendarDay> = emptyList(),
)

private fun LocalDate.atStartMs(zoneId: ZoneId): Long = atStartOfDay(zoneId).toInstant().toEpochMilli()

class WidgetDataRepository(
    private val context: Context,
    private val dao: CalendarDao,
) {
    private val privacyManager = AppPrivacyManager(context.applicationContext)
    private val sideStore = SharedSideStore(context.applicationContext)

    suspend fun load(
        config: WidgetInstanceConfig,
        defaultMaxItems: Int,
        monthOffset: Int = 0,
        nowMs: Long = System.currentTimeMillis(),
    ): WidgetCalendarData = withContext(Dispatchers.IO) {
        val zoneId = ZoneId.systemDefault()
        val now = Instant.ofEpochMilli(nowMs).atZone(zoneId)
        val today = now.toLocalDate()
        val displayMonthDate = today.plusMonths(monthOffset.toLong())
        val displayMonth = YearMonth.from(displayMonthDate)
        val displayMonthStart = displayMonth.atDay(1)
        val displayMonthEndExclusive = displayMonth.atEndOfMonth().plusDays(1)
        val preferences = context.calendarPreferencesDataStore.data.first()
        val weekStart = widgetWeekStartFromPreferences(preferences)
        val rangeStart = minOf(today, displayMonthStart)
        val rangeEnd = maxOf(today.plusDays(config.effectiveRangeDays()), displayMonthEndExclusive)
        val privateIds = privacyManager.observePrivateVaultIds().first()
        val accountId = config.calendarFilter.accountId
        val shiftEventIds = if (config.category == WidgetCategory.Shift) {
            sideStore.readNamespace(SHIFT_EVENT_METADATA_NAMESPACE).keys
        } else {
            emptySet()
        }
        val providerRdates = sideStore.readNamespace(EventSideStoreNamespaces.ProviderRdates)
        val visibleItems = expandRecurringEvents(
            events = dao.getVisibleEventsForWidget(rangeStart.atStartMs(zoneId), rangeEnd.atStartMs(zoneId), accountId)
                .filterNot { it.baseEventId() in privateIds },
            rangeStart = rangeStart,
            rangeEndExclusive = rangeEnd,
            providerRdates = providerRdates,
        )
            .filter { it.endTimeMs >= nowMs }
            .filter { config.content.showAllDayEvents || it.isAllDay == 0 }
            .filter { config.category != WidgetCategory.Shift || it.baseEventId() in shiftEventIds }
            .sortedForWidget(zoneId)
        val use24Hour = DateFormat.is24HourFormat(context)
        val items = visibleItems.asWidgetItems(zoneId, use24Hour, nowMs)
        val maxItems = config.maxVisibleItems(defaultMaxItems)
        val pinnedCountdownItems = if (config.category == WidgetCategory.Countdown) {
            loadPinnedCountdownItems(today, rangeEnd, zoneId, use24Hour, nowMs, privateIds)
        } else {
            emptyList()
        }
        val tasks = if (config.content.showTasks || config.category == WidgetCategory.Tasks) {
            dao.getOpenTasksForWidget(rangeEnd.atStartMs(zoneId), accountId)
                .filterNot { it.baseEventId() in privateIds }
                .asWidgetItems(zoneId, use24Hour, nowMs)
                .take(maxItems)
        } else {
            emptyList()
        }
        val displayItems = pinnedCountdownItems.ifEmpty { items }
        val todayEndMs = today.plusDays(1).atStartMs(zoneId)
        val remainingEventsToday = visibleItems.count { it.startTimeMs < todayEndMs && it.endTimeMs >= nowMs }
        WidgetCalendarData(
            header = today.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
            monthLabel = displayMonthDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            todayLabel = today.dayOfMonth.toString(),
            nextEvent = displayItems.firstOrNull(),
            events = displayItems.take(maxItems),
            tasks = tasks,
            todayEventCount = visibleItems.count { it.startTimeMs < todayEndMs },
            remainingEventCount = remainingEventsToday,
            moreItemCount = (displayItems.size - maxItems).coerceAtLeast(0),
            days = if (config.category == WidgetCategory.Calendar) monthDays(displayMonthDate, today, weekStart, visibleItems, zoneId) else emptyList(),
        )
    }

    private fun WidgetInstanceConfig.effectiveRangeDays(): Long {
        return when {
            category == WidgetCategory.Calendar && viewType == WidgetViewType.Month -> WIDGET_RANGE_DAYS
            category == WidgetCategory.Shift -> WidgetTimeRange.Next14Days.days
            else -> timeRange.days
        }
    }

    private fun List<CalendarEvent>.sortedForWidget(zoneId: ZoneId): List<CalendarEvent> {
        return sortedWith(
            compareBy<CalendarEvent> { it.widgetDayStart(zoneId) }
                .thenBy { it.widgetPriority() }
                .thenBy { it.startTimeMs }
                .thenBy { it.title },
        )
    }

    private fun CalendarEvent.widgetDayStart(zoneId: ZoneId): Long {
        return Instant.ofEpochMilli(startTimeMs).atZone(zoneId).toLocalDate().atStartMs(zoneId)
    }

    private fun CalendarEvent.widgetPriority(): Int {
        return if (isAllDay == 0) 0 else 1
    }

    private suspend fun loadPinnedCountdownItems(
        rangeStart: LocalDate,
        rangeEndExclusive: LocalDate,
        zoneId: ZoneId,
        use24Hour: Boolean,
        nowMs: Long,
        privateIds: Set<String>,
    ): List<WidgetEventItem> {
        val pinnedIds = sideStore.readNamespace(CountdownPinStore.Namespace).filterValues { it == "true" }.keys
        if (pinnedIds.isEmpty()) return emptyList()
        return pinnedIds
            .mapNotNull { id -> dao.getEvent(id) }
            .filterNot { it.baseEventId() in privateIds }
            .filter { event -> dao.getAccount(event.accountId)?.isVisible == 1 }
            .let { events ->
                expandRecurringEvents(
                    events = events,
                    rangeStart = rangeStart,
                    rangeEndExclusive = rangeEndExclusive.plusYears(1),
                    providerRdates = sideStore.readNamespace(EventSideStoreNamespaces.ProviderRdates),
                )
            }
            .filter { it.endTimeMs >= nowMs }
            .sortedForWidget(zoneId)
            .asWidgetItems(zoneId, use24Hour, nowMs)
    }

    private fun List<CalendarEvent>.asWidgetItems(zoneId: ZoneId, use24Hour: Boolean, nowMs: Long): List<WidgetEventItem> {
        return map { event ->
            val dateTime = Instant.ofEpochMilli(event.startTimeMs).atZone(zoneId)
            WidgetEventItem(
                id = event.baseEventId(),
                title = event.title.toWidgetTitleCase(),
                timeLabel = event.widgetTimeLabel(zoneId, use24Hour),
                location = event.location.trim(),
                dateLabel = dateTime.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                dayOfMonth = dateTime.dayOfMonth.toString(),
                startTimeMs = event.startTimeMs,
                countdownDays = CountdownPinStore.daysUntil(event.startTimeMs, zoneId, nowMs).toString(),
            )
        }
    }

    private fun CalendarEvent.widgetTimeLabel(zoneId: ZoneId, use24Hour: Boolean): String {
        if (isAllDay == 0) {
            val pattern = if (use24Hour) "HH:mm" else "h:mm a"
            return Instant.ofEpochMilli(startTimeMs).atZone(zoneId).format(DateTimeFormatter.ofPattern(pattern)).uppercase(Locale.getDefault())
        }
        return "All day"
    }

    private fun String.toWidgetTitleCase(): String {
        return trim().split(Regex("\\s+")).joinToString(" ") { word ->
            if (word.length > 1 && word.all { it.isUpperCase() || !it.isLetter() }) {
                word
            } else {
                word.lowercase(Locale.getDefault()).replaceFirstChar { first ->
                    if (first.isLowerCase()) first.titlecase(Locale.getDefault()) else first.toString()
                }
            }
        }
    }

    private fun monthDays(
        displayMonthDate: LocalDate,
        today: LocalDate,
        weekStart: DayOfWeek,
        events: List<CalendarEvent>,
        zoneId: ZoneId,
    ): List<WidgetCalendarDay> {
        val month = YearMonth.from(displayMonthDate)
        val eventDays = events
            .filter { YearMonth.from(Instant.ofEpochMilli(it.startTimeMs).atZone(zoneId)) == month }
            .map { Instant.ofEpochMilli(it.startTimeMs).atZone(zoneId).dayOfMonth }
            .toSet()
        return widgetMonthDays(displayMonthDate, today, weekStart, eventDays)
    }

    companion object {
        private const val WIDGET_RANGE_DAYS = 45L
        private const val SHIFT_EVENT_METADATA_NAMESPACE = "shift_event_metadata"

        fun create(context: Context): WidgetDataRepository {
            return WidgetDataRepository(context.applicationContext, DotCalDatabase.create(context).calendarDao())
        }
    }
}
