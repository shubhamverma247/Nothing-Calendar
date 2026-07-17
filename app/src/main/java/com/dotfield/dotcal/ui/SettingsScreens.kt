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
import androidx.compose.material.icons.filled.Apps
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.Font
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


@Composable
internal fun SettingsPreview(
    themeMode: DotCalThemeMode,
    accentColor: AccentColor,
    appFont: AppFont,
    palette: DotCalPalette,
    screen: SettingsScreen,
    onBack: () -> Unit,
    onScreenChange: (SettingsScreen) -> Unit,
    onThemeSelected: (DotCalThemeMode) -> Unit,
    onAccentSelected: (AccentColor) -> Unit,
    onAppFontSelected: (AppFont) -> Unit,
    syncEnabled: Boolean,
    syncIntervalMins: Int,
    syncMetadata: List<SyncMetadata>,
    isSyncing: Boolean,
    birthdayEnabled: Boolean,
    defaultReminderMinutes: Int?,
    defaultEventDurationMinutes: Int,
    defaultCalendarTab: CalendarTab,
    showWeekNumbers: Boolean,
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
    onDefaultEventDurationSelected: (Int) -> Unit,
    onDefaultViewSelected: (CalendarTab) -> Unit,
    onShowWeekNumbersChange: (Boolean) -> Unit,
    onDefaultAllDayReminderTimeSelected: (LocalTime) -> Unit,
    onWeekStartSelected: (WeekStartOption) -> Unit,
    onWidgetTransparentChange: (Boolean) -> Unit,
    onWidgetDotTextureChange: (Boolean) -> Unit,
    onBirthdayEnabledChange: (Boolean) -> Unit,
    onAddHolidayCountry: (HolidayCountryUiItem) -> Unit,
    onRemoveHolidayCountry: (HolidayCountryUiItem) -> Unit,
    onRateDotCal: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onMoreApps: () -> Unit,
    onRequestCalendarAccess: () -> Unit,
    onAddAccount: () -> Unit,
    isPro: Boolean,
    onDotCalPro: () -> Unit,
    onRestorePurchase: () -> Unit,
    onDateCalculator: () -> Unit,
    onTimeInsights: () -> Unit,
    onAppPrivacy: () -> Unit,
    onSetAppLockPin: (String, (Result<Unit>) -> Unit) -> Unit,
    onVerifyAppLockPin: (String, (Boolean) -> Unit) -> Unit,
    onSetAppLockEnabled: (Boolean) -> Unit,
    onDisableAppLock: () -> Unit,
    onClearAppLockPin: () -> Unit,
    onRestorePrivateEvent: (String) -> Unit,
    onRecentlyDeleted: () -> Unit,
    onTemplates: () -> Unit,
    onCalendarSets: () -> Unit,
    onShiftPatterns: () -> Unit,
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
            appFont = appFont,
            palette = palette,
            onBack = onBack,
            onThemeSelected = onThemeSelected,
            onAccentSelected = onAccentSelected,
            onAppFontSelected = onAppFontSelected,
            onThemeSettings = { onScreenChange(SettingsScreen.Theme) },
            onSyncSettings = { onScreenChange(SettingsScreen.Sync) },
            onCalendarPreferencesSettings = { onScreenChange(SettingsScreen.CalendarPreferences) },
            onReminderDefaultsSettings = { onScreenChange(SettingsScreen.ReminderDefaults) },
            onWidgetSettings = { onScreenChange(SettingsScreen.Widgets) },
            onDataSettings = { onScreenChange(SettingsScreen.DataRestore) },
            syncEnabled = syncEnabled,
            syncIntervalMins = syncIntervalMins,
            syncMetadata = syncMetadata,
            isSyncing = isSyncing,
            birthdayEnabled = birthdayEnabled,
            defaultReminderMinutes = defaultReminderMinutes,
            defaultEventDurationMinutes = defaultEventDurationMinutes,
            defaultCalendarTab = defaultCalendarTab,
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
            onSyncNow = onSyncNow,
            onAccountVisibilityChange = onAccountVisibilityChange,
            onSyncEnabledChange = onSyncEnabledChange,
            onSyncIntervalSelected = onSyncIntervalSelected,
            onDefaultReminderSelected = onDefaultReminderSelected,
            onDefaultEventDurationSelected = onDefaultEventDurationSelected,
            onDefaultViewSelected = onDefaultViewSelected,
            onShowWeekNumbersChange = onShowWeekNumbersChange,
            onDefaultAllDayReminderTimeSelected = onDefaultAllDayReminderTimeSelected,
            onWeekStartSelected = onWeekStartSelected,
            onWidgetTransparentChange = onWidgetTransparentChange,
            onWidgetDotTextureChange = onWidgetDotTextureChange,
            onBirthdayEnabledChange = onBirthdayEnabledChange,
            onGlobalHolidays = { onScreenChange(SettingsScreen.GlobalHolidays) },
            onPrivacyPolicy = { onScreenChange(SettingsScreen.PrivacyPolicy) },
            onRateDotCal = onRateDotCal,
            onCheckForUpdates = onCheckForUpdates,
            onMoreApps = onMoreApps,
            onRequestCalendarAccess = onRequestCalendarAccess,
            onAddAccount = { onScreenChange(SettingsScreen.AddAccount) },
            isPro = isPro,
            onDotCalPro = onDotCalPro,
            onRestorePurchase = onRestorePurchase,
            onDateCalculator = onDateCalculator,
            onTimeInsights = onTimeInsights,
            onAppPrivacy = onAppPrivacy,
            onRecentlyDeleted = onRecentlyDeleted,
            onTemplates = onTemplates,
            onCalendarSets = onCalendarSets,
            onShiftPatterns = onShiftPatterns,
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
                appFont = appFont,
                palette = palette,
                isPro = isPro,
                onBack = { onScreenChange(SettingsScreen.Root) },
                onThemeSelected = onThemeSelected,
                onAccentSelected = onAccentSelected,
                onAppFontSelected = onAppFontSelected,
                onRequestPro = onDotCalPro,
            )
        }
        AnimatedVisibility(
            visible = screen == SettingsScreen.CalendarPreferences,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
        ) {
            CalendarPreferencesSettings(
                defaultCalendarTab = defaultCalendarTab,
                showWeekNumbers = showWeekNumbers,
                birthdayEnabled = birthdayEnabled,
                weekStartOption = weekStartOption,
                holidayCountries = holidayCountries,
                palette = palette,
                onBack = { onScreenChange(SettingsScreen.Root) },
                onDefaultViewSelected = onDefaultViewSelected,
                onShowWeekNumbersChange = onShowWeekNumbersChange,
                onBirthdayEnabledChange = onBirthdayEnabledChange,
                onWeekStartSelected = onWeekStartSelected,
                onGlobalHolidays = { onScreenChange(SettingsScreen.GlobalHolidays) },
            )
        }
        AnimatedVisibility(
            visible = screen == SettingsScreen.ReminderDefaults,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
        ) {
            ReminderDefaultsSettings(
                defaultReminderMinutes = defaultReminderMinutes,
                defaultEventDurationMinutes = defaultEventDurationMinutes,
                defaultAllDayReminderTime = defaultAllDayReminderTime,
                palette = palette,
                onBack = { onScreenChange(SettingsScreen.Root) },
                onDefaultReminderSelected = onDefaultReminderSelected,
                onDefaultEventDurationSelected = onDefaultEventDurationSelected,
                onDefaultAllDayReminderTimeSelected = onDefaultAllDayReminderTimeSelected,
            )
        }
        AnimatedVisibility(
            visible = screen == SettingsScreen.Widgets,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
        ) {
            WidgetSettings(
                widgetTransparent = widgetTransparent,
                widgetDotTexture = widgetDotTexture,
                isPro = isPro,
                palette = palette,
                onBack = { onScreenChange(SettingsScreen.Root) },
                onWidgetTransparentChange = onWidgetTransparentChange,
                onWidgetDotTextureChange = onWidgetDotTextureChange,
            )
        }
        AnimatedVisibility(
            visible = screen == SettingsScreen.DataRestore,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
        ) {
            DataRestoreSettings(
                palette = palette,
                onBack = { onScreenChange(SettingsScreen.Root) },
                onExportIcs = onExportIcs,
                onImportIcs = onImportIcs,
                onBackup = onBackup,
                onRestore = onRestore,
                onRecentlyDeleted = onRecentlyDeleted,
            )
        }
        AnimatedVisibility(
            visible = screen == SettingsScreen.Sync,
            enter = slideInHorizontally(animationSpec = tween(220, easing = FastOutSlowInEasing), initialOffsetX = { it }),
            exit = slideOutHorizontally(animationSpec = tween(200, easing = FastOutSlowInEasing), targetOffsetX = { it }),
            modifier = Modifier.fillMaxSize().background(palette.calendarSurface),
        ) {
            SyncSettings(
                syncEnabled = syncEnabled,
                syncIntervalMins = syncIntervalMins,
                syncMetadata = syncMetadata,
                isSyncing = isSyncing,
                palette = palette,
                onBack = { onScreenChange(SettingsScreen.Root) },
                onSyncEnabledChange = onSyncEnabledChange,
                onSyncIntervalSelected = onSyncIntervalSelected,
                onSyncNow = onSyncNow,
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
internal fun SettingsRoot(
    themeMode: DotCalThemeMode,
    accentColor: AccentColor,
    appFont: AppFont,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onThemeSelected: (DotCalThemeMode) -> Unit,
    onAccentSelected: (AccentColor) -> Unit,
    onAppFontSelected: (AppFont) -> Unit,
    onThemeSettings: () -> Unit,
    onSyncSettings: () -> Unit,
    onCalendarPreferencesSettings: () -> Unit,
    onReminderDefaultsSettings: () -> Unit,
    onWidgetSettings: () -> Unit,
    onDataSettings: () -> Unit,
    syncEnabled: Boolean,
    syncIntervalMins: Int,
    syncMetadata: List<SyncMetadata>,
    isSyncing: Boolean,
    birthdayEnabled: Boolean,
    defaultReminderMinutes: Int?,
    defaultEventDurationMinutes: Int,
    defaultCalendarTab: CalendarTab,
    showWeekNumbers: Boolean,
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
    onDefaultEventDurationSelected: (Int) -> Unit,
    onDefaultViewSelected: (CalendarTab) -> Unit,
    onShowWeekNumbersChange: (Boolean) -> Unit,
    onDefaultAllDayReminderTimeSelected: (LocalTime) -> Unit,
    onWeekStartSelected: (WeekStartOption) -> Unit,
    onWidgetTransparentChange: (Boolean) -> Unit,
    onWidgetDotTextureChange: (Boolean) -> Unit,
    onBirthdayEnabledChange: (Boolean) -> Unit,
    onGlobalHolidays: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    onRateDotCal: () -> Unit,
    onCheckForUpdates: () -> Unit,
    onMoreApps: () -> Unit,
    onRequestCalendarAccess: () -> Unit,
    onAddAccount: () -> Unit,
    isPro: Boolean,
    onDotCalPro: () -> Unit,
    onRestorePurchase: () -> Unit,
    onDateCalculator: () -> Unit,
    onTimeInsights: () -> Unit,
    onAppPrivacy: () -> Unit,
    onRecentlyDeleted: () -> Unit,
    onTemplates: () -> Unit,
    onCalendarSets: () -> Unit,
    onShiftPatterns: () -> Unit,
    onExportIcs: () -> Unit,
    onImportIcs: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val showCompactHeader = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 96
    Box(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 150.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                SettingsNothingHero(
                    palette = palette,
                )
            }
            item {
                SettingsPanel(title = "Accounts", palette = palette) {
                    SettingsIconMenuRow(
                        title = "Calendar Accounts",
                        value = calendarAccountsLabel(accounts, hasCalendarPermission),
                        icon = Icons.Default.CalendarMonth,
                        palette = palette,
                        onClick = onRequestCalendarAccess,
                    )
                }
            }
            item {
                SettingsPanel(title = "Settings", palette = palette) {
                    SettingsIconMenuRow(
                        title = "Calendar Preferences",
                        value = defaultCalendarTab.label,
                        icon = Icons.Default.CalendarMonth,
                        palette = palette,
                        onClick = onCalendarPreferencesSettings,
                    )
                    SettingsContentDivider(palette)
                    SettingsIconMenuRow(
                        title = "Reminder Defaults",
                        value = reminderLabel(defaultReminderMinutes),
                        icon = Icons.Default.Notifications,
                        palette = palette,
                        onClick = onReminderDefaultsSettings,
                    )
                    SettingsContentDivider(palette)
                    SettingsIconMenuRow(
                        title = "Appearance",
                        value = "${themeMode.label} / ${accentColor.label}",
                        icon = Icons.Default.AutoAwesome,
                        palette = palette,
                        onClick = onThemeSettings,
                    )
                    SettingsContentDivider(palette)
                    SettingsIconMenuRow(
                        title = "Widgets",
                        value = if (widgetTransparent) "Transparent" else "Default",
                        icon = Icons.Default.Widgets,
                        palette = palette,
                        onClick = onWidgetSettings,
                    )
                    SettingsContentDivider(palette)
                    SettingsIconMenuRow(
                        title = "App Lock & Private Vault",
                        value = if (isPro) "Active" else "",
                        icon = Icons.Default.Lock,
                        isProLocked = !isPro,
                        palette = palette,
                        onClick = onAppPrivacy,
                    )
                    SettingsContentDivider(palette)
                    SettingsIconMenuRow(
                        title = "Sync",
                        value = if (syncEnabled) syncIntervalLabel(syncIntervalMins) else "Off",
                        icon = Icons.Default.AccessTime,
                        palette = palette,
                        onClick = onSyncSettings,
                    )
                    SettingsContentDivider(palette)
                    SettingsIconMenuRow(
                        title = "Data & Restore",
                        value = "Local",
                        icon = Icons.AutoMirrored.Filled.Article,
                        palette = palette,
                        onClick = onDataSettings,
                    )
                }
            }
            item {
                SettingsToolsPanel(
                    palette = palette,
                    isPro = isPro,
                    onTimeInsights = onTimeInsights,
                    onDateCalculator = onDateCalculator,
                )
            }
            item {
                SettingsPanel(title = "About", palette = palette) {
                    SettingsIconMenuRow(title = "Check for updates", value = "", icon = Icons.Default.AutoAwesome, palette = palette, onClick = onCheckForUpdates)
                    SettingsContentDivider(palette)
                    SettingsIconMenuRow(title = "Privacy Policy", value = "", icon = Icons.Default.Description, palette = palette, onClick = onPrivacyPolicy)
                    SettingsContentDivider(palette)
                    SettingsIconMenuRow(title = "Rate DotCal", value = "", icon = Icons.Default.Star, palette = palette, onClick = onRateDotCal)
                    SettingsContentDivider(palette)
                    SettingsIconMenuRow(title = "More apps from us", value = "DotFiles — File Manager", icon = Icons.Default.Apps, palette = palette, onClick = onMoreApps)
                    SettingsContentDivider(palette)
                    SettingsIconMenuRow(title = "Send Feedback", value = "", icon = Icons.Default.Edit, palette = palette, onClick = {
                        context.startActivity(
                            Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:dotfieldstudio@gmail.com?subject=DotCal%20Feedback")
                            }
                        )
                    })
                    SettingsContentDivider(palette)
                    SettingsIconMenuRow(title = "Version", value = BuildConfig.VERSION_NAME, icon = Icons.Default.Description, palette = palette, showChevron = false, onClick = {})
                }
            }
            item {
                SettingsProCard(
                    isPro = isPro,
                    palette = palette,
                    onClick = onDotCalPro,
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        if (showCompactHeader) {
            SettingsCompactHeader(palette = palette, onBack = onBack, title = "Settings", showBack = false)
        }
    }
}

@Composable
private fun CalendarPreferencesSettings(
    defaultCalendarTab: CalendarTab,
    showWeekNumbers: Boolean,
    birthdayEnabled: Boolean,
    weekStartOption: WeekStartOption,
    holidayCountries: List<HolidayCountryUiItem>,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onDefaultViewSelected: (CalendarTab) -> Unit,
    onShowWeekNumbersChange: (Boolean) -> Unit,
    onBirthdayEnabledChange: (Boolean) -> Unit,
    onWeekStartSelected: (WeekStartOption) -> Unit,
    onGlobalHolidays: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(palette.calendarSurface).padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            SettingsLargeHeader(palette = palette, onBack = onBack, title = "Calendar Preferences")
        }
        item {
            SettingsPanel(title = "Calendar", palette = palette, framed = false) {
                SettingsWeekStartRow(
                    selectedOption = weekStartOption,
                    palette = palette,
                    onWeekStartSelected = onWeekStartSelected,
                )
                SettingsContentDivider(palette)
                SettingsDefaultViewRow(
                    selectedTab = defaultCalendarTab,
                    palette = palette,
                    onViewSelected = onDefaultViewSelected,
                )
                SettingsContentDivider(palette)
                SettingsToggleRow(
                    title = "Week numbers",
                    subtitle = "Show ISO week labels in Month and Week",
                    checked = showWeekNumbers,
                    palette = palette,
                    onCheckedChange = onShowWeekNumbersChange,
                )
                SettingsContentDivider(palette)
                SettingsToggleRow(
                    title = "Birthday calendar",
                    subtitle = "Import contacts' birthdays",
                    checked = birthdayEnabled,
                    palette = palette,
                    onCheckedChange = onBirthdayEnabledChange,
                )
                SettingsContentDivider(palette)
                SettingsMenuRow(
                    title = "Global Holidays",
                    value = selectedHolidayCountriesLabel(holidayCountries),
                    palette = palette,
                    onClick = onGlobalHolidays,
                )
            }
        }
    }
}

@Composable
private fun ReminderDefaultsSettings(
    defaultReminderMinutes: Int?,
    defaultEventDurationMinutes: Int,
    defaultAllDayReminderTime: LocalTime,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onDefaultReminderSelected: (Int?) -> Unit,
    onDefaultEventDurationSelected: (Int) -> Unit,
    onDefaultAllDayReminderTimeSelected: (LocalTime) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(palette.calendarSurface).padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            SettingsLargeHeader(palette = palette, onBack = onBack, title = "Reminder Defaults")
        }
        item {
            SettingsPanel(title = "Defaults", palette = palette, framed = false) {
                SettingsDefaultReminderRow(
                    selectedMinutes = defaultReminderMinutes,
                    palette = palette,
                    onReminderSelected = onDefaultReminderSelected,
                )
                SettingsContentDivider(palette)
                SettingsDefaultEventDurationRow(
                    selectedMinutes = defaultEventDurationMinutes,
                    palette = palette,
                    onDurationSelected = onDefaultEventDurationSelected,
                )
                SettingsContentDivider(palette)
                SettingsAllDayReminderTimeRow(
                    selectedTime = defaultAllDayReminderTime,
                    palette = palette,
                    onTimeSelected = onDefaultAllDayReminderTimeSelected,
                )
            }
        }
    }
}

@Composable
private fun WidgetSettings(
    widgetTransparent: Boolean,
    widgetDotTexture: Boolean,
    isPro: Boolean,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onWidgetTransparentChange: (Boolean) -> Unit,
    onWidgetDotTextureChange: (Boolean) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(palette.calendarSurface).padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            SettingsLargeHeader(palette = palette, onBack = onBack, title = "Widgets")
        }
        item {
            SettingsPanel(title = "Widget Style", palette = palette, framed = false) {
                SettingsWidgetToggleRow(
                    title = "Transparent Widgets",
                    subtitle = "Let wallpaper show through all DotCal widgets",
                    checked = widgetTransparent,
                    isPro = isPro,
                    palette = palette,
                    onCheckedChange = onWidgetTransparentChange,
                )
                SettingsContentDivider(palette)
                SettingsWidgetToggleRow(
                    title = "Widget Dot Texture",
                    subtitle = if (widgetTransparent) "Only applies when transparent widgets are off" else "Show the subtle DotCal dotted surface",
                    checked = !widgetTransparent && widgetDotTexture,
                    enabled = !widgetTransparent,
                    isPro = isPro,
                    palette = palette,
                    onCheckedChange = onWidgetDotTextureChange,
                )
            }
        }
    }
}

@Composable
private fun DataRestoreSettings(
    palette: DotCalPalette,
    onBack: () -> Unit,
    onExportIcs: () -> Unit,
    onImportIcs: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    onRecentlyDeleted: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(palette.calendarSurface).padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            SettingsLargeHeader(palette = palette, onBack = onBack, title = "Data & Restore")
        }
        item {
            SettingsPanel(title = "Calendar Files", palette = palette, framed = false) {
                SettingsImportExportRow(
                    title = "Export Calendar",
                    subtitle = "Save all events & tasks to an .ics file",
                    isPro = true,
                    palette = palette,
                    onClick = onExportIcs,
                )
                SettingsContentDivider(palette)
                SettingsImportExportRow(
                    title = "Import Calendar",
                    subtitle = "Load events & tasks from an .ics file",
                    isPro = true,
                    palette = palette,
                    onClick = onImportIcs,
                )
            }
        }
        item {
            SettingsPanel(title = "Backup & Restore", palette = palette, framed = false) {
                SettingsImportExportRow(
                    title = "Back Up Data",
                    subtitle = "Save all events, tasks & reminders to a file",
                    isPro = true,
                    palette = palette,
                    onClick = onBackup,
                )
                SettingsContentDivider(palette)
                SettingsImportExportRow(
                    title = "Restore Data",
                    subtitle = "Merge a backup file into this device",
                    isPro = true,
                    palette = palette,
                    onClick = onRestore,
                )
                SettingsContentDivider(palette)
                SettingsMenuRow(
                    title = "Recently Deleted",
                    value = "",
                    palette = palette,
                    onClick = onRecentlyDeleted,
                )
            }
        }
    }
}

@Composable
private fun SyncSettings(
    syncEnabled: Boolean,
    syncIntervalMins: Int,
    syncMetadata: List<SyncMetadata>,
    isSyncing: Boolean,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onSyncEnabledChange: (Boolean) -> Unit,
    onSyncIntervalSelected: (Int) -> Unit,
    onSyncNow: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(palette.calendarSurface).padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            SettingsLargeHeader(palette = palette, onBack = onBack, title = "Sync")
        }
        item {
            SettingsPanel(title = "Calendar Sync", palette = palette, framed = false) {
                SettingsToggleRow(
                    title = "Sync enabled",
                    subtitle = "Keep DotCal updated from device calendars",
                    checked = syncEnabled,
                    palette = palette,
                    onCheckedChange = onSyncEnabledChange,
                )
                SettingsContentDivider(palette)
                SettingsSyncIntervalRow(
                    intervalMins = syncIntervalMins,
                    palette = palette,
                    onIntervalSelected = onSyncIntervalSelected,
                )
                SettingsContentDivider(palette)
                SettingsSyncNowRow(
                    syncMetadata = syncMetadata,
                    isSyncing = isSyncing,
                    palette = palette,
                    onClick = onSyncNow,
                )
            }
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
        modifier = Modifier.fillMaxSize().background(palette.calendarSurface).padding(horizontal = 20.dp),
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            SettingsLargeHeader(palette = palette, onBack = onBack, title = "Privacy")
        }
        item {
            SettingsPanel(title = "App Lock", palette = palette, framed = false) {
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
                SettingsContentDivider(palette)
                SettingsMenuRow(
                    title = if (appLockState.hasPin) "Change PIN" else "Set PIN",
                    value = "",
                    palette = palette,
                    onClick = { showSetPin = true },
                )
                if (appLockState.hasPin) {
                    SettingsContentDivider(palette)
                    SettingsMenuRow(
                        title = "Remove PIN",
                        value = "",
                        palette = palette,
                        onClick = { showClearConfirm = true },
                    )
                }
            }
        }
        item {
            SettingsPanel(title = "Private Vault", palette = palette, framed = false) {
            Text(
                "Hidden events and tasks stay off calendars, task lists, widgets, and reminders until restored.",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(vertical = 8.dp),
            )
            }
        }
        if (privateVaultEvents.isEmpty()) {
            item {
                SettingsPanel(title = "Private Items", palette = palette, framed = false) {
                    Text(
                        "No private items",
                        color = palette.secondaryText,
                        fontFamily = mono,
                        fontSize = 15.sp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        } else {
            item {
                SettingsPanel(title = "Private Items", palette = palette, framed = false) {
                    privateVaultEvents.forEachIndexed { index, event ->
                        PrivateVaultRow(
                            event = event,
                            palette = palette,
                            onRestore = { onRestorePrivateEvent(event.baseEventId()) },
                        )
                        if (index != privateVaultEvents.lastIndex) {
                            SettingsContentDivider(palette)
                        }
                    }
                }
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
internal fun AppLockScreen(
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
            Text("DotCal Locked", color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 24.sp)
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
        title = { Text(title, fontFamily = LocalHeadingFont.current) },
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
            fontFamily = LocalHeadingFont.current,
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
            fontFamily = LocalHeadingFont.current,
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
    appFont: AppFont,
    palette: DotCalPalette,
    isPro: Boolean,
    onBack: () -> Unit,
    onThemeSelected: (DotCalThemeMode) -> Unit,
    onAccentSelected: (AccentColor) -> Unit,
    onAppFontSelected: (AppFont) -> Unit,
    onRequestPro: () -> Unit,
) {
    var showCustomPicker by remember { mutableStateOf(false) }
    var showFontSheet by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val showCompactHeader = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 96
    Box(modifier = Modifier.fillMaxSize().background(palette.calendarSurface)) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 150.dp),
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            item {
                SettingsLargeHeader(palette = palette, onBack = onBack, title = "Appearance")
                Text("Choose app appearance", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, modifier = Modifier.padding(bottom = 16.dp))
            }
            item {
                SettingsPanel(title = "Font", palette = palette, framed = false) {
                    SettingsFontRow(
                        font = appFont,
                        palette = palette,
                        onClick = { showFontSheet = true },
                    )
                }
            }
            item {
                SettingsPanel(title = "Theme", palette = palette, framed = false) {
                    DotCalThemeMode.entries.forEachIndexed { index, mode ->
                        ThemeOptionRow(
                            mode = mode,
                            accentColor = accentColor,
                            palette = palette,
                            selected = themeMode == mode,
                            onClick = { onThemeSelected(mode) },
                        )
                        if (index != DotCalThemeMode.entries.lastIndex) {
                            SettingsContentDivider(palette)
                        }
                    }
                }
            }
            item {
                SettingsPanel(title = "Accent Color", palette = palette, framed = false) {
                    AccentColorSwatches(
                        accents = AccentColor.freePresets,
                        selectedAccent = accentColor,
                        palette = palette,
                        locked = false,
                        onAccentSelected = onAccentSelected,
                        onLockedClick = onRequestPro,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "More Colors",
                            color = palette.primaryText,
                            fontFamily = LocalHeadingFont.current,
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
                    Spacer(modifier = Modifier.height(24.dp))
                    CustomAccentRow(
                        accentColor = accentColor,
                        palette = palette,
                        isPro = isPro,
                        onClick = { if (isPro) showCustomPicker = true else onRequestPro() },
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(720.dp)) }
        }
        if (showCompactHeader) {
            SettingsCompactHeader(palette = palette, onBack = onBack, title = "Appearance")
        }
    }
    if (showFontSheet) {
        FontPickerSheet(
            current = appFont,
            palette = palette,
            onDismiss = { showFontSheet = false },
            onSelect = { font ->
                showFontSheet = false
                onAppFontSelected(font)
            },
        )
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
                        fontFamily = LocalHeadingFont.current,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "DotCal stores your calendar data locally on your device. Your calendar content does not go to Dotfield Studio servers. No account, no cloud sync, no analytics.",
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
                    "DotCal does not collect your calendar content and does not transmit events, tasks, reminders, attachments, or settings to Dotfield Studio. Your calendar data exists only on your Android device.\n\nThis policy covers the DotCal Android app (com.dotfield.dotcal), published by Dotfield Studio, and applies from the date of your first install.\n\nWe built DotCal with a simple rule: data about your life belongs to you. The app has no Dotfield backend, no account system, no advertising, and no analytics SDK embedded anywhere in the code. Google Play services are used only for Play Billing, purchase restore, and in-app update checks.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "02  Data We Collect",
                    "DotCal does not collect, store, or transmit your calendar content to Dotfield Studio.\n\nCalendar events, tasks, reminder alarms, voice notes, image attachments, theme and app settings, and any imported contact birthdays or Google Calendar events all live only on your device. None of that content is sent to Dotfield Studio, and we cannot access it.\n\nDotCal stores a local Pro entitlement flag after a successful Google Play purchase or restore. Payment processing, receipts, and account-level purchase data are handled by Google Play, not by Dotfield Studio. No crash analytics. No usage analytics. No advertising identifiers.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "03  App Permissions",
                    "DotCal requests only what it needs to function:\n\n- Calendar (read/write) - only if you enable Google Calendar sync, to read/create events via Android's CalendarProvider. Calendar data does not leave the device through DotCal.\n- Contacts - optional, to read contact birthdays as yearly events. Only reads dates, not full contact data.\n- Exact Alarm & Boot - schedule reminder alarms and re-register them after a reboot.\n- Notifications - display event and task reminders (Android 13+).\n- Microphone - optional, to record voice notes. Saved to app-private storage, only when you tap record.\n- Photos - selected via Android Photo Picker only. DotCal never accesses your full gallery.\n- Internet / Network State - used by Google Play Billing and in-app update checks. DotCal does not use this permission to upload calendar data, analytics, or ads.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "04  Google Calendar Sync",
                    "DotCal's calendar sync does not connect to Google's Calendar API. It reads from Android's built-in CalendarProvider - the same local database the default Calendar app uses. No Google Calendar credentials are ever seen or stored by DotCal.\n\nThis is different from apps that use the Google Calendar REST API or OAuth. DotCal does not make HTTP requests for calendar sync and never sees your Google credentials.\n\nIf you disable sync, DotCal stops querying CalendarProvider. Previously imported events stay in DotCal's local database until you delete them.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "05  Local Storage",
                    "All DotCal data is stored in Android's app-private storage, inaccessible to other apps without root access, and deleted automatically when you uninstall.\n\nStored data includes the SQLite database (events, tasks, recurrence rules), local preferences (theme, view, sync toggles), voice note files, and image attachments.\n\nDotCal does not write to shared external storage. If your device's Google Backup is enabled at the system level, Android may back up app data - this is outside DotCal's control.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "06  Third Parties",
                    "DotCal integrates no third-party SDKs for analytics, advertising, crash reporting, or remote configuration. No Firebase Analytics, no Crashlytics, no advertising networks, no A/B testing.\n\nDotCal uses Google Play Billing for the one-time Pro purchase and restore flow, Google Play In-App Updates for update checks, and the Nothing Glyph SDK for local Glyph Toy integration on supported Nothing devices. These services do not receive your calendar events, tasks, reminders, attachments, voice notes, or settings from DotCal.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "07  Security",
                    "Because DotCal stores calendar data locally and does not upload your calendar content, the main risk is physical device access. Keep your device PIN or biometric lock active.\n\nAndroid's app sandbox provides the primary protection. On modern devices with hardware-backed encryption, the OS-level encryption protects DotCal data at rest.",
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
                    "Because DotCal does not collect or store personal data on any server, all of your data is under your direct control:\n\n- Access - your data is in the app on your device.\n- Delete - uninstall to remove everything, or delete individual events in-app.\n- Export - PDF export of calendar views is a planned feature.\n- Portability - events synced via CalendarProvider remain in Android's calendar database.\n\nIf you are in the EU, UK, or California, GDPR and CCPA rights apply - exercised entirely on-device, since we hold no data.",
                    palette,
                )
            }
            item {
                PrivacySection(
                    "10  Policy Changes",
                    "If this policy changes materially - for example, if DotCal adds cloud sync, analytics, advertising, or a Dotfield server feature - we will update this page and the effective date below, and flag the change in a release note. We commit to never adding advertising, analytics, or cloud storage without updating this policy and prominently notifying users.",
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
                    "Effective July 14, 2026 / Dotfield Studio",
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
internal fun CalendarAddAccountRow(palette: DotCalPalette, onClick: () -> Unit, label: String = "Add Account") {
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
                label,
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
private fun SettingsDefaultViewRow(
    selectedTab: CalendarTab,
    palette: DotCalPalette,
    onViewSelected: (CalendarTab) -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .noRippleClickable { showSheet = true },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("Default view", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(selectedTab.shortLabel, color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            UpDownChevron(tint = palette.secondaryText)
        }
    }
    if (showSheet) {
        SettingsOptionSheet(
            title = "Default view",
            options = CalendarTab.pickerEntries,
            selected = selectedTab,
            palette = palette,
            label = { it.shortLabel },
            onDismiss = { showSheet = false },
            onSelected = {
                onViewSelected(it)
                showSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDefaultEventDurationRow(
    selectedMinutes: Int,
    palette: DotCalPalette,
    onDurationSelected: (Int) -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .noRippleClickable { showSheet = true },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text("Default event duration", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Normal, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(eventDurationLabel(selectedMinutes), color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp)
            Spacer(modifier = Modifier.width(8.dp))
            UpDownChevron(tint = palette.secondaryText)
        }
    }
    if (showSheet) {
        SettingsOptionSheet(
            title = "Default event duration",
            options = defaultEventDurationOptions,
            selected = selectedMinutes,
            palette = palette,
            label = ::eventDurationLabel,
            onDismiss = { showSheet = false },
            onSelected = {
                onDurationSelected(it)
                showSheet = false
            },
        )
    }
}

private fun eventDurationLabel(minutes: Int): String {
    return when (minutes) {
        60 -> "1 hour"
        120 -> "2 hours"
        else -> "$minutes min"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsWeekStartRow(
    selectedOption: WeekStartOption,
    palette: DotCalPalette,
    onWeekStartSelected: (WeekStartOption) -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .noRippleClickable { showSheet = true },
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
    if (showSheet) {
        SettingsOptionSheet(
            title = "Start of the week",
            options = WeekStartOption.entries,
            selected = selectedOption,
            palette = palette,
            label = { it.label },
            onDismiss = { showSheet = false },
            onSelected = {
                onWeekStartSelected(it)
                showSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsDefaultReminderRow(
    selectedMinutes: Int?,
    palette: DotCalPalette,
    onReminderSelected: (Int?) -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .noRippleClickable { showSheet = true },
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
    if (showSheet) {
        SettingsOptionSheet(
            title = "Default reminder",
            options = reminderOptions,
            selected = selectedMinutes,
            palette = palette,
            label = ::reminderLabel,
            onDismiss = { showSheet = false },
            onSelected = {
                onReminderSelected(it)
                showSheet = false
            },
        )
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
            Text("Default all-day reminder time", color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontSize = 20.sp)
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
                    border = secondaryActionBorder(palette),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = secondaryActionContainer(palette),
                        contentColor = secondaryActionContent(palette),
                    ),
                    shape = RoundedCornerShape(18.dp),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
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
private fun SettingsNothingHero(
    palette: DotCalPalette,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 2.dp),
    ) {
        Text(
            "Settings",
            color = palette.primaryText,
            fontFamily = LocalHeadingFont.current,
            fontWeight = FontWeight.Normal,
            fontSize = 34.sp,
            lineHeight = 38.sp,
        )
    }
}

@Composable
private fun SettingsPanel(
    title: String,
    palette: DotCalPalette,
    framed: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsSectionTitle(title, palette)
        val contentModifier = if (framed) {
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(palette.cancelSurface)
                .border(1.dp, palette.cancelBorder.copy(alpha = 0.72f), RoundedCornerShape(26.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        } else {
            Modifier.fillMaxWidth()
        }
        Column(
            modifier = contentModifier,
            content = content,
        )
    }
}

@Composable
private fun SettingsIconMenuRow(
    title: String,
    value: String,
    icon: ImageVector,
    palette: DotCalPalette,
    showChevron: Boolean = true,
    isProLocked: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 66.dp)
            .noRippleClickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SettingsIconCell(icon = icon, palette = palette, active = isProLocked)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (value.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    value,
                    color = if (isProLocked) palette.accent else palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        if (isProLocked) {
            Text(
                "Pro",
                color = palette.accent,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (showChevron) {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SettingsIconCell(icon: ImageVector, palette: DotCalPalette, active: Boolean = false) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (active) palette.accent.copy(alpha = 0.10f) else Color.Transparent),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (active) palette.accent else palette.primaryText,
            modifier = Modifier.size(19.dp),
        )
    }
}

@Composable
private fun SettingsToolsPanel(
    palette: DotCalPalette,
    isPro: Boolean,
    onTimeInsights: () -> Unit,
    onDateCalculator: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SettingsSectionTitle("Tools", palette)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            SettingsToolCard(
                title = "Time Insights",
                caption = "Load map",
                icon = Icons.Default.AccessTime,
                isPro = isPro,
                palette = palette,
                modifier = Modifier.weight(1f),
                onClick = onTimeInsights,
            )
            SettingsToolCard(
                title = "Date Calculator",
                caption = "Date math",
                icon = Icons.Default.CalendarMonth,
                isPro = isPro,
                palette = palette,
                modifier = Modifier.weight(1f),
                onClick = onDateCalculator,
            )
        }
    }
}

@Composable
private fun SettingsToolCard(
    title: String,
    caption: String,
    icon: ImageVector,
    isPro: Boolean,
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .height(132.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(palette.cancelSurface)
            .border(1.dp, palette.cancelBorder.copy(alpha = 0.72f), RoundedCornerShape(24.dp))
            .noRippleClickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            SettingsIconCell(icon = icon, palette = palette, active = !isPro)
            Text(
                if (isPro) "" else "Pro",
                color = palette.accent,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
            )
        }
        Column {
            Text(
                title,
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(caption, color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
        }
    }
}

@Composable
private fun SettingsProCard(isPro: Boolean, palette: DotCalPalette, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(palette.cancelSurface)
            .border(1.dp, palette.accent.copy(alpha = 0.26f), RoundedCornerShape(28.dp))
            .noRippleClickable(onClick = onClick)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(palette.accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = palette.onAccent, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "DotCal Pro",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                if (isPro) "Lifetime unlocked" else "Lifetime tools / INR 149",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
internal fun SettingsSectionTitle(title: String, palette: DotCalPalette) {
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
        Text(
            title,
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp),
        ) {
            if (value.isNotBlank()) {
                Text(
                    value,
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
private fun SettingsFontRow(
    font: AppFont,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .noRippleClickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Font",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${font.label} / ${font.tagline}",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(20.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontPickerSheet(
    current: AppFont,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onSelect: (AppFont) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "Font",
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
            AppFont.entries.forEach { font ->
                val selected = font == current
                val family = remember(font) {
                    when (font) {
                        AppFont.NDot -> FontFamily(Font(R.font.ndot))
                        AppFont.NType -> FontFamily(Font(R.font.ntype82))
                        AppFont.System -> FontFamily.Default
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) palette.accent.copy(alpha = 0.10f) else palette.cancelSurface)
                        .border(1.dp, if (selected) palette.accent else palette.cancelBorder, RoundedCornerShape(10.dp))
                        .noRippleClickable { onSelect(font) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            font.label,
                            color = palette.primaryText,
                            fontFamily = family,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            font.tagline,
                            color = palette.secondaryText,
                            fontFamily = mono,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (selected) {
                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = palette.accent, modifier = Modifier.size(22.dp))
                    }
                }
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
                if (isPro) "Active - thank you for your support!" else "Unlock Pro features",
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
internal fun SettingsToggleRow(
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
    var showSheet by remember { mutableStateOf(false) }
    val options = listOf(0, 15, 30, 60)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .noRippleClickable { showSheet = true },
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
    if (showSheet) {
        SettingsOptionSheet(
            title = "Sync interval",
            options = options,
            selected = intervalMins,
            palette = palette,
            label = ::syncIntervalLabel,
            onDismiss = { showSheet = false },
            onSelected = {
                onIntervalSelected(it)
                showSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> SettingsOptionSheet(
    title: String,
    options: List<T>,
    selected: T,
    palette: DotCalPalette,
    label: (T) -> String,
    onDismiss: () -> Unit,
    onSelected: (T) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 22.dp, end = 22.dp, top = 20.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                title,
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
            options.forEach { option ->
                val isSelected = option == selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) palette.accent.copy(alpha = 0.10f) else palette.cancelSurface)
                        .border(1.dp, if (isSelected) palette.accent else palette.cancelBorder, RoundedCornerShape(10.dp))
                        .noRippleClickable { onSelected(option) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        label(option),
                        color = palette.primaryText,
                        fontFamily = mono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = palette.accent,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
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
    HorizontalDivider(color = palette.line, thickness = 1.dp, modifier = Modifier.fillMaxWidth())
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
internal fun CustomAccentPickerDialog(
    initial: Color,
    palette: DotCalPalette,
    title: String = "Custom Accent",
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
            Text(title, color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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
internal fun CalcSectionLabelSafe(text: String, palette: DotCalPalette) {
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
