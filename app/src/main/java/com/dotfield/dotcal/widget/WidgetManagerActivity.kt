package com.dotfield.dotcal.widget

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.dotfield.dotcal.prefs.CalendarPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private data class PlacedWidgetUi(
    val appWidgetId: Int,
    val label: String,
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
    val context = androidx.compose.ui.platform.LocalContext.current
    var widgets by remember { mutableStateOf<List<PlacedWidgetUi>>(emptyList()) }
    var loaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        widgets = withContext(Dispatchers.IO) { loadPlacedWidgets(context) }
        loaded = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
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
                "< Back",
                color = Color(0xFFFF3B30),
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.clickable(onClick = onBack),
            )
        }
        Column(modifier = Modifier.padding(horizontal = 22.dp)) {
            Text("Your Widgets", color = Color.White, fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(Modifier.height(6.dp))
            Text(
                "Tap a widget to customize what it shows.",
                color = Color(0xFFB3B3B3),
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(20.dp))
        }

        if (!loaded) {
            Spacer(Modifier.weight(1f))
            return@Column
        }

        if (widgets.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141414))
                    .padding(24.dp),
            ) {
                Text(
                    "No widgets yet",
                    color = Color.White,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Add a DotCal widget from your home screen, then customize it here.",
                    color = Color(0xFFB3B3B3),
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                )
            }
            Spacer(Modifier.weight(1f))
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 22.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(widgets.size) { index ->
                val widget = widgets[index]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF141414))
                        .clickable { onOpenWidget(widget.appWidgetId) }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                ) {
                    Text(
                        widget.label,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                    if (widget.summary.isNotBlank()) {
                        Spacer(Modifier.height(3.dp))
                        Text(
                            widget.summary,
                            color = Color(0xFFB3B3B3),
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

private suspend fun loadPlacedWidgets(context: Context): List<PlacedWidgetUi> {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val glanceManager = GlanceAppWidgetManager(context)
    val result = mutableListOf<PlacedWidgetUi>()
    for (className in PLACED_WIDGET_RECEIVER_CLASSES) {
        val componentName = ComponentName(context, "com.dotfield.dotcal.widget.$className")
        val ids = runCatching { appWidgetManager.getAppWidgetIds(componentName) }.getOrNull() ?: continue
        for (id in ids) {
            val label = appWidgetManager.getAppWidgetInfo(id)?.label.orEmpty().ifBlank { className }
            var summary = ""
            runCatching {
                val glanceId = glanceManager.getGlanceIdBy(id)
                val state = getAppWidgetState<Preferences>(context, PreferencesGlanceStateDefinition, glanceId)
                val encoded = state[CalendarPreferences.KEY_WIDGET_INSTANCE_CONFIG]
                if (encoded != null) summary = summarize(encoded)
            }
            result.add(PlacedWidgetUi(id, label, summary))
        }
    }
    return result.sortedWith(compareBy({ it.label }, { it.appWidgetId }))
}

private fun summarize(encoded: String): String {
    val config = WidgetInstanceConfig.decodeOrDefault(encoded, WidgetInstanceConfig(WidgetCategory.Schedule, WidgetViewType.Agenda))
    val range = when (config.timeRange) {
        WidgetTimeRange.Today -> "Today"
        WidgetTimeRange.Next24Hours -> "Next 24 hours"
        WidgetTimeRange.Next3Days -> "3 days"
        WidgetTimeRange.Next7Days -> "7 days"
        WidgetTimeRange.Next14Days -> "14 days"
    }
    return "${config.category.name} · $range"
}
