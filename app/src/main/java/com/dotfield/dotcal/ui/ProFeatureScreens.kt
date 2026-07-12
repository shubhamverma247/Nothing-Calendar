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
import androidx.compose.material.icons.filled.KeyboardArrowDown
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


private data class ProFeature(val name: String, val description: String)

private val PRO_FEATURES = listOf(
    ProFeature("Image Attachments", "Add up to 5 photos to any event"),
    ProFeature("Voice Notes", "Record audio notes on your events"),
    ProFeature("Large Widget", "Full month grid widget for your home screen"),
    ProFeature("Widget Pack Config", "Transparent widgets plus DotCal dot texture"),
    ProFeature("Date Calculator", "Calculate days between dates instantly"),
    ProFeature("Custom Accent Colors", "Extra palettes plus any custom hex color"),
    ProFeature("Quick Add", "Type 'gym every mon 7am' - we build the event"),
    ProFeature("Advanced Recurrence", "Every N weeks, nth weekday, end date or count"),
    ProFeature("App Lock & Private Vault", "PIN lock plus hidden events and tasks"),
    ProFeature("Event & Task Templates", "Save presets and reuse them from the + button"),
    ProFeature("Calendar Sets", "Save Work/Personal/Family visibility and switch instantly"),
    ProFeature("Shift Patterns", "Build rotating shift cycles and generate them in bulk"),
    ProFeature("Time Insights", "See hours, busiest days, and task completion"),
)

private enum class TimeInsightRange(val label: String) {
    Week("This week"),
    Month("This month"),
    Custom("Custom"),
}

private data class CalendarHourStat(
    val account: CalendarAccount,
    val hours: Double,
)

private data class TimeInsightsStats(
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
    val totalHours: Double,
    val eventCount: Int,
    val busiestDay: LocalDate?,
    val busiestDayHours: Double,
    val taskCompletionRate: Int?,
    val completedTasks: Int,
    val totalTasks: Int,
    val accountHours: List<CalendarHourStat>,
    val weekdayHours: List<Double>,
)

private val timeInsightDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.getDefault())

@Composable
internal fun PaywallScreen(
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
                showDotCalToast(context, palette, result.message)
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
            Text("You're Pro!", color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
        return
    }

    val connected = billingState is ProManager.BillingConnectionState.Connected
    val price = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
        ?: stringResource(R.string.pro_price_fallback)
    val priceIsEstimate = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice == null
    val buyEnabled = connected && !purchasing
    val buyLabel = if (connected) "Unlock Pro - $price" else "Connecting..."
    val launchPurchase = {
        val activity = context.findActivity()
        if (activity != null) {
            purchasing = true
            viewModel.purchasePro(activity)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp).statusBarsPadding().padding(horizontal = 8.dp)) {
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterStart).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text(
                "DotCal Pro",
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 22.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp))
                        .background(palette.eventCardSurface)
                        .border(1.dp, palette.eventCardBorder, RoundedCornerShape(28.dp))
                        .padding(horizontal = 22.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.mipmap.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(24.dp)),
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Unlock the power tools",
                        color = palette.primaryText,
                        fontFamily = LocalHeadingFont.current,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Quick capture, templates, calendar sets, widgets, privacy tools, and time insights in one lifetime upgrade.",
                        color = palette.secondaryText,
                        fontFamily = mono,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(palette.accent.copy(alpha = 0.10f))
                            .border(1.dp, palette.accent.copy(alpha = 0.26f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Lifetime Pro", color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                if (priceIsEstimate) "One-time purchase. Price may vary by region." else "One-time purchase. No subscription.",
                                color = palette.secondaryText,
                                fontFamily = mono,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                            )
                        }
                        Text(price, color = palette.accent, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    PaywallMetricCard("Pay once", "Forever access", palette, Modifier.weight(1f))
                    PaywallMetricCard("Offline-first", "No cloud account", palette, Modifier.weight(1f))
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(palette.eventCardSurface)
                        .border(1.dp, palette.eventCardBorder, RoundedCornerShape(24.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    PRO_FEATURES.forEach { feature ->
                        PaywallFeatureRow(feature = feature, palette = palette)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .background(palette.background)
                .padding(start = 22.dp, end = 22.dp, top = 12.dp, bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                if (priceIsEstimate) "One-time purchase. Regional price estimate." else "One-time purchase. No subscription.",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (buyEnabled) palette.accent else palette.disabledText)
                    .noRippleClickable(enabled = buyEnabled) { launchPurchase() },
                contentAlignment = Alignment.Center,
            ) {
                if (purchasing) {
                    CircularProgressIndicator(color = palette.onAccent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                } else {
                    Text(buyLabel, color = palette.onAccent, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Restore Purchase",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
                modifier = Modifier
                    .noRippleClickable {
                        viewModel.restorePro { restored ->
                            val message = if (restored) {
                                "Purchase restored - enjoy DotCal Pro!"
                            } else {
                                "No previous purchase found on this account"
                            }
                            showDotCalToast(context, palette, message)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun PaywallMetricCard(
    label: String,
    value: String,
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .height(86.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(palette.eventCardSurface)
            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp, maxLines = 1)
        Text(value, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 2, lineHeight = 17.sp)
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
internal fun SearchScreen(
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
    // Debounced query - re-run the DAO search a beat after typing stops.
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
    val filtered = remember(results, typeFilter, datePreset, accountId, todayStartMs, monthStartMs, monthEndMs) {
        results.filter { item ->
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
                fontFamily = LocalHeadingFont.current,
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
                                    "Title, location, notes...",
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SearchFilterDropdown(
                    label = "Type",
                    value = typeFilter.label,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                ) { close ->
                    SearchTypeFilter.values().forEach { type ->
                        SearchDropdownItem(
                            label = type.label,
                            selected = typeFilter == type,
                            palette = palette,
                            onClick = {
                                typeFilter = type
                                close()
                            },
                        )
                    }
                }
                SearchFilterDropdown(
                    label = "Time",
                    value = datePreset.label,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                ) { close ->
                    SearchDatePreset.values().forEach { preset ->
                        SearchDropdownItem(
                            label = preset.label,
                            selected = datePreset == preset,
                            palette = palette,
                            onClick = {
                                datePreset = preset
                                close()
                            },
                        )
                    }
                }
                val calendarLabel = accounts.firstOrNull { it.id == accountId }?.displayName ?: "All calendars"
                SearchFilterDropdown(
                    label = "Calendar",
                    value = calendarLabel,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                ) { close ->
                    SearchDropdownItem(
                        label = "All calendars",
                        selected = accountId == null,
                        palette = palette,
                        onClick = {
                            accountId = null
                            close()
                        },
                    )
                    accounts.forEach { account ->
                        SearchDropdownItem(
                            label = account.displayName,
                            selected = accountId == account.id,
                            palette = palette,
                            onClick = {
                                accountId = account.id
                                close()
                            },
                        )
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
private fun SearchFilterDropdown(
    label: String,
    value: String,
    palette: DotCalPalette,
    modifier: Modifier = Modifier,
    content: @Composable (close: () -> Unit) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(palette.eventCardSurface)
                .drawBehind {
                    drawRoundRect(
                        color = if (expanded) palette.accent else palette.eventCardBorder,
                        size = size,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }
                .noRippleClickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            Text(label, color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    value,
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = palette.secondaryText,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = palette.dialogSurface,
            shape = RoundedCornerShape(14.dp),
        ) {
            content { expanded = false }
        }
    }
}

@Composable
private fun SearchDropdownItem(label: String, selected: Boolean, palette: DotCalPalette, onClick: () -> Unit) {
    DropdownMenuItem(
        onClick = onClick,
        text = {
            Text(
                label,
                color = if (selected) palette.accent else palette.primaryText,
                fontFamily = mono,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailingIcon = {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
            }
        },
    )
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
            Text("TASK / $whenLabel", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, maxLines = 1)
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
internal fun QuickAddScreen(
    palette: DotCalPalette,
    onBack: () -> Unit,
    onContinue: (QuickAddResult) -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val trimmed = text.trim()
    // Re-parsed on every keystroke; pure and cheap.
    val parsed = remember(trimmed) { if (trimmed.isEmpty()) null else QuickAddParser.parse(trimmed) }
    val focusRequester = remember { FocusRequester() }
    val examples = remember {
        listOf("Gym every mon 7am", "Lunch tomorrow noon", "Pay rent on 1st", "Standup daily 9:30am")
    }

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
                fontFamily = LocalHeadingFont.current,
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
                    QuickAddPreviewRow("Title", parsed.title.ifBlank { "(none - add in next step)" }, palette)
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
        "$date / All-day"
    } else {
        "$date / ${time.format(editorTimeFormatter)}"
    }
}

private fun quickAddRepeatLabel(rrule: String): String =
    RecurrenceRule.parse(rrule)?.humanLabel() ?: "Custom"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BulkTemplatePickerSheet(
    palette: DotCalPalette,
    templates: List<EventTemplate>,
    onDismiss: () -> Unit,
    onTemplateSelected: (EventTemplate) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.dialogSurface,
        dragHandle = null,
    ) {
        Column(modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 22.dp, vertical = 18.dp)) {
            Text("Apply Template", color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Choose an event template to stamp onto selected dates.", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(14.dp))
            if (templates.isEmpty()) {
                Text("No event templates yet", color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), textAlign = TextAlign.Center)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    lazyItems(templates, key = { it.id }) { template ->
                        TemplateCard(
                            template = template,
                            palette = palette,
                            onUse = { onTemplateSelected(template) },
                            onDelete = {},
                            showDelete = false,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
internal fun TemplatesScreen(
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
                fontFamily = LocalHeadingFont.current,
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
                Text("No templates yet", color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
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
    showDelete: Boolean = true,
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
        if (showDelete) {
            IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete template", tint = palette.secondaryText, modifier = Modifier.size(20.dp))
            }
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
    return parts.joinToString(" / ")
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
internal fun FocusProfilesScreen(
    palette: DotCalPalette,
    profiles: List<FocusProfile>,
    totalCalendars: Int,
    onBack: () -> Unit,
    onApply: (String) -> Unit,
    onSaveCurrent: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var deleteTarget by remember { mutableStateOf<FocusProfile?>(null) }
    var showSaveDialog by remember { mutableStateOf(false) }
    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text(
                "Calendar Sets",
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center),
            )
            HorizontalDivider(color = palette.line.copy(alpha = 0.55f), thickness = 1.dp, modifier = Modifier.align(Alignment.BottomCenter))
        }
        if (profiles.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("No calendar sets yet", color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Show or hide calendars the way you want, then save that view as a set - Work, Personal, Family - and switch between them any time.",
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                lazyItems(profiles, key = { it.id }) { profile ->
                    FocusProfileCard(
                        profile = profile,
                        totalCalendars = totalCalendars,
                        palette = palette,
                        onApply = { onApply(profile.id) },
                        onDelete = { deleteTarget = profile },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
        CalendarAddAccountRow(
            palette = palette,
            onClick = { showSaveDialog = true },
            label = "Save Current as Set",
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
    if (showSaveDialog) {
        TemplateNameDialog(
            title = "Save current calendars as a set",
            defaultName = "",
            palette = palette,
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                onSaveCurrent(name.trim().ifBlank { "Set" })
                showSaveDialog = false
            },
        )
    }
    deleteTarget?.let { target ->
        ConfirmDeleteDialog(
            title = "Delete calendar set?",
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
private fun FocusProfileCard(
    profile: FocusProfile,
    totalCalendars: Int,
    palette: DotCalPalette,
    onApply: () -> Unit,
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
            .noRippleClickable(onClick = onApply)
            .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(profile.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1)
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                "${profile.accountIds.size} of $totalCalendars calendars",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
                maxLines = 1,
            )
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete calendar set", tint = palette.secondaryText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
internal fun TimeInsightsScreen(
    palette: DotCalPalette,
    events: List<CalendarEvent>,
    accounts: List<CalendarAccount>,
    onBack: () -> Unit,
) {
    val today = LocalDate.now()
    val weekStart = remember(today) { today.with(WeekFields.ISO.dayOfWeek(), 1) }
    var range by remember { mutableStateOf(TimeInsightRange.Week) }
    var customStart by remember { mutableStateOf(today.minusDays(29)) }
    var customEnd by remember { mutableStateOf(today) }
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }
    val rangeStart = when (range) {
        TimeInsightRange.Week -> weekStart
        TimeInsightRange.Month -> today.withDayOfMonth(1)
        TimeInsightRange.Custom -> minOf(customStart, customEnd)
    }
    val rangeEnd = when (range) {
        TimeInsightRange.Week -> weekStart.plusDays(6)
        TimeInsightRange.Month -> today.withDayOfMonth(today.lengthOfMonth())
        TimeInsightRange.Custom -> maxOf(customStart, customEnd)
    }
    val stats = remember(events, accounts, rangeStart, rangeEnd) {
        buildTimeInsightsStats(events, accounts, rangeStart, rangeEnd)
    }

    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text("Time Insights", color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
            HorizontalDivider(color = palette.line.copy(alpha = 0.55f), thickness = 1.dp, modifier = Modifier.align(Alignment.BottomCenter))
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp),
            contentPadding = PaddingValues(start = 0.dp, top = 18.dp, end = 0.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TimeInsightRange.entries.forEach { option ->
                        TimeInsightRangeChip(
                            label = option.label,
                            selected = range == option,
                            palette = palette,
                            modifier = Modifier.weight(1f),
                            onClick = { range = option },
                        )
                    }
                }
            }
            if (range == TimeInsightRange.Custom) {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        TimeInsightDateRow("From", rangeStart, palette, Modifier.weight(1f)) { pickingStart = true }
                        TimeInsightDateRow("To", rangeEnd, palette, Modifier.weight(1f)) { pickingEnd = true }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(palette.eventCardSurface).border(1.dp, palette.eventCardBorder, RoundedCornerShape(24.dp)).padding(18.dp),
                ) {
                    Text("${rangeStart.format(timeInsightDateFormatter)} - ${rangeEnd.format(timeInsightDateFormatter)}", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(formatInsightHours(stats.totalHours), color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 34.sp)
                    Text("scheduled hours", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TimeInsightMetricCard("Events", stats.eventCount.toString(), palette, Modifier.weight(1f))
                    TimeInsightMetricCard("Busiest", stats.busiestDay?.dayOfWeek?.name?.take(3) ?: "None", palette, Modifier.weight(1f), footer = if (stats.busiestDayHours > 0.0) formatInsightHours(stats.busiestDayHours) else "")
                    TimeInsightMetricCard("Tasks", stats.taskCompletionRate?.let { "$it%" } ?: "None", palette, Modifier.weight(1f), footer = if (stats.totalTasks > 0) "${stats.completedTasks}/${stats.totalTasks}" else "")
                }
            }
            item {
                SettingsSectionTitle("WEEKDAY LOAD", palette)
                WeekdayHoursChart(hours = stats.weekdayHours, palette = palette)
            }
            item {
                SettingsSectionTitle("CALENDARS", palette)
                if (stats.accountHours.isEmpty()) {
                    ShiftEmptyText("No scheduled timed events in this range.", palette)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val maxHours = stats.accountHours.maxOfOrNull { it.hours }?.coerceAtLeast(0.1) ?: 0.1
                        stats.accountHours.forEach { item ->
                            CalendarHoursRow(item = item, maxHours = maxHours, palette = palette)
                        }
                    }
                }
            }
        }
    }

    if (pickingStart) {
        DateTimeChoiceSheet(
            title = "Start date",
            selectedDate = customStart,
            selectedTime = LocalTime.NOON,
            minDate = null,
            includeTime = false,
            palette = palette,
            onDismiss = { pickingStart = false },
            onSelected = { date, _ ->
                customStart = date
                pickingStart = false
            },
        )
    }
    if (pickingEnd) {
        DateTimeChoiceSheet(
            title = "End date",
            selectedDate = customEnd,
            selectedTime = LocalTime.NOON,
            minDate = null,
            includeTime = false,
            palette = palette,
            onDismiss = { pickingEnd = false },
            onSelected = { date, _ ->
                customEnd = date
                pickingEnd = false
            },
        )
    }
}

@Composable
private fun TimeInsightRangeChip(label: String, selected: Boolean, palette: DotCalPalette, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) palette.accent else palette.cell)
            .noRippleClickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = if (selected) palette.onAccent else palette.secondaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1)
    }
}

@Composable
private fun TimeInsightDateRow(label: String, date: LocalDate, palette: DotCalPalette, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(16.dp)).background(palette.cell).noRippleClickable(onClick = onClick).padding(12.dp),
    ) {
        Text(label, color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(date.format(timeInsightDateFormatter), color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun TimeInsightMetricCard(label: String, value: String, palette: DotCalPalette, modifier: Modifier = Modifier, footer: String = "") {
    Column(
        modifier = modifier.height(96.dp).clip(RoundedCornerShape(20.dp)).background(palette.eventCardSurface).border(1.dp, palette.eventCardBorder, RoundedCornerShape(20.dp)).padding(12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp, maxLines = 1)
        Column {
            Text(value, color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 20.sp, maxLines = 1)
            Text(footer, color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp, maxLines = 1)
        }
    }
}

@Composable
private fun WeekdayHoursChart(hours: List<Double>, palette: DotCalPalette) {
    val maxHours = hours.maxOrNull()?.coerceAtLeast(0.1) ?: 0.1
    Row(
        modifier = Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(22.dp)).background(palette.eventCardSurface).border(1.dp, palette.eventCardBorder, RoundedCornerShape(22.dp)).padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        val labels = listOf("M", "T", "W", "T", "F", "S", "S")
        hours.forEachIndexed { index, value ->
            Column(modifier = Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.58f)
                            .height(((value / maxHours) * 92).coerceAtLeast(if (value > 0.0) 8.0 else 2.0).dp)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(if (value > 0.0) palette.accent else palette.line),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(labels[index], color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CalendarHoursRow(item: CalendarHourStat, maxHours: Double, palette: DotCalPalette) {
    val accountColor = remember(item.account.color) {
        runCatching { Color(android.graphics.Color.parseColor(item.account.color)) }.getOrDefault(palette.accent)
    }
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(palette.eventCardSurface).border(1.dp, palette.eventCardBorder, RoundedCornerShape(16.dp)).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(accountColor))
            Spacer(Modifier.width(8.dp))
            Text(item.account.displayName, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(formatInsightHours(item.hours), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
        }
        Spacer(Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(palette.line)) {
            Box(modifier = Modifier.fillMaxWidth((item.hours / maxHours).toFloat().coerceIn(0.04f, 1f)).height(6.dp).clip(RoundedCornerShape(3.dp)).background(accountColor))
        }
    }
}

private fun buildTimeInsightsStats(
    events: List<CalendarEvent>,
    accounts: List<CalendarAccount>,
    rangeStart: LocalDate,
    rangeEnd: LocalDate,
): TimeInsightsStats {
    val zone = ZoneId.systemDefault()
    val startMs = rangeStart.atStartOfDay(zone).toInstant().toEpochMilli()
    val endMs = rangeEnd.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
    val accountMap = accounts.associateBy { it.id }
    val timedEvents = events.filter { event ->
        event.isTask == 0 &&
            event.isAllDay == 0 &&
            event.source != "BIRTHDAY" &&
            event.endTimeMs > startMs &&
            event.startTimeMs < endMs
    }
    val tasks = events.filter { event ->
        event.isTask == 1 &&
            event.startTimeMs >= startMs &&
            event.startTimeMs < endMs
    }
    val hoursByAccount = mutableMapOf<String, Double>()
    val hoursByDay = mutableMapOf<LocalDate, Double>()
    val weekdayHours = MutableList(7) { 0.0 }
    timedEvents.forEach { event ->
        val clippedStart = maxOf(event.startTimeMs, startMs)
        val clippedEnd = minOf(event.endTimeMs, endMs)
        val hours = ((clippedEnd - clippedStart).coerceAtLeast(0L) / 3_600_000.0)
        if (hours > 0.0) {
            hoursByAccount[event.accountId] = (hoursByAccount[event.accountId] ?: 0.0) + hours
            val date = Instant.ofEpochMilli(clippedStart).atZone(zone).toLocalDate()
            hoursByDay[date] = (hoursByDay[date] ?: 0.0) + hours
            val weekdayIndex = date.dayOfWeek.value - 1
            weekdayHours[weekdayIndex] = weekdayHours[weekdayIndex] + hours
        }
    }
    val accountHours = hoursByAccount.mapNotNull { (accountId, hours) ->
        accountMap[accountId]?.let { CalendarHourStat(it, hours) }
    }.sortedByDescending { it.hours }
    val busiest = hoursByDay.maxByOrNull { it.value }
    val completedTasks = tasks.count { it.isCompleted == 1 }
    val completionRate = if (tasks.isEmpty()) null else ((completedTasks * 100.0) / tasks.size).roundToInt()
    return TimeInsightsStats(
        rangeStart = rangeStart,
        rangeEnd = rangeEnd,
        totalHours = hoursByAccount.values.sum(),
        eventCount = timedEvents.size,
        busiestDay = busiest?.key,
        busiestDayHours = busiest?.value ?: 0.0,
        taskCompletionRate = completionRate,
        completedTasks = completedTasks,
        totalTasks = tasks.size,
        accountHours = accountHours,
        weekdayHours = weekdayHours,
    )
}

private fun formatInsightHours(hours: Double): String {
    return if (hours < 10.0) {
        "${(hours * 10).roundToInt() / 10.0}h"
    } else {
        "${hours.roundToInt()}h"
    }
}

@Composable
internal fun ShiftPatternsScreen(
    palette: DotCalPalette,
    shiftTypes: List<ShiftType>,
    patterns: List<ShiftPattern>,
    accounts: List<CalendarAccount>,
    onBack: () -> Unit,
    onSaveType: (ShiftType) -> Unit,
    onDeleteType: (String) -> Unit,
    onSavePattern: (ShiftPattern) -> Unit,
    onDeletePattern: (String, Boolean) -> Unit,
    onGenerate: (String, LocalDate, LocalDate, String?) -> Unit,
) {
    var showTypeEditor by remember { mutableStateOf(false) }
    var showPatternEditor by remember { mutableStateOf(false) }
    var editingType by remember { mutableStateOf<ShiftType?>(null) }
    var generatingPattern by remember { mutableStateOf<ShiftPattern?>(null) }
    var deletePattern by remember { mutableStateOf<ShiftPattern?>(null) }
    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text("Shift Patterns", color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
            HorizontalDivider(color = palette.line.copy(alpha = 0.55f), thickness = 1.dp, modifier = Modifier.align(Alignment.BottomCenter))
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                SettingsSectionTitle("SHIFT TYPES", palette)
                if (shiftTypes.isEmpty()) {
                    ShiftEmptyText("Create Day, Night, and Off presets first.", palette)
                }
            }
            lazyItems(shiftTypes, key = { it.id }) { type ->
                ShiftTypeCard(
                    type = type,
                    palette = palette,
                    onClick = { editingType = type },
                    onDelete = { onDeleteType(type.id) },
                )
            }
            item {
                CalendarAddAccountRow(palette = palette, onClick = { showTypeEditor = true }, label = "Add Shift Type")
                Spacer(modifier = Modifier.height(10.dp))
                SettingsSectionTitle("PATTERNS", palette)
                if (patterns.isEmpty()) {
                    ShiftEmptyText("Build a cycle like Day, Day, Night, Night, Off x4.", palette)
                }
            }
            lazyItems(patterns, key = { it.id }) { pattern ->
                ShiftPatternCard(
                    pattern = pattern,
                    shiftTypes = shiftTypes,
                    palette = palette,
                    onGenerate = { generatingPattern = pattern },
                    onDelete = { deletePattern = pattern },
                )
            }
            item {
                CalendarAddAccountRow(
                    palette = palette,
                    onClick = { showPatternEditor = true },
                    label = "Build Pattern",
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    if (showTypeEditor) {
        ShiftTypeEditorDialog(
            palette = palette,
            existing = null,
            onDismiss = { showTypeEditor = false },
            onSave = {
                onSaveType(it)
                showTypeEditor = false
            },
        )
    }
    editingType?.let { type ->
        ShiftTypeEditorDialog(
            palette = palette,
            existing = type,
            onDismiss = { editingType = null },
            onSave = {
                onSaveType(it)
                editingType = null
            },
        )
    }
    if (showPatternEditor) {
        ShiftPatternEditorDialog(
            palette = palette,
            shiftTypes = shiftTypes,
            onDismiss = { showPatternEditor = false },
            onSave = {
                onSavePattern(it)
                showPatternEditor = false
            },
        )
    }
    generatingPattern?.let { pattern ->
        ShiftGenerateDialog(
            pattern = pattern,
            accounts = accounts,
            palette = palette,
            onDismiss = { generatingPattern = null },
            onGenerate = { start, months, accountId ->
                onGenerate(pattern.id, start, start.plusMonths(months.toLong()), accountId)
                generatingPattern = null
            },
        )
    }
    deletePattern?.let { pattern ->
        ConfirmDeleteDialog(
            title = "Delete shift pattern?",
            confirmLabel = "Delete",
            palette = palette,
            onDismiss = { deletePattern = null },
            onConfirm = {
                onDeletePattern(pattern.id, true)
                deletePattern = null
            },
        )
    }
}

@Composable
private fun ShiftEmptyText(text: String, palette: DotCalPalette) {
    Text(text, color = palette.secondaryText, fontFamily = mono, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(vertical = 10.dp))
}

@Composable
private fun ShiftTypeCard(type: ShiftType, palette: DotCalPalette, onClick: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(palette.eventCardSurface)
            .noRippleClickable(onClick = onClick)
            .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(Color(parseColor(type.colorHex))))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(type.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(shiftTypeSummary(type), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete shift type", tint = palette.secondaryText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ShiftPatternCard(
    pattern: ShiftPattern,
    shiftTypes: List<ShiftType>,
    palette: DotCalPalette,
    onGenerate: () -> Unit,
    onDelete: () -> Unit,
) {
    val typeMap = remember(shiftTypes) { shiftTypes.associateBy { it.id } }
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(palette.eventCardSurface).noRippleClickable(onClick = onGenerate).padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(pattern.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(shiftPatternSummary(pattern, typeMap), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, maxLines = 2)
        }
        IconButton(onClick = onGenerate, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Generate shifts", tint = palette.accent, modifier = Modifier.size(20.dp))
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete shift pattern", tint = palette.secondaryText, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun ShiftTypeEditorDialog(
    palette: DotCalPalette,
    existing: ShiftType?,
    onDismiss: () -> Unit,
    onSave: (ShiftType) -> Unit,
) {
    var name by remember(existing?.id) { mutableStateOf(existing?.name.orEmpty()) }
    var isOff by remember(existing?.id) { mutableStateOf(existing?.generatesEvent == false) }
    var startHour by remember(existing?.id) { mutableStateOf(((existing?.startMinuteOfDay ?: 7 * 60) / 60).toString()) }
    var durationHours by remember(existing?.id) { mutableStateOf(((existing?.durationMinutes ?: 12 * 60) / 60).coerceAtLeast(1).toString()) }
    var color by remember(existing?.id) { mutableStateOf(existing?.colorHex ?: "#FF3B30") }
    var showColorPicker by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        title = { Text(if (existing == null) "Shift type" else "Edit shift type", color = palette.primaryText, fontFamily = LocalHeadingFont.current) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, colors = dotCalTextFieldColors(palette), textStyle = TextStyle(color = palette.primaryText, fontFamily = mono))
                SettingsToggleRow(title = "Off day", checked = isOff, palette = palette, onCheckedChange = { isOff = it })
                if (!isOff) {
                    OutlinedTextField(value = startHour, onValueChange = { startHour = it.filter(Char::isDigit).take(2) }, label = { Text("Start hour") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = dotCalTextFieldColors(palette), textStyle = TextStyle(color = palette.primaryText, fontFamily = mono))
                    OutlinedTextField(value = durationHours, onValueChange = { durationHours = it.filter(Char::isDigit).take(2) }, label = { Text("Duration hours") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = dotCalTextFieldColors(palette), textStyle = TextStyle(color = palette.primaryText, fontFamily = mono))
                    ShiftColorRow(colorHex = color, palette = palette, onClick = { showColorPicker = true })
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val hour = startHour.toIntOrNull()?.coerceIn(0, 23) ?: 7
                    val duration = durationHours.toIntOrNull()?.coerceIn(1, 24) ?: 12
                    onSave(
                        ShiftType(
                            id = existing?.id ?: ShiftType.newId(),
                            name = name.trim(),
                            colorHex = color.takeIf { it.matches(Regex("#[0-9A-Fa-f]{6}")) } ?: "#FF3B30",
                            startMinuteOfDay = if (isOff) null else hour * 60,
                            durationMinutes = if (isOff) null else duration * 60,
                            isAllDay = false,
                            reminderMinutes = null,
                            createdAtMs = existing?.createdAtMs ?: System.currentTimeMillis(),
                        ),
                    )
                },
            ) { Text("Save", color = if (name.isNotBlank()) palette.accent else palette.disabledText) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = palette.primaryText) } },
    )
    if (showColorPicker) {
        CustomAccentPickerDialog(
            initial = Color(parseColor(color)),
            palette = palette,
            title = "Shift Color",
            onDismiss = { showColorPicker = false },
            onConfirm = {
                color = it
                showColorPicker = false
            },
        )
    }
}

@Composable
private fun ShiftPatternEditorDialog(
    palette: DotCalPalette,
    shiftTypes: List<ShiftType>,
    onDismiss: () -> Unit,
    onSave: (ShiftPattern) -> Unit,
) {
    var name by remember { mutableStateOf("4 on 4 off") }
    var cycle by remember { mutableStateOf<List<String>>(emptyList()) }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        title = { Text("Build pattern", color = palette.primaryText, fontFamily = LocalHeadingFont.current) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, singleLine = true, colors = dotCalTextFieldColors(palette), textStyle = TextStyle(color = palette.primaryText, fontFamily = mono))
                ShiftDateRow(label = "Start date", date = startDate, palette = palette, onClick = { showStartDatePicker = true })
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    lazyItems(shiftTypes, key = { it.id }) { type ->
                        ShiftChip(type.name, palette, onClick = { cycle = cycle + type.id })
                    }
                }
                Text(shiftCycleLabel(cycle, shiftTypes.associateBy { it.id }), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, lineHeight = 17.sp)
                if (cycle.isNotEmpty()) {
                    TextButton(onClick = { cycle = cycle.dropLast(1) }) { Text("Remove Last", color = palette.accent, fontFamily = mono) }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && cycle.isNotEmpty(),
                onClick = {
                    onSave(
                        ShiftPattern(
                            id = ShiftPattern.newId(),
                            name = name.trim(),
                            cycleShiftTypeIds = cycle,
                            cycleStartDate = startDate,
                            createdAtMs = System.currentTimeMillis(),
                        ),
                    )
                },
            ) { Text("Save", color = palette.accent) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = palette.primaryText) } },
    )
    if (showStartDatePicker) {
        DateTimeChoiceSheet(
            title = "Pattern start",
            selectedDate = startDate,
            selectedTime = LocalTime.NOON,
            minDate = null,
            includeTime = false,
            palette = palette,
            onDismiss = { showStartDatePicker = false },
            onSelected = { date, _ ->
                startDate = date
                showStartDatePicker = false
            },
        )
    }
}

@Composable
private fun ShiftGenerateDialog(
    pattern: ShiftPattern,
    accounts: List<CalendarAccount>,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onGenerate: (LocalDate, Int, String?) -> Unit,
) {
    var startDate by remember(pattern.id) { mutableStateOf(LocalDate.now()) }
    var months by remember { mutableStateOf("6") }
    var accountId by remember(accounts) { mutableStateOf(accounts.firstOrNull()?.id) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        title = { Text("Generate shifts", color = palette.primaryText, fontFamily = LocalHeadingFont.current) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(pattern.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold)
                ShiftDateRow(label = "Generate from", date = startDate, palette = palette, onClick = { showStartDatePicker = true })
                OutlinedTextField(value = months, onValueChange = { months = it.filter(Char::isDigit).take(2) }, label = { Text("Months ahead") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = dotCalTextFieldColors(palette), textStyle = TextStyle(color = palette.primaryText, fontFamily = mono))
                Text("Calendar", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    lazyItems(accounts, key = { it.id }) { account ->
                        ShiftChip(account.displayName.readableCalendarLabel(), palette, selected = account.id == accountId, onClick = { accountId = account.id })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onGenerate(startDate, months.toIntOrNull()?.coerceIn(1, 24) ?: 6, accountId) }) {
                Text("Generate", color = palette.accent)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = palette.primaryText) } },
    )
    if (showStartDatePicker) {
        DateTimeChoiceSheet(
            title = "Generate from",
            selectedDate = startDate,
            selectedTime = LocalTime.NOON,
            minDate = null,
            includeTime = false,
            palette = palette,
            onDismiss = { showStartDatePicker = false },
            onSelected = { date, _ ->
                startDate = date
                showStartDatePicker = false
            },
        )
    }
}

@Composable
private fun ShiftDateRow(label: String, date: LocalDate, palette: DotCalPalette, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, palette.textFieldBorder, RoundedCornerShape(10.dp))
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
            Spacer(Modifier.height(2.dp))
            Text(date.format(editorDateFormatter), color = palette.primaryText, fontFamily = mono, fontSize = 15.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ShiftColorRow(colorHex: String, palette: DotCalPalette, onClick: () -> Unit) {
    val swatchColor = remember(colorHex) { Color(parseColor(colorHex)) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, palette.textFieldBorder, RoundedCornerShape(10.dp))
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(28.dp).clip(CircleShape).background(swatchColor))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text("Color", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
            Spacer(Modifier.height(2.dp))
            Text(colorHex.uppercase(Locale.US), color = palette.primaryText, fontFamily = mono, fontSize = 15.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ShiftChip(label: String, palette: DotCalPalette, selected: Boolean = false, onClick: () -> Unit) {
    Text(
        label,
        color = if (selected) palette.onAccent else palette.primaryText,
        fontFamily = mono,
        fontSize = 12.sp,
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(if (selected) palette.accent else palette.cell).noRippleClickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

private fun shiftTypeSummary(type: ShiftType): String {
    if (!type.generatesEvent) return "Off"
    val start = type.startMinuteOfDay?.let { LocalTime.of(it / 60, it % 60).format(editorTimeFormatter) } ?: "All-day"
    val duration = type.durationMinutes?.let { formatDurationShort(it) } ?: "All-day"
    return "$start - $duration"
}

private fun shiftPatternSummary(pattern: ShiftPattern, types: Map<String, ShiftType>): String =
    "${pattern.cycleShiftTypeIds.size}-day cycle: " + shiftCycleLabel(pattern.cycleShiftTypeIds, types)

private fun shiftCycleLabel(ids: List<String>, types: Map<String, ShiftType>): String =
    ids.map { types[it]?.name ?: "Missing" }.joinToString(", ").ifBlank { "No shifts selected" }

@Composable
internal fun RecentlyDeletedScreen(
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
                fontFamily = LocalHeadingFont.current,
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
                        fontFamily = LocalHeadingFont.current,
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
        // Foreground content - drag left to reveal actions, tap to close when open.
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
                    text = "${deletedWhenLabel(event)} / deleted ${deletedAgoLabel(snapshot.deletedAtMs, nowMs)}",
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
    if (event.isTask == 1 && event.startTimeMs <= 0L) return "$prefix / No due date"
    val zone = runCatching { java.time.ZoneId.of(event.timeZone) }.getOrDefault(java.time.ZoneId.systemDefault())
    val start = java.time.Instant.ofEpochMilli(event.startTimeMs).atZone(zone)
    val dateFmt = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
    val timeFmt = java.time.format.DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    val date = start.format(dateFmt)
    return if (event.isAllDay == 1) "$prefix / $date" else "$prefix / $date, ${start.format(timeFmt)}"
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
internal fun DateCalculatorScreen(
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
        // ? Top bar: back + title.
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text(
                "Date Calculator",
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
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
            // ? Mode segmented control.
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
        CalcStepperButton("-", palette) { onChange((days - 1).coerceAtLeast(0)) }
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
internal fun CalcStepperButton(symbol: String, palette: DotCalPalette, onClick: () -> Unit) {
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
internal fun TwoOptionSegmentedControl(
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

