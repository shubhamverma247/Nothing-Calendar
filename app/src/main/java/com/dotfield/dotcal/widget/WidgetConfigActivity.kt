package com.dotfield.dotcal.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.DotCalDatabase
import com.dotfield.dotcal.prefs.CalendarPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            WidgetConfigScreen(
                appWidgetId = appWidgetId,
                legacyKind = legacyKindForWidget(),
                onSave = { config -> saveConfig(config) },
                onCancel = { finish() },
            )
        }
    }

    private fun saveConfig(config: WidgetInstanceConfig) {
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(this@WidgetConfigActivity).getGlanceIdBy(appWidgetId)
            updateAppWidgetState(this@WidgetConfigActivity, glanceId) { preferences ->
                val accountId = config.calendarFilter.accountId
                if (accountId == null) {
                    preferences.remove(CalendarPreferences.KEY_WIDGET_ACCOUNT_ID)
                } else {
                    preferences[CalendarPreferences.KEY_WIDGET_ACCOUNT_ID] = accountId
                }
                preferences[CalendarPreferences.KEY_WIDGET_INSTANCE_CONFIG] = config.encode()
            }
            WidgetUpdateWorker.updateNow(this@WidgetConfigActivity)
            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }

    private fun legacyKindForWidget(): LegacyWidgetKind {
        val providerName = AppWidgetManager.getInstance(this)
            .getAppWidgetInfo(appWidgetId)
            ?.provider
            ?.className
            .orEmpty()
        return when {
            providerName.endsWith("SmallDotCalWidgetReceiver") -> LegacyWidgetKind.Small
            providerName.endsWith("MediumDotCalWidgetReceiver") -> LegacyWidgetKind.Medium
            providerName.endsWith("LargeDotCalWidgetReceiver") -> LegacyWidgetKind.Large
            providerName.endsWith("EventCountdownWidgetReceiver") -> LegacyWidgetKind.Countdown
            providerName.endsWith("AgendaListWidgetReceiver") -> LegacyWidgetKind.Agenda
            providerName.endsWith("MonthGridWidgetReceiver") -> LegacyWidgetKind.MonthGrid
            else -> LegacyWidgetKind.Medium
        }
    }
}

@Composable
private fun WidgetConfigScreen(
    appWidgetId: Int,
    legacyKind: LegacyWidgetKind,
    onSave: (WidgetInstanceConfig) -> Unit,
    onCancel: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var accounts by remember { mutableStateOf<List<CalendarAccount>>(emptyList()) }
    var config by remember(legacyKind) { mutableStateOf(WidgetInstanceConfig.legacyDefault(legacyKind)) }

    LaunchedEffect(Unit) {
        val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
        val state = getAppWidgetState<Preferences>(context, PreferencesGlanceStateDefinition, glanceId)
        val fallback = WidgetInstanceConfig.legacyDefault(legacyKind, state[CalendarPreferences.KEY_WIDGET_ACCOUNT_ID])
        config = WidgetInstanceConfig.decodeOrDefault(state[CalendarPreferences.KEY_WIDGET_INSTANCE_CONFIG], fallback)
        accounts = withContext(Dispatchers.IO) {
            DotCalDatabase.create(context).calendarDao().getAccountsForWidgetConfig()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 22.dp, vertical = 24.dp),
    ) {
        Text("DotCal Widget", color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            "Choose what this widget shows. Each widget keeps its own setup.",
            color = Color(0xFFB3B3B3),
            fontFamily = FontFamily.SansSerif,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(22.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                WidgetSectionLabel("WHAT TO SHOW")
                WidgetCategory.entries.forEach { category ->
                    WidgetConfigOption(
                        title = category.configTitle(),
                        subtitle = category.configSubtitle(),
                        selected = config.category == category,
                        enabled = true,
                        onClick = {
                            config = config.defaultForCategory(category)
                        },
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            item {
                WidgetSectionLabel("TIME RANGE")
                config.timeRangeOptions().forEach { range ->
                    WidgetConfigOption(
                        title = range.configTitle(),
                        subtitle = range.configSubtitle(),
                        selected = config.timeRange == range,
                        enabled = true,
                        onClick = { config = config.copy(timeRange = range) },
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            item {
                WidgetSectionLabel("LAYOUT")
                WidgetLayoutMode.entries.forEach { mode ->
                    WidgetConfigOption(
                        title = mode.name.uppercase(),
                        subtitle = mode.configSubtitle(),
                        selected = config.layoutMode == mode,
                        enabled = true,
                        onClick = { config = config.copy(layoutMode = mode) },
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            if (config.category == WidgetCategory.QuickActions) {
                item {
                    WidgetSectionLabel("ACTION")
                    quickActionOptions().forEach { action ->
                        WidgetConfigOption(
                            title = action.configTitle(),
                            subtitle = action.configSubtitle(),
                            selected = config.interaction.tapAction == action,
                            enabled = true,
                            onClick = { config = config.copy(interaction = WidgetInteractionConfig(action)) },
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
            item {
                WidgetSectionLabel("CALENDARS")
                WidgetAccountOption(
                    title = "All Calendars",
                    subtitle = "Use visible calendars",
                    color = Color(0xFFFF3B30),
                    selected = config.calendarFilter.accountId == null,
                    enabled = true,
                    onClick = { config = config.copy(calendarFilter = WidgetCalendarFilter(null)) },
                )
            }
            items(accounts, key = { it.id }) { account ->
                WidgetAccountOption(
                    title = account.displayName.ifBlank { account.accountName },
                    subtitle = account.accountType,
                    color = runCatching { Color(android.graphics.Color.parseColor(account.color)) }.getOrDefault(Color(0xFFFF3B30)),
                    selected = config.calendarFilter.accountId == account.id,
                    enabled = true,
                    onClick = { config = config.copy(calendarFilter = WidgetCalendarFilter(account.id)) },
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E), contentColor = Color.White),
                modifier = Modifier.weight(1f),
            ) {
                Text("Cancel")
            }
            Button(
                onClick = { onSave(config) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30), contentColor = Color.White),
                modifier = Modifier.weight(1f),
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun WidgetSectionLabel(label: String) {
    Text(
        label,
        color = Color(0xFFB3B3B3),
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun WidgetConfigOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (enabled) Color.White else Color(0xFF777777),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
            Text(
                subtitle,
                color = if (enabled) Color(0xFFB3B3B3) else Color(0xFF555555),
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
            )
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF3B30), unselectedColor = Color(0xFF777777)),
        )
    }
}

@Composable
private fun WidgetAccountOption(
    title: String,
    subtitle: String,
    color: Color,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable(enabled = enabled, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(if (enabled) color else Color(0xFF555555)),
        )
        Spacer(Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = if (enabled) Color.White else Color(0xFF777777),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
            Text(
                subtitle,
                color = if (enabled) Color(0xFFB3B3B3) else Color(0xFF555555),
                fontFamily = FontFamily.SansSerif,
                fontSize = 12.sp,
            )
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFF3B30), unselectedColor = Color(0xFF777777)),
        )
    }
}

private fun WidgetInstanceConfig.defaultForCategory(category: WidgetCategory): WidgetInstanceConfig {
    val filter = calendarFilter
    return when (category) {
        WidgetCategory.Calendar -> WidgetInstanceConfig(
            category = category,
            viewType = WidgetViewType.Month,
            calendarFilter = filter,
            timeRange = WidgetTimeRange.Next7Days,
            layoutMode = WidgetLayoutMode.Detailed,
        )
        WidgetCategory.Schedule -> WidgetInstanceConfig(
            category = category,
            viewType = WidgetViewType.Agenda,
            calendarFilter = filter,
            timeRange = WidgetTimeRange.Next14Days,
            layoutMode = WidgetLayoutMode.Compact,
        )
        WidgetCategory.Today -> WidgetInstanceConfig(
            category = category,
            viewType = WidgetViewType.Today,
            content = WidgetContentOptions(showTasks = true),
            calendarFilter = filter,
            timeRange = WidgetTimeRange.Today,
            layoutMode = WidgetLayoutMode.Compact,
            interaction = WidgetInteractionConfig(WidgetTapAction.OpenToday),
        )
        WidgetCategory.Tasks -> WidgetInstanceConfig(
            category = category,
            viewType = WidgetViewType.TaskList,
            calendarFilter = filter,
            timeRange = WidgetTimeRange.Next7Days,
            layoutMode = WidgetLayoutMode.Compact,
            interaction = WidgetInteractionConfig(WidgetTapAction.OpenToday),
        )
        WidgetCategory.Countdown -> WidgetInstanceConfig(
            category = category,
            viewType = WidgetViewType.Countdown,
            calendarFilter = filter,
            timeRange = WidgetTimeRange.Next14Days,
            layoutMode = WidgetLayoutMode.Minimal,
        )
        WidgetCategory.QuickActions -> WidgetInstanceConfig(
            category = category,
            viewType = WidgetViewType.QuickAction,
            calendarFilter = filter,
            timeRange = WidgetTimeRange.Today,
            layoutMode = WidgetLayoutMode.Minimal,
            interaction = WidgetInteractionConfig(WidgetTapAction.CreateEvent),
        )
    }
}

private fun WidgetInstanceConfig.timeRangeOptions(): List<WidgetTimeRange> {
    return when (category) {
        WidgetCategory.Calendar -> listOf(WidgetTimeRange.Next7Days)
        WidgetCategory.Schedule,
        WidgetCategory.Tasks,
        WidgetCategory.Countdown -> listOf(WidgetTimeRange.Today, WidgetTimeRange.Next24Hours, WidgetTimeRange.Next3Days, WidgetTimeRange.Next7Days, WidgetTimeRange.Next14Days)
        WidgetCategory.Today,
        WidgetCategory.QuickActions -> listOf(WidgetTimeRange.Today)
    }
}

private fun WidgetCategory.configTitle(): String {
    return when (this) {
        WidgetCategory.Calendar -> "Calendar"
        WidgetCategory.Schedule -> "Schedule"
        WidgetCategory.Today -> "Today"
        WidgetCategory.Tasks -> "Tasks"
        WidgetCategory.Countdown -> "Countdown"
        WidgetCategory.QuickActions -> "Quick Actions"
    }
}

private fun WidgetCategory.configSubtitle(): String {
    return when (this) {
        WidgetCategory.Calendar -> "Month overview"
        WidgetCategory.Schedule -> "Agenda, next event, or 14-day schedule"
        WidgetCategory.Today -> "Date, event count, next event, tasks"
        WidgetCategory.Tasks -> "Open tasks and due items"
        WidgetCategory.Countdown -> "Pinned or next upcoming event"
        WidgetCategory.QuickActions -> "Fast add and calendar shortcuts"
    }
}

private fun quickActionOptions(): List<WidgetTapAction> {
    return listOf(
        WidgetTapAction.CreateEvent,
        WidgetTapAction.OpenToday,
        WidgetTapAction.OpenAgenda,
        WidgetTapAction.OpenCalendar,
    )
}

private fun WidgetTapAction.configTitle(): String {
    return when (this) {
        WidgetTapAction.CreateEvent -> "Add Event"
        WidgetTapAction.OpenToday -> "Open Today"
        WidgetTapAction.OpenAgenda -> "Open Agenda"
        WidgetTapAction.OpenCalendar -> "Open Calendar"
        WidgetTapAction.QuickAdd -> "Quick Add"
        WidgetTapAction.CreateTask -> "Add Task"
        WidgetTapAction.Search -> "Search"
    }
}

private fun WidgetTapAction.configSubtitle(): String {
    return when (this) {
        WidgetTapAction.CreateEvent -> "Start a new event"
        WidgetTapAction.OpenToday -> "Jump to today's calendar"
        WidgetTapAction.OpenAgenda -> "Open the agenda view"
        WidgetTapAction.OpenCalendar -> "Open the month view"
        WidgetTapAction.QuickAdd -> "Open natural-language entry"
        WidgetTapAction.CreateTask -> "Start a new task"
        WidgetTapAction.Search -> "Open search"
    }
}

private fun WidgetTimeRange.configTitle(): String {
    return when (this) {
        WidgetTimeRange.Today -> "Today"
        WidgetTimeRange.Next24Hours -> "Next 24 Hours"
        WidgetTimeRange.Next3Days -> "Next 3 Days"
        WidgetTimeRange.Next7Days -> "Next 7 Days"
        WidgetTimeRange.Next14Days -> "Next 14 Days"
    }
}

private fun WidgetTimeRange.configSubtitle(): String {
    return when (this) {
        WidgetTimeRange.Today -> "Only today's items"
        WidgetTimeRange.Next24Hours -> "Rolling one-day window"
        WidgetTimeRange.Next3Days -> "Short upcoming view"
        WidgetTimeRange.Next7Days -> "Standard agenda range"
        WidgetTimeRange.Next14Days -> "Two-week widget range"
    }
}

private fun WidgetLayoutMode.configSubtitle(): String {
    return when (this) {
        WidgetLayoutMode.Minimal -> "Show the essentials"
        WidgetLayoutMode.Compact -> "Balanced detail"
        WidgetLayoutMode.Detailed -> "Use more space when available"
    }
}
