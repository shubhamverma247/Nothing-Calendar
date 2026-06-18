package com.dotfield.dotcal.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.zIndex
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.datastore.preferences.core.edit
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventEditorData
import com.dotfield.dotcal.data.EventReminder
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
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val mono = FontFamily.SansSerif
private val sheetDateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM", Locale.US)
private val detailDateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.US)
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
fun DotCalApp(viewModel: DotCalViewModel, initialEventId: String? = null) {
    val month by viewModel.month.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val detailEvent by viewModel.detailEvent.collectAsStateWithLifecycle()
    var screenTab by remember { mutableStateOf(ScreenTab.Calendar) }
    var previousScreenTab by remember { mutableStateOf(ScreenTab.Calendar) }
    var showSheet by remember { mutableStateOf(false) }
    var addSheet by remember { mutableStateOf(false) }
    var addStartTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var editorSessionKey by remember { mutableStateOf(UUID.randomUUID().toString()) }
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
    val storedSelectedDateValue by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            preferences[CalendarPreferences.KEY_LAST_SELECTED_DATE].orEmpty()
        }
    }.collectAsState(initial = null)
    var selectedDateRestored by remember { mutableStateOf(false) }
    var calendarTab by remember { mutableStateOf<CalendarTab?>(null) }
    LaunchedEffect(storedCalendarTab) {
        if (calendarTab == null) calendarTab = storedCalendarTab
    }
    val activeCalendarTab = calendarTab ?: storedCalendarTab
    val systemDark = isSystemInDarkTheme()
    val resolvedThemeMode = themeMode
    val palette = remember(resolvedThemeMode, systemDark) { resolvedThemeMode?.let { dotCalPalette(it, systemDark) } ?: dotCalBootPalette() }
    SystemBarColorSync(palette)
    LaunchedEffect(resolvedThemeMode) {
        resolvedThemeMode?.let { mode ->
            bootPreferences.edit().putString(BOOT_THEME_KEY, mode.name).apply()
        }
    }
    LaunchedEffect(storedSelectedDateValue, initialEventId) {
        val storedValue = storedSelectedDateValue ?: return@LaunchedEffect
        if (!selectedDateRestored) {
            if (initialEventId == null && storedValue.isNotBlank()) {
                runCatching { LocalDate.parse(storedValue) }.getOrNull()?.let(viewModel::selectDate)
            }
            selectedDateRestored = true
        }
    }
    LaunchedEffect(selectedDate, selectedDateRestored) {
        if (selectedDateRestored) {
            context.calendarPreferencesDataStore.edit { preferences ->
                preferences[CalendarPreferences.KEY_LAST_SELECTED_DATE] = selectedDate.toString()
            }
        }
    }
    LaunchedEffect(initialEventId) {
        initialEventId?.let(viewModel::openEventDetailById)
    }
    fun selectCalendarTab(tab: CalendarTab) {
        calendarTab = tab
        scope.launch {
            context.calendarPreferencesDataStore.edit { preferences ->
                preferences[CalendarPreferences.KEY_DEFAULT_VIEW] = tab.name
            }
        }
    }
    fun openAddEditor(startTime: LocalTime = LocalTime.of(9, 0)) {
        editorSessionKey = UUID.randomUUID().toString()
        addStartTime = startTime
        editingEvent = null
        addSheet = true
    }
    fun openEditEditor(event: CalendarEvent) {
        editorSessionKey = UUID.randomUUID().toString()
        editingEvent = event
        addSheet = true
    }
    fun closeTopSurface() {
        when {
            detailEvent != null -> viewModel.closeEventDetail()
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
    BackHandler(enabled = detailEvent != null || addSheet || screenTab == ScreenTab.Settings || screenTab == ScreenTab.Tasks) {
        closeTopSurface()
    }

    Box(modifier = Modifier.fillMaxSize().background(palette.topBarSurface)) {
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
            containerColor = palette.topBarSurface,
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
                    .padding(padding)
                    .background(palette.background),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.topBarSurface),
                ) {
                    CalendarActionBar(
                        title = if (visibleMainTab == ScreenTab.Calendar) calendarHeaderLabel else visibleMainTab.name,
                        palette = palette,
                        onTitleClick = {
                            if (visibleMainTab == ScreenTab.Calendar) viewModel.selectDate(LocalDate.now())
                        },
                        onAdd = {
                            openAddEditor()
                        },
                    )
                }
                if (visibleMainTab == ScreenTab.Calendar) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .background(palette.topBarSurface),
                    )
                    CalendarViewSegmentedControl(
                        selected = activeCalendarTab,
                        palette = palette,
                        onSelected = {
                            screenTab = ScreenTab.Calendar
                            previousScreenTab = ScreenTab.Calendar
                            selectCalendarTab(it)
                        },
                    )
                    if (activeCalendarTab != CalendarTab.Year) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
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
                                openAddEditor(time)
                            },
                            onEventClick = viewModel::openEventDetail,
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
                                openAddEditor(time)
                            },
                            onEventClick = viewModel::openEventDetail,
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
                                openAddEditor(time)
                            },
                            onEventClick = viewModel::openEventDetail,
                        )
                        CalendarTab.Agenda -> AgendaPreview(
                            selectedDate = selectedDate,
                            events = events,
                            palette = palette,
                            onJumpToday = { viewModel.selectDate(LocalDate.now()) },
                            onEventClick = viewModel::openEventDetail,
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
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface).statusBarsPadding(),
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
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            EventEditorScreen(
                event = editingEvent,
                editorSessionKey = editorSessionKey,
                selectedDate = selectedDate,
                selectedTime = addStartTime,
                initialReminderMinutes = editingEvent?.let { event -> reminders.firstOrNull { it.eventId == event.baseEventId() }?.minutesBefore },
                palette = palette,
                onDismiss = {
                    editingEvent = null
                    addSheet = false
                },
                onSave = { data, scope ->
                    viewModel.saveEvent(editingEvent, data, scope) {
                        viewModel.selectDate(data.date)
                        editingEvent = null
                        addSheet = false
                    }
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
        AnimatedVisibility(
            visible = detailEvent != null,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            detailEvent?.let { event ->
                EventDetailScreen(
                    event = event,
                    reminders = reminders.filter { it.eventId == event.baseEventId() },
                    account = accounts.firstOrNull { it.id == event.accountId },
                    palette = palette,
                    onBack = viewModel::closeEventDetail,
                    onEdit = {
                        viewModel.closeEventDetail()
                        openEditEditor(event)
                    },
                    onDelete = {
                        viewModel.deleteEvent(event)
                        viewModel.closeEventDetail()
                    },
                )
            }
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
                openAddEditor()
            },
            onEdit = {
                showSheet = false
                openEditEditor(it)
            },
        )
    }

}

@Composable
private fun SystemBarColorSync(palette: DotCalPalette) {
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? Activity)?.window ?: return@SideEffect
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT < 35) {
            @Suppress("DEPRECATION")
            window.statusBarColor = palette.topBarSurface.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = palette.bottomNavSurface.toArgb()
        }
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !palette.isDark
        controller.isAppearanceLightNavigationBars = !palette.isDark
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
            .background(palette.topBarSurface)
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
    val surface = palette.bottomNavSurface
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
    val segmentSurface = palette.topBarSurface
    val segmentBorder = palette.disabledText.copy(alpha = if (palette.isDark) 0.35f else 0.45f)
    val segmentSelected = palette.segmentSelected
    val inactiveText = palette.secondaryText
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.topBarSurface)
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
        val stroke = Stroke(width = 2.1.dp.toPx())
        drawRoundRect(
            color = tint,
            topLeft = Offset(5.dp.toPx(), 5.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(20.dp.toPx(), 20.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = stroke,
        )
        drawLine(tint, Offset(10.dp.toPx(), 12.dp.toPx()), Offset(12.5.dp.toPx(), 14.5.dp.toPx()), strokeWidth = 2.1.dp.toPx())
        drawLine(tint, Offset(12.5.dp.toPx(), 14.5.dp.toPx()), Offset(16.dp.toPx(), 10.dp.toPx()), strokeWidth = 2.1.dp.toPx())
        drawLine(tint, Offset(18.dp.toPx(), 12.dp.toPx()), Offset(22.dp.toPx(), 12.dp.toPx()), strokeWidth = 2.1.dp.toPx())
        drawLine(tint, Offset(10.dp.toPx(), 20.dp.toPx()), Offset(22.dp.toPx(), 20.dp.toPx()), strokeWidth = 2.1.dp.toPx())
    }
}

@Composable
private fun BottomSettingsIcon(tint: Color) {
    Canvas(modifier = Modifier.size(30.dp)) {
        val strokeWidth = 1.9.dp.toPx()
        val stroke = Stroke(width = strokeWidth)
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(tint, radius = 11.5.dp.toPx(), center = center, style = stroke)
        drawCircle(tint, radius = 3.dp.toPx(), center = center, style = stroke)
        val dotRadius = 1.35.dp.toPx()
        repeat(12) { index ->
            val angle = Math.toRadians((index * 30).toDouble())
            val dotCenter = Offset(
                center.x + kotlin.math.cos(angle).toFloat() * 8.dp.toPx(),
                center.y + kotlin.math.sin(angle).toFloat() * 8.dp.toPx(),
            )
            drawCircle(tint, radius = dotRadius, center = dotCenter)
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
    val haptic = LocalHapticFeedback.current
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(palette.calendarSurface)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
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
    onEventClick: (CalendarEvent) -> Unit,
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
                    onEventClick = onEventClick,
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
    onEventClick: (CalendarEvent) -> Unit,
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
                    },
            ) {
                if (dayEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { onAddAtDate(day, LocalTime.of(hour, 0)) },
                    )
                }
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
                            .zIndex(1f)
                            .padding(horizontal = 2.dp),
                    ) {
                        repeat(layout.columnCount) { column ->
                            if (column == layout.column) {
                                WeekEventBlock(
                                    event = event,
                                    palette = palette,
                                    onClick = { onEventClick(event) },
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
private fun WeekEventBlock(
    event: CalendarEvent,
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(parseColor(event.colorHex ?: "#FF0000")).copy(alpha = 0.80f))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
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
    onEventClick: (CalendarEvent) -> Unit,
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
                            .clickable { onEventClick(allDayEvents[index]) }
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
                    onEventClick = onEventClick,
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
    onEventClick: (CalendarEvent) -> Unit,
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
                },
        ) {
            if (hourEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { onAddAtDate(selectedDate, LocalTime.of(hour, 0)) },
                )
            }
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
                    onClick = { onEventClick(event) },
                    modifier = Modifier.zIndex(1f).padding(start = 6.dp, end = 8.dp, top = (6 + index * 30).dp).height(24.dp),
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        contentColor = palette.primaryText,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(palette.dialogSurface).padding(horizontal = 20.dp).padding(bottom = 12.dp)) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(selectedDate.format(sheetDateFormatter), fontFamily = mono, fontSize = 16.sp, color = palette.primaryText)
            Spacer(modifier = Modifier.height(16.dp))
            if (events.isEmpty()) {
                Text("No events", modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp), fontFamily = mono, fontSize = 14.sp, color = palette.dimText, textAlign = TextAlign.Center)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    lazyItems(events, key = { it.id }) { event ->
                        EventRow(event = event, palette = palette, onClick = { onEdit(event) })
                    }
                }
            }
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
            ) { Text("+ Add Event", fontFamily = mono) }
        }
    }
}

@Composable
private fun EventDetailScreen(
    event: CalendarEvent,
    reminders: List<EventReminder>,
    account: CalendarAccount?,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val isReadOnly = event.source == "BIRTHDAY"
    val imageUris = remember(event.imageUris) { parseJsonStringArray(event.imageUris) }
    var previewImageUri by remember { mutableStateOf<String?>(null) }
    Box(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(palette.topBarSurface)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
                }
                Text(
                    "Event Details",
                    modifier = Modifier.weight(1f),
                    color = palette.primaryText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Box(modifier = Modifier.width(64.dp).height(48.dp), contentAlignment = Alignment.Center) {
                    if (!isReadOnly) {
                        Text(
                            "Edit",
                            color = palette.primaryText,
                            fontSize = 15.sp,
                            modifier = Modifier.clickable(onClick = onEdit).padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(palette.background),
                contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 28.dp),
            ) {
                item {
                    Text(
                        event.title,
                        color = palette.primaryText,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 32.sp,
                        lineHeight = 38.sp,
                    )
                    Spacer(modifier = Modifier.height(26.dp))
                }
                item {
                    DetailSection(label = "TIME", palette = palette) {
                        if (event.isAllDay == 1) {
                            Text("All-day", color = palette.primaryText, fontSize = 16.sp, lineHeight = 23.sp)
                        } else {
                            Text(event.detailDateLine(), color = palette.primaryText, fontSize = 16.sp, lineHeight = 23.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(event.detailTimeLine(), color = palette.primaryText, fontSize = 16.sp, lineHeight = 23.sp)
                        }
                        event.recurrenceDetailLabel()?.let { label ->
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(label.toSentenceCase(), color = palette.secondaryText, fontSize = 14.sp, lineHeight = 20.sp)
                        }
                    }
                }
                if (event.location.isNotBlank()) {
                    item {
                        DetailDivider(palette)
                        DetailSection(label = "LOCATION", palette = palette) {
                            Text(event.location, color = palette.primaryText, fontSize = 16.sp, lineHeight = 23.sp)
                        }
                    }
                }
                if (reminders.isNotEmpty()) {
                    item {
                        DetailDivider(palette)
                        DetailSection(label = "REMINDER", palette = palette) {
                            Text(
                                reminders.sortedBy { it.minutesBefore }.joinToString { it.detailLabel().toSentenceCase() },
                                color = palette.primaryText,
                                fontSize = 16.sp,
                                lineHeight = 23.sp,
                            )
                        }
                    }
                }
                item {
                    DetailDivider(palette)
                    DetailSection(label = "CALENDAR", palette = palette) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(palette.accent),
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(account?.displayName ?: event.accountId, color = palette.primaryText, fontSize = 16.sp, lineHeight = 23.sp)
                        }
                    }
                }
                if (event.description.isNotBlank()) {
                    item {
                        DetailDivider(palette)
                        DetailSection(label = "DESCRIPTION", palette = palette) {
                            SelectionContainer {
                                Text(
                                    event.description,
                                    color = palette.primaryText,
                                    fontSize = 16.sp,
                                    lineHeight = 23.sp,
                                )
                            }
                        }
                    }
                }
                if (imageUris.isNotEmpty()) {
                    item {
                        DetailDivider(palette)
                        DetailSection(label = "IMAGES", palette = palette) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                lazyItems(imageUris.take(5), key = { it }) { uri ->
                                    DetailImageThumb(uri = uri, palette = palette, onClick = { previewImageUri = uri })
                                }
                            }
                        }
                    }
                }
                event.voiceNotePath?.takeIf { it.isNotBlank() }?.let { path ->
                    item {
                        DetailDivider(palette)
                        DetailSection(label = "VOICE NOTE", palette = palette) {
                            DetailVoiceNotePlayer(path = path, palette = palette)
                        }
                    }
                }
                item {
                    DetailDivider(palette)
                    Spacer(modifier = Modifier.height(24.dp))
                    if (!isReadOnly) {
                        Text(
                            "Delete Event",
                            color = palette.accent,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onDelete)
                                .padding(vertical = 12.dp),
                        )
                    }
                }
            }
        }
        previewImageUri?.let { uri ->
            FullscreenImagePreview(uri = uri, palette = palette, onDismiss = { previewImageUri = null })
        }
    }
}

@Composable
private fun DetailSection(label: String, palette: DotCalPalette, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
        Text(
            label,
            color = palette.secondaryText,
            fontSize = 12.sp,
            letterSpacing = 0.35.sp,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun DetailDivider(palette: DotCalPalette) {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(palette.line))
}

@Composable
private fun DetailImageThumb(uri: String, palette: DotCalPalette, onClick: () -> Unit) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            loadImageThumbnail(context, uri)
        }
    }
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.background)
            .border(1.dp, palette.line, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        bitmap?.let { image ->
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } ?: Text("IMG", color = palette.secondaryText, fontSize = 11.sp)
    }
}

@Composable
private fun DetailVoiceNotePlayer(path: String, palette: DotCalPalette) {
    var playing by remember(path) { mutableStateOf(false) }
    var positionMs by remember(path) { mutableStateOf(0) }
    val mediaPlayer = remember(path) {
        runCatching {
            MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    playing = false
                    positionMs = 0
                    seekTo(0)
                }
            }
        }.getOrNull()
    }
    LaunchedEffect(playing, mediaPlayer) {
        while (playing && mediaPlayer != null) {
            positionMs = runCatching { mediaPlayer.currentPosition }.getOrDefault(positionMs)
            delay(500)
        }
    }
    DisposableEffect(mediaPlayer) {
        onDispose {
            runCatching {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
        }
    }
    val durationMs = (mediaPlayer?.duration ?: 0).coerceAtLeast(1)
    val progress = (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    Row(
        modifier = Modifier.fillMaxWidth().height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DetailVoicePlayGlyph(
            playing = playing,
            tint = palette.primaryText,
            modifier = Modifier
                .size(28.dp)
                .clickable(enabled = mediaPlayer != null) {
                    mediaPlayer?.let { player ->
                        if (player.isPlaying) {
                            player.pause()
                            playing = false
                        } else {
                            player.start()
                            playing = true
                        }
                    }
                },
        )
        Spacer(modifier = Modifier.width(14.dp))
        Box(modifier = Modifier.weight(1f).height(16.dp), contentAlignment = Alignment.CenterStart) {
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(if (palette.isDark) Color(0xFF6E6E6E) else Color(0xFFBDBDBD)))
            Box(modifier = Modifier.fillMaxWidth(progress).height(2.dp).background(palette.accent))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            formatVoiceDuration((durationMs / 1000).coerceAtLeast(1)),
            color = palette.primaryText,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun DetailVoicePlayGlyph(playing: Boolean, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (playing) {
            val barWidth = 4.dp.toPx()
            val barHeight = 16.dp.toPx()
            val top = (size.height - barHeight) / 2f
            drawRoundRect(tint, Offset(size.width / 2f - 6.dp.toPx(), top), androidx.compose.ui.geometry.Size(barWidth, barHeight))
            drawRoundRect(tint, Offset(size.width / 2f + 2.dp.toPx(), top), androidx.compose.ui.geometry.Size(barWidth, barHeight))
        } else {
            val left = size.width / 2f - 4.dp.toPx()
            val top = size.height / 2f - 8.dp.toPx()
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(left, top)
                lineTo(left, top + 16.dp.toPx())
                lineTo(left + 13.dp.toPx(), top + 8.dp.toPx())
                close()
            }
            drawPath(path, tint)
        }
    }
}

@Composable
private fun VoiceNoteRow(path: String, palette: DotCalPalette) {
    var playing by remember(path) { mutableStateOf(false) }
    val mediaPlayer = remember(path) {
        runCatching {
            MediaPlayer().apply {
                setDataSource(path)
                prepare()
            }
        }.getOrNull()
    }
    DisposableEffect(mediaPlayer) {
        onDispose {
            runCatching {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(palette.cell)
            .clickable(enabled = mediaPlayer != null) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        playing = false
                    } else {
                        player.start()
                        playing = true
                    }
                }
            }
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(if (playing) "PAUSE" else "PLAY", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        Text(
            mediaPlayer?.duration?.let { "${(it / 1000).coerceAtLeast(1)} SEC" } ?: "UNAVAILABLE",
            color = palette.secondaryText,
            fontFamily = mono,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 18.dp),
        )
    }
}

@Composable
private fun dotCalTextFieldColors(palette: DotCalPalette) = OutlinedTextFieldDefaults.colors(
    focusedTextColor = palette.primaryText,
    unfocusedTextColor = palette.primaryText,
    focusedBorderColor = palette.accent,
    unfocusedBorderColor = palette.textFieldBorder,
    focusedLabelColor = palette.accent,
    cursorColor = palette.accent,
    focusedLeadingIconColor = palette.primaryText,
    unfocusedLeadingIconColor = palette.secondaryText,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
)

@Composable
private fun ImageAttachmentSection(
    imageUris: List<String>,
    palette: DotCalPalette,
    onAddImage: () -> Unit,
    onRemoveImage: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Images", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            if (imageUris.isNotEmpty()) {
                Text("${imageUris.size}/5", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            lazyItems(imageUris, key = { it }) { uri ->
                ImageAttachmentThumb(uri = uri, palette = palette, onRemove = { onRemoveImage(uri) })
            }
            if (imageUris.size < 5) {
                item {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, palette.line, RoundedCornerShape(12.dp))
                            .clickable(onClick = onAddImage),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("+", color = palette.accent, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 24.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageAttachmentThumb(uri: String, palette: DotCalPalette, onRemove: () -> Unit) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            loadImageThumbnail(context, uri)
        }
    }
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.cell),
    ) {
        bitmap?.let { image ->
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } ?: Box(modifier = Modifier.fillMaxSize().background(palette.cell), contentAlignment = Alignment.Center) {
            Text("IMG", color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp)
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.72f))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Close, contentDescription = "Remove image", tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}

@Composable
private fun ImageDisplayThumb(uri: String, palette: DotCalPalette, onClick: (() -> Unit)? = null) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            loadImageThumbnail(context, uri)
        }
    }
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.cell)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        bitmap?.let { image ->
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } ?: Text("IMG", color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp)
    }
}

@Composable
private fun FullscreenImagePreview(uri: String, palette: DotCalPalette, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri) {
        value = withContext(Dispatchers.IO) {
            loadImagePreview(context, uri)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
            .clickable(onClick = onDismiss)
            .zIndex(10f),
        contentAlignment = Alignment.Center,
    ) {
        bitmap?.let { image ->
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .aspectRatio(image.width.toFloat() / image.height.toFloat()),
                contentScale = ContentScale.Fit,
            )
        } ?: Text("Image unavailable", color = Color.White, fontFamily = mono, fontSize = 14.sp)
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .size(44.dp),
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close image", tint = Color.White)
        }
    }
}

@Composable
private fun VoiceNoteEditorSection(
    eventId: String,
    voiceNotePath: String?,
    palette: DotCalPalette,
    onVoiceNoteChanged: (String?) -> Unit,
) {
    val context = LocalContext.current
    var permissionDenied by remember { mutableStateOf(false) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var recordingStartedAt by remember { mutableStateOf(0L) }
    var recordingSeconds by remember { mutableStateOf(0) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            startVoiceRecording(context, eventId)?.let { started ->
                recorder = started
                recordingStartedAt = SystemClock.elapsedRealtime()
                recordingSeconds = 0
            }
        } else {
            permissionDenied = true
        }
    }

    fun stopRecording() {
        val activeRecorder = recorder ?: return
        runCatching { activeRecorder.stop() }
        runCatching { activeRecorder.release() }
        recorder = null
        recordingStartedAt = 0L
        recordingSeconds = 0
        onVoiceNoteChanged(voiceNoteFile(context, eventId).absolutePath)
    }

    LaunchedEffect(recorder, recordingStartedAt) {
        while (recorder != null && recordingStartedAt > 0L) {
            val elapsed = ((SystemClock.elapsedRealtime() - recordingStartedAt) / 1000L).toInt()
            recordingSeconds = elapsed.coerceAtMost(MAX_VOICE_NOTE_SECONDS)
            if (elapsed >= MAX_VOICE_NOTE_SECONDS) {
                stopRecording()
                break
            }
            delay(500)
        }
    }
    DisposableEffect(recorder) {
        val recorderForThisEffect = recorder
        onDispose {
            recorderForThisEffect?.let { activeRecorder ->
                runCatching { activeRecorder.stop() }
                runCatching { activeRecorder.release() }
            }
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Voice note", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(10.dp))
        when {
            recorder != null -> RecordingVoiceNoteRow(
                seconds = recordingSeconds,
                palette = palette,
                onStop = { stopRecording() },
            )
            !voiceNotePath.isNullOrBlank() -> ExistingVoiceNoteRow(
                path = voiceNotePath,
                palette = palette,
                onDelete = {
                    runCatching { File(voiceNotePath).delete() }
                    onVoiceNoteChanged(null)
                },
            )
            !permissionDenied -> EmptyVoiceNoteRow(
                palette = palette,
                onRecord = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                        startVoiceRecording(context, eventId)?.let { started ->
                            recorder = started
                            recordingStartedAt = SystemClock.elapsedRealtime()
                            recordingSeconds = 0
                        }
                    } else {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
            )
        }
    }
}

@Composable
private fun EmptyVoiceNoteRow(palette: DotCalPalette, onRecord: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, palette.line, RoundedCornerShape(12.dp))
            .clickable(onClick = onRecord)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicGlyph(tint = palette.primaryText)
        Text("TAP TO RECORD", color = palette.secondaryText, fontFamily = mono, fontSize = 13.sp, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun RecordingVoiceNoteRow(seconds: Int, palette: DotCalPalette, onStop: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.cell)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(palette.accent))
        Text(formatVoiceDuration(seconds), color = palette.primaryText, fontFamily = mono, fontSize = 14.sp, modifier = Modifier.padding(start = 12.dp).weight(1f))
        Text(
            "STOP",
            color = palette.accent,
            fontFamily = mono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.clickable(onClick = onStop).padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun ExistingVoiceNoteRow(path: String, palette: DotCalPalette, onDelete: (() -> Unit)?) {
    var playing by remember(path) { mutableStateOf(false) }
    var positionMs by remember(path) { mutableStateOf(0) }
    val mediaPlayer = remember(path) {
        runCatching {
            MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener {
                    playing = false
                    positionMs = 0
                    seekTo(0)
                }
            }
        }.getOrNull()
    }
    LaunchedEffect(playing, mediaPlayer) {
        while (playing && mediaPlayer != null) {
            positionMs = runCatching { mediaPlayer.currentPosition }.getOrDefault(positionMs)
            delay(500)
        }
    }
    DisposableEffect(mediaPlayer) {
        onDispose {
            runCatching {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(palette.cell)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            if (playing) "PAUSE" else "PLAY",
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            modifier = Modifier.clickable(enabled = mediaPlayer != null) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        player.pause()
                        playing = false
                    } else {
                        player.start()
                        playing = true
                    }
                }
            },
        )
        val durationSeconds = ((mediaPlayer?.duration ?: 0) / 1000).coerceAtLeast(1)
        val positionSeconds = (positionMs / 1000).coerceAtLeast(0)
        Text(
            "${formatVoiceDuration(positionSeconds)} / ${formatVoiceDuration(durationSeconds)}",
            color = palette.secondaryText,
            fontFamily = mono,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 18.dp).weight(1f),
        )
        if (onDelete != null) {
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Delete voice note", tint = palette.secondaryText, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun MicGlyph(tint: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.dp.toPx())
        val centerX = size.width / 2f
        drawRoundRect(
            color = tint,
            topLeft = Offset(centerX - 4.dp.toPx(), 3.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(8.dp.toPx(), 12.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = stroke,
        )
        drawLine(tint, Offset(7.dp.toPx(), 12.dp.toPx()), Offset(7.dp.toPx(), 14.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawLine(tint, Offset(17.dp.toPx(), 12.dp.toPx()), Offset(17.dp.toPx(), 14.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawArc(tint, 0f, 180f, false, topLeft = Offset(7.dp.toPx(), 9.dp.toPx()), size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 10.dp.toPx()), style = stroke)
        drawLine(tint, Offset(centerX, 19.dp.toPx()), Offset(centerX, 22.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawLine(tint, Offset(9.dp.toPx(), 22.dp.toPx()), Offset(15.dp.toPx(), 22.dp.toPx()), strokeWidth = 2.dp.toPx())
    }
}

@Composable
private fun EventEditorScreen(
    event: CalendarEvent?,
    editorSessionKey: String,
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
    val editorStateKey = event?.id ?: editorSessionKey
    val draftEventId = remember(editorStateKey) {
        if (event == null || event.isRecurrenceOccurrence()) UUID.randomUUID().toString() else event.baseEventId()
    }
    var title by remember(editorStateKey) { mutableStateOf(event?.title.orEmpty()) }
    var description by remember(editorStateKey) { mutableStateOf(event?.description.orEmpty()) }
    var location by remember(editorStateKey) { mutableStateOf(event?.location.orEmpty()) }
    var startDate by remember(editorStateKey) { mutableStateOf(editorDate) }
    var endDate by remember(editorStateKey) { mutableStateOf(maxOf(editorDate, initialEndDate)) }
    var startTime by remember(editorStateKey) { mutableStateOf(initialStart) }
    var endTime by remember(editorStateKey) { mutableStateOf(coerceEndAfterStart(initialStart, initialEnd)) }
    var allDay by remember(editorStateKey) { mutableStateOf(event?.isAllDay == 1) }
    var reminderMinutes by remember(editorStateKey, initialReminderMinutes) { mutableStateOf(initialReminderMinutes) }
    var recurrenceRule by remember(editorStateKey) { mutableStateOf(event?.rrule) }
    var imageUris by remember(editorStateKey) { mutableStateOf(parseJsonStringArray(event?.imageUris ?: "[]")) }
    var voiceNotePath by remember(editorStateKey) { mutableStateOf(event?.voiceNotePath) }
    var dateTimePicker by remember { mutableStateOf<DateTimeField?>(null) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var showRepeatPicker by remember { mutableStateOf(false) }
    var showApplyScopePicker by remember { mutableStateOf(false) }
    var submitted by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusSinkRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
    ) { uris ->
        val availableSlots = (5 - imageUris.size).coerceAtLeast(0)
        val selected = uris.take(availableSlots).map { it.toString() }
        uris.take(availableSlots).forEach { uri ->
            runCatching {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        imageUris = (imageUris + selected).distinct().take(5)
    }
    val isRecurringInstance = event?.isRecurrenceOccurrence() == true
    val canChooseRecurrenceScope = isRecurringInstance
    var recurringEditScope by remember(editorStateKey) {
        mutableStateOf(if (isRecurringInstance) RecurringEditScope.ThisEvent else RecurringEditScope.WholeSeries)
    }
    val editsWholeSeries = (event?.rrule != null || event?.isRecurrenceOccurrence() == true) &&
        recurringEditScope == RecurringEditScope.WholeSeries
    fun clearEditorFocus() {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
        runCatching { focusSinkRequester.requestFocus() }
    }
    fun trySave() {
        submitted = true
        val startDateTime = startDate.atTime(startTime)
        val endDateTime = endDate.atTime(endTime)
        if (title.isNotBlank() && (if (allDay) endDate >= startDate else endDateTime.isAfter(startDateTime))) {
            onSave(
                EventEditorData(
                    eventId = draftEventId,
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
                    imageUris = imageUris.toJsonStringArray(),
                    voiceNotePath = voiceNotePath,
                ),
                recurringEditScope,
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(palette.topBarSurface)
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
                .pointerInput(event?.id) {
                    awaitPointerEventScope {
                        while (true) {
                            val pointerEvent = awaitPointerEvent(PointerEventPass.Initial)
                            if (pointerEvent.changes.any { it.pressed && !it.previousPressed }) {
                                clearEditorFocus()
                            }
                        }
                    }
                }
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            Box(modifier = Modifier.size(1.dp).focusRequester(focusSinkRequester).focusable())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Event title", fontFamily = mono, color = palette.secondaryText) },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, tint = palette.secondaryText) },
                colors = dotCalTextFieldColors(palette),
                textStyle = TextStyle(color = palette.primaryText, fontFamily = mono),
                singleLine = true,
            )
            if (submitted && title.isBlank()) Text("Title required", color = palette.accent, fontFamily = mono, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Location", fontFamily = mono, color = palette.secondaryText) },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = palette.secondaryText) },
                colors = dotCalTextFieldColors(palette),
                textStyle = TextStyle(color = palette.primaryText, fontFamily = mono),
                singleLine = true,
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Description", fontFamily = mono, color = palette.secondaryText) },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null, tint = palette.secondaryText) },
                colors = dotCalTextFieldColors(palette),
                textStyle = TextStyle(color = palette.primaryText, fontFamily = mono),
                minLines = 2,
            )
            Spacer(modifier = Modifier.height(16.dp))
            ImageAttachmentSection(
                imageUris = imageUris,
                palette = palette,
                onAddImage = {
                    clearEditorFocus()
                    val availableSlots = (5 - imageUris.size).coerceAtLeast(0)
                    if (availableSlots > 0) {
                        imagePicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    }
                },
                onRemoveImage = { uri -> imageUris = imageUris.filterNot { it == uri } },
            )
            Spacer(modifier = Modifier.height(16.dp))
            VoiceNoteEditorSection(
                eventId = draftEventId,
                voiceNotePath = voiceNotePath,
                palette = palette,
                onVoiceNoteChanged = { voiceNotePath = it },
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Text("All-day", color = palette.primaryText, fontFamily = mono, fontSize = 14.sp)
                Switch(
                checked = allDay,
                onCheckedChange = {
                        clearEditorFocus()
                        allDay = it
                    },
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
                onClick = {
                    clearEditorFocus()
                    dateTimePicker = DateTimeField.Start
                },
            )
            EditorValueRow(
                title = "Ends",
                value = if (allDay) endDate.format(editorDateFormatter) else dateTimeLabel(endDate, endTime),
                palette = palette,
                onClick = {
                    clearEditorFocus()
                    dateTimePicker = DateTimeField.End
                },
            )
            Spacer(modifier = Modifier.height(12.dp))
            EditorValueRow(
                title = "Reminder",
                value = reminderLabel(reminderMinutes),
                palette = palette,
                onClick = {
                    clearEditorFocus()
                    showReminderPicker = true
                },
            )
            EditorValueRow(
                title = "Repeat",
                value = if (recurringEditScope == RecurringEditScope.ThisEvent) "None" else recurrenceOptions.firstOrNull { it.rrule == recurrenceRule }?.label ?: "None",
                palette = palette,
                onClick = {
                    clearEditorFocus()
                    showRepeatPicker = true
                },
                enabled = recurringEditScope == RecurringEditScope.WholeSeries,
            )
            if (canChooseRecurrenceScope) {
                EditorValueRow(
                    title = "Apply to",
                    value = recurringEditScope.label(),
                    palette = palette,
                    onClick = {
                        clearEditorFocus()
                        showApplyScopePicker = true
                    },
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
                Text(
                    if (editsWholeSeries) "Delete series" else "Delete event",
                    color = palette.accent,
                    fontFamily = mono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 28.dp)
                        .clickable { onDelete(recurringEditScope) },
                )
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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = dialogBackground,
        contentColor = palette.primaryText,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(dialogBackground)
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 20.dp),
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
                horizontalArrangement = Arrangement.spacedBy(16.dp),
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
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.cancelSurface,
                        contentColor = palette.primaryText,
                    ),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.dp, palette.cancelBorder, RoundedCornerShape(18.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Cancel", fontFamily = mono, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
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
                    .drawBehind {
                        val lineColor = palette.line.copy(alpha = if (isCentered) 0.78f else 0.34f)
                        drawLine(lineColor, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                        drawLine(lineColor, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
                    }
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
                    color = if (isCentered) palette.primaryText else palette.disabledText,
                    fontFamily = mono,
                    fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Normal,
                    fontSize = if (isCentered) 24.sp else 17.sp,
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        contentColor = palette.primaryText,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        contentColor = palette.primaryText,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        contentColor = palette.primaryText,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        contentColor = palette.primaryText,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
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
    Column(modifier = Modifier.fillMaxWidth().background(palette.dialogSurface).padding(horizontal = 20.dp).padding(bottom = 16.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
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
private fun BottomSheetDragHandle(palette: DotCalPalette) {
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 8.dp)
            .size(width = 36.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(palette.dragHandle),
    )
}

@Composable
private fun EventRow(event: CalendarEvent, palette: DotCalPalette, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.eventCardSurface)
            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(16.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(event.timeRange(), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(event.title, color = palette.primaryText, fontFamily = mono, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (event.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = palette.secondaryText,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        event.location,
                        color = palette.secondaryText,
                        fontFamily = mono,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (onClick != null) {
            Spacer(modifier = Modifier.width(12.dp))
            EventCardChevron(tint = palette.eventCardChevron)
        }
    }
}

@Composable
private fun EventCardChevron(tint: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val strokeWidth = 2.dp.toPx()
        val startX = 9.dp.toPx()
        val midX = 15.dp.toPx()
        val topY = 7.dp.toPx()
        val midY = 12.dp.toPx()
        val bottomY = 17.dp.toPx()
        drawLine(tint, Offset(startX, topY), Offset(midX, midY), strokeWidth = strokeWidth)
        drawLine(tint, Offset(midX, midY), Offset(startX, bottomY), strokeWidth = strokeWidth)
    }
}

@Composable
private fun AgendaPreview(
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    onJumpToday: () -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
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
                items(dayEvents.size) { index ->
                    val event = dayEvents[index]
                    EventRow(event, palette, onClick = { onEventClick(event) })
                }
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
    onEventClick: (CalendarEvent) -> Unit,
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
                    onEventClick = onEventClick,
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
    onEventClick: (CalendarEvent) -> Unit,
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
                    },
            ) {
                if (dayEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { onAddAtDate(day, LocalTime.of(hour, 0)) },
                    )
                }
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
                    WeekEventBlock(
                        event = event,
                        palette = palette,
                        onClick = { onEventClick(event) },
                        modifier = Modifier.zIndex(1f).padding(start = 5.dp, end = 5.dp, top = (5 + index * 29).dp).height(24.dp),
                    )
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

private fun CalendarEvent.detailTimeRange(): String {
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault())
    val end = Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault())
    return "${start.format(detailDateFormatter).uppercase(Locale.US)} - ${start.toLocalTime().format(timeFormatter)} - ${end.toLocalTime().format(timeFormatter)}"
}

private fun CalendarEvent.detailDateLine(): String {
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault())
    return start.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.US))
}

private fun CalendarEvent.detailTimeLine(): String {
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault())
    val end = Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault())
    return "${start.toLocalTime().format(timeFormatter)} - ${end.toLocalTime().format(timeFormatter)}"
}

private fun CalendarEvent.recurrenceDetailLabel(): String? {
    return when (rrule?.trim()) {
        "FREQ=DAILY" -> "REPEATS DAILY"
        "FREQ=WEEKLY" -> "REPEATS WEEKLY"
        "FREQ=MONTHLY" -> "REPEATS MONTHLY"
        else -> null
    }
}

private fun EventReminder.detailLabel(): String {
    return when (minutesBefore) {
        1 -> "1 MINUTE BEFORE"
        60 -> "1 HOUR BEFORE"
        1440 -> "1 DAY BEFORE"
        else -> "$minutesBefore MINUTES BEFORE"
    }
}

private fun String.toSentenceCase(): String {
    val lower = lowercase(Locale.US)
    return lower.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString() }
}

private fun parseJsonStringArray(value: String): List<String> {
    val trimmed = value.trim()
    if (trimmed.length < 2 || trimmed.first() != '[' || trimmed.last() != ']') return emptyList()
    return trimmed
        .removePrefix("[")
        .removeSuffix("]")
        .split(',')
        .mapNotNull { raw ->
            raw.trim()
                .removeSurrounding("\"")
                .replace("\\\"", "\"")
                .takeIf { it.isNotBlank() }
        }
}

private fun List<String>.toJsonStringArray(): String {
    return joinToString(prefix = "[", postfix = "]") { value ->
        "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }
}

private fun loadImageThumbnail(context: Context, uriValue: String): Bitmap? {
    val uri = runCatching { Uri.parse(uriValue) }.getOrNull() ?: return null
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(uri, Size(180, 180), null)
        } else {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }
    }.getOrNull()
}

private fun loadImagePreview(context: Context, uriValue: String): Bitmap? {
    val uri = runCatching { Uri.parse(uriValue) }.getOrNull() ?: return null
    return runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(uri, Size(1280, 1280), null)
        } else {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            }
        }
    }.getOrNull()
}

private const val MAX_VOICE_NOTE_SECONDS = 300

private fun voiceNoteFile(context: Context, eventId: String): File {
    val directory = File(context.filesDir, "voice_notes").apply { mkdirs() }
    return File(directory, "$eventId.m4a")
}

private fun startVoiceRecording(context: Context, eventId: String): MediaRecorder? {
    val outputFile = voiceNoteFile(context, eventId)
    runCatching { if (outputFile.exists()) outputFile.delete() }
    return runCatching {
        mediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setMaxDuration(MAX_VOICE_NOTE_SECONDS * 1000)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }
    }.getOrNull()
}

private fun mediaRecorder(context: Context): MediaRecorder {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
    }
}

private fun formatVoiceDuration(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    return "${safeSeconds / 60}:${(safeSeconds % 60).toString().padStart(2, '0')}"
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
    val topBarSurface: Color,
    val bottomNavSurface: Color,
    val dialogSurface: Color,
    val cancelSurface: Color,
    val cancelBorder: Color,
    val dragHandle: Color,
    val eventCardSurface: Color,
    val eventCardBorder: Color,
    val eventCardChevron: Color,
    val textFieldBorder: Color,
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
            topBarSurface = Color(0xFF000000),
            bottomNavSurface = Color(0xFF000000),
            dialogSurface = Color(0xFF1E1E1E),
            cancelSurface = Color(0xFF121212),
            cancelBorder = Color(0xFF2A2A2A),
            dragHandle = Color(0xFF707070),
            eventCardSurface = Color(0xFF121212),
            eventCardBorder = Color(0xFF2A2A2A),
            eventCardChevron = Color(0xFF6E6E6E),
            textFieldBorder = Color(0xFF4A4A4A),
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
            line = Color(0xFFE8E8E8),
            cell = Color(0xFFF7F7F7),
            calendarSurface = Color(0xFFF7F7F7),
            topBarSurface = Color(0xFFFFFFFF),
            bottomNavSurface = Color(0xFFFFFFFF),
            dialogSurface = Color(0xFFFFFFFF),
            cancelSurface = Color(0xFFEFEFEF),
            cancelBorder = Color(0xFFE0E0E0),
            dragHandle = Color(0xFFC8C8C8),
            eventCardSurface = Color(0xFFFFFFFF),
            eventCardBorder = Color(0xFFE8E8E8),
            eventCardChevron = Color(0xFFBDBDBD),
            textFieldBorder = Color(0xFFDADADA),
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
        topBarSurface = Color(0xFF000000),
        bottomNavSurface = Color(0xFF000000),
        dialogSurface = Color(0xFF1E1E1E),
        cancelSurface = Color(0xFF121212),
        cancelBorder = Color(0xFF2A2A2A),
        dragHandle = Color(0xFF707070),
        eventCardSurface = Color(0xFF121212),
        eventCardBorder = Color(0xFF2A2A2A),
        eventCardChevron = Color(0xFF6E6E6E),
        textFieldBorder = Color(0xFF4A4A4A),
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
