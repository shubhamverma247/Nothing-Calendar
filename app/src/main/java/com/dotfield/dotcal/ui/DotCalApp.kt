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
    RecurrenceOption("Yearly", "FREQ=YEARLY"),
)

/** Label for a Repeat row: "None", a preset name, or a custom rule's human sentence. */
private fun repeatRowLabel(rrule: String?): String {
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
                Toast.makeText(context, "Import failed — not a valid .ics file", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, "Restore failed — not a valid DotCal backup", Toast.LENGTH_SHORT).show()
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
            showTemplates -> showTemplates = false
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
    BackHandler(enabled = !showOnboarding && (showPaywall || showTemplates || showSearch || showRecentlyDeleted || showDateCalculator || showQuickAdd || detailEvent != null || taskDetail != null || addSheet || showTaskEditor || screenTab == ScreenTab.Settings || screenTab == ScreenTab.Tasks)) {
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
                                        onDateSelected = {
                                            viewModel.selectDate(it)
                                            showSheet = true
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
                            if (restored) "Purchase restored — enjoy DotCal Pro!" else "No previous purchase found on this account",
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
        // Floating bottom nav — rendered AFTER Settings overlay so it appears on top of Settings,
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
        // (Calendar/Tasks were fully transparent, Settings was solid black — now unified).
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
private fun TemplateNameDialog(
    defaultName: String,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(defaultName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        titleContentColor = palette.primaryText,
        textContentColor = palette.secondaryText,
        title = { Text("Save as template", fontFamily = mono) },
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
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !palette.isDark
        controller.isAppearanceLightNavigationBars = !palette.isDark
    }
}

@Composable
private fun CalendarTabContainer(
    title: String,
    activeCalendarTab: CalendarTab,
    palette: DotCalPalette,
    onTitleClick: () -> Unit,
    onAdd: () -> Unit,
    onTemplates: (() -> Unit)? = null,
    onQuickAdd: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    onCalendarTabSelected: (CalendarTab) -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.topBarSurface),
        ) {
            CalendarActionBar(
                title = title,
                palette = palette,
                onTitleClick = onTitleClick,
                onAdd = onAdd,
                onTemplates = onTemplates,
                onQuickAdd = onQuickAdd,
                onSearch = onSearch,
            )
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(palette.topBarSurface),
        )
        CalendarViewSegmentedControl(
            selected = activeCalendarTab,
            palette = palette,
            onSelected = onCalendarTabSelected,
        )
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(palette.topBarSurface),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(palette.background),
        ) {
            content()
        }
    }
}

@Composable
private fun CalendarActionBar(
    title: String,
    palette: DotCalPalette,
    onTitleClick: () -> Unit,
    onAdd: () -> Unit,
    onTemplates: (() -> Unit)? = null,
    onQuickAdd: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
) {
    val topIconTint = if (palette.isDark) NWhite else palette.accent
    var showOverflow by remember { mutableStateOf(false) }
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
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = if (title.length <= 4) 30.sp else 28.sp,
            modifier = Modifier.padding(start = 8.dp).clickable(onClick = onTitleClick),
            maxLines = 1,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onSearch != null) {
                IconButton(
                    onClick = onSearch,
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = topIconTint)
                }
            }
            IconButton(
                onClick = onAdd,
                modifier = Modifier.size(44.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add event", tint = topIconTint)
            }
            // Quick Add and Templates live together in the overflow menu.
            if (onQuickAdd != null || onTemplates != null) {
                Box {
                    IconButton(
                        onClick = { showOverflow = true },
                        modifier = Modifier.size(44.dp),
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = topIconTint)
                    }
                    DropdownMenu(
                        expanded = showOverflow,
                        onDismissRequest = { showOverflow = false },
                        containerColor = palette.dialogSurface,
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 0.dp,
                        modifier = Modifier.width(216.dp),
                    ) {
                        if (onQuickAdd != null) {
                            ActionBarMenuItem(
                                label = "Quick Add",
                                subtitle = "Type it, we schedule it",
                                icon = Icons.Default.AutoAwesome,
                                palette = palette,
                                onClick = {
                                    showOverflow = false
                                    onQuickAdd()
                                },
                            )
                        }
                        if (onTemplates != null) {
                            ActionBarMenuItem(
                                label = "Templates",
                                subtitle = "Reuse saved events & tasks",
                                icon = Icons.Default.Description,
                                palette = palette,
                                onClick = {
                                    showOverflow = false
                                    onTemplates()
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionBarMenuItem(
    label: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(palette.accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
            }
        },
        text = {
            Column {
                Text(label, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Medium, fontSize = 15.sp)
                Text(subtitle, color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp)
            }
        },
    )
}

@Composable
private fun DotCalBottomNav(
    selected: ScreenTab,
    palette: DotCalPalette,
    onCalendar: () -> Unit,
    onTasks: () -> Unit,
    onSettings: () -> Unit,
) {
    val active = palette.accent
    val inactive = palette.secondaryText
    val pillColor = if (palette.isDark) Color(0xFF1A1A1A) else Color(0xFFFFFFFF)
    val borderColor = palette.disabledText.copy(alpha = if (palette.isDark) 0.22f else 0.16f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        val pillShape = RoundedCornerShape(34.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = pillShape,
                    clip = false,
                    ambientColor = if (palette.isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.12f),
                    spotColor = if (palette.isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.10f),
                )
                .clip(pillShape)
                .background(pillColor)
                .border(width = 0.5.dp, color = borderColor, shape = pillShape)
                .noRippleClickable {}
                .padding(horizontal = 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(80.dp, Alignment.CenterHorizontally),
        ) {
            BottomNavItem(
                selected = selected == ScreenTab.Calendar,
                activeColor = active,
                inactiveColor = inactive,
                icon = { tint -> BottomCalendarIcon(tint) },
                onClick = onCalendar,
            )
            BottomNavItem(
                selected = selected == ScreenTab.Tasks,
                activeColor = active,
                inactiveColor = inactive,
                icon = { tint -> BottomTaskIcon(tint) },
                onClick = onTasks,
            )
            BottomNavItem(
                selected = selected == ScreenTab.Settings,
                activeColor = active,
                inactiveColor = inactive,
                icon = { tint -> BottomSettingsIcon(tint) },
                onClick = onSettings,
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit,
) {
    val tint by animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = tween(200),
        label = "navTint",
    )
    val selectedFill by animateColorAsState(
        targetValue = Color.Transparent,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "navSelectedFill",
    )
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(selectedFill)
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        icon(tint)
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
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) segmentSelected else Color.Transparent)
                    .noRippleClickable { onSelected(tab) },
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

@Composable
private fun BottomCalendarIcon(tint: Color) {
    Canvas(modifier = Modifier.size(26.dp)) {
        val stroke = Stroke(width = 1.85.dp.toPx())
        val left = 4.5.dp.toPx()
        val top = 5.5.dp.toPx()
        val right = size.width - 4.5.dp.toPx()
        val bottom = size.height - 3.5.dp.toPx()
        drawRoundRect(
            color = tint,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(right - left, bottom - top),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = stroke,
        )
        drawLine(tint, Offset(left, 10.5.dp.toPx()), Offset(right, 10.5.dp.toPx()), strokeWidth = 1.85.dp.toPx())
        drawLine(tint, Offset(9.dp.toPx(), 2.5.dp.toPx()), Offset(9.dp.toPx(), 7.dp.toPx()), strokeWidth = 1.85.dp.toPx())
        drawLine(tint, Offset(17.dp.toPx(), 2.5.dp.toPx()), Offset(17.dp.toPx(), 7.dp.toPx()), strokeWidth = 1.85.dp.toPx())
        drawCircle(tint, radius = 1.2.dp.toPx(), center = Offset(9.5.dp.toPx(), 15.dp.toPx()))
        drawCircle(tint, radius = 1.2.dp.toPx(), center = Offset(13.dp.toPx(), 15.dp.toPx()))
        drawCircle(tint, radius = 1.2.dp.toPx(), center = Offset(16.5.dp.toPx(), 15.dp.toPx()))
    }
}

@Composable
private fun BottomTaskIcon(tint: Color) {
    Canvas(modifier = Modifier.size(28.dp)) {
        val stroke = Stroke(width = 1.8.dp.toPx())
        drawRoundRect(
            color = tint,
            topLeft = Offset(5.dp.toPx(), 5.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(18.dp.toPx(), 18.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.5.dp.toPx(), 3.5.dp.toPx()),
            style = stroke,
        )
        drawLine(tint, Offset(9.dp.toPx(), 11.dp.toPx()), Offset(11.dp.toPx(), 13.dp.toPx()), strokeWidth = 1.8.dp.toPx())
        drawLine(tint, Offset(11.dp.toPx(), 13.dp.toPx()), Offset(14.5.dp.toPx(), 9.dp.toPx()), strokeWidth = 1.8.dp.toPx())
        drawLine(tint, Offset(16.dp.toPx(), 11.dp.toPx()), Offset(20.dp.toPx(), 11.dp.toPx()), strokeWidth = 1.8.dp.toPx())
        drawLine(tint, Offset(9.dp.toPx(), 18.dp.toPx()), Offset(20.dp.toPx(), 18.dp.toPx()), strokeWidth = 1.8.dp.toPx())
    }
}

@Composable
private fun BottomSettingsIcon(tint: Color) {
    Icon(Icons.Filled.SettingsGearIcon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
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
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    palette: DotCalPalette,
    weekStart: DayOfWeek,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onJumpToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
    val days = remember(month, weekStart) { monthGrid(month, weekStart) }
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
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
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
    val weekEvents = remember(eventsByDate, days) { days.flatMap { eventsByDate[it].orEmpty() } }
    val timedEvents = remember(weekEvents) { weekEvents.filter { it.isAllDay == 0 } }
    val allDayEvents = remember(weekEvents) { weekEvents.filter { it.isAllDay == 1 } }
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
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    palette: DotCalPalette,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onJumpToday: () -> Unit,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val dayAll = remember(eventsByDate, selectedDate) { eventsByDate[selectedDate].orEmpty() }
    val dayEvents = remember(dayAll) { dayAll.filter { it.isTask == 0 } }
    val allDayEvents = remember(dayEvents) { dayEvents.filter { it.isAllDay == 1 } }
    val timedEvents = remember(dayEvents) { dayEvents.filter { it.isAllDay == 0 } }
    val timedEventsByHour = remember(timedEvents) { timedEvents.groupBy { it.startLocalTime().hour } }
    val tasks = remember(dayAll) { dayAll.filter { it.isTask == 1 } }

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
private fun EventDetailScreen(
    event: CalendarEvent,
    reminders: List<EventReminder>,
    account: CalendarAccount?,
    palette: DotCalPalette,
    isPrivate: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onMoveToPrivate: () -> Unit,
    onRestoreFromPrivate: () -> Unit,
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
                            if (isPrivate) "Restore From Private Vault" else "Move to Private Vault",
                            color = palette.primaryText,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = if (isPrivate) onRestoreFromPrivate else onMoveToPrivate)
                                .padding(vertical = 12.dp),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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
    isPro: Boolean,
    onAddImage: () -> Unit,
    onRemoveImage: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Images", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                if (!isPro) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pro feature", color = palette.accent, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 11.sp)
                }
            }
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
    isPro: Boolean,
    onRequestPro: () -> Unit,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Voice note", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            if (!isPro) {
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pro feature", color = palette.accent, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 11.sp)
            }
        }
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
            else -> EmptyVoiceNoteRow(
                palette = palette,
                permissionDenied = permissionDenied,
                onRecord = {
                    if (!isPro) {
                        onRequestPro()
                        return@EmptyVoiceNoteRow
                    }
                    if (permissionDenied) {
                        context.startActivity(
                            Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            },
                        )
                    } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
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
private fun EmptyVoiceNoteRow(palette: DotCalPalette, onRecord: () -> Unit, permissionDenied: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, palette.line, RoundedCornerShape(12.dp))
            .clickable(onClick = onRecord)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MicGlyph(tint = if (permissionDenied) palette.secondaryText else palette.primaryText)
        Text(
            if (permissionDenied) "MIC PERMISSION DENIED — TAP TO ENABLE" else "TAP TO RECORD",
            color = palette.secondaryText,
            fontFamily = mono,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 12.dp),
        )
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
    accounts: List<CalendarAccount>,
    lastSelectedAccountId: String?,
    palette: DotCalPalette,
    isPro: Boolean,
    onRequestPro: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (EventEditorData, RecurringEditScope) -> Unit,
    onDelete: ((RecurringEditScope) -> Unit)?,
    prefill: QuickAddResult? = null,
    templatePrefill: EventTemplate? = null,
    onSaveTemplate: ((EventTemplate) -> Unit)? = null,
) {
    // Quick-add prefill only seeds a brand-new event, never an existing one being edited.
    val seed = if (event == null) prefill else null
    // Template prefill likewise only seeds a brand-new event. Applied to the current date.
    val tpl = if (event == null) templatePrefill else null
    val tplStartTime: LocalTime? = tpl?.startMinuteOfDay?.let { LocalTime.of(it / 60, it % 60) }
    val editorDate = event?.localDate() ?: seed?.date ?: selectedDate
    val initialStart = event?.startLocalTime() ?: seed?.startTime ?: tplStartTime ?: selectedTime
    val tplEndTime: LocalTime? = if (tpl != null && tplStartTime != null) {
        tplStartTime.plusMinutes(tpl.durationMinutes.toLong())
    } else null
    val tplEndDate: LocalDate? = if (tpl != null && tpl.startMinuteOfDay != null) {
        editorDate.plusDays(((tpl.startMinuteOfDay + tpl.durationMinutes) / (24 * 60)).toLong())
    } else null
    val initialEnd = event?.endLocalTime() ?: seed?.endTime ?: tplEndTime ?: initialStart.plusHours(1)
    val initialEndDate = event?.endLocalDateForEditor() ?: seed?.endDate ?: tplEndDate ?: editorDate
    val editorStateKey = event?.id ?: editorSessionKey
    val draftEventId = remember(editorStateKey) {
        if (event == null || event.isRecurrenceOccurrence()) UUID.randomUUID().toString() else event.baseEventId()
    }
    var title by remember(editorStateKey) { mutableStateOf(event?.title ?: seed?.title ?: tpl?.title ?: "") }
    var description by remember(editorStateKey) { mutableStateOf(event?.description ?: tpl?.description ?: "") }
    var location by remember(editorStateKey) { mutableStateOf(event?.location ?: tpl?.location ?: "") }
    var startDate by remember(editorStateKey) { mutableStateOf(editorDate) }
    var endDate by remember(editorStateKey) { mutableStateOf(maxOf(editorDate, initialEndDate)) }
    var startTime by remember(editorStateKey) { mutableStateOf(initialStart) }
    var endTime by remember(editorStateKey) { mutableStateOf(coerceEndAfterStart(initialStart, initialEnd)) }
    var allDay by remember(editorStateKey) { mutableStateOf(event?.let { it.isAllDay == 1 } ?: seed?.isAllDay ?: tpl?.isAllDay ?: false) }
    var reminderMinutes by remember(editorStateKey, initialReminderMinutes) { mutableStateOf(if (tpl != null) tpl.reminderMinutes else initialReminderMinutes) }
    var recurrenceRule by remember(editorStateKey) { mutableStateOf(event?.rrule ?: seed?.rrule ?: tpl?.rrule) }
    var imageUris by remember(editorStateKey) { mutableStateOf(parseJsonStringArray(event?.imageUris ?: "[]")) }
    var voiceNotePath by remember(editorStateKey) { mutableStateOf(event?.voiceNotePath) }
    val writableAccounts = accounts
    var selectedAccountId by remember(editorStateKey, writableAccounts, lastSelectedAccountId) {
        mutableStateOf(event?.accountId?.takeIf { id -> writableAccounts.any { it.id == id } }
            ?: tpl?.accountId?.takeIf { id -> writableAccounts.any { it.id == id } }
            ?: lastSelectedAccountId?.takeIf { id -> event == null && writableAccounts.any { it.id == id } }
            ?: writableAccounts.firstOrNull { it.isPrimary == 1 }?.id
            ?: writableAccounts.firstOrNull()?.id
            ?: DotCalRepository.LOCAL_ACCOUNT_ID)
    }
    var showCalendarPicker by remember { mutableStateOf(false) }
    var dateTimePicker by remember { mutableStateOf<DateTimeField?>(null) }
    var showReminderPicker by remember { mutableStateOf(false) }
    var showRepeatPicker by remember { mutableStateOf(false) }
    var showApplyScopePicker by remember { mutableStateOf(false) }
    var pendingPermissionSave by remember { mutableStateOf<Pair<EventEditorData, RecurringEditScope>?>(null) }
    var submitted by remember { mutableStateOf(false) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    var showEditorMenu by remember { mutableStateOf(false) }
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
            accountId = selectedAccountId,
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
    fun buildTemplate(name: String): EventTemplate {
        val startMinute = if (allDay) null else startTime.hour * 60 + startTime.minute
        val durationMinutes = if (allDay) {
            0
        } else {
            java.time.temporal.ChronoUnit.MINUTES.between(startDate.atTime(startTime), endDate.atTime(endTime))
                .coerceIn(0L, (7L * 24 * 60)).toInt()
        }
        return EventTemplate(
            id = EventTemplate.newId(),
            name = name.trim().ifBlank { title.trim().ifBlank { "Template" } },
            isTask = false,
            title = title.trim(),
            description = description.trim(),
            location = location.trim(),
            accountId = selectedAccountId,
            isAllDay = allDay,
            startMinuteOfDay = startMinute,
            durationMinutes = durationMinutes,
            reminderMinutes = reminderMinutes,
            rrule = recurrenceRule,
            createdAtMs = System.currentTimeMillis(),
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
            // "Save as template" lives in an overflow menu, and only when editing an
            // existing event (the Add screen stays clean per design).
            if (event != null && onSaveTemplate != null) {
                Box {
                    IconButton(onClick = { showEditorMenu = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = palette.primaryText)
                    }
                    DropdownMenu(
                        expanded = showEditorMenu,
                        onDismissRequest = { showEditorMenu = false },
                        containerColor = palette.dialogSurface,
                    ) {
                        DropdownMenuItem(
                            text = { Text("Save as template", color = palette.primaryText, fontFamily = mono, fontSize = 15.sp) },
                            onClick = {
                                showEditorMenu = false
                                clearEditorFocus()
                                if (!isPro) onRequestPro() else showSaveTemplateDialog = true
                            },
                        )
                    }
                }
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
            CalendarFieldPill(
                account = writableAccounts.firstOrNull { it.id == selectedAccountId },
                palette = palette,
                enabled = writableAccounts.size > 1,
                onClick = {
                    clearEditorFocus()
                    showCalendarPicker = true
                },
            )
            Spacer(modifier = Modifier.height(10.dp))
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
                isPro = isPro,
                onAddImage = {
                    clearEditorFocus()
                    if (!isPro) {
                        onRequestPro()
                        return@ImageAttachmentSection
                    }
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
                isPro = isPro,
                onRequestPro = onRequestPro,
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
                value = if (recurringEditScope == RecurringEditScope.ThisEvent) "None" else repeatRowLabel(recurrenceRule),
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
                        .padding(top = 20.dp)
                        .clickable { onDelete(recurringEditScope) },
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
    if (showSaveTemplateDialog && onSaveTemplate != null) {
        TemplateNameDialog(
            defaultName = title.trim(),
            palette = palette,
            onDismiss = { showSaveTemplateDialog = false },
            onConfirm = { name ->
                onSaveTemplate(buildTemplate(name))
                showSaveTemplateDialog = false
                Toast.makeText(context, "Template saved", Toast.LENGTH_SHORT).show()
            },
        )
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
            anchorDate = startDate,
            isPro = isPro,
            palette = palette,
            onDismiss = { showRepeatPicker = false },
            onRequestPro = onRequestPro,
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
    if (showCalendarPicker) {
        CalendarChoiceDialog(
            accounts = writableAccounts,
            selectedAccountId = selectedAccountId,
            palette = palette,
            onDismiss = { showCalendarPicker = false },
            onSelected = {
                selectedAccountId = it
                showCalendarPicker = false
            },
        )
    }
}

@Composable
private fun CalendarFieldPill(
    account: CalendarAccount?,
    palette: DotCalPalette,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val swatch = account?.color?.let { Color(parseColor(it)) } ?: palette.accent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.dialogSurface)
            .drawBehind {
                drawRoundRect(
                    color = palette.line.copy(alpha = 0.55f),
                    cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            .noRippleClickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(swatch),
        )
        Text(
            "Calendar",
            color = palette.secondaryText,
            fontFamily = mono,
            fontSize = 13.sp,
            modifier = Modifier.padding(start = 10.dp),
        )
        Text(
            account?.displayName?.readableCalendarLabel() ?: "Personal",
            color = palette.primaryText,
            fontFamily = mono,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 12.dp),
        )
        if (enabled) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun CalendarChoiceDialog(
    accounts: List<CalendarAccount>,
    selectedAccountId: String,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        title = {
            Text("Choose calendar", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold)
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                lazyItems(accounts, key = { it.id }) { account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelected(account.id) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color(parseColor(account.color))),
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(
                                account.displayName.readableCalendarLabel(),
                                color = palette.primaryText,
                                fontFamily = mono,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp,
                            )
                            CalendarAccountSubtitle(account = account, palette = palette)
                        }
                        if (account.id == selectedAccountId) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = palette.accent, modifier = Modifier.size(20.dp))
                        }
                    }
                    HorizontalDivider(color = palette.line.copy(alpha = 0.45f), thickness = 1.dp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.primaryText, fontFamily = mono)
            }
        },
    )
}

@Composable
private fun CalendarAccountSubtitle(account: CalendarAccount, palette: DotCalPalette) {
    val subtitle = account.cleanPickerSubtitle()
    if (subtitle.isBlank()) return
    Text(
        subtitle,
        color = palette.secondaryText,
        fontFamily = mono,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

private fun CalendarAccount.cleanPickerSubtitle(): String {
    return when {
        id == DotCalRepository.LOCAL_ACCOUNT_ID || accountType.equals("LOCAL", ignoreCase = true) -> "On this device"
        accountType.equals("GOOGLE", ignoreCase = true) -> "Google calendar"
        accountType.equals("DEVICE", ignoreCase = true) -> "Device calendar"
        accountType.isNotBlank() -> accountType.readableCalendarLabel()
        else -> ""
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
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
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
        listState.scrollToItem(selectedIndex)
    }
    LaunchedEffect(listState.isScrollInProgress, centeredIndex) {
        if (!listState.isScrollInProgress && items.isNotEmpty()) {
            val targetIndex = centeredIndex.coerceIn(0, (virtualCount - 1).coerceAtLeast(0))
            val targetItemIndex = if (circular) targetIndex % items.size else targetIndex
            listState.animateScrollToItem(targetIndex)
            if (items[targetItemIndex] != selected) onSelected(items[targetItemIndex])
        }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.height(rowHeight * 3),
        contentPadding = PaddingValues(vertical = rowHeight),
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
                            listState.animateScrollToItem(targetIndex)
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
private fun RepeatChoiceSheet(
    selected: String?,
    anchorDate: LocalDate,
    isPro: Boolean,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onRequestPro: () -> Unit,
    onSelected: (String?) -> Unit,
) {
    val isCustomSelected = !selected.isNullOrBlank() && recurrenceOptions.none { it.rrule == selected }
    val selectedRule = remember(selected) { RecurrenceRule.parse(selected) }
    var showBuilder by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        contentColor = palette.primaryText,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
        Column(modifier = Modifier.fillMaxWidth().background(palette.dialogSurface).padding(horizontal = 20.dp).padding(bottom = 16.dp)) {
            Spacer(modifier = Modifier.height(12.dp))
            Text("Repeat", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(4.dp))
            recurrenceOptions.forEach { option ->
                RepeatOptionRow(
                    label = option.label,
                    selected = !isCustomSelected && option.rrule == selected,
                    palette = palette,
                    onClick = {
                        onSelected(option.rrule)
                        onDismiss()
                    },
                )
                HorizontalDivider(color = palette.line.copy(alpha = 0.45f), thickness = 1.dp)
            }
            RepeatOptionRow(
                label = if (isCustomSelected) (selectedRule?.humanLabel() ?: "Custom…") else "Custom…",
                selected = isCustomSelected,
                palette = palette,
                locked = !isPro,
                onClick = {
                    if (!isPro) {
                        onDismiss()
                        onRequestPro()
                    } else {
                        showBuilder = true
                    }
                },
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showBuilder) {
        CustomRecurrenceSheet(
            initial = selectedRule,
            anchorDate = anchorDate,
            palette = palette,
            onDismiss = { showBuilder = false },
            onConfirm = { rrule ->
                showBuilder = false
                onSelected(rrule)
                onDismiss()
            },
        )
    }
}

@Composable
private fun RepeatOptionRow(
    label: String,
    selected: Boolean,
    palette: DotCalPalette,
    locked: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = palette.primaryText, fontFamily = mono, fontSize = 15.sp, modifier = Modifier.weight(1f))
        when {
            locked -> Icon(Icons.Default.Lock, contentDescription = "Pro feature", tint = palette.secondaryText, modifier = Modifier.size(16.dp))
            selected -> Icon(Icons.Default.Check, contentDescription = null, tint = palette.primaryText, modifier = Modifier.size(18.dp))
        }
    }
}

private enum class RecurrenceEndMode { Never, OnDate, AfterCount }

/** Pro custom recurrence builder: frequency, interval, weekday/monthly-mode, and an end condition. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomRecurrenceSheet(
    initial: RecurrenceRule?,
    anchorDate: LocalDate,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val freqOrder = listOf(RecurrenceFreq.DAILY, RecurrenceFreq.WEEKLY, RecurrenceFreq.MONTHLY, RecurrenceFreq.YEARLY)
    var freq by remember { mutableStateOf(initial?.freq ?: RecurrenceFreq.WEEKLY) }
    var interval by remember { mutableStateOf((initial?.interval ?: 1).coerceAtLeast(1)) }
    var weekdays by remember {
        mutableStateOf(
            initial?.byDay?.map { it.day }?.toSet()?.takeIf { it.isNotEmpty() } ?: setOf(anchorDate.dayOfWeek)
        )
    }
    // Monthly: false = on day-of-month, true = on the nth weekday.
    var monthlyByWeekday by remember { mutableStateOf(initial?.freq == RecurrenceFreq.MONTHLY && initial.byDay.isNotEmpty()) }
    var endMode by remember {
        mutableStateOf(
            when {
                initial?.count != null -> RecurrenceEndMode.AfterCount
                initial?.until != null -> RecurrenceEndMode.OnDate
                else -> RecurrenceEndMode.Never
            }
        )
    }
    var untilDate by remember { mutableStateOf(initial?.until ?: anchorDate.plusMonths(3)) }
    var countN by remember { mutableStateOf(initial?.count ?: 10) }
    var showUntilPicker by remember { mutableStateOf(false) }

    val anchorNthByDay = remember(anchorDate) { anchorDate.nthWeekdayByDay() }

    fun buildRule(): RecurrenceRule = RecurrenceRule(
        freq = freq,
        interval = interval.coerceAtLeast(1),
        byDay = when (freq) {
            RecurrenceFreq.WEEKLY -> {
                val days = weekdays.ifEmpty { setOf(anchorDate.dayOfWeek) }
                if (days.size == 1 && days.first() == anchorDate.dayOfWeek) emptyList()
                else days.sortedBy { it.value }.map { ByDay(null, it) }
            }
            RecurrenceFreq.MONTHLY -> if (monthlyByWeekday) listOf(anchorNthByDay) else emptyList()
            else -> emptyList()
        },
        count = if (endMode == RecurrenceEndMode.AfterCount) countN.coerceAtLeast(1) else null,
        until = if (endMode == RecurrenceEndMode.OnDate) untilDate else null,
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.dialogSurface,
        contentColor = palette.primaryText,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .background(palette.dialogSurface),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 96.dp),
            ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Custom Repeat", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(buildRule().humanLabel(), color = palette.accent, fontFamily = mono, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(18.dp))
            CalcSectionLabelSafe("FREQUENCY", palette)
            Spacer(modifier = Modifier.height(8.dp))
            TwoOptionSegmentedControl(
                options = listOf("Day", "Week", "Month", "Year"),
                selectedIndex = freqOrder.indexOf(freq).coerceAtLeast(0),
                palette = palette,
                onSelected = { freq = freqOrder[it] },
            )

            Spacer(modifier = Modifier.height(18.dp))
            CalcSectionLabelSafe("EVERY", palette)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CalcStepperButton("−", palette) { interval = (interval - 1).coerceAtLeast(1) }
                Text(
                    "$interval " + freqUnitLabel(freq, interval),
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                )
                CalcStepperButton("+", palette) { interval = (interval + 1).coerceAtMost(999) }
            }

            if (freq == RecurrenceFreq.WEEKLY) {
                Spacer(modifier = Modifier.height(18.dp))
                CalcSectionLabelSafe("ON DAYS", palette)
                Spacer(modifier = Modifier.height(8.dp))
                WeekdayPickerRow(
                    selected = weekdays,
                    palette = palette,
                    onToggle = { day ->
                        weekdays = if (day in weekdays) (weekdays - day) else (weekdays + day)
                    },
                )
            }

            if (freq == RecurrenceFreq.MONTHLY) {
                Spacer(modifier = Modifier.height(18.dp))
                CalcSectionLabelSafe("ON", palette)
                Spacer(modifier = Modifier.height(8.dp))
                RepeatOptionRow(
                    label = "Day ${anchorDate.dayOfMonth} of the month",
                    selected = !monthlyByWeekday,
                    palette = palette,
                    onClick = { monthlyByWeekday = false },
                )
                HorizontalDivider(color = palette.line.copy(alpha = 0.45f), thickness = 1.dp)
                RepeatOptionRow(
                    label = "The ${nthWeekdayPhrase(anchorNthByDay)}",
                    selected = monthlyByWeekday,
                    palette = palette,
                    onClick = { monthlyByWeekday = true },
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            CalcSectionLabelSafe("ENDS", palette)
            Spacer(modifier = Modifier.height(8.dp))
            RepeatOptionRow(
                label = "Never",
                selected = endMode == RecurrenceEndMode.Never,
                palette = palette,
                onClick = { endMode = RecurrenceEndMode.Never },
            )
            HorizontalDivider(color = palette.line.copy(alpha = 0.45f), thickness = 1.dp)
            RepeatOptionRow(
                label = if (endMode == RecurrenceEndMode.OnDate) "On ${untilDate.format(editorDateFormatter)}" else "On a date",
                selected = endMode == RecurrenceEndMode.OnDate,
                palette = palette,
                onClick = {
                    endMode = RecurrenceEndMode.OnDate
                    showUntilPicker = true
                },
            )
            HorizontalDivider(color = palette.line.copy(alpha = 0.45f), thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { endMode = RecurrenceEndMode.AfterCount }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("After", color = palette.primaryText, fontFamily = mono, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(12.dp))
                if (endMode == RecurrenceEndMode.AfterCount) {
                    CalcStepperButton("−", palette) { countN = (countN - 1).coerceAtLeast(1) }
                    Text(
                        "$countN",
                        color = palette.primaryText,
                        fontFamily = mono,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(min = 44.dp).padding(horizontal = 8.dp),
                    )
                    CalcStepperButton("+", palette) { countN = (countN + 1).coerceAtMost(999) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (countN == 1) "time" else "times", color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp)
                } else {
                    Text("a number of times", color = palette.secondaryText, fontFamily = mono, fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Transparent, modifier = Modifier.size(18.dp))
                }
            }

            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(palette.dialogSurface)
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = palette.primaryText, fontFamily = mono) }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.accent)
                        .noRippleClickable { onConfirm(buildRule().toRRule()) }
                        .padding(horizontal = 28.dp, vertical = 12.dp),
                ) {
                    Text("Done", color = palette.onAccent, fontFamily = mono, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showUntilPicker) {
        DateTimeChoiceSheet(
            title = "Ends on",
            selectedDate = untilDate,
            selectedTime = LocalTime.NOON,
            minDate = anchorDate,
            includeTime = false,
            palette = palette,
            onDismiss = { showUntilPicker = false },
            onSelected = { pickedDate, _ ->
                untilDate = pickedDate
                endMode = RecurrenceEndMode.OnDate
                showUntilPicker = false
            },
        )
    }
}

@Composable
private fun WeekdayPickerRow(selected: Set<DayOfWeek>, palette: DotCalPalette, onToggle: (DayOfWeek) -> Unit) {
    val order = listOf(
        DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY,
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        order.forEach { day ->
            val isOn = day in selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isOn) palette.accent else palette.topBarSurface)
                    .noRippleClickable { onToggle(day) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    day.getDisplayName(java.time.format.TextStyle.NARROW, Locale.US),
                    color = if (isOn) palette.onAccent else palette.secondaryText,
                    fontFamily = mono,
                    fontWeight = if (isOn) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

private fun freqUnitLabel(freq: RecurrenceFreq, interval: Int): String {
    val plural = interval != 1
    return when (freq) {
        RecurrenceFreq.DAILY -> if (plural) "days" else "day"
        RecurrenceFreq.WEEKLY -> if (plural) "weeks" else "week"
        RecurrenceFreq.MONTHLY -> if (plural) "months" else "month"
        RecurrenceFreq.YEARLY -> if (plural) "years" else "year"
    }
}

/** The nth (or last) weekday of the month containing this date, as a BYDAY token. */
private fun LocalDate.nthWeekdayByDay(): ByDay {
    val ordinal = if (dayOfMonth > lengthOfMonth() - 7) -1 else ((dayOfMonth - 1) / 7) + 1
    return ByDay(ordinal, dayOfWeek)
}

private fun nthWeekdayPhrase(byDay: ByDay): String {
    val ord = when (byDay.ordinal) {
        -1 -> "last"
        2 -> "2nd"
        3 -> "3rd"
        4 -> "4th"
        5 -> "5th"
        else -> "1st"
    }
    return "$ord ${byDay.day.getDisplayName(java.time.format.TextStyle.FULL, Locale.US)}"
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
private fun EventRow(event: CalendarEvent, palette: DotCalPalette, onClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
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
private fun AgendaDateHeader(date: LocalDate, isFirst: Boolean, palette: DotCalPalette) {
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
private fun TaskNoDueDateHeader(isFirst: Boolean, palette: DotCalPalette) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isFirst) 0.dp else 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "NO DATE",
            color = palette.secondaryText,
            fontFamily = mono,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
        )
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

@Composable
private fun TaskDetailScreen(
    task: CalendarEvent,
    reminder: EventReminder?,
    palette: DotCalPalette,
    isPrivate: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onTimeBlock: () -> Unit,
    onMoveToPrivate: () -> Unit,
    onRestoreFromPrivate: () -> Unit,
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (task.isCompleted != 1) {
                        Text(
                            "Add to Calendar",
                            color = palette.primaryText,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .clickable(onClick = onTimeBlock)
                                .padding(vertical = 12.dp),
                        )
                    }
                    Text(
                        if (isPrivate) "Restore From Private Vault" else "Move to Private Vault",
                        color = palette.primaryText,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable(onClick = if (isPrivate) onRestoreFromPrivate else onMoveToPrivate)
                            .padding(vertical = 12.dp),
                    )
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
            TasksTopChrome(
                selectedFilter = filter,
                palette = palette,
                onAddClick = onAddClick,
                onFilterSelected = { filter = it },
            )
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    TaskEmptyState(filter = filter, palette = palette, onAddClick = onAddClick)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    val sortedGroups = groupedTasks.entries
                        .sortedWith(compareBy<Map.Entry<LocalDate?, List<CalendarEvent>>> { it.key == null }.thenBy { it.key ?: LocalDate.MAX })
                    sortedGroups.forEachIndexed { groupIndex, (date, group) ->
                            item(key = "header-${date ?: "none"}") {
                                if (date != null) {
                                    AgendaDateHeader(date = date, isFirst = groupIndex == 0, palette = palette)
                                } else {
                                    TaskNoDueDateHeader(isFirst = groupIndex == 0, palette = palette)
                                }
                            }
                            lazyItems(group, key = { it.id }) { task ->
                                TaskRow(
                                    task = task,
                                    reminder = reminders.firstOrNull { it.eventId == task.baseEventId() },
                                    palette = palette,
                                    onClick = { onTaskClick(task) },
                                    onComplete = { onCompleteTask(task) },
                                    onDelete = { onDeleteTask(task) },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun TasksTopChrome(
    selectedFilter: TaskFilter,
    palette: DotCalPalette,
    onAddClick: () -> Unit,
    onFilterSelected: (TaskFilter) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.topBarSurface),
    ) {
        CalendarActionBar(
            title = "Tasks",
            palette = palette,
            onTitleClick = {},
            onAdd = onAddClick,
        )
        Spacer(modifier = Modifier.fillMaxWidth().height(12.dp).background(palette.topBarSurface))
        TaskFilterSegmentedControl(
            selected = selectedFilter,
            palette = palette,
            onSelected = onFilterSelected,
        )
        Spacer(modifier = Modifier.fillMaxWidth().height(16.dp).background(palette.topBarSurface))
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
            val segBg by animateColorAsState(
                targetValue = if (isSelected) segmentSelected else segmentSurface,
                animationSpec = tween(180),
                label = "segBg",
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(segBg)
                    .noRippleClickable { onSelected(option) }
                    .padding(horizontal = 10.dp),
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

@Composable
private fun TaskRow(
    task: CalendarEvent,
    reminder: EventReminder?,
    palette: DotCalPalette,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val completed = task.isCompleted == 1
    val accentStrip = task.displayColor(palette)
    val cardBg = if (palette.isDark) palette.dialogSurface else palette.eventCardSurface
    val cardShape = RoundedCornerShape(16.dp)
    val titleColor = if (completed) palette.primaryText.copy(alpha = 0.5f) else palette.primaryText
    val metadataColor = if (completed) palette.secondaryText.copy(alpha = 0.5f) else palette.secondaryText
    var dragOffset by remember(task.id) { mutableFloatStateOf(0f) }
    val thresholdPx = with(LocalDensity.current) { 96.dp.toPx() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(cardShape)
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
                .clip(cardShape)
                .drawBehind {
                    drawRect(cardBg)
                    drawRect(
                        accentStrip,
                        topLeft = Offset.Zero,
                        size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height),
                    )
                }
                .border(1.dp, palette.eventCardBorder, cardShape)
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
                .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (completed) accentStrip.copy(alpha = 0.15f) else Color.Transparent)
                    .border(1.5.dp, if (completed) accentStrip else palette.disabledText, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (completed) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = accentStrip, modifier = Modifier.size(12.dp))
                }
            }
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    task.title,
                    color = titleColor,
                    fontFamily = mono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textDecoration = if (completed) TextDecoration.LineThrough else null,
                )
                if ((task.hasTaskDate() && task.isAllDay == 0) || reminder != null) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (task.hasTaskDate() && task.isAllDay == 0) {
                            TaskMetadata(label = task.startLocalTime().format(timeFormatter), icon = Icons.Default.AccessTime, color = metadataColor, palette = palette)
                        }
                        reminder?.let {
                            TaskMetadata(label = taskReminderMetadataLabel(it.minutesBefore), icon = Icons.Default.Notifications, color = metadataColor, palette = palette)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskMetadata(label: String, icon: ImageVector, color: Color, palette: DotCalPalette) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = palette.accent, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(2.dp))
        Text(label, color = color, fontFamily = mono, fontSize = 12.sp, maxLines = 1)
    }
}

private fun taskCardColor(palette: DotCalPalette): Color {
    return if (palette.isDark) palette.dialogSurface else palette.eventCardSurface
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
private fun TaskEmptyState(filter: TaskFilter, palette: DotCalPalette, onAddClick: () -> Unit) {
    val title = when (filter) {
        TaskFilter.All -> "No tasks yet"
        TaskFilter.Today -> "Nothing due today"
        TaskFilter.Upcoming -> "All clear"
        TaskFilter.Completed -> "No completed tasks"
    }
    // Only the "All" filter shows the tappable add affordance — other filters are
    // just empty results, not an invitation to create.
    val showAddAffordance = filter == TaskFilter.All
    val subtitle = when (filter) {
        TaskFilter.All -> "Tap to create your first task"
        TaskFilter.Today -> "Enjoy your free time"
        TaskFilter.Upcoming -> "No upcoming tasks scheduled"
        TaskFilter.Completed -> "Completed tasks show up here"
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(bottom = 80.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(palette.cell)
                .then(if (showAddAffordance) Modifier.noRippleClickable(onClick = onAddClick) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (showAddAffordance) Icons.Default.Add else Icons.Default.Check,
                contentDescription = if (showAddAffordance) "Add task" else null,
                tint = palette.accent,
                modifier = Modifier.size(44.dp),
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(title, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Medium, fontSize = 18.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitle, color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp, textAlign = TextAlign.Center)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskEditorSheet(
    task: CalendarEvent?,
    initialReminder: EventReminder?,
    palette: DotCalPalette,
    isPro: Boolean,
    onRequestPro: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (TaskEditorData) -> Unit,
    onDelete: (() -> Unit)? = null,
    templatePrefill: EventTemplate? = null,
    onSaveTemplate: ((EventTemplate) -> Unit)? = null,
) {
    val taskKey = task?.id ?: "new-task"
    // Template prefill only seeds a brand-new task.
    val tpl = if (task == null) templatePrefill else null
    val tplTime: LocalTime? = tpl?.startMinuteOfDay?.let { LocalTime.of(it / 60, it % 60) }
    var title by remember(taskKey) { mutableStateOf(task?.title ?: tpl?.title ?: "") }
    var titleError by remember { mutableStateOf(false) }
    var dueDate by remember(taskKey) { mutableStateOf<LocalDate?>(task?.takeIf { it.hasTaskDate() }?.localDate() ?: LocalDate.now()) }
    var dueTime by remember(taskKey) { mutableStateOf<LocalTime?>(task?.takeIf { it.hasTaskDate() && it.isAllDay == 0 }?.startLocalTime() ?: tplTime) }
    var reminderMinutes by remember(taskKey, initialReminder?.minutesBefore) { mutableStateOf<Int?>(if (tpl != null) tpl.reminderMinutes else initialReminder?.minutesBefore) }
    var recurrenceRule by remember(taskKey) { mutableStateOf(task?.rrule ?: tpl?.rrule) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    var showTaskMenu by remember { mutableStateOf(false) }
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (task == null) "Add Task" else "Edit Task",
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    modifier = Modifier.weight(1f),
                )
                // "Save as template" overflow — only when editing an existing task.
                if (task != null && onSaveTemplate != null) {
                    Box {
                        IconButton(onClick = { showTaskMenu = true }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = palette.primaryText)
                        }
                        DropdownMenu(
                            expanded = showTaskMenu,
                            onDismissRequest = { showTaskMenu = false },
                            containerColor = palette.dialogSurface,
                        ) {
                            DropdownMenuItem(
                                text = { Text("Save as template", color = palette.primaryText, fontFamily = mono, fontSize = 15.sp) },
                                onClick = {
                                    showTaskMenu = false
                                    clearTaskFocus()
                                    if (!isPro) onRequestPro() else showSaveTemplateDialog = true
                                },
                            )
                        }
                    }
                }
            }
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
                value = repeatRowLabel(recurrenceRule),
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
    if (showSaveTemplateDialog && onSaveTemplate != null) {
        TemplateNameDialog(
            defaultName = title.trim(),
            palette = palette,
            onDismiss = { showSaveTemplateDialog = false },
            onConfirm = { name ->
                val template = EventTemplate(
                    id = EventTemplate.newId(),
                    name = name.trim().ifBlank { title.trim().ifBlank { "Template" } },
                    isTask = true,
                    title = title.trim(),
                    description = "",
                    location = "",
                    accountId = null,
                    isAllDay = dueTime == null,
                    startMinuteOfDay = dueTime?.let { it.hour * 60 + it.minute },
                    durationMinutes = 0,
                    reminderMinutes = reminderMinutes,
                    rrule = if (dueDate == null) null else recurrenceRule,
                    createdAtMs = System.currentTimeMillis(),
                )
                onSaveTemplate(template)
                showSaveTemplateDialog = false
                Toast.makeText(context, "Template saved", Toast.LENGTH_SHORT).show()
            },
        )
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
            anchorDate = dueDate ?: LocalDate.now(),
            isPro = isPro,
            palette = palette,
            onDismiss = { showRepeatPicker = false },
            onRequestPro = onRequestPro,
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
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    palette: DotCalPalette,
    onPreviousRange: () -> Unit,
    onNextRange: () -> Unit,
    onJumpToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onAddAtDate: (LocalDate, LocalTime) -> Unit,
    onEventClick: (CalendarEvent) -> Unit,
) {
    val days = remember(selectedDate) { List(3) { selectedDate.plusDays(it.toLong()) } }
    val rangeEvents = remember(eventsByDate, days) {
        days.flatMap { eventsByDate[it].orEmpty() }.filter { it.isAllDay == 0 }
    }
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
    eventsByDate: Map<LocalDate, List<CalendarEvent>>,
    palette: DotCalPalette,
    weekStart: DayOfWeek,
    onPreviousYear: () -> Unit,
    onNextYear: () -> Unit,
    onJumpToday: () -> Unit,
    onMonthSelected: (LocalDate) -> Unit,
) {
    var dragTotal by remember { mutableFloatStateOf(0f) }
    val months = remember(selectedDate.year) { List(12) { selectedDate.withMonth(it + 1).withDayOfMonth(1) } }
    val eventDates = remember(eventsByDate, selectedDate.year) {
        eventsByDate.entries
            .filter { (date, dayEvents) -> date.year == selectedDate.year && dayEvents.any { it.isTask == 0 } }
            .map { it.key }
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
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
            contentPadding = PaddingValues(start = 8.dp, top = 0.dp, end = 8.dp, bottom = 150.dp),
        ) {
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
    widgetTransparent: Boolean,
    widgetDotTexture: Boolean,
    appLockState: AppLockState,
    privateVaultEvents: List<CalendarEvent>,
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
    onWidgetTransparentChange: (Boolean) -> Unit,
    onWidgetDotTextureChange: (Boolean) -> Unit,
    onBirthdayEnabledChange: (Boolean) -> Unit,
    onAddHolidayCountry: (HolidayCountryUiItem) -> Unit,
    onRemoveHolidayCountry: (HolidayCountryUiItem) -> Unit,
    onRateDotCal: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onRequestCalendarAccess: () -> Unit,
    onAddAccount: () -> Unit,
    isPro: Boolean,
    onDotCalPro: () -> Unit,
    onRestorePurchase: () -> Unit,
    onDateCalculator: () -> Unit,
    onAppPrivacy: () -> Unit,
    onSetAppLockPin: (String, (Result<Unit>) -> Unit) -> Unit,
    onVerifyAppLockPin: (String, (Boolean) -> Unit) -> Unit,
    onSetAppLockEnabled: (Boolean) -> Unit,
    onDisableAppLock: () -> Unit,
    onClearAppLockPin: () -> Unit,
    onRestorePrivateEvent: (String) -> Unit,
    onRecentlyDeleted: () -> Unit,
    onTemplates: () -> Unit,
    onExportIcs: () -> Unit,
    onImportIcs: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
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
            widgetTransparent = widgetTransparent,
            widgetDotTexture = widgetDotTexture,
            appLockState = appLockState,
            privateVaultEvents = privateVaultEvents,
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
            onWidgetTransparentChange = onWidgetTransparentChange,
            onWidgetDotTextureChange = onWidgetDotTextureChange,
            onBirthdayEnabledChange = onBirthdayEnabledChange,
            onGlobalHolidays = { onScreenChange(SettingsScreen.GlobalHolidays) },
            onPrivacyPolicy = { onScreenChange(SettingsScreen.PrivacyPolicy) },
            onRateDotCal = onRateDotCal,
            onCheckForUpdates = onCheckForUpdates,
            onRequestCalendarAccess = onRequestCalendarAccess,
            onAddAccount = { onScreenChange(SettingsScreen.AddAccount) },
            isPro = isPro,
            onDotCalPro = onDotCalPro,
            onRestorePurchase = onRestorePurchase,
            onDateCalculator = onDateCalculator,
            onAppPrivacy = onAppPrivacy,
            onRecentlyDeleted = onRecentlyDeleted,
            onTemplates = onTemplates,
            onExportIcs = onExportIcs,
            onImportIcs = onImportIcs,
            onBackup = onBackup,
            onRestore = onRestore,
        )
        AnimatedVisibility(
            visible = screen == SettingsScreen.Theme,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
        ) {
            ThemeSettings(
            themeMode = themeMode,
            accentColor = accentColor,
            palette = palette,
            isPro = isPro,
            onBack = { onScreenChange(SettingsScreen.Root) },
            onThemeSelected = onThemeSelected,
            onAccentSelected = onAccentSelected,
            onRequestPro = onDotCalPro,
            )
        }
        AnimatedVisibility(
            visible = screen == SettingsScreen.CalendarAccounts,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
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
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
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
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
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
            visible = screen == SettingsScreen.AppPrivacy,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
        ) {
            AppPrivacySettings(
                palette = palette,
                appLockState = appLockState,
                privateVaultEvents = privateVaultEvents,
                onBack = { onScreenChange(SettingsScreen.Root) },
                onSetPin = onSetAppLockPin,
                onVerifyPin = onVerifyAppLockPin,
                onSetLockEnabled = onSetAppLockEnabled,
                onDisableLock = onDisableAppLock,
                onClearPin = onClearAppLockPin,
                onRestorePrivateEvent = onRestorePrivateEvent,
            )
        }
        AnimatedVisibility(
            visible = screen == SettingsScreen.PrivacyPolicy,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
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
    widgetTransparent: Boolean,
    widgetDotTexture: Boolean,
    appLockState: AppLockState,
    privateVaultEvents: List<CalendarEvent>,
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
    onWidgetTransparentChange: (Boolean) -> Unit,
    onWidgetDotTextureChange: (Boolean) -> Unit,
    onBirthdayEnabledChange: (Boolean) -> Unit,
    onGlobalHolidays: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onRateDotCal: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onRequestCalendarAccess: () -> Unit,
    onAddAccount: () -> Unit,
    isPro: Boolean,
    onDotCalPro: () -> Unit,
    onRestorePurchase: () -> Unit,
    onDateCalculator: () -> Unit,
    onAppPrivacy: () -> Unit,
    onRecentlyDeleted: () -> Unit,
    onTemplates: () -> Unit,
    onExportIcs: () -> Unit,
    onImportIcs: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val showCompactHeader = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 96
    Box(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 150.dp), modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
            item {
                SettingsLargeHeader(palette = palette, onBack = onBack, showBack = false)
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
            SettingsProBadgeRow(
                title = "Date Calculator",
                isPro = isPro,
                palette = palette,
                onClick = onDateCalculator,
            )
            SettingsProBadgeRow(
                title = "App Lock & Private Vault",
                isPro = isPro,
                palette = palette,
                onClick = onAppPrivacy,
            )
            SettingsWidgetToggleRow(
                title = "Transparent Widgets",
                subtitle = "Let wallpaper show through all DotCal widgets",
                checked = widgetTransparent,
                isPro = isPro,
                palette = palette,
                onCheckedChange = onWidgetTransparentChange,
            )
            SettingsWidgetToggleRow(
                title = "Widget Dot Texture",
                subtitle = if (widgetTransparent) "Only applies when transparent widgets are off" else "Show the subtle DotCal dotted surface",
                checked = !widgetTransparent && widgetDotTexture,
                enabled = !widgetTransparent,
                isPro = isPro,
                palette = palette,
                onCheckedChange = onWidgetDotTextureChange,
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

            SettingsSectionTitle("Data", palette)
            // Import/Export + Backup/Restore are FREE (data portability & safety).
            // isPro = true forces the unlocked chevron and suppresses the Pro tag/lock.
            SettingsImportExportRow(
                title = "Export Calendar",
                subtitle = "Save all events & tasks to an .ics file",
                isPro = true,
                palette = palette,
                onClick = onExportIcs,
            )
            SettingsImportExportRow(
                title = "Import Calendar",
                subtitle = "Load events & tasks from an .ics file",
                isPro = true,
                palette = palette,
                onClick = onImportIcs,
            )
            SettingsImportExportRow(
                title = "Back Up Data",
                subtitle = "Save all events, tasks & reminders to a file",
                isPro = true,
                palette = palette,
                onClick = onBackup,
            )
            SettingsImportExportRow(
                title = "Restore Data",
                subtitle = "Merge a backup file into this device",
                isPro = true,
                palette = palette,
                onClick = onRestore,
            )
            SettingsMenuRow(
                title = "Recently Deleted",
                value = "",
                palette = palette,
                onClick = onRecentlyDeleted,
            )
            SettingsProBadgeRow(
                title = "Templates",
                isPro = isPro,
                palette = palette,
                onClick = onTemplates,
            )
            SettingsDivider(palette)

            SettingsSectionTitle("About", palette)
            SettingsMenuRow(title = "Check for updates", value = "", palette = palette, onClick = onCheckForUpdates)
            SettingsMenuRow(title = "Privacy Policy", value = "", palette = palette, onClick = onPrivacyPolicy)
            SettingsMenuRow(title = "Rate DotCal", value = "", palette = palette, onClick = onRateDotCal)
            SettingsMenuRow(title = "Send Feedback", value = "", palette = palette, onClick = {
                context.startActivity(
                    Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:dotfieldstudio@gmail.com?subject=DotCal%20Feedback")
                    }
                )
            })
            SettingsMenuRow(title = "Version", value = BuildConfig.VERSION_NAME, palette = palette, showChevron = false, onClick = {})
            SettingsDivider(palette)

            SettingsSectionTitle("DotCal Pro", palette)
            SettingsProRow(
                isPro = isPro,
                palette = palette,
                onClick = onDotCalPro,
            )
            Spacer(modifier = Modifier.height(32.dp))
            }
        }
        if (showCompactHeader) {
            SettingsCompactHeader(palette = palette, onBack = onBack, showBack = false)
        }
    }
}

@Composable
private fun AppPrivacySettings(
    palette: DotCalPalette,
    appLockState: AppLockState,
    privateVaultEvents: List<CalendarEvent>,
    onBack: () -> Unit,
    onSetPin: (String, (Result<Unit>) -> Unit) -> Unit,
    onVerifyPin: (String, (Boolean) -> Unit) -> Unit,
    onSetLockEnabled: (Boolean) -> Unit,
    onDisableLock: () -> Unit,
    onClearPin: () -> Unit,
    onRestorePrivateEvent: (String) -> Unit,
) {
    var showSetPin by remember { mutableStateOf(false) }
    var showDisableConfirm by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(palette.calendarSurface).padding(horizontal = 22.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
    ) {
        item {
            SettingsLargeHeader(palette = palette, onBack = onBack, title = "Privacy")
            Spacer(modifier = Modifier.height(10.dp))
        }
        item {
            SettingsSectionTitle("App Lock", palette)
            SettingsWidgetToggleRow(
                title = "Require PIN",
                subtitle = if (appLockState.hasPin) "Lock DotCal after leaving the app" else "Set a 4-8 digit PIN",
                checked = appLockState.enabled,
                isPro = true,
                palette = palette,
                onCheckedChange = { enabled ->
                    when {
                        enabled && !appLockState.hasPin -> showSetPin = true
                        enabled -> onSetLockEnabled(true)
                        else -> showDisableConfirm = true
                    }
                },
            )
            SettingsMenuRow(
                title = if (appLockState.hasPin) "Change PIN" else "Set PIN",
                value = "",
                palette = palette,
                onClick = { showSetPin = true },
            )
            if (appLockState.hasPin) {
                SettingsMenuRow(
                    title = "Remove PIN",
                    value = "",
                    palette = palette,
                    onClick = { showClearConfirm = true },
                )
            }
            SettingsDivider(palette)
            SettingsSectionTitle("Private Vault", palette)
            Text(
                "Hidden events and tasks stay off calendars, task lists, widgets, and reminders until restored.",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (privateVaultEvents.isEmpty()) {
            item {
                Text(
                    "No private items",
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 15.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp),
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            lazyItems(privateVaultEvents, key = { it.id }) { event ->
                PrivateVaultRow(
                    event = event,
                    palette = palette,
                    onRestore = { onRestorePrivateEvent(event.baseEventId()) },
                )
                PrivateVaultDivider(palette)
            }
        }
    }
    if (showSetPin) {
        AppPinDialog(
            title = if (appLockState.hasPin) "Change PIN" else "Set PIN",
            confirmLabel = "Save",
            palette = palette,
            onDismiss = { showSetPin = false },
            onConfirm = { pin, onResult ->
                onSetPin(pin) { result ->
                    onResult(result.isSuccess)
                    if (result.isSuccess) showSetPin = false
                }
            },
        )
    }
    if (showDisableConfirm) {
        AppPinDialog(
            title = "Disable App Lock",
            confirmLabel = "Disable",
            palette = palette,
            onDismiss = { showDisableConfirm = false },
            onConfirm = { pin, onResult ->
                onVerifyPin(pin) { ok ->
                    onResult(ok)
                    if (ok) {
                        onDisableLock()
                        showDisableConfirm = false
                    }
                }
            },
        )
    }
    if (showClearConfirm) {
        AppPinDialog(
            title = "Remove PIN",
            confirmLabel = "Remove",
            palette = palette,
            onDismiss = { showClearConfirm = false },
            onConfirm = { pin, onResult ->
                onVerifyPin(pin) { ok ->
                    onResult(ok)
                    if (ok) {
                        onClearPin()
                        showClearConfirm = false
                    }
                }
            },
        )
    }
}

@Composable
private fun PrivateVaultRow(
    event: CalendarEvent,
    palette: DotCalPalette,
    onRestore: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                event.title.ifBlank { "(No title)" },
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                privateVaultWhenLabel(event),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            "Restore",
            color = palette.accent,
            fontFamily = mono,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            modifier = Modifier.noRippleClickable(onClick = onRestore).padding(horizontal = 10.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun PrivateVaultDivider(palette: DotCalPalette) {
    HorizontalDivider(color = palette.line, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
}

private fun privateVaultWhenLabel(event: CalendarEvent): String {
    if (event.isTask == 1 && event.startTimeMs <= 0L) return "Task - No due date"
    val zone = runCatching { ZoneId.of(event.timeZone) }.getOrDefault(ZoneId.systemDefault())
    val start = Instant.ofEpochMilli(event.startTimeMs).atZone(zone)
    val type = if (event.isTask == 1) "Task" else "Event"
    return "$type - ${start.format(compactDateFormatter)}"
}

@Composable
private fun AppLockScreen(
    palette: DotCalPalette,
    canUseDeviceLock: Boolean,
    onUnlockWithPin: (String, (Boolean) -> Unit) -> Unit,
    onDeviceUnlock: () -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .zIndex(20f),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(palette.eventCardSurface)
                    .border(1.dp, palette.eventCardBorder, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = palette.accent, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.height(22.dp))
            Text("DotCal Locked", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Enter your PIN to continue", color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { value ->
                    pin = value.filter(Char::isDigit).take(8)
                    showError = false
                },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onUnlockWithPin(pin) { ok -> showError = !ok } }),
                textStyle = TextStyle(color = palette.primaryText, fontFamily = mono, fontSize = 20.sp, textAlign = TextAlign.Center),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = palette.primaryText,
                    unfocusedTextColor = palette.primaryText,
                    focusedBorderColor = palette.accent,
                    unfocusedBorderColor = palette.textFieldBorder,
                    cursorColor = palette.accent,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            if (showError) {
                Spacer(modifier = Modifier.height(10.dp))
                Text("Incorrect PIN", color = palette.accent, fontFamily = mono, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(18.dp))
            Button(
                onClick = { onUnlockWithPin(pin) { ok -> showError = !ok } },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text("Unlock", fontFamily = mono, fontWeight = FontWeight.SemiBold)
            }
            if (canUseDeviceLock) {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(onClick = onDeviceUnlock) {
                    Text("Use Device Lock", color = palette.primaryText, fontFamily = mono)
                }
            }
        }
    }
}

@Composable
private fun AppPinDialog(
    title: String,
    confirmLabel: String,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onConfirm: (String, (Boolean) -> Unit) -> Unit,
) {
    var pin by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        titleContentColor = palette.primaryText,
        textContentColor = palette.secondaryText,
        title = { Text(title, fontFamily = mono) },
        text = {
            Column {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { value ->
                        pin = value.filter(Char::isDigit).take(8)
                        showError = false
                    },
                    singleLine = true,
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onConfirm(pin) { ok -> showError = !ok }
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = palette.primaryText,
                        unfocusedTextColor = palette.primaryText,
                        focusedBorderColor = palette.accent,
                        unfocusedBorderColor = palette.textFieldBorder,
                        cursorColor = palette.accent,
                        focusedLabelColor = palette.accent,
                        unfocusedLabelColor = palette.secondaryText,
                    ),
                )
                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Enter the correct 4-8 digit PIN", color = palette.accent, fontFamily = mono, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(pin) { ok -> showError = !ok } }) {
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
private fun SettingsLargeHeader(
    palette: DotCalPalette,
    onBack: () -> Unit,
    title: String = "Calendar",
    showBack: Boolean = true,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 8.dp)) {
        if (showBack) {
            IconButton(onClick = onBack, modifier = Modifier.offset(x = (-16).dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
        }
        Text(
            title,
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            modifier = Modifier.padding(start = 0.dp, top = if (showBack) 2.dp else 10.dp),
        )
    }
}

@Composable
private fun SettingsCompactHeader(
    palette: DotCalPalette,
    onBack: () -> Unit,
    title: String = "Calendar",
    showBack: Boolean = true,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(palette.calendarSurface),
    ) {
        if (showBack) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
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
    isPro: Boolean,
    onBack: () -> Unit,
    onThemeSelected: (DotCalThemeMode) -> Unit,
    onAccentSelected: (AccentColor) -> Unit,
    onRequestPro: () -> Unit,
) {
    var showCustomPicker by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showCompactHeader = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 96
    Box(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 150.dp), modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
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
                accents = AccentColor.freePresets,
                selectedAccent = accentColor,
                palette = palette,
                locked = false,
                onAccentSelected = onAccentSelected,
                onLockedClick = onRequestPro,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "More Colors",
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(start = 4.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (!isPro) {
                    Text("Pro", color = palette.accent, fontFamily = mono, fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            AccentColorSwatches(
                accents = AccentColor.proPresets,
                selectedAccent = accentColor,
                palette = palette,
                locked = !isPro,
                onAccentSelected = onAccentSelected,
                onLockedClick = onRequestPro,
            )
            Spacer(modifier = Modifier.height(28.dp))
            CustomAccentRow(
                accentColor = accentColor,
                palette = palette,
                isPro = isPro,
                onClick = { if (isPro) showCustomPicker = true else onRequestPro() },
            )
            Spacer(modifier = Modifier.height(960.dp))
            }
        }
        if (showCompactHeader) {
            SettingsCompactHeader(palette = palette, onBack = onBack, title = "Theme")
        }
    }
    if (showCustomPicker) {
        CustomAccentPickerDialog(
            initial = accentColor.color,
            palette = palette,
            onDismiss = { showCustomPicker = false },
            onConfirm = { hex ->
                showCustomPicker = false
                onAccentSelected(AccentColor.Custom(hex))
            },
        )
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
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 150.dp), modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
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
            .height(64.dp)
            .noRippleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(country.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        if (selected) {
            Icon(Icons.Default.Close, contentDescription = "Remove", tint = palette.secondaryText, modifier = Modifier.size(22.dp))
        } else {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = palette.accent, modifier = Modifier.size(22.dp))
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
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 150.dp), modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
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
        LazyColumn(state = listState, contentPadding = PaddingValues(bottom = 150.dp), modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp)) {
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
            .height(64.dp)
            .noRippleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.dotfield.dotcal.R.drawable.ic_google_logo),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                "Google",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
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
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
        SettingsCompactHeader(palette = palette, onBack = onBack, title = "Privacy Policy")
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            // Hero intro
            item {
                Column {
                    Text(
                        "LEGAL",
                        color = palette.accent,
                        fontFamily = mono,
                        fontSize = 11.sp,
                        letterSpacing = 2.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Your data stays on your device.",
                        color = palette.primaryText,
                        fontFamily = mono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "DotCal stores your calendar data locally on your device. Nothing goes to any server. No account, no sync service, no telemetry — ever.",
                        color = palette.secondaryText,
                        fontFamily = mono,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                }
            }

            item {
                PrivacySection(
                    "01  Overview",
                    "DotCal collects no personal data and transmits no information to any server. All calendar events, tasks, reminders, attachments, and settings exist only on your Android device.\n\nThis policy covers the DotCal Android app (com.dotfield.dotcal), published by Dotfield Studio, and applies from the date of your first install.\n\nWe built DotCal with a simple rule: data about your life belongs to you. The app works entirely offline. There is no backend, no account system, and no analytics SDK embedded anywhere in the code.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "02  Data We Collect",
                    "DotCal does not collect, store, or transmit any personal data to Dotfield Studio or any third party.\n\nCalendar events, tasks, reminder alarms, voice notes, image attachments, theme and app settings, and any imported contact birthdays or Google Calendar events all live only on your device. None of it is ever sent anywhere, and we cannot access any of it.\n\nNo crash analytics. No usage analytics. Nothing is collected.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "03  App Permissions",
                    "DotCal requests only what it needs to function:\n\n· Calendar (read/write) — only if you enable Google Calendar sync, to read/create events via Android's CalendarProvider. No data leaves the device.\n· Contacts — optional, to read contact birthdays as yearly events. Only reads dates, not full contact data.\n· Exact Alarm & Boot — schedule reminder alarms and re-register them after a reboot.\n· Notifications — display event and task reminders (Android 13+).\n· Microphone — optional, to record voice notes. Saved to app-private storage, only when you tap record.\n· Photos — selected via Android Photo Picker only. DotCal never accesses your full gallery.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "04  Google Calendar Sync",
                    "DotCal does not connect to Google's servers. It reads from Android's built-in CalendarProvider — the same local database the default Calendar app uses. No Google credentials are ever seen or stored.\n\nThis is different from apps that use the Google Calendar REST API or OAuth. DotCal never makes HTTP requests and never sees your Google credentials.\n\nIf you disable sync, DotCal stops querying CalendarProvider. Previously imported events stay in DotCal's local database until you delete them.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "05  Local Storage",
                    "All DotCal data is stored in Android's app-private storage, inaccessible to other apps without root access, and deleted automatically when you uninstall.\n\nStored data includes the SQLite database (events, tasks, recurrence rules), local preferences (theme, view, sync toggles), voice note files, and image attachments.\n\nDotCal does not write to shared external storage. If your device's Google Backup is enabled at the system level, Android may back up app data — this is outside DotCal's control.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "06  Third Parties",
                    "DotCal integrates no third-party SDKs for analytics, advertising, crash reporting, or remote configuration. No Firebase, no Crashlytics, no advertising networks, no A/B testing — nothing that makes outbound network requests.\n\nThe only external dependency is the Android operating system itself.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "07  Security",
                    "Because DotCal stores all data locally and makes no network calls, the attack surface is limited to physical device access. Keep your device PIN or biometric lock active.\n\nAndroid's app sandbox provides the primary protection. On modern devices with hardware-backed encryption, the OS-level encryption protects DotCal data at rest.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "08  Children's Privacy",
                    "DotCal does not knowingly collect information from anyone, including children under 13. Because DotCal collects no data at all, there is no children's data to protect beyond standard Android privacy protections.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "09  Your Rights",
                    "Because DotCal does not collect or store personal data on any server, all of your data is under your direct control:\n\n· Access — your data is in the app on your device.\n· Delete — uninstall to remove everything, or delete individual events in-app.\n· Export — PDF export of calendar views is a planned feature.\n· Portability — events synced via CalendarProvider remain in Android's calendar database.\n\nIf you are in the EU, UK, or California, GDPR and CCPA rights apply — exercised entirely on-device, since we hold no data.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "10  Policy Changes",
                    "If this policy changes materially — for example, if DotCal adds any network feature — we will update this page and the effective date below, and flag the change in a release note. We commit to never adding advertising, analytics, or cloud storage without updating this policy and prominently notifying users.",
                    palette,
                )
            }

            // Contact card
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(palette.cell)
                        .noRippleClickable {
                            context.startActivity(
                                Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:dotfieldstudio@gmail.com?subject=DotCal%20Privacy")
                                }
                            )
                        }
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Questions? Contact us",
                            color = palette.primaryText,
                            fontFamily = mono,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                        Text(
                            "dotfieldstudio@gmail.com",
                            color = palette.accent,
                            fontFamily = mono,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp),
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
            item {
                Text(
                    "Effective June 27, 2026 · Dotfield Studio",
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun PrivacySection(title: String, content: String, palette: DotCalPalette) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            title,
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            content,
            color = palette.secondaryText,
            fontFamily = mono,
            fontSize = 13.sp,
            lineHeight = 19.sp,
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
        modifier = Modifier.fillMaxWidth().height(72.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(parseColor(account.color))),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    account.displayName.readableCalendarLabel(),
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    account.secondaryCalendarLabel(),
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 12.sp,
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
                .height(64.dp)
                .noRippleClickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Start of the week", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(selectedOption.label, color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp)
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
                .height(64.dp)
                .noRippleClickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Default reminder", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(reminderLabel(selectedMinutes), color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp)
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
            .height(64.dp)
            .noRippleClickable { showPicker = true },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("Default all-day reminder time", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(allDayReminderTimeLabel(selectedTime), color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp)
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
        fontSize = 14.sp,
        modifier = Modifier.padding(top = 24.dp, bottom = 14.dp),
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
            .height(64.dp)
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 0.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(title, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value.isNotBlank()) {
                Text(value, color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp)
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
private fun ProBadge(palette: DotCalPalette) {
    Box(
        modifier = Modifier
            .background(palette.accent)
            .padding(horizontal = 4.dp, vertical = 2.dp),
    ) {
        Text("PRO", color = palette.onAccent, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

@Composable
private fun ProFeatureTag(palette: DotCalPalette) {
    Text(
        "Pro feature",
        color = palette.accent,
        fontFamily = mono,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
    )
}

@Composable
private fun SettingsProRow(isPro: Boolean, palette: DotCalPalette, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .noRippleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Star, contentDescription = null, tint = palette.accent, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("DotCal Pro", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Text(
                if (isPro) "Active — thank you for your support!" else "Unlock Pro features",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SettingsImportExportRow(
    title: String,
    subtitle: String,
    isPro: Boolean,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .noRippleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 16.sp)
                if (!isPro) {
                    Spacer(modifier = Modifier.width(8.dp))
                    ProFeatureTag(palette)
                }
            }
            Text(subtitle, color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
        }
        SettingsTrailingIcon(isPro = isPro, palette = palette)
    }
}

@Composable
private fun SettingsProBadgeRow(title: String, isPro: Boolean, palette: DotCalPalette, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .noRippleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 16.sp)
        if (!isPro) {
            Spacer(modifier = Modifier.width(8.dp))
            ProFeatureTag(palette)
        }
        Spacer(modifier = Modifier.weight(1f))
        SettingsTrailingIcon(isPro = isPro, palette = palette)
    }
}

@Composable
private fun SettingsTrailingIcon(isPro: Boolean, palette: DotCalPalette) {
    Box(
        modifier = Modifier.width(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (isPro) Icons.Default.ChevronRight else Icons.Default.Lock,
            contentDescription = null,
            tint = palette.secondaryText,
            modifier = Modifier.size(if (isPro) 20.dp else 16.dp),
        )
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
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
            )
            Text(
                syncMetadata.lastSyncedSubtitle(),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
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
            .height(if (subtitle == null) 64.dp else 76.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 16.sp)
            if (subtitle != null) {
                Text(subtitle, color = palette.secondaryText, fontFamily = mono, fontSize = 13.sp, lineHeight = 16.sp)
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
private fun SettingsWidgetToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    isPro: Boolean,
    palette: DotCalPalette,
    onCheckedChange: (Boolean) -> Unit,
) {
    val titleColor = if (enabled || !isPro) palette.primaryText else palette.secondaryText
    val subtitleColor = if (enabled || !isPro) palette.secondaryText else palette.disabledText
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .noRippleClickable(enabled = !isPro) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = titleColor, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 16.sp)
                if (!isPro) {
                    Spacer(modifier = Modifier.width(8.dp))
                    ProFeatureTag(palette)
                }
            }
            Text(subtitle, color = subtitleColor, fontFamily = mono, fontSize = 13.sp, lineHeight = 16.sp)
        }
        if (isPro) {
            DotCalSwitch(
                checked = checked,
                palette = palette,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
            )
        } else {
            Box(modifier = Modifier.noRippleClickable { onCheckedChange(!checked) }) {
                SettingsTrailingIcon(isPro = false, palette = palette)
            }
        }
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
                .height(64.dp)
                .noRippleClickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Sync interval", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(syncIntervalLabel(intervalMins), color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp)
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
            .height(76.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) palette.cell else Color.Transparent)
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            ThemePreview(mode = mode, accentColor = accentColor)
            Column {
                Text(mode.label, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(if (selected) "Active" else "Tap to apply", color = if (selected) palette.accent else palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
            }
        }
        Box(
            modifier = Modifier
                .size(20.dp)
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccentColorSwatches(
    accents: List<AccentColor.Preset>,
    selectedAccent: AccentColor,
    palette: DotCalPalette,
    locked: Boolean,
    onAccentSelected: (AccentColor) -> Unit,
    onLockedClick: () -> Unit,
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        accents.forEach { accent ->
            val selected = accent.storageValue == selectedAccent.storageValue
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (selected) palette.primaryText else palette.line, CircleShape)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(accent.color)
                    .noRippleClickable { if (locked) onLockedClick() else onAccentSelected(accent) },
                contentAlignment = Alignment.Center,
            ) {
                when {
                    locked -> Icon(
                        Icons.Default.Lock,
                        contentDescription = "Pro",
                        tint = accent.onColor.copy(alpha = 0.9f),
                        modifier = Modifier.size(16.dp),
                    )
                    selected -> Icon(
                        Icons.Default.Check,
                        contentDescription = accent.label,
                        tint = accent.onColor,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomAccentRow(
    accentColor: AccentColor,
    palette: DotCalPalette,
    isPro: Boolean,
    onClick: () -> Unit,
) {
    val isCustom = accentColor is AccentColor.Custom
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .noRippleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(2.dp, if (isCustom) palette.primaryText else palette.line, CircleShape)
                .padding(3.dp)
                .clip(CircleShape)
                .background(if (isCustom) accentColor.color else palette.cell),
            contentAlignment = Alignment.Center,
        ) {
            if (!isPro) {
                Icon(Icons.Default.Lock, contentDescription = "Pro", tint = palette.secondaryText, modifier = Modifier.size(16.dp))
            } else {
                Icon(Icons.Default.Add, contentDescription = "Custom color", tint = if (isCustom) accentColor.onColor else palette.secondaryText, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Custom Color", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                if (!isPro) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pro", color = palette.accent, fontFamily = mono, fontSize = 11.sp)
                }
            }
            Text(
                if (isCustom) accentColor.label else "Pick any hex color",
                color = if (isCustom) palette.accent else palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun CustomAccentPickerDialog(
    initial: Color,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val initialHsv = remember(initial) {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(initial.toArgb(), hsv)
        hsv
    }
    var hue by remember { mutableStateOf(initialHsv[0]) }
    var sat by remember { mutableStateOf(initialHsv[1]) }
    var value by remember { mutableStateOf(initialHsv[2]) }
    val current = remember(hue, sat, value) {
        Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, value)))
    }
    val currentHex = remember(current) { AccentColor.normalizeHex("#%06X".format(0xFFFFFF and current.toArgb())) ?: "#FF3B30" }
    var hexField by remember { mutableStateOf(currentHex) }
    // Keep the hex text field in sync when the user drags the picker.
    LaunchedEffect(currentHex) { hexField = currentHex }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        title = {
            Text("Custom Accent", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Live preview.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(current),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        currentHex,
                        color = if (current.luminanceApprox() > 0.5f) Color(0xFF101010) else Color(0xFFFFFFFF),
                        fontFamily = mono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
                CalcSectionLabelSafe("HUE", palette)
                Spacer(modifier = Modifier.height(8.dp))
                HueSlider(hue = hue, palette = palette, onHueChange = { hue = it })
                Spacer(modifier = Modifier.height(18.dp))
                CalcSectionLabelSafe("SATURATION", palette)
                Spacer(modifier = Modifier.height(8.dp))
                ValueSlider(
                    fraction = sat,
                    track = Brush.horizontalGradient(
                        listOf(
                            Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 0f, value))),
                            Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, value))),
                        ),
                    ),
                    palette = palette,
                    onChange = { sat = it },
                )
                Spacer(modifier = Modifier.height(18.dp))
                CalcSectionLabelSafe("BRIGHTNESS", palette)
                Spacer(modifier = Modifier.height(8.dp))
                ValueSlider(
                    fraction = value,
                    track = Brush.horizontalGradient(
                        listOf(
                            Color.Black,
                            Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, sat, 1f))),
                        ),
                    ),
                    palette = palette,
                    onChange = { value = it },
                )
                Spacer(modifier = Modifier.height(18.dp))
                CalcSectionLabelSafe("HEX", palette)
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, palette.textFieldBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    BasicTextField(
                        value = hexField,
                        onValueChange = { raw ->
                            hexField = raw
                            AccentColor.normalizeHex(raw)?.let { normalized ->
                                val hsv = FloatArray(3)
                                android.graphics.Color.colorToHSV(android.graphics.Color.parseColor(normalized), hsv)
                                hue = hsv[0]; sat = hsv[1]; value = hsv[2]
                            }
                        },
                        singleLine = true,
                        textStyle = TextStyle(color = palette.primaryText, fontFamily = mono, fontSize = 15.sp),
                        cursorBrush = SolidColor(palette.accent),
                    )
                }
            }
        },
        confirmButton = {
            Text(
                "Apply",
                color = palette.accent,
                fontFamily = mono,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.noRippleClickable { onConfirm(currentHex) }.padding(8.dp),
            )
        },
        dismissButton = {
            Text(
                "Cancel",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 15.sp,
                modifier = Modifier.noRippleClickable(onClick = onDismiss).padding(8.dp),
            )
        },
    )
}

@Composable
private fun CalcSectionLabelSafe(text: String, palette: DotCalPalette) {
    Text(text, color = palette.secondaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
}

/** Rainbow hue slider (0..360). */
@Composable
private fun HueSlider(hue: Float, palette: DotCalPalette, onHueChange: (Float) -> Unit) {
    var widthPx by remember { mutableStateOf(1) }
    val hueBrush = remember {
        Brush.horizontalGradient(
            (0..360 step 60).map { Color(android.graphics.Color.HSVToColor(floatArrayOf(it.toFloat(), 1f, 1f))) },
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(hueBrush)
            .onSizeChanged { widthPx = it.width.coerceAtLeast(1) }
            .pointerInput(Unit) {
                detectTapGestures { offset -> onHueChange((offset.x / widthPx).coerceIn(0f, 1f) * 360f) }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    onHueChange((change.position.x / widthPx).coerceIn(0f, 1f) * 360f)
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        SliderThumb(fraction = hue / 360f, widthPx = widthPx)
    }
}

/** Generic 0..1 slider with a custom gradient [track]. */
@Composable
private fun ValueSlider(fraction: Float, track: Brush, palette: DotCalPalette, onChange: (Float) -> Unit) {
    var widthPx by remember { mutableStateOf(1) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(track)
            .onSizeChanged { widthPx = it.width.coerceAtLeast(1) }
            .pointerInput(Unit) {
                detectTapGestures { offset -> onChange((offset.x / widthPx).coerceIn(0f, 1f)) }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    onChange((change.position.x / widthPx).coerceIn(0f, 1f))
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        SliderThumb(fraction = fraction, widthPx = widthPx)
    }
}

@Composable
private fun SliderThumb(fraction: Float, widthPx: Int) {
    val thumb = 22.dp
    val density = LocalDensity.current
    val trackWidthDp = with(density) { widthPx.toDp() }
    val x = (trackWidthDp - thumb) * fraction.coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .offset(x = x)
            .size(thumb)
            .clip(CircleShape)
            .background(Color.White)
            .border(2.dp, Color(0x33000000), CircleShape),
    )
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
    val rule = RecurrenceRule.parse(rrule) ?: return null
    return "REPEATS · " + rule.humanLabel().uppercase()
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
    AppPrivacy,
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

/**
 * Accent color model. Free tier uses the 5 [Preset] swatches; Pro unlocks the extra curated
 * [proPresets] and a full [Custom] hex color picker.
 *
 * Storage format in [CalendarPreferences.KEY_ACCENT_COLOR] (and the boot SharedPreferences mirror):
 *  - a preset enum name, e.g. "BLUE" (backward compatible with existing installs), or
 *  - a "#RRGGBB" hex string for a Pro custom color.
 */
private sealed interface AccentColor {
    val color: Color
    val label: String

    /** Text/icon color that stays legible on top of [color]. */
    val onColor: Color
        get() = if (color.luminanceApprox() > 0.5f) Color(0xFF101010) else Color(0xFFFFFFFF)

    /** Value persisted to DataStore + boot prefs. */
    val storageValue: String

    enum class Preset(val hex: String, override val label: String) : AccentColor {
        // Free presets. Order/names are storage-stable; do not rename.
        RED("#FF3B30", "Red"),
        BLUE("#0A84FF", "Blue"),
        GREEN("#30D158", "Green"),
        PURPLE("#BF5AF2", "Purple"),
        AMBER("#FF9F0A", "Amber"),
        // Pro presets (extra curated palette).
        TEAL("#2AB8B0", "Teal"),
        PINK("#FF375F", "Pink"),
        ORANGE("#FF6B00", "Orange"),
        CYAN("#32ADE6", "Cyan"),
        INDIGO("#5E5CE6", "Indigo"),
        MINT("#66D4A0", "Mint"),
        ROSE("#F06292", "Rose"),
        LIME("#B0C948", "Lime");

        override val color: Color get() = Color(android.graphics.Color.parseColor(hex))
        override val storageValue: String get() = name
        val isPro: Boolean get() = this in proPresets
    }

    /** Pro-only arbitrary hex color chosen from the color picker. */
    data class Custom(val hex: String) : AccentColor {
        override val color: Color get() = Color(android.graphics.Color.parseColor(hex))
        override val label: String get() = hex.uppercase()
        override val storageValue: String get() = hex.uppercase()
    }

    companion object {
        val Default: Preset = Preset.RED

        /** Free presets shown to everyone. */
        val freePresets: List<Preset> = listOf(
            Preset.RED, Preset.BLUE, Preset.GREEN, Preset.PURPLE, Preset.AMBER,
        )

        /** Extra presets unlocked by Pro. */
        val proPresets: List<Preset> = listOf(
            Preset.TEAL, Preset.PINK, Preset.ORANGE, Preset.CYAN,
            Preset.INDIGO, Preset.MINT, Preset.ROSE, Preset.LIME,
        )

        fun fromStorage(value: String?): AccentColor {
            if (value == null) return Default
            Preset.entries.firstOrNull { it.name == value }?.let { return it }
            return normalizeHex(value)?.let { Custom(it) } ?: Default
        }

        /** Returns a canonical "#RRGGBB" string if [raw] is a valid hex color, else null. */
        fun normalizeHex(raw: String?): String? {
            if (raw.isNullOrBlank()) return null
            val trimmed = raw.trim().removePrefix("#")
            val hex = when (trimmed.length) {
                3 -> trimmed.map { "$it$it" }.joinToString("")
                6 -> trimmed
                8 -> trimmed.substring(2) // drop alpha, force opaque
                else -> return null
            }
            if (!hex.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) return null
            return "#${hex.uppercase()}"
        }
    }
}

/** Cheap perceptual-ish luminance used to pick a legible on-accent text color. */
private fun Color.luminanceApprox(): Float = 0.299f * red + 0.587f * green + 0.114f * blue

private fun dotCalPalette(mode: DotCalThemeMode, accentColor: AccentColor = AccentColor.Default, systemDark: Boolean = false): DotCalPalette {
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
            background = Color(0xFFFFFFFF),
            primaryText = Color(0xFF101010),
            secondaryText = Color(0xFF6B6B6B),
            dimText = Color(0xFFBDBDBD),
            line = Color(0xFFE8E8E8),
            cell = Color(0xFFFFFFFF),
            calendarSurface = Color(0xFFFFFFFF),
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

private fun dotCalBootPalette(accentColor: AccentColor = AccentColor.Default): DotCalPalette {
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

private fun android.content.Context.findActivity(): android.app.Activity? {
    var ctx: android.content.Context? = this
    while (ctx is android.content.ContextWrapper) {
        if (ctx is android.app.Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private data class ProFeature(val name: String, val description: String)

private val PRO_FEATURES = listOf(
    ProFeature("Image Attachments", "Add up to 5 photos to any event"),
    ProFeature("Voice Notes", "Record audio notes on your events"),
    ProFeature("Large Widget", "Full month grid widget for your home screen"),
    ProFeature("Widget Pack Config", "Transparent widgets plus DotCal dot texture"),
    ProFeature("Date Calculator", "Calculate days between dates instantly"),
    ProFeature("Custom Accent Colors", "Extra palettes plus any custom hex color"),
    ProFeature("Quick Add", "Type 'gym every mon 7am' — we build the event"),
    ProFeature("Advanced Recurrence", "Every N weeks, nth weekday, end date or count"),
    ProFeature("App Lock & Private Vault", "PIN lock plus hidden events and tasks"),
    ProFeature("Event & Task Templates", "Save presets and reuse them from the + button"),
)

@Composable
private fun PaywallScreen(
    viewModel: DotCalViewModel,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val productDetails by viewModel.productDetails.collectAsStateWithLifecycle()
    val billingState by viewModel.billingState.collectAsStateWithLifecycle()
    val purchaseResult by viewModel.purchaseResult.collectAsStateWithLifecycle()
    var purchasing by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(purchaseResult) {
        when (val result = purchaseResult) {
            is ProManager.PurchaseResult.Success -> {
                purchasing = false
                showSuccess = true
            }
            is ProManager.PurchaseResult.Cancelled -> {
                purchasing = false
                viewModel.clearPurchaseResult()
            }
            is ProManager.PurchaseResult.Error -> {
                purchasing = false
                Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                viewModel.clearPurchaseResult()
            }
            null -> Unit
        }
    }
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            delay(1500)
            onDismiss()
            viewModel.clearPurchaseResult()
        }
    }

    if (showSuccess) {
        Column(
            modifier = Modifier.fillMaxSize().background(palette.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = palette.accent,
                modifier = Modifier.size(64.dp),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("You're Pro!", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
        return
    }

    val connected = billingState is ProManager.BillingConnectionState.Connected
    val price = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
        ?: stringResource(R.string.pro_price_fallback)
    val priceIsEstimate = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        // ① Top bar: close only, no title.
        Box(modifier = Modifier.fillMaxWidth().height(28.dp).padding(horizontal = 8.dp)) {
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterStart).size(40.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
        }

        // ② Header — icon + title side by side, no vertical gap.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(30.dp)),
            )
            Text(
                "DotCal Pro",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-18).dp),
            )
        }
        Spacer(modifier = Modifier.height(0.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {

        // ③ Feature list — bordered card.
        Column(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(1.dp, palette.eventCardBorder, RoundedCornerShape(14.dp))
                .background(palette.eventCardSurface)
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            PRO_FEATURES.forEachIndexed { index, feature ->
                PaywallFeatureRow(feature = feature, palette = palette)
                if (index != PRO_FEATURES.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        // ⑤ Price row.
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "Pay once. Own it forever.",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                if (priceIsEstimate) "No subscription. Price estimate." else "No subscription.",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 28.dp),
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        // ⑥ Buy button — accent bg, rounded, full width.
        val buyEnabled = connected && !purchasing
        Box(
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (buyEnabled) palette.accent else palette.disabledText)
                .noRippleClickable(enabled = buyEnabled) {
                    val activity = context.findActivity()
                    if (activity != null) {
                        purchasing = true
                        viewModel.purchasePro(activity)
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            if (purchasing) {
                CircularProgressIndicator(color = palette.onAccent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            } else {
                Text("Buy Pro - $price", color = palette.onAccent, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // ⑦ Restore purchase.
        Text(
            "Restore Purchase",
            color = palette.secondaryText,
            fontFamily = mono,
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .noRippleClickable {
                    viewModel.restorePro { restored ->
                        val message = if (restored) {
                            "Purchase restored — enjoy DotCal Pro!"
                        } else {
                            "No previous purchase found on this account"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
                .padding(8.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


@Composable
private fun PaywallFeatureRow(feature: ProFeature, palette: DotCalPalette) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            Icons.Default.Check,
            contentDescription = null,
            tint = palette.accent,
            modifier = Modifier.size(16.dp).padding(top = 1.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(feature.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(feature.description, color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp, lineHeight = 13.sp)
        }
    }
}

private enum class SearchTypeFilter(val label: String) { All("All"), Events("Events"), Tasks("Tasks") }

private enum class SearchDatePreset(val label: String) {
    AnyTime("Any time"), Upcoming("Upcoming"), Past("Past"), ThisMonth("This month")
}

/**
 * Global Search (FREE): full-screen overlay to find events + tasks by text, with in-memory
 * type / date-preset / calendar facets over the ViewModel's [searchResults]. Reuses [EventRow]
 * for events and a lightweight local row for tasks. No Pro gate. Mirrors the QuickAdd overlay.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchScreen(
    palette: DotCalPalette,
    results: List<CalendarEvent>,
    accounts: List<CalendarAccount>,
    onQueryChange: (String) -> Unit,
    onOpenEvent: (CalendarEvent) -> Unit,
    onOpenTask: (CalendarEvent) -> Unit,
    onBack: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf(SearchTypeFilter.All) }
    var datePreset by remember { mutableStateOf(SearchDatePreset.AnyTime) }
    var accountId by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }
    val trimmed = query.trim()

    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }
    // Debounced query — re-run the DAO search a beat after typing stops.
    LaunchedEffect(query) {
        delay(220)
        onQueryChange(query)
    }

    // In-memory facet filtering over the already-fetched results (cheap; no re-query).
    val zone = ZoneId.systemDefault()
    val todayStartMs = LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
    val month = YearMonth.now()
    val monthStartMs = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val monthEndMs = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val filtered = results.filter { item ->
        val typeOk = when (typeFilter) {
            SearchTypeFilter.All -> true
            SearchTypeFilter.Events -> item.isTask == 0
            SearchTypeFilter.Tasks -> item.isTask == 1
        }
        val dateOk = when (datePreset) {
            SearchDatePreset.AnyTime -> true
            SearchDatePreset.Upcoming -> item.startTimeMs >= todayStartMs
            SearchDatePreset.Past -> item.startTimeMs < todayStartMs
            SearchDatePreset.ThisMonth -> item.startTimeMs in monthStartMs until monthEndMs
        }
        val accountOk = accountId == null || item.accountId == accountId
        typeOk && dateOk && accountOk
    }

    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        // Top bar: back + search field.
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text(
                "Search",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center),
            )
            HorizontalDivider(color = palette.line.copy(alpha = 0.55f), thickness = 1.dp, modifier = Modifier.align(Alignment.BottomCenter))
        }

        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 14.dp)) {
            CalcFieldGroup(palette) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 4.dp)) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it.replace("\n", "") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        textStyle = TextStyle(
                            color = palette.primaryText,
                            fontFamily = mono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                        ),
                        cursorBrush = SolidColor(palette.accent),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                            .padding(vertical = 16.dp),
                        decorationBox = { inner ->
                            if (query.isEmpty()) {
                                Text(
                                    "Title, location, notes…",
                                    color = palette.disabledText,
                                    fontFamily = mono,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                )
                            }
                            inner()
                        },
                    )
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = palette.secondaryText, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Facet chips: type, then date preset, then calendar.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SearchTypeFilter.values().forEach { t ->
                    SearchFilterChip(label = t.label, selected = typeFilter == t, palette = palette) { typeFilter = t }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SearchDatePreset.values().forEach { d ->
                    SearchFilterChip(label = d.label, selected = datePreset == d, palette = palette) { datePreset = d }
                }
            }
            if (accounts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SearchFilterChip(label = "All calendars", selected = accountId == null, palette = palette) { accountId = null }
                    accounts.forEach { acc ->
                        SearchFilterChip(label = acc.displayName, selected = accountId == acc.id, palette = palette) { accountId = acc.id }
                    }
                }
            }
        }

        HorizontalDivider(color = palette.line.copy(alpha = 0.4f), thickness = 1.dp)

        when {
            trimmed.isEmpty() -> SearchHintBox("Search your events and tasks", palette)
            filtered.isEmpty() -> SearchHintBox("No results for \"$trimmed\"", palette)
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                lazyItems(filtered, key = { it.id }) { item ->
                    if (item.isTask == 1) {
                        SearchTaskResultRow(task = item, palette = palette, onClick = { onOpenTask(item) })
                    } else {
                        EventRow(event = item, palette = palette, onClick = { onOpenEvent(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchFilterChip(label: String, selected: Boolean, palette: DotCalPalette, onClick: () -> Unit) {
    val bg = if (selected) palette.accent.copy(alpha = 0.16f) else palette.eventCardSurface
    val border = if (selected) palette.accent else palette.eventCardBorder
    val textColor = if (selected) palette.accent else palette.secondaryText
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .drawBehind {
                drawRoundRect(
                    color = border,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(label, color = textColor, fontFamily = mono, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, fontSize = 13.sp, maxLines = 1)
    }
}

@Composable
private fun SearchHintBox(message: String, palette: DotCalPalette) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp)
    }
}

@Composable
private fun SearchTaskResultRow(task: CalendarEvent, palette: DotCalPalette, onClick: () -> Unit) {
    val whenLabel = Instant.ofEpochMilli(task.startTimeMs).atZone(ZoneId.systemDefault()).toLocalDate()
        .format(DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", Locale.US))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.eventCardSurface)
            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(16.dp))
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("TASK • $whenLabel", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                task.title,
                color = if (task.isCompleted == 1) palette.primaryText.copy(alpha = 0.5f) else palette.primaryText,
                fontFamily = mono,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        EventCardChevron(tint = palette.eventCardChevron)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickAddScreen(
    palette: DotCalPalette,
    onBack: () -> Unit,
    onContinue: (QuickAddResult) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val trimmed = text.trim()
    // Re-parsed on every keystroke; pure and cheap.
    val parsed = remember(trimmed) { if (trimmed.isEmpty()) null else QuickAddParser.parse(trimmed) }
    val focusRequester = remember { FocusRequester() }
    val examples = listOf("Gym every mon 7am", "Lunch tomorrow noon", "Pay rent on 1st", "Standup daily 9:30am")

    fun submit() {
        parsed?.let(onContinue)
    }

    LaunchedEffect(Unit) { runCatching { focusRequester.requestFocus() } }

    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        // Top bar: back + title.
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text(
                "Quick Add",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center),
            )
            HorizontalDivider(color = palette.line.copy(alpha = 0.55f), thickness = 1.dp, modifier = Modifier.align(Alignment.BottomCenter))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 16.dp),
        ) {
            CalcSectionLabel("Describe your event", palette)
            Spacer(modifier = Modifier.height(10.dp))
            CalcFieldGroup(palette) {
                BasicTextField(
                    value = text,
                    onValueChange = { text = it.replace("\n", "") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    textStyle = TextStyle(
                        color = palette.primaryText,
                        fontFamily = mono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    ),
                    cursorBrush = SolidColor(palette.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .padding(vertical = 18.dp),
                    decorationBox = { inner ->
                        if (text.isEmpty()) {
                            Text(
                                "gym every mon 7am",
                                color = palette.disabledText,
                                fontFamily = mono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                            )
                        }
                        inner()
                    },
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (parsed != null) {
                CalcSectionLabel("Preview", palette)
                Spacer(modifier = Modifier.height(10.dp))
                CalcFieldGroup(palette) {
                    QuickAddPreviewRow("Title", parsed.title.ifBlank { "(none — add in next step)" }, palette)
                    HorizontalDivider(color = palette.line.copy(alpha = 0.4f), thickness = 1.dp)
                    QuickAddPreviewRow("When", quickAddWhenLabel(parsed), palette)
                    parsed.rrule?.let { rule ->
                        HorizontalDivider(color = palette.line.copy(alpha = 0.4f), thickness = 1.dp)
                        QuickAddPreviewRow("Repeats", quickAddRepeatLabel(rule), palette)
                    }
                }
            } else {
                CalcSectionLabel("Try one", palette)
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    examples.forEach { example ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(palette.eventCardSurface)
                                .drawBehind {
                                    drawRoundRect(
                                        color = palette.eventCardBorder,
                                        size = size,
                                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                                        style = Stroke(width = 1.dp.toPx()),
                                    )
                                }
                                .noRippleClickable { text = example }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(example, color = palette.secondaryText, fontFamily = mono, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Continue button pinned to the bottom.
        Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 16.dp)) {
            val enabled = parsed != null
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) palette.accent else palette.disabledText.copy(alpha = 0.25f))
                    .noRippleClickable(enabled = enabled) { submit() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "Continue",
                    color = if (enabled) NWhite else palette.disabledText,
                    fontFamily = mono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Composable
private fun QuickAddPreviewRow(label: String, value: String, palette: DotCalPalette) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = palette.secondaryText, fontFamily = mono, fontSize = 13.sp, modifier = Modifier.width(72.dp))
        Text(
            value,
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
        )
    }
}

private fun quickAddWhenLabel(result: QuickAddResult): String {
    val date = result.date.format(editorDateFormatter)
    val time = result.startTime
    return if (result.isAllDay || time == null) {
        "$date · All-day"
    } else {
        "$date · ${time.format(editorTimeFormatter)}"
    }
}

private fun quickAddRepeatLabel(rrule: String): String =
    RecurrenceRule.parse(rrule)?.humanLabel() ?: "Custom"

@Composable
private fun TemplatesScreen(
    palette: DotCalPalette,
    templates: List<EventTemplate>,
    onBack: () -> Unit,
    onUse: (EventTemplate) -> Unit,
    onDelete: (String) -> Unit,
) {
    var deleteTarget by remember { mutableStateOf<EventTemplate?>(null) }
    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text(
                "Templates",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center),
            )
            HorizontalDivider(color = palette.line.copy(alpha = 0.55f), thickness = 1.dp, modifier = Modifier.align(Alignment.BottomCenter))
        }
        if (templates.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No templates yet", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Open any event or task, then tap \"Save as template\" to reuse it any time.",
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                lazyItems(templates, key = { it.id }) { template ->
                    TemplateCard(
                        template = template,
                        palette = palette,
                        onUse = { onUse(template) },
                        onDelete = { deleteTarget = template },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
    deleteTarget?.let { target ->
        ConfirmDeleteDialog(
            title = "Delete template?",
            confirmLabel = "Delete",
            palette = palette,
            onDismiss = { deleteTarget = null },
            onConfirm = {
                onDelete(target.id)
                deleteTarget = null
            },
        )
    }
}

@Composable
private fun TemplateCard(
    template: EventTemplate,
    palette: DotCalPalette,
    onUse: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.eventCardSurface)
            .drawBehind {
                drawRoundRect(
                    color = palette.eventCardBorder,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            .noRippleClickable(onClick = onUse)
            .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(template.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(3.dp))
            Text(templateSummaryLabel(template), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, maxLines = 1)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete template", tint = palette.secondaryText, modifier = Modifier.size(20.dp))
        }
    }
}

private fun templateSummaryLabel(t: EventTemplate): String {
    val type = if (t.isTask) "Task" else "Event"
    val timeLabel = if (t.startMinuteOfDay == null) {
        if (t.isTask) "No time" else "All-day"
    } else {
        LocalTime.of(t.startMinuteOfDay / 60, t.startMinuteOfDay % 60).format(editorTimeFormatter)
    }
    val parts = mutableListOf(type, timeLabel)
    if (!t.isTask && t.startMinuteOfDay != null && t.durationMinutes > 0) {
        parts.add(formatDurationShort(t.durationMinutes))
    }
    t.rrule?.let { parts.add(RecurrenceRule.parse(it)?.humanLabel() ?: "Repeats") }
    return parts.joinToString(" · ")
}

private fun formatDurationShort(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "${h}h ${m}m"
        h > 0 -> "${h}h"
        else -> "${m}m"
    }
}

@Composable
private fun RecentlyDeletedScreen(
    palette: DotCalPalette,
    items: List<DeletedSnapshot>,
    onBack: () -> Unit,
    onRestore: (String) -> Unit,
    onPurge: (String) -> Unit,
    onEmptyAll: () -> Unit,
) {
    var purgeTarget by remember { mutableStateOf<DeletedSnapshot?>(null) }
    var confirmEmpty by remember { mutableStateOf(false) }
    var openRowId by remember { mutableStateOf<String?>(null) }
    val now = System.currentTimeMillis()

    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text(
                "Recently Deleted",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center),
            )
            if (items.isNotEmpty()) {
                Text(
                    "Empty",
                    color = palette.accent,
                    fontFamily = mono,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 18.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { confirmEmpty = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                )
            }
            HorizontalDivider(color = palette.line.copy(alpha = 0.55f), thickness = 1.dp, modifier = Modifier.align(Alignment.BottomCenter))
        }

        if (items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Nothing here",
                        color = palette.secondaryText,
                        fontFamily = mono,
                        fontSize = 16.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Deleted events and tasks show up here for 30 days, so you can bring them back.",
                        color = palette.dimText,
                        fontFamily = mono,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    Text(
                        "Swipe left to restore or delete. Kept for 30 days.",
                        color = palette.dimText,
                        fontFamily = mono,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 22.dp, end = 22.dp, top = 16.dp, bottom = 6.dp),
                    )
                }
                lazyItems(items, key = { it.event.id }) { snap ->
                    SwipeableDeletedRow(
                        snapshot = snap,
                        nowMs = now,
                        palette = palette,
                        isOpen = openRowId == snap.event.id,
                        onOpen = { openRowId = snap.event.id },
                        onClose = { if (openRowId == snap.event.id) openRowId = null },
                        onRestore = {
                            openRowId = null
                            onRestore(snap.event.id)
                        },
                        onDelete = { purgeTarget = snap },
                    )
                    HorizontalDivider(color = palette.line.copy(alpha = 0.4f), thickness = 1.dp)
                }
            }
        }
    }

    purgeTarget?.let { snap ->
        ConfirmDeleteDialog(
            title = "Delete permanently?",
            confirmLabel = "Delete",
            palette = palette,
            onDismiss = { purgeTarget = null },
            onConfirm = {
                onPurge(snap.event.id)
                purgeTarget = null
            },
        )
    }
    if (confirmEmpty) {
        ConfirmDeleteDialog(
            title = "Empty Recently Deleted?",
            confirmLabel = "Empty",
            palette = palette,
            onDismiss = { confirmEmpty = false },
            onConfirm = {
                onEmptyAll()
                confirmEmpty = false
            },
        )
    }
}

@Composable
private fun SwipeableDeletedRow(
    snapshot: DeletedSnapshot,
    nowMs: Long,
    palette: DotCalPalette,
    isOpen: Boolean,
    onOpen: () -> Unit,
    onClose: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
) {
    val event = snapshot.event
    val density = LocalDensity.current
    val actionButtonWidth = 92.dp
    val revealPx = with(density) { (actionButtonWidth * 2).toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Snap shut when another row is opened (external close).
    LaunchedEffect(isOpen) {
        if (!isOpen && offsetX.value != 0f) offsetX.animateTo(0f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .background(palette.background),
    ) {
        // Actions revealed behind the row: Restore (brand neutral) + Delete (red).
        Row(modifier = Modifier.matchParentSize(), horizontalArrangement = Arrangement.End) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(actionButtonWidth)
                    .background(palette.primaryText)
                    .clickable { onRestore() },
                contentAlignment = Alignment.Center,
            ) {
                Text("Restore", color = palette.background, fontFamily = mono, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(actionButtonWidth)
                    .background(Color(0xFFFF3B30))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center,
            ) {
                Text("Delete", color = Color.White, fontFamily = mono, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
        // Foreground content — drag left to reveal actions, tap to close when open.
        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .background(palette.background)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch { offsetX.snapTo((offsetX.value + delta).coerceIn(-revealPx, 0f)) }
                    },
                    onDragStopped = {
                        val target = if (offsetX.value < -revealPx / 2f) -revealPx else 0f
                        scope.launch { offsetX.animateTo(target) }
                        if (target != 0f) onOpen() else onClose()
                    },
                )
                .noRippleClickable(enabled = isOpen) { onClose() }
                .padding(horizontal = 22.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title.ifBlank { "(No title)" },
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "${deletedWhenLabel(event)} · deleted ${deletedAgoLabel(snapshot.deletedAtMs, nowMs)}",
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

/** Human-readable "when" line for a deleted event or task snapshot. */
private fun deletedWhenLabel(event: CalendarEvent): String {
    val prefix = if (event.isTask == 1) "Task" else "Event"
    // Tasks with no due date store startTimeMs = 0.
    if (event.isTask == 1 && event.startTimeMs <= 0L) return "$prefix • No due date"
    val zone = runCatching { java.time.ZoneId.of(event.timeZone) }.getOrDefault(java.time.ZoneId.systemDefault())
    val start = java.time.Instant.ofEpochMilli(event.startTimeMs).atZone(zone)
    val dateFmt = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    val timeFmt = java.time.format.DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val date = start.format(dateFmt)
    return if (event.isAllDay == 1) "$prefix • $date" else "$prefix • $date, ${start.format(timeFmt)}"
}

/** Relative "deleted X ago" phrasing from a deletion timestamp. */
private fun deletedAgoLabel(deletedAtMs: Long, nowMs: Long): String {
    val diff = (nowMs - deletedAtMs).coerceAtLeast(0L)
    val minutes = diff / 60_000L
    val hours = diff / 3_600_000L
    val days = diff / 86_400_000L
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        days < 30 -> "${days}d ago"
        else -> "30d ago"
    }
}

@Composable
private fun DateCalculatorScreen(
    palette: DotCalPalette,
    onBack: () -> Unit,
    calcViewModel: DateCalculatorViewModel = viewModel(),
) {
    val mode by calcViewModel.mode.collectAsStateWithLifecycle()
    val fromDate by calcViewModel.fromDate.collectAsStateWithLifecycle()
    val toDate by calcViewModel.toDate.collectAsStateWithLifecycle()
    val startDate by calcViewModel.startDate.collectAsStateWithLifecycle()
    val daysCount by calcViewModel.daysCount.collectAsStateWithLifecycle()
    val isSubtract by calcViewModel.isSubtract.collectAsStateWithLifecycle()
    val result by calcViewModel.result.collectAsStateWithLifecycle()

    var picker by remember { mutableStateOf<CalcDateField?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        // ① Top bar: back + title.
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text(
                "Date Calculator",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center),
            )
            HorizontalDivider(color = palette.line.copy(alpha = 0.55f), thickness = 1.dp, modifier = Modifier.align(Alignment.BottomCenter))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 16.dp),
        ) {
            // ② Mode segmented control.
            TwoOptionSegmentedControl(
                options = listOf("Days Between", "Add / Subtract"),
                selectedIndex = if (mode == DateCalculatorViewModel.Mode.DAYS_BETWEEN) 0 else 1,
                palette = palette,
                onSelected = {
                    calcViewModel.setMode(
                        if (it == 0) DateCalculatorViewModel.Mode.DAYS_BETWEEN else DateCalculatorViewModel.Mode.ADD_SUBTRACT,
                    )
                },
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (mode == DateCalculatorViewModel.Mode.DAYS_BETWEEN) {
                CalcSectionLabel("Date range", palette)
                Spacer(modifier = Modifier.height(10.dp))
                CalcFieldGroup(palette) {
                    CalcDateRow("From", fromDate, palette) { picker = CalcDateField.From }
                    HorizontalDivider(color = palette.line.copy(alpha = 0.4f), thickness = 1.dp)
                    CalcDateRow("To", toDate, palette) { picker = CalcDateField.To }
                }
                Spacer(modifier = Modifier.height(24.dp))
                (result as? DateCalculatorViewModel.CalculatorResult.DaysBetween)?.let { r ->
                    CalcSectionLabel("Result", palette)
                    Spacer(modifier = Modifier.height(10.dp))
                    CalcResultCard(palette) {
                        CalcResultHero("${r.totalDays}", if (r.totalDays == 1) "day total" else "days total", palette)
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = palette.eventCardBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        CalcResultLine("Working days (Mon-Fri)", "${r.workingDays}", palette)
                        CalcResultLine("Weekends", "${r.weekends}", palette)
                    }
                }
            } else {
                CalcSectionLabel("Start date", palette)
                Spacer(modifier = Modifier.height(10.dp))
                CalcFieldGroup(palette) {
                    CalcDateRow("Start date", startDate, palette) { picker = CalcDateField.Start }
                }
                Spacer(modifier = Modifier.height(24.dp))

                CalcSectionLabel("Operation", palette)
                Spacer(modifier = Modifier.height(10.dp))
                TwoOptionSegmentedControl(
                    options = listOf("Add", "Subtract"),
                    selectedIndex = if (isSubtract) 1 else 0,
                    palette = palette,
                    onSelected = { calcViewModel.setSubtract(it == 1) },
                )
                Spacer(modifier = Modifier.height(24.dp))

                CalcSectionLabel("Number of days", palette)
                Spacer(modifier = Modifier.height(10.dp))
                CalcDaysStepper(
                    days = daysCount,
                    palette = palette,
                    onChange = { calcViewModel.setDaysCount(it) },
                )
                Spacer(modifier = Modifier.height(24.dp))

                (result as? DateCalculatorViewModel.CalculatorResult.AddSubtractResult)?.let { r ->
                    CalcSectionLabel("Result", palette)
                    Spacer(modifier = Modifier.height(10.dp))
                    CalcResultCard(palette) {
                        Text(
                            "$daysCount ${if (daysCount == 1) "day" else "days"} ${if (isSubtract) "before" else "after"} start date",
                            color = palette.secondaryText,
                            fontFamily = mono,
                            fontSize = 13.sp,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            r.formattedDate,
                            color = palette.primaryText,
                            fontFamily = mono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                        )
                    }
                }
            }
        }
    }

    picker?.let { field ->
        val current = when (field) {
            CalcDateField.From -> fromDate
            CalcDateField.To -> toDate
            CalcDateField.Start -> startDate
        } ?: LocalDate.now()
        DateTimeChoiceSheet(
            title = when (field) {
                CalcDateField.From -> "From"
                CalcDateField.To -> "To"
                CalcDateField.Start -> "Start date"
            },
            selectedDate = current,
            selectedTime = LocalTime.of(9, 0),
            minDate = null,
            includeTime = false,
            palette = palette,
            onDismiss = { picker = null },
            onSelected = { date, _ ->
                when (field) {
                    CalcDateField.From -> calcViewModel.setFromDate(date)
                    CalcDateField.To -> calcViewModel.setToDate(date)
                    CalcDateField.Start -> calcViewModel.setStartDate(date)
                }
                picker = null
            },
        )
    }
}

private enum class CalcDateField { From, To, Start }

@Composable
private fun CalcDateRow(label: String, date: LocalDate?, palette: DotCalPalette, onClick: () -> Unit) {
    val formatter = remember { java.time.format.DateTimeFormatter.ofPattern("EEE, dd MMM yyyy", java.util.Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .noRippleClickable(onClick = onClick)
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = palette.primaryText, fontFamily = mono, fontSize = 16.sp)
        Text(
            date?.format(formatter)?.uppercase(java.util.Locale.getDefault()) ?: "Select",
            color = if (date != null) palette.accent else palette.secondaryText,
            fontFamily = mono,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun CalcResultCard(palette: DotCalPalette, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.eventCardSurface)
            .drawBehind {
                drawRect(color = palette.eventCardBorder, size = size, style = Stroke(width = 1.dp.toPx()))
            }
            .padding(20.dp),
        content = content,
    )
}

@Composable
private fun CalcResultLine(label: String, value: String, palette: DotCalPalette) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp)
        Text(value, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun CalcSectionLabel(text: String, palette: DotCalPalette) {
    Text(
        text.uppercase(java.util.Locale.getDefault()),
        color = palette.secondaryText,
        fontFamily = mono,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        letterSpacing = 1.5.sp,
    )
}

@Composable
private fun CalcFieldGroup(palette: DotCalPalette, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.eventCardSurface)
            .drawBehind {
                drawRoundRect(
                    color = palette.eventCardBorder,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx(), 14.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            .padding(horizontal = 18.dp),
        content = content,
    )
}

@Composable
private fun CalcResultHero(number: String, caption: String, palette: DotCalPalette) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(number, color = palette.accent, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 40.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            caption,
            color = palette.secondaryText,
            fontFamily = mono,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 6.dp),
        )
    }
}

@Composable
private fun CalcDaysStepper(days: Int, palette: DotCalPalette, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalcStepperButton("−", palette) { onChange((days - 1).coerceAtLeast(0)) }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .drawBehind {
                    drawRoundRect(
                        color = palette.textFieldBorder,
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            BasicTextField(
                value = if (days == 0) "" else days.toString(),
                onValueChange = { text ->
                    onChange(text.filter { it.isDigit() }.take(6).toIntOrNull() ?: 0)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = TextStyle(
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                ),
                cursorBrush = SolidColor(palette.accent),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.Center) {
                        if (days == 0) {
                            Text("0", color = palette.disabledText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        }
                        inner()
                    }
                },
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        CalcStepperButton("+", palette) { onChange(days + 1) }
    }
}

@Composable
private fun CalcStepperButton(symbol: String, palette: DotCalPalette, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(palette.accent)
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(symbol, color = palette.onAccent, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 26.sp)
    }
}

@Composable
private fun TwoOptionSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    palette: DotCalPalette,
    onSelected: (Int) -> Unit,
) {
    val segmentShape = RoundedCornerShape(28.dp)
    val segmentBorder = palette.disabledText.copy(alpha = if (palette.isDark) 0.35f else 0.45f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .clip(segmentShape)
            .background(palette.topBarSurface)
            .drawBehind {
                drawRoundRect(
                    color = segmentBorder,
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) palette.segmentSelected else Color.Transparent)
                    .noRippleClickable { onSelected(index) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    fontFamily = mono,
                    color = if (isSelected) palette.primaryText else palette.secondaryText,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
