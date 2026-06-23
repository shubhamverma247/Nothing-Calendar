package com.dotfield.dotcal.ui

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Typeface
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.os.SystemClock
import android.widget.Toast
import android.util.Size
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings as SettingsGearIcon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.zIndex
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.datastore.preferences.core.edit
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.dotfield.dotcal.BuildConfig
import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventEditorData
import com.dotfield.dotcal.data.EventReminder
import com.dotfield.dotcal.data.baseEventId
import com.dotfield.dotcal.data.isRecurrenceOccurrence
import com.dotfield.dotcal.data.RecurringEditScope
import com.dotfield.dotcal.data.SyncMetadata
import com.dotfield.dotcal.data.TaskEditorData
import com.dotfield.dotcal.prefs.CalendarPreferences
import com.dotfield.dotcal.prefs.calendarPreferencesDataStore
import com.dotfield.dotcal.sync.CalendarSyncWorkScheduler
import com.dotfield.dotcal.widget.WidgetUpdateWorker
import com.dotfield.dotcal.ui.theme.NBlack
import com.dotfield.dotcal.ui.theme.NWhite
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.io.File
import java.util.Locale
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val mono = FontFamily.SansSerif
private val sheetDateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM", Locale.US)
private val detailDateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.US)
private val agendaDateHeaderFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.US)
private val compactDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
private val editorDateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM, yyyy", Locale.US)
private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
private val editorTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
private const val WEEK_HOUR_HEIGHT_DP = 64f
private const val BOOT_PREFS = "dotcal_boot"
private const val BOOT_THEME_KEY = "theme_mode"
private const val BOOT_ACCENT_KEY = "accent_color"
private val reminderOptions = listOf(null, 5, 10, 30, 60, 1440)
private val taskReminderOptions = listOf(null, 5, 10, 30, 1440)
private data class RecurrenceOption(val label: String, val rrule: String?)
private val recurrenceOptions = listOf(
    RecurrenceOption("None", null),
    RecurrenceOption("Daily", "FREQ=DAILY"),
    RecurrenceOption("Weekly", "FREQ=WEEKLY"),
    RecurrenceOption("Monthly", "FREQ=MONTHLY"),
)
private val onboardingPages = listOf(
    OnboardingPage.Welcome,
    OnboardingPage.CalendarPermission,
    OnboardingPage.Notifications,
    OnboardingPage.Contacts,
    OnboardingPage.Ready,
)
private enum class DateTimeField { Start, End }
private enum class DeleteSource { Editor, Detail }
private data class PendingDelete(
    val event: CalendarEvent,
    val scope: RecurringEditScope,
    val source: DeleteSource,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DotCalApp(
    viewModel: DotCalViewModel,
    initialEventId: String? = null,
    initialTaskId: String? = null,
    initialCalendarTab: String? = null,
    initialCalendarDate: String? = null,
    initialAddEvent: Boolean = false,
    initialRouteToken: Long? = null,
) {
    val month by viewModel.month.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val holidayCountries by viewModel.holidayCountries.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val syncMetadata by viewModel.syncMetadata.collectAsStateWithLifecycle()
    val detailEvent by viewModel.detailEvent.collectAsStateWithLifecycle()
    var screenTab by remember { mutableStateOf(ScreenTab.Calendar) }
    var previousScreenTab by remember { mutableStateOf(ScreenTab.Calendar) }
    var showSheet by remember { mutableStateOf(false) }
    var addSheet by remember { mutableStateOf(false) }
    var addStartTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var taskDetail by remember { mutableStateOf<CalendarEvent?>(null) }
    var lastTaskDetail by remember { mutableStateOf<CalendarEvent?>(null) }
    var editingTask by remember { mutableStateOf<CalendarEvent?>(null) }
    var showTaskEditor by remember { mutableStateOf(false) }
    var editorSessionKey by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var settingsScreen by remember { mutableStateOf(SettingsScreen.Root) }
    var pendingDelete by remember { mutableStateOf<PendingDelete?>(null) }
    var pendingTaskDelete by remember { mutableStateOf<CalendarEvent?>(null) }
    var handledTaskDeepLinkId by remember { mutableStateOf<String?>(null) }
    var handledRouteToken by remember { mutableStateOf<Long?>(null) }
    var routePending by remember(initialRouteToken) {
        mutableStateOf(initialRouteToken != null && (initialEventId != null || !initialTaskId.isNullOrBlank() || initialAddEvent || initialCalendarDate != null))
    }
    var isSyncing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val bootPreferences = remember(context) { context.getSharedPreferences(BOOT_PREFS, android.content.Context.MODE_PRIVATE) }
    val bootThemeMode = remember(bootPreferences) {
        DotCalThemeMode.fromStorage(bootPreferences.getString(BOOT_THEME_KEY, null))
    }
    val bootAccentColor = remember(bootPreferences) {
        AccentColor.fromStorage(bootPreferences.getString(BOOT_ACCENT_KEY, null))
    }
    val themeMode by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            DotCalThemeMode.fromStorage(preferences[CalendarPreferences.KEY_THEME_MODE])
        }
    }.collectAsStateWithLifecycle(initialValue = bootThemeMode)
    val accentColor by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            AccentColor.fromStorage(preferences[CalendarPreferences.KEY_ACCENT_COLOR])
        }
    }.collectAsStateWithLifecycle(initialValue = bootAccentColor)
    val storedCalendarTab by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            CalendarTab.fromStorage(preferences[CalendarPreferences.KEY_DEFAULT_VIEW])
        }
    }.collectAsStateWithLifecycle(initialValue = CalendarTab.Month)
    val storedSelectedDateValue by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            preferences[CalendarPreferences.KEY_LAST_SELECTED_DATE].orEmpty()
        }
    }.collectAsStateWithLifecycle(initialValue = null)
    val syncEnabled by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            preferences[CalendarPreferences.KEY_SYNC_ENABLED] ?: false
        }
    }.collectAsStateWithLifecycle(initialValue = false)
    val syncIntervalMins by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            preferences[CalendarPreferences.KEY_SYNC_INTERVAL_MINS] ?: CalendarSyncWorkScheduler.DEFAULT_SYNC_INTERVAL_MINS
        }
    }.collectAsStateWithLifecycle(initialValue = CalendarSyncWorkScheduler.DEFAULT_SYNC_INTERVAL_MINS)
    val birthdayEnabled by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            preferences[CalendarPreferences.KEY_BIRTHDAY_ENABLED] ?: false
        }
    }.collectAsStateWithLifecycle(initialValue = false)
    val defaultReminderMinutes by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            val stored = preferences[CalendarPreferences.KEY_DEFAULT_REMINDER] ?: 5
            stored.takeIf { it >= 0 }
        }
    }.collectAsStateWithLifecycle(initialValue = 5)
    val defaultAllDayReminderTime by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            parseStoredTime(preferences[CalendarPreferences.KEY_DEFAULT_ALL_DAY_REMINDER_TIME]) ?: LocalTime.of(8, 0)
        }
    }.collectAsStateWithLifecycle(initialValue = LocalTime.of(8, 0))
    val weekStartOption by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            parseWeekStartOption(preferences[CalendarPreferences.KEY_WEEK_START])
        }
    }.collectAsStateWithLifecycle(initialValue = WeekStartOption.RegionDefault)
    val weekStartDay = remember(weekStartOption) { resolveWeekStartDay(weekStartOption) }
    val onboardingDone by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            preferences[CalendarPreferences.KEY_ONBOARDING_DONE] ?: false
        }
    }.collectAsStateWithLifecycle<Boolean?>(initialValue = null)
    var hasCalendarPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED)
    }
    var hasContactsPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
    }
    var hasNotificationPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var calendarPermissionRequested by remember { mutableStateOf(false) }
    var pendingAddAccountAfterPermission by remember { mutableStateOf(false) }
    fun launchGoogleAddAccount() {
        val activity = context as? Activity ?: run {
            Toast.makeText(context, "Account setup unavailable", Toast.LENGTH_SHORT).show()
            return
        }
        runCatching {
            AccountManager.get(context).addAccount(
                "com.google",
                null,
                null,
                null,
                activity,
                { future ->
                    runCatching { future.result }.onSuccess {
                        if (!isSyncing) {
                            isSyncing = true
                            viewModel.syncNow { isSyncing = false }
                        }
                        settingsScreen = SettingsScreen.CalendarAccounts
                    }
                },
                Handler(Looper.getMainLooper()),
            )
        }.onFailure {
            Toast.makeText(context, "Account setup unavailable", Toast.LENGTH_SHORT).show()
        }
    }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        hasCalendarPermission = grants[Manifest.permission.READ_CALENDAR] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        if (hasCalendarPermission) {
            if (pendingAddAccountAfterPermission) {
                pendingAddAccountAfterPermission = false
                launchGoogleAddAccount()
            } else {
                viewModel.syncNow()
            }
        } else {
            pendingAddAccountAfterPermission = false
        }
    }
    val contactsPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasContactsPermission = granted ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (hasContactsPermission) {
            viewModel.setBirthdayCalendarEnabled(true) { result ->
                val imported = result.getOrNull()?.importedCount ?: 0
                Toast.makeText(context, "$imported Birthdays Imported", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Contacts access needed", Toast.LENGTH_SHORT).show()
        }
    }
    val onboardingCalendarPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        hasCalendarPermission = grants[Manifest.permission.READ_CALENDAR] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        if (hasCalendarPermission) viewModel.syncNow()
    }
    val onboardingNotificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasNotificationPermission = granted ||
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
    val onboardingContactsPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasContactsPermission = granted ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }
    var onboardingPageIndex by remember { mutableStateOf(0) }
    var selectedDateRestored by remember { mutableStateOf(false) }
    var calendarTab by remember { mutableStateOf<CalendarTab?>(null) }
    LaunchedEffect(storedCalendarTab) {
        if (calendarTab == null) calendarTab = storedCalendarTab
    }
    val activeCalendarTab = calendarTab ?: storedCalendarTab
    val systemDark = isSystemInDarkTheme()
    val resolvedThemeMode = themeMode
    val resolvedAccentColor = accentColor
    val palette = remember(resolvedThemeMode, resolvedAccentColor, systemDark) {
        dotCalPalette(resolvedThemeMode, resolvedAccentColor, systemDark)
    }
    SystemBarColorSync(palette)
    LaunchedEffect(resolvedThemeMode, resolvedAccentColor) {
        bootPreferences.edit()
            .putString(BOOT_THEME_KEY, resolvedThemeMode.name)
            .putString(BOOT_ACCENT_KEY, resolvedAccentColor.name)
            .apply()
    }
    LaunchedEffect(storedSelectedDateValue, initialEventId, initialTaskId, initialCalendarDate) {
        val storedValue = storedSelectedDateValue ?: return@LaunchedEffect
        if (!selectedDateRestored) {
            if (initialEventId == null && initialTaskId == null && initialCalendarDate == null && storedValue.isNotBlank()) {
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
    LaunchedEffect(initialRouteToken, initialEventId) {
        if (initialRouteToken != null && handledRouteToken != initialRouteToken && !initialEventId.isNullOrBlank()) {
            routePending = true
            viewModel.closeEventDetail()
            settingsScreen = SettingsScreen.Root
            previousScreenTab = ScreenTab.Calendar
            screenTab = ScreenTab.Calendar
            viewModel.openEventDetailById(initialEventId) {
                handledRouteToken = initialRouteToken
                routePending = false
            }
        }
    }
    LaunchedEffect(initialRouteToken, initialTaskId, tasks) {
        if (initialRouteToken != null && handledRouteToken != initialRouteToken && !initialTaskId.isNullOrBlank()) {
            routePending = true
            viewModel.closeEventDetail()
            settingsScreen = SettingsScreen.Root
            previousScreenTab = ScreenTab.Calendar
            screenTab = ScreenTab.Tasks
            tasks.firstOrNull { it.baseEventId() == initialTaskId || it.id == initialTaskId }?.let { task ->
                taskDetail = task
                handledTaskDeepLinkId = initialTaskId
                handledRouteToken = initialRouteToken
                routePending = false
            } ?: run {
                if (tasks.isNotEmpty()) routePending = false
            }
        }
    }
    LaunchedEffect(initialRouteToken, initialCalendarTab, initialCalendarDate) {
        val routedCalendarTab = CalendarTab.entries.firstOrNull { it.name.equals(initialCalendarTab, ignoreCase = true) }
        if (initialRouteToken != null && handledRouteToken != initialRouteToken && routedCalendarTab != null) {
            viewModel.closeEventDetail()
            taskDetail = null
            settingsScreen = SettingsScreen.Root
            previousScreenTab = ScreenTab.Calendar
            screenTab = ScreenTab.Calendar
            calendarTab = if (routedCalendarTab == CalendarTab.ThreeDay) CalendarTab.Month else routedCalendarTab
            initialCalendarDate?.let { date ->
                runCatching { LocalDate.parse(date) }.getOrNull()?.let(viewModel::selectDate)
            }
            handledRouteToken = initialRouteToken
            routePending = false
        }
    }
    LaunchedEffect(taskDetail) {
        taskDetail?.let { lastTaskDetail = it }
    }
    LaunchedEffect(Unit) {
        hasCalendarPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
        hasContactsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        hasNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }
    LaunchedEffect(birthdayEnabled, hasContactsPermission) {
        if (birthdayEnabled && hasContactsPermission) {
            viewModel.refreshBirthdayCalendarIfEnabled()
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
    fun openAddEditor(startTime: LocalTime = LocalTime.of(9, 0)) {
        editorSessionKey = UUID.randomUUID().toString()
        addStartTime = startTime
        editingEvent = null
        addSheet = true
    }
    LaunchedEffect(initialRouteToken, initialAddEvent) {
        if (initialRouteToken != null && handledRouteToken != initialRouteToken && initialAddEvent) {
            viewModel.closeEventDetail()
            taskDetail = null
            settingsScreen = SettingsScreen.Root
            previousScreenTab = ScreenTab.Calendar
            screenTab = ScreenTab.Calendar
            openAddEditor()
            handledRouteToken = initialRouteToken
            routePending = false
        }
    }
    fun openEditEditor(event: CalendarEvent) {
        editorSessionKey = UUID.randomUUID().toString()
        editingEvent = event
        addSheet = true
    }
    fun closeTopSurface() {
        when {
            addSheet -> {
                editingEvent = null
                addSheet = false
            }
            showTaskEditor -> {
                editingTask = null
                showTaskEditor = false
            }
            taskDetail != null -> taskDetail = null
            detailEvent != null -> viewModel.closeEventDetail()
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
    fun runSyncNow(showToast: Boolean = true) {
        if (isSyncing) return
        isSyncing = true
        viewModel.syncNow { result ->
            isSyncing = false
            if (showToast) {
                val message = if (result.isSuccess && result.getOrNull()?.permissionDenied != true) {
                    "Calendars synced"
                } else {
                    "Sync failed\nCheck your internet connection"
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val onboardingPreferenceLoaded = onboardingDone != null
    val showOnboarding = onboardingDone == false && initialRouteToken == null
    fun finishOnboarding() {
        scope.launch {
            context.calendarPreferencesDataStore.edit { preferences ->
                preferences[CalendarPreferences.KEY_ONBOARDING_DONE] = true
            }
        }
    }
    fun advanceOnboarding() {
        if (onboardingPageIndex < onboardingPages.lastIndex) {
            onboardingPageIndex += 1
        } else {
            finishOnboarding()
        }
    }
    fun requestCurrentOnboardingPermission() {
        when (onboardingPages[onboardingPageIndex]) {
            OnboardingPage.CalendarPermission -> {
                if (!hasCalendarPermission) {
                    onboardingCalendarPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_CALENDAR,
                            Manifest.permission.WRITE_CALENDAR,
                        ),
                    )
                } else {
                    advanceOnboarding()
                }
            }
            OnboardingPage.Notifications -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                    onboardingNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    advanceOnboarding()
                }
            }
            OnboardingPage.Contacts -> {
                if (!hasContactsPermission) {
                    onboardingContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                } else {
                    advanceOnboarding()
                }
            }
            OnboardingPage.Welcome,
            OnboardingPage.Ready -> advanceOnboarding()
        }
    }
    BackHandler(enabled = showOnboarding) {
        if (onboardingPageIndex > 0) onboardingPageIndex -= 1 else finishOnboarding()
    }
    BackHandler(enabled = !showOnboarding && (detailEvent != null || taskDetail != null || addSheet || showTaskEditor || screenTab == ScreenTab.Settings || screenTab == ScreenTab.Tasks)) {
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
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(16.dp)
                            .background(palette.topBarSurface),
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
                            weekStart = weekStartDay,
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
                            weekStart = weekStartDay,
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
                            onAdd = { openAddEditor() },
                            onEventClick = viewModel::openEventDetail,
                        )
                        CalendarTab.Year -> YearView(
                            selectedDate = selectedDate,
                            events = events,
                            palette = palette,
                            weekStart = weekStartDay,
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
                    ScreenTab.Tasks -> TasksScreen(
                        tasks = tasks,
                        reminders = reminders,
                        palette = palette,
                        onAddClick = {
                            editingTask = null
                            showTaskEditor = true
                        },
                        onTaskClick = { taskDetail = it },
                        onCompleteTask = viewModel::completeTask,
                        onDeleteTask = viewModel::deleteTask,
                    )
                    ScreenTab.Settings -> Unit
                }
            }
        }
        if (routePending) {
            Box(modifier = Modifier.fillMaxSize().background(palette.background))
        }
        if (!onboardingPreferenceLoaded && initialRouteToken == null) {
            Box(modifier = Modifier.fillMaxSize().background(palette.background))
        }
        AnimatedVisibility(
            visible = showOnboarding,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            OnboardingScreen(
                page = onboardingPages[onboardingPageIndex],
                pageIndex = onboardingPageIndex,
                pageCount = onboardingPages.size,
                palette = palette,
                hasCalendarPermission = hasCalendarPermission,
                hasNotificationPermission = hasNotificationPermission,
                hasContactsPermission = hasContactsPermission,
                onBack = { if (onboardingPageIndex > 0) onboardingPageIndex -= 1 else finishOnboarding() },
                onSkip = ::finishOnboarding,
                onPrimary = ::requestCurrentOnboardingPermission,
                onSecondary = ::advanceOnboarding,
            )
        }
        AnimatedVisibility(
            visible = screenTab == ScreenTab.Settings,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface).statusBarsPadding(),
        ) {
            SettingsPreview(
                themeMode = resolvedThemeMode,
                accentColor = resolvedAccentColor,
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
                        WidgetUpdateWorker.enqueue(context)
                    }
                },
                onAccentSelected = { selectedAccent ->
                    bootPreferences.edit().putString(BOOT_ACCENT_KEY, selectedAccent.name).apply()
                    scope.launch {
                        context.calendarPreferencesDataStore.edit { preferences ->
                            preferences[CalendarPreferences.KEY_ACCENT_COLOR] = selectedAccent.name
                        }
                        WidgetUpdateWorker.enqueue(context)
                    }
                },
                syncEnabled = syncEnabled,
                syncIntervalMins = syncIntervalMins,
                syncMetadata = syncMetadata,
                isSyncing = isSyncing,
                birthdayEnabled = birthdayEnabled,
                defaultReminderMinutes = defaultReminderMinutes,
                defaultAllDayReminderTime = defaultAllDayReminderTime,
                weekStartOption = weekStartOption,
                holidayCountries = holidayCountries,
                accounts = accounts,
                hasCalendarPermission = hasCalendarPermission,
                onSyncNow = { runSyncNow(showToast = true) },
                onAccountVisibilityChange = { accountId, visible ->
                    viewModel.setAccountVisible(accountId, visible)
                    runSyncNow(showToast = false)
                },
                onSyncEnabledChange = { enabled ->
                    scope.launch {
                        context.calendarPreferencesDataStore.edit { preferences ->
                            preferences[CalendarPreferences.KEY_SYNC_ENABLED] = enabled
                        }
                        if (enabled) {
                            CalendarSyncWorkScheduler.schedulePeriodic(context, syncIntervalMins)
                            runSyncNow(showToast = false)
                        } else {
                            CalendarSyncWorkScheduler.cancelPeriodic(context)
                        }
                    }
                },
                onSyncIntervalSelected = { interval ->
                    scope.launch {
                        context.calendarPreferencesDataStore.edit { preferences ->
                            preferences[CalendarPreferences.KEY_SYNC_INTERVAL_MINS] = interval
                        }
                        if (syncEnabled && interval > 0) {
                            CalendarSyncWorkScheduler.cancelPeriodic(context)
                            CalendarSyncWorkScheduler.schedulePeriodic(context, interval)
                        } else {
                            CalendarSyncWorkScheduler.cancelPeriodic(context)
                        }
                    }
                },
                onDefaultReminderSelected = { minutes ->
                    scope.launch {
                        context.calendarPreferencesDataStore.edit { preferences ->
                            if (minutes == null) {
                                preferences[CalendarPreferences.KEY_DEFAULT_REMINDER] = -1
                            } else {
                                preferences[CalendarPreferences.KEY_DEFAULT_REMINDER] = minutes
                            }
                        }
                    }
                },
                onDefaultAllDayReminderTimeSelected = { time ->
                    scope.launch {
                        context.calendarPreferencesDataStore.edit { preferences ->
                            preferences[CalendarPreferences.KEY_DEFAULT_ALL_DAY_REMINDER_TIME] = time.toString()
                        }
                    }
                },
                onWeekStartSelected = { option ->
                    scope.launch {
                        context.calendarPreferencesDataStore.edit { preferences ->
                            preferences[CalendarPreferences.KEY_WEEK_START] = option.storageKey
                        }
                    }
                },
                onBirthdayEnabledChange = { enabled ->
                    if (enabled) {
                        hasContactsPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                        if (hasContactsPermission) {
                            viewModel.setBirthdayCalendarEnabled(true) { result ->
                                val birthdayResult = result.getOrNull()
                                val message = if (result.isSuccess && birthdayResult?.permissionDenied != true) {
                                    "${birthdayResult?.importedCount ?: 0} Birthdays Imported"
                                } else {
                                    "Contacts access needed"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    } else {
                        viewModel.setBirthdayCalendarEnabled(false) {
                            Toast.makeText(context, "Birthdays disabled", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onAddHolidayCountry = { item ->
                    viewModel.addHolidayCountry(item) { result ->
                        if (result.isFailure) {
                            Toast.makeText(context, "Could not add holidays", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onRemoveHolidayCountry = { item ->
                    viewModel.removeHolidayCountry(item) { result ->
                        if (result.isFailure) {
                            Toast.makeText(context, "Could not remove holidays", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onRateDotCal = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.dotfield.dotcal")),
                    )
                },
                onRequestCalendarAccess = {
                    if (hasCalendarPermission) {
                        runSyncNow(showToast = false)
                        settingsScreen = SettingsScreen.CalendarAccounts
                    } else {
                        if (calendarPermissionRequested) {
                            context.startActivity(
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                },
                            )
                        } else {
                            calendarPermissionRequested = true
                            calendarPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_CALENDAR,
                                    Manifest.permission.WRITE_CALENDAR,
                                ),
                            )
                        }
                    }
                },
                onAddAccount = {
                    if (hasCalendarPermission) {
                        launchGoogleAddAccount()
                    } else {
                        pendingAddAccountAfterPermission = true
                        calendarPermissionRequested = true
                        calendarPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.READ_CALENDAR,
                                Manifest.permission.WRITE_CALENDAR,
                            ),
                        )
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
                        openEditEditor(event)
                        viewModel.closeEventDetail()
                    },
                    onDelete = {
                        pendingDelete = PendingDelete(event, RecurringEditScope.WholeSeries, DeleteSource.Detail)
                    },
                )
            }
        }
        AnimatedVisibility(
            visible = taskDetail != null,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            lastTaskDetail?.let { task ->
                TaskDetailScreen(
                    task = task,
                    reminder = reminders.firstOrNull { it.eventId == task.baseEventId() },
                    palette = palette,
                    onBack = { taskDetail = null },
                    onEdit = {
                        editingTask = task
                        showTaskEditor = true
                    },
                    onComplete = {
                        if (task.isCompleted == 1) {
                            viewModel.reopenTask(task)
                        } else {
                            viewModel.completeTask(task)
                        }
                        taskDetail = null
                    },
                    onDelete = {
                        pendingTaskDelete = task
                    },
                )
            }
        }
        if (showTaskEditor) {
            TaskEditorSheet(
                task = editingTask,
                initialReminder = editingTask?.let { task -> reminders.firstOrNull { it.eventId == task.baseEventId() } },
                palette = palette,
                onDismiss = {
                    editingTask = null
                    showTaskEditor = false
                },
                onSave = { data ->
                    viewModel.saveTask(editingTask, data) {
                        editingTask = null
                        showTaskEditor = false
                    }
                },
                onDelete = editingTask?.let { task ->
                    {
                        pendingTaskDelete = task
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
                initialReminderMinutes = if (editingEvent == null) {
                    defaultReminderMinutes
                } else {
                    reminders.firstOrNull { it.eventId == editingEvent?.baseEventId() }?.minutesBefore
                },
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
                        pendingDelete = PendingDelete(eventToDelete, scope, DeleteSource.Editor)
                    }
                },
            )
        }
        pendingDelete?.let { request ->
            ConfirmDeleteDialog(
                deleteSeries = request.scope == RecurringEditScope.WholeSeries && !request.event.rrule.isNullOrBlank(),
                palette = palette,
                onDismiss = { pendingDelete = null },
                onConfirm = {
                    viewModel.deleteEvent(request.event, request.scope)
                    when (request.source) {
                        DeleteSource.Editor -> {
                            editingEvent = null
                            addSheet = false
                        }
                        DeleteSource.Detail -> viewModel.closeEventDetail()
                    }
                    pendingDelete = null
                },
            )
        }
        pendingTaskDelete?.let { task ->
            ConfirmDeleteDialog(
                title = if (!task.rrule.isNullOrBlank()) "Delete task series?" else "Delete task?",
                confirmLabel = if (!task.rrule.isNullOrBlank()) "Delete series" else "Delete",
                palette = palette,
                onDismiss = { pendingTaskDelete = null },
                onConfirm = {
                    viewModel.deleteTask(task)
                    taskDetail = null
                    editingTask = null
                    showTaskEditor = false
                    pendingTaskDelete = null
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
                openAddEditor()
            },
            onEdit = {
                showSheet = false
                viewModel.openEventDetail(it)
            },
        )
    }

}

@Composable
private fun ConfirmDeleteDialog(
    deleteSeries: Boolean,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    ConfirmDeleteDialog(
        title = if (deleteSeries) "Delete series?" else "Delete event?",
        confirmLabel = if (deleteSeries) "Delete series" else "Delete",
        palette = palette,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    confirmLabel: String,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        titleContentColor = palette.primaryText,
        textContentColor = palette.secondaryText,
        title = { Text(title) },
        text = { Text("This cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = palette.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.primaryText)
            }
        },
    )
}

@Composable
private fun OnboardingScreen(
    page: OnboardingPage,
    pageIndex: Int,
    pageCount: Int,
    palette: DotCalPalette,
    hasCalendarPermission: Boolean,
    hasNotificationPermission: Boolean,
    hasContactsPermission: Boolean,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
) {
    val copy = onboardingCopy(
        page = page,
        hasCalendarPermission = hasCalendarPermission,
        hasNotificationPermission = hasNotificationPermission,
        hasContactsPermission = hasContactsPermission,
    )
    val colors = remember(palette.isDark) { onboardingColors(palette.isDark) }
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 14.dp),
    ) {
        val compactHeight = maxHeight < 720.dp
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().height(46.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(42.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.primaryText)
                }
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = onSkip) {
                    Text("Skip", color = colors.secondaryText, fontFamily = mono, fontSize = 14.sp)
                }
            }
            Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 12.dp))
            OnboardingHero(
                page = page,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (compactHeight) 0.9f else 1.05f),
            )
            Spacer(modifier = Modifier.height(if (compactHeight) 28.dp else 64.dp))
            Text(
                text = copy.label,
                color = colors.accent,
                fontFamily = mono,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = copy.title,
                color = colors.primaryText,
                fontFamily = mono,
                fontSize = if (compactHeight) 32.sp else 36.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = if (compactHeight) 36.sp else 40.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 12.dp))
            Text(
                text = copy.description,
                color = colors.secondaryText,
                fontFamily = mono,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                maxLines = if (compactHeight) 2 else 3,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.weight(1f))
            OnboardingProgress(pageIndex = pageIndex, pageCount = pageCount, colors = colors)
            Spacer(modifier = Modifier.height(if (compactHeight) 14.dp else 20.dp))
            Button(
                onClick = onPrimary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = Color.White,
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(if (compactHeight) 54.dp else 60.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
            ) {
                Text(copy.primaryLabel, fontFamily = mono, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            if (page != OnboardingPage.Welcome && page != OnboardingPage.Ready) {
                TextButton(
                    onClick = onSecondary,
                    modifier = Modifier.fillMaxWidth().height(if (compactHeight) 44.dp else 48.dp),
                ) {
                    Text("Not Now", color = colors.secondaryText, fontFamily = mono, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            } else {
                Spacer(modifier = Modifier.height(if (compactHeight) 44.dp else 48.dp))
            }
        }
    }
}

private data class OnboardingCopy(
    val label: String,
    val title: String,
    val description: String,
    val primaryLabel: String,
)

private data class OnboardingColors(
    val background: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val mutedText: Color,
    val accent: Color,
    val glow: Color,
    val shadow: Color,
    val line: Color,
    val isDark: Boolean,
)

private fun onboardingCopy(
    page: OnboardingPage,
    hasCalendarPermission: Boolean,
    hasNotificationPermission: Boolean,
    hasContactsPermission: Boolean,
): OnboardingCopy {
    return when (page) {
        OnboardingPage.Welcome -> OnboardingCopy(
            label = "DOTCAL",
            title = "DotCal",
            description = "A focused calendar for events, tasks, reminders, birthdays, and widgets.",
            primaryLabel = "Continue",
        )
        OnboardingPage.CalendarPermission -> OnboardingCopy(
            label = "OPTIONAL",
            title = "Calendar Access",
            description = "Connect device calendars for local CalendarProvider sync. Local DotCal events still work without it.",
            primaryLabel = if (hasCalendarPermission) "Continue" else "Allow Calendar",
        )
        OnboardingPage.Notifications -> OnboardingCopy(
            label = "OPTIONAL",
            title = "Reminders",
            description = "Allow notifications so event and task reminders can appear at the scheduled time.",
            primaryLabel = if (hasNotificationPermission) "Continue" else "Allow Reminders",
        )
        OnboardingPage.Contacts -> OnboardingCopy(
            label = "OPTIONAL",
            title = "Birthdays",
            description = "Allow contacts to import birthdays as read-only yearly events. You can skip this now.",
            primaryLabel = if (hasContactsPermission) "Continue" else "Allow Contacts",
        )
        OnboardingPage.Ready -> OnboardingCopy(
            label = "READY",
            title = "You're all set",
            description = "Your calendar is ready.",
            primaryLabel = "Start",
        )
    }
}

private fun onboardingColors(isDark: Boolean): OnboardingColors {
    return if (isDark) {
        OnboardingColors(
            background = Color(0xFF0B0B0D),
            surface = Color(0xFF121216),
            elevatedSurface = Color(0xFF1A1A20),
            primaryText = Color.White,
            secondaryText = Color(0xFF9CA3AF),
            mutedText = Color(0xFF5B626E),
            accent = Color(0xFFFF3B30),
            glow = Color(0xFFFF3B30).copy(alpha = 0.22f),
            shadow = Color.Black.copy(alpha = 0.55f),
            line = Color(0xFF2A2A32),
            isDark = true,
        )
    } else {
        OnboardingColors(
            background = Color(0xFFFAFAFA),
            surface = Color.White,
            elevatedSurface = Color(0xFFF7F7F8),
            primaryText = Color(0xFF111111),
            secondaryText = Color(0xFF6B7280),
            mutedText = Color(0xFFD1D5DB),
            accent = Color(0xFFFF3B30),
            glow = Color(0xFFFF3B30).copy(alpha = 0.13f),
            shadow = Color(0xFF111827).copy(alpha = 0.13f),
            line = Color(0xFFE5E7EB),
            isDark = false,
        )
    }
}

@Composable
private fun OnboardingHero(page: OnboardingPage, colors: OnboardingColors, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .then(modifier)
            .heightIn(min = 270.dp, max = 360.dp),
        contentAlignment = Alignment.Center,
    ) {
        val imageRes = if (page == OnboardingPage.Ready) {
            com.dotfield.dotcal.R.drawable.both5
        } else if (colors.isDark) {
            when (page) {
                OnboardingPage.Welcome -> com.dotfield.dotcal.R.drawable.dark1
                OnboardingPage.CalendarPermission -> com.dotfield.dotcal.R.drawable.dark2
                OnboardingPage.Notifications -> com.dotfield.dotcal.R.drawable.dark3
                OnboardingPage.Contacts -> com.dotfield.dotcal.R.drawable.dark4
                else -> com.dotfield.dotcal.R.drawable.both5
            }
        } else {
            when (page) {
                OnboardingPage.Welcome -> com.dotfield.dotcal.R.drawable.screen1
                OnboardingPage.CalendarPermission -> com.dotfield.dotcal.R.drawable.screen2
                OnboardingPage.Notifications -> com.dotfield.dotcal.R.drawable.screen3
                OnboardingPage.Contacts -> com.dotfield.dotcal.R.drawable.screen4
                else -> com.dotfield.dotcal.R.drawable.both5
            }
        }

        Image(
            painter = androidx.compose.ui.res.painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.72f)
                .offset(y = 4.dp)
                .padding(vertical = 12.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun OnboardingProgress(pageIndex: Int, pageCount: Int, colors: OnboardingColors) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${pageIndex + 1}",
                color = colors.accent,
                fontFamily = mono,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = " / $pageCount",
                color = colors.primaryText,
                fontFamily = mono,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pageCount) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (index == pageIndex) colors.accent else colors.mutedText.copy(alpha = if (colors.isDark) 0.55f else 0.75f)),
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeroAtmosphere(colors: OnboardingColors) {
    val center = Offset(size.width * 0.52f, size.height * 0.47f)
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(colors.glow, Color.Transparent),
            center = center,
            radius = size.minDimension * 0.52f,
        ),
        radius = size.minDimension * 0.52f,
        center = center,
    )
    repeat(5) { index ->
        val x = size.width * (0.18f + index * 0.16f)
        val y = size.height * (0.22f + (index % 3) * 0.18f)
        drawCircle(colors.accent.copy(alpha = 0.75f - index * 0.08f), radius = 2.2.dp.toPx(), center = Offset(x, y))
    }
    repeat(3) { index ->
        val x = size.width * (0.22f + index * 0.28f)
        val y = size.height * (0.74f - index * 0.08f)
        drawCircle(colors.mutedText.copy(alpha = 0.35f), radius = 1.6.dp.toPx(), center = Offset(x, y))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCalendarHero(colors: OnboardingColors) {
    drawLeafCluster(colors, Offset(size.width * 0.15f, size.height * 0.68f), -1f)
    drawLeafCluster(colors, Offset(size.width * 0.85f, size.height * 0.68f), 1f)
    drawSoftShadow(colors, Offset(size.width * 0.5f, size.height * 0.78f), size.width * 0.34f, size.height * 0.055f)
    
    val left = size.width * 0.19f
    val top = size.height * 0.28f
    val width = size.width * 0.56f
    val height = size.height * 0.42f
    
    val standPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(left + width - 12.dp.toPx(), top + 10.dp.toPx())
        lineTo(left + width + 24.dp.toPx(), top + height - 4.dp.toPx())
        lineTo(left + width, top + height)
        close()
    }
    drawPath(standPath, colors.elevatedSurface)
    drawPath(standPath, colors.line.copy(alpha = 0.55f), style = Stroke(1.dp.toPx()))

    drawRoundRect(
        color = colors.line.copy(alpha = 0.4f),
        topLeft = Offset(left + 3.dp.toPx(), top - 5.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(width - 6.dp.toPx(), height),
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
    )
    drawRoundRect(
        color = colors.elevatedSurface,
        topLeft = Offset(left + 6.dp.toPx(), top - 2.dp.toPx()),
        size = androidx.compose.ui.geometry.Size(width - 12.dp.toPx(), height),
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
    )

    drawRoundRect(colors.surface, Offset(left, top), androidx.compose.ui.geometry.Size(width, height), CornerRadius(16.dp.toPx(), 16.dp.toPx()))
    drawRoundRect(colors.line.copy(alpha = 0.55f), Offset(left, top), androidx.compose.ui.geometry.Size(width, height), CornerRadius(16.dp.toPx(), 16.dp.toPx()), style = Stroke(1.dp.toPx()))
    
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(colors.accent, Color(0xFFFF2A22))),
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(width, height * 0.24f),
        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
    )
    drawRect(
        brush = Brush.verticalGradient(listOf(colors.accent, Color(0xFFFF2A22))),
        topLeft = Offset(left, top + height * 0.12f),
        size = androidx.compose.ui.geometry.Size(width, height * 0.12f)
    )

    val ringCount = 5
    val ringWidth = 10.dp.toPx()
    val ringHeight = 18.dp.toPx()
    repeat(ringCount) { i ->
        val x = left + width * (0.16f + i * 0.17f)
        drawCircle(
            color = Color.Black.copy(alpha = 0.3f),
            radius = 2.dp.toPx(),
            center = Offset(x, top + height * 0.08f)
        )
        drawArc(
            color = colors.primaryText.copy(alpha = 0.85f),
            startAngle = 180f,
            sweepAngle = 200f,
            useCenter = false,
            topLeft = Offset(x - ringWidth / 2f, top - 10.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(ringWidth, ringHeight),
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    val colStep = width * 0.17f
    val rowStep = height * 0.16f
    val gridStartX = left + width * 0.16f
    val gridStartY = top + height * 0.35f
    val cellW = colStep * 0.65f
    val cellH = rowStep * 0.65f

    repeat(3) { row ->
        repeat(4) { col ->
            val x = gridStartX + col * colStep
            val y = gridStartY + row * rowStep
            val active = row == 2 && col == 2
            
            drawRoundRect(
                color = if (active) colors.accent else colors.line.copy(alpha = if (colors.isDark) 0.6f else 0.4f),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(cellW, cellH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
            
            if (active) {
                val checkPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(x + cellW * 0.25f, y + cellH * 0.5f)
                    lineTo(x + cellW * 0.45f, y + cellH * 0.7f)
                    lineTo(x + cellW * 0.75f, y + cellH * 0.3f)
                }
                drawPath(
                    path = checkPath,
                    color = Color.White,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            } else if (row == 0 && col == 2) {
                val checkPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(x + cellW * 0.25f, y + cellH * 0.5f)
                    lineTo(x + cellW * 0.45f, y + cellH * 0.7f)
                    lineTo(x + cellW * 0.75f, y + cellH * 0.3f)
                }
                drawPath(
                    path = checkPath,
                    color = colors.accent.copy(alpha = 0.55f),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            } else if (row == 1 && col == 1) {
                val checkPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(x + cellW * 0.25f, y + cellH * 0.5f)
                    lineTo(x + cellW * 0.45f, y + cellH * 0.7f)
                    lineTo(x + cellW * 0.75f, y + cellH * 0.3f)
                }
                drawPath(
                    path = checkPath,
                    color = colors.secondaryText.copy(alpha = 0.45f),
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
            } else if (row == 1 && col == 3) {
                drawCircle(
                    color = colors.accent.copy(alpha = 0.85f),
                    radius = 3.dp.toPx(),
                    center = Offset(x + cellW / 2f, y + cellH / 2f)
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCalendarHubHero(colors: OnboardingColors) {
    val center = Offset(size.width * 0.5f, size.height * 0.48f)
    drawConnectionDots(colors, center, Offset(size.width * 0.22f, size.height * 0.28f))
    drawConnectionDots(colors, center, Offset(size.width * 0.78f, size.height * 0.3f))
    drawConnectionDots(colors, center, Offset(size.width * 0.23f, size.height * 0.68f))
    drawConnectionDots(colors, center, Offset(size.width * 0.76f, size.height * 0.64f))

    drawFloatingCard(colors, Offset(size.width * 0.06f, size.height * 0.2f), size.width * 0.34f, "calendar", "Team Meeting", "10:00 AM")
    drawFloatingCard(colors, Offset(size.width * 0.61f, size.height * 0.22f), size.width * 0.34f, "calendar", "Project Review", "2:30 PM")
    drawFloatingCard(colors, Offset(size.width * 0.08f, size.height * 0.62f), size.width * 0.36f, "calendar", "Dinner with Alex", "7:00 PM")
    drawFloatingCard(colors, Offset(size.width * 0.58f, size.height * 0.58f), size.width * 0.34f, "grid")

    drawSoftShadow(colors, Offset(center.x, size.height * 0.76f), size.width * 0.22f, size.height * 0.04f)
    drawRoundRect(colors.surface, Offset(center.x - size.width * 0.18f, center.y - size.height * 0.16f), androidx.compose.ui.geometry.Size(size.width * 0.36f, size.height * 0.32f), CornerRadius(22.dp.toPx(), 22.dp.toPx()))
    drawRoundRect(colors.line.copy(alpha = 0.5f), Offset(center.x - size.width * 0.18f, center.y - size.height * 0.16f), androidx.compose.ui.geometry.Size(size.width * 0.36f, size.height * 0.32f), CornerRadius(22.dp.toPx(), 22.dp.toPx()), style = Stroke(1.dp.toPx()))
    drawRoundRect(colors.accent, Offset(center.x - size.width * 0.18f, center.y - size.height * 0.16f), androidx.compose.ui.geometry.Size(size.width * 0.36f, size.height * 0.09f), CornerRadius(22.dp.toPx(), 22.dp.toPx()))
    repeat(3) { i ->
        val x = center.x - size.width * 0.08f + i * size.width * 0.08f
        drawLine(colors.primaryText.copy(alpha = 0.35f), Offset(x, center.y - size.height * 0.19f), Offset(x, center.y - size.height * 0.105f), strokeWidth = 3.dp.toPx())
    }
    repeat(3) { row ->
        repeat(3) { col ->
            val x = center.x - size.width * 0.09f + col * size.width * 0.09f
            val y = center.y - size.height * 0.02f + row * size.height * 0.075f
            drawRoundRect(if (row == 1 && col == 1) colors.accent else colors.line, Offset(x, y), androidx.compose.ui.geometry.Size(14.dp.toPx(), 14.dp.toPx()), CornerRadius(4.dp.toPx(), 4.dp.toPx()))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawReminderHero(colors: OnboardingColors) {
    val c = Offset(size.width * 0.52f, size.height * 0.44f)
    repeat(4) { index ->
        drawCircle(colors.accent.copy(alpha = 0.06f - index * 0.01f), radius = size.minDimension * (0.2f + index * 0.09f), center = c, style = Stroke(1.dp.toPx()))
    }
    drawLeafCluster(colors, Offset(size.width * 0.13f, size.height * 0.7f), -1f)
    drawLeafCluster(colors, Offset(size.width * 0.88f, size.height * 0.64f), 1f)
    drawFloatingCard(colors, Offset(size.width * 0.05f, size.height * 0.18f), size.width * 0.35f, "bell", "Meeting", "in 10 min")
    drawFloatingCard(colors, Offset(size.width * 0.58f, size.height * 0.66f), size.width * 0.36f, "check", "Buy groceries", "Today, 6:00 PM")
    drawSoftShadow(colors, Offset(c.x, size.height * 0.72f), size.width * 0.25f, size.height * 0.05f)
    drawCircle(colors.accent.copy(alpha = 0.34f), size.width * 0.19f, c)
    drawRoundRect(
        brush = Brush.verticalGradient(listOf(Color(0xFFFF6A60), colors.accent, Color(0xFFD82018))),
        topLeft = Offset(c.x - size.width * 0.15f, c.y - size.height * 0.1f),
        size = androidx.compose.ui.geometry.Size(size.width * 0.3f, size.height * 0.26f),
        cornerRadius = CornerRadius(90.dp.toPx(), 90.dp.toPx()),
    )
    drawRoundRect(Color(0xFFFF6A60), Offset(c.x - size.width * 0.05f, c.y - size.height * 0.15f), androidx.compose.ui.geometry.Size(size.width * 0.1f, size.height * 0.06f), CornerRadius(12.dp.toPx(), 12.dp.toPx()))
    drawRoundRect(colors.accent, Offset(c.x - size.width * 0.2f, c.y + size.height * 0.12f), androidx.compose.ui.geometry.Size(size.width * 0.4f, size.height * 0.06f), CornerRadius(26.dp.toPx(), 26.dp.toPx()))
    drawCircle(Color(0xFFD82018), 10.dp.toPx(), Offset(c.x, c.y + size.height * 0.19f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBirthdayHero(colors: OnboardingColors) {
    val cardLeft = size.width * 0.18f
    val cardTop = size.height * 0.36f
    drawSoftShadow(colors, Offset(size.width * 0.5f, size.height * 0.76f), size.width * 0.32f, size.height * 0.055f)
    drawRoundRect(colors.surface, Offset(cardLeft, cardTop), androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.25f), CornerRadius(26.dp.toPx(), 26.dp.toPx()))
    drawRoundRect(colors.line.copy(alpha = 0.75f), Offset(cardLeft, cardTop), androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.25f), CornerRadius(26.dp.toPx(), 26.dp.toPx()), style = Stroke(1.dp.toPx()))
    
    val avatarCenter = Offset(cardLeft + size.width * 0.12f, cardTop + size.height * 0.11f)
    drawCircle(colors.accent.copy(alpha = 0.85f), 22.dp.toPx(), avatarCenter)
    drawCircle(colors.surface, 8.dp.toPx(), avatarCenter - Offset(0f, 3.dp.toPx()))
    drawRoundRect(colors.surface, Offset(avatarCenter.x - 9.dp.toPx(), avatarCenter.y + 2.dp.toPx()), androidx.compose.ui.geometry.Size(18.dp.toPx(), 11.dp.toPx()), CornerRadius(6.dp.toPx(), 6.dp.toPx()))
    
    val nativeCanvas = drawContext.canvas.nativeCanvas
    val textStartX = cardLeft + size.width * 0.22f
    val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colors.primaryText.toArgb()
        textSize = 10.sp.toPx()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colors.secondaryText.toArgb()
        textSize = 8.sp.toPx()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }
    val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colors.accent.toArgb()
        textSize = 9.sp.toPx()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    nativeCanvas.drawText("Alex Smith", textStartX, cardTop + size.height * 0.08f, namePaint)
    nativeCanvas.drawText("Birthday", textStartX, cardTop + size.height * 0.14f, subtitlePaint)
    nativeCanvas.drawText("May 20", textStartX, cardTop + size.height * 0.21f, datePaint)

    drawCake(colors, Offset(cardLeft + size.width * 0.44f, cardTop - size.height * 0.06f))
    drawGift(colors, Offset(cardLeft + size.width * 0.55f, cardTop + size.height * 0.15f))
    drawLockBadge(colors, Offset(cardLeft + size.width * 0.58f, cardTop + size.height * 0.21f))
    drawLeafCluster(colors, Offset(size.width * 0.18f, size.height * 0.68f), -1f)
    drawLeafCluster(colors, Offset(size.width * 0.82f, size.height * 0.65f), 1f)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawReadyHero(colors: OnboardingColors) {
    val c = Offset(size.width * 0.5f, size.height * 0.47f)
    repeat(5) { index ->
        drawCircle(colors.accent.copy(alpha = 0.1f - index * 0.014f), size.minDimension * (0.18f + index * 0.075f), c)
    }
    drawCircle(
        brush = Brush.verticalGradient(listOf(Color(0xFFFF7168), colors.accent, Color(0xFFD71912))),
        radius = size.minDimension * 0.18f,
        center = c,
    )
    val checkPath = androidx.compose.ui.graphics.Path().apply {
        moveTo(c.x - 28.dp.toPx(), c.y - 2.dp.toPx())
        lineTo(c.x - 7.dp.toPx(), c.y + 19.dp.toPx())
        lineTo(c.x + 28.dp.toPx(), c.y - 19.dp.toPx())
    }
    drawPath(
        path = checkPath,
        color = Color.White,
        style = Stroke(width = 7.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
    )
    repeat(8) { index ->
        val angle = index * 0.78f
        val x = c.x + kotlin.math.cos(angle) * size.width * 0.33f
        val y = c.y + kotlin.math.sin(angle) * size.height * 0.27f
        if (index % 2 == 0) {
            drawCircle(colors.accent.copy(alpha = 0.8f), 2.5.dp.toPx(), Offset(x, y))
        } else {
            val starPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(x, y - 6.dp.toPx())
                lineTo(x + 2.dp.toPx(), y - 2.dp.toPx())
                lineTo(x + 6.dp.toPx(), y)
                lineTo(x + 2.dp.toPx(), y + 2.dp.toPx())
                lineTo(x, y + 6.dp.toPx())
                lineTo(x - 2.dp.toPx(), y + 2.dp.toPx())
                lineTo(x - 6.dp.toPx(), y)
                lineTo(x - 2.dp.toPx(), y - 2.dp.toPx())
                close()
            }
            drawPath(starPath, colors.accent.copy(alpha = 0.65f))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFloatingCard(
    colors: OnboardingColors,
    topLeft: Offset,
    width: Float,
    type: String,
    titleText: String = "",
    subtitleText: String = ""
) {
    val height = width * 0.42f
    drawRoundRect(colors.shadow, topLeft + Offset(0f, 6.dp.toPx()), androidx.compose.ui.geometry.Size(width, height), CornerRadius(12.dp.toPx(), 12.dp.toPx()))
    drawRoundRect(colors.surface, topLeft, androidx.compose.ui.geometry.Size(width, height), CornerRadius(12.dp.toPx(), 12.dp.toPx()))
    drawRoundRect(colors.line.copy(alpha = 0.7f), topLeft, androidx.compose.ui.geometry.Size(width, height), CornerRadius(12.dp.toPx(), 12.dp.toPx()), style = Stroke(1.dp.toPx()))
    val dotCenter = topLeft + Offset(15.dp.toPx(), height * 0.5f)
    if (type == "grid") {
        val gridStart = topLeft + Offset(14.dp.toPx(), height * 0.22f)
        val gridWidth = width - 28.dp.toPx()
        val gridHeight = height * 0.56f
        repeat(3) { row ->
            repeat(4) { col ->
                val dotX = gridStart.x + col * (gridWidth / 3f)
                val dotY = gridStart.y + row * (gridHeight / 2f)
                drawCircle(
                    color = if (row == 1 && col == 2) colors.accent else colors.line.copy(alpha = if (colors.isDark) 0.8f else 0.6f),
                    radius = 2.dp.toPx(),
                    center = Offset(dotX, dotY)
                )
            }
        }
    } else {
        when (type) {
            "calendar" -> drawMiniCalendarIcon(colors, dotCenter)
            "bell" -> drawMiniBellIcon(colors, dotCenter)
            "check" -> drawMiniCheckIcon(colors, dotCenter)
            else -> drawCircle(colors.accent, 5.dp.toPx(), dotCenter)
        }
        if (titleText.isNotEmpty()) {
            val nativeCanvas = drawContext.canvas.nativeCanvas
            val textStartX = topLeft.x + 29.dp.toPx()
            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.primaryText.toArgb()
                textSize = 9.sp.toPx()
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = colors.secondaryText.toArgb()
                textSize = 7.5.sp.toPx()
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }
            nativeCanvas.drawText(titleText, textStartX, topLeft.y + height * 0.44f, titlePaint)
            if (subtitleText.isNotEmpty()) {
                nativeCanvas.drawText(subtitleText, textStartX, topLeft.y + height * 0.78f, subtitlePaint)
            }
        } else {
            drawRoundRect(colors.primaryText.copy(alpha = if (colors.isDark) 0.8f else 0.55f), topLeft + Offset(30.dp.toPx(), height * 0.28f), androidx.compose.ui.geometry.Size(width * 0.42f, 4.dp.toPx()), CornerRadius(2.dp.toPx(), 2.dp.toPx()))
            drawRoundRect(colors.secondaryText.copy(alpha = 0.55f), topLeft + Offset(30.dp.toPx(), height * 0.54f), androidx.compose.ui.geometry.Size(width * 0.32f, 4.dp.toPx()), CornerRadius(2.dp.toPx(), 2.dp.toPx()))
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawConnectionDots(colors: OnboardingColors, start: Offset, end: Offset) {
    repeat(9) { index ->
        val t = index / 8f
        val x = start.x + (end.x - start.x) * t
        val y = start.y + (end.y - start.y) * t
        drawCircle(if (index % 2 == 0) colors.accent.copy(alpha = 0.7f) else colors.line, 1.8.dp.toPx(), Offset(x, y))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSoftShadow(colors: OnboardingColors, center: Offset, width: Float, height: Float) {
    drawOval(colors.shadow, topLeft = Offset(center.x - width / 2f, center.y - height / 2f), size = androidx.compose.ui.geometry.Size(width, height))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCake(colors: OnboardingColors, topLeft: Offset) {
    drawRoundRect(colors.accent, topLeft + Offset(0f, 18.dp.toPx()), androidx.compose.ui.geometry.Size(58.dp.toPx(), 26.dp.toPx()), CornerRadius(8.dp.toPx(), 8.dp.toPx()))
    drawRoundRect(colors.surface, topLeft + Offset(7.dp.toPx(), 8.dp.toPx()), androidx.compose.ui.geometry.Size(44.dp.toPx(), 14.dp.toPx()), CornerRadius(6.dp.toPx(), 6.dp.toPx()))
    repeat(3) { index ->
        val x = topLeft.x + 15.dp.toPx() + index * 14.dp.toPx()
        drawLine(colors.accent, Offset(x, topLeft.y + 2.dp.toPx()), Offset(x, topLeft.y + 11.dp.toPx()), strokeWidth = 2.dp.toPx())
        drawCircle(Color(0xFFFFD166), 2.dp.toPx(), Offset(x, topLeft.y))
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGift(colors: OnboardingColors, topLeft: Offset) {
    drawCircle(colors.shadow, 24.dp.toPx(), topLeft + Offset(22.dp.toPx(), 24.dp.toPx()))
    drawRoundRect(colors.elevatedSurface, topLeft, androidx.compose.ui.geometry.Size(44.dp.toPx(), 44.dp.toPx()), CornerRadius(14.dp.toPx(), 14.dp.toPx()))
    drawLine(colors.accent, topLeft + Offset(22.dp.toPx(), 4.dp.toPx()), topLeft + Offset(22.dp.toPx(), 39.dp.toPx()), strokeWidth = 4.dp.toPx())
    drawLine(colors.accent, topLeft + Offset(8.dp.toPx(), 18.dp.toPx()), topLeft + Offset(36.dp.toPx(), 18.dp.toPx()), strokeWidth = 4.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniCalendarIcon(colors: OnboardingColors, center: Offset) {
    val s = 14.dp.toPx()
    val left = center.x - s / 2f
    val top = center.y - s / 2f
    drawRoundRect(colors.accent, Offset(left, top), androidx.compose.ui.geometry.Size(s, s), CornerRadius(3.dp.toPx(), 3.dp.toPx()))
    drawRoundRect(Color.White.copy(alpha = 0.95f), Offset(left + 3.dp.toPx(), top + 5.dp.toPx()), androidx.compose.ui.geometry.Size(8.dp.toPx(), 6.dp.toPx()), CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx()))
    drawCircle(colors.accent, 1.2.dp.toPx(), Offset(left + 6.dp.toPx(), top + 8.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniBellIcon(colors: OnboardingColors, center: Offset) {
    drawCircle(colors.accent, 8.dp.toPx(), center)
    drawRoundRect(Color.White, Offset(center.x - 4.5.dp.toPx(), center.y - 4.dp.toPx()), androidx.compose.ui.geometry.Size(9.dp.toPx(), 9.dp.toPx()), CornerRadius(7.dp.toPx(), 7.dp.toPx()))
    drawRoundRect(Color.White, Offset(center.x - 7.dp.toPx(), center.y + 3.dp.toPx()), androidx.compose.ui.geometry.Size(14.dp.toPx(), 3.dp.toPx()), CornerRadius(2.dp.toPx(), 2.dp.toPx()))
    drawCircle(Color.White, 1.8.dp.toPx(), Offset(center.x, center.y + 8.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniCheckIcon(colors: OnboardingColors, center: Offset) {
    drawCircle(colors.accent, 8.dp.toPx(), center)
    drawLine(Color.White, center + Offset(-4.dp.toPx(), 0f), center + Offset(-1.dp.toPx(), 4.dp.toPx()), strokeWidth = 2.dp.toPx())
    drawLine(Color.White, center + Offset(-1.dp.toPx(), 4.dp.toPx()), center + Offset(5.dp.toPx(), -5.dp.toPx()), strokeWidth = 2.dp.toPx())
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLockBadge(colors: OnboardingColors, center: Offset) {
    drawCircle(colors.shadow, 24.dp.toPx(), center + Offset(0f, 5.dp.toPx()))
    drawCircle(colors.elevatedSurface, 23.dp.toPx(), center)
    drawCircle(colors.accent, 15.dp.toPx(), center)
    drawRoundRect(Color.White, Offset(center.x - 6.dp.toPx(), center.y - 1.dp.toPx()), androidx.compose.ui.geometry.Size(12.dp.toPx(), 10.dp.toPx()), CornerRadius(3.dp.toPx(), 3.dp.toPx()))
    drawArc(Color.White, startAngle = 200f, sweepAngle = 140f, useCenter = false, topLeft = Offset(center.x - 7.dp.toPx(), center.y - 10.dp.toPx()), size = androidx.compose.ui.geometry.Size(14.dp.toPx(), 14.dp.toPx()), style = Stroke(2.dp.toPx()))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLeafCluster(
    colors: OnboardingColors,
    origin: Offset,
    direction: Float
) {
    val leafColor = if (direction < 0) {
        colors.accent.copy(alpha = 0.35f)
    } else {
        colors.mutedText.copy(alpha = 0.35f)
    }
    
    val leafCount = 4
    repeat(leafCount) { index ->
        val angle = if (direction < 0) {
            -45f + index * 20f
        } else {
            15f + index * 20f
        }
        val distance = (index * 8).dp.toPx()
        val leafX = origin.x + direction * distance
        val leafY = origin.y - (index * 6).dp.toPx()
        
        rotate(degrees = angle, pivot = Offset(leafX, leafY)) {
            drawOval(
                color = leafColor,
                topLeft = Offset(leafX - 7.dp.toPx(), leafY - 18.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(14.dp.toPx(), 36.dp.toPx())
            )
        }
    }
    
    val accentAngle = if (direction < 0) -25f else 25f
    val mainLeafX = origin.x + direction * 10.dp.toPx()
    val mainLeafY = origin.y + 4.dp.toPx()
    rotate(degrees = accentAngle, pivot = Offset(mainLeafX, mainLeafY)) {
        drawOval(
            color = colors.accent.copy(alpha = 0.85f),
            topLeft = Offset(mainLeafX - 9.dp.toPx(), mainLeafY - 22.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(18.dp.toPx(), 44.dp.toPx())
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
            .background(surface)
            .navigationBarsPadding()
            .height(78.dp)
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
            .noRippleClickable(onClick = onClick),
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
                    .noRippleClickable { onSelected(tab) },
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
    Icon(Icons.Filled.SettingsGearIcon, contentDescription = null, tint = tint, modifier = Modifier.size(26.dp))
}

@Composable
private fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    return clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled,
        onClick = onClick,
    )
}

@Composable
private fun MonthView(
    month: LocalDate,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    palette: DotCalPalette,
    weekStart: DayOfWeek,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val days = remember(month, weekStart) { monthGrid(month, weekStart) }
    val eventsByDate = remember(events) { events.groupBy { it.localDate() } }
    val weekDayLabels = remember(weekStart) { weekDayLabels(weekStart) }
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
            weekDayLabels.forEach {
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
            .noRippleClickable {
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
                                isSelected -> Modifier.border(1.5.dp, palette.accent, CircleShape)
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
                                .background(event.displayColor(palette)),
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
    weekStart: DayOfWeek,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onJumpToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val days = remember(selectedDate, weekStart) { weekDays(selectedDate, weekStart) }
    val weekRangeStart = days.first()
    val weekRangeEnd = days.last()
    val timedEvents = remember(events, weekRangeStart, weekRangeEnd) {
        events.filter { it.isAllDay == 0 && it.localDate() in weekRangeStart..weekRangeEnd }
    }
    val allDayEvents = remember(events, weekRangeStart, weekRangeEnd) {
        events.filter { it.isAllDay == 1 && it.localDate() in weekRangeStart..weekRangeEnd }
    }
    val eventLayouts = remember(timedEvents) { layoutTimedEvents(timedEvents) }
    val timedEventsByDayHour = remember(timedEvents) {
        timedEvents.groupBy { event -> event.localDate() to event.startLocalTime().hour }
    }

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
                        modifier = Modifier.weight(1f).height(32.dp).padding(2.dp).background(if (event == null) Color.Transparent else event.displayColor(palette).copy(alpha = 0.75f)),
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
                    eventsByDayHour = timedEventsByDayHour,
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
        modifier = modifier.noRippleClickable(onClick = onClick).padding(vertical = 8.dp),
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
    eventsByDayHour: Map<Pair<LocalDate, Int>, List<CalendarEvent>>,
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
            val dayEvents = eventsByDayHour[day to hour].orEmpty()
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
            .background(event.displayColor(palette).copy(alpha = 0.80f))
            .noRippleClickable(enabled = onClick != null) { onClick?.invoke() }
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
    val dayEvents = remember(events, selectedDate) {
        events.filter { it.isTask == 0 && it.localDate() == selectedDate }
    }
    val allDayEvents = remember(dayEvents) { dayEvents.filter { it.isAllDay == 1 } }
    val timedEvents = remember(dayEvents) { dayEvents.filter { it.isAllDay == 0 } }
    val timedEventsByHour = remember(timedEvents) { timedEvents.groupBy { it.startLocalTime().hour } }
    val tasks = remember(events, selectedDate) {
        events.filter { it.isTask == 1 && it.localDate() == selectedDate }
    }

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
                            .background(allDayEvents[index].displayColor(palette).copy(alpha = 0.75f))
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
                    eventsByHour = timedEventsByHour,
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
    eventsByHour: Map<Int, List<CalendarEvent>>,
    palette: DotCalPalette,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val now = LocalTime.now()
    val showNow = selectedDate == LocalDate.now() && hour == now.hour
    val hourEvents = eventsByHour[hour].orEmpty()

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
    val isReadOnly = event.source == "BIRTHDAY" || event.source == "HOLIDAY"
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
            fontWeight = FontWeight.Medium,
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
    var pendingPermissionSave by remember { mutableStateOf<Pair<EventEditorData, RecurringEditScope>?>(null) }
    var submitted by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusSinkRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pendingPermissionSave?.let { pending ->
            val data = if (granted) pending.first else pending.first.copy(reminderMinutes = null)
            onSave(data, pending.second)
            pendingPermissionSave = null
            if (!granted) Toast.makeText(context, "Event saved without reminder", Toast.LENGTH_SHORT).show()
        }
    }
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
    fun requestNotificationPermissionForReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    fun needsNotificationPermissionForReminder(): Boolean {
        return reminderMinutes != null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    }
    fun currentEditorData(): EventEditorData {
        return EventEditorData(
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
        )
    }
    fun trySave() {
        submitted = true
        val startDateTime = startDate.atTime(startTime)
        val endDateTime = endDate.atTime(endTime)
        if (title.isNotBlank() && (if (allDay) endDate >= startDate else endDateTime.isAfter(startDateTime))) {
            val data = currentEditorData()
            if (needsNotificationPermissionForReminder()) {
                pendingPermissionSave = data to recurringEditScope
                requestNotificationPermissionForReminder()
            } else {
                onSave(data, recurringEditScope)
            }
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
                if (it != null) requestNotificationPermissionForReminder()
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
    leadingIcon: ImageVector? = null,
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
        leadingIcon?.let {
            Icon(it, contentDescription = null, tint = valueTextColor, modifier = Modifier.padding(end = 12.dp).size(18.dp))
        }
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
            .noRippleClickable(enabled = onClick != null) { onClick?.invoke() }
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
    onAdd: () -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val dayEvents = remember(events, selectedDate) {
        events
            .filter { it.isTask == 0 && it.localDate() == selectedDate }
            .sortedBy { it.startTimeMs }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.calendarSurface),
        contentPadding = PaddingValues(start = 13.dp, end = 13.dp, top = 22.dp, bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Text(
                selectedDate.format(agendaDateHeaderFormatter),
                color = palette.secondaryText,
                fontFamily = mono,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                letterSpacing = 0.4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            )
        }
        lazyItems(dayEvents, key = { it.id }) { event ->
            AgendaEventCard(event = event, palette = palette, onClick = { onEventClick(event) })
        }
        item {
            if (dayEvents.isEmpty()) {
                AgendaEndOfDayState(
                    palette = palette,
                    modifier = Modifier.fillParentMaxHeight(0.72f),
                    onAdd = onAdd,
                )
            } else {
                AgendaEndOfDayState(
                    palette = palette,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 128.dp),
                    onAdd = onAdd,
                )
            }
        }
    }
}

@Composable
private fun AgendaEventCard(
    event: CalendarEvent,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(if (palette.isDark) palette.dialogSurface else palette.eventCardSurface)
            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(20.dp))
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        Text(
            event.agendaTimeRange(),
            color = palette.secondaryText,
            fontFamily = mono,
            fontSize = 14.sp,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            event.title,
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (event.location.isNotBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(palette.secondaryText),
                )
                Spacer(modifier = Modifier.width(9.dp))
                Text(
                    event.location,
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun AgendaEndOfDayState(
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
    onAdd: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AgendaCalendarOutlineIcon(tint = palette.dimText)
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            "No more events for this day",
            color = palette.dimText,
            fontFamily = mono,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            "+ Add Event",
            color = palette.accent,
            fontFamily = mono,
            fontSize = 16.sp,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAdd)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun AgendaCalendarOutlineIcon(tint: Color) {
    Canvas(modifier = Modifier.size(38.dp)) {
        val stroke = 1.6.dp.toPx()
        val radius = 5.dp.toPx()
        val left = 3.dp.toPx()
        val top = 6.dp.toPx()
        val right = size.width - 3.dp.toPx()
        val bottom = size.height - 3.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
            style = Stroke(width = stroke),
        )
        drawLine(tint, Offset(left, 15.dp.toPx()), Offset(right, 15.dp.toPx()), strokeWidth = stroke)
        drawLine(tint, Offset(12.dp.toPx(), 3.dp.toPx()), Offset(12.dp.toPx(), 9.dp.toPx()), strokeWidth = stroke)
        drawLine(tint, Offset(26.dp.toPx(), 3.dp.toPx()), Offset(26.dp.toPx(), 9.dp.toPx()), strokeWidth = stroke)
        val dotRadius = 1.4.dp.toPx()
        listOf(12.dp, 19.dp, 26.dp).forEach { x ->
            drawCircle(tint, radius = dotRadius, center = Offset(x.toPx(), 23.dp.toPx()))
        }
    }
}

@Composable
private fun TaskDetailScreen(
    task: CalendarEvent,
    reminder: EventReminder?,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
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
                "Task Details",
                modifier = Modifier.weight(1f),
                color = palette.primaryText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                "Edit",
                color = palette.primaryText,
                fontSize = 15.sp,
                modifier = Modifier.clickable(onClick = onEdit).padding(horizontal = 12.dp, vertical = 10.dp),
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(palette.background),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 28.dp),
        ) {
            item {
                Text(
                    task.title,
                    color = palette.primaryText,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                )
                Spacer(modifier = Modifier.height(26.dp))
            }
            item {
                DetailSection(label = "STATUS", palette = palette) {
                    Text(if (task.isCompleted == 1) "Completed" else "Open", color = palette.primaryText, fontSize = 20.sp, lineHeight = 27.sp)
                }
            }
            item {
                DetailDivider(palette)
                DetailSection(label = "DUE", palette = palette) {
                    if (task.hasTaskDate()) {
                        Text(task.taskDueDateLine(), color = palette.primaryText, fontSize = 16.sp, lineHeight = 23.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(task.taskDueTimeLine(), color = palette.primaryText, fontSize = 16.sp, lineHeight = 23.sp)
                    } else {
                        Text("None", color = palette.primaryText, fontSize = 16.sp, lineHeight = 23.sp)
                    }
                    task.recurrenceDetailLabel()?.let { label ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(label.toSentenceCase(), color = palette.secondaryText, fontSize = 14.sp, lineHeight = 20.sp)
                    }
                }
            }
            item {
                DetailDivider(palette)
                DetailSection(label = "REMINDER", palette = palette) {
                    Text(reminder?.detailLabel()?.toSentenceCase() ?: "None", color = palette.primaryText, fontSize = 16.sp, lineHeight = 23.sp)
                }
            }
            item {
                DetailDivider(palette)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (task.isCompleted != 1) {
                        Text(
                            "Mark Complete",
                            color = palette.accent,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable(onClick = onComplete)
                                .padding(vertical = 12.dp),
                        )
                        Spacer(modifier = Modifier.width(32.dp))
                    }
                    Text(
                        "Delete Task",
                        color = palette.accent,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable(onClick = onDelete)
                            .padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}

private enum class TaskFilter(val label: String) {
    All("All"),
    Today("Today"),
    Upcoming("Upcoming"),
    Completed("Completed"),
}

@Composable
private fun TasksScreen(
    tasks: List<CalendarEvent>,
    reminders: List<EventReminder>,
    palette: DotCalPalette,
    onAddClick: () -> Unit,
    onTaskClick: (CalendarEvent) -> Unit,
    onCompleteTask: (CalendarEvent) -> Unit,
    onDeleteTask: (CalendarEvent) -> Unit,
) {
    var filter by remember { mutableStateOf(TaskFilter.All) }
    val today = LocalDate.now()
    val filteredTasks = remember(tasks, filter, today) {
        tasks
            .filter { task ->
                when (filter) {
                    TaskFilter.All -> true
                    TaskFilter.Today -> task.hasTaskDate() && task.localDate() == today
                    TaskFilter.Upcoming -> task.isCompleted == 0 && task.hasTaskDate() && task.startTimeMs > System.currentTimeMillis()
                    TaskFilter.Completed -> task.isCompleted == 1
                }
            }
            .sortedWith(compareBy<CalendarEvent> { it.isCompleted }.thenBy { if (it.hasTaskDate()) it.startTimeMs else Long.MAX_VALUE }.thenBy { it.title })
    }
    val groupedTasks = remember(filteredTasks) {
        filteredTasks.groupBy { task -> if (task.hasTaskDate()) task.localDate() else null }
    }

    Box(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.fillMaxWidth().height(12.dp).background(palette.topBarSurface))
            TaskFilterSegmentedControl(
                selected = filter,
                palette = palette,
                onSelected = { filter = it },
            )
            Spacer(modifier = Modifier.fillMaxWidth().height(16.dp).background(palette.topBarSurface))
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    TaskEmptyState(filter = filter, palette = palette)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    groupedTasks.entries
                        .sortedWith(compareBy<Map.Entry<LocalDate?, List<CalendarEvent>>> { it.key == null }.thenBy { it.key ?: LocalDate.MAX })
                        .forEach { (date, group) ->
                            item(key = "header-${date ?: "none"}") {
                                Text(
                                    date?.format(taskDateHeaderFormatter()) ?: "No Date",
                                    color = palette.secondaryText,
                                    fontFamily = mono,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.2.sp,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
                                )
                            }
                            lazyItems(group, key = { it.id }) { task ->
                                TaskRow(
                                    task = task,
                                    reminder = reminders.firstOrNull { it.eventId == task.baseEventId() },
                                    palette = palette,
                                    onClick = { onTaskClick(task) },
                                    onComplete = { onCompleteTask(task) },
                                    onDelete = { onDeleteTask(task) },
                                )
                            }
                        }
                }
            }
        }
        Button(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(64.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add task")
        }
    }
}

@Composable
private fun TaskFilterSegmentedControl(
    selected: TaskFilter,
    palette: DotCalPalette,
    onSelected: (TaskFilter) -> Unit,
) {
    val segmentShape = RoundedCornerShape(28.dp)
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
        TaskFilter.entries.forEach { option ->
            val isSelected = selected == option
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) segmentSelected else Color.Transparent)
                    .noRippleClickable { onSelected(option) },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = if (isSelected) 14.dp else 0.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        option.label,
                        fontFamily = mono,
                        color = if (isSelected) palette.primaryText else inactiveText,
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
private fun TaskRow(
    task: CalendarEvent,
    reminder: EventReminder?,
    palette: DotCalPalette,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    var dragOffset by remember(task.id) { mutableFloatStateOf(0f) }
    val thresholdPx = with(LocalDensity.current) { 96.dp.toPx() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                when {
                    dragOffset < 0f -> Color(0xFFE53935)
                    dragOffset > 0f -> Color(0xFF2E7D32)
                    else -> Color.Transparent
                },
            ),
    ) {
        if (dragOffset != 0f) {
            val icon = if (dragOffset < 0f) Icons.Default.Delete else Icons.Default.Check
            val alignment = if (dragOffset < 0f) Alignment.CenterEnd else Alignment.CenterStart
            Icon(
                icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(alignment)
                    .padding(horizontal = 22.dp)
                    .size(22.dp),
            )
        }
        Row(
            modifier = Modifier
                .offset { IntOffset(dragOffset.roundToInt(), 0) }
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(taskCardColor(palette))
                .border(1.dp, palette.line, RoundedCornerShape(20.dp))
                .noRippleClickable(onClick = onClick)
                .pointerInput(task.id) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                dragOffset <= -thresholdPx -> onDelete()
                                dragOffset >= thresholdPx && task.isCompleted == 0 -> onComplete()
                            }
                            dragOffset = 0f
                        },
                        onDragCancel = { dragOffset = 0f },
                        onHorizontalDrag = { _, amount ->
                            dragOffset = (dragOffset + amount).coerceIn(-thresholdPx * 1.25f, thresholdPx * 1.25f)
                        },
                    )
                }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val completed = task.isCompleted == 1
            val titleColor = if (completed) palette.primaryText.copy(alpha = 0.6f) else palette.primaryText
            val metadataColor = if (completed) palette.secondaryText.copy(alpha = 0.6f) else palette.secondaryText
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, if (completed) palette.secondaryText else palette.primaryText, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center,
            ) {
                if (completed) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(15.dp))
                }
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    task.title,
                    color = titleColor,
                    fontFamily = mono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (completed) TextDecoration.LineThrough else null,
                )
                if ((task.hasTaskDate() && task.isAllDay == 0) || reminder != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (task.hasTaskDate() && task.isAllDay == 0) {
                            TaskMetadata(label = task.startLocalTime().format(timeFormatter), icon = Icons.Default.AccessTime, color = metadataColor)
                        }
                        reminder?.let {
                            TaskMetadata(label = taskReminderMetadataLabel(it.minutesBefore), icon = Icons.Default.Notifications, color = metadataColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskMetadata(label: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, color = color, fontFamily = mono, fontSize = 14.sp, maxLines = 1)
    }
}

private fun taskCardColor(palette: DotCalPalette): Color {
    return if (palette.isDark) Color(0xFF0A0A0A) else Color(0xFFFFFFFF)
}

private fun taskReminderMetadataLabel(minutes: Int): String {
    return when (minutes) {
        5 -> "5 min before"
        10 -> "10 min before"
        30 -> "30 min before"
        1440 -> "1 day before"
        else -> "Reminder"
    }
}

@Composable
private fun TaskEmptyState(filter: TaskFilter, palette: DotCalPalette) {
    val title = when (filter) {
        TaskFilter.All -> "No tasks yet"
        TaskFilter.Today -> "Nothing due today"
        TaskFilter.Upcoming -> "All clear"
        TaskFilter.Completed -> "No completed tasks"
    }
    val subtitle = if (filter == TaskFilter.All) "Tap + to create your first task" else null
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(title, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Medium, fontSize = 18.sp, textAlign = TextAlign.Center)
        subtitle?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditorSheet(
    task: CalendarEvent?,
    initialReminder: EventReminder?,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onSave: (TaskEditorData) -> Unit,
    onDelete: (() -> Unit)? = null,
) {
    val taskKey = task?.id ?: "new-task"
    var title by remember(taskKey) { mutableStateOf(task?.title.orEmpty()) }
    var titleError by remember { mutableStateOf(false) }
    var dueDate by remember(taskKey) { mutableStateOf<LocalDate?>(task?.takeIf { it.hasTaskDate() }?.localDate() ?: LocalDate.now()) }
    var dueTime by remember(taskKey) { mutableStateOf<LocalTime?>(task?.takeIf { it.hasTaskDate() && it.isAllDay == 0 }?.startLocalTime()) }
    var reminderMinutes by remember(taskKey, initialReminder?.minutesBefore) { mutableStateOf<Int?>(initialReminder?.minutesBefore) }
    var recurrenceRule by remember(taskKey) { mutableStateOf(task?.rrule) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var showRepeatPicker by remember { mutableStateOf(false) }
    var pendingTaskPermissionSave by remember { mutableStateOf<TaskEditorData?>(null) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusSinkRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pendingTaskPermissionSave?.let { pending ->
            onSave(if (granted) pending else pending.copy(reminderMinutes = null))
            pendingTaskPermissionSave = null
            if (!granted) Toast.makeText(context, "Task saved without reminder", Toast.LENGTH_SHORT).show()
        }
    }
    val taskSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun clearTaskFocus() {
        keyboardController?.hide()
        focusManager.clearFocus(force = true)
        runCatching { focusSinkRequester.requestFocus() }
    }

    fun requestNotificationPermissionForTaskReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    fun needsNotificationPermissionForTaskReminder(): Boolean {
        return reminderMinutes != null &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = taskSheetState,
        containerColor = palette.dialogSurface,
        contentColor = palette.primaryText,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.dialogSurface)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val pointerEvent = awaitPointerEvent(PointerEventPass.Initial)
                            if (pointerEvent.changes.any { it.pressed && !it.previousPressed }) {
                                clearTaskFocus()
                            }
                        }
                    }
                }
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 22.dp),
        ) {
            Box(modifier = Modifier.size(1.dp).focusRequester(focusSinkRequester).focusable())
            Text(if (task == null) "Add Task" else "Edit Task", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    titleError = false
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                isError = titleError,
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = palette.secondaryText) },
                colors = dotCalTextFieldColors(palette),
                textStyle = TextStyle(fontFamily = mono, fontSize = 16.sp),
            )
            if (titleError) {
                Text("Title required", color = palette.accent, fontFamily = mono, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            EditorValueRow(
                title = "Date",
                value = dueDate?.format(editorDateFormatter) ?: "None",
                palette = palette,
                leadingIcon = Icons.Default.CalendarMonth,
                onClick = {
                    clearTaskFocus()
                    showDatePicker = true
                },
            )
            if (dueDate != null) {
                Text(
                    "Clear date",
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable {
                        clearTaskFocus()
                        dueDate = null
                        dueTime = null
                        reminderMinutes = null
                        recurrenceRule = null
                    }.padding(vertical = 8.dp),
                )
            }
            EditorValueRow(
                title = "Time",
                value = dueTime?.format(timeFormatter) ?: "None",
                palette = palette,
                enabled = dueDate != null,
                leadingIcon = Icons.Default.AccessTime,
                onClick = {
                    clearTaskFocus()
                    showTimePicker = true
                },
            )
            if (dueTime != null) {
                Text(
                    "Clear time",
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable {
                        clearTaskFocus()
                        dueTime = null
                    }.padding(vertical = 8.dp),
                )
            }
            EditorValueRow(
                title = "Reminder",
                value = reminderLabel(reminderMinutes),
                palette = palette,
                enabled = dueDate != null,
                leadingIcon = Icons.Default.Notifications,
                onClick = {
                    clearTaskFocus()
                    showReminderPicker = true
                },
            )
            EditorValueRow(
                title = "Repeat",
                value = recurrenceOptions.firstOrNull { it.rrule == recurrenceRule }?.label ?: "None",
                palette = palette,
                enabled = dueDate != null,
                leadingIcon = Icons.Default.CalendarMonth,
                onClick = {
                    clearTaskFocus()
                    showReminderPicker = false
                    showDatePicker = false
                    showTimePicker = false
                    showRepeatPicker = true
                },
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (title.isBlank()) {
                        titleError = true
                    } else {
                        val data = TaskEditorData(
                            title = title,
                            date = dueDate,
                            time = dueTime,
                            reminderMinutes = reminderMinutes,
                            rrule = if (dueDate == null) null else recurrenceRule,
                        )
                        if (needsNotificationPermissionForTaskReminder()) {
                            pendingTaskPermissionSave = data
                            requestNotificationPermissionForTaskReminder()
                        } else {
                            onSave(data)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = Color.White),
                shape = RoundedCornerShape(16.dp),
            ) {
                Text("SAVE TASK", fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
            onDelete?.let { delete ->
                Text(
                    "Delete Task",
                    color = palette.accent,
                    fontFamily = mono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = delete)
                        .padding(top = 18.dp, bottom = 4.dp),
                )
            }
        }
    }
    if (showDatePicker) {
        DateTimeChoiceSheet(
            title = "Date",
            selectedDate = dueDate ?: LocalDate.now(),
            selectedTime = dueTime ?: LocalTime.of(9, 0),
            minDate = null,
            includeTime = false,
            palette = palette,
            onDismiss = { showDatePicker = false },
            onSelected = { date, _ ->
                dueDate = date
                showDatePicker = false
            },
        )
    }
    if (showTimePicker && dueDate != null) {
        TaskTimeChoiceSheet(
            title = "Time",
            selected = dueTime ?: LocalTime.of(9, 0),
            palette = palette,
            onDismiss = { showTimePicker = false },
            onSelected = {
                dueTime = it
                showTimePicker = false
            },
        )
    }
    if (showReminderPicker && dueDate != null) {
        ModalBottomSheet(
            onDismissRequest = { showReminderPicker = false },
            containerColor = palette.dialogSurface,
            contentColor = palette.primaryText,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = { BottomSheetDragHandle(palette) },
        ) {
            ChoiceSheetContent(
                title = "Reminder",
                items = taskReminderOptions,
                selected = reminderMinutes,
                label = { reminderLabel(it) },
                palette = palette,
                onSelected = {
                    reminderMinutes = it
                    if (it != null) requestNotificationPermissionForTaskReminder()
                    showReminderPicker = false
                },
            )
        }
    }
    if (showRepeatPicker && dueDate != null) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskTimeChoiceSheet(
    title: String,
    selected: LocalTime,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onSelected: (LocalTime) -> Unit,
) {
    val hours = remember { (0..23).toList() }
    val minutes = remember { (0..59).toList() }
    var pickedHour by remember(selected) { mutableStateOf(selected.hour) }
    var pickedMinute by remember(selected) { mutableStateOf(selected.minute) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        contentColor = palette.primaryText,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.dialogSurface)
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(title, color = palette.primaryText, fontFamily = mono, fontSize = 20.sp)
            Text(
                LocalTime.of(pickedHour, pickedMinute).format(timeFormatter),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().height(188.dp),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WheelColumn(
                    items = hours,
                    selected = pickedHour,
                    label = { it.toString().padStart(2, '0') },
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    circular = true,
                    onSelected = { pickedHour = it },
                )
                WheelColumn(
                    items = minutes,
                    selected = pickedMinute,
                    label = { it.toString().padStart(2, '0') },
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    circular = true,
                    onSelected = { pickedMinute = it },
                )
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
                    onClick = { onSelected(LocalTime.of(pickedHour, pickedMinute)) },
                    modifier = Modifier.weight(1f).height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = Color.White),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text("OK", fontFamily = mono, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
    val rangeEventsByDayHour = remember(rangeEvents) {
        rangeEvents.groupBy { event -> event.localDate() to event.startLocalTime().hour }
    }
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
                    eventsByDayHour = rangeEventsByDayHour,
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
    eventsByDayHour: Map<Pair<LocalDate, Int>, List<CalendarEvent>>,
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
            val dayEvents = eventsByDayHour[day to hour].orEmpty()
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
    weekStart: DayOfWeek,
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
                    weekStart = weekStart,
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
    weekStart: DayOfWeek,
    onClick: () -> Unit,
) {
    val days = remember(month, weekStart) { monthGrid(month, weekStart) }
    val today = LocalDate.now()
    val isCurrentMonth = month.year == today.year && month.monthValue == today.monthValue
    Column(
        modifier = Modifier
            .aspectRatio(0.82f)
            .padding(3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) palette.cell else Color.Transparent)
            .noRippleClickable(onClick = onClick)
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
    accentColor: AccentColor,
    palette: DotCalPalette,
    screen: SettingsScreen,
    onBack: () -> Unit,
    onScreenChange: (SettingsScreen) -> Unit,
    onThemeSelected: (DotCalThemeMode) -> Unit,
    onAccentSelected: (AccentColor) -> Unit,
    syncEnabled: Boolean,
    syncIntervalMins: Int,
    syncMetadata: List<SyncMetadata>,
    isSyncing: Boolean,
    birthdayEnabled: Boolean,
    defaultReminderMinutes: Int?,
    defaultAllDayReminderTime: LocalTime,
    weekStartOption: WeekStartOption,
    holidayCountries: List<HolidayCountryUiItem>,
    accounts: List<CalendarAccount>,
    hasCalendarPermission: Boolean,
    onSyncNow: () -> Unit,
    onAccountVisibilityChange: (String, Boolean) -> Unit,
    onSyncEnabledChange: (Boolean) -> Unit,
    onSyncIntervalSelected: (Int) -> Unit,
    onDefaultReminderSelected: (Int?) -> Unit,
    onDefaultAllDayReminderTimeSelected: (LocalTime) -> Unit,
    onWeekStartSelected: (WeekStartOption) -> Unit,
    onBirthdayEnabledChange: (Boolean) -> Unit,
    onAddHolidayCountry: (HolidayCountryUiItem) -> Unit,
    onRemoveHolidayCountry: (HolidayCountryUiItem) -> Unit,
    onRateDotCal: () -> Unit,
    onRequestCalendarAccess: () -> Unit,
    onAddAccount: () -> Unit,
) {
    BackHandler {
        when (screen) {
            SettingsScreen.Root -> onBack()
            SettingsScreen.AddAccount -> onScreenChange(SettingsScreen.CalendarAccounts)
            else -> onScreenChange(SettingsScreen.Root)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        SettingsRoot(
            themeMode = themeMode,
            accentColor = accentColor,
            palette = palette,
            onBack = onBack,
            onThemeSelected = onThemeSelected,
            onAccentSelected = onAccentSelected,
            onThemeSettings = { onScreenChange(SettingsScreen.Theme) },
            syncEnabled = syncEnabled,
            syncIntervalMins = syncIntervalMins,
            syncMetadata = syncMetadata,
            isSyncing = isSyncing,
            birthdayEnabled = birthdayEnabled,
            defaultReminderMinutes = defaultReminderMinutes,
            defaultAllDayReminderTime = defaultAllDayReminderTime,
            weekStartOption = weekStartOption,
            holidayCountries = holidayCountries,
            accounts = accounts,
            hasCalendarPermission = hasCalendarPermission,
            onSyncNow = onSyncNow,
            onAccountVisibilityChange = onAccountVisibilityChange,
            onSyncEnabledChange = onSyncEnabledChange,
            onSyncIntervalSelected = onSyncIntervalSelected,
            onDefaultReminderSelected = onDefaultReminderSelected,
            onDefaultAllDayReminderTimeSelected = onDefaultAllDayReminderTimeSelected,
            onWeekStartSelected = onWeekStartSelected,
            onBirthdayEnabledChange = onBirthdayEnabledChange,
            onGlobalHolidays = { onScreenChange(SettingsScreen.GlobalHolidays) },
            onPrivacyPolicy = { onScreenChange(SettingsScreen.PrivacyPolicy) },
            onRateDotCal = onRateDotCal,
            onRequestCalendarAccess = onRequestCalendarAccess,
            onAddAccount = { onScreenChange(SettingsScreen.AddAccount) },
        )
        AnimatedVisibility(
            visible = screen == SettingsScreen.Theme,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
        ) {
            ThemeSettings(
            themeMode = themeMode,
            accentColor = accentColor,
            palette = palette,
            onBack = { onScreenChange(SettingsScreen.Root) },
            onThemeSelected = onThemeSelected,
            onAccentSelected = onAccentSelected,
            )
        }
        AnimatedVisibility(
            visible = screen == SettingsScreen.CalendarAccounts,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
        ) {
            CalendarAccountsSettings(
            accounts = accounts,
            palette = palette,
            hasCalendarPermission = hasCalendarPermission,
            syncMetadata = syncMetadata,
            isSyncing = isSyncing,
            onBack = { onScreenChange(SettingsScreen.Root) },
            onRequestCalendarAccess = onRequestCalendarAccess,
            onSyncNow = onSyncNow,
            onAccountVisibilityChange = onAccountVisibilityChange,
            onAddAccount = { onScreenChange(SettingsScreen.AddAccount) },
            )
        }
        AnimatedVisibility(
            visible = screen == SettingsScreen.AddAccount,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
        ) {
            AddAccountSettings(
                palette = palette,
                onBack = { onScreenChange(SettingsScreen.CalendarAccounts) },
                onGoogleAccount = onAddAccount,
            )
        }
        AnimatedVisibility(
            visible = screen == SettingsScreen.GlobalHolidays,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
        ) {
            GlobalHolidaysSettings(
                countries = holidayCountries,
                palette = palette,
                onBack = { onScreenChange(SettingsScreen.Root) },
                onAddCountry = onAddHolidayCountry,
                onRemoveCountry = onRemoveHolidayCountry,
            )
        }
        AnimatedVisibility(
            visible = screen == SettingsScreen.PrivacyPolicy,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
        ) {
            PrivacyPolicySettings(
                palette = palette,
                onBack = { onScreenChange(SettingsScreen.Root) },
            )
        }
    }
}

@Composable
private fun SettingsRoot(
    themeMode: DotCalThemeMode,
    accentColor: AccentColor,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onThemeSelected: (DotCalThemeMode) -> Unit,
    onAccentSelected: (AccentColor) -> Unit,
    onThemeSettings: () -> Unit,
    syncEnabled: Boolean,
    syncIntervalMins: Int,
    syncMetadata: List<SyncMetadata>,
    isSyncing: Boolean,
    birthdayEnabled: Boolean,
    defaultReminderMinutes: Int?,
    defaultAllDayReminderTime: LocalTime,
    weekStartOption: WeekStartOption,
    holidayCountries: List<HolidayCountryUiItem>,
    accounts: List<CalendarAccount>,
    hasCalendarPermission: Boolean,
    onSyncNow: () -> Unit,
    onAccountVisibilityChange: (String, Boolean) -> Unit,
    onSyncEnabledChange: (Boolean) -> Unit,
    onSyncIntervalSelected: (Int) -> Unit,
    onDefaultReminderSelected: (Int?) -> Unit,
    onDefaultAllDayReminderTimeSelected: (LocalTime) -> Unit,
    onWeekStartSelected: (WeekStartOption) -> Unit,
    onBirthdayEnabledChange: (Boolean) -> Unit,
    onGlobalHolidays: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onRateDotCal: () -> Unit,
    onRequestCalendarAccess: () -> Unit,
    onAddAccount: () -> Unit,
) {
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
            SettingsMenuRow(
                title = "Calendar Accounts",
                value = calendarAccountsLabel(accounts, hasCalendarPermission),
                palette = palette,
                onClick = onRequestCalendarAccess,
            )
            SettingsDivider(palette)

            SettingsSectionTitle("General", palette)
            SettingsWeekStartRow(
                selectedOption = weekStartOption,
                palette = palette,
                onWeekStartSelected = onWeekStartSelected,
            )
            SettingsMenuRow(
                title = "Global Holidays",
                value = selectedHolidayCountriesLabel(holidayCountries),
                palette = palette,
                onClick = onGlobalHolidays,
            )
            SettingsDivider(palette)

            SettingsSectionTitle("Reminders", palette)
            SettingsDefaultReminderRow(
                selectedMinutes = defaultReminderMinutes,
                palette = palette,
                onReminderSelected = onDefaultReminderSelected,
            )
            SettingsAllDayReminderTimeRow(
                selectedTime = defaultAllDayReminderTime,
                palette = palette,
                onTimeSelected = onDefaultAllDayReminderTimeSelected,
            )
            SettingsDivider(palette)

            SettingsSectionTitle("Additional", palette)
            SettingsMenuRow(
                title = "Theme",
                value = "${themeMode.label} • ${accentColor.label}",
                palette = palette,
                onClick = onThemeSettings,
            )
            SettingsToggleRow(
                title = "Birthday calendar",
                subtitle = "Import contacts' birthdays",
                checked = birthdayEnabled,
                palette = palette,
                onCheckedChange = onBirthdayEnabledChange,
            )
            SettingsToggleRow(title = "Sync enabled", checked = syncEnabled, palette = palette, onCheckedChange = onSyncEnabledChange)
            SettingsSyncIntervalRow(intervalMins = syncIntervalMins, palette = palette, onIntervalSelected = onSyncIntervalSelected)
            SettingsSyncNowRow(
                syncMetadata = syncMetadata,
                isSyncing = isSyncing,
                palette = palette,
                onClick = onSyncNow,
            )
            SettingsDivider(palette)

            SettingsSectionTitle("About", palette)
            SettingsMenuRow(title = "Privacy Policy", value = "", palette = palette, onClick = onPrivacyPolicy)
            SettingsMenuRow(title = "Rate DotCal", value = "", palette = palette, onClick = onRateDotCal)
            SettingsMenuRow(title = "Version", value = BuildConfig.VERSION_NAME, palette = palette, showChevron = false, onClick = {})
            Spacer(modifier = Modifier.height(32.dp))
            }
        }
        if (showCompactHeader) {
            SettingsCompactHeader(palette = palette, onBack = onBack)
        }
    }
}

@Composable
private fun SettingsLargeHeader(palette: DotCalPalette, onBack: () -> Unit, title: String = "Calendar") {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.offset(x = (-16).dp).size(44.dp)) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
        }
        Text(
            title,
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            modifier = Modifier.padding(start = 0.dp, top = 2.dp),
        )
    }
}

@Composable
private fun SettingsCompactHeader(palette: DotCalPalette, onBack: () -> Unit, title: String = "Calendar") {
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
            title,
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
    accentColor: AccentColor,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onThemeSelected: (DotCalThemeMode) -> Unit,
    onAccentSelected: (AccentColor) -> Unit,
) {
    val listState = rememberLazyListState()
    val showCompactHeader = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 96
    Box(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
            item {
                SettingsLargeHeader(palette = palette, onBack = onBack, title = "Theme")
                Spacer(modifier = Modifier.height(10.dp))
                Text("Choose app appearance", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
            }
            item {
            DotCalThemeMode.entries.forEach { mode ->
                ThemeOptionRow(
                    mode = mode,
                    accentColor = accentColor,
                    palette = palette,
                    selected = themeMode == mode,
                    onClick = { onThemeSelected(mode) },
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Accent Color",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 14.dp),
            )
            AccentColorSwatches(
                selectedAccent = accentColor,
                palette = palette,
                onAccentSelected = onAccentSelected,
            )
            Spacer(modifier = Modifier.height(960.dp))
            }
        }
        if (showCompactHeader) {
            SettingsCompactHeader(palette = palette, onBack = onBack, title = "Theme")
        }
    }
}

@Composable
private fun GlobalHolidaysSettings(
    countries: List<HolidayCountryUiItem>,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onAddCountry: (HolidayCountryUiItem) -> Unit,
    onRemoveCountry: (HolidayCountryUiItem) -> Unit,
) {
    val selected = remember(countries) { countries.filter { it.isSelected } }
    val available = remember(countries) { countries.filterNot { it.isSelected } }
    val listState = rememberLazyListState()
    val showCompactHeader = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 96
    Box(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
            item {
                SettingsLargeHeader(palette = palette, onBack = onBack, title = "Global Holidays")
                Spacer(modifier = Modifier.height(10.dp))
            }
            if (selected.isNotEmpty()) {
                item { SettingsSectionTitle("SELECTED", palette) }
                lazyItems(selected, key = { it.code }) { country ->
                    HolidayCountryRow(
                        country = country,
                        palette = palette,
                        selected = true,
                        onClick = { onRemoveCountry(country) },
                    )
                    HolidayCountryDivider(palette)
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
            if (available.isNotEmpty()) {
                item { SettingsSectionTitle("AVAILABLE", palette) }
                lazyItems(available, key = { it.code }) { country ->
                    HolidayCountryRow(
                        country = country,
                        palette = palette,
                        selected = false,
                        onClick = { onAddCountry(country) },
                    )
                    HolidayCountryDivider(palette)
                }
            }
            item { Spacer(modifier = Modifier.height(960.dp)) }
        }
        if (showCompactHeader) {
            SettingsCompactHeader(palette = palette, onBack = onBack, title = "Global Holidays")
        }
    }
}

@Composable
private fun HolidayCountryDivider(palette: DotCalPalette) {
    HorizontalDivider(color = palette.line, thickness = 1.dp)
}

@Composable
private fun HolidayCountryRow(
    country: HolidayCountryUiItem,
    palette: DotCalPalette,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(country.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        if (selected) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = palette.secondaryText, modifier = Modifier.size(20.dp))
        } else {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = palette.accent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CalendarAccountsSettings(
    accounts: List<CalendarAccount>,
    palette: DotCalPalette,
    hasCalendarPermission: Boolean,
    syncMetadata: List<SyncMetadata>,
    isSyncing: Boolean,
    onBack: () -> Unit,
    onRequestCalendarAccess: () -> Unit,
    onSyncNow: () -> Unit,
    onAccountVisibilityChange: (String, Boolean) -> Unit,
    onAddAccount: () -> Unit,
) {
    val listState = rememberLazyListState()
    val showCompactHeader = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 96
    val sortedAccounts = remember(accounts) {
        accounts.sortedWith(compareBy<CalendarAccount> { it.sortOrder }.thenBy { it.displayName })
    }
    Box(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
            item {
                SettingsLargeHeader(palette = palette, onBack = onBack, title = "Calendar Accounts")
                Spacer(modifier = Modifier.height(10.dp))
            if (!hasCalendarPermission) {
                SettingsMenuRow(
                    title = "Grant calendar access",
                    value = "",
                    palette = palette,
                    onClick = onRequestCalendarAccess,
                )
                SettingsDivider(palette)
            } else {
                SettingsSyncNowRow(
                    syncMetadata = syncMetadata,
                    isSyncing = isSyncing,
                    palette = palette,
                    onClick = onSyncNow,
                )
                SettingsDivider(palette)
            }
        }
            if (sortedAccounts.isEmpty()) {
                item {
                    Text(
                        if (hasCalendarPermission) "Tap Sync Now to load accounts" else "Calendar access needed",
                        color = palette.secondaryText,
                        fontFamily = mono,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            } else {
                lazyItems(sortedAccounts, key = { it.id }) { account ->
                    CalendarAccountToggleRow(
                        account = account,
                        palette = palette,
                        onAccountVisibilityChange = onAccountVisibilityChange,
                    )
                    SettingsContentDivider(palette)
                }
            }
            item {
                CalendarAddAccountRow(palette = palette, onClick = onAddAccount)
            }
            item { Spacer(modifier = Modifier.height(28.dp)) }
        }
        if (showCompactHeader) {
            SettingsCompactHeader(palette = palette, onBack = onBack, title = "Calendar Accounts")
        }
    }
}

@Composable
private fun AddAccountSettings(
    palette: DotCalPalette,
    onBack: () -> Unit,
    onGoogleAccount: () -> Unit,
) {
    val listState = rememberLazyListState()
    val showCompactHeader = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 96
    Box(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
        LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
            item {
                SettingsLargeHeader(palette = palette, onBack = onBack, title = "Add an account")
                Spacer(modifier = Modifier.height(10.dp))
                GoogleAccountProviderRow(palette = palette, onClick = onGoogleAccount)
                HorizontalDivider(color = palette.line, thickness = 1.dp)
            }
            item { Spacer(modifier = Modifier.height(960.dp)) }
        }
        if (showCompactHeader) {
            SettingsCompactHeader(palette = palette, onBack = onBack, title = "Add an account")
        }
    }
}

@Composable
private fun GoogleAccountProviderRow(palette: DotCalPalette, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.dotfield.dotcal.R.drawable.ic_google_logo),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                "Google",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = palette.secondaryText,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun PrivacyPolicySettings(
    palette: DotCalPalette,
    onBack: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
        SettingsCompactHeader(palette = palette, onBack = onBack, title = "Privacy Policy")
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = false
                    loadUrl("https://dotfieldstudio.com/dotcal/privacy")
                }
            },
            update = { webView ->
                if (webView.url != "https://dotfieldstudio.com/dotcal/privacy") {
                    webView.loadUrl("https://dotfieldstudio.com/dotcal/privacy")
                }
            },
        )
    }
}

@Composable
private fun CalendarAddAccountRow(palette: DotCalPalette, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 22.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .height(46.dp)
                .clip(RoundedCornerShape(23.dp))
                .background(palette.accent)
                .noRippleClickable(onClick = onClick)
                .padding(horizontal = 22.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = null,
                tint = palette.onAccent,
                modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Add Account",
                color = palette.onAccent,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun CalendarAccountToggleRow(
    account: CalendarAccount,
    palette: DotCalPalette,
    onAccountVisibilityChange: (String, Boolean) -> Unit,
) {
    val isLocal = account.id == "local-primary"
    Row(
        modifier = Modifier.fillMaxWidth().height(68.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(Color(parseColor(account.color))),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    account.displayName.readableCalendarLabel(),
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    account.secondaryCalendarLabel(),
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        DotCalSwitch(
            checked = account.isVisible == 1,
            enabled = !isLocal,
            palette = palette,
            onCheckedChange = { checked -> onAccountVisibilityChange(account.id, checked) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsWeekStartRow(
    selectedOption: WeekStartOption,
    palette: DotCalPalette,
    onWeekStartSelected: (WeekStartOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .noRippleClickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Start of the week", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selectedOption.label, color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                UpDownChevron(tint = palette.secondaryText)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(palette.dialogSurface),
        ) {
            WeekStartOption.entries.forEach { option ->
                DropdownMenuItem(
                    modifier = Modifier.background(palette.dialogSurface),
                    text = {
                        Text(
                            option.label,
                            color = palette.primaryText,
                            fontFamily = mono,
                            fontSize = 16.sp,
                        )
                    },
                    trailingIcon = {
                        if (option == selectedOption) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = palette.primaryText)
                        }
                    },
                    onClick = {
                        onWeekStartSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDefaultReminderRow(
    selectedMinutes: Int?,
    palette: DotCalPalette,
    onReminderSelected: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .noRippleClickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Default reminder", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(reminderLabel(selectedMinutes), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                UpDownChevron(tint = palette.secondaryText)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(palette.dialogSurface),
        ) {
            reminderOptions.forEach { option ->
                DropdownMenuItem(
                    modifier = Modifier.background(palette.dialogSurface),
                    text = {
                        Text(
                            reminderLabel(option),
                            color = palette.primaryText,
                            fontFamily = mono,
                            fontSize = 16.sp,
                        )
                    },
                    trailingIcon = {
                        if (option == selectedMinutes) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = palette.primaryText)
                        }
                    },
                    onClick = {
                        onReminderSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsAllDayReminderTimeRow(
    selectedTime: LocalTime,
    palette: DotCalPalette,
    onTimeSelected: (LocalTime) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleClickable { showPicker = true },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("Default all-day reminder time", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(allDayReminderTimeLabel(selectedTime), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))
            UpDownChevron(tint = palette.secondaryText)
        }
    }
    if (showPicker) {
        AllDayReminderTimeSheet(
            selectedTime = selectedTime,
            palette = palette,
            onDismiss = { showPicker = false },
            onSelected = {
                onTimeSelected(it)
                showPicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllDayReminderTimeSheet(
    selectedTime: LocalTime,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onSelected: (LocalTime) -> Unit,
) {
    val hours = remember { (1..12).toList() }
    val minutes = remember { (0..59).toList() }
    val periods = remember { listOf("AM", "PM") }
    val initialHour = selectedTime.toHour12()
    val initialPeriod = if (selectedTime.hour < 12) "AM" else "PM"
    var pickedHour by remember(selectedTime) { mutableStateOf(initialHour) }
    var pickedMinute by remember(selectedTime) { mutableStateOf(selectedTime.minute) }
    var pickedPeriod by remember(selectedTime) { mutableStateOf(initialPeriod) }
    val pickedTime = remember(pickedHour, pickedMinute, pickedPeriod) {
        LocalTime.of(toHour24(pickedHour, pickedPeriod), pickedMinute)
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        contentColor = palette.primaryText,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.dialogSurface)
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Default all-day reminder time", color = palette.primaryText, fontFamily = mono, fontSize = 20.sp)
            Text(
                allDayReminderTimeLabel(pickedTime),
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
                    items = hours,
                    selected = pickedHour,
                    label = { it.toString() },
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    circular = true,
                    onSelected = { pickedHour = it },
                )
                WheelColumn(
                    items = minutes,
                    selected = pickedMinute,
                    label = { it.toString().padStart(2, '0') },
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    circular = true,
                    onSelected = { pickedMinute = it },
                )
                WheelColumn(
                    items = periods,
                    selected = pickedPeriod,
                    label = { it },
                    palette = palette,
                    modifier = Modifier.weight(1f),
                    circular = true,
                    onSelected = { pickedPeriod = it },
                )
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
                    onClick = { onSelected(pickedTime) },
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
    showChevron: Boolean = true,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .noRippleClickable(onClick = onClick)
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
            } else if (showChevron) {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun SettingsSyncNowRow(
    syncMetadata: List<SyncMetadata>,
    isSyncing: Boolean,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .noRippleClickable(enabled = !isSyncing, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                if (isSyncing) "Syncing..." else "Sync Now",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
            Text(
                syncMetadata.lastSyncedSubtitle(),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 11.sp,
                lineHeight = 13.sp,
            )
        }
        if (isSyncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = palette.accent,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun DotCalSwitch(
    checked: Boolean,
    palette: DotCalPalette,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val trackColor = when {
        checked && enabled -> palette.accent
        checked -> palette.accent.copy(alpha = 0.55f)
        else -> palette.switchOffTrack
    }
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = 48.dp)
            .noRippleClickable(enabled = enabled) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(width = 52.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(trackColor)
                .padding(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(NWhite),
            )
        }
    }
}

@Composable
private fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    palette: DotCalPalette,
    subtitle: String? = null,
    onCheckedChange: (Boolean) -> Unit = {},
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
        DotCalSwitch(
            checked = checked,
            palette = palette,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun SettingsSyncIntervalRow(
    intervalMins: Int,
    palette: DotCalPalette,
    onIntervalSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(0, 15, 30, 60)
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .noRippleClickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Sync interval", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(syncIntervalLabel(intervalMins), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                Spacer(modifier = Modifier.width(8.dp))
                UpDownChevron(tint = palette.secondaryText)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(palette.dialogSurface),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    modifier = Modifier.background(palette.dialogSurface),
                    text = {
                        Text(
                            syncIntervalLabel(option),
                            color = palette.primaryText,
                            fontFamily = mono,
                            fontSize = 16.sp,
                        )
                    },
                    trailingIcon = {
                        if (option == intervalMins) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = palette.primaryText)
                        }
                    },
                    onClick = {
                        onIntervalSelected(option)
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
private fun SettingsContentDivider(palette: DotCalPalette) {
    HorizontalDivider(color = palette.line, thickness = 1.dp, modifier = Modifier.padding(start = 24.dp))
}

@Composable
private fun ThemeOptionRow(
    mode: DotCalThemeMode,
    accentColor: AccentColor,
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
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ThemePreview(mode = mode, accentColor = accentColor)
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
private fun ThemePreview(mode: DotCalThemeMode, accentColor: AccentColor) {
    val preview = dotCalPalette(mode, accentColor)
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
private fun AccentColorSwatches(
    selectedAccent: AccentColor,
    palette: DotCalPalette,
    onAccentSelected: (AccentColor) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AccentColor.entries.forEach { accent ->
            val selected = accent == selectedAccent
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (selected) palette.primaryText else palette.line, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(accent.color)
                    .noRippleClickable { onAccentSelected(accent) },
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(Icons.Default.Check, contentDescription = accent.label, tint = accent.onColor, modifier = Modifier.size(20.dp))
                }
            }
        }
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

private fun weekDays(date: LocalDate, weekStart: DayOfWeek): List<LocalDate> {
    val delta = (7 + date.dayOfWeek.value - weekStart.value) % 7
    val start = date.minusDays(delta.toLong())
    return List(7) { start.plusDays(it.toLong()) }
}

private fun weekDayLabels(weekStart: DayOfWeek): List<String> {
    return List(7) { index ->
        weekStart.plus(index.toLong()).name.take(3)
    }
}

private fun parseWeekStartOption(value: String?): WeekStartOption {
    return WeekStartOption.entries.firstOrNull { it.storageKey == value || it.name == value } ?: WeekStartOption.RegionDefault
}

private fun resolveWeekStartDay(option: WeekStartOption): DayOfWeek {
    return option.fixedDay ?: WeekFields.of(Locale.getDefault()).firstDayOfWeek
}

private fun CalendarEvent.localDate(): LocalDate {
    return Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault()).toLocalDate()
}

private fun CalendarEvent.hasTaskDate(): Boolean {
    return startTimeMs > 0L
}

private fun CalendarEvent.taskDueDetailLabel(): String {
    val date = localDate().format(editorDateFormatter)
    return if (isAllDay == 1) date else "$date, ${startLocalTime().format(timeFormatter)}"
}

private fun CalendarEvent.taskDueDateLine(): String {
    return Instant.ofEpochMilli(startTimeMs)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.US))
}

private fun CalendarEvent.taskDueTimeLine(): String {
    return if (isAllDay == 1) "All-day" else startLocalTime().format(timeFormatter)
}

private fun taskDateHeaderFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.US)
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

private fun LocalTime.toHour12(): Int {
    val h = hour % 12
    return if (h == 0) 12 else h
}

private fun toHour24(hour12: Int, period: String): Int {
    return if (period.uppercase(Locale.US) == "PM") {
        if (hour12 == 12) 12 else hour12 + 12
    } else {
        if (hour12 == 12) 0 else hour12
    }
}

private fun allDayReminderTimeLabel(time: LocalTime): String {
    return DateTimeFormatter.ofPattern("h:mm a", Locale.US).format(time).lowercase(Locale.US)
}

private fun parseStoredTime(value: String?): LocalTime? {
    if (value.isNullOrBlank()) return null
    return runCatching { LocalTime.parse(value) }.getOrNull()
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
    return when (minutes) {
        null -> "None"
        60 -> "1 hour before"
        1440 -> "1 day before"
        else -> "$minutes minutes before"
    }
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

private fun syncIntervalLabel(minutes: Int): String {
    return when (minutes) {
        0 -> "Manual"
        60 -> "1 hour"
        120 -> "2 hours"
        else -> "$minutes min"
    }
}

private fun calendarAccountsLabel(accounts: List<CalendarAccount>, hasCalendarPermission: Boolean): String {
    if (!hasCalendarPermission) return "Local only"
    val providerCount = accounts.count { it.id != "local-primary" }
    if (providerCount == 0) return "Connected"
    val selectedCount = accounts.count { it.id != "local-primary" && it.isVisible == 1 }
    return "$selectedCount/$providerCount selected"
}

private fun selectedHolidayCountriesLabel(countries: List<HolidayCountryUiItem>): String {
    val count = countries.count { it.isSelected }
    return when (count) {
        0 -> "None selected"
        1 -> "1 country selected"
        else -> "$count countries selected"
    }
}

private fun List<SyncMetadata>.lastSyncedLabel(): String {
    return lastSyncedRelativeLabel().replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
    }
}

private fun List<SyncMetadata>.lastSyncedSubtitle(): String {
    return "Last synced ${lastSyncedRelativeLabel()}"
}

private fun List<SyncMetadata>.lastSyncedRelativeLabel(): String {
    val lastSyncMs = maxOfOrNull { it.lastSyncMs } ?: 0L
    if (lastSyncMs <= 0L) return "never"
    val elapsedMinutes = ((System.currentTimeMillis() - lastSyncMs) / 60_000L).coerceAtLeast(0L)
    return when {
        elapsedMinutes < 1L -> "just now"
        elapsedMinutes < 60L -> "$elapsedMinutes min ago"
        elapsedMinutes < 24L * 60L -> "${elapsedMinutes / 60L} hr ago"
        elapsedMinutes < 48L * 60L -> "yesterday"
        else -> "${elapsedMinutes / (24L * 60L)} d ago"
    }
}

private fun String.readableCalendarLabel(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) return "Calendar"
    if (trimmed.contains("@")) return trimmed
    if (trimmed.any { it.isLowerCase() }) return trimmed
    return trimmed.lowercase(Locale.US).replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
    }
}

private fun CalendarAccount.secondaryCalendarLabel(): String {
    val raw = accountName.ifBlank { accountType }.trim()
    if (raw.isBlank()) return "Local"
    return raw.readableCalendarLabel()
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

private fun CalendarEvent.agendaTimeRange(): String {
    if (isAllDay == 1) return "All-day"
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
    val end = Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
    return "${start.format(timeFormatter)} – ${end.format(timeFormatter)}"
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
        "FREQ=YEARLY" -> "REPEATS YEARLY"
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
        context.contentResolver.loadThumbnail(uri, Size(180, 180), null)
    }.getOrNull()
}

private fun loadImagePreview(context: Context, uriValue: String): Bitmap? {
    val uri = runCatching { Uri.parse(uriValue) }.getOrNull() ?: return null
    return runCatching {
        context.contentResolver.loadThumbnail(uri, Size(1280, 1280), null)
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

private fun CalendarEvent.displayColor(palette: DotCalPalette): Color {
    return colorHex?.let { Color(parseColor(it)) } ?: palette.accent
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
    CalendarAccounts,
    AddAccount,
    GlobalHolidays,
    PrivacyPolicy,
}

private enum class WeekStartOption(val storageKey: String, val label: String, val fixedDay: DayOfWeek?) {
    RegionDefault("REGION_DEFAULT", "Region default", null),
    Saturday("SATURDAY", "Saturday", DayOfWeek.SATURDAY),
    Sunday("SUNDAY", "Sunday", DayOfWeek.SUNDAY),
    Monday("MONDAY", "Monday", DayOfWeek.MONDAY),
}

private enum class OnboardingPage {
    Welcome,
    CalendarPermission,
    Notifications,
    Contacts,
    Ready,
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
    val switchOffTrack: Color,
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

private enum class AccentColor(val hex: String, val label: String) {
    RED("#FF3B30", "Red"),
    BLUE("#0A84FF", "Blue"),
    GREEN("#30D158", "Green"),
    PURPLE("#BF5AF2", "Purple"),
    AMBER("#FF9F0A", "Amber");

    val color: Color
        get() = Color(android.graphics.Color.parseColor(hex))

    val onColor: Color
        get() = when (this) {
            GREEN, AMBER -> Color(0xFF101010)
            RED, BLUE, PURPLE -> Color(0xFFFFFFFF)
        }

    companion object {
        fun fromStorage(value: String?): AccentColor {
            return entries.firstOrNull { it.name == value } ?: RED
        }
    }
}

private fun dotCalPalette(mode: DotCalThemeMode, accentColor: AccentColor = AccentColor.RED, systemDark: Boolean = false): DotCalPalette {
    val resolved = if (mode == DotCalThemeMode.System) {
        if (systemDark) DotCalThemeMode.Dark else DotCalThemeMode.Light
    } else {
        mode
    }
    val accent = accentColor.color
    val onAccent = accentColor.onColor
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
            switchOffTrack = Color(0xFF3A3A3A),
            dot = Color(0xFFFFFFFF),
            yearWeekday = Color(0xFFFFFFFF),
            yearMonthLabel = Color(0xFFFFFFFF),
            accent = accent,
            onAccent = onAccent,
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
            switchOffTrack = Color(0xFFDADADA),
            dot = Color(0xFF101010),
            yearWeekday = Color(0xFF101010),
            yearMonthLabel = Color(0xFF101010),
            accent = accent,
            onAccent = onAccent,
            isDark = false,
        )
        DotCalThemeMode.System -> error("System must be resolved before palette creation")
    }
}

private fun dotCalBootPalette(accentColor: AccentColor = AccentColor.RED): DotCalPalette {
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
        switchOffTrack = Color(0xFF3A3A3A),
        dot = Color(0xFFFFFFFF),
        yearWeekday = Color(0xFFFFFFFF),
        yearMonthLabel = Color(0xFFFFFFFF),
        accent = accentColor.color,
        onAccent = accentColor.onColor,
        isDark = true,
    )
}

private enum class ScreenTab {
    Calendar,
    Tasks,
    Settings,
}
