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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.filled.Lock
import androidx.compose.ui.layout.onSizeChanged
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings as SettingsGearIcon
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.draw.shadow
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.graphics.SolidColor
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.ColumnScope
import com.dotfield.dotcal.R
import com.dotfield.dotcal.data.billing.ProManager
import com.dotfield.dotcal.presentation.datecalculator.DateCalculatorViewModel
import androidx.datastore.preferences.core.edit
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowCompat
import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.DotCalRepository
import com.dotfield.dotcal.data.EventEditorData
import com.dotfield.dotcal.data.EventReminder
import com.dotfield.dotcal.data.ics.IcsExporter
import com.dotfield.dotcal.data.nlp.QuickAddParser
import com.dotfield.dotcal.data.nlp.QuickAddResult
import com.dotfield.dotcal.data.privacy.AppLockState
import com.dotfield.dotcal.data.recurrence.ByDay
import com.dotfield.dotcal.data.recurrence.RecurrenceFreq
import com.dotfield.dotcal.data.recurrence.RecurrenceRule
import com.dotfield.dotcal.data.baseEventId
import com.dotfield.dotcal.data.isRecurrenceOccurrence
import com.dotfield.dotcal.data.RecurringEditScope
import com.dotfield.dotcal.data.SyncMetadata
import com.dotfield.dotcal.data.TaskEditorData
import com.dotfield.dotcal.data.profiles.FocusProfile
import com.dotfield.dotcal.data.shifts.ShiftPattern
import com.dotfield.dotcal.data.shifts.ShiftType
import com.dotfield.dotcal.data.templates.EventTemplate
import com.dotfield.dotcal.data.trash.DeletedSnapshot
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

internal val mono = FontFamily.SansSerif
private const val BOOT_PREFS = "dotcal_boot"
private const val BOOT_THEME_KEY = "theme_mode"
private const val BOOT_ACCENT_KEY = "accent_color"
private const val BOOT_DEFAULT_VIEW_KEY = "default_view"
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
    initialAddEventDate: String? = null,
    initialPaywall: Boolean = false,
    initialRouteToken: Long? = null,
) {
    val month by viewModel.month.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val agendaEvents by viewModel.agendaEvents.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val accounts by viewModel.accounts.collectAsStateWithLifecycle()
    val assignableAccounts by viewModel.assignableAccounts.collectAsStateWithLifecycle()
    val lastSelectedEventAccountId by viewModel.lastSelectedEventAccountId.collectAsStateWithLifecycle()
    val conflictWarnings by viewModel.conflictWarnings.collectAsStateWithLifecycle()
    val holidayCountries by viewModel.holidayCountries.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val syncMetadata by viewModel.syncMetadata.collectAsStateWithLifecycle()
    val detailEvent by viewModel.detailEvent.collectAsStateWithLifecycle()
    var screenTab by remember { mutableStateOf(ScreenTab.Calendar) }
    var previousScreenTab by remember { mutableStateOf(ScreenTab.Calendar) }
    var showSheet by remember { mutableStateOf(false) }
    var addSheet by remember { mutableStateOf(false) }
    var addStartTime by remember { mutableStateOf(LocalTime.of(9, 0)) }
    var addEditorDateOverride by remember { mutableStateOf<LocalDate?>(null) }
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var taskDetail by remember { mutableStateOf<CalendarEvent?>(null) }
    var lastTaskDetail by remember { mutableStateOf<CalendarEvent?>(null) }
    var editingTask by remember { mutableStateOf<CalendarEvent?>(null) }
    var showTaskEditor by remember { mutableStateOf(false) }
    var editorSessionKey by remember { mutableStateOf(UUID.randomUUID().toString()) }
    var settingsScreen by remember { mutableStateOf(SettingsScreen.Root) }
    var showPaywall by remember { mutableStateOf(false) }
    var showDateCalculator by remember { mutableStateOf(false) }
    var showQuickAdd by remember { mutableStateOf(false) }
    var showRecentlyDeleted by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var showTemplates by remember { mutableStateOf(false) }
    var showFocusProfiles by remember { mutableStateOf(false) }
    var showShiftPatterns by remember { mutableStateOf(false) }
    var selectedBulkDates by remember { mutableStateOf<Set<LocalDate>>(emptySet()) }
    var showBulkTemplatePicker by remember { mutableStateOf(false) }
    var quickAddPrefill by remember { mutableStateOf<QuickAddResult?>(null) }
    var duplicateDraftPrefill by remember { mutableStateOf<EventEditorData?>(null) }
    var pendingCopyToDateEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var pendingShareEvent by remember { mutableStateOf<CalendarEvent?>(null) }
    var templatePrefill by remember { mutableStateOf<EventTemplate?>(null) }
    var taskTemplatePrefill by remember { mutableStateOf<EventTemplate?>(null) }
    val isPro by viewModel.isPro.collectAsStateWithLifecycle()
    val appLockState by viewModel.appLockState.collectAsStateWithLifecycle()
    val privateVaultIds by viewModel.privateVaultIds.collectAsStateWithLifecycle()
    val privateVaultEvents by viewModel.privateVaultEvents.collectAsStateWithLifecycle()
    var pendingDelete by remember { mutableStateOf<PendingDelete?>(null) }
    var pendingTaskDelete by remember { mutableStateOf<CalendarEvent?>(null) }
    var appUnlocked by remember { mutableStateOf(false) }
    var handledTaskDeepLinkId by remember { mutableStateOf<String?>(null) }
    var handledRouteToken by remember { mutableStateOf<Long?>(null) }
    var routePending by remember(initialRouteToken) {
        mutableStateOf(initialRouteToken != null && (initialEventId != null || !initialTaskId.isNullOrBlank() || initialAddEvent || initialCalendarDate != null || initialPaywall))
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
    val bootDefaultView = remember(bootPreferences) {
        CalendarTab.fromStorage(bootPreferences.getString(BOOT_DEFAULT_VIEW_KEY, null))
    }
    val bootPalette = remember(bootAccentColor) {
        dotCalBootPalette(bootAccentColor)
    }
    val keyguardManager = remember(context) {
        context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
    }
    val deviceCredentialLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) appUnlocked = true
    }
    val requestDeviceUnlock: () -> Unit = {
        val intent = keyguardManager.createConfirmDeviceCredentialIntent("Unlock DotCal", "Confirm your device lock to continue.")
        if (intent != null) {
            deviceCredentialLauncher.launch(intent)
        } else {
            showDotCalToast(context, bootPalette, "Device lock unavailable")
        }
    }

    // In-app update (Play Flexible). Silent-fails when not installed from Play Store.
    val appUpdateManager = remember(context) { AppUpdateManagerFactory.create(context) }
    var updateAvailable by remember { mutableStateOf(false) }
    var updateDownloaded by remember { mutableStateOf(false) }
    var updateCheckedThisSession by remember { mutableStateOf(false) }
    val updateFlowLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { /* download progress handled by InstallStateUpdatedListener */ }
    val checkForUpdates: (Boolean) -> Unit = { manual ->
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                val flexibleAllowed = info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
                when {
                    info.installStatus() == InstallStatus.DOWNLOADED -> updateDownloaded = true
                    info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE && flexibleAllowed -> updateAvailable = true
                    manual -> showDotCalToast(context, bootPalette, "DotCal is up to date")
                }
            }
            .addOnFailureListener {
                if (manual) showDotCalToast(context, bootPalette, "Couldn't check for updates")
            }
    }
    val startFlexibleUpdate: () -> Unit = {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                runCatching {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateFlowLauncher,
                        AppUpdateOptions.defaultOptions(AppUpdateType.FLEXIBLE),
                    )
                }
            }
        }
    }
    DisposableEffect(appUpdateManager) {
        val listener = InstallStateUpdatedListener { state ->
            if (state.installStatus() == InstallStatus.DOWNLOADED) {
                updateDownloaded = true
            }
        }
        appUpdateManager.registerListener(listener)
        onDispose { appUpdateManager.unregisterListener(listener) }
    }
    val updateLifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(updateLifecycleOwner, appUpdateManager, appLockState.enabled) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                    if (info.installStatus() == InstallStatus.DOWNLOADED) updateDownloaded = true
                }
            } else if (event == Lifecycle.Event.ON_STOP && appLockState.enabled) {
                appUnlocked = false
                // Bottom sheets and dialogs render in their own window, above the
                // AppLockScreen composable. Dismiss any that are open so event content
                // can't float over the PIN screen while the app is re-locked.
                showSheet = false
                showTaskEditor = false
                editingTask = null
                pendingDelete = null
                pendingTaskDelete = null
            }
        }
        updateLifecycleOwner.lifecycle.addObserver(observer)
        onDispose { updateLifecycleOwner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) {
        if (!updateCheckedThisSession) {
            updateCheckedThisSession = true
            checkForUpdates(false)
        }
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
    val appFont by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            AppFont.fromId(preferences[CalendarPreferences.KEY_APP_FONT])
        }
    }.collectAsStateWithLifecycle(initialValue = AppFont.NDot)
    val storedCalendarTab by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            CalendarTab.fromStorage(preferences[CalendarPreferences.KEY_DEFAULT_VIEW])
        }
    }.collectAsStateWithLifecycle(initialValue = bootDefaultView)
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
    val defaultEventDurationMinutes by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            val stored = preferences[CalendarPreferences.KEY_DEFAULT_EVENT_DURATION] ?: 60
            stored.takeIf { it in defaultEventDurationOptions } ?: 60
        }
    }.collectAsStateWithLifecycle(initialValue = 60)
    val defaultAllDayReminderTime by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            parseStoredTime(preferences[CalendarPreferences.KEY_DEFAULT_ALL_DAY_REMINDER_TIME]) ?: LocalTime.of(8, 0)
        }
    }.collectAsStateWithLifecycle(initialValue = LocalTime.of(8, 0))
    val use24HourFormat by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            preferences[CalendarPreferences.KEY_24_HOUR_FORMAT] ?: true
        }
    }.collectAsStateWithLifecycle(initialValue = true)
    val weekStartOption by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            parseWeekStartOption(preferences[CalendarPreferences.KEY_WEEK_START])
        }
    }.collectAsStateWithLifecycle(initialValue = WeekStartOption.RegionDefault)
    val showWeekNumbers by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            preferences[CalendarPreferences.KEY_SHOW_WEEK_NUMBERS] ?: false
        }
    }.collectAsStateWithLifecycle(initialValue = false)
    val widgetTransparent by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            preferences[CalendarPreferences.KEY_WIDGET_TRANSPARENT] ?: false
        }
    }.collectAsStateWithLifecycle(initialValue = false)
    val widgetDotTexture by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            preferences[CalendarPreferences.KEY_WIDGET_DOT_TEXTURE] ?: true
        }
    }.collectAsStateWithLifecycle(initialValue = true)
    val weekStartDay = remember(weekStartOption) { resolveWeekStartDay(weekStartOption) }
    val onboardingDone by remember(context) {
        context.calendarPreferencesDataStore.data.map { preferences ->
            preferences[CalendarPreferences.KEY_ONBOARDING_DONE] ?: false
        }
    }.collectAsStateWithLifecycle<Boolean?>(initialValue = null)
    val systemDark = isSystemInDarkTheme()
    val resolvedThemeMode = themeMode
    val resolvedAccentColor = accentColor
    val palette = remember(resolvedThemeMode, resolvedAccentColor, systemDark) {
        dotCalPalette(resolvedThemeMode, resolvedAccentColor, systemDark)
    }
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
            showDotCalToast(context, palette, "Account setup unavailable")
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
            showDotCalToast(context, palette, "Account setup unavailable")
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
                showDotCalToast(context, palette, "$imported Birthdays Imported")
            }
        } else {
            showDotCalToast(context, palette, "Contacts access needed")
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

    // ----- ICS export / import (Pro) -----
    val exportIcsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/calendar"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.exportIcs { result ->
            result.onSuccess { text ->
                val ok = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                }.isSuccess
                showDotCalToast(context, palette, if (ok) "Calendar exported" else "Export failed")
            }.onFailure {
                showDotCalToast(context, palette, "Export failed")
            }
        }
    }
    val importIcsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
        if (text.isNullOrBlank()) {
            showDotCalToast(context, palette, "Couldn't read file")
            return@rememberLauncherForActivityResult
        }
        viewModel.importIcs(text) { result ->
            result.onSuccess { summary ->
                showDotCalToast(
                    context,
                    palette,
                    "Imported: ${summary.inserted} new, ${summary.updated} updated",
                    Toast.LENGTH_LONG,
                )
            }.onFailure {
                showDotCalToast(context, palette, "Import failed - not a valid .ics file")
            }
        }
    }
    val backupLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        viewModel.exportBackup { result ->
            result.onSuccess { text ->
                val ok = runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                }.isSuccess
                showDotCalToast(context, palette, if (ok) "Backup saved" else "Backup failed")
            }.onFailure {
                showDotCalToast(context, palette, "Backup failed")
            }
        }
    }
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val text = runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
        if (text.isNullOrBlank()) {
            showDotCalToast(context, palette, "Couldn't read file")
            return@rememberLauncherForActivityResult
        }
        viewModel.importBackup(text) { result ->
            result.onSuccess { summary ->
                showDotCalToast(
                    context,
                    palette,
                    "Restored: ${summary.eventsInserted} new, ${summary.eventsUpdated} updated",
                    Toast.LENGTH_LONG,
                )
            }.onFailure {
                showDotCalToast(context, palette, "Restore failed - not a valid DotCal backup")
            }
        }
    }
    var calendarTab by remember { mutableStateOf(storedCalendarTab) }
    LaunchedEffect(storedCalendarTab) {
        calendarTab = storedCalendarTab
    }
    val activeCalendarTab = calendarTab
    SystemBarColorSync(palette)
    LaunchedEffect(resolvedThemeMode, resolvedAccentColor, storedCalendarTab, systemDark) {
        bootPreferences.edit()
            .putString(BOOT_THEME_KEY, resolvedThemeMode.name)
            .putString(BOOT_ACCENT_KEY, resolvedAccentColor.storageValue)
            .putString(BOOT_DEFAULT_VIEW_KEY, storedCalendarTab.name)
            .apply()
        WidgetUpdateWorker.enqueue(context)
    }
    LaunchedEffect(storedSelectedDateValue, initialEventId, initialTaskId, initialCalendarDate) {
        val storedValue = storedSelectedDateValue ?: return@LaunchedEffect
        if (!selectedDateRestored) {
            selectedDateRestored = true
        }
    }
    LaunchedEffect(selectedDate, selectedDateRestored) {
        if (selectedDateRestored) {
            // Debounce: rapid Week/Day paging cancels this effect on each change, so
            // only the final date after a short idle is committed to disk.
            delay(400)
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
        bootPreferences.edit().putString(BOOT_DEFAULT_VIEW_KEY, tab.name).apply()
        scope.launch {
            context.calendarPreferencesDataStore.edit { preferences ->
                preferences[CalendarPreferences.KEY_DEFAULT_VIEW] = tab.name
            }
        }
    }
    fun openAddEditor(startTime: LocalTime = LocalTime.of(9, 0), date: LocalDate? = null) {
        editorSessionKey = UUID.randomUUID().toString()
        quickAddPrefill = null
        duplicateDraftPrefill = null
        templatePrefill = null
        addStartTime = startTime
        addEditorDateOverride = date
        editingEvent = null
        addSheet = true
    }
    fun openQuickAddResult(result: QuickAddResult) {
        editorSessionKey = UUID.randomUUID().toString()
        quickAddPrefill = result
        duplicateDraftPrefill = null
        templatePrefill = null
        addStartTime = result.startTime ?: LocalTime.of(9, 0)
        addEditorDateOverride = result.date
        editingEvent = null
        showQuickAdd = false
        addSheet = true
    }
    fun blockFromTask(task: CalendarEvent) {
        // Task Time Blocking (FREE): seed a brand-new calendar event from the task so the
        // user can place it on the timeline. Reuses the QuickAdd prefill path; the original
        // task is left untouched (non-destructive).
        val hasDate = task.hasTaskDate()
        val blockDate = if (hasDate) task.localDate() else selectedDate
        val timed = hasDate && task.isAllDay == 0
        val startTime = if (timed) task.startLocalTime() else LocalTime.of(9, 0)
        val endTime = startTime.plusHours(1)
        val prefill = QuickAddResult(
            title = task.title,
            date = blockDate,
            endDate = blockDate,
            startTime = startTime,
            endTime = endTime,
            isAllDay = false,
            rrule = null,
        )
        taskDetail = null
        openQuickAddResult(prefill)
    }
    fun useTemplate(template: EventTemplate) {
        showTemplates = false
        if (template.isTask) {
            taskTemplatePrefill = template
            editingTask = null
            showTaskEditor = true
        } else {
            editorSessionKey = UUID.randomUUID().toString()
            quickAddPrefill = null
            duplicateDraftPrefill = null
            templatePrefill = template
            addStartTime = template.startMinuteOfDay?.let { LocalTime.of(it / 60, it % 60) } ?: LocalTime.of(9, 0)
            addEditorDateOverride = selectedDate
            editingEvent = null
            addSheet = true
        }
    }
    LaunchedEffect(initialRouteToken, initialAddEvent, initialAddEventDate) {
        if (initialRouteToken != null && handledRouteToken != initialRouteToken && initialAddEvent) {
            viewModel.closeEventDetail()
            taskDetail = null
            settingsScreen = SettingsScreen.Root
            previousScreenTab = ScreenTab.Calendar
            screenTab = ScreenTab.Calendar
            val addDate = initialAddEventDate
                ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                ?: LocalDate.now()
            viewModel.selectDate(addDate)
            openAddEditor(date = addDate)
            handledRouteToken = initialRouteToken
            routePending = false
        }
    }
    LaunchedEffect(initialRouteToken, initialPaywall) {
        if (initialRouteToken != null && handledRouteToken != initialRouteToken && initialPaywall) {
            viewModel.closeEventDetail()
            taskDetail = null
            settingsScreen = SettingsScreen.Root
            showPaywall = true
            handledRouteToken = initialRouteToken
            routePending = false
        }
    }
    fun openEditEditor(event: CalendarEvent) {
        editorSessionKey = UUID.randomUUID().toString()
        quickAddPrefill = null
        duplicateDraftPrefill = null
        templatePrefill = null
        addEditorDateOverride = null
        editingEvent = event
        addSheet = true
    }
    fun duplicateDraftFor(event: CalendarEvent, targetDate: LocalDate? = null): EventEditorData {
        val originalStartDate = event.localDate()
        val copyDate = targetDate ?: originalStartDate
        val originalEndDate = event.endLocalDateForEditor()
        val endDate = copyDate.plusDays(
            java.time.temporal.ChronoUnit.DAYS.between(originalStartDate, originalEndDate).coerceAtLeast(0),
        )
        val reminderMinutes = reminders
            .filter { it.eventId == event.baseEventId() }
            .map { it.minutesBefore }
            .distinct()
            .sorted()
        return EventEditorData(
            eventId = UUID.randomUUID().toString(),
            accountId = event.accountId,
            title = event.title,
            description = event.description,
            location = event.location,
            date = copyDate,
            endDate = endDate,
            startTime = event.startLocalTime(),
            endTime = event.endLocalTime(),
            isAllDay = event.isAllDay == 1,
            reminderMinutes = reminderMinutes.firstOrNull(),
            reminderMinutesList = reminderMinutes.takeIf { it.isNotEmpty() },
            rrule = event.rrule,
            imageUris = "[]",
            voiceNotePath = null,
            colorHex = event.colorHex,
        )
    }
    fun openDuplicateEditor(event: CalendarEvent, targetDate: LocalDate? = null) {
        val draft = duplicateDraftFor(event, targetDate)
        editorSessionKey = UUID.randomUUID().toString()
        quickAddPrefill = null
        duplicateDraftPrefill = draft
        templatePrefill = null
        addStartTime = draft.startTime
        addEditorDateOverride = draft.date
        editingEvent = null
        pendingCopyToDateEvent = null
        viewModel.closeEventDetail()
        addSheet = true
    }
    fun closeTopSurface() {
        when {
            showPaywall -> showPaywall = false
            pendingShareEvent != null -> pendingShareEvent = null
            pendingCopyToDateEvent != null -> pendingCopyToDateEvent = null
            showBulkTemplatePicker -> showBulkTemplatePicker = false
            showTemplates -> showTemplates = false
            showFocusProfiles -> showFocusProfiles = false
            showShiftPatterns -> showShiftPatterns = false
            showSearch -> showSearch = false
            showRecentlyDeleted -> showRecentlyDeleted = false
            showDateCalculator -> showDateCalculator = false
            showQuickAdd -> showQuickAdd = false
            addSheet -> {
                editingEvent = null
                addEditorDateOverride = null
                quickAddPrefill = null
                duplicateDraftPrefill = null
                templatePrefill = null
                addSheet = false
            }
            showTaskEditor -> {
                editingTask = null
                taskTemplatePrefill = null
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
                showDotCalToast(context, palette, message)
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
    val isAppLocked = appLockState.enabled && !appUnlocked && !showOnboarding && onboardingPreferenceLoaded
    BackHandler(enabled = isAppLocked) {}
    BackHandler(enabled = !showOnboarding && (showPaywall || pendingShareEvent != null || pendingCopyToDateEvent != null || showTemplates || showFocusProfiles || showSearch || showRecentlyDeleted || showDateCalculator || showQuickAdd || detailEvent != null || taskDetail != null || addSheet || showTaskEditor || screenTab == ScreenTab.Settings || screenTab == ScreenTab.Tasks)) {
        closeTopSurface()
    }

    // Group events by day once at the top level so the buckets survive Calendar <-> Tasks
    // <-> Settings switches and every calendar view reuses them instead of re-deriving.
    val eventsByDate = remember(events) { events.groupBy { it.localDate() } }
    val appFontFamily = rememberAppFontFamily(appFont)
    CompositionLocalProvider(LocalHeadingFont provides appFontFamily) {
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
            containerColor = palette.background,
            bottomBar = {
                // Zero-height spacer: Scaffold's contentWindowInsets already adds the nav bar
                // height. Removing the 90dp extra here lets content extend to the nav bar edge
                // so LazyColumns can use their own 90dp contentPadding to clear the floating pill
                // (same approach as the Settings overlay).
                Box(Modifier.fillMaxWidth().height(0.dp))
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(palette.background),
            ) {
                AnimatedContent(
                    targetState = visibleMainTab,
                    transitionSpec = {
                        val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                        (slideInHorizontally(tween(220, easing = FastOutSlowInEasing)) { (it * 0.25f).toInt() * direction } +
                            fadeIn(tween(180))) togetherWith
                            (slideOutHorizontally(tween(200, easing = FastOutSlowInEasing)) { (it * 0.25f).toInt() * -direction } +
                                fadeOut(tween(150)))
                    },
                    modifier = Modifier.fillMaxSize(),
                    label = "tabContent",
                ) { tab ->
                    when (tab) {
                        ScreenTab.Calendar -> CalendarTabContainer(
                            title = calendarHeaderLabel,
                            activeCalendarTab = activeCalendarTab,
                            palette = palette,
                            onTitleClick = { viewModel.selectDate(LocalDate.now()) },
                            onAdd = { openAddEditor() },
                            onTemplates = {
                                if (isPro) {
                                    viewModel.refreshTemplates()
                                    showTemplates = true
                                } else {
                                    showPaywall = true
                                }
                            },
                            onQuickAdd = { if (isPro) showQuickAdd = true else showPaywall = true },
                            onSearch = { showSearch = true },
                            onCalendarTabSelected = {
                                screenTab = ScreenTab.Calendar
                                previousScreenTab = ScreenTab.Calendar
                                selectCalendarTab(it)
                            },
                        ) {
                            Crossfade(
                                targetState = activeCalendarTab,
                                animationSpec = tween(durationMillis = 150),
                                label = "calendarViewSwitch",
                            ) { calendarTab ->
                                when (calendarTab) {
                                    CalendarTab.Month -> MonthView(
                                        month = month,
                                        selectedDate = selectedDate,
                                        eventsByDate = eventsByDate,
                                        palette = palette,
                                        weekStart = weekStartDay,
                                        showWeekNumbers = showWeekNumbers,
                                        onPrevious = viewModel::previousMonth,
                                        onNext = viewModel::nextMonth,
                                        onJumpToday = { viewModel.selectDate(LocalDate.now()) },
                                        selectedBulkDates = selectedBulkDates,
                                        onBulkSelectionStart = { date ->
                                            if (!isPro) {
                                                showPaywall = true
                                            } else {
                                                selectedBulkDates = setOf(date)
                                            }
                                        },
                                        onBulkApply = {
                                            if (isPro) {
                                                viewModel.refreshTemplates()
                                                showBulkTemplatePicker = true
                                            } else {
                                                showPaywall = true
                                            }
                                        },
                                        onBulkClear = { selectedBulkDates = emptySet() },
                                        onDateSelected = {
                                            if (selectedBulkDates.isNotEmpty()) {
                                                selectedBulkDates = if (it in selectedBulkDates) selectedBulkDates - it else selectedBulkDates + it
                                            } else {
                                                viewModel.selectDate(it)
                                                showSheet = true
                                            }
                                        },
                                    )
                                    CalendarTab.Week -> WeekView(
                                        selectedDate = selectedDate,
                                        eventsByDate = eventsByDate,
                                        palette = palette,
                                        weekStart = weekStartDay,
                                        showWeekNumbers = showWeekNumbers,
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
                                        eventsByDate = eventsByDate,
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
                                        eventsByDate = eventsByDate,
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
                                        events = agendaEvents,
                                        palette = palette,
                                        onAdd = { openAddEditor() },
                                        onEventClick = viewModel::openEventDetail,
                                    )
                                    CalendarTab.Year -> YearView(
                                        selectedDate = selectedDate,
                                        eventsByDate = eventsByDate,
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
                        }
                        ScreenTab.Tasks -> TasksScreen(
                            tasks = tasks,
                            reminders = reminders,
                            palette = palette,
                            onAddClick = {
                                editingTask = null
                                taskTemplatePrefill = null
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
        }
        if (routePending) {
            Box(modifier = Modifier.fillMaxSize().background(palette.background))
        }
        if (!onboardingPreferenceLoaded && initialRouteToken == null) {
            Box(modifier = Modifier.fillMaxSize().background(palette.background))
        }
        AnimatedVisibility(
            visible = showOnboarding,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
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
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier
                .fillMaxSize()
                .background(palette.calendarSurface)
                .statusBarsPadding(),
        ) {
            SettingsPreview(
                themeMode = resolvedThemeMode,
                accentColor = resolvedAccentColor,
                appFont = appFont,
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
                    bootPreferences.edit().putString(BOOT_ACCENT_KEY, selectedAccent.storageValue).apply()
                    scope.launch {
                        context.calendarPreferencesDataStore.edit { preferences ->
                            preferences[CalendarPreferences.KEY_ACCENT_COLOR] = selectedAccent.storageValue
                        }
                        WidgetUpdateWorker.enqueue(context)
                    }
                },
                onAppFontSelected = { selectedFont ->
                    scope.launch {
                        context.calendarPreferencesDataStore.edit { preferences ->
                            preferences[CalendarPreferences.KEY_APP_FONT] = selectedFont.id
                        }
                    }
                },
                syncEnabled = syncEnabled,
                syncIntervalMins = syncIntervalMins,
                syncMetadata = syncMetadata,
                isSyncing = isSyncing,
                birthdayEnabled = birthdayEnabled,
                defaultReminderMinutes = defaultReminderMinutes,
                defaultEventDurationMinutes = defaultEventDurationMinutes,
                defaultCalendarTab = storedCalendarTab,
                showWeekNumbers = showWeekNumbers,
                defaultAllDayReminderTime = defaultAllDayReminderTime,
                weekStartOption = weekStartOption,
                widgetTransparent = widgetTransparent,
                widgetDotTexture = widgetDotTexture,
                appLockState = appLockState,
                privateVaultEvents = privateVaultEvents,
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
                onDefaultEventDurationSelected = { minutes ->
                    scope.launch {
                        context.calendarPreferencesDataStore.edit { preferences ->
                            preferences[CalendarPreferences.KEY_DEFAULT_EVENT_DURATION] = minutes
                        }
                    }
                },
                onDefaultViewSelected = { tab ->
                    selectCalendarTab(tab)
                },
                onShowWeekNumbersChange = { enabled ->
                    scope.launch {
                        context.calendarPreferencesDataStore.edit { preferences ->
                            preferences[CalendarPreferences.KEY_SHOW_WEEK_NUMBERS] = enabled
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
                onWidgetTransparentChange = { enabled ->
                    if (!isPro) {
                        showPaywall = true
                    } else {
                        scope.launch {
                            context.calendarPreferencesDataStore.edit { preferences ->
                                preferences[CalendarPreferences.KEY_WIDGET_TRANSPARENT] = enabled
                            }
                            WidgetUpdateWorker.updateNow(context)
                        }
                    }
                },
                onWidgetDotTextureChange = { enabled ->
                    if (!isPro) {
                        showPaywall = true
                    } else {
                        scope.launch {
                            context.calendarPreferencesDataStore.edit { preferences ->
                                preferences[CalendarPreferences.KEY_WIDGET_DOT_TEXTURE] = enabled
                            }
                            WidgetUpdateWorker.updateNow(context)
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
                                showDotCalToast(context, palette, message)
                            }
                        } else {
                            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                    } else {
                        viewModel.setBirthdayCalendarEnabled(false) {
                            showDotCalToast(context, palette, "Birthdays disabled")
                        }
                    }
                },
                onAddHolidayCountry = { item ->
                    viewModel.addHolidayCountry(item) { result ->
                        if (result.isFailure) {
                            showDotCalToast(context, palette, "Could not add holidays")
                        }
                    }
                },
                onRemoveHolidayCountry = { item ->
                    viewModel.removeHolidayCountry(item) { result ->
                        if (result.isFailure) {
                            showDotCalToast(context, palette, "Could not remove holidays")
                        }
                    }
                },
                onRateDotCal = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.dotfield.dotcal")),
                    )
                },
                onCheckForUpdates = { checkForUpdates(true) },
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
                isPro = isPro,
                onDotCalPro = {
                    if (isPro) {
                        showDotCalToast(context, palette, "You're already Pro!")
                    } else {
                        showPaywall = true
                    }
                },
                onRestorePurchase = {
                    viewModel.restorePro { restored ->
                        showDotCalToast(
                            context,
                            palette,
                            if (restored) "Purchase restored - enjoy DotCal Pro!" else "No previous purchase found on this account",
                        )
                    }
                },
                onDateCalculator = {
                    if (isPro) showDateCalculator = true else showPaywall = true
                },
                onAppPrivacy = {
                    if (!isPro) {
                        showPaywall = true
                    } else {
                        viewModel.refreshPrivateVault()
                        settingsScreen = SettingsScreen.AppPrivacy
                    }
                },
                onSetAppLockPin = { pin, onResult ->
                    viewModel.setAppLockPin(pin) { result ->
                        if (result.isSuccess) appUnlocked = true
                        onResult(result)
                    }
                },
                onVerifyAppLockPin = viewModel::verifyAppLockPin,
                onSetAppLockEnabled = { enabled ->
                    viewModel.setAppLockEnabled(enabled) {
                        appUnlocked = enabled
                    }
                },
                onDisableAppLock = {
                    viewModel.disableAppLock {
                        appUnlocked = false
                        showDotCalToast(context, palette, "App Lock disabled")
                    }
                },
                onClearAppLockPin = {
                    viewModel.clearAppLockPin {
                        appUnlocked = false
                        showDotCalToast(context, palette, "PIN removed")
                    }
                },
                onRestorePrivateEvent = { eventId ->
                    viewModel.restoreFromPrivateVault(eventId) {
                        showDotCalToast(context, palette, "Restored from Private Vault")
                    }
                },
                onRecentlyDeleted = {
                    viewModel.refreshRecentlyDeleted()
                    showRecentlyDeleted = true
                },
                onTemplates = {
                    if (isPro) {
                        viewModel.refreshTemplates()
                        showTemplates = true
                    } else {
                        showPaywall = true
                    }
                },
                onCalendarSets = {
                    if (isPro) {
                        viewModel.refreshFocusProfiles()
                        showFocusProfiles = true
                    } else {
                        showPaywall = true
                    }
                },
                onShiftPatterns = {
                    if (isPro) {
                        viewModel.refreshShiftPatterns()
                        showShiftPatterns = true
                    } else {
                        showPaywall = true
                    }
                },
                onExportIcs = {
                    // FREE feature (data portability): no Pro gate.
                    val stamp = java.time.LocalDate.now().toString()
                    exportIcsLauncher.launch("dotcal-$stamp.ics")
                },
                onImportIcs = {
                    // FREE feature (data portability): no Pro gate.
                    importIcsLauncher.launch(arrayOf("text/calendar", "application/octet-stream", "*/*"))
                },
                onBackup = {
                    // FREE feature (data safety): no Pro gate.
                    val stamp = java.time.LocalDate.now().toString()
                    backupLauncher.launch("dotcal-backup-$stamp.json")
                },
                onRestore = {
                    // FREE feature (data safety): never trap user data behind a paywall.
                    restoreLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
                },
            )
        }
        // Floating bottom nav - rendered AFTER Settings overlay so it appears on top of Settings,
        // but BEFORE full-screen overlays (EventDetail, AddEvent, etc.) so those cover it correctly.
        // Hidden on onboarding and on Settings sub-screens (non-Root).
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            if (!showOnboarding && (screenTab != ScreenTab.Settings || settingsScreen == SettingsScreen.Root))
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
        }
        // Light-black (or light-white) translucent scrim drawn ONLY behind the system
        // navigation buttons. Same on Calendar/Tasks/Settings so the phone buttons stay
        // readable with a "frosted transparent" feel instead of bleeding into content
        // (Calendar/Tasks were fully transparent, Settings was solid black - now unified).
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                .background(
                    if (palette.isDark) Color.Black.copy(alpha = 0.45f)
                    else Color.White.copy(alpha = 0.55f),
                ),
        )
        AnimatedVisibility(
            visible = detailEvent != null,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            detailEvent?.let { event ->
                EventDetailScreen(
                    event = event,
                    reminders = reminders.filter { it.eventId == event.baseEventId() },
                    account = accounts.firstOrNull { it.id == event.accountId },
                    palette = palette,
                    isPrivate = event.baseEventId() in privateVaultIds,
                    onBack = viewModel::closeEventDetail,
                    onEdit = {
                        openEditEditor(event)
                    },
                    onShare = { pendingShareEvent = event },
                    onDuplicate = { openDuplicateEditor(event) },
                    onCopyToDate = { pendingCopyToDateEvent = event },
                    onMoveToPrivate = {
                        if (!isPro) {
                            showPaywall = true
                        } else {
                            viewModel.moveToPrivateVault(event) {
                                viewModel.closeEventDetail()
                                showDotCalToast(context, palette, "Moved to Private Vault")
                            }
                        }
                    },
                    onRestoreFromPrivate = {
                        viewModel.restoreFromPrivateVault(event.baseEventId()) {
                            showDotCalToast(context, palette, "Restored from Private Vault")
                        }
                    },
                    onDelete = {
                        pendingDelete = PendingDelete(event, RecurringEditScope.WholeSeries, DeleteSource.Detail)
                    },
                )
            }
        }
        pendingShareEvent?.let { event ->
            ModalBottomSheet(
                onDismissRequest = { pendingShareEvent = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = palette.dialogSurface,
                dragHandle = { BottomSheetDragHandle(palette) },
            ) {
                CompactActionSheetContent(
                    title = "Share Event",
                    actions = ShareEventOption.entries.map { option ->
                        CompactActionItem(option.label) {
                            val eventReminders = reminders.filter { it.eventId == event.baseEventId() }
                            pendingShareEvent = null
                            when (option) {
                                ShareEventOption.Text -> shareEventText(context, event, use24HourFormat, palette)
                                ShareEventOption.Ics -> scope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        runCatching { createSingleEventIcsUri(context, event, eventReminders) }
                                    }
                                    result
                                        .onSuccess { uri -> shareEventIcs(context, event, uri, palette) }
                                        .onFailure { showDotCalToast(context, palette, "Could not share event") }
                                }
                            }
                        }
                    },
                    palette = palette,
                )
            }
        }
        pendingCopyToDateEvent?.let { event ->
            DateTimeChoiceSheet(
                title = "Copy to date",
                selectedDate = event.localDate(),
                selectedTime = event.startLocalTime(),
                minDate = null,
                includeTime = false,
                palette = palette,
                onDismiss = { pendingCopyToDateEvent = null },
                onSelected = { pickedDate, _ -> openDuplicateEditor(event, pickedDate) },
            )
        }
        AnimatedVisibility(
            visible = taskDetail != null,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            lastTaskDetail?.let { task ->
                TaskDetailScreen(
                    task = task,
                    reminder = reminders.firstOrNull { it.eventId == task.baseEventId() },
                    palette = palette,
                    isPrivate = task.baseEventId() in privateVaultIds,
                    onBack = { taskDetail = null },
                    onEdit = {
                        editingTask = task
                        taskTemplatePrefill = null
                        showTaskEditor = true
                    },
                    onTimeBlock = { blockFromTask(task) },
                    onMoveToPrivate = {
                        if (!isPro) {
                            showPaywall = true
                        } else {
                            viewModel.moveToPrivateVault(task) {
                                taskDetail = null
                                showDotCalToast(context, palette, "Moved to Private Vault")
                            }
                        }
                    },
                    onRestoreFromPrivate = {
                        viewModel.restoreFromPrivateVault(task.baseEventId()) {
                            showDotCalToast(context, palette, "Restored from Private Vault")
                        }
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
                isPro = isPro,
                onRequestPro = { showPaywall = true },
                templatePrefill = taskTemplatePrefill,
                onSaveTemplate = { template -> viewModel.saveTemplate(template) },
                onDismiss = {
                    editingTask = null
                    taskTemplatePrefill = null
                    showTaskEditor = false
                },
                onSave = { data ->
                    viewModel.saveTask(editingTask, data) {
                        editingTask = null
                        taskTemplatePrefill = null
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
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            EventEditorScreen(
                event = editingEvent,
                editorSessionKey = editorSessionKey,
                selectedDate = addEditorDateOverride ?: selectedDate,
                selectedTime = addStartTime,
                initialReminderMinutes = if (editingEvent == null) {
                    duplicateDraftPrefill?.reminderMinutes ?: defaultReminderMinutes
                } else {
                    reminders.firstOrNull { it.eventId == editingEvent?.baseEventId() }?.minutesBefore
                },
                defaultEventDurationMinutes = defaultEventDurationMinutes,
                accounts = assignableAccounts,
                lastSelectedAccountId = lastSelectedEventAccountId,
                palette = palette,
                isPro = isPro,
                conflictWarnings = conflictWarnings,
                use24HourFormat = use24HourFormat,
                onConflictRangeChanged = viewModel::refreshConflictWarnings,
                prefill = quickAddPrefill,
                draftPrefill = duplicateDraftPrefill,
                templatePrefill = templatePrefill,
                onSaveTemplate = { template -> viewModel.saveTemplate(template) },
                onRequestPro = { showPaywall = true },
                onDismiss = {
                    editingEvent = null
                    addEditorDateOverride = null
                    quickAddPrefill = null
                    duplicateDraftPrefill = null
                    templatePrefill = null
                    viewModel.clearConflictWarnings()
                    addSheet = false
                },
                onSave = { data, scope ->
                    val shouldReturnToDetail = detailEvent != null && editingEvent != null
                    val savedEventId = data.eventId ?: editingEvent?.baseEventId()
                    viewModel.saveEvent(editingEvent, data, scope) {
                        viewModel.selectDate(data.date)
                        editingEvent = null
                        addEditorDateOverride = null
                        quickAddPrefill = null
                        duplicateDraftPrefill = null
                        templatePrefill = null
                        viewModel.clearConflictWarnings()
                        addSheet = false
                        if (shouldReturnToDetail && savedEventId != null) {
                            viewModel.openEventDetailById(savedEventId)
                        }
                    }
                },
                onDelete = editingEvent?.let { eventToDelete ->
                    { scope ->
                        pendingDelete = PendingDelete(eventToDelete, scope, DeleteSource.Editor)
                    }
                },
            )
        }
        AnimatedVisibility(
            visible = showPaywall,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            PaywallScreen(
                viewModel = viewModel,
                palette = palette,
                onDismiss = { showPaywall = false },
            )
        }
        AnimatedVisibility(
            visible = showDateCalculator,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            DateCalculatorScreen(
                palette = palette,
                onBack = { showDateCalculator = false },
            )
        }
        AnimatedVisibility(
            visible = showQuickAdd,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            QuickAddScreen(
                palette = palette,
                onBack = { showQuickAdd = false },
                onContinue = { result -> openQuickAddResult(result) },
            )
        }
        AnimatedVisibility(
            visible = showTemplates,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            val templateItems by viewModel.templates.collectAsStateWithLifecycle()
            TemplatesScreen(
                palette = palette,
                templates = templateItems,
                onBack = { showTemplates = false },
                onUse = { template -> useTemplate(template) },
                onDelete = { id -> viewModel.deleteTemplate(id) },
            )
        }
        AnimatedVisibility(
            visible = showFocusProfiles,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            val focusProfileItems by viewModel.focusProfiles.collectAsStateWithLifecycle()
            FocusProfilesScreen(
                palette = palette,
                profiles = focusProfileItems,
                totalCalendars = accounts.size,
                onBack = { showFocusProfiles = false },
                onApply = { id ->
                    viewModel.applyFocusProfile(id) {
                        showDotCalToast(context, palette, "Calendar set applied")
                    }
                },
                onSaveCurrent = { name ->
                    val visibleIds = accounts.filter { it.isVisible == 1 }.map { it.id }.toSet()
                    viewModel.saveFocusProfile(
                        FocusProfile(
                            id = FocusProfile.newId(),
                            name = name,
                            accountIds = visibleIds,
                            createdAtMs = System.currentTimeMillis(),
                        ),
                    )
                    showDotCalToast(context, palette, "Calendar set saved")
                },
                onDelete = { id -> viewModel.deleteFocusProfile(id) },
            )
        }
        AnimatedVisibility(
            visible = showShiftPatterns,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            val shiftTypes by viewModel.shiftTypes.collectAsStateWithLifecycle()
            val shiftPatterns by viewModel.shiftPatterns.collectAsStateWithLifecycle()
            ShiftPatternsScreen(
                palette = palette,
                shiftTypes = shiftTypes,
                patterns = shiftPatterns,
                accounts = assignableAccounts,
                onBack = { showShiftPatterns = false },
                onSaveType = { type -> viewModel.saveShiftType(type) },
                onDeleteType = { id -> viewModel.deleteShiftType(id) },
                onSavePattern = { pattern -> viewModel.saveShiftPattern(pattern) },
                onDeletePattern = { id, removeGenerated ->
                    viewModel.deleteShiftPattern(id, removeGenerated) {
                        showDotCalToast(context, palette, "Shift pattern deleted")
                    }
                },
                onGenerate = { patternId, rangeStart, rangeEnd, accountId ->
                    viewModel.applyShiftPattern(patternId, rangeStart, rangeEnd, accountId) { result ->
                        val message = when {
                            result.generatedCount == 0 -> "No shifts added. Check that the pattern uses Day/Night shift types, not only Off."
                            result.replacedCount > 0 -> "${result.generatedCount} shifts added, ${result.replacedCount} replaced"
                            else -> "${result.generatedCount} shifts added"
                        }
                        showDotCalToast(context, palette, message, Toast.LENGTH_LONG)
                    }
                },
            )
        }
        if (showBulkTemplatePicker) {
            val templateItems by viewModel.templates.collectAsStateWithLifecycle()
            BulkTemplatePickerSheet(
                palette = palette,
                templates = templateItems.filterNot { it.isTask },
                onDismiss = { showBulkTemplatePicker = false },
                onTemplateSelected = { template ->
                    val dates = selectedBulkDates.toList()
                    viewModel.applyTemplateToDates(template.id, dates, template.accountId ?: assignableAccounts.firstOrNull()?.id) { count ->
                        showDotCalToast(context, palette, "$count events added")
                        selectedBulkDates = emptySet()
                        showBulkTemplatePicker = false
                    }
                },
            )
        }
        AnimatedVisibility(
            visible = showRecentlyDeleted,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            val deletedItems by viewModel.recentlyDeleted.collectAsStateWithLifecycle()
            RecentlyDeletedScreen(
                palette = palette,
                items = deletedItems,
                onBack = { showRecentlyDeleted = false },
                onRestore = { id -> viewModel.restoreDeleted(id) },
                onPurge = { id -> viewModel.purgeDeleted(id) },
                onEmptyAll = { viewModel.emptyRecentlyDeleted() },
            )
        }
        AnimatedVisibility(
            visible = showSearch,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.background).statusBarsPadding(),
        ) {
            val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
            val searchAccounts by viewModel.assignableAccounts.collectAsStateWithLifecycle()
            SearchScreen(
                palette = palette,
                results = searchResults,
                accounts = searchAccounts,
                onQueryChange = { viewModel.search(it) },
                onOpenEvent = { event ->
                    showSearch = false
                    viewModel.clearSearch()
                    viewModel.openEventDetail(event)
                },
                onOpenTask = { task ->
                    showSearch = false
                    viewModel.clearSearch()
                    taskDetail = task
                },
                onBack = {
                    showSearch = false
                    viewModel.clearSearch()
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
                            addEditorDateOverride = null
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
        if (isAppLocked) {
            AppLockScreen(
                palette = palette,
                canUseDeviceLock = keyguardManager.isKeyguardSecure,
                onUnlockWithPin = { pin, onResult ->
                    viewModel.verifyAppLockPin(pin) { ok ->
                        if (ok) appUnlocked = true
                        onResult(ok)
                    }
                },
                onDeviceUnlock = requestDeviceUnlock,
            )
        }
        if (updateAvailable) {
            UpdateAvailableDialog(
                palette = palette,
                onUpdate = {
                    updateAvailable = false
                    startFlexibleUpdate()
                },
                onDismiss = { updateAvailable = false },
            )
        }
        if (updateDownloaded) {
            UpdateReadyDialog(
                palette = palette,
                onRestart = {
                    updateDownloaded = false
                    appUpdateManager.completeUpdate()
                },
                onDismiss = { updateDownloaded = false },
            )
        }
        BackHandler(enabled = addSheet) {
            editingEvent = null
            addEditorDateOverride = null
            addSheet = false
        }
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

private enum class ShareEventOption(val label: String) {
    Text("Share as text"),
    Ics("Share as .ics"),
}

private fun shareEventText(
    context: Context,
    event: CalendarEvent,
    use24HourFormat: Boolean,
    palette: DotCalPalette,
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, event.title)
        putExtra(Intent.EXTRA_TEXT, event.shareText(use24HourFormat))
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Share Event"))
    }.onFailure {
        showDotCalToast(context, palette, "No share target found")
    }
}

private fun shareEventIcs(
    context: Context,
    event: CalendarEvent,
    uri: Uri,
    palette: DotCalPalette,
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/calendar"
        putExtra(Intent.EXTRA_SUBJECT, event.title)
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, "Share Event"))
    }.onFailure {
        showDotCalToast(context, palette, "No share target found")
    }
}

private fun createSingleEventIcsUri(
    context: Context,
    event: CalendarEvent,
    reminders: List<EventReminder>,
): Uri {
    val shareDir = File(context.cacheDir, "shared_events").apply { mkdirs() }
    val file = File(shareDir, "${event.title.safeShareFilename()}-${event.baseEventId().safeShareFilename()}.ics")
    val remindersById = mapOf(event.id to reminders, event.baseEventId() to reminders)
    file.writeText(IcsExporter.export(listOf(event), remindersById), Charsets.UTF_8)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun CalendarEvent.shareText(use24HourFormat: Boolean): String {
    val lines = mutableListOf(title)
    lines += "Date: ${shareDateTimeLine(use24HourFormat)}"
    if (location.isNotBlank()) lines += "Location: $location"
    if (description.isNotBlank()) lines += "Notes: $description"
    return lines.joinToString("\n")
}

private fun CalendarEvent.shareDateTimeLine(use24HourFormat: Boolean): String {
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault())
    val end = Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault())
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())
    if (isAllDay == 1) {
        val startDate = start.toLocalDate()
        val endDate = end.minusNanos(1).toLocalDate()
        return if (startDate == endDate) {
            "${startDate.format(dateFormatter)} (All-day)"
        } else {
            "${startDate.format(dateFormatter)} - ${endDate.format(dateFormatter)} (All-day)"
        }
    }
    val timeFormatter = DateTimeFormatter.ofPattern(if (use24HourFormat) "HH:mm" else "h:mm a", Locale.getDefault())
    val startText = "${start.toLocalDate().format(dateFormatter)} ${start.toLocalTime().format(timeFormatter)}"
    val endText = if (start.toLocalDate() == end.toLocalDate()) {
        end.toLocalTime().format(timeFormatter)
    } else {
        "${end.toLocalDate().format(dateFormatter)} ${end.toLocalTime().format(timeFormatter)}"
    }
    return "$startText - $endText"
}

private fun String.safeShareFilename(): String {
    val cleaned = lowercase(Locale.US)
        .replace(Regex("[^a-z0-9]+"), "-")
        .trim('-')
        .take(48)
    return cleaned.ifBlank { "event" }
}
