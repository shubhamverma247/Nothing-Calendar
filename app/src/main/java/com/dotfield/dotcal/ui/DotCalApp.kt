package com.dotfield.dotcal.ui

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.datastore.preferences.core.edit
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventEditorData
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import com.dotfield.dotcal.ui.theme.NBlack
import com.dotfield.dotcal.ui.theme.NDim
import com.dotfield.dotcal.ui.theme.NGray
import com.dotfield.dotcal.ui.theme.NLine
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
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
private const val WEEK_HOUR_HEIGHT_DP = 64f
private val reminderOptions = listOf(null, 5, 10, 30)
private data class RecurrenceOption(val label: String, val rrule: String?)
private val recurrenceOptions = listOf(
    RecurrenceOption("None", null),
    RecurrenceOption("Daily", "FREQ=DAILY"),
    RecurrenceOption("Weekly", "FREQ=WEEKLY"),
    RecurrenceOption("Monthly", "FREQ=MONTHLY"),
)

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
    val themeMode by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            DotCalThemeMode.fromStorage(preferences[CalendarPreferences.KEY_THEME_MODE])
        }
    }.collectAsState(initial = DotCalThemeMode.System)
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
    val palette = remember(themeMode, systemDark) { dotCalPalette(themeMode, systemDark) }
    fun selectCalendarTab(tab: CalendarTab) {
        calendarTab = tab
        scope.launch {
            context.calendarPreferencesDataStore.edit { preferences ->
                preferences[CalendarPreferences.KEY_DEFAULT_VIEW] = tab.name
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val visibleMainTab = if (screenTab == ScreenTab.Settings) previousScreenTab else screenTab
        Scaffold(containerColor = palette.background) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.background)
                    .padding(padding),
            ) {
                CalendarActionBar(
                    selected = activeCalendarTab,
                    palette = palette,
                    onAdd = {
                        addStartTime = LocalTime.of(9, 0)
                        editingEvent = null
                        addSheet = true
                    },
                    onSelected = {
                        screenTab = ScreenTab.Calendar
                        previousScreenTab = ScreenTab.Calendar
                        selectCalendarTab(it)
                    },
                    onOpenSettings = {
                        previousScreenTab = if (screenTab == ScreenTab.Settings) previousScreenTab else screenTab
                        settingsScreen = SettingsScreen.Root
                        screenTab = ScreenTab.Settings
                    },
                    onOpenTasks = { screenTab = ScreenTab.Tasks },
                )
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
                themeMode = themeMode,
                palette = palette,
                screen = settingsScreen,
                onBack = { screenTab = previousScreenTab },
                onScreenChange = { settingsScreen = it },
                onThemeSelected = { selectedTheme ->
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
                initialReminderMinutes = editingEvent?.let { event -> reminders.firstOrNull { it.eventId == event.id }?.minutesBefore },
                palette = palette,
                onDismiss = {
                    editingEvent = null
                    addSheet = false
                },
                onSave = {
                    viewModel.saveEvent(editingEvent, it)
                    editingEvent = null
                    addSheet = false
                },
                onDelete = editingEvent?.let { eventToDelete ->
                    {
                        viewModel.deleteEvent(eventToDelete)
                        editingEvent = null
                        addSheet = false
                    }
                },
            )
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
    selected: CalendarTab,
    palette: DotCalPalette,
    onAdd: () -> Unit,
    onSelected: (CalendarTab) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenTasks: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    var moreOpen by remember { mutableStateOf(false) }
    val menuSurface = if (palette.isDark) Color(0xFF191919) else palette.calendarSurface
    val menuText = palette.primaryText
    val topIconTint = if (palette.isDark) NWhite else palette.accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(palette.calendarSurface)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onAdd,
                modifier = Modifier.size(44.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add event", tint = topIconTint)
            }
            Box {
                IconButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.size(44.dp),
                ) {
                    CalendarTabIcon(tab = selected, tint = topIconTint)
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false },
                    modifier = Modifier.background(menuSurface),
                ) {
                    CalendarTab.pickerEntries.forEach { tab ->
                        val selectedTab = selected == tab
                        DropdownMenuItem(
                            modifier = Modifier.background(menuSurface),
                            text = {
                                Text(
                                    tab.label,
                                    color = menuText,
                                    fontFamily = mono,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 18.sp,
                                )
                            },
                            trailingIcon = {
                                if (selectedTab) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = menuText)
                                }
                            },
                            onClick = {
                                onSelected(tab)
                                menuOpen = false
                            },
                        )
                    }
                }
            }
            Box {
                IconButton(
                    onClick = { moreOpen = true },
                    modifier = Modifier.size(44.dp),
                ) {
                    VerticalDotsIcon(tint = if (palette.isDark) NWhite else palette.primaryText)
                }
                DropdownMenu(
                    expanded = moreOpen,
                    onDismissRequest = { moreOpen = false },
                    modifier = Modifier.background(menuSurface),
                ) {
                    DropdownMenuItem(
                        modifier = Modifier.background(menuSurface),
                        text = {
                            Text("Settings", color = menuText, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 18.sp)
                        },
                        onClick = {
                            onOpenSettings()
                            moreOpen = false
                        },
                    )
                    DropdownMenuItem(
                        modifier = Modifier.background(menuSurface),
                        text = {
                            Text("Tasks", color = menuText, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 18.sp)
                        },
                        onClick = {
                            onOpenTasks()
                            moreOpen = false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun VerticalDotsIcon(tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(24.dp)) {
        drawCircle(tint, radius = 1.8.dp.toPx(), center = Offset(size.width / 2f, 6.dp.toPx()))
        drawCircle(tint, radius = 1.8.dp.toPx(), center = Offset(size.width / 2f, size.height / 2f))
        drawCircle(tint, radius = 1.8.dp.toPx(), center = Offset(size.width / 2f, size.height - 6.dp.toPx()))
    }
}

@Composable
private fun CalendarTabs(selected: CalendarTab, palette: DotCalPalette, onSelected: (CalendarTab) -> Unit) {
    TabRow(
        selectedTabIndex = selected.ordinal,
        containerColor = palette.background,
        contentColor = palette.primaryText,
    ) {
        CalendarTab.entries.forEach { tab ->
            Tab(
                selected = selected == tab,
                onClick = { onSelected(tab) },
                text = {
                    Text(
                        tab.label,
                        fontFamily = mono,
                        fontSize = 12.sp,
                        color = if (selected == tab) palette.primaryText else palette.secondaryText,
                    )
                },
            )
        }
    }
}

@Composable
private fun CalendarTabIcon(tab: CalendarTab, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(26.dp)) {
        val stroke = Stroke(width = 2.2.dp.toPx())
        val radius = 4.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(3.dp.toPx(), 4.dp.toPx()),
            size = Size(size.width - 6.dp.toPx(), size.height - 7.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            style = stroke,
        )
        drawLine(tint, Offset(7.dp.toPx(), 2.dp.toPx()), Offset(7.dp.toPx(), 7.dp.toPx()), strokeWidth = 2.2.dp.toPx())
        drawLine(tint, Offset(size.width - 7.dp.toPx(), 2.dp.toPx()), Offset(size.width - 7.dp.toPx(), 7.dp.toPx()), strokeWidth = 2.2.dp.toPx())
        drawLine(tint, Offset(5.dp.toPx(), 10.dp.toPx()), Offset(size.width - 5.dp.toPx(), 10.dp.toPx()), strokeWidth = 1.6.dp.toPx())

        when (tab) {
            CalendarTab.Year -> {
                drawLine(tint, Offset(8.dp.toPx(), 15.dp.toPx()), Offset(size.width - 8.dp.toPx(), 15.dp.toPx()), strokeWidth = 1.8.dp.toPx())
            }
            CalendarTab.Month -> {
                for (row in 0..1) {
                    for (col in 0..2) {
                        drawCircle(tint, radius = 1.4.dp.toPx(), center = Offset((9 + col * 4).dp.toPx(), (15 + row * 4).dp.toPx()))
                    }
                }
            }
            CalendarTab.Week -> {
                drawLine(tint, Offset(7.dp.toPx(), 15.dp.toPx()), Offset(size.width - 7.dp.toPx(), 15.dp.toPx()), strokeWidth = 2.2.dp.toPx())
                drawLine(tint, Offset(7.dp.toPx(), 19.dp.toPx()), Offset(size.width - 11.dp.toPx(), 19.dp.toPx()), strokeWidth = 1.8.dp.toPx())
            }
            CalendarTab.Day -> {
                drawCircle(tint, radius = 2.dp.toPx(), center = Offset(10.dp.toPx(), 17.dp.toPx()))
                drawLine(tint, Offset(14.dp.toPx(), 17.dp.toPx()), Offset(size.width - 7.dp.toPx(), 17.dp.toPx()), strokeWidth = 2.dp.toPx())
            }
            CalendarTab.ThreeDay -> {
                drawLine(tint, Offset(8.dp.toPx(), 14.dp.toPx()), Offset(8.dp.toPx(), 21.dp.toPx()), strokeWidth = 2.dp.toPx())
                drawLine(tint, Offset(13.dp.toPx(), 14.dp.toPx()), Offset(13.dp.toPx(), 21.dp.toPx()), strokeWidth = 2.dp.toPx())
                drawLine(tint, Offset(18.dp.toPx(), 14.dp.toPx()), Offset(18.dp.toPx(), 21.dp.toPx()), strokeWidth = 2.dp.toPx())
            }
            CalendarTab.Agenda -> {
                drawLine(tint, Offset(8.dp.toPx(), 15.dp.toPx()), Offset(16.dp.toPx(), 15.dp.toPx()), strokeWidth = 1.8.dp.toPx())
                drawLine(tint, Offset(8.dp.toPx(), 19.dp.toPx()), Offset(14.dp.toPx(), 19.dp.toPx()), strokeWidth = 1.8.dp.toPx())
                drawLine(tint, Offset(17.dp.toPx(), 19.dp.toPx()), Offset(20.dp.toPx(), 22.dp.toPx()), strokeWidth = 1.8.dp.toPx())
                drawLine(tint, Offset(20.dp.toPx(), 22.dp.toPx()), Offset(24.dp.toPx(), 16.dp.toPx()), strokeWidth = 1.8.dp.toPx())
            }
        }
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
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).background(palette.calendarSurface),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Column(modifier = Modifier.padding(start = 16.dp).clickable(onClick = onJumpToday), horizontalAlignment = Alignment.Start) {
                Text("${month.year}/${month.monthValue}", fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = palette.primaryText)
                Text("Local / Device calendar", fontFamily = mono, fontSize = 9.sp, color = palette.secondaryText)
            }
        }

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
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).background(palette.calendarSurface),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Column(modifier = Modifier.padding(start = 16.dp).clickable(onClick = onJumpToday), horizontalAlignment = Alignment.Start) {
                Text(
                    "${weekStart.year}/${weekStart.monthValue}",
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Text("Week", color = palette.secondaryText, fontFamily = mono, fontSize = 9.sp)
            }
        }

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
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).background(palette.calendarSurface),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Column(modifier = Modifier.padding(start = 16.dp).clickable(onClick = onJumpToday), horizontalAlignment = Alignment.Start) {
                Text(
                    "${selectedDate.year}/${selectedDate.monthValue}",
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                Text("Day", color = palette.secondaryText, fontFamily = mono, fontSize = 9.sp)
            }
        }

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
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = palette.calendarSurface, contentColor = palette.primaryText) {
        Column(modifier = Modifier.fillMaxWidth().background(palette.calendarSurface).padding(horizontal = 20.dp, vertical = 12.dp)) {
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
    onSave: (EventEditorData) -> Unit,
    onDelete: (() -> Unit)?,
) {
    val editorDate = event?.localDate() ?: selectedDate
    val initialStart = event?.startLocalTime() ?: selectedTime
    val initialEnd = event?.endLocalTime() ?: selectedTime.plusHours(1)
    var title by remember(event?.id, editorDate, selectedTime) { mutableStateOf(event?.title.orEmpty()) }
    var description by remember(event?.id, editorDate, selectedTime) { mutableStateOf(event?.description.orEmpty()) }
    var location by remember(event?.id, editorDate, selectedTime) { mutableStateOf(event?.location.orEmpty()) }
    var startText by remember(event?.id, editorDate, selectedTime) { mutableStateOf(initialStart.format(timeFormatter)) }
    var endText by remember(event?.id, editorDate, selectedTime) { mutableStateOf(initialEnd.format(timeFormatter)) }
    var allDay by remember(event?.id, editorDate, selectedTime) { mutableStateOf(event?.isAllDay == 1) }
    var reminderMinutes by remember(event?.id, editorDate, selectedTime, initialReminderMinutes) { mutableStateOf(initialReminderMinutes) }
    var recurrenceRule by remember(event?.id, editorDate, selectedTime) { mutableStateOf(event?.rrule) }
    var submitted by remember { mutableStateOf(false) }
    fun trySave() {
        submitted = true
        val validStart = parseEditorTime(startText)
        val validEnd = parseEditorTime(endText)
        if (title.isNotBlank() && (allDay || (validStart != null && validEnd != null && validEnd.isAfter(validStart)))) {
            onSave(
                EventEditorData(
                    title = title,
                    description = description,
                    location = location,
                    date = editorDate,
                    startTime = validStart ?: LocalTime.MIDNIGHT,
                    endTime = validEnd ?: LocalTime.MIDNIGHT,
                    isAllDay = allDay,
                    reminderMinutes = reminderMinutes,
                    rrule = recurrenceRule,
                ),
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
            if (!allDay) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = startText,
                        onValueChange = { startText = it.take(5) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Start", fontFamily = mono) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    )
                    OutlinedTextField(
                        value = endText,
                        onValueChange = { endText = it.take(5) },
                        modifier = Modifier.weight(1f),
                        label = { Text("End", fontFamily = mono) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    )
                }
            }
            Text(editorDate.format(sheetDateFormatter), modifier = Modifier.padding(top = 12.dp), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Reminder", color = palette.primaryText, fontFamily = mono, fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                reminderOptions.forEach { minutes ->
                    val selected = reminderMinutes == minutes
                    Box(
                        modifier = Modifier
                            .background(if (selected) palette.accent.copy(alpha = 0.12f) else palette.cell)
                            .clickable { reminderMinutes = minutes }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Text(minutes?.let { "${it}m" } ?: "None", color = if (selected) palette.accent else palette.primaryText, fontFamily = mono, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text("Repeat", color = palette.primaryText, fontFamily = mono, fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                recurrenceOptions.forEach { option ->
                    val selected = recurrenceRule == option.rrule
                    Box(
                        modifier = Modifier
                            .background(if (selected) palette.accent.copy(alpha = 0.12f) else palette.cell)
                            .clickable { recurrenceRule = option.rrule }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    ) {
                        Text(option.label, color = if (selected) palette.accent else palette.primaryText, fontFamily = mono, fontSize = 12.sp)
                    }
                }
            }
            val start = parseEditorTime(startText)
            val end = parseEditorTime(endText)
            if (submitted && !allDay && (start == null || end == null || !end.isAfter(start))) {
                Text("Use HH:mm and end after start", color = palette.accent, fontFamily = mono, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
            }
            if (event != null && onDelete != null) {
                Button(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
                    shape = RoundedCornerShape(0.dp),
                ) {
                    Text("Delete event", fontFamily = mono)
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
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
        item {
            Column(modifier = Modifier.clickable(onClick = onJumpToday)) {
                Text("${selectedDate.year}/${selectedDate.monthValue}", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("Agenda", color = palette.secondaryText, fontFamily = mono, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp, bottom = 16.dp))
            }
        }
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
        Column(modifier = Modifier.fillMaxWidth().height(56.dp).background(palette.calendarSurface).padding(start = 16.dp).clickable(onClick = onJumpToday), verticalArrangement = Arrangement.Center) {
            Text("${selectedDate.year}/${selectedDate.monthValue}", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Text("3 Days", color = palette.secondaryText, fontFamily = mono, fontSize = 9.sp)
        }
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
        Column(modifier = Modifier.fillMaxWidth().height(56.dp).background(palette.calendarSurface).padding(start = 16.dp).clickable(onClick = onJumpToday), verticalArrangement = Arrangement.Center) {
            Text(selectedDate.year.toString(), color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Text("Year", color = palette.secondaryText, fontFamily = mono, fontSize = 9.sp)
        }
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
        listOf("S", "M", "T", "W", "T", "F", "S").chunked(7).forEach { labels ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                labels.forEach { label ->
                    Text(label, color = palette.yearWeekday, fontFamily = mono, fontWeight = FontWeight.ExtraBold, fontSize = 7.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
        }
        days.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                week.forEach { day ->
                    val inMonth = day.monthValue == month.monthValue
                    val hasEvent = day in eventDates
                    val isToday = day == today
                    val isWeekdayDate = inMonth && day.dayOfWeek != DayOfWeek.SATURDAY && day.dayOfWeek != DayOfWeek.SUNDAY
                    Box(modifier = Modifier.weight(1f).height(17.dp), contentAlignment = Alignment.Center) {
                        Box(
                            modifier = Modifier
                                .size(15.dp)
                                .clip(CircleShape)
                                .background(if (isToday) palette.accent else Color.Transparent),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                day.dayOfMonth.toString(),
                                color = when {
                                    isToday -> palette.onAccent
                                    !inMonth -> palette.dimText.copy(alpha = 0.35f)
                                    hasEvent -> palette.accent
                                    else -> palette.secondaryText
                                },
                                fontFamily = mono,
                                fontWeight = if (isWeekdayDate) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 7.sp,
                                lineHeight = 7.sp,
                            )
                        }
                    }
                }
            }
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
        color = Color(0xFF9BA3B2),
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
                Text(value, color = Color(0xFFAAAAAA), fontFamily = mono, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
            }
            if (showStepper) {
                UpDownChevron(tint = Color(0xFFAAAAAA))
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFFB8B8B8), modifier = Modifier.size(20.dp))
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
                Text(subtitle, color = Color(0xFF777777), fontFamily = mono, fontSize = 11.sp, lineHeight = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = {},
            colors = SwitchDefaults.colors(
                checkedThumbColor = NWhite,
                checkedTrackColor = palette.accent,
                uncheckedThumbColor = NWhite,
                uncheckedTrackColor = Color(0xFFE5E5E5),
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
    val menuSurface = if (palette.isDark) Color(0xFF191919) else palette.calendarSurface
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
                Text(themeMode.label, color = Color(0xFFAAAAAA), fontFamily = mono, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                UpDownChevron(tint = Color(0xFFAAAAAA))
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

private fun CalendarEvent.endLocalTime(): LocalTime {
    return Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
}

private fun parseEditorTime(value: String): LocalTime? {
    return runCatching { LocalTime.parse(value, timeFormatter) }.getOrNull()
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

private enum class CalendarTab(val label: String) {
    Year("Year view"),
    Month("Month view"),
    Week("Week view"),
    Day("Day view"),
    ThreeDay("Three-day view"),
    Agenda("Agenda view");

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
            background = NBlack,
            primaryText = Color(0xFFF4F4F4),
            secondaryText = Color(0xFF9A9A9A),
            dimText = Color(0xFF555555),
            line = Color(0xFF2A2A2A),
            cell = NBlack,
            calendarSurface = NBlack,
            dot = Color(0xFFF4F4F4),
            yearWeekday = Color(0xFFFFFFFF),
            yearMonthLabel = Color(0xFFFFFFFF),
            accent = NRed,
            onAccent = NBlack,
            isDark = true,
        )
        DotCalThemeMode.Light -> DotCalPalette(
            background = Color(0xFFF7F7F2),
            primaryText = Color(0xFF111111),
            secondaryText = Color(0xFF666666),
            dimText = Color(0xFFAAAAAA),
            line = Color(0xFFD8D8D0),
            cell = Color(0xFFF2F2EC),
            calendarSurface = NWhite,
            dot = Color(0xFF111111),
            yearWeekday = Color(0xFF111111),
            yearMonthLabel = Color(0xFF111111),
            accent = NRed,
            onAccent = NWhite,
            isDark = false,
        )
        DotCalThemeMode.System -> error("System must be resolved before palette creation")
    }
}

private enum class ScreenTab {
    Calendar,
    Tasks,
    Settings,
}
