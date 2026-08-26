package com.dotfield.dotcal.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.dotfield.dotcal.R
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

private data class PlacedWidgetUi(
    val appWidgetId: Int,
    val label: String,
    val sizeLabel: String,
    val summary: String,
)

class WidgetManagerActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WidgetManagerScreen(
                onBack = { finish() },
                onOpenWidget = { appWidgetId -> openWidgetConfig(appWidgetId) },
            )
        }
    }

    private fun openWidgetConfig(appWidgetId: Int) {
        startActivity(
            Intent(this, WidgetConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId),
        )
    }

    companion object {
        fun start(context: Context) {
            context.startActivity(Intent(context, WidgetManagerActivity::class.java))
        }
    }
}

private val PLACED_WIDGET_RECEIVER_CLASSES = listOf(
    "DateOnlyDotCalWidgetReceiver",
    "CompactMonthDotCalWidgetReceiver",
    "ShiftWideWidgetReceiver",
    "SmallDotCalWidgetReceiver",
    "MediumDotCalWidgetReceiver",
    "LargeDotCalWidgetReceiver",
    "EventCountdownWidgetReceiver",
    "AgendaListWidgetReceiver",
    "MonthGridWidgetReceiver",
)

@Composable
private fun WidgetManagerScreen(
    onBack: () -> Unit,
    onOpenWidget: (Int) -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    var isDark by remember { mutableStateOf(true) }
    val ui = remember(isDark) { EditorPalette(isDark) }
    var widgets by remember { mutableStateOf<List<PlacedWidgetUi>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(isDark) {
        val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
    }

    LaunchedEffect(Unit) {
        isDark = resolveIsDark(
            context,
            context.calendarPreferencesDataStore.data.first()[CalendarPreferences.KEY_THEME_MODE],
        )
        widgets = withContext(Dispatchers.IO) { loadPlacedWidgets(context) }
        loaded = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ui.ink)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(R.string.widget_manager_back),
                color = EditorAccent,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.clickable(onClick = onBack),
            )
        }
        Column(modifier = Modifier.padding(horizontal = 22.dp)) {
            Text(
                stringResource(R.string.widget_manager_title),
                color = ui.paper,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.widget_manager_subtitle),
                color = ui.muted,
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(20.dp))
        }

        if (!loaded) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = EditorAccent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(26.dp),
                )
            }
            return@Column
        }

        if (widgets.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ui.panel)
                    .padding(24.dp),
            ) {
                Text(
                    stringResource(R.string.widget_manager_empty_title),
                    color = ui.paper,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.widget_manager_empty_body),
                    color = ui.muted,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                )
            }
            Spacer(Modifier.weight(1f))
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(widgets, key = { it.appWidgetId }) { widget ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ui.panel)
                        .clickable { onOpenWidget(widget.appWidgetId) }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            widget.label,
                            color = ui.paper,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f),
                        )
                        if (widget.sizeLabel.isNotBlank()) {
                            Text(
                                widget.sizeLabel,
                                color = ui.muted,
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(ui.divider)
                                    .padding(horizontal = 7.dp, vertical = 3.dp),
                            )
                        }
                    }
                    if (widget.summary.isNotBlank()) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            widget.summary,
                            color = ui.muted,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

/** Friendly per-kind display names keyed by provider class suffix (manifest labels like
 *  "DC 2x2 Event" are launcher-picker names and read poorly inside the manager list). */
private val RECEIVER_DISPLAY_NAMES = mapOf(
    "DateOnlyDotCalWidgetReceiver" to "Date · 1x1",
    "CompactMonthDotCalWidgetReceiver" to "Compact month · 2x2",
    "ShiftWideWidgetReceiver" to "Shifts · 4x1",
    "SmallDotCalWidgetReceiver" to "Event · 2x2",
    "MediumDotCalWidgetReceiver" to "Agenda · 4x2",
    "LargeDotCalWidgetReceiver" to "Month · 4x4",
    "EventCountdownWidgetReceiver" to "Countdown",
    "AgendaListWidgetReceiver" to "Agenda list",
    "MonthGridWidgetReceiver" to "Month grid",
)

private suspend fun loadPlacedWidgets(context: Context): List<PlacedWidgetUi> {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val glanceManager = GlanceAppWidgetManager(context)
    val result = mutableListOf<PlacedWidgetUi>()
    for (className in PLACED_WIDGET_RECEIVER_CLASSES) {
        val componentName = ComponentName(context, "com.dotfield.dotcal.widget.$className")
        val ids = runCatching { appWidgetManager.getAppWidgetIds(componentName) }.getOrNull() ?: continue
        for (id in ids) {
            val info = appWidgetManager.getAppWidgetInfo(id)
            val label = RECEIVER_DISPLAY_NAMES[className]
                ?: info?.label.orEmpty().ifBlank { className }
            var summary = ""
            runCatching {
                val glanceId = glanceManager.getGlanceIdBy(id)
                val state = getAppWidgetState<Preferences>(context, PreferencesGlanceStateDefinition, glanceId)
                val encoded = state[CalendarPreferences.KEY_WIDGET_INSTANCE_CONFIG]
                if (encoded != null) summary = summarize(context, encoded)
            }
            result.add(PlacedWidgetUi(id, label, gridSizeLabel(info?.minWidth, info?.minHeight), summary))
        }
    }
    // Number duplicate kinds ("Event · 2x2", "Event · 2x2 2", ...) so identical widget
    // types stay distinguishable in the list.
    val totals = result.groupingBy { it.label }.eachCount()
    val seq = mutableMapOf<String, Int>()
    return result
        .map { widget ->
            if ((totals[widget.label] ?: 0) > 1) {
                val n = (seq[widget.label] ?: 0) + 1
                seq[widget.label] = n
                widget.copy(label = "${widget.label} $n")
            } else {
                widget
            }
        }
        .sortedWith(compareBy({ it.label }, { it.appWidgetId }))
}

/** Approximates launcher grid cells (70dp cell + 30dp spacing, launcher standard). */
private fun gridSizeLabel(minWidthDp: Int?, minHeightDp: Int?): String {
    if (minWidthDp == null || minHeightDp == null || minWidthDp <= 0 || minHeightDp <= 0) return ""
    val cols = ((minWidthDp + 30) / 70).coerceIn(1, 5)
    val rows = ((minHeightDp + 30) / 70).coerceIn(1, 5)
    return "$cols×$rows"
}

internal fun summarize(context: Context, encoded: String): String {
    val config = WidgetInstanceConfig.decodeOrDefault(encoded, WidgetInstanceConfig(WidgetCategory.Schedule, WidgetViewType.Agenda))
    val range = when (config.timeRange) {
        WidgetTimeRange.Today -> context.getString(R.string.widget_range_today)
        WidgetTimeRange.Next24Hours -> context.getString(R.string.widget_range_24h)
        WidgetTimeRange.Next3Days -> context.getString(R.string.widget_range_short_3)
        WidgetTimeRange.Next7Days -> context.getString(R.string.widget_range_short_7)
        WidgetTimeRange.Next14Days -> context.getString(R.string.widget_range_short_14)
    }
    return "${context.getString(categoryLabelRes(config.category))} · $range"
}
