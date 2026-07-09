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
import androidx.core.view.WindowCompat
import com.dotfield.dotcal.BuildConfig
import com.dotfield.dotcal.data.CalendarAccount
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.DotCalRepository
import com.dotfield.dotcal.data.EventEditorData
import com.dotfield.dotcal.data.EventReminder
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
private val sheetDateFormatter = DateTimeFormatter.ofPattern("EEEE, dd MMM", Locale.US)
internal val detailDateFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.US)
private val agendaDateHeaderFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.US)
internal val dayHeaderFormatter = DateTimeFormatter.ofPattern("EEE dd MMM", Locale.US)
internal val compactDateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US)
internal val editorDateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM, yyyy", Locale.US)
internal val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
internal val editorTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
internal const val WEEK_HOUR_HEIGHT_DP = 64f
internal const val DAY_HOUR_HEIGHT_DP = 72f
internal const val TIMELINE_BOTTOM_CLEARANCE_DP = 104f
private const val BOOT_PREFS = "dotcal_boot"
private const val BOOT_THEME_KEY = "theme_mode"
private const val BOOT_ACCENT_KEY = "accent_color"
internal val reminderOptions = listOf(null, 5, 10, 30, 60, 1440)
internal val taskReminderOptions = listOf(null, 5, 10, 30, 1440)
internal data class RecurrenceOption(val label: String, val rrule: String?)
internal val recurrenceOptions = listOf(
    RecurrenceOption("None", null),
    RecurrenceOption("Daily", "FREQ=DAILY"),
    RecurrenceOption("Weekly", "FREQ=WEEKLY"),
    RecurrenceOption("Monthly", "FREQ=MONTHLY"),
    RecurrenceOption("Yearly", "FREQ=YEARLY"),
)

internal enum class SettingsScreen {
    Root,
    Theme,
    CalendarAccounts,
    AddAccount,
    GlobalHolidays,
    AppPrivacy,
    PrivacyPolicy,
}

internal enum class WeekStartOption(val storageKey: String, val label: String, val fixedDay: DayOfWeek?) {
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

/** Label for a Repeat row: "None", a preset name, or a custom rule's human sentence. */
internal fun repeatRowLabel(rrule: String?): String {
    if (rrule.isNullOrBlank()) return "None"
    recurrenceOptions.firstOrNull { it.rrule == rrule }?.let { return it.label }
    return RecurrenceRule.parse(rrule)?.humanLabel() ?: "None"
}
private val onboardingPages = listOf(
    OnboardingPage.Welcome,
    OnboardingPage.CalendarPermission,
    OnboardingPage.Notifications,
    OnboardingPage.Contacts,
    OnboardingPage.Ready,
)
internal enum class DateTimeField { Start, End }
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
            Toast.makeText(context, "Device lock unavailable", Toast.LENGTH_SHORT).show()
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
                    manual -> Toast.makeText(context, "DotCal is up to date", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                if (manual) Toast.makeText(context, "Couldn't check for updates", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, if (ok) "Calendar exported" else "Export failed", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Export failed", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Couldn't read file", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        viewModel.importIcs(text) { result ->
            result.onSuccess { summary ->
                Toast.makeText(
                    context,
                    "Imported: ${summary.inserted} new, ${summary.updated} updated",
                    Toast.LENGTH_LONG,
                ).show()
            }.onFailure {
                Toast.makeText(context, "Import failed - not a valid .ics file", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, if (ok) "Backup saved" else "Backup failed", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(context, "Backup failed", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Couldn't read file", Toast.LENGTH_SHORT).show()
            return@rememberLauncherForActivityResult
        }
        viewModel.importBackup(text) { result ->
            result.onSuccess { summary ->
                Toast.makeText(
                    context,
                    "Restored: ${summary.eventsInserted} new, ${summary.eventsUpdated} updated",
                    Toast.LENGTH_LONG,
                ).show()
            }.onFailure {
                Toast.makeText(context, "Restore failed - not a valid DotCal backup", Toast.LENGTH_SHORT).show()
            }
        }
    }
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
    LaunchedEffect(resolvedThemeMode, resolvedAccentColor, systemDark) {
        bootPreferences.edit()
            .putString(BOOT_THEME_KEY, resolvedThemeMode.name)
            .putString(BOOT_ACCENT_KEY, resolvedAccentColor.storageValue)
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
        scope.launch {
            context.calendarPreferencesDataStore.edit { preferences ->
                preferences[CalendarPreferences.KEY_DEFAULT_VIEW] = tab.name
            }
        }
    }
    fun openAddEditor(startTime: LocalTime = LocalTime.of(9, 0), date: LocalDate? = null) {
        editorSessionKey = UUID.randomUUID().toString()
        quickAddPrefill = null
        templatePrefill = null
        addStartTime = startTime
        addEditorDateOverride = date
        editingEvent = null
        addSheet = true
    }
    fun openQuickAddResult(result: QuickAddResult) {
        editorSessionKey = UUID.randomUUID().toString()
        quickAddPrefill = result
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
        templatePrefill = null
        addEditorDateOverride = null
        editingEvent = event
        addSheet = true
    }
    fun closeTopSurface() {
        when {
            showPaywall -> showPaywall = false
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
    val isAppLocked = appLockState.enabled && !appUnlocked && !showOnboarding && onboardingPreferenceLoaded
    BackHandler(enabled = isAppLocked) {}
    BackHandler(enabled = !showOnboarding && (showPaywall || showTemplates || showFocusProfiles || showSearch || showRecentlyDeleted || showDateCalculator || showQuickAdd || detailEvent != null || taskDetail != null || addSheet || showTaskEditor || screenTab == ScreenTab.Settings || screenTab == ScreenTab.Tasks)) {
        closeTopSurface()
    }

    // Group events by day once at the top level so the buckets survive Calendar <-> Tasks
    // <-> Settings switches and every calendar view reuses them instead of re-deriving.
    val eventsByDate = remember(events) { events.groupBy { it.localDate() } }
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
                syncEnabled = syncEnabled,
                syncIntervalMins = syncIntervalMins,
                syncMetadata = syncMetadata,
                isSyncing = isSyncing,
                birthdayEnabled = birthdayEnabled,
                defaultReminderMinutes = defaultReminderMinutes,
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
                        Toast.makeText(context, "You're already Pro!", Toast.LENGTH_SHORT).show()
                    } else {
                        showPaywall = true
                    }
                },
                onRestorePurchase = {
                    viewModel.restorePro { restored ->
                        Toast.makeText(
                            context,
                            if (restored) "Purchase restored - enjoy DotCal Pro!" else "No previous purchase found on this account",
                            Toast.LENGTH_SHORT,
                        ).show()
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
                        Toast.makeText(context, "App Lock disabled", Toast.LENGTH_SHORT).show()
                    }
                },
                onClearAppLockPin = {
                    viewModel.clearAppLockPin {
                        appUnlocked = false
                        Toast.makeText(context, "PIN removed", Toast.LENGTH_SHORT).show()
                    }
                },
                onRestorePrivateEvent = { eventId ->
                    viewModel.restoreFromPrivateVault(eventId) {
                        Toast.makeText(context, "Restored from Private Vault", Toast.LENGTH_SHORT).show()
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
                        viewModel.closeEventDetail()
                    },
                    onMoveToPrivate = {
                        if (!isPro) {
                            showPaywall = true
                        } else {
                            viewModel.moveToPrivateVault(event) {
                                viewModel.closeEventDetail()
                                Toast.makeText(context, "Moved to Private Vault", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onRestoreFromPrivate = {
                        viewModel.restoreFromPrivateVault(event.baseEventId()) {
                            Toast.makeText(context, "Restored from Private Vault", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onDelete = {
                        pendingDelete = PendingDelete(event, RecurringEditScope.WholeSeries, DeleteSource.Detail)
                    },
                )
            }
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
                                Toast.makeText(context, "Moved to Private Vault", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onRestoreFromPrivate = {
                        viewModel.restoreFromPrivateVault(task.baseEventId()) {
                            Toast.makeText(context, "Restored from Private Vault", Toast.LENGTH_SHORT).show()
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
                    defaultReminderMinutes
                } else {
                    reminders.firstOrNull { it.eventId == editingEvent?.baseEventId() }?.minutesBefore
                },
                accounts = assignableAccounts,
                lastSelectedAccountId = lastSelectedEventAccountId,
                palette = palette,
                isPro = isPro,
                prefill = quickAddPrefill,
                templatePrefill = templatePrefill,
                onSaveTemplate = { template -> viewModel.saveTemplate(template) },
                onRequestPro = { showPaywall = true },
                onDismiss = {
                    editingEvent = null
                    addEditorDateOverride = null
                    quickAddPrefill = null
                    templatePrefill = null
                    addSheet = false
                },
                onSave = { data, scope ->
                    viewModel.saveEvent(editingEvent, data, scope) {
                        viewModel.selectDate(data.date)
                        editingEvent = null
                        addEditorDateOverride = null
                        quickAddPrefill = null
                        templatePrefill = null
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
                        Toast.makeText(context, "Calendar set applied", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(context, "Calendar set saved", Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(context, "Shift pattern deleted", Toast.LENGTH_SHORT).show()
                    }
                },
                onGenerate = { patternId, rangeStart, rangeEnd, accountId ->
                    viewModel.applyShiftPattern(patternId, rangeStart, rangeEnd, accountId) { result ->
                        val message = when {
                            result.generatedCount == 0 -> "No shifts added. Check that the pattern uses Day/Night shift types, not only Off."
                            result.replacedCount > 0 -> "${result.generatedCount} shifts added, ${result.replacedCount} replaced"
                            else -> "${result.generatedCount} shifts added"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
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
                        Toast.makeText(context, "$count events added", Toast.LENGTH_SHORT).show()
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
internal fun ConfirmDeleteDialog(
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
internal fun ConfirmDeleteDialog(
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
internal fun TemplateNameDialog(
    defaultName: String,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    title: String = "Save as template",
) {
    var name by remember { mutableStateOf(defaultName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        titleContentColor = palette.primaryText,
        textContentColor = palette.secondaryText,
        title = { Text(title, fontFamily = mono) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Template name", fontFamily = mono, color = palette.secondaryText) },
                colors = dotCalTextFieldColors(palette),
                textStyle = TextStyle(color = palette.primaryText, fontFamily = mono),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
            ) {
                Text("Save", color = if (name.isNotBlank()) palette.accent else palette.disabledText)
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
private fun UpdateAvailableDialog(
    palette: DotCalPalette,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        titleContentColor = palette.primaryText,
        textContentColor = palette.secondaryText,
        title = { Text("Update available") },
        text = { Text("A new version of DotCal is available. Update to get the latest improvements.") },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text("Update", color = palette.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not now", color = palette.primaryText)
            }
        },
    )
}

@Composable
private fun UpdateReadyDialog(
    palette: DotCalPalette,
    onRestart: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        titleContentColor = palette.primaryText,
        textContentColor = palette.secondaryText,
        title = { Text("Update ready") },
        text = { Text("The update has been downloaded. Restart DotCal to apply it.") },
        confirmButton = {
            TextButton(onClick = onRestart) {
                Text("Restart", color = palette.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", color = palette.primaryText)
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
    val colors = remember(palette) { onboardingColors(palette) }
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
            Spacer(modifier = Modifier.height(if (compactHeight) 20.dp else 28.dp))
            OnboardingHero(
                page = page,
                colors = colors,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compactHeight) 120.dp else 132.dp),
            )
            Spacer(modifier = Modifier.height(if (compactHeight) 24.dp else 34.dp))
            Text(
                text = copy.label,
                color = colors.accent,
                fontFamily = mono,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = copy.title,
                color = colors.primaryText,
                fontFamily = mono,
                fontSize = if (compactHeight) 22.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = if (compactHeight) 26.sp else 28.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(if (compactHeight) 8.dp else 12.dp))
            Text(
                text = copy.description,
                color = colors.secondaryText,
                fontFamily = mono,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                maxLines = if (compactHeight) 3 else 4,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.weight(1f))
            OnboardingProgress(pageIndex = pageIndex, pageCount = pageCount, colors = colors)
            Spacer(modifier = Modifier.height(if (compactHeight) 14.dp else 20.dp))
            Button(
                onClick = onPrimary,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = colors.onAccent,
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().height(if (compactHeight) 48.dp else 52.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
            ) {
                Text(copy.primaryLabel, fontFamily = mono, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            if (page != OnboardingPage.Welcome && page != OnboardingPage.Ready) {
                TextButton(
                    onClick = onSecondary,
                    modifier = Modifier.fillMaxWidth().height(if (compactHeight) 44.dp else 48.dp),
                ) {
                    Text("Not Now", color = colors.secondaryText, fontFamily = mono, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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
    val onAccent: Color,
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

private fun onboardingColors(palette: DotCalPalette): OnboardingColors {
    return if (palette.isDark) {
        OnboardingColors(
            background = palette.background,
            surface = palette.dialogSurface,
            elevatedSurface = palette.eventCardSurface,
            primaryText = palette.primaryText,
            secondaryText = palette.secondaryText,
            mutedText = palette.dimText,
            accent = palette.accent,
            onAccent = palette.onAccent,
            glow = palette.accent.copy(alpha = 0.22f),
            shadow = Color.Black.copy(alpha = 0.55f),
            line = palette.line,
            isDark = true,
        )
    } else {
        OnboardingColors(
            background = palette.background,
            surface = palette.dialogSurface,
            elevatedSurface = palette.eventCardSurface,
            primaryText = palette.primaryText,
            secondaryText = palette.secondaryText,
            mutedText = palette.dimText,
            accent = palette.accent,
            onAccent = palette.onAccent,
            glow = palette.accent.copy(alpha = 0.13f),
            shadow = Color(0xFF111827).copy(alpha = 0.13f),
            line = palette.line,
            isDark = false,
        )
    }
}

@Composable
private fun OnboardingHero(page: OnboardingPage, colors: OnboardingColors, modifier: Modifier = Modifier) {
    Box(
        modifier = Modifier
            .then(modifier)
            .heightIn(min = 110.dp, max = 150.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            when (page) {
                OnboardingPage.Welcome -> drawFlatWelcomeHero(colors)
                OnboardingPage.CalendarPermission -> drawFlatCalendarAccessHero(colors)
                OnboardingPage.Notifications -> drawFlatReminderHero(colors)
                OnboardingPage.Contacts -> drawFlatBirthdayHero(colors)
                OnboardingPage.Ready -> drawFlatReadyHero(colors)
            }
        }
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

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            repeat(pageCount) { index ->
                val active = index == pageIndex
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .width(if (active) 16.dp else 5.dp)
                        .height(5.dp)
                        .background(if (active) colors.accent else colors.mutedText.copy(alpha = if (colors.isDark) 0.55f else 0.75f)),
                )
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlatWelcomeHero(colors: OnboardingColors) {
    val calendarWidth = size.minDimension * 0.441f
    val calendarHeight = calendarWidth * 0.86f
    val left = (size.width - calendarWidth) / 2f
    val top = size.height * 0.22f
    drawFlatCalendarPage(colors, Offset(left, top), calendarWidth, calendarHeight, dotGrid = true)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlatCalendarAccessHero(colors: OnboardingColors) {
    val side = size.minDimension * 0.441f
    val calendarHeight = side * 0.86f
    val left = (size.width - side) / 2f
    val top = size.height * 0.22f
    drawRect(
        color = colors.background,
        topLeft = Offset(left, top),
        size = androidx.compose.ui.geometry.Size(side, calendarHeight),
    )
    drawRect(colors.primaryText, Offset(left, top), androidx.compose.ui.geometry.Size(side, calendarHeight), style = Stroke(2.dp.toPx()))
    drawRect(colors.accent, Offset(left, top), androidx.compose.ui.geometry.Size(side, calendarHeight * 0.3f))
    drawCircle(colors.accent, 4.dp.toPx(), Offset(left + side * 0.5f, top + calendarHeight * 0.58f))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlatReminderHero(colors: OnboardingColors) {
    val c = Offset(size.width * 0.5f, size.height * 0.42f)
    drawCircle(colors.primaryText, size.minDimension * 0.168f, c, style = Stroke(2.dp.toPx()))
    drawRect(
        colors.accent,
        Offset(c.x - 5.dp.toPx(), c.y + size.minDimension * 0.15f),
        androidx.compose.ui.geometry.Size(10.dp.toPx(), 10.dp.toPx()),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlatBirthdayHero(colors: OnboardingColors) {
    val c = Offset(size.width * 0.5f, size.height * 0.3f)
    val radius = size.minDimension * 0.105f
    drawCircle(colors.primaryText, radius, c, style = Stroke(2.dp.toPx()))
    drawCircle(colors.accent, 4.5.dp.toPx(), Offset(c.x + radius * 1.45f, c.y - radius * 0.95f))
    drawArc(
        color = colors.primaryText,
        startAngle = 205f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(c.x - radius * 1.62f, c.y + radius * 1.72f),
        size = androidx.compose.ui.geometry.Size(radius * 3.24f, radius * 1.72f),
        style = Stroke(2.dp.toPx(), cap = StrokeCap.Round),
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlatReadyHero(colors: OnboardingColors) {
    val side = size.minDimension * 0.441f
    val left = (size.width - side) / 2f
    val top = size.height * 0.22f
    drawRect(colors.accent, Offset(left, top), androidx.compose.ui.geometry.Size(side, side), style = Stroke(2.dp.toPx()))
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(left + side * 0.24f, top + side * 0.52f)
        lineTo(left + side * 0.43f, top + side * 0.71f)
        lineTo(left + side * 0.78f, top + side * 0.31f)
    }
    drawPath(path, colors.accent, style = Stroke(3.dp.toPx(), cap = StrokeCap.Square, join = StrokeJoin.Miter))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlatCalendarPage(
    colors: OnboardingColors,
    topLeft: Offset,
    width: Float,
    height: Float,
    dotGrid: Boolean,
) {
    drawRect(colors.background, topLeft, androidx.compose.ui.geometry.Size(width, height))
    val borderColor = if (colors.isDark) Color(0xFF171717) else Color(0xFF101010)
    drawRect(borderColor, topLeft, androidx.compose.ui.geometry.Size(width, height), style = Stroke(1.5.dp.toPx()))
    drawRect(colors.accent, topLeft, androidx.compose.ui.geometry.Size(width, height * 0.3f))
    val pinWidth = 4.dp.toPx()
    val pinHeight = 14.dp.toPx()
    listOf(0.28f, 0.72f).forEach { xFactor ->
        drawRect(
            colors.primaryText,
            Offset(topLeft.x + width * xFactor - pinWidth / 2f, topLeft.y - pinHeight * 0.65f),
            androidx.compose.ui.geometry.Size(pinWidth, pinHeight),
        )
    }
    if (dotGrid) {
        val gapX = width * 0.26f
        val startX = topLeft.x + (width - gapX * 2f) / 2f
        val startY = topLeft.y + height * 0.52f
        val gapY = height * 0.24f
        repeat(2) { row ->
            repeat(3) { col ->
                val active = row == 1 && col == 1
                val inactiveColor = if (colors.isDark) {
                    Color(0xFF303030)
                } else {
                    Color(0xFF4A4A4A)
                }
                drawCircle(
                    if (active) colors.accent else inactiveColor,
                    if (active) 4.dp.toPx() else 3.dp.toPx(),
                    Offset(startX + col * gapX, startY + row * gapY),
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
                        EventRow(event = event, palette = palette, onClick = { onEdit(event) }, modifier = Modifier.animateItem())
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
internal fun EventRow(event: CalendarEvent, palette: DotCalPalette, onClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
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
internal fun EventCardChevron(tint: Color) {
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
    val agendaStartDate = LocalDate.now()
    val upcomingEvents = remember(events, agendaStartDate) {
        events
            .filter { it.isTask == 0 && !it.localDate().isBefore(agendaStartDate) }
            .sortedBy { it.startTimeMs }
    }
    val eventsByDate = remember(upcomingEvents) { upcomingEvents.groupBy { it.localDate() } }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.calendarSurface),
        contentPadding = PaddingValues(start = 13.dp, end = 13.dp, top = 0.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (upcomingEvents.isEmpty()) {
            item {
                AgendaEndOfDayState(
                    palette = palette,
                    modifier = Modifier.fillParentMaxHeight(0.82f),
                    onAdd = onAdd,
                )
            }
        } else {
            eventsByDate.forEach { (date, dateEvents) ->
                item(key = "agenda-header-$date") {
                    AgendaDateHeader(date = date, isFirst = date == agendaStartDate, palette = palette)
                }
                lazyItems(dateEvents, key = { it.id }) { event ->
                    AgendaEventCard(event = event, palette = palette, onClick = { onEventClick(event) }, modifier = Modifier.animateItem())
                }
            }
            item {
                AgendaEndOfDayState(
                    palette = palette,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
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
    modifier: Modifier = Modifier,
) {
    val cardBg = if (palette.isDark) palette.dialogSurface else palette.eventCardSurface
    val accentStrip = event.displayColor(palette)
    val cardShape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
            .drawBehind {
                drawRect(cardBg)
                drawRect(accentStrip, topLeft = Offset.Zero, size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height))
            }
            .border(1.dp, palette.eventCardBorder, cardShape)
            .noRippleClickable(onClick = onClick)
            .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                event.agendaTimeRange(),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
                letterSpacing = 0.2.sp,
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                event.title,
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (event.location.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(palette.secondaryText),
                    )
                    Spacer(modifier = Modifier.width(7.dp))
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
    }
}

@Composable
internal fun AgendaDateHeader(date: LocalDate, isFirst: Boolean, palette: DotCalPalette) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isFirst) 0.dp else 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            date.dayOfMonth.toString().padStart(2, '0'),
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            lineHeight = 26.sp,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                date.dayOfWeek.name.take(3),
                color = palette.accent,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
            Text(
                date.month.name.take(3),
                color = palette.secondaryText,
                fontFamily = mono,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(0.5.dp)
                .background(palette.line),
        )
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
            "You're all caught up",
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

private fun parseWeekStartOption(value: String?): WeekStartOption {
    return WeekStartOption.entries.firstOrNull { it.storageKey == value || it.name == value } ?: WeekStartOption.RegionDefault
}

private fun resolveWeekStartDay(option: WeekStartOption): DayOfWeek {
    return option.fixedDay ?: WeekFields.of(Locale.getDefault()).firstDayOfWeek
}

internal fun CalendarEvent.localDate(): LocalDate {
    return Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault()).toLocalDate()
}

internal fun CalendarEvent.hasTaskDate(): Boolean {
    return startTimeMs > 0L
}

internal fun CalendarEvent.taskDueDetailLabel(): String {
    val date = localDate().format(editorDateFormatter)
    return if (isAllDay == 1) date else "$date, ${startLocalTime().format(timeFormatter)}"
}

internal fun CalendarEvent.taskDueDateLine(): String {
    return Instant.ofEpochMilli(startTimeMs)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.US))
}

internal fun CalendarEvent.taskDueTimeLine(): String {
    return if (isAllDay == 1) "All-day" else startLocalTime().format(timeFormatter)
}

internal fun taskDateHeaderFormatter(): DateTimeFormatter {
    return DateTimeFormatter.ofPattern("EEE, dd MMM", Locale.US)
}

internal fun CalendarEvent.startLocalTime(): LocalTime {
    return Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
}

internal fun CalendarEvent.endLocalDateForEditor(): LocalDate {
    val endInstant = if (isAllDay == 1) endTimeMs - 1 else endTimeMs
    return Instant.ofEpochMilli(endInstant).atZone(ZoneId.systemDefault()).toLocalDate()
}

internal fun CalendarEvent.endLocalTime(): LocalTime {
    return Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault()).toLocalTime()
}

private fun parseEditorTime(value: String): LocalTime? {
    return runCatching { LocalTime.parse(value, timeFormatter) }.getOrNull()
}

internal fun LocalTime.toHour12(): Int {
    val h = hour % 12
    return if (h == 0) 12 else h
}

internal fun toHour24(hour12: Int, period: String): Int {
    return if (period.uppercase(Locale.US) == "PM") {
        if (hour12 == 12) 12 else hour12 + 12
    } else {
        if (hour12 == 12) 0 else hour12
    }
}

internal fun allDayReminderTimeLabel(time: LocalTime): String {
    return DateTimeFormatter.ofPattern("h:mm a", Locale.US).format(time).lowercase(Locale.US)
}

private fun parseStoredTime(value: String?): LocalTime? {
    if (value.isNullOrBlank()) return null
    return runCatching { LocalTime.parse(value) }.getOrNull()
}

internal fun coerceEndAfterStart(start: LocalTime, end: LocalTime): LocalTime {
    if (end.isAfter(start)) return end
    return when {
        start < LocalTime.of(22, 45) -> start.plusHours(1)
        start < LocalTime.of(23, 45) -> LocalTime.of(23, 45)
        else -> LocalTime.of(23, 59)
    }
}

internal fun reminderLabel(minutes: Int?): String {
    return when (minutes) {
        null -> "None"
        60 -> "1 hour before"
        1440 -> "1 day before"
        else -> "$minutes minutes before"
    }
}

internal fun RecurringEditScope.label(): String {
    return when (this) {
        RecurringEditScope.ThisEvent -> "This event"
        RecurringEditScope.WholeSeries -> "Whole series"
    }
}

internal fun dateTimeLabel(date: LocalDate, time: LocalTime): String {
    return "${date.format(editorDateFormatter)} ${time.format(editorTimeFormatter).lowercase(Locale.US)}"
}

internal fun syncIntervalLabel(minutes: Int): String {
    return when (minutes) {
        0 -> "Manual"
        60 -> "1 hour"
        120 -> "2 hours"
        else -> "$minutes min"
    }
}

internal fun calendarAccountsLabel(accounts: List<CalendarAccount>, hasCalendarPermission: Boolean): String {
    if (!hasCalendarPermission) return "Local only"
    val providerCount = accounts.count { it.id != "local-primary" }
    if (providerCount == 0) return "Connected"
    val selectedCount = accounts.count { it.id != "local-primary" && it.isVisible == 1 }
    return "$selectedCount/$providerCount selected"
}

internal fun selectedHolidayCountriesLabel(countries: List<HolidayCountryUiItem>): String {
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

internal fun List<SyncMetadata>.lastSyncedSubtitle(): String {
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

internal fun String.readableCalendarLabel(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) return "Calendar"
    if (trimmed.contains("@")) return trimmed
    if (trimmed.any { it.isLowerCase() }) return trimmed
    return trimmed.lowercase(Locale.US).replaceFirstChar { char ->
        if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString()
    }
}

internal fun CalendarAccount.secondaryCalendarLabel(): String {
    val raw = accountName.ifBlank { accountType }.trim()
    if (raw.isBlank()) return "Local"
    return raw.readableCalendarLabel()
}

internal fun nearestCircularIndex(currentIndex: Int, targetItemIndex: Int, itemCount: Int): Int {
    if (itemCount <= 0) return currentIndex
    val currentItemIndex = currentIndex % itemCount
    val forward = (targetItemIndex - currentItemIndex + itemCount) % itemCount
    val backward = forward - itemCount
    val delta = if (kotlin.math.abs(backward) < forward) backward else forward
    return currentIndex + delta
}

internal fun CalendarEvent.durationMinutes(): Int {
    return ((normalizedEndTimeMs() - startTimeMs) / 60_000L).toInt().coerceAtLeast(15).coerceAtMost(24 * 60)
}

internal fun CalendarEvent.normalizedEndTimeMs(): Long {
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
    return "${start.format(timeFormatter)} - ${end.format(timeFormatter)}"
}

private fun CalendarEvent.detailTimeRange(): String {
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault())
    val end = Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault())
    return "${start.format(detailDateFormatter).uppercase(Locale.US)} - ${start.toLocalTime().format(timeFormatter)} - ${end.toLocalTime().format(timeFormatter)}"
}

internal fun CalendarEvent.detailDateLine(): String {
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault())
    return start.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy", Locale.US))
}

internal fun CalendarEvent.detailTimeLine(): String {
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault())
    val end = Instant.ofEpochMilli(endTimeMs).atZone(ZoneId.systemDefault())
    return "${start.toLocalTime().format(timeFormatter)} - ${end.toLocalTime().format(timeFormatter)}"
}

internal fun CalendarEvent.recurrenceDetailLabel(): String? {
    val rule = RecurrenceRule.parse(rrule) ?: return null
    return "REPEATS / " + rule.humanLabel().uppercase()
}

internal fun EventReminder.detailLabel(): String {
    return when (minutesBefore) {
        1 -> "1 MINUTE BEFORE"
        60 -> "1 HOUR BEFORE"
        1440 -> "1 DAY BEFORE"
        else -> "$minutesBefore MINUTES BEFORE"
    }
}

internal fun String.toSentenceCase(): String {
    val lower = lowercase(Locale.US)
    return lower.replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(Locale.US) else char.toString() }
}

internal fun parseJsonStringArray(value: String): List<String> {
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

internal fun List<String>.toJsonStringArray(): String {
    return joinToString(prefix = "[", postfix = "]") { value ->
        "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""
    }
}

internal fun loadImageThumbnail(context: Context, uriValue: String): Bitmap? {
    val uri = runCatching { Uri.parse(uriValue) }.getOrNull() ?: return null
    return runCatching {
        context.contentResolver.loadThumbnail(uri, Size(180, 180), null)
    }.getOrNull()
}

internal fun loadImagePreview(context: Context, uriValue: String): Bitmap? {
    val uri = runCatching { Uri.parse(uriValue) }.getOrNull() ?: return null
    return runCatching {
        context.contentResolver.loadThumbnail(uri, Size(1280, 1280), null)
    }.getOrNull()
}

internal const val MAX_VOICE_NOTE_SECONDS = 300

internal fun voiceNoteFile(context: Context, eventId: String): File {
    val directory = File(context.filesDir, "voice_notes").apply { mkdirs() }
    return File(directory, "$eventId.m4a")
}

internal fun startVoiceRecording(context: Context, eventId: String): MediaRecorder? {
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

internal fun formatVoiceDuration(seconds: Int): String {
    val safeSeconds = seconds.coerceAtLeast(0)
    return "${safeSeconds / 60}:${(safeSeconds % 60).toString().padStart(2, '0')}"
}

internal fun parseColor(hex: String): Int {
    return try {
        android.graphics.Color.parseColor(hex)
    } catch (_: IllegalArgumentException) {
        android.graphics.Color.RED
    }
}

internal fun CalendarEvent.displayColor(palette: DotCalPalette): Color {
    return colorHex?.let { Color(parseColor(it)) } ?: palette.accent
}


internal fun android.content.Context.findActivity(): android.app.Activity? {
    var ctx: android.content.Context? = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is android.app.Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
