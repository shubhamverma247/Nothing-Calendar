package com.dotfield.dotcal.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.datastore.preferences.core.edit
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventEditorData
import com.dotfield.dotcal.data.baseEventId
import com.dotfield.dotcal.data.isRecurrenceOccurrence
import com.dotfield.dotcal.data.RecurringEditScope
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import com.dotfield.dotcal.ui.theme.NBlack
import com.dotfield.dotcal.ui.theme.NRed
import com.dotfield.dotcal.ui.theme.NWhite
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

private val mono = FontFamily.SansSerif
private val sheetDateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM", Locale.US)
private val compactDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
private val editorDateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM, yyyy", Locale.US)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
private val editorTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
private const val WEEK_HOUR_HEIGHT_DP = 64f
private const val BOOT_PREFS = "dotcal_boot"
private const val BOOT_THEME_KEY = "theme_mode"
private val reminderOptions = listOf(null, 5, 10, 30)
private data class RecurrenceOption(val label: String, val rrule: String?)
private val recurrenceOptions = listOf(
    RecurrenceOption("None", null),
    RecurrenceOption("Daily", "FREQ=DAILY"),
    RecurrenceOption("Weekly", "FREQ=WEEKLY"),
    RecurrenceOption("Monthly", "FREQ=MONTHLY"),
)
private enum class DateTimeField { Start, End }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DotCalApp(viewModel: DotCalViewModel) {
    val month by viewModel.month.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    var screenTab by remember { mutableStateOf(ScreenTab.Calendar) }
    var previousScreenTab by remember { mutableStateOf(ScreenTab.Calendar) }
    var showSheet by remember { mutableStateOf(false) }
    var addSheet by remember { mutableStateOf(false) }
    var addStartTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var settingsScreen by remember { mutableStateOf(SettingsScreen.Root) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bootPreferences = remember(context) { context.getSharedPreferences(BOOT_PREFS, android.content.Context.MODE_PRIVATE) }
    val bootThemeMode = remember(bootPreferences) {
        DotCalThemeMode.fromStorage(bootPreferences.getString(BOOT_THEME_KEY, null))
    }
    val themeMode by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            DotCalThemeMode.fromStorage(preferences[CalendarPreferences.KEY_THEME_MODE])
        }
    }.collectAsState(initial = bootThemeMode)
    val storedCalendarTab by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            CalendarTab.fromStorage(preferences[CalendarPreferences.KEY_DEFAULT_VIEW])
        }
    }.collectAsState(initial = CalendarTab.Month)
    var calendarTab by remember { mutableStateOf<CalendarTab?>(null) }
    LaunchedEffect(storedCalendarTab) {
        if (calendarTab == null) calendarTab = storedCalendarTab
    }
    val activeCalendarTab = calendarTab ?: storedCalendarTab
    val systemDark = isSystemInDarkTheme()
    val resolvedThemeMode = themeMode
    val palette = remember(resolvedThemeMode, systemDark) { resolvedThemeMode?.let { dotCalPalette(it, systemDark) } ?: dotCalBootPalette() }
    LaunchedEffect(resolvedThemeMode) {
        resolvedThemeMode?.let { mode ->
            bootPreferences.edit().putString(BOOT_THEME_KEY, mode.name).apply()
        }
    }
    fun selectCalendarTab(tab: CalendarTab) {
        calendarTab = tab
        scope.launch {
            context.calendarPreferencesDataStore.edit { preferences ->
                preferences[CalendarPreferences.KEY_DEFAULT_VIEW] = tab.name
            }
        }
    }
    fun closeTopSurface() {
        when {
            addSheet -> {
                editingEvent = null
                addSheet = false
            }
            screenTab == ScreenTab.Settings && settingsScreen != SettingsScreen.Root -> {
                settingsScreen = SettingsScreen.Root
            }
            screenTab == ScreenTab.Settings -> {
                settingsScreen = SettingsScreen.Root
                screenTab = previousScreenTab
            }
            screenTab == ScreenTab.Tasks -> {
                screenTab = ScreenTab.Calendar
                previousScreenTab = ScreenTab.Calendar
            }
        }
    }
    BackHandler(enabled = addSheet || screenTab == ScreenTab.Settings || screenTab == ScreenTab.Tasks) {
        closeTopSurface()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val visibleMainTab = if (screenTab == ScreenTab.Settings) previousScreenTab else screenTab
        val calendarHeaderLabel = when (activeCalendarTab) {
            CalendarTab.Month -> "${month.year}/${month.monthValue}"
            CalendarTab.Week,
            CalendarTab.Day,
            CalendarTab.ThreeDay,
            CalendarTab.Agenda -> "${selectedDate.year}/${selectedDate.monthValue}"
            CalendarTab.Year -> selectedDate.year.toString()
        }
        Scaffold(
            containerColor = palette.background,
            bottomBar = {
                DotCalBottomNav(
                    selected = screenTab,
                    palette = palette,
                    onCalendar = {
                        settingsScreen = SettingsScreen.Root
                        screenTab = ScreenTab.Calendar
                        previousScreenTab = ScreenTab.Calendar
                    },
                    onTasks = {
                        settingsScreen = SettingsScreen.Root
                        previousScreenTab = ScreenTab.Calendar
                        screenTab = ScreenTab.Tasks
                    },
                    onSettings = {
                        previousScreenTab = if (screenTab == ScreenTab.Settings) previousScreenTab else screenTab
                        settingsScreen = SettingsScreen.Root
                        screenTab = ScreenTab.Settings
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.background)
                    .padding(padding),
            ) {
                CalendarActionBar(
                    title = if (visibleMainTab == ScreenTab.Calendar) calendarHeaderLabel else visibleMainTab.name,
                    palette = palette,
                    onTitleClick = {
                        if (visibleMainTab == ScreenTab.Calendar) viewModel.selectDate(LocalDate.now())
                    },
                    onAdd = {
                        addStartTime = LocalTime.of(9, 0)
                        editingEvent = null
                        addSheet = true
                    },
                )
                if (visibleMainTab == ScreenTab.Calendar) {
                    CalendarViewSegmentedControl(
                        selected = activeCalendarTab,
                        palette = palette,
                        onSelected = {
                            screenTab = ScreenTab.Calendar
                            previousScreenTab = ScreenTab.Calendar
                            selectCalendarTab(it)
                        },
                    )
                }
                when (visibleMainTab) {
                ScreenTab.Calendar -> {
                    when (activeCalendarTab) {
                        CalendarTab.Month -> MonthView(
                            month = month,
                            selectedDate = selectedDate,
                            events = events,
                            palette = palette,
                            onPrevious = viewModel::previousMonth,
                            onNext = viewModel::nextMonth,
                            onJumpToday = { viewModel.selectDate(LocalDate.now()) },
                            onDateSelected = {
                                viewModel.selectDate(it)
                                showSheet = true
                            },
                        )
                        CalendarTab.Week -> WeekView(
                            selectedDate = selectedDate,
                            events = events,
                            palette = palette,
                            onPreviousWeek = { viewModel.selectDate(selectedDate.minusWeeks(1)) },
                            onNextWeek = { viewModel.selectDate(selectedDate.plusWeeks(1)) },
                            onJumpToday = { viewModel.selectDate(LocalDate.now()) },
                            onDateSelected = viewModel::selectDate,
                            onAddAtDate = { date, time ->
                                viewModel.selectDate(date)
                                addStartTime = time
                                editingEvent = null
                                addSheet = true
                            },
                        )
                        CalendarTab.Day -> DayView(
                            selectedDate = selectedDate,
                            events = events,
                            palette = palette,
                            onPreviousDay = { viewModel.selectDate(selectedDate.minusDays(1)) },
                            onNextDay = { viewModel.selectDate(selectedDate.plusDays(1)) },
                            onJumpToday = { viewModel.selectDate(LocalDate.now()) },
                            onAddAtDate = { date, time ->
                                viewModel.selectDate(date)
                                addStartTime = time
                                editingEvent = null
                                addSheet = true
                            },
                        )
                        CalendarTab.ThreeDay -> ThreeDayView(
                            selectedDate = selectedDate,
                            events = events,
                            palette = palette,
                            onPreviousRange = { viewModel.selectDate(selectedDate.minusDays(3)) },
                            onNextRange = { viewModel.selectDate(selectedDate.plusDays(3)) },
                            onJumpToday = { viewModel.selectDate(LocalDate.now()) },
                            onDateSelected = viewModel::selectDate,
                            onAddAtDate = { date, time ->
                                viewModel.selectDate(date)
                                addStartTime = time
                                editingEvent = null
                                addSheet = true
                            },
                        )
                        CalendarTab.Agenda -> AgendaPreview(
                            selectedDate = selectedDate,
                            events = events,
                            palette = palette,
                            onJumpToday = { viewModel.selectDate(LocalDate.now()) },
                        )
                        CalendarTab.Year -> YearView(
                            selectedDate = selectedDate,
                            events = events,
                            palette = palette,
                            onPreviousYear = { viewModel.selectDate(selectedDate.minusYears(1)) },
                            onNextYear = { viewModel.selectDate(selectedDate.plusYears(1)) },
                            onJumpToday = { viewModel.selectDate(LocalDate.now()) },
                            onMonthSelected = {
                                viewModel.selectDate(it)
                                selectCalendarTab(CalendarTab.Month)
                            },
                        )
                    }
                }
                    ScreenTab.Tasks -> TasksPreview(tasks, palette)
                    ScreenTab.Settings -> Unit
                }
            }
        }
        AnimatedVisibility(
            visible = screenTab == ScreenTab.Settings,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize(),
        ) {
            SettingsPreview(
                themeMode = resolvedThemeMode ?: DotCalThemeMode.System,
                palette = palette,
                screen = settingsScreen,
                onBack = { closeTopSurface() },
                onScreenChange = { settingsScreen = it },
                onThemeSelected = { selectedTheme ->
                    bootPreferences.edit().putString(BOOT_THEME_KEY, selectedTheme.name).apply()
                    scope.launch {
                        context.calendarPreferencesDataStore.edit { preferences ->
                            preferences[CalendarPreferences.KEY_THEME_MODE] = selectedTheme.name
                        }
                    }
                },
            )
        }
        AnimatedVisibility(
            visible = addSheet,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize(),
        ) {
            EventEditorScreen(
                event = editingEvent,
                selectedDate = selectedDate,
                selectedTime = addStartTime,
                initialReminderMinutes = editingEvent?.let { event -> reminders.firstOrNull { it.eventId == event.baseEventId() }?.minutesBefore },
                palette = palette,
                onDismiss = {
                    editingEvent = null
                    addSheet = false
                },
                onSave = { data, scope ->
                    viewModel.saveEvent(editingEvent, data, scope)
                    editingEvent = null
                    addSheet = false
                },
                onDelete = editingEvent?.let { eventToDelete ->
                    { scope ->
                        viewModel.deleteEvent(eventToDelete, scope)
                        editingEvent = null
                        addSheet = false
                    }
                },
            )
        }
        BackHandler(enabled = addSheet) {
            editingEvent = null
            addSheet = false
        }
    }

    if (showSheet) {
        EventListSheet(
            selectedDate = selectedDate,
            events = events.filter { it.localDate() == selectedDate },
            palette = palette,
            onDismiss = { showSheet = false },
            onAdd = {
                showSheet = false
                addStartTime = LocalTime.of(9, 0)
                editingEvent = null
                addSheet = true
            },
            onEdit = {
                showSheet = false
                editingEvent = it
                addSheet = true
            },
        )
    }

}

@Composable
private fun CalendarActionBar(
    title: String,
    palette: DotCalPalette,
    onTitleClick: () -> Unit,
    onAdd: () -> Unit,
) {
    val topIconTint = if (palette.isDark) NWhite else palette.accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(palette.background)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            title,
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.Bold,
            fontSize = if (title.length <= 4) 30.sp else 28.sp,
            modifier = Modifier.padding(start = 8.dp).clickable(onClick = onTitleClick),
            maxLines = 1,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onAdd,
                modifier = Modifier.size(44.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add event", tint = topIconTint)
            }
        }
    }
}

@Composable
private fun DotCalBottomNav(
    selected: ScreenTab,
    palette: DotCalPalette,
    onCalendar: () -> Unit,
    onTasks: () -> Unit,
    onSettings: () -> Unit,
) {
    val surface = palette.background
    val border = palette.disabledText.copy(alpha = if (palette.isDark) 0.35f else 0.45f)
    val active = palette.accent
    val inactive = palette.secondaryText
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(78.dp)
            .background(surface)
            .drawBehind {
                drawLine(
                    border,
                    Offset(0f, 0f),
                    Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx(),
                )
            }
            .padding(horizontal = 36.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        BottomNavItem(
            label = "Calendar",
            selected = selected == ScreenTab.Calendar,
            activeColor = active,
            inactiveColor = inactive,
            icon = { tint -> BottomCalendarIcon(tint) },
            onClick = onCalendar,
        )
        BottomNavItem(
            label = "Tasks",
            selected = selected == ScreenTab.Tasks,
            activeColor = active,
            inactiveColor = inactive,
            icon = { tint -> BottomTaskIcon(tint) },
            onClick = onTasks,
        )
        BottomNavItem(
            label = "Settings",
            selected = selected == ScreenTab.Settings,
            activeColor = active,
            inactiveColor = inactive,
            icon = { tint -> BottomSettingsIcon(tint) },
            onClick = onSettings,
        )
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    selected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit,
) {
    val tint = if (selected) activeColor else inactiveColor
    Column(
        modifier = Modifier
            .width(72.dp)
            .fillMaxHeight()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        icon(tint)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            color = tint,
            fontFamily = mono,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
        )
    }
}

@Composable
private fun CalendarViewSegmentedControl(
    selected: CalendarTab,
    palette: DotCalPalette,
    onSelected: (CalendarTab) -> Unit,
) {
    val segmentShape = RoundedCornerShape(28.dp)
    val compactTabs = CalendarTab.pickerEntries
    val segmentSurface = palette.background
    val segmentBorder = palette.disabledText.copy(alpha = if (palette.isDark) 0.35f else 0.45f)
    val segmentSelected = palette.segmentSelected
    val inactiveText = palette.secondaryText
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.background)
            .padding(horizontal = 22.dp, vertical = 0.dp)
            .height(42.dp)
            .clip(segmentShape)
            .background(segmentSurface)
            .drawBehind {
                drawRoundRect(
                    color = segmentBorder,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            .padding(horizontal = 18.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        compactTabs.forEach { tab ->
            val isSelected = selected == tab
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) segmentSelected else Color.Transparent)
                    .clickable { onSelected(tab) },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = if (isSelected) 14.dp else 0.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        tab.shortLabel,
                        fontFamily = mono,
                        color = if (selected == tab) palette.primaryText else inactiveText,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomCalendarIcon(tint: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val stroke = Stroke(width = 2.2.dp.toPx())
        val left = 4.dp.toPx()
        val top = 5.dp.toPx()
        val right = size.width - 4.dp.toPx()
        val bottom = size.height - 3.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = stroke,
        )
        drawLine(tint, Offset(left, 11.dp.toPx()), Offset(right, 11.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawLine(tint, Offset(9.dp.toPx(), 2.dp.toPx()), Offset(9.dp.toPx(), 7.dp.toPx()), strokeWidth = 2.2.dp.toPx())
        drawLine(tint, Offset(19.dp.toPx(), 2.dp.toPx()), Offset(19.dp.toPx(), 7.dp.toPx()), strokeWidth = 2.2.dp.toPx())
        drawCircle(tint, radius = 1.5.dp.toPx(), center = Offset(10.dp.toPx(), 16.dp.toPx()))
        drawCircle(tint, radius = 1.5.dp.toPx(), center = Offset(15.dp.toPx(), 16.dp.toPx()))
        drawCircle(tint, radius = 1.5.dp.toPx(), center = Offset(20.dp.toPx(), 16.dp.toPx()))
    }
}

@Composable
private fun BottomTaskIcon(tint: Color) {
    Canvas(modifier = Modifier.size(30.dp)) {
        val stroke = Stroke(width = 2.2.dp.toPx())
        drawCircle(tint, radius = 12.dp.toPx(), center = Offset(size.width / 2f, size.height / 2f), style = stroke)
        drawLine(tint, Offset(10.dp.toPx(), 15.dp.toPx()), Offset(14.dp.toPx(), 19.dp.toPx()), strokeWidth = 2.2.dp.toPx())
        drawLine(tint, Offset(14.dp.toPx(), 19.dp.toPx()), Offset(21.dp.toPx(), 11.dp.toPx()), strokeWidth = 2.2.dp.toPx())
    }
}

@Composable
private fun BottomSettingsIcon(tint: Color) {
    Canvas(modifier = Modifier.size(30.dp)) {
        val stroke = Stroke(width = 2.1.dp.toPx())
        val center = Offset(size.width / 2f, size.height / 2f)
        repeat(8) { index ->
            val angle = Math.toRadians((index * 45).toDouble())
            val start = Offset(
                center.x + kotlin.math.cos(angle).toFloat() * 9.dp.toPx(),
                center.y + kotlin.math.sin(angle).toFloat() * 9.dp.toPx(),
            )
            val end = Offset(
                center.x + kotlin.math.cos(angle).toFloat() * 13.dp.toPx(),
                center.y + kotlin.math.sin(angle).toFloat() * 13.dp.toPx(),
            )
            drawLine(tint, start, end, strokeWidth = 2.1.dp.toPx())
        }
        drawCircle(tint, radius = 9.dp.toPx(), center = center, style = stroke)
        drawCircle(tint, radius = 3.dp.toPx(), center = center, style = stroke)
    }
}

@Composable
private fun MonthView(
    month: LocalDate,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val days = remember(month) { monthGrid(month, DayOfWeek.SUNDAY) }
    val eventsByDate = remember(events) { events.groupBy { it.localDate() } }
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
        Row(modifier = Modifier.fillMaxWidth().height(32.dp).background(palette.calendarSurface)) {
            listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT").forEach {
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

        LazyVerticalGrid(columns = GridCells.Fixed(7), userScrollEnabled = false, modifier = Modifier.fillMaxWidth()) {
            items(days) { day ->
                DayCell(
                    date = day,
                    activeMonth = YearMonth.from(month),
                    isSelected = day == selectedDate,
                    events = eventsByDate[day].orEmpty(),
                    palette = palette,
                    onClick = { onDateSelected(day) },
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    activeMonth: YearMonth,
    isSelected: Boolean,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    val isToday = date == LocalDate.now()
    val inMonth = YearMonth.from(date) == activeMonth
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(palette.calendarSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 8.dp)) {
            if (inMonth) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .then(
                            when {
                                isToday -> Modifier.clip(CircleShape).background(palette.accent)
                                isSelected -> Modifier.clip(CircleShape).background(palette.dimText.copy(alpha = 0.45f))
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
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    events.take(3).forEach { event ->
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(Color(parseColor(event.colorHex ?: "#FF0000"))),
                        )
                    }
                }
            } else {
                Spacer(
                        modifier = Modifier
                            .size(28.dp),
                )
            }
        }
    }
}

@Composable
private fun WeekView(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onJumpToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
) {
    val days = remember(selectedDate) { weekDays(selectedDate) }
    val weekStart = days.first()
    val weekEnd = days.last()
    val timedEvents = events.filter { it.isAllDay == 0 && it.localDate() in weekStart..weekEnd }
    val allDayEvents = events.filter { it.isAllDay == 1 && it.localDate() in weekStart..weekEnd }
    val eventLayouts = remember(timedEvents) { layoutTimedEvents(timedEvents) }

    var dragTotal by remember { mutableFloatStateOf(0f) }
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
            Spacer(modifier = Modifier.width(48.dp))
            days.forEach { day ->
                WeekDayHeader(
                    date = day,
                    selected = day == selectedDate,
                    palette = palette,
                    onClick = { onDateSelected(day) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        if (allDayEvents.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth().height(32.dp).background(palette.calendarSurface)) {
                Spacer(modifier = Modifier.width(48.dp))
                days.forEach { day ->
                    val event = allDayEvents.firstOrNull { it.localDate() == day }
                    Box(
                        modifier = Modifier.weight(1f).height(32.dp).padding(2.dp).background(if (event == null) Color.Transparent else Color(parseColor(event.colorHex ?: "#FF0000")).copy(alpha = 0.75f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (event != null) {
                            Text(event.title, color = NWhite, fontFamily = mono, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
            items(24) { hour ->
                WeekHourRow(
                    hour = hour,
                    days = days,
                    selectedDate = selectedDate,
                    events = timedEvents,
                    eventLayouts = eventLayouts,
                    palette = palette,
                    onAddAtDate = onAddAtDate,
                )
            }
        }
    }
}

@Composable
private fun WeekDayHeader(
    date: LocalDate,
    selected: Boolean,
    palette: DotCalPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = date == LocalDate.now()
    Column(
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(date.dayOfWeek.name.take(3), color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp)
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
private fun WeekHourRow(
    hour: Int,
    days: List<LocalDate>,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    eventLayouts: Map<String, WeekEventLayout>,
    palette: DotCalPalette,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
) {
    val now = LocalTime.now()
    val showNow = selectedDate in days && selectedDate == LocalDate.now() && hour == now.hour
    Row(modifier = Modifier.fillMaxWidth().height(WEEK_HOUR_HEIGHT_DP.dp).background(palette.calendarSurface)) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(WEEK_HOUR_HEIGHT_DP.dp)
                .drawBehind {
                    drawLine(palette.line, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                },
            contentAlignment = Alignment.TopCenter,
        ) {
            Text("${hour.toString().padStart(2, '0')}:00", color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp, modifier = Modifier.padding(top = 4.dp))
            if (showNow) {
                Box(
                    modifier = Modifier
                        .offset(y = ((now.minute / 60f) * 64).dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(palette.accent),
                )
            }
        }
        days.forEach { day ->
            val dayEvents = events.filter { event ->
                event.localDate() == day && event.startLocalTime().hour == hour
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(WEEK_HOUR_HEIGHT_DP.dp)
                    .background(palette.calendarSurface)
                    .drawBehind {
                        drawLine(palette.line, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                        drawLine(palette.line, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                    }
                    .clickable { onAddAtDate(day, LocalTime.of(hour, 0)) },
            ) {
                if (showNow && day == selectedDate) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .offset(y = ((now.minute / 60f) * 64).dp)
                            .background(palette.accent),
                    )
                }
                dayEvents.sortedBy { it.startTimeMs }.forEach { event ->
                    val layout = eventLayouts[event.id] ?: WeekEventLayout(column = 0, columnCount = 1)
                    val top = ((event.startLocalTime().minute / 60f) * WEEK_HOUR_HEIGHT_DP).dp
                    val height = ((event.durationMinutes() / 60f) * WEEK_HOUR_HEIGHT_DP).coerceAtLeast(22f).dp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height)
                            .offset(y = top)
                            .padding(horizontal = 2.dp),
                    ) {
                        repeat(layout.columnCount) { column ->
                            if (column == layout.column) {
                                WeekEventBlock(
                                    event = event,
                                    palette = palette,
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekEventBlock(event: CalendarEvent, palette: DotCalPalette, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(parseColor(event.colorHex ?: "#FF0000")).copy(alpha = 0.80f))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(event.title, color = NWhite, fontFamily = mono, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DayView(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onJumpToday: () -> Unit,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
) {
    val dayEvents = events.filter { it.isTask == 0 && it.localDate() == selectedDate }
    val allDayEvents = dayEvents.filter { it.isAllDay == 1 }
    val timedEvents = dayEvents.filter { it.isAllDay == 0 }
    val tasks = events.filter { it.isTask == 1 && it.localDate() == selectedDate }

    Column(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
        if (allDayEvents.isNotEmpty()) {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(44.dp).background(palette.calendarSurface)) {
                items(allDayEvents.size) { index ->
                    Text(
                        allDayEvents[index].title,
                        color = NWhite,
                        fontFamily = mono,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .background(Color(parseColor(allDayEvents[index].colorHex ?: "#FF0000")).copy(alpha = 0.75f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).background(palette.calendarSurface)) {
            items(24) { hour ->
                DayHourRow(
                    hour = hour,
                    selectedDate = selectedDate,
                    events = timedEvents,
                    palette = palette,
                    onAddAtDate = onAddAtDate,
                )
            }
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Tasks", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                    if (tasks.isEmpty()) {
                        Text("No tasks", color = palette.dimText, fontFamily = mono, fontSize = 13.sp, modifier = Modifier.padding(top = 12.dp, bottom = 28.dp))
                    } else {
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
            }
        }
    }
}

@Composable
private fun DayHourRow(
    hour: Int,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
) {
    val now = LocalTime.now()
    val showNow = selectedDate == LocalDate.now() && hour == now.hour
    val hourEvents = events.filter { it.startLocalTime().hour == hour }

    Row(modifier = Modifier.fillMaxWidth().height(72.dp).background(palette.calendarSurface)) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(72.dp)
                .drawBehind {
                    drawLine(palette.line, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                },
            contentAlignment = Alignment.TopCenter,
        ) {
            Text("${hour.toString().padStart(2, '0')}:00", color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp, modifier = Modifier.padding(top = 6.dp))
            if (showNow) {
                Box(
                    modifier = Modifier
                        .offset(y = ((now.minute / 60f) * 72).dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(palette.accent),
                )
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(72.dp)
                .background(palette.calendarSurface)
                .drawBehind {
                    drawLine(palette.line, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                }
                .clickable { onAddAtDate(selectedDate, LocalTime.of(hour, 0)) },
        ) {
            if (showNow) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .offset(y = ((now.minute / 60f) * 72).dp)
                        .background(palette.accent),
                )
            }
            hourEvents.take(2).forEachIndexed { index, event ->
                WeekEventBlock(
                    event = event,
                    palette = palette,
                    modifier = Modifier.padding(start = 6.dp, end = 8.dp, top = (6 + index * 30).dp).height(24.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventListSheet(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (CalendarEvent) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.dialogSurface, contentColor = palette.primaryText) {
        Column(modifier = Modifier.fillMaxWidth().background(palette.dialogSurface).padding(horizontal = 20.dp, vertical = 12.dp)) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally).size(width = 32.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(palette.dimText))
            Spacer(modifier = Modifier.height(20.dp))
            Text(selectedDate.format(sheetDateFormatter), fontFamily = mono, fontSize = 16.sp, color = palette.primaryText)
            Spacer(modifier = Modifier.height(16.dp))
            if (events.isEmpty()) {
                Text("No events", modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp), fontFamily = mono, fontSize = 14.sp, color = palette.dimText, textAlign = TextAlign.Center)
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                    lazyItems(events, key = { it.id }) { event ->
                        EventRow(event = event, palette = palette, onClick = { onEdit(event) })
                    }
                }
            }
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
                shape = RoundedCornerShape(0.dp),
            ) { Text("+ Add event", fontFamily = mono) }
        }
    }
}

@Composable
private fun EventEditorScreen(
    event: CalendarEvent?,
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    initialReminderMinutes: Int?,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onSave: (EventEditorData, RecurringEditScope) -> Unit,
    onDelete: ((RecurringEditScope) -> Unit)?,
) {
    val editorDate = event?.localDate() ?: selectedDate
    val initialStart = event?.startLocalTime() ?: selectedTime
    val initialEnd = event?.endLocalTime() ?: selectedTime.plusHours(1)
    val initialEndDate = event?.endLocalDateForEditor() ?: editorDate
    var title by remember(event?.id, editorDate, selectedTime) { mutableStateOf(event?.title.orEmpty()) }
    var description by remember(event?.id, editorDate, selectedTime) { mutableStateOf(event?.description.orEmpty()) }
    var location by remember(event?.id, editorDate, selectedTime) { mutableStateOf(event?.location.orEmpty()) }
    var startDate by remember(event?.id, editorDate, selectedTime) { mutableStateOf(editorDate) }
    var endDate by remember(event?.id, editorDate, selectedTime) { mutableStateOf(maxOf(editorDate, initialEndDate)) }
    var startTime by remember(event?.id, editorDate, selectedTime) { mutableStateOf(initialStart) }
    var endTime by remember(event?.id, editorDate, selectedTime) { mutableStateOf(coerceEndAfterStart(initialStart, initialEnd)) }
    var allDay by remember(event?.id, editorDate, selectedTime) { mutableStateOf(event?.isAllDay == 1) }
    var reminderMinutes by remember(event?.id, editorDate, selectedTime, initialReminderMinutes) { mutableStateOf(initialReminderMinutes) }
    var recurrenceRule by remember(event?.id, editorDate, selectedTime) { mutableStateOf(event?.rrule) }
    var dateTimePicker by remember { mutableStateOf<DateTimeField?>(null) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var showRepeatPicker by remember { mutableStateOf(false) }
    var showApplyScopePicker by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    val isRecurringInstance = event?.isRecurrenceOccurrence() == true
    val canChooseRecurrenceScope = isRecurringInstance
    var recurringEditScope by remember(event?.id) {
        mutableStateOf(if (isRecurringInstance) RecurringEditScope.ThisEvent else RecurringEditScope.WholeSeries)
    }
    val editsWholeSeries = (event?.rrule != null || event?.isRecurrenceOccurrence() == true) &&
        recurringEditScope == RecurringEditScope.WholeSeries
    fun trySave() {
        submitted = true
        val startDateTime = startDate.atTime(startTime)
        val endDateTime = endDate.atTime(endTime)
        if (title.isNotBlank() && (if (allDay) endDate >= startDate else endDateTime.isAfter(startDateTime))) {
            onSave(
                EventEditorData(
                    title = title,
                    description = description,
                    location = location,
                    date = startDate,
                    endDate = endDate,
                    startTime = startTime,
                    endTime = endTime,
                    isAllDay = allDay,
                    reminderMinutes = reminderMinutes,
                    rrule = recurrenceRule,
                ),
                recurringEditScope,
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.calendarSurface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = palette.primaryText)
            }
            Text(
                if (event == null) "Add event" else "Edit event",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { trySave() }, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Check, contentDescription = "Save event", tint = palette.primaryText)
            }
        }
        HorizontalDivider(color = palette.line.copy(alpha = 0.55f), thickness = 1.dp)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Event title", fontFamily = mono, color = palette.secondaryText) },
                singleLine = true,
            )
            if (submitted && title.isBlank()) Text("Title required", color = palette.accent, fontFamily = mono, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Location", fontFamily = mono, color = palette.secondaryText) },
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Description", fontFamily = mono, color = palette.secondaryText) },
                minLines = 2,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("All-day", color = palette.primaryText, fontFamily = mono, fontSize = 14.sp)
                Switch(
                    checked = allDay,
                    onCheckedChange = { allDay = it },
                    modifier = Modifier.scale(0.86f),
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = NWhite,
                        checkedTrackColor = palette.accent,
                        uncheckedThumbColor = NWhite,
                        uncheckedTrackColor = Color(0xFFE5E5E5),
                        uncheckedBorderColor = Color.Transparent,
                    ),
                )
            }
            EditorValueRow(
                title = "Starts",
                value = if (allDay) startDate.format(editorDateFormatter) else dateTimeLabel(startDate, startTime),
                palette = palette,
                onClick = { dateTimePicker = DateTimeField.Start },
            )
            EditorValueRow(
                title = "Ends",
                value = if (allDay) endDate.format(editorDateFormatter) else dateTimeLabel(endDate, endTime),
                palette = palette,
                onClick = { dateTimePicker = DateTimeField.End },
            )
            Spacer(modifier = Modifier.height(12.dp))
            EditorValueRow(
                title = "Reminder",
                value = reminderLabel(reminderMinutes),
                palette = palette,
                onClick = { showReminderPicker = true },
            )
            EditorValueRow(
                title = "Repeat",
                value = if (recurringEditScope == RecurringEditScope.ThisEvent) "None" else recurrenceOptions.firstOrNull { it.rrule == recurrenceRule }?.label ?: "None",
                palette = palette,
                onClick = { showRepeatPicker = true },
                enabled = recurringEditScope == RecurringEditScope.WholeSeries,
            )
            if (canChooseRecurrenceScope) {
                EditorValueRow(
                    title = "Apply to",
                    value = recurringEditScope.label(),
                    palette = palette,
                    onClick = { showApplyScopePicker = true },
                )
            }
            if (canChooseRecurrenceScope) {
                Text(
                    if (editsWholeSeries) "Changes apply to the whole series" else "Changes apply only to this event",
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (submitted && allDay && endDate < startDate) {
                Text("End date must be on or after start date", color = palette.accent, fontFamily = mono, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            if (submitted && !allDay && !endDate.atTime(endTime).isAfter(startDate.atTime(startTime))) {
                Text("End must be after start", color = palette.accent, fontFamily = mono, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            if (event != null && onDelete != null) {
                Button(
                    onClick = { onDelete(recurringEditScope) },
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.cell, contentColor = palette.accent),
                    shape = RoundedCornerShape(0.dp),
                ) {
                    Text(if (editsWholeSeries) "Delete series" else "Delete event", fontFamily = mono)
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
    dateTimePicker?.let { field ->
        DateTimeChoiceSheet(
            title = if (field == DateTimeField.Start) "From" else "To",
            selectedDate = if (field == DateTimeField.Start) startDate else endDate,
            selectedTime = if (field == DateTimeField.Start) startTime else endTime,
            minDate = if (field == DateTimeField.End) startDate else null,
            includeTime = !allDay,
            palette = palette,
            onDismiss = { dateTimePicker = null },
            onSelected = { pickedDate, pickedTime ->
                if (field == DateTimeField.Start) {
                    val deltaDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate).coerceAtLeast(0)
                    startDate = pickedDate
                    startTime = pickedTime
                    endDate = pickedDate.plusDays(deltaDays)
                    if (!allDay && !endDate.atTime(endTime).isAfter(startDate.atTime(startTime))) {
                        endTime = coerceEndAfterStart(pickedTime, endTime)
                    }
                } else {
                    endDate = pickedDate
                    endTime = pickedTime
                    if (!allDay && !endDate.atTime(endTime).isAfter(startDate.atTime(startTime))) {
                        endDate = startDate
                        endTime = coerceEndAfterStart(startTime, endTime)
                    }
                }
                dateTimePicker = null
            },
        )
    }
    if (showReminderPicker) {
        ReminderChoiceSheet(
            selected = reminderMinutes,
            palette = palette,
            onDismiss = { showReminderPicker = false },
            onSelected = {
                reminderMinutes = it
                showReminderPicker = false
            },
        )
    }
    if (showRepeatPicker) {
        RepeatChoiceSheet(
            selected = recurrenceRule,
            palette = palette,
            onDismiss = { showRepeatPicker = false },
            onSelected = {
                recurrenceRule = it
                showRepeatPicker = false
            },
        )
    }
    if (showApplyScopePicker) {
        ApplyScopeChoiceSheet(
            selected = recurringEditScope,
            palette = palette,
            onDismiss = { showApplyScopePicker = false },
            onSelected = {
                recurringEditScope = it
                showApplyScopePicker = false
            },
        )
    }
}

@Composable
private fun EditorValueRow(
    title: String,
    value: String,
    palette: DotCalPalette,
    onClick: () -> Unit,
    visible: Boolean = true,
    enabled: Boolean = true,
) {
    if (!visible) return
    val rowTextColor = if (enabled) palette.primaryText else palette.disabledText
    val valueTextColor = if (enabled) palette.secondaryText else palette.disabledText
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .drawBehind {
                drawLine(palette.line.copy(alpha = 0.55f), Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            }
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = rowTextColor, fontFamily = mono, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Text(
            value,
            color = valueTextColor,
            fontFamily = mono,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1.35f),
        )
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = valueTextColor, modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeChoiceSheet(
    title: String,
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    minDate: LocalDate?,
    includeTime: Boolean,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onSelected: (LocalDate, LocalTime) -> Unit,
) {
    val initialDate = maxOf(selectedDate, minDate ?: selectedDate)
    val dates = remember(initialDate, minDate) {
        val start = maxOf(minDate ?: initialDate.minusYears(1), initialDate.minusYears(1))
        List(731) { start.plusDays(it.toLong()) }
    }
    val hours = remember { (0..23).toList() }
    val minutes = remember { (0..59).toList() }
    var pickedDate by remember { mutableStateOf(initialDate) }
    var pickedHour by remember { mutableStateOf(selectedTime.hour) }
    var pickedMinute by remember { mutableStateOf(selectedTime.minute) }
    val dialogBackground = palette.dialogSurface

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = dialogBackground, contentColor = palette.primaryText) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(dialogBackground)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, color = palette.primaryText, fontFamily = mono, fontSize = 20.sp)
            Text(
                if (includeTime) dateTimeLabel(pickedDate, LocalTime.of(pickedHour, pickedMinute)) else pickedDate.format(editorDateFormatter),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().height(188.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WheelColumn(
                    items = dates,
                    selected = pickedDate,
                    label = { it.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)) },
                    palette = palette,
                    modifier = Modifier.weight(1.8f),
                    onSelected = { pickedDate = it },
                )
                if (includeTime) {
                    WheelColumn(
                        items = hours,
                        selected = pickedHour,
                        label = { it.toString().padStart(2, '0') },
                        palette = palette,
                        modifier = Modifier.weight(0.7f),
                        circular = true,
                        onSelected = { pickedHour = it },
                    )
                    WheelColumn(
                        items = minutes,
                        selected = pickedMinute,
                        label = { it.toString().padStart(2, '0') },
                        palette = palette,
                        modifier = Modifier.weight(0.7f),
                        circular = true,
                        onSelected = { pickedMinute = it },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.cancelSurface,
                        contentColor = palette.primaryText,
                    ),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("Cancel", fontFamily = mono, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { onSelected(pickedDate, LocalTime.of(pickedHour, pickedMinute)) },
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("OK", fontFamily = mono, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun <T> WheelColumn(
    items: List<T>,
    selected: T,
    label: (T) -> String,
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
    circular: Boolean = false,
    onSelected: (T) -> Unit,
) {
    val selectedIndex = items.indexOf(selected).coerceAtLeast(0)
    val rowHeight = 56.dp
    val virtualCount = if (circular && items.isNotEmpty()) items.size * 1000 else items.size
    val initialIndex = if (circular && items.isNotEmpty()) {
        (virtualCount / 2 / items.size) * items.size + selectedIndex
    } else {
        selectedIndex
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (initialIndex - 1).coerceAtLeast(0))
    val scope = rememberCoroutineScope()
    val centeredIndex by remember {
        derivedStateOf {
            val layout = listState.layoutInfo
            val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2
            layout.visibleItemsInfo.minByOrNull { item ->
                kotlin.math.abs((item.offset + item.size / 2) - viewportCenter)
            }?.index ?: initialIndex
        }
    }
    LaunchedEffect(selectedIndex, circular) {
        if (circular) return@LaunchedEffect
        listState.scrollToItem((selectedIndex - 1).coerceAtLeast(0))
    }
    LaunchedEffect(listState.isScrollInProgress, centeredIndex) {
        if (!listState.isScrollInProgress && items.isNotEmpty()) {
            val targetIndex = centeredIndex.coerceIn(0, (virtualCount - 1).coerceAtLeast(0))
            val targetItemIndex = if (circular) targetIndex % items.size else targetIndex
            listState.animateScrollToItem((targetIndex - 1).coerceAtLeast(0))
            if (items[targetItemIndex] != selected) onSelected(items[targetItemIndex])
        }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.height(rowHeight * 3),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(virtualCount) { index ->
            val itemIndex = if (circular) index % items.size else index
            val item = items[itemIndex]
            val isCentered = index == centeredIndex
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(rowHeight)
                    .clickable {
                        scope.launch {
                            val targetIndex = if (circular && items.isNotEmpty()) {
                                nearestCircularIndex(centeredIndex, itemIndex, items.size)
                            } else {
                                index
                            }
                            listState.animateScrollToItem((targetIndex - 1).coerceAtLeast(0))
                            onSelected(item)
                        }
                    }
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label(item),
                    color = if (isCentered) {
                        if (palette.isDark) Color.White else palette.primaryText
                    } else {
                        palette.secondaryText.copy(alpha = 0.55f)
                    },
                    fontFamily = mono,
                    fontWeight = if (isCentered) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = if (isCentered) 23.sp else 17.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeChoiceSheet(
    title: String,
    selected: LocalTime,
    minExclusive: LocalTime?,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onSelected: (LocalTime) -> Unit,
) {
    val times = remember(minExclusive) {
        buildList {
            repeat(24 * 4) { index ->
                val time = LocalTime.MIDNIGHT.plusMinutes(index * 15L)
                if (minExclusive == null || time.isAfter(minExclusive)) add(time)
            }
            if (minExclusive != null && all { it != LocalTime.of(23, 59) } && LocalTime.of(23, 59).isAfter(minExclusive)) {
                add(LocalTime.of(23, 59))
            }
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.dialogSurface, contentColor = palette.primaryText) {
        ChoiceSheetContent(
            title = title,
            items = times,
            selected = selected,
            label = { it.format(timeFormatter) },
            palette = palette,
            onSelected = onSelected,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderChoiceSheet(selected: Int?, palette: DotCalPalette, onDismiss: () -> Unit, onSelected: (Int?) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.dialogSurface, contentColor = palette.primaryText) {
        ChoiceSheetContent(
            title = "Reminder",
            items = reminderOptions,
            selected = selected,
            label = { reminderLabel(it) },
            palette = palette,
            onSelected = onSelected,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RepeatChoiceSheet(selected: String?, palette: DotCalPalette, onDismiss: () -> Unit, onSelected: (String?) -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.dialogSurface, contentColor = palette.primaryText) {
        ChoiceSheetContent(
            title = "Repeat",
            items = recurrenceOptions,
            selected = recurrenceOptions.firstOrNull { it.rrule == selected } ?: recurrenceOptions.first(),
            label = { it.label },
            palette = palette,
            onSelected = { onSelected(it.rrule) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplyScopeChoiceSheet(
    selected: RecurringEditScope,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onSelected: (RecurringEditScope) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.dialogSurface, contentColor = palette.primaryText) {
        ChoiceSheetContent(
            title = "Apply to",
            items = listOf(RecurringEditScope.ThisEvent, RecurringEditScope.WholeSeries),
            selected = selected,
            label = { it.label() },
            palette = palette,
            onSelected = onSelected,
        )
    }
}

@Composable
private fun <T> ChoiceSheetContent(
    title: String,
    items: List<T>,
    selected: T,
    label: (T) -> String,
    palette: DotCalPalette,
    onSelected: (T) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(palette.dialogSurface).padding(horizontal = 20.dp, vertical = 12.dp)) {
        Box(modifier = Modifier.align(Alignment.CenterHorizontally).size(width = 32.dp, height = 4.dp).clip(RoundedCornerShape(2.dp)).background(palette.dimText))
        Spacer(modifier = Modifier.height(20.dp))
        Text(title, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.fillMaxWidth().height(320.dp)) {
            lazyItems(items, key = { label(it) }) { item ->
                val isSelected = item == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelected(item) }
                        .padding(vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(label(item), color = palette.primaryText, fontFamily = mono, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = palette.primaryText, modifier = Modifier.size(18.dp))
                }
                HorizontalDivider(color = palette.line.copy(alpha = 0.45f), thickness = 1.dp)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun EventRow(event: CalendarEvent, palette: DotCalPalette, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(palette.cell)
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.width(4.dp).height(42.dp).background(Color(parseColor(event.colorHex ?: "#FF0000"))))
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(event.timeRange(), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
            Text(event.title, color = palette.primaryText, fontFamily = mono, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AgendaPreview(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onJumpToday: () -> Unit,
) {
    val grouped = remember(events) {
        events
            .filter { it.isTask == 0 }
            .sortedBy { it.startTimeMs }
            .groupBy { it.localDate() }
            .toSortedMap()
    }
    LazyColumn(modifier = Modifier.fillMaxSize().background(palette.calendarSurface).padding(16.dp)) {
        if (grouped.isEmpty()) {
            item {
                Text(
                    "No events",
                    color = palette.dimText,
                    fontFamily = mono,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            grouped.forEach { (date, dayEvents) ->
                item {
                    Text(
                        "${date.dayOfWeek.name.take(3)} ${date.dayOfMonth.toString().padStart(2, '0')}",
                        color = palette.secondaryText,
                        fontFamily = mono,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 14.dp, bottom = 6.dp),
                    )
                }
                items(dayEvents.size) { index -> EventRow(dayEvents[index], palette) }
            }
        }
    }
}

@Composable
private fun TasksPreview(tasks: List<CalendarEvent>, palette: DotCalPalette) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (tasks.isEmpty()) {
            item { PlaceholderScreen("All done", palette) }
        } else {
            items(tasks.size) { index ->
                val task = tasks[index]
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                    Box(modifier = Modifier.size(18.dp).background(palette.cell))
                    Text(
                        task.title,
                        modifier = Modifier.padding(start = 12.dp),
                        color = if (task.isCompleted == 1) palette.secondaryText else palette.primaryText,
                        fontFamily = mono,
                        textDecoration = if (task.isCompleted == 1) TextDecoration.LineThrough else null,
                    )
                }
            }
        }
    }
}

@Composable
private fun ThreeDayView(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onPreviousRange: () -> Unit,
    onNextRange: () -> Unit,
    onJumpToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
) {
    val days = remember(selectedDate) { List(3) { selectedDate.plusDays(it.toLong()) } }
    val rangeEvents = events.filter { it.isAllDay == 0 && it.localDate() in days.first()..days.last() }
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
                    palette = palette,
                    onClick = { onDateSelected(day) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        LazyColumn(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
            items(24) { hour ->
                ThreeDayHourRow(
                    hour = hour,
                    days = days,
                    selectedDate = selectedDate,
                    events = rangeEvents,
                    palette = palette,
                    onAddAtDate = onAddAtDate,
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
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
) {
    val now = LocalTime.now()
    val showNow = selectedDate == LocalDate.now() && hour == now.hour
    Row(modifier = Modifier.fillMaxWidth().height(68.dp).background(palette.calendarSurface)) {
        Box(modifier = Modifier.width(52.dp).height(68.dp), contentAlignment = Alignment.TopCenter) {
            Text("${hour.toString().padStart(2, '0')}:00", color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp, modifier = Modifier.padding(top = 6.dp))
        }
        days.forEach { day ->
            val dayEvents = events.filter { it.localDate() == day && it.startLocalTime().hour == hour }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .background(palette.calendarSurface)
                    .drawBehind {
                        drawLine(palette.line, Offset(size.width, 0f), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                        drawLine(palette.line, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                    }
                    .clickable { onAddAtDate(day, LocalTime.of(hour, 0)) },
            ) {
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
                    WeekEventBlock(event = event, palette = palette, modifier = Modifier.padding(start = 5.dp, end = 5.dp, top = (5 + index * 29).dp).height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun YearView(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onJumpToday: () -> Unit,
    onMonthSelected: (LocalDate) -> Unit,
) {
    var dragTotal by remember { mutableFloatStateOf(0f) }
    val months = remember(selectedDate.year) { List(12) { selectedDate.withMonth(it + 1).withDayOfMonth(1) } }
    val eventDates = remember(events, selectedDate.year) {
        events.filter { it.isTask == 0 }
            .map { it.localDate() }
            .filter { it.year == selectedDate.year }
            .toSet()
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
        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.fillMaxSize().background(palette.calendarSurface).padding(8.dp)) {
            items(months) { month ->
                YearMonthCell(
                    month = month,
                    selected = month.year == selectedDate.year && month.monthValue == selectedDate.monthValue,
                    eventDates = eventDates,
                    palette = palette,
                    onClick = { onMonthSelected(month) },
                )
            }
        }
    }
}

@Composable
private fun YearMonthCell(
    month: LocalDate,
    selected: Boolean,
    eventDates: Set<LocalDate>,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    val days = remember(month) { monthGrid(month, DayOfWeek.SUNDAY) }
    val today = LocalDate.now()
    val isCurrentMonth = month.year == today.year && month.monthValue == today.monthValue
    Column(
        modifier = Modifier
            .aspectRatio(0.82f)
            .padding(3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) palette.cell else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 7.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(month.month.name.take(3), color = if (isCurrentMonth) palette.accent else palette.yearMonthLabel, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text(month.monthValue.toString().padStart(2, '0'), color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp)
        }
        Spacer(modifier = Modifier.height(5.dp))
        MiniMonthGridCanvas(
            month = month,
            days = days,
            today = today,
            eventDates = eventDates,
            palette = palette,
            modifier = Modifier.fillMaxWidth().weight(1f),
        )
    }
}

@Composable
private fun MiniMonthGridCanvas(
    month: LocalDate,
    days: List<LocalDate>,
    today: LocalDate,
    eventDates: Set<LocalDate>,
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
) {
    val weekdayColor = palette.yearWeekday.toArgb()
    val secondaryColor = palette.secondaryText.toArgb()
    val dimColor = palette.dimText.copy(alpha = 0.35f).toArgb()
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
        val columnWidth = size.width / 7f
        val rowHeight = size.height / 7f
        val labels = listOf("S", "M", "T", "W", "T", "F", "S")
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
            val isToday = day == today
            val hasEvent = day in eventDates
            val isWeekdayDate = inMonth && day.dayOfWeek != DayOfWeek.SATURDAY && day.dayOfWeek != DayOfWeek.SUNDAY
            if (isToday) {
                nativeCanvas.drawCircle(x, rowHeight * row + rowHeight / 2f, 7.5.dp.toPx(), circlePaint)
            }
            datePaint.color = when {
                isToday -> onAccentColor
                !inMonth -> dimColor
                hasEvent -> accentColor
                else -> secondaryColor
            }
            datePaint.typeface = Typeface.create(Typeface.DEFAULT, if (isWeekdayDate) Typeface.BOLD else Typeface.NORMAL)
            nativeCanvas.drawText(day.dayOfMonth.toString(), x, y, datePaint)
        }
    }
}

@Composable
private fun SettingsPreview(
    themeMode: DotCalThemeMode,
    palette: DotCalPalette,
    screen: SettingsScreen,
    onBack: () -> Unit,
    onScreenChange: (SettingsScreen) -> Unit,
    onThemeSelected: (DotCalThemeMode) -> Unit,
) {
    BackHandler {
        if (screen == SettingsScreen.Theme) {
            onScreenChange(SettingsScreen.Root)
        } else {
            onBack()
        }
    }
    when (screen) {
        SettingsScreen.Root -> SettingsRoot(
            themeMode = themeMode,
            palette = palette,
            onBack = onBack,
            onThemeSelected = onThemeSelected,
        )
        SettingsScreen.Theme -> ThemeSettings(
            themeMode = themeMode,
            palette = palette,
            onBack = { onScreenChange(SettingsScreen.Root) },
            onThemeSelected = onThemeSelected,
        )
    }
}

@Composable
private fun SettingsRoot(themeMode: DotCalThemeMode, palette: DotCalPalette, onBack: () -> Unit, onThemeSelected: (DotCalThemeMode) -> Unit) {
    val listState = rememberLazyListState()
    val showCompactHeader = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 96
    Box(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
            item {
                SettingsLargeHeader(palette = palette, onBack = onBack)
                Spacer(modifier = Modifier.height(10.dp))
            }
            item {
            SettingsSectionTitle("Accounts", palette)
            SettingsMenuRow(title = "Calendar accounts", value = "", palette = palette, onClick = {})
            SettingsDivider(palette)

            SettingsSectionTitle("General", palette)
            SettingsMenuRow(title = "Start of the week", value = "Region default", palette = palette, showStepper = true, onClick = {})
            SettingsMenuRow(title = "Time zone", value = "", palette = palette, onClick = {})
            SettingsToggleRow(title = "Show week number", checked = false, palette = palette)
            SettingsMenuRow(title = "Other calendars", value = "None", palette = palette, onClick = {})
            SettingsMenuRow(title = "Global holidays", value = "", palette = palette, onClick = {})
            SettingsDivider(palette)

            SettingsSectionTitle("Reminders", palette)
            SettingsMenuRow(title = "Reminders", value = "", palette = palette, onClick = {})
            SettingsMenuRow(title = "Default reminder time", value = "5 minutes before", palette = palette, showStepper = true, onClick = {})
            SettingsMenuRow(title = "Default all-day reminder time", value = "8:00 am", palette = palette, onClick = {})
            SettingsToggleRow(
                title = "Import contacts' birthdays",
                subtitle = "Automatically import and display contacts' birthdays",
                checked = false,
                palette = palette,
            )
            SettingsDivider(palette)

            SettingsSectionTitle("Additional", palette)
            SettingsThemeDropdownRow(themeMode = themeMode, palette = palette, onThemeSelected = onThemeSelected)
            SettingsMenuRow(title = "About this app", value = "", palette = palette, onClick = {})
            Spacer(modifier = Modifier.height(32.dp))
            }
        }
        if (showCompactHeader) {
            SettingsCompactHeader(palette = palette, onBack = onBack)
        }
    }
}

@Composable
private fun SettingsLargeHeader(palette: DotCalPalette, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.offset(x = (-16).dp).size(44.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
        }
        Text(
            "Calendar",
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            modifier = Modifier.padding(start = 0.dp, top = 2.dp),
        )
    }
}

@Composable
private fun SettingsCompactHeader(palette: DotCalPalette, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(palette.calendarSurface),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
        }
        Text(
            "Calendar",
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Center),
        )
        HorizontalDivider(color = palette.line.copy(alpha = 0.55f), thickness = 1.dp, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ThemeSettings(
    themeMode: DotCalThemeMode,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onThemeSelected: (DotCalThemeMode) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize().background(palette.calendarSurface).padding(16.dp)) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = palette.primaryText)
                }
                Text("Theme", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Text("Choose app appearance", color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp, bottom = 16.dp))
            DotCalThemeMode.entries.forEach { mode ->
                ThemeOptionRow(
                    mode = mode,
                    palette = palette,
                    selected = themeMode == mode,
                    onClick = { onThemeSelected(mode) },
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(title: String, palette: DotCalPalette) {
    Text(
        title,
        color = palette.secondaryText,
        fontFamily = mono,
        fontSize = 12.sp,
        modifier = Modifier.padding(top = 14.dp, bottom = 8.dp),
    )
}

@Composable
private fun SettingsMenuRow(
    title: String,
    value: String,
    palette: DotCalPalette,
    showStepper: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotBlank()) {
                Text(value, color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (showStepper) {
                UpDownChevron(tint = palette.secondaryText)
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    palette: DotCalPalette,
    subtitle: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (subtitle == null) 52.dp else 68.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            if (subtitle != null) {
                Text(subtitle, color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp, lineHeight = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = {},
            colors = SwitchDefaults.colors(
                checkedThumbColor = NWhite,
                checkedTrackColor = palette.accent,
                uncheckedThumbColor = NWhite,
                uncheckedTrackColor = palette.disabledText,
                uncheckedBorderColor = Color.Transparent,
            ),
        )
    }
}

@Composable
private fun SettingsThemeDropdownRow(
    themeMode: DotCalThemeMode,
    palette: DotCalPalette,
    onThemeSelected: (DotCalThemeMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val menuSurface = palette.dialogSurface
    val menuText = palette.primaryText
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Theme", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(themeMode.label, color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                UpDownChevron(tint = palette.secondaryText)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(menuSurface),
        ) {
            DotCalThemeMode.entries.forEach { mode ->
                DropdownMenuItem(
                    modifier = Modifier.background(menuSurface),
                    text = {
                        Text(
                            mode.label,
                            color = menuText,
                            fontFamily = mono,
                            fontSize = 16.sp,
                        )
                    },
                    trailingIcon = {
                        if (mode == themeMode) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = menuText)
                        }
                    },
                    onClick = {
                        onThemeSelected(mode)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun UpDownChevron(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(width = 12.dp, height = 16.dp)) {
        val stroke = 1.4.dp.toPx()
        drawLine(tint, Offset(2.dp.toPx(), 6.dp.toPx()), Offset(6.dp.toPx(), 2.dp.toPx()), strokeWidth = stroke)
        drawLine(tint, Offset(6.dp.toPx(), 2.dp.toPx()), Offset(10.dp.toPx(), 6.dp.toPx()), strokeWidth = stroke)
        drawLine(tint, Offset(2.dp.toPx(), 10.dp.toPx()), Offset(6.dp.toPx(), 14.dp.toPx()), strokeWidth = stroke)
        drawLine(tint, Offset(6.dp.toPx(), 14.dp.toPx()), Offset(10.dp.toPx(), 10.dp.toPx()), strokeWidth = stroke)
    }
}

@Composable
private fun SettingsDivider(palette: DotCalPalette) {
    HorizontalDivider(color = palette.line.copy(alpha = 0.55f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
}

@Composable
private fun ThemeOptionRow(
    mode: DotCalThemeMode,
    palette: DotCalPalette,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) palette.cell else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemePreview(mode = mode)
            Column {
                Text(mode.label, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(if (selected) "Active" else "Tap to apply", color = if (selected) palette.accent else palette.secondaryText, fontFamily = mono, fontSize = 10.sp)
            }
        }
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (selected) palette.accent else palette.cell),
        )
    }
}

@Composable
private fun ThemePreview(mode: DotCalThemeMode) {
    val preview = dotCalPalette(mode)
    Row(
        modifier = Modifier
            .size(width = 46.dp, height = 32.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(preview.background)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(preview.accent))
        Box(modifier = Modifier.size(width = 20.dp, height = 2.dp).background(preview.primaryText))
    }
}

@Composable
private fun PlaceholderScreen(label: String, palette: DotCalPalette) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(label, color = palette.secondaryText, fontFamily = mono, fontSize = 16.sp)
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

private fun weekDays(date: LocalDate): List<LocalDate> {
    val delta = (7 + date.dayOfWeek.value - DayOfWeek.SUNDAY.value) % 7
    val start = date.minusDays(delta.toLong())
    return List(7) { start.plusDays(it.toLong()) }
}

private fun CalendarEvent.localDate(): LocalDate {
    return Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault()).toLocalDate()
}

private fun CalendarEvent.startLocalTime(): LocalTime {
    return Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
}

private fun CalendarEvent.endLocalDateForEditor(): LocalDate {
    val endInstant = if (isAllDay == 1) endTimeMs - 1 else endTimeMs
    return Instant.ofEpochMilli(endInstant).atZone(ZoneId.systemDefault()).toLocalDate()
}

private fun CalendarEvent.endLocalTime(): LocalTime {
    return Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
}

private fun parseEditorTime(value: String): LocalTime? {
    return runCatching { LocalTime.parse(value, timeFormatter) }.getOrNull()
}

private fun coerceEndAfterStart(start: LocalTime, end: LocalTime): LocalTime {
    if (end.isAfter(start)) return end
    return when {
        start < LocalTime.of(22, 45) -> start.plusHours(1)
        start < LocalTime.of(23, 45) -> LocalTime.of(23, 45)
        else -> LocalTime.of(23, 59)
    }
}

private fun reminderLabel(minutes: Int?): String {
    return minutes?.let { "$it minutes before" } ?: "None"
}

private fun RecurringEditScope.label(): String {
    return when (this) {
        RecurringEditScope.ThisEvent -> "This event"
        RecurringEditScope.WholeSeries -> "Whole series"
    }
}

private fun dateTimeLabel(date: LocalDate, time: LocalTime): String {
    return "${date.format(editorDateFormatter)} ${time.format(editorTimeFormatter).lowercase(Locale.US)}"
}

private fun nearestCircularIndex(currentIndex: Int, targetItemIndex: Int, itemCount: Int): Int {
    if (itemCount <= 0) return currentIndex
    val currentItemIndex = currentIndex % itemCount
    val forward = (targetItemIndex - currentItemIndex + itemCount) % itemCount
    val backward = forward - itemCount
    val delta = if (kotlin.math.abs(backward) < forward) backward else forward
    return currentIndex + delta
}

private fun CalendarEvent.durationMinutes(): Int {
    return ((normalizedEndTimeMs() - startTimeMs) / 60_000L).toInt().coerceAtLeast(15).coerceAtMost(24 * 60)
}

private fun CalendarEvent.normalizedEndTimeMs(): Long {
    return endTimeMs.coerceAtLeast(startTimeMs + 15 * 60 * 1000L)
}

private fun CalendarEvent.timeRange(): String {
    if (isAllDay == 1) return "All-day"
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
    val end = Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
    return "${start.format(timeFormatter)} - ${end.format(timeFormatter)}"
}

private fun parseColor(hex: String): Int {
    return try {
        android.graphics.Color.parseColor(hex)
    } catch (_: IllegalArgumentException) {
        android.graphics.Color.RED
    }
}

private enum class CalendarTab(val label: String, val shortLabel: String) {
    Year("Year view", "Year"),
    Month("Month view", "Month"),
    Week("Week view", "Week"),
    Day("Day view", "Day"),
    ThreeDay("Three-day view", "3 Days"),
    Agenda("Agenda view", "Agenda");

    companion object {
        val pickerEntries = listOf(Year, Month, Week, Day, Agenda)

        fun fromStorage(value: String?): CalendarTab {
            val stored = entries.firstOrNull { it.name == value } ?: Month
            return if (stored == ThreeDay) Month else stored
        }
    }
}

private enum class SettingsScreen {
    Root,
    Theme,
}

private data class DotCalPalette(
    val background: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val dimText: Color,
    val line: Color,
    val cell: Color,
    val calendarSurface: Color,
    val dialogSurface: Color,
    val cancelSurface: Color,
    val segmentSelected: Color,
    val disabledText: Color,
    val dot: Color,
    val yearWeekday: Color,
    val yearMonthLabel: Color,
    val accent: Color,
    val onAccent: Color,
    val isDark: Boolean,
)

private enum class DotCalThemeMode(val label: String) {
    Light("Light"),
    Dark("Dark"),
    System("System");

    companion object {
        fun fromStorage(value: String?): DotCalThemeMode {
            return entries.firstOrNull { it.name == value } ?: System
        }
    }
}

private fun dotCalPalette(mode: DotCalThemeMode, systemDark: Boolean = false): DotCalPalette {
    val resolved = if (mode == DotCalThemeMode.System) {
        if (systemDark) DotCalThemeMode.Dark else DotCalThemeMode.Light
    } else {
        mode
    }
    return when (resolved) {
        DotCalThemeMode.Dark -> DotCalPalette(
            background = Color(0xFF000000),
            primaryText = Color(0xFFFFFFFF),
            secondaryText = Color(0xFFB3B3B3),
            dimText = Color(0xFF6E6E6E),
            line = Color(0xFF2A2A2A),
            cell = NBlack,
            calendarSurface = NBlack,
            dialogSurface = Color(0xFF1E1E1E),
            cancelSurface = Color(0xFF121212),
            segmentSelected = Color(0xFF1E1E1E),
            disabledText = Color(0xFF6E6E6E),
            dot = Color(0xFFFFFFFF),
            yearWeekday = Color(0xFFFFFFFF),
            yearMonthLabel = Color(0xFFFFFFFF),
            accent = NRed,
            onAccent = Color(0xFFFFFFFF),
            isDark = true,
        )
        DotCalThemeMode.Light -> DotCalPalette(
            background = Color(0xFFF7F7F7),
            primaryText = Color(0xFF101010),
            secondaryText = Color(0xFF6B6B6B),
            dimText = Color(0xFFBDBDBD),
            line = Color(0xFFBDBDBD),
            cell = Color(0xFFF7F7F7),
            calendarSurface = Color(0xFFF7F7F7),
            dialogSurface = Color(0xFFFFFFFF),
            cancelSurface = Color(0xFFEFEFEF),
            segmentSelected = Color(0xFFEFEFEF),
            disabledText = Color(0xFFBDBDBD),
            dot = Color(0xFF101010),
            yearWeekday = Color(0xFF101010),
            yearMonthLabel = Color(0xFF101010),
            accent = NRed,
            onAccent = NWhite,
            isDark = false,
        )
        DotCalThemeMode.System -> error("System must be resolved before palette creation")
    }
}

private fun dotCalBootPalette(): DotCalPalette {
    return DotCalPalette(
        background = Color(0xFF000000),
        primaryText = Color(0xFFFFFFFF),
        secondaryText = Color(0xFFB3B3B3),
        dimText = Color(0xFF6E6E6E),
        line = Color(0xFF2A2A2A),
        cell = NBlack,
        calendarSurface = NBlack,
        dialogSurface = Color(0xFF1E1E1E),
        cancelSurface = Color(0xFF121212),
        segmentSelected = Color(0xFF1E1E1E),
        disabledText = Color(0xFF6E6E6E),
        dot = Color(0xFFFFFFFF),
        yearWeekday = Color(0xFFFFFFFF),
        yearMonthLabel = Color(0xFFFFFFFF),
        accent = NRed,
        onAccent = Color(0xFFFFFFFF),
        isDark = true,
    )
}

private enum class ScreenTab {
    Calendar,
    Tasks,
    Settings,
}
