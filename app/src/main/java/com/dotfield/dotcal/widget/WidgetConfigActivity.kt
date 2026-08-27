package com.dotfield.dotcal.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.dotfield.dotcal.R
import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.DotCalDatabase
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import com.dotfield.dotcal.ui.DotCalPalette
import com.dotfield.dotcal.ui.DotCalSwitch
import com.dotfield.dotcal.ui.DotCalThemeMode
import com.dotfield.dotcal.ui.dotCalPalette
import com.dotfield.dotcal.ui.secondaryActionBorder
import com.dotfield.dotcal.ui.secondaryActionContainer
import com.dotfield.dotcal.ui.secondaryActionContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

internal val EditorAccent = Color(0xFFFF3B30)

/** Shared light/dark chrome palette for the widget config editor and manager screens. */
internal data class EditorPalette(val isDark: Boolean) {
    val ink get() = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
    val paper get() = if (isDark) Color(0xFFFFFFFF) else Color(0xFF101010)
    val muted get() = if (isDark) Color(0xFFB3B3B3) else Color(0xFF6B6B6B)
    val faint get() = if (isDark) Color(0xFF777777) else Color(0xFFA0A0A0)
    val panel get() = if (isDark) Color(0xFF141414) else Color(0xFFF4F4F4)
    val divider get() = if (isDark) Color(0xFF262626) else Color(0xFFECECEC)
    val sheetBg get() = if (isDark) Color(0xFF161616) else Color(0xFFFFFFFF)
}

internal fun resolveIsDark(context: android.content.Context, storedMode: String?): Boolean {
    val systemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    return when (storedMode) {
        "Light" -> false
        "Dark" -> true
        else -> systemDark
    }
}

class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var initialIsDark by mutableStateOf(true)

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

        lifecycleScope.launch {
            initialIsDark = resolveIsDark(
                this@WidgetConfigActivity,
                calendarPreferencesDataStore.data.first()[CalendarPreferences.KEY_THEME_MODE],
            )
        }

        setContent {
            WidgetConfigScreen(
                appWidgetId = appWidgetId,
                widgetLabel = resolveWidgetLabel(),
                initialIsDark = initialIsDark,
                onSave = { config -> saveConfig(config) },
                onCancel = { finish() },
                onRequestPro = { requestPro() },
            )
        }
    }

    private fun requestPro() {
        runCatching {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("dotcal://paywall")))
        }
    }

    private fun resolveWidgetLabel(): String {
        return AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)?.loadLabel(packageManager).orEmpty()
    }

    private fun saveConfig(config: WidgetInstanceConfig) {
        lifecycleScope.launch {
            val saved = runCatching {
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
            }.onFailure { Log.e(TAG, "Failed to persist config for widget $appWidgetId", it) }.isSuccess
            if (saved) {
                val receiverClass = AppWidgetManager.getInstance(this@WidgetConfigActivity)
                    .getAppWidgetInfo(appWidgetId)
                    ?.provider
                    ?.className
                    ?.substringAfterLast('.')
                    .orEmpty()
                runCatching { registerConfiguredWidget(this@WidgetConfigActivity, receiverClass, appWidgetId) }
                    .onFailure { Log.e(TAG, "Failed to register widget $appWidgetId", it) }
            }
            WidgetUpdateWorker.updateNow(this@WidgetConfigActivity)
            val result = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }

    companion object {
        private const val TAG = "WidgetConfigActivity"
    }
}

private enum class ConfigSheet {
    TimeRange,
    Calendar,
    Display,
    Density,
    Appearance,
    Advanced,
    TapAction,
}

internal data class WidgetProfile(
    val showContent: Boolean,
    val showTimeRange: Boolean,
    val showCalendar: Boolean,
    val showDisplay: Boolean,
    val showDensity: Boolean,
    val showTapAction: Boolean,
    val showAdvanced: Boolean,
    val categories: List<WidgetCategory>,
    val ranges: List<WidgetTimeRange>,
)

internal fun profileForKind(kind: LegacyWidgetKind): WidgetProfile {
    val allCategories = listOf(
        WidgetCategory.Schedule,
        WidgetCategory.Today,
        WidgetCategory.Tasks,
        WidgetCategory.Countdown,
        WidgetCategory.QuickActions,
    )
    val shortRanges = listOf(WidgetTimeRange.Today, WidgetTimeRange.Next3Days)
    val longRanges = listOf(
        WidgetTimeRange.Today,
        WidgetTimeRange.Next3Days,
        WidgetTimeRange.Next7Days,
        WidgetTimeRange.Next14Days,
    )
    return when (kind) {
        LegacyWidgetKind.DateOnly,
        LegacyWidgetKind.Countdown -> WidgetProfile(
            showContent = false,
            showTimeRange = false,
            showCalendar = false,
            showDisplay = false,
            showDensity = false,
            showTapAction = false,
            showAdvanced = false,
            categories = emptyList(),
            ranges = emptyList(),
        )
        LegacyWidgetKind.MonthCompact -> WidgetProfile(
            showContent = false,
            showTimeRange = false,
            showCalendar = true,
            showDisplay = false,
            showDensity = false,
            showTapAction = true,
            showAdvanced = false,
            categories = listOf(WidgetCategory.Calendar),
            ranges = emptyList(),
        )
        LegacyWidgetKind.Small -> WidgetProfile(
            showContent = true,
            showTimeRange = true,
            showCalendar = true,
            showDisplay = true,
            showDensity = false,
            showTapAction = true,
            showAdvanced = true,
            categories = allCategories,
            ranges = shortRanges,
        )
        LegacyWidgetKind.Medium,
        LegacyWidgetKind.Agenda -> WidgetProfile(
            showContent = true,
            showTimeRange = true,
            showCalendar = true,
            showDisplay = true,
            showDensity = true,
            showTapAction = true,
            showAdvanced = true,
            categories = allCategories,
            ranges = longRanges,
        )
        LegacyWidgetKind.Large,
        LegacyWidgetKind.MonthGrid -> WidgetProfile(
            showContent = true,
            showTimeRange = true,
            showCalendar = true,
            showDisplay = true,
            showDensity = true,
            showTapAction = true,
            showAdvanced = true,
            categories = listOf(WidgetCategory.Calendar) + allCategories,
            ranges = longRanges,
        )
        LegacyWidgetKind.ShiftWide -> WidgetProfile(
            showContent = false,
            showTimeRange = true,
            showCalendar = true,
            showDisplay = false,
            showDensity = true,
            showTapAction = false,
            showAdvanced = true,
            categories = listOf(WidgetCategory.Shift),
            ranges = longRanges,
        )
    }
}

/**
 * Free-tier save path: strips every Pro-gated override so a config saved while Pro
 * (or tampered state) cannot keep gated values after the entitlement is gone.
 */
internal fun sanitizeForFree(config: WidgetInstanceConfig): WidgetInstanceConfig {
    return config.copy(
        calendarFilter = WidgetCalendarFilter(null),
        timeRange = if (config.timeRange == WidgetTimeRange.Next14Days) {
            WidgetTimeRange.Next7Days
        } else {
            config.timeRange
        },
        density = if (config.density == WidgetContentDensity.High) WidgetContentDensity.Medium else config.density,
        interaction = config.interaction.copy(tapAction = WidgetTapAction.OpenCalendar),
        appearance = config.appearance.copy(
            accentColor = null,
            transparent = null,
            opacityPercent = null,
            showDotTexture = null,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WidgetConfigScreen(
    appWidgetId: Int,
    widgetLabel: String,
    initialIsDark: Boolean,
    onSave: (WidgetInstanceConfig) -> Unit,
    onCancel: () -> Unit,
    onRequestPro: () -> Unit,
) {
    val context = LocalContext.current
    val view = LocalView.current
    val haptics = LocalHapticFeedback.current
    var isDark by remember { mutableStateOf(initialIsDark) }
    val ui = remember(isDark) { EditorPalette(isDark) }
    val controlPalette = remember(isDark) {
        dotCalPalette(
            if (isDark) DotCalThemeMode.Dark else DotCalThemeMode.Light,
            systemDark = isDark,
        )
    }
    LaunchedEffect(isDark) {
        val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
    }
    var accounts by remember { mutableStateOf<List<CalendarAccount>>(emptyList()) }
    var isPro by remember { mutableStateOf(false) }
    var loaded by remember { mutableStateOf(false) }
    var kind by remember { mutableStateOf(LegacyWidgetKind.Medium) }
    var config by remember { mutableStateOf(WidgetInstanceConfig(WidgetCategory.Schedule, WidgetViewType.Agenda)) }
    var openSheet by remember { mutableStateOf<ConfigSheet?>(null) }
    var showColorPicker by remember { mutableStateOf(false) }
    val proBadge = stringResource(R.string.widget_config_badge_pro)

    LaunchedEffect(Unit) {
        val success = runCatching {
            val manager = GlanceAppWidgetManager(context)
            val glanceId = manager.getGlanceIdBy(appWidgetId)
            val state = getAppWidgetState<Preferences>(context, PreferencesGlanceStateDefinition, glanceId)
            val encoded = state[CalendarPreferences.KEY_WIDGET_INSTANCE_CONFIG]
            kind = legacyKindForAppWidget(appWidgetId, context)
            val profile = profileForKind(kind)
            var loadedConfig = WidgetInstanceConfig.decodeOrDefault(encoded, WidgetInstanceConfig.legacyDefault(kind))
            if (profile.ranges.isNotEmpty() && loadedConfig.timeRange !in profile.ranges) {
                loadedConfig = loadedConfig.copy(
                    timeRange = profile.ranges.minByOrNull { abs(it.days - loadedConfig.timeRange.days) }
                        ?: loadedConfig.timeRange,
                )
            }
            config = loadedConfig
            val prefs = context.calendarPreferencesDataStore.data.first()
            isPro = prefs[CalendarPreferences.KEY_IS_PRO] ?: false
            isDark = resolveIsDark(context, prefs[CalendarPreferences.KEY_THEME_MODE])
            accounts = withContext(Dispatchers.IO) {
                DotCalDatabase.create(context).calendarDao().getAccountsForWidgetConfig()
            }
        }.isSuccess
        if (!success) {
            (context as? Activity)?.finish()
        } else {
            loaded = true
        }
    }

    val profile = profileForKind(kind)
    val hasContentRows = profile.showTimeRange || profile.showCalendar || profile.showDisplay || profile.showDensity

    fun accountDisplayName(id: String): String =
        accounts.firstOrNull { it.id == id }?.let { it.displayName.ifBlank { it.accountName } } ?: id

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ui.ink)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
            Text(
                stringResource(R.string.widget_config_title),
                color = ui.paper,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
            if (widgetLabel.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(widgetLabel, color = ui.muted, fontFamily = FontFamily.SansSerif, fontSize = 13.sp)
            }
        }

        if (!loaded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = EditorAccent,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(26.dp),
                )
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp),
        ) {
            SectionHeader(ui, stringResource(R.string.widget_config_section_preview))
            WidgetPreviewCard(ui = ui, config = config, appIsDark = isDark)
            Spacer(Modifier.height(18.dp))
            SectionHeader(ui, stringResource(R.string.widget_config_section_type))
            if (profile.showContent) {
                WidgetTypeGrid(
                    ui = ui,
                    categories = profile.categories,
                    selected = config.category,
                    onSelect = { category ->
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        config = withCategory(config, category)
                    },
                )
            } else {
                Text(
                    stringResource(R.string.widget_config_type_fixed, stringResource(categoryLabelRes(config.category))),
                    color = ui.muted,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            if (hasContentRows) {
                SectionHeader(ui, stringResource(R.string.widget_config_section_content))
                ConfigPanel(ui) {
                    if (profile.showTimeRange) {
                        SheetRow(
                            ui = ui,
                            title = stringResource(R.string.widget_row_time_range),
                            value = timeRangeLabel(config.timeRange),
                            badge = if (!isPro && profile.ranges.contains(WidgetTimeRange.Next14Days)) proBadge else null,
                            onClick = { openSheet = ConfigSheet.TimeRange },
                        )
                        PanelDivider(ui)
                    }
                    if (profile.showCalendar) {
                        SheetRow(
                            ui = ui,
                            title = stringResource(R.string.widget_row_calendar),
                            value = config.calendarFilter.accountId?.let { accountDisplayName(it) }
                                ?: stringResource(R.string.widget_calendar_all),
                            badge = if (!isPro) proBadge else null,
                            onClick = { openSheet = ConfigSheet.Calendar },
                        )
                        PanelDivider(ui)
                    }
                    if (profile.showDisplay) {
                        SheetRow(
                            ui = ui,
                            title = stringResource(R.string.widget_row_display),
                            value = displaySummary(config),
                            onClick = { openSheet = ConfigSheet.Display },
                        )
                    }
                    if (profile.showDensity) {
                        SheetRow(
                            ui = ui,
                            title = stringResource(R.string.widget_row_density),
                            value = densityLabel(config.density),
                            badge = if (!isPro) proBadge else null,
                            onClick = { openSheet = ConfigSheet.Density },
                        )
                        PanelDivider(ui)
                    }
                }
                Spacer(Modifier.height(18.dp))
            }
            SectionHeader(ui, stringResource(R.string.widget_config_section_style))
            ConfigPanel(ui) {
                SheetRow(
                    ui = ui,
                    title = stringResource(R.string.widget_row_appearance),
                    value = appearanceSummary(config),
                    badge = if (!isPro) proBadge else null,
                    onClick = { openSheet = ConfigSheet.Appearance },
                )
                if (profile.showAdvanced) {
                    PanelDivider(ui)
                    SheetRow(
                        ui = ui,
                        title = stringResource(R.string.widget_row_advanced),
                        value = advancedSummary(config),
                        badge = if (!isPro) proBadge else null,
                        onClick = { openSheet = ConfigSheet.Advanced },
                    )
                }
                if (profile.showTapAction) {
                    PanelDivider(ui)
                    SheetRow(
                        ui = ui,
                        title = stringResource(R.string.widget_row_tap_action),
                        value = tapActionLabel(config.interaction.tapAction),
                        badge = if (!isPro) proBadge else null,
                        onClick = { openSheet = ConfigSheet.TapAction },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 16.dp),
        ) {
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    config = WidgetInstanceConfig.legacyDefault(kind)
                },
                border = secondaryActionBorder(controlPalette),
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondaryActionContainer(controlPalette),
                    contentColor = secondaryActionContent(controlPalette),
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(stringResource(R.string.widget_config_reset), maxLines = 1)
            }
            Button(
                onClick = onCancel,
                border = secondaryActionBorder(controlPalette),
                colors = ButtonDefaults.buttonColors(
                    containerColor = secondaryActionContainer(controlPalette),
                    contentColor = secondaryActionContent(controlPalette),
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(stringResource(R.string.widget_config_cancel), maxLines = 1)
            }
            Button(
                onClick = {
                    val safe = if (isPro) config else sanitizeForFree(config)
                    onSave(safe)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = controlPalette.accent,
                    contentColor = controlPalette.onAccent,
                ),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(stringResource(R.string.widget_config_save), maxLines = 1)
            }
        }
    }

    when (openSheet) {
        ConfigSheet.TimeRange -> ConfigBottomSheet(
            ui = ui,
            title = stringResource(R.string.widget_row_time_range),
            onDismiss = { openSheet = null },
        ) {
            timeRangeOptions().filter { it.first in profile.ranges }.forEach { (range, labelRes) ->
                val locked = !isPro && range == WidgetTimeRange.Next14Days
                RadioRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(labelRes),
                    badge = if (locked) proBadge else null,
                    selected = config.timeRange == range,
                    enabled = true,
                    onClick = {
                        if (locked) {
                            openSheet = null
                            onRequestPro()
                        } else {
                            config = config.copy(timeRange = range)
                            openSheet = null
                        }
                    },
                )
            }
        }
        ConfigSheet.Calendar -> ConfigBottomSheet(
            ui = ui,
            title = stringResource(R.string.widget_row_calendar),
            onDismiss = { openSheet = null },
        ) {
            RadioRow(
                ui = ui,
                controlPalette = controlPalette,
                title = stringResource(R.string.widget_calendar_all),
                subtitle = stringResource(R.string.widget_calendar_all_subtitle),
                selected = config.calendarFilter.accountId == null,
                onClick = {
                    config = config.copy(calendarFilter = WidgetCalendarFilter(null))
                    openSheet = null
                },
            )
            accounts.forEach { account ->
                val locked = !isPro
                RadioRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = account.displayName.ifBlank { account.accountName },
                    subtitle = if (locked) stringResource(R.string.widget_calendar_pro_hint) else account.accountType,
                    badge = if (locked) proBadge else null,
                    selected = config.calendarFilter.accountId == account.id,
                    onClick = {
                        if (locked) {
                            openSheet = null
                            onRequestPro()
                        } else {
                            config = config.copy(calendarFilter = WidgetCalendarFilter(account.id))
                            openSheet = null
                        }
                    },
                )
            }
        }
        ConfigSheet.Display -> ConfigBottomSheet(
            ui = ui,
            title = stringResource(R.string.widget_row_display),
            onDismiss = { openSheet = null },
        ) {
            ToggleRow(
                ui = ui,
                controlPalette = controlPalette,
                title = stringResource(R.string.widget_display_show_time),
                checked = config.content.showTime,
                onChange = { config = config.copy(content = config.content.copy(showTime = it)) },
            )
            if (config.category == WidgetCategory.Schedule || config.category == WidgetCategory.Calendar) {
                ToggleRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(R.string.widget_display_show_location),
                    checked = config.content.showLocation,
                    onChange = { config = config.copy(content = config.content.copy(showLocation = it)) },
                )
                ToggleRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(R.string.widget_display_colorize),
                    subtitle = stringResource(R.string.widget_display_colorize_sub),
                    checked = config.content.showEventColors,
                    onChange = { config = config.copy(content = config.content.copy(showEventColors = it)) },
                )
                ToggleRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(R.string.widget_display_show_titles),
                    checked = config.content.showEventTitle,
                    onChange = { config = config.copy(content = config.content.copy(showEventTitle = it)) },
                )
            }
            if (config.category == WidgetCategory.Calendar || config.category == WidgetCategory.Schedule) {
                ToggleRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(R.string.widget_display_allday),
                    checked = config.content.showAllDayEvents,
                    onChange = { config = config.copy(content = config.content.copy(showAllDayEvents = it)) },
                )
            }
            if (config.category == WidgetCategory.Today || config.category == WidgetCategory.Tasks) {
                ToggleRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(R.string.widget_display_tasks),
                    checked = config.content.showTasks,
                    onChange = { config = config.copy(content = config.content.copy(showTasks = it)) },
                )
            }
            if (config.category == WidgetCategory.Tasks) {
                ToggleRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(R.string.widget_display_completed),
                    checked = config.content.showCompletedTasks,
                    onChange = { config = config.copy(content = config.content.copy(showCompletedTasks = it)) },
                )
            }
            if (config.category == WidgetCategory.Calendar) {
                ToggleRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(R.string.widget_display_today_highlight),
                    checked = config.content.showTodayHighlight,
                    onChange = { config = config.copy(content = config.content.copy(showTodayHighlight = it)) },
                )
                ToggleRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(R.string.widget_display_week_numbers),
                    checked = config.content.showWeekNumbers,
                    onChange = { config = config.copy(content = config.content.copy(showWeekNumbers = it)) },
                )
            }
        }
        ConfigSheet.Density -> ConfigBottomSheet(
            ui = ui,
            title = stringResource(R.string.widget_row_density),
            onDismiss = { openSheet = null },
        ) {
            listOf(
                WidgetContentDensity.Low to R.string.widget_density_low,
                WidgetContentDensity.Medium to R.string.widget_density_medium,
                WidgetContentDensity.High to R.string.widget_density_high,
            ).forEach { (density, labelRes) ->
                val locked = !isPro && density == WidgetContentDensity.High
                RadioRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(labelRes),
                    badge = if (locked) proBadge else null,
                    selected = config.density == density,
                    onClick = {
                        if (locked) {
                            openSheet = null
                            onRequestPro()
                        } else {
                            config = config.copy(density = density)
                            openSheet = null
                        }
                    },
                )
            }
        }
        ConfigSheet.Appearance -> ConfigBottomSheet(
            ui = ui,
            title = stringResource(R.string.widget_row_appearance),
            onDismiss = { openSheet = null },
        ) {
            listOf(
                null to stringResource(R.string.widget_appearance_follow_theme),
                "Light" to stringResource(R.string.widget_appearance_light),
                "Dark" to stringResource(R.string.widget_appearance_dark),
            ).forEach { (mode, label) ->
                RadioRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = label,
                    selected = (config.appearance.themeMode ?: "System") == (mode ?: "System"),
                    onClick = {
                        config = config.copy(appearance = config.appearance.copy(themeMode = mode))
                    },
                )
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ui.divider),
            )
            Spacer(Modifier.height(14.dp))
            SectionHeader(ui, stringResource(R.string.widget_section_accent), topPadding = true)
            accentOptions().forEach { (value, labelRes) ->
                val locked = !isPro && value != null
                RadioRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(labelRes),
                    badge = if (locked) proBadge else null,
                    selected = config.appearance.accentColor == value ||
                        (value == null && config.appearance.accentColor == null),
                    onClick = {
                        if (locked) {
                            openSheet = null
                            onRequestPro()
                        } else {
                            config = config.copy(appearance = config.appearance.copy(accentColor = value))
                        }
                    },
                )
            }
            PanelDivider(ui)
            RadioRow(
                ui = ui,
                controlPalette = controlPalette,
                title = stringResource(R.string.widget_accent_custom),
                subtitle = config.appearance.accentColor?.takeIf { it.startsWith("#") }?.uppercase()
                    ?: stringResource(R.string.widget_accent_custom_hint),
                badge = if (!isPro) proBadge else null,
                selected = config.appearance.accentColor?.startsWith("#") == true,
                onClick = {
                    if (!isPro) {
                        openSheet = null
                        onRequestPro()
                    } else {
                        showColorPicker = true
                    }
                },
            )
        }
        ConfigSheet.Advanced -> ConfigBottomSheet(
            ui = ui,
            title = stringResource(R.string.widget_row_advanced),
            onDismiss = { openSheet = null },
        ) {
            SectionHeader(ui, stringResource(R.string.widget_section_layout))
            listOf(
                WidgetLayoutMode.Minimal to R.string.widget_layout_minimal,
                WidgetLayoutMode.Compact to R.string.widget_layout_compact,
                WidgetLayoutMode.Detailed to R.string.widget_layout_detailed,
            ).forEach { (mode, labelRes) ->
                RadioRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(labelRes),
                    selected = config.layoutMode == mode,
                    onClick = { config = config.copy(layoutMode = mode) },
                )
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ui.divider),
            )
            Spacer(Modifier.height(14.dp))
            SectionHeader(ui, stringResource(R.string.widget_section_background))
            listOf(
                -1 to R.string.widget_background_follow,
                0 to R.string.widget_background_solid,
                1 to R.string.widget_background_transparent,
            ).forEach { (value, labelRes) ->
                val locked = !isPro && value != -1
                RadioRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(labelRes),
                    badge = if (locked) proBadge else null,
                    selected = when (value) {
                        -1 -> config.appearance.transparent == null
                        0 -> config.appearance.transparent == false
                        else -> config.appearance.transparent == true
                    },
                    onClick = {
                        if (locked) {
                            openSheet = null
                            onRequestPro()
                        } else {
                            config = config.copy(
                                appearance = config.appearance.copy(
                                    transparent = when (value) {
                                        -1 -> null
                                        else -> value == 1
                                    },
                                    opacityPercent = if (value == 1 && config.appearance.opacityPercent == null) 50 else config.appearance.opacityPercent,
                                ),
                            )
                        }
                    },
                )
            }
            if (config.appearance.transparent == true) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        stringResource(R.string.widget_opacity_label, config.appearance.opacityPercent ?: 50),
                        color = ui.muted,
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 13.sp,
                    )
                    Slider(
                        value = (config.appearance.opacityPercent ?: 50).toFloat(),
                        onValueChange = {
                            config = config.copy(appearance = config.appearance.copy(opacityPercent = it.toInt()))
                        },
                        valueRange = 0f..100f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = EditorAccent,
                            activeTrackColor = EditorAccent,
                            inactiveTrackColor = ui.divider,
                        ),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ui.divider),
            )
            Spacer(Modifier.height(14.dp))
            SectionHeader(ui, stringResource(R.string.widget_section_texture))
            listOf(
                -1 to R.string.widget_background_follow,
                1 to R.string.widget_texture_on,
                0 to R.string.widget_texture_off,
            ).forEach { (value, labelRes) ->
                val locked = !isPro && value != -1
                RadioRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = stringResource(labelRes),
                    badge = if (locked) proBadge else null,
                    selected = when (value) {
                        -1 -> config.appearance.showDotTexture == null
                        else -> config.appearance.showDotTexture == (value == 1)
                    },
                    onClick = {
                        if (locked) {
                            openSheet = null
                            onRequestPro()
                        } else {
                            config = config.copy(
                                appearance = config.appearance.copy(
                                    showDotTexture = if (value == -1) null else value == 1,
                                ),
                            )
                        }
                    },
                )
            }
        }
        ConfigSheet.TapAction -> ConfigBottomSheet(
            ui = ui,
            title = stringResource(R.string.widget_row_tap_action),
            onDismiss = { openSheet = null },
        ) {
            tapActionOptions().forEach { action ->
                val locked = !isPro && action != WidgetTapAction.OpenCalendar
                RadioRow(
                    ui = ui,
                    controlPalette = controlPalette,
                    title = tapActionLabel(action),
                    badge = if (locked) proBadge else null,
                    selected = config.interaction.tapAction == action,
                    onClick = {
                        if (locked) {
                            openSheet = null
                            onRequestPro()
                        } else {
                            config = config.copy(interaction = config.interaction.copy(tapAction = action))
                            openSheet = null
                        }
                    },
                )
            }
        }
        null -> {}
    }

    if (showColorPicker) {
        val initialColor = runCatching {
            Color(android.graphics.Color.parseColor(config.appearance.accentColor?.takeIf { it.startsWith("#") } ?: "#FF3B30"))
        }.getOrDefault(EditorAccent)
        com.dotfield.dotcal.ui.CustomAccentPickerDialog(
            initial = initialColor,
            palette = controlPalette,
            title = stringResource(R.string.widget_accent_picker_title),
            onDismiss = { showColorPicker = false },
            onConfirm = { hex ->
                config = config.copy(appearance = config.appearance.copy(accentColor = hex))
                showColorPicker = false
                openSheet = ConfigSheet.Appearance
            },
        )
    }
}

/** Static mock of how the widget will look with the current config applied. */
@Composable
private fun WidgetPreviewCard(ui: EditorPalette, config: WidgetInstanceConfig, appIsDark: Boolean) {
    val dark = when (config.appearance.themeMode) {
        "Light" -> false
        "Dark" -> true
        else -> appIsDark
    }
    val accent = remember(config.appearance.accentColor) { widgetAccentColor(config.appearance.accentColor) }
    val transparent = config.appearance.transparent ?: false
    val opacity = (config.appearance.opacityPercent ?: 50) / 100f
    val baseBg = if (dark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val bg = if (transparent) baseBg.copy(alpha = opacity) else baseBg
    val fg = if (dark) Color(0xFFFFFFFF) else Color(0xFF101010)
    val rowCount = config.maxVisibleItems(3).coerceIn(1, 4)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(width = 1.dp, color = ui.divider, shape = RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column {
            Text(
                stringResource(categoryLabelRes(config.category)),
                color = fg,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                timeRangeLabel(config.timeRange),
                color = fg.copy(alpha = 0.55f),
                fontFamily = FontFamily.SansSerif,
                fontSize = 10.sp,
            )
            Spacer(Modifier.height(10.dp))
            repeat(rowCount) { row ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 5.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accent),
                    )
                    Spacer(Modifier.width(9.dp))
                    Column {
                        Box(
                            modifier = Modifier
                                .size(height = 6.dp, width = when (config.category) {
                                    WidgetCategory.Calendar -> if (row == 0) 118.dp else 88.dp
                                    WidgetCategory.Countdown -> if (row == 0) 92.dp else 58.dp
                                    WidgetCategory.Shift -> if (row == 0) 126.dp else 102.dp
                                    WidgetCategory.Tasks -> if (row == 0) 116.dp else 92.dp
                                    else -> if (row == 0) 130.dp else 96.dp
                                })
                                .clip(RoundedCornerShape(3.dp))
                                .background(fg.copy(alpha = 0.55f)),
                        )
                        Spacer(Modifier.height(5.dp))
                        Box(
                            modifier = Modifier
                                .size(height = 5.dp, width = 58.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        config.content.showLocation -> fg.copy(alpha = 0.28f)
                                        config.content.showTasks -> fg.copy(alpha = 0.34f)
                                        config.content.showEventTitle -> fg.copy(alpha = 0.22f)
                                        else -> fg.copy(alpha = 0.16f)
                                    },
                                ),
                        )
                    }
                }
            }
        }
    }
}

private fun accentOptions(): List<Pair<String?, Int>> = listOf(
    null to R.string.widget_accent_follow,
    "RED" to R.string.widget_accent_red,
    "BLUE" to R.string.widget_accent_blue,
    "GREEN" to R.string.widget_accent_green,
    "PURPLE" to R.string.widget_accent_purple,
    "AMBER" to R.string.widget_accent_amber,
    "TEAL" to R.string.widget_accent_teal,
    "PINK" to R.string.widget_accent_pink,
    "ORANGE" to R.string.widget_accent_orange,
    "CYAN" to R.string.widget_accent_cyan,
)

@Composable
private fun advancedSummary(config: WidgetInstanceConfig): String {
    val layout = stringResource(
        when (config.layoutMode) {
            WidgetLayoutMode.Minimal -> R.string.widget_layout_minimal
            WidgetLayoutMode.Compact -> R.string.widget_layout_compact
            WidgetLayoutMode.Detailed -> R.string.widget_layout_detailed
        },
    )
    val bg = when (config.appearance.transparent) {
        null -> stringResource(R.string.widget_background_follow)
        false -> stringResource(R.string.widget_background_solid)
        else -> stringResource(R.string.widget_summary_percent, config.appearance.opacityPercent ?: 50)
    }
    return "$layout · $bg"
}

@Composable
private fun appearanceSummary(config: WidgetInstanceConfig): String {
    val theme = when (config.appearance.themeMode) {
        "Light" -> stringResource(R.string.widget_appearance_light)
        "Dark" -> stringResource(R.string.widget_appearance_dark)
        else -> stringResource(R.string.widget_summary_app_theme)
    }
    val accent = when {
        config.appearance.accentColor == null -> stringResource(R.string.widget_summary_default)
        config.appearance.accentColor?.startsWith("#") == true -> stringResource(R.string.widget_summary_custom)
        else -> stringResource(accentOptions().firstOrNull { it.first == config.appearance.accentColor }?.second ?: R.string.widget_summary_default)
    }
    return "$theme · $accent"
}

internal fun categoryLabelRes(category: WidgetCategory): Int = when (category) {
    WidgetCategory.Calendar -> R.string.widget_cat_month
    WidgetCategory.Schedule -> R.string.widget_cat_schedule
    WidgetCategory.Today -> R.string.widget_cat_today
    WidgetCategory.Tasks -> R.string.widget_cat_tasks
    WidgetCategory.Countdown -> R.string.widget_cat_countdown
    WidgetCategory.Shift -> R.string.widget_cat_shift
    WidgetCategory.QuickActions -> R.string.widget_cat_quick_actions
}

private fun categorySubtitleRes(category: WidgetCategory): Int = when (category) {
    WidgetCategory.Calendar -> R.string.widget_sub_month
    WidgetCategory.Schedule -> R.string.widget_sub_schedule
    WidgetCategory.Today -> R.string.widget_sub_today
    WidgetCategory.Tasks -> R.string.widget_sub_tasks
    WidgetCategory.Countdown -> R.string.widget_sub_countdown
    WidgetCategory.Shift -> R.string.widget_sub_shift
    WidgetCategory.QuickActions -> R.string.widget_sub_quick
}

private fun timeRangeOptions(): List<Pair<WidgetTimeRange, Int>> = listOf(
    WidgetTimeRange.Today to R.string.widget_range_today,
    WidgetTimeRange.Next3Days to R.string.widget_range_3days,
    WidgetTimeRange.Next7Days to R.string.widget_range_7days,
    WidgetTimeRange.Next14Days to R.string.widget_range_14days,
)

@Composable
private fun timeRangeLabel(range: WidgetTimeRange): String =
    stringResource(timeRangeOptions().firstOrNull { it.first == range }?.second ?: R.string.widget_range_today)

@Composable
private fun densityLabel(density: WidgetContentDensity): String = stringResource(
    when (density) {
        WidgetContentDensity.Low -> R.string.widget_density_low
        WidgetContentDensity.Medium -> R.string.widget_density_medium
        WidgetContentDensity.High -> R.string.widget_density_high
    },
)

private fun tapActionOptions(): List<WidgetTapAction> = listOf(
    WidgetTapAction.OpenCalendar,
    WidgetTapAction.OpenToday,
    WidgetTapAction.OpenAgenda,
    WidgetTapAction.QuickAdd,
    WidgetTapAction.CreateEvent,
    WidgetTapAction.CreateTask,
    WidgetTapAction.Search,
)

@Composable
private fun tapActionLabel(action: WidgetTapAction): String = stringResource(
    when (action) {
        WidgetTapAction.OpenCalendar -> R.string.widget_tap_open_calendar
        WidgetTapAction.OpenToday -> R.string.widget_tap_open_today
        WidgetTapAction.OpenAgenda -> R.string.widget_tap_open_agenda
        WidgetTapAction.QuickAdd -> R.string.widget_tap_quick_add
        WidgetTapAction.CreateEvent -> R.string.widget_tap_new_event
        WidgetTapAction.CreateTask -> R.string.widget_tap_new_task
        WidgetTapAction.Search -> R.string.widget_tap_search
    },
)

@Composable
private fun displaySummary(config: WidgetInstanceConfig): String {
    val parts = mutableListOf<String>()
    if (config.content.showTime) parts.add(stringResource(R.string.widget_summary_time))
    if (config.content.showLocation) parts.add(stringResource(R.string.widget_summary_location))
    if (config.content.showTasks) parts.add(stringResource(R.string.widget_display_tasks))
    if (config.content.showCompletedTasks) parts.add(stringResource(R.string.widget_display_completed))
    return if (parts.isEmpty()) stringResource(R.string.widget_summary_basic) else parts.joinToString(", ")
}

private fun withCategory(config: WidgetInstanceConfig, category: WidgetCategory): WidgetInstanceConfig {
    val viewType = when (category) {
        WidgetCategory.Calendar -> WidgetViewType.Month
        WidgetCategory.Schedule -> WidgetViewType.Agenda
        WidgetCategory.Today -> WidgetViewType.Today
        WidgetCategory.Tasks -> WidgetViewType.TaskList
        WidgetCategory.Countdown -> WidgetViewType.Countdown
        WidgetCategory.Shift -> WidgetViewType.ShiftList
        WidgetCategory.QuickActions -> WidgetViewType.QuickAction
    }
    return config.copy(category = category, viewType = viewType)
}

internal fun legacyKindForAppWidget(
    appWidgetId: Int,
    context: android.content.Context,
): LegacyWidgetKind {
    val providerName = AppWidgetManager.getInstance(context)
        .getAppWidgetInfo(appWidgetId)
        ?.provider
        ?.className
        .orEmpty()
    return when {
        providerName.endsWith("DateOnlyDotCalWidgetReceiver") -> LegacyWidgetKind.DateOnly
        providerName.endsWith("CompactMonthDotCalWidgetReceiver") -> LegacyWidgetKind.MonthCompact
        providerName.endsWith("ShiftWideWidgetReceiver") -> LegacyWidgetKind.ShiftWide
        providerName.endsWith("SmallDotCalWidgetReceiver") -> LegacyWidgetKind.Small
        providerName.endsWith("MediumDotCalWidgetReceiver") -> LegacyWidgetKind.Medium
        providerName.endsWith("LargeDotCalWidgetReceiver") -> LegacyWidgetKind.Large
        providerName.endsWith("EventCountdownWidgetReceiver") -> LegacyWidgetKind.Countdown
        providerName.endsWith("AgendaListWidgetReceiver") -> LegacyWidgetKind.Agenda
        providerName.endsWith("MonthGridWidgetReceiver") -> LegacyWidgetKind.MonthGrid
        else -> LegacyWidgetKind.Medium
    }
}

@Composable
private fun WidgetTypeGrid(
    ui: EditorPalette,
    categories: List<WidgetCategory>,
    selected: WidgetCategory,
    onSelect: (WidgetCategory) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { category ->
                    val isSelected = category == selected
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) EditorAccent.copy(alpha = 0.12f) else ui.panel)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = EditorAccent,
                                shape = RoundedCornerShape(14.dp),
                            )
                            .clickable { onSelect(category) }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                    ) {
                        Text(
                            stringResource(categoryLabelRes(category)),
                            color = if (isSelected) EditorAccent else ui.paper,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                        Text(
                            stringResource(categorySubtitleRes(category)),
                            color = ui.muted,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 11.sp,
                            maxLines = 1,
                        )
                    }
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(ui: EditorPalette, title: String, topPadding: Boolean = false) {
    Text(
        title,
        color = ui.faint,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = if (topPadding) 12.dp else 0.dp, bottom = 8.dp),
    )
}

@Composable
private fun ConfigPanel(ui: EditorPalette, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ui.panel)
            .padding(vertical = 4.dp),
    ) {
        content()
    }
}

@Composable
private fun PanelDivider(ui: EditorPalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(1.dp)
            .background(ui.divider),
    )
}

@Composable
private fun ProBadge(text: String, ui: EditorPalette) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(EditorAccent.copy(alpha = 0.16f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
    ) {
        Text(
            text,
            color = EditorAccent,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            letterSpacing = 1.sp,
        )
    }
}

@Composable
private fun SheetRow(
    ui: EditorPalette,
    title: String,
    value: String,
    badge: String? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            color = ui.paper,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            modifier = Modifier.weight(0.34f),
        )
        Text(
            value,
            color = ui.muted,
            fontFamily = FontFamily.SansSerif,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(0.5f),
        )
        if (badge != null) {
            ProBadge(badge, ui)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigBottomSheet(
    ui: EditorPalette,
    title: String,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ui.sheetBg,
        tonalElevation = 0.dp,
    ) {
            Column(modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 28.dp)) {
            Text(
                title,
                color = ui.faint,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                content()
            }
        }
    }
}

@Composable
private fun ToggleRow(
    ui: EditorPalette,
    controlPalette: DotCalPalette,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = ui.paper,
                fontFamily = FontFamily.SansSerif,
                fontSize = 15.sp,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    color = ui.muted,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                )
            }
        }
        com.dotfield.dotcal.ui.DotCalSwitch(
            checked = checked,
            palette = controlPalette,
            onCheckedChange = onChange,
        )
    }
}

@Composable
private fun RadioRow(
    ui: EditorPalette,
    controlPalette: DotCalPalette,
    title: String,
    subtitle: String? = null,
    badge: String? = null,
    selected: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    color = ui.paper,
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                )
                if (badge != null) {
                    Spacer(Modifier.width(8.dp))
                    ProBadge(badge, ui)
                }
            }
            if (!subtitle.isNullOrBlank()) {
                Text(
                    subtitle,
                    color = ui.muted,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 12.sp,
                )
            }
        }
        RadioButton(
            selected = selected,
            onClick = null,
            enabled = enabled,
            colors = RadioButtonDefaults.colors(
                selectedColor = controlPalette.accent,
                unselectedColor = controlPalette.secondaryText,
            ),
        )
    }
}
