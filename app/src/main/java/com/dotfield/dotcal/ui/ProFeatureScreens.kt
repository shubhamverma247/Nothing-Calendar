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
import androidx.annotation.StringRes
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings as SettingsGearIcon
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.zIndex
import androidx.activity.compose.BackHandler
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.layout.ColumnScope
import com.dotfield.dotcal.R
import com.dotfield.dotcal.data.billing.ProManager
import com.dotfield.dotcal.data.billing.ProPurchasePlan
import com.dotfield.dotcal.data.billing.ProPurchaseOffer
import com.dotfield.dotcal.data.billing.isSevenDayTrial
import com.dotfield.dotcal.data.billing.selectionKey
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
import com.dotfield.dotcal.data.scheduling.FreeSlot
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


/**
 * Paywall feature list. Same `@StringRes` + `@Composable` getter pattern as the converted enums:
 * the list itself stays a plain top-level `val` (no Context at construction), and the text is only
 * resolved when a composable reads [name] / [description].
 */
private data class ProFeature(
    @StringRes val nameRes: Int,
    @StringRes val descriptionRes: Int,
) {
    val name: String
        @Composable get() = stringResource(nameRes)
    val description: String
        @Composable get() = stringResource(descriptionRes)
}

private val PRO_FEATURES = listOf(
    ProFeature(R.string.pro_feature_app_lock, R.string.pro_feature_app_lock_desc),
    ProFeature(R.string.pro_feature_pdf_attachments, R.string.pro_feature_pdf_attachments_desc),
    ProFeature(R.string.pro_feature_images, R.string.pro_feature_images_desc),
    ProFeature(R.string.pro_feature_voice, R.string.pro_feature_voice_desc),
    ProFeature(R.string.pro_feature_unlimited_countdowns, R.string.pro_feature_unlimited_countdowns_desc),
    ProFeature(R.string.pro_feature_shift_patterns, R.string.pro_feature_shift_patterns_desc),
    ProFeature(R.string.pro_feature_shift_sharing, R.string.pro_feature_shift_sharing_desc),
    ProFeature(R.string.pro_feature_calendar_sets, R.string.pro_feature_calendar_sets_desc),
    ProFeature(R.string.pro_feature_bulk_actions, R.string.pro_feature_bulk_actions_desc),
    ProFeature(R.string.pro_feature_date_calc, R.string.pro_feature_date_calc_desc),
    ProFeature(R.string.pro_feature_time_insights, R.string.pro_feature_time_insights_desc),
    ProFeature(R.string.pro_feature_dead_time, R.string.pro_feature_dead_time_desc),
    ProFeature(R.string.pro_feature_share_availability, R.string.pro_feature_share_availability_desc),
    ProFeature(R.string.pro_feature_year_heatmap, R.string.pro_feature_year_heatmap_desc),
    ProFeature(R.string.pro_feature_large_widget, R.string.pro_feature_large_widget_desc),
    ProFeature(R.string.pro_feature_widget_pack, R.string.pro_feature_widget_pack_desc),
    ProFeature(R.string.pro_feature_widget_calendar_picker, R.string.pro_feature_widget_calendar_picker_desc),
    ProFeature(R.string.pro_feature_accents, R.string.pro_feature_accents_desc),
    ProFeature(R.string.pro_feature_recurrence, R.string.pro_feature_recurrence_desc),
    ProFeature(R.string.pro_feature_templates, R.string.pro_feature_templates_desc),
)

private enum class TimeInsightRange(@StringRes val labelRes: Int) {
    Week(R.string.insights_range_week),
    Month(R.string.insights_range_month),
    Custom(R.string.insights_range_custom);

    val label: String
        @Composable get() = stringResource(labelRes)
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

// timeInsightDateFormatter removed: a top-level val caches Locale.getDefault() at class-init
// time and survives an in-app language change. compactDateFormatter is the same "MMM d"
// pattern routed through localizedFormatter().

@Composable
internal fun PaywallScreen(
    viewModel: DotCalViewModel,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val productDetails by viewModel.productDetails.collectAsStateWithLifecycle()
    val purchaseOffers by viewModel.purchaseOffers.collectAsStateWithLifecycle()
    val hasActiveSubscription by viewModel.hasActiveSubscription.collectAsStateWithLifecycle()
    val billingState by viewModel.billingState.collectAsStateWithLifecycle()
    val purchaseResult by viewModel.purchaseResult.collectAsStateWithLifecycle()
    var purchasing by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var selectedOfferKey by remember { mutableStateOf<String?>(null) }

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
            Text(stringResource(R.string.paywall_youre_pro), color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        }
        return
    }

    val connected = billingState is ProManager.BillingConnectionState.Connected
    LaunchedEffect(purchaseOffers) {
        val currentStillEligible = purchaseOffers.any { it.selectionKey == selectedOfferKey }
        if (!currentStillEligible) selectedOfferKey = purchaseOffers.firstOrNull()?.selectionKey
    }
    val selectedOffer = purchaseOffers.firstOrNull { it.selectionKey == selectedOfferKey } ?: purchaseOffers.firstOrNull()
    val price = selectedOffer?.formattedPrice ?: productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
    val buyEnabled = connected && !purchasing && selectedOffer != null
    val buyLabel = when {
        !connected -> stringResource(R.string.paywall_connecting)
        selectedOffer?.hasFreeTrial == true -> stringResource(R.string.paywall_start_free_trial)
        price != null -> stringResource(R.string.paywall_unlock_pro_price, price)
        else -> stringResource(R.string.paywall_unlock_pro)
    }
    val launchPurchase = {
        val activity = context.findActivity()
        if (activity != null) {
            purchasing = true
            viewModel.purchasePro(activity, selectedOffer?.selectionKey)
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
                stringResource(R.string.pro_product_name),
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
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = androidx.compose.ui.res.painterResource(id = R.mipmap.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(96.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.paywall_headline),
                        color = palette.primaryText,
                        fontFamily = LocalHeadingFont.current,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.paywall_subhead),
                        color = palette.secondaryText,
                        fontFamily = mono,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            if (purchaseOffers.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(palette.eventCardSurface)
                            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            stringResource(R.string.paywall_purchase_options),
                            color = palette.primaryText,
                            fontFamily = mono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                        )
                        purchaseOffers.forEach { offer ->
                            PaywallOfferRow(
                                offer = offer,
                                selected = offer.selectionKey == selectedOffer?.selectionKey,
                                palette = palette,
                                onClick = { selectedOfferKey = offer.selectionKey },
                            )
                        }
                    }
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
                selectedOffer?.checkoutFinePrint() ?: stringResource(R.string.paywall_final_price_at_checkout),
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
            // The two toast strings are hoisted out of the click lambda: it is not composable.
            val restoredToast = stringResource(R.string.paywall_restore_success)
            val noPurchaseToast = stringResource(R.string.paywall_restore_none_found)
            Text(
                stringResource(R.string.paywall_restore_purchase),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
                modifier = Modifier
                    .noRippleClickable {
                        viewModel.restorePro { restored ->
                            showDotCalToast(
                                context,
                                palette,
                                if (restored) restoredToast else noPurchaseToast,
                            )
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
            if (hasActiveSubscription) {
                Text(
                    stringResource(R.string.paywall_manage_subscription),
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .noRippleClickable { context.openDotCalSubscriptionManagement() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun ProPurchaseOffer.checkoutFinePrint(): String = when (plan) {
    ProPurchasePlan.Yearly -> if (isSevenDayTrial) {
        stringResource(R.string.paywall_yearly_trial_fine_print, formattedPrice)
    } else {
        stringResource(R.string.paywall_subscription_fine_print)
    }
    ProPurchasePlan.Monthly -> stringResource(R.string.paywall_subscription_fine_print)
    ProPurchasePlan.Lifetime -> stringResource(R.string.paywall_one_time_no_subscription)
}

private fun Context.openDotCalSubscriptionManagement() {
    val uri = Uri.parse(
        "https://play.google.com/store/account/subscriptions" +
            "?sku=${ProManager.PRODUCT_ID_PRO_SUBSCRIPTION}&package=$packageName",
    )
    val intent = Intent(Intent.ACTION_VIEW, uri)
    if (this !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { startActivity(intent) }
}

@Composable
private fun PaywallOfferRow(
    offer: ProPurchaseOffer,
    selected: Boolean,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    val name = when (offer.plan) {
        ProPurchasePlan.Yearly -> stringResource(R.string.paywall_yearly_plan)
        ProPurchasePlan.Monthly -> stringResource(R.string.paywall_monthly_plan)
        ProPurchasePlan.Lifetime -> stringResource(R.string.paywall_lifetime_pro)
    }
    val detail = when (offer.plan) {
        ProPurchasePlan.Yearly -> if (offer.isSevenDayTrial) {
            stringResource(R.string.paywall_yearly_trial_detail)
        } else {
            stringResource(R.string.paywall_yearly_detail)
        }
        ProPurchasePlan.Monthly -> stringResource(R.string.paywall_monthly_detail)
        ProPurchasePlan.Lifetime -> stringResource(R.string.paywall_lifetime_detail)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) palette.accent.copy(alpha = 0.12f) else palette.cell)
            .border(1.dp, if (selected) palette.accent else palette.line, RoundedCornerShape(14.dp))
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (offer.isSevenDayTrial) {
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        stringResource(R.string.paywall_trial_badge),
                        color = palette.accent,
                        fontFamily = mono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 8.sp,
                        maxLines = 1,
                    )
                }
            }
            Text(detail, color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp, lineHeight = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Text(offer.formattedPrice, color = palette.accent, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        if (selected) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.Default.Check, contentDescription = null, tint = palette.accent, modifier = Modifier.size(16.dp))
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

private enum class SearchTypeFilter(@StringRes val labelRes: Int) {
    All(R.string.search_type_all),
    Events(R.string.search_type_events),
    Tasks(R.string.search_type_tasks);

    val label: String
        @Composable get() = stringResource(labelRes)
}

private enum class SearchDatePreset(@StringRes val labelRes: Int) {
    AnyTime(R.string.search_date_any_time),
    Upcoming(R.string.search_date_upcoming),
    Past(R.string.search_date_past),
    ThisMonth(R.string.search_date_this_month);

    val label: String
        @Composable get() = stringResource(labelRes)
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
                stringResource(R.string.search_title),
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
                                    stringResource(R.string.search_placeholder),
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
                    label = stringResource(R.string.search_facet_type),
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
                    label = stringResource(R.string.search_facet_time),
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
                val allCalendarsLabel = stringResource(R.string.search_facet_all_calendars)
                val calendarLabel = accounts.firstOrNull { it.id == accountId }?.displayName ?: allCalendarsLabel
                SearchFilterDropdown(
                    label = stringResource(R.string.search_facet_calendar),
                    value = calendarLabel,
                    palette = palette,
                    modifier = Modifier.weight(1f),
                ) { close ->
                    SearchDropdownItem(
                        label = allCalendarsLabel,
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
            trimmed.isEmpty() -> SearchHintBox(stringResource(R.string.search_hint), palette)
            filtered.isEmpty() -> SearchHintBox(stringResource(R.string.search_no_results, trimmed), palette)
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
        .format(detailDateFormatter)
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
            Text(stringResource(R.string.search_task_when, whenLabel), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, maxLines = 1)
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
                stringResource(R.string.quick_add_title),
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
            CalcSectionLabel(stringResource(R.string.quick_add_describe_event), palette)
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
                CalcSectionLabel(stringResource(R.string.quick_add_preview), palette)
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val untitledLabel = stringResource(R.string.quick_add_untitled)
                    QuickAddPreviewChip(
                        stringResource(R.string.quick_add_chip_title),
                        parsed.title.ifBlank { untitledLabel },
                        palette,
                        onClick = ::submit,
                    )
                    QuickAddPreviewChip(
                        stringResource(R.string.quick_add_chip_date),
                        parsed.date.format(editorDateFormatter),
                        palette,
                        onClick = ::submit,
                    )
                    QuickAddPreviewChip(
                        stringResource(R.string.quick_add_chip_time),
                        if (parsed.isAllDay || parsed.startTime == null) {
                            stringResource(R.string.template_all_day)
                        } else {
                            quickAddTimeLabel(parsed)
                        },
                        palette,
                        onClick = ::submit,
                    )
                    QuickAddPreviewChip(
                        stringResource(R.string.quick_add_chip_repeats),
                        // Not `::quickAddRepeatLabel` — Kotlin rejects function references to
                        // @Composable lambdas.
                        parsed.rrule?.let { quickAddRepeatLabel(it) }
                            ?: stringResource(R.string.event_repeat_none),
                        palette,
                        onClick = ::submit,
                    )
                }
            } else {
                CalcSectionLabel(stringResource(R.string.quick_add_try_one), palette)
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

            Spacer(modifier = Modifier.height(30.dp))
            val enabled = parsed != null
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (enabled) palette.accent else palette.disabledText.copy(alpha = 0.25f))
                    .noRippleClickable(enabled = enabled) { submit() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(R.string.quick_add_continue),
                    color = if (enabled) NWhite else palette.disabledText,
                    fontFamily = mono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun QuickAddPreviewChip(label: String, value: String, palette: DotCalPalette, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(palette.eventCardSurface)
            .drawBehind {
                drawRoundRect(
                    color = palette.accent.copy(alpha = 0.28f),
                    size = size,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
    ) {
        // Callers pass already-uppercase resources; an uppercase() here breaks Turkish dotless i.
        Text(label, color = palette.secondaryText, fontFamily = mono, fontSize = 10.sp, maxLines = 1)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            value,
            color = palette.primaryText,
            fontFamily = mono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun quickAddWhenLabel(result: QuickAddResult): String {
    val date = result.date.format(editorDateFormatter)
    val time = result.startTime
    val value = if (result.isAllDay || time == null) {
        stringResource(R.string.template_all_day)
    } else {
        time.format(editorTimeFormatter)
    }
    return stringResource(R.string.settings_pair_value, date, value)
}

@Composable
private fun quickAddTimeLabel(result: QuickAddResult): String {
    val start = result.startTime?.format(editorTimeFormatter) ?: return stringResource(R.string.template_all_day)
    val end = result.endTime?.format(editorTimeFormatter)
    return if (end == null) start else "$start-${end}"
}

@Composable
private fun quickAddRepeatLabel(rrule: String): String =
    recurrenceHumanLabel(rrule) ?: stringResource(R.string.quick_add_repeat_custom)

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
            Text(stringResource(R.string.template_apply_title), color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(stringResource(R.string.template_apply_subtitle), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(14.dp))
            if (templates.isEmpty()) {
                Text(stringResource(R.string.template_no_event_templates), color = palette.secondaryText, fontFamily = mono, fontSize = 14.sp, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), textAlign = TextAlign.Center)
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
                stringResource(R.string.templates_title),
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
                Text(stringResource(R.string.template_none_yet), color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    stringResource(R.string.template_none_yet_blurb),
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
            title = stringResource(R.string.template_delete_title),
            confirmLabel = stringResource(R.string.action_delete),
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

@Composable
private fun templateSummaryLabel(t: EventTemplate): String {
    // Every fragment is hoisted into a val before the list is built: `mutableListOf`/`add` run in
    // ordinary (non-composable) code, so a stringResource call cannot live inside them.
    val type = if (t.isTask) {
        stringResource(R.string.template_type_task)
    } else {
        stringResource(R.string.template_type_event)
    }
    val noTimeLabel = stringResource(R.string.template_no_time)
    val allDayLabel = stringResource(R.string.template_all_day)
    val timeLabel = if (t.startMinuteOfDay == null) {
        if (t.isTask) noTimeLabel else allDayLabel
    } else {
        LocalTime.of(t.startMinuteOfDay / 60, t.startMinuteOfDay % 60).format(editorTimeFormatter)
    }
    val repeatsFallback = stringResource(R.string.template_repeats)
    val recurrenceLabel = t.rrule?.let { recurrenceHumanLabel(it) ?: repeatsFallback }
    val separator = stringResource(R.string.template_summary_separator)
    val parts = mutableListOf(type, timeLabel)
    if (!t.isTask && t.startMinuteOfDay != null && t.durationMinutes > 0) {
        parts.add(formatDurationShort(t.durationMinutes))
    }
    recurrenceLabel?.let { parts.add(it) }
    return parts.joinToString(separator)
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
                stringResource(R.string.calendar_sets_title),
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
                Text(stringResource(R.string.calendar_set_none_yet), color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    stringResource(R.string.calendar_set_none_yet_blurb),
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
            label = stringResource(R.string.calendar_set_save_current),
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
    if (showSaveDialog) {
        // Hoisted: the onConfirm lambda is not composable, so stringResource cannot be called inside it.
        val fallbackSetName = stringResource(R.string.calendar_set_default_name)
        TemplateNameDialog(
            title = stringResource(R.string.calendar_set_save_dialog_title),
            defaultName = "",
            palette = palette,
            onDismiss = { showSaveDialog = false },
            onConfirm = { name ->
                onSaveCurrent(name.trim().ifBlank { fallbackSetName })
                showSaveDialog = false
            },
        )
    }
    deleteTarget?.let { target ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.calendar_set_delete_title),
            confirmLabel = stringResource(R.string.action_delete),
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
    deadTimeState: DeadTimeUiState,
    freeTimeStartHour: Int,
    freeTimeEndHour: Int,
    use24HourFormat: Boolean,
    onBack: () -> Unit,
    onRefreshDeadTime: (LocalDate, Int, Int) -> Unit,
    onFreeTimeBoundsChange: (Int, Int) -> Unit,
    onUseFreeSlot: (FreeSlot) -> Unit,
    onShareFreeDay: (LocalDate) -> Unit,
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
    LaunchedEffect(today, freeTimeStartHour, freeTimeEndHour, events) {
        onRefreshDeadTime(today, freeTimeStartHour, freeTimeEndHour)
    }

    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text(stringResource(R.string.insights_title), color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
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
                        TimeInsightDateRow(stringResource(R.string.event_from), rangeStart, palette, Modifier.weight(1f)) { pickingStart = true }
                        TimeInsightDateRow(stringResource(R.string.event_to), rangeEnd, palette, Modifier.weight(1f)) { pickingEnd = true }
                    }
                }
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(palette.eventCardSurface).border(1.dp, palette.eventCardBorder, RoundedCornerShape(24.dp)).padding(18.dp),
                ) {
                    Text("${rangeStart.format(compactDateFormatter)} - ${rangeEnd.format(compactDateFormatter)}", color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(formatInsightHours(stats.totalHours), color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 34.sp)
                    Text(stringResource(R.string.insights_scheduled_hours), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    val noneLabel = stringResource(R.string.insights_metric_none)
                    TimeInsightMetricCard(stringResource(R.string.insights_metric_events), stats.eventCount.toString(), palette, Modifier.weight(1f))
                    // Was dayOfWeek.name.take(3) — the raw English enum constant. Same fix as pass 1.
                    TimeInsightMetricCard(stringResource(R.string.insights_metric_busiest), stats.busiestDay?.dayOfWeek?.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()) ?: noneLabel, palette, Modifier.weight(1f), footer = if (stats.busiestDayHours > 0.0) formatInsightHours(stats.busiestDayHours) else "")
                    TimeInsightMetricCard(stringResource(R.string.insights_metric_tasks), stats.taskCompletionRate?.let { "$it%" } ?: noneLabel, palette, Modifier.weight(1f), footer = if (stats.totalTasks > 0) "${stats.completedTasks}/${stats.totalTasks}" else "")
                }
            }
            item {
                DeadTimeControls(
                    startHour = freeTimeStartHour,
                    endHour = freeTimeEndHour,
                    use24HourFormat = use24HourFormat,
                    palette = palette,
                    onBoundsChange = onFreeTimeBoundsChange,
                )
            }
            when {
                deadTimeState.isLoading && deadTimeState.slots.isEmpty() -> item {
                    Box(Modifier.fillMaxWidth().height(72.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = palette.accent, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    }
                }
                deadTimeState.error != null -> item {
                    ShiftEmptyText(deadTimeState.error, palette)
                }
                deadTimeState.slots.isEmpty() -> item {
                    ShiftEmptyText(stringResource(R.string.insights_no_open_slots), palette)
                }
                else -> lazyItems(
                    items = deadTimeState.slots,
                    key = { slot -> "${slot.date}-${slot.start}-${slot.end}" },
                ) { slot ->
                    DeadTimeSlotRow(slot, use24HourFormat, palette, onUseFreeSlot, onShareFreeDay)
                }
            }
            item {
                SettingsSectionTitle(stringResource(R.string.insights_section_weekday_load), palette)
                WeekdayHoursChart(hours = stats.weekdayHours, palette = palette)
            }
            item {
                SettingsSectionTitle(stringResource(R.string.insights_section_calendars), palette)
                if (stats.accountHours.isEmpty()) {
                    ShiftEmptyText(stringResource(R.string.insights_no_timed_events), palette)
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
            title = stringResource(R.string.calc_start_date_row),
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
            title = stringResource(R.string.calc_end_date_row),
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
private fun DeadTimeControls(
    startHour: Int,
    endHour: Int,
    use24HourFormat: Boolean,
    palette: DotCalPalette,
    onBoundsChange: (Int, Int) -> Unit,
) {
    var pendingBounds by remember(startHour, endHour) {
        mutableStateOf(startHour.toFloat()..endHour.toFloat())
    }
    val pendingStart = pendingBounds.start.roundToInt().coerceIn(0, 22)
    val pendingEnd = pendingBounds.endInclusive.roundToInt().coerceIn(pendingStart + 1, 23)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SettingsSectionTitle(stringResource(R.string.insights_section_dead_time), palette)
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                .background(palette.eventCardSurface)
                .border(1.dp, palette.eventCardBorder, RoundedCornerShape(22.dp))
                .padding(16.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatDeadTimeHour(pendingStart, use24HourFormat), color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(formatDeadTimeHour(pendingEnd, use24HourFormat), color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            RangeSlider(
                value = pendingBounds,
                onValueChange = { pendingBounds = it },
                onValueChangeFinished = { onBoundsChange(pendingStart, pendingEnd) },
                valueRange = 0f..23f,
                steps = 22,
                colors = SliderDefaults.colors(
                    thumbColor = palette.accent,
                    activeTrackColor = palette.accent,
                    inactiveTrackColor = palette.line,
                ),
            )
            Text(stringResource(R.string.availability_next_7_days), color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp)
        }
    }
}

@Composable
private fun DeadTimeSlotRow(
    slot: FreeSlot,
    use24HourFormat: Boolean,
    palette: DotCalPalette,
    onUseSlot: (FreeSlot) -> Unit,
    onShareDay: (LocalDate) -> Unit,
) {
    val minutes = java.time.Duration.between(slot.start, slot.end).toMinutes()
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .background(palette.eventCardSurface)
            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(16.dp))
            .clickable { onUseSlot(slot) }
            .padding(start = 14.dp, top = 10.dp, end = 6.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "${slot.date.format(localizedFormatter("EEE, MMM d"))} · ${formatDeadTime(slot.start, use24HourFormat)}-${formatDeadTime(slot.end, use24HourFormat)}",
                color = palette.primaryText,
                fontFamily = mono,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(formatDeadTimeDuration(minutes), color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp)
        }
        TextButton(onClick = { onShareDay(slot.date) }) {
            Text(stringResource(R.string.availability_share), color = palette.accent, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = "Create event in this slot", tint = palette.secondaryText, modifier = Modifier.size(18.dp))
    }
}

private fun formatDeadTime(time: LocalTime, use24HourFormat: Boolean): String =
    time.format(localizedFormatter(if (use24HourFormat) "HH:mm" else "h:mm a"))

private fun formatDeadTimeHour(hour: Int, use24HourFormat: Boolean): String =
    formatDeadTime(LocalTime.of(hour, 0), use24HourFormat)

private fun formatDeadTimeDuration(minutes: Long): String {
    val hours = minutes / 60
    val remainder = minutes % 60
    return when {
        remainder == 0L -> "$hours h free"
        hours == 0L -> "$remainder min free"
        else -> "$hours h $remainder min free"
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
        Text(date.format(compactDateFormatter), color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
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
    onSharePlan: (ShiftPattern, LocalDate, LocalDate, ShiftPlanShareFormat) -> Unit,
) {
    var showTypeEditor by remember { mutableStateOf(false) }
    var showPatternEditor by remember { mutableStateOf(false) }
    var editingType by remember { mutableStateOf<ShiftType?>(null) }
    var generatingPattern by remember { mutableStateOf<ShiftPattern?>(null) }
    var sharingPattern by remember { mutableStateOf<ShiftPattern?>(null) }
    var deletePattern by remember { mutableStateOf<ShiftPattern?>(null) }
    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart).padding(start = 4.dp).size(44.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = palette.primaryText)
            }
            Text(stringResource(R.string.shift_patterns_title), color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.align(Alignment.Center))
            HorizontalDivider(color = palette.line.copy(alpha = 0.55f), thickness = 1.dp, modifier = Modifier.align(Alignment.BottomCenter))
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 22.dp),
            contentPadding = PaddingValues(top = 18.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                ShiftScreenHeader(
                    palette = palette,
                    typeCount = shiftTypes.size,
                    patternCount = patterns.size,
                )
            }
            item {
                ShiftSectionHeader(
                    title = stringResource(R.string.shift_section_types),
                    actionLabel = stringResource(R.string.shift_add_type),
                    palette = palette,
                    onAction = { showTypeEditor = true },
                )
                if (shiftTypes.isEmpty()) {
                    ShiftEmptyText(stringResource(R.string.shift_types_empty), palette)
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
                Spacer(modifier = Modifier.height(4.dp))
                ShiftSectionHeader(
                    title = stringResource(R.string.shift_section_patterns),
                    actionLabel = stringResource(R.string.shift_build_pattern_row),
                    palette = palette,
                    onAction = { showPatternEditor = true },
                )
                if (patterns.isEmpty()) {
                    ShiftEmptyText(stringResource(R.string.shift_patterns_empty), palette)
                }
            }
            lazyItems(patterns, key = { it.id }) { pattern ->
                ShiftPatternCard(
                    pattern = pattern,
                    shiftTypes = shiftTypes,
                    palette = palette,
                    onGenerate = { generatingPattern = pattern },
                    onShare = { sharingPattern = pattern },
                    onDelete = { deletePattern = pattern },
                )
            }
            item {
                Spacer(modifier = Modifier.height(12.dp))
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
    sharingPattern?.let { pattern ->
        ShiftPlanShareDialog(
            pattern = pattern,
            palette = palette,
            onDismiss = { sharingPattern = null },
            onShare = { start, end, format ->
                onSharePlan(pattern, start, end, format)
                sharingPattern = null
            },
        )
    }
    deletePattern?.let { pattern ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.shift_pattern_delete_title),
            confirmLabel = stringResource(R.string.action_delete),
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
private fun ShiftScreenHeader(palette: DotCalPalette, typeCount: Int, patternCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(palette.eventCardSurface)
            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(palette.accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.EventRepeat, contentDescription = null, tint = palette.accent, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.shift_patterns_title),
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                stringResource(R.string.shift_pattern_counts, typeCount, patternCount),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ShiftSectionHeader(
    title: String,
    actionLabel: String,
    palette: DotCalPalette,
    onAction: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            color = palette.secondaryText,
            fontFamily = mono,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 0.6.sp,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier
                .height(34.dp)
                .clip(RoundedCornerShape(17.dp))
                .background(palette.accent)
                .noRippleClickable(onClick = onAction)
                .padding(start = 10.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = palette.onAccent, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
            Text(actionLabel, color = palette.onAccent, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ShiftEmptyText(text: String, palette: DotCalPalette) {
    Text(text, color = palette.secondaryText, fontFamily = mono, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(vertical = 10.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun QuickShiftAddSheet(
    palette: DotCalPalette,
    shiftTypes: List<ShiftType>,
    accounts: List<CalendarAccount>,
    initialDate: LocalDate,
    initialShiftTypeId: String?,
    initialAccountId: String?,
    onDismiss: () -> Unit,
    onManageTypes: () -> Unit,
    onAddShift: (String, LocalDate, String?) -> Unit,
) {
    val usableTypes = remember(shiftTypes) { shiftTypes.filter { it.generatesEvent } }
    var selectedDate by remember(initialDate) { mutableStateOf(initialDate) }
    var selectedTypeId by remember(usableTypes, initialShiftTypeId) {
        mutableStateOf(usableTypes.firstOrNull { it.id == initialShiftTypeId }?.id ?: usableTypes.firstOrNull()?.id)
    }
    var selectedAccountId by remember(accounts, initialAccountId) {
        mutableStateOf(accounts.firstOrNull { it.id == initialAccountId }?.id ?: accounts.firstOrNull()?.id)
    }
    val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId } ?: accounts.firstOrNull()
    val effectiveAccountId = selectedAccount?.id
    var showDatePicker by remember { mutableStateOf(false) }
    var showCalendarPicker by remember { mutableStateOf(false) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
                .padding(top = 4.dp, bottom = 24.dp),
        ) {
            Text(
                stringResource(R.string.shift_quick_add_title),
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(R.string.shift_quick_add_subtitle),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
            ShiftDateRow(
                label = stringResource(R.string.shift_quick_add_date),
                date = selectedDate,
                palette = palette,
                onClick = { showDatePicker = true },
            )
            Spacer(Modifier.height(14.dp))
            SettingsSectionTitle(stringResource(R.string.shift_section_types), palette)
            if (usableTypes.isEmpty()) {
                ShiftEmptyText(stringResource(R.string.shift_quick_add_empty), palette)
                CalendarAddAccountRow(
                    palette = palette,
                    onClick = {
                        onDismiss()
                        onManageTypes()
                    },
                    label = stringResource(R.string.shift_manage_types),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    lazyItems(usableTypes, key = { it.id }) { type ->
                        QuickShiftTypeRow(
                            type = type,
                            selected = type.id == selectedTypeId,
                            palette = palette,
                            onClick = { selectedTypeId = type.id },
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                if (selectedAccount != null) {
                    QuickShiftCalendarRow(
                        account = selectedAccount,
                        palette = palette,
                        onClick = { showCalendarPicker = true },
                    )
                    Spacer(Modifier.height(18.dp))
                }
                Button(
                    onClick = {
                        selectedTypeId?.let { onAddShift(it, selectedDate, effectiveAccountId) }
                    },
                    enabled = selectedTypeId != null,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent, contentColor = palette.onAccent),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Text(stringResource(R.string.shift_quick_add_confirm), fontFamily = mono, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
    if (showDatePicker) {
        DateTimeChoiceSheet(
            title = stringResource(R.string.shift_quick_add_date),
            selectedDate = selectedDate,
            selectedTime = LocalTime.NOON,
            minDate = null,
            includeTime = false,
            palette = palette,
            onDismiss = { showDatePicker = false },
            onSelected = { date, _ ->
                selectedDate = date
                showDatePicker = false
            },
        )
    }
    if (showCalendarPicker) {
        QuickShiftCalendarDialog(
            accounts = accounts,
            selectedAccountId = effectiveAccountId,
            palette = palette,
            onDismiss = { showCalendarPicker = false },
            onSelected = { accountId ->
                selectedAccountId = accountId
                showCalendarPicker = false
            },
        )
    }
}

internal enum class ShiftPlanShareFormat { Image, Pdf, Ics, Qr }

@Composable
private fun QuickShiftTypeRow(
    type: ShiftType,
    selected: Boolean,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) palette.accent.copy(alpha = 0.12f) else palette.eventCardSurface)
            .border(1.dp, if (selected) palette.accent.copy(alpha = 0.45f) else palette.line.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(14.dp).clip(CircleShape).background(Color(parseColor(type.colorHex))))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(type.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(shiftTypeSummary(type), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = palette.accent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun QuickShiftCalendarRow(
    account: CalendarAccount,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, palette.textFieldBorder, RoundedCornerShape(14.dp))
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(Color(parseColor(account.color))))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.shift_calendar), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
            Spacer(Modifier.height(2.dp))
            Text(
                account.displayName.readableCalendarLabel(),
                color = palette.primaryText,
                fontFamily = mono,
                fontSize = 15.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun QuickShiftCalendarDialog(
    accounts: List<CalendarAccount>,
    selectedAccountId: String?,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onSelected: (String) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        title = {
            Text(stringResource(R.string.shift_calendar), color = palette.primaryText, fontFamily = LocalHeadingFont.current)
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                lazyItems(accounts, key = { it.id }) { account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (account.id == selectedAccountId) palette.accent.copy(alpha = 0.10f) else Color.Transparent)
                            .noRippleClickable(onClick = { onSelected(account.id) })
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(Color(parseColor(account.color))))
                        Text(
                            account.displayName.readableCalendarLabel(),
                            color = palette.primaryText,
                            fontFamily = mono,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f).padding(start = 12.dp),
                        )
                        if (account.id == selectedAccountId) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = palette.accent, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = palette.primaryText, fontFamily = mono)
            }
        },
    )
}

@Composable
private fun ShiftTypeCard(type: ShiftType, palette: DotCalPalette, onClick: () -> Unit, onDelete: () -> Unit) {
    val typeColor = remember(type.colorHex) { Color(parseColor(type.colorHex)) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.eventCardSurface)
            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(18.dp))
            .noRippleClickable(onClick = onClick)
            .padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(typeColor.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(13.dp).clip(CircleShape).background(typeColor))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(type.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(3.dp))
            Text(shiftTypeSummary(type), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        ShiftMiniActionButton(icon = Icons.Default.DeleteOutline, contentDescription = "Delete shift type", tint = palette.secondaryText, palette = palette, onClick = onDelete)
    }
}

@Composable
private fun ShiftPatternCard(
    pattern: ShiftPattern,
    shiftTypes: List<ShiftType>,
    palette: DotCalPalette,
    onGenerate: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
) {
    val typeMap = remember(shiftTypes) { shiftTypes.associateBy { it.id } }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(palette.eventCardSurface)
            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(18.dp))
            .noRippleClickable(onClick = onGenerate)
            .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(pattern.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(5.dp))
            Text(shiftPatternSummary(pattern, typeMap), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, lineHeight = 17.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.width(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            ShiftMiniActionButton(icon = Icons.Default.CalendarMonth, contentDescription = "Generate shifts", tint = palette.accent, palette = palette, onClick = onGenerate)
            ShiftMiniActionButton(icon = Icons.Default.Share, contentDescription = stringResource(R.string.shift_share_plan), tint = palette.secondaryText, palette = palette, onClick = onShare)
            ShiftMiniActionButton(icon = Icons.Default.DeleteOutline, contentDescription = "Delete shift pattern", tint = palette.secondaryText, palette = palette, onClick = onDelete)
        }
    }
}

@Composable
private fun ShiftMiniActionButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    palette: DotCalPalette,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(palette.cell.copy(alpha = 0.55f)),
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(19.dp))
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
        title = { Text(stringResource(if (existing == null) R.string.shift_type_title else R.string.shift_type_edit_title), color = palette.primaryText, fontFamily = LocalHeadingFont.current) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.shift_field_name)) }, singleLine = true, colors = dotCalTextFieldColors(palette), textStyle = TextStyle(color = palette.primaryText, fontFamily = mono))
                SettingsToggleRow(title = stringResource(R.string.shift_off_day), checked = isOff, palette = palette, onCheckedChange = { isOff = it })
                if (!isOff) {
                    OutlinedTextField(value = startHour, onValueChange = { startHour = it.filter(Char::isDigit).take(2) }, label = { Text(stringResource(R.string.shift_field_start_hour)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = dotCalTextFieldColors(palette), textStyle = TextStyle(color = palette.primaryText, fontFamily = mono))
                    OutlinedTextField(value = durationHours, onValueChange = { durationHours = it.filter(Char::isDigit).take(2) }, label = { Text(stringResource(R.string.shift_field_duration_hours)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = dotCalTextFieldColors(palette), textStyle = TextStyle(color = palette.primaryText, fontFamily = mono))
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
            ) { Text(stringResource(R.string.action_save), color = if (name.isNotBlank()) palette.accent else palette.disabledText) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = palette.primaryText) } },
    )
    if (showColorPicker) {
        CustomAccentPickerDialog(
            initial = Color(parseColor(color)),
            palette = palette,
            title = stringResource(R.string.shift_color_title),
            onDismiss = { showColorPicker = false },
            onConfirm = {
                color = it
                showColorPicker = false
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShiftPatternEditorDialog(
    palette: DotCalPalette,
    shiftTypes: List<ShiftType>,
    onDismiss: () -> Unit,
    onSave: (ShiftPattern) -> Unit,
) {
    // Hoisted: the remember {} lambda is not composable.
    val defaultPatternName = stringResource(R.string.shift_pattern_default_name)
    var name by remember { mutableStateOf(defaultPatternName) }
    var cycle by remember { mutableStateOf<List<String>>(emptyList()) }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        title = { Text(stringResource(R.string.shift_build_pattern_title), color = palette.primaryText, fontFamily = LocalHeadingFont.current) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 560.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.shift_field_name)) }, singleLine = true, colors = dotCalTextFieldColors(palette), textStyle = TextStyle(color = palette.primaryText, fontFamily = mono))
                ShiftDateRow(label = stringResource(R.string.calc_start_date_row), date = startDate, palette = palette, onClick = { showStartDatePicker = true })

                Text(stringResource(R.string.shift_pattern_type_picker), color = palette.secondaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.6.sp)
                if (shiftTypes.isEmpty()) {
                    ShiftEmptyText(stringResource(R.string.shift_types_empty), palette)
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        shiftTypes.forEach { type ->
                            ShiftPatternTypeButton(
                                type = type,
                                palette = palette,
                                onClick = { cycle = cycle + type.id },
                            )
                        }
                    }
                }

                ShiftPatternCyclePreview(
                    cycle = cycle,
                    typeMap = shiftTypes.associateBy { it.id },
                    palette = palette,
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { cycle = cycle.dropLast(1) }, enabled = cycle.isNotEmpty()) {
                        Text(stringResource(R.string.shift_remove_last), color = if (cycle.isNotEmpty()) palette.accent else palette.disabledText, fontFamily = mono)
                    }
                    TextButton(onClick = { cycle = emptyList() }, enabled = cycle.isNotEmpty()) {
                        Text(stringResource(R.string.calendar_clear), color = if (cycle.isNotEmpty()) palette.secondaryText else palette.disabledText, fontFamily = mono)
                    }
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
            ) { Text(stringResource(R.string.action_save), color = palette.accent) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = palette.primaryText) } },
    )
    if (showStartDatePicker) {
        DateTimeChoiceSheet(
            title = stringResource(R.string.shift_pattern_start),
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
private fun ShiftPatternTypeButton(type: ShiftType, palette: DotCalPalette, onClick: () -> Unit) {
    val typeColor = remember(type.colorHex) { Color(parseColor(type.colorHex)) }
    Row(
        modifier = Modifier
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.eventCardSurface)
            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(14.dp))
            .noRippleClickable(onClick = onClick)
            .padding(start = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(11.dp).clip(CircleShape).background(typeColor))
        Spacer(Modifier.width(8.dp))
        Text(type.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.width(7.dp))
        Icon(Icons.Default.Add, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ShiftPatternCyclePreview(
    cycle: List<String>,
    typeMap: Map<String, ShiftType>,
    palette: DotCalPalette,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(palette.eventCardSurface)
            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.shift_pattern_cycle_label), color = palette.secondaryText, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 0.6.sp, modifier = Modifier.weight(1f))
            if (cycle.isNotEmpty()) {
                Text(stringResource(R.string.shift_pattern_cycle_days, cycle.size), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        if (cycle.isEmpty()) {
            Text(stringResource(R.string.shift_pattern_cycle_empty), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp, lineHeight = 17.sp)
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                lazyItems(cycle.withIndex().toList(), key = { "${it.index}-${it.value}" }) { item ->
                    val type = typeMap[item.value]
                    ShiftPatternCycleChip(
                        index = item.index + 1,
                        label = type?.name ?: item.value,
                        colorHex = type?.colorHex ?: "#FF3B30",
                        palette = palette,
                    )
                }
            }
        }
    }
}

@Composable
private fun ShiftPatternCycleChip(index: Int, label: String, colorHex: String, palette: DotCalPalette) {
    val typeColor = remember(colorHex) { Color(parseColor(colorHex)) }
    Row(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(typeColor.copy(alpha = 0.13f))
            .border(1.dp, typeColor.copy(alpha = 0.35f), RoundedCornerShape(13.dp))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(index.toString(), color = typeColor, fontFamily = mono, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        Spacer(Modifier.width(7.dp))
        Text(label, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
        title = { Text(stringResource(R.string.shift_generate_title), color = palette.primaryText, fontFamily = LocalHeadingFont.current) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(pattern.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold)
                ShiftDateRow(label = stringResource(R.string.shift_generate_from), date = startDate, palette = palette, onClick = { showStartDatePicker = true })
                OutlinedTextField(value = months, onValueChange = { months = it.filter(Char::isDigit).take(2) }, label = { Text(stringResource(R.string.shift_months_ahead)) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), colors = dotCalTextFieldColors(palette), textStyle = TextStyle(color = palette.primaryText, fontFamily = mono))
                Text(stringResource(R.string.shift_calendar), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    lazyItems(accounts, key = { it.id }) { account ->
                        ShiftChip(account.displayName.readableCalendarLabel(), palette, selected = account.id == accountId, onClick = { accountId = account.id })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onGenerate(startDate, months.toIntOrNull()?.coerceIn(1, 24) ?: 6, accountId) }) {
                Text(stringResource(R.string.shift_generate_confirm), color = palette.accent)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = palette.primaryText) } },
    )
    if (showStartDatePicker) {
        DateTimeChoiceSheet(
            title = stringResource(R.string.shift_generate_from),
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
private fun ShiftPlanShareDialog(
    pattern: ShiftPattern,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onShare: (LocalDate, LocalDate, ShiftPlanShareFormat) -> Unit,
) {
    var startDate by remember(pattern.id) { mutableStateOf(LocalDate.now()) }
    var days by remember(pattern.id) { mutableStateOf("14") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    val dayCount = days.toIntOrNull()?.coerceIn(1, 90) ?: 14
    val rangeEnd = startDate.plusDays(dayCount.toLong() - 1L)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        title = {
            Text(stringResource(R.string.shift_share_plan_title), color = palette.primaryText, fontFamily = LocalHeadingFont.current)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(pattern.name, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold)
                ShiftDateRow(label = stringResource(R.string.shift_share_from), date = startDate, palette = palette, onClick = { showStartDatePicker = true })
                OutlinedTextField(
                    value = days,
                    onValueChange = { days = it.filter(Char::isDigit).take(2) },
                    label = { Text(stringResource(R.string.shift_share_days)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = dotCalTextFieldColors(palette),
                    textStyle = TextStyle(color = palette.primaryText, fontFamily = mono),
                )
                Text(
                    stringResource(R.string.shift_share_qr_limit),
                    color = palette.secondaryText,
                    fontFamily = mono,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                )
                ShiftPlanShareButton(stringResource(R.string.shift_share_image), palette) { onShare(startDate, rangeEnd, ShiftPlanShareFormat.Image) }
                ShiftPlanShareButton(stringResource(R.string.shift_share_pdf), palette) { onShare(startDate, rangeEnd, ShiftPlanShareFormat.Pdf) }
                ShiftPlanShareButton(stringResource(R.string.shift_share_ics), palette) { onShare(startDate, rangeEnd, ShiftPlanShareFormat.Ics) }
                ShiftPlanShareButton(stringResource(R.string.shift_share_qr), palette) { onShare(startDate, rangeEnd, ShiftPlanShareFormat.Qr) }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel), color = palette.primaryText) } },
    )
    if (showStartDatePicker) {
        DateTimeChoiceSheet(
            title = stringResource(R.string.shift_share_from),
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
private fun ShiftPlanShareButton(label: String, palette: DotCalPalette, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(palette.eventCardSurface)
            .noRippleClickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = palette.primaryText, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = palette.secondaryText, modifier = Modifier.size(20.dp))
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
            Text(stringResource(R.string.shift_color_label), color = palette.secondaryText, fontFamily = mono, fontSize = 12.sp)
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

@Composable
private fun shiftTypeSummary(type: ShiftType): String {
    if (!type.generatesEvent) return stringResource(R.string.shift_summary_off)
    val allDay = stringResource(R.string.shift_summary_all_day)
    val start = type.startMinuteOfDay?.let { LocalTime.of(it / 60, it % 60).format(editorTimeFormatter) } ?: allDay
    val duration = type.durationMinutes?.let { formatDurationShort(it) } ?: allDay
    return stringResource(R.string.shift_summary_range, start, duration)
}

@Composable
private fun shiftPatternSummary(pattern: ShiftPattern, types: Map<String, ShiftType>): String =
    stringResource(
        R.string.shift_pattern_summary,
        pluralStringResource(R.plurals.shift_day_cycle, pattern.cycleShiftTypeIds.size, pattern.cycleShiftTypeIds.size),
        shiftCycleLabel(pattern.cycleShiftTypeIds, types),
    )

@Composable
private fun shiftCycleLabel(ids: List<String>, types: Map<String, ShiftType>): String {
    // Hoisted: joinToString / map / ifBlank lambdas are not composable.
    val missing = stringResource(R.string.shift_type_missing)
    val none = stringResource(R.string.shift_no_shifts_selected)
    return ids.map { types[it]?.name ?: missing }.joinToString(", ").ifBlank { none }
}

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
                stringResource(R.string.trash_title),
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.align(Alignment.Center),
            )
            if (items.isNotEmpty()) {
                Text(
                    stringResource(R.string.trash_empty_action),
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
                        stringResource(R.string.trash_nothing_here),
                        color = palette.secondaryText,
                        fontFamily = LocalHeadingFont.current,
                        fontSize = 16.sp,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.trash_nothing_here_blurb),
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
                        stringResource(R.string.trash_swipe_hint),
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
            title = stringResource(R.string.trash_delete_permanently_title),
            confirmLabel = stringResource(R.string.action_delete),
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
            title = stringResource(R.string.trash_empty_confirm_title),
            confirmLabel = stringResource(R.string.trash_empty_action),
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
    // Hoisted: the ifBlank {} lambda below is not composable.
    val noTitleLabel = stringResource(R.string.vault_no_title)
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
                Text(stringResource(R.string.action_restore), color = palette.background, fontFamily = mono, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(actionButtonWidth)
                    .background(Color(0xFFFF3B30))
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.action_delete), color = Color.White, fontFamily = mono, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
                    text = event.title.ifBlank { noTitleLabel },
                    color = palette.primaryText,
                    fontFamily = mono,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(R.string.trash_deleted_when, deletedWhenLabel(event), deletedAgoLabel(snapshot.deletedAtMs, nowMs)),
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
@Composable
private fun deletedWhenLabel(event: CalendarEvent): String {
    val prefix = stringResource(if (event.isTask == 1) R.string.vault_type_task else R.string.vault_type_event)
    // Tasks with no due date store startTimeMs = 0.
    if (event.isTask == 1 && event.startTimeMs <= 0L) {
        return stringResource(R.string.settings_pair_value, prefix, stringResource(R.string.trash_no_due_date))
    }
    val zone = runCatching { java.time.ZoneId.of(event.timeZone) }.getOrDefault(java.time.ZoneId.systemDefault())
    val start = java.time.Instant.ofEpochMilli(event.startTimeMs).atZone(zone)
    val date = start.format(localizedFormatter("MMM d, yyyy"))
    val value = if (event.isAllDay == 1) date else "$date, ${start.format(localizedFormatter("h:mm a"))}"
    return stringResource(R.string.settings_pair_value, prefix, value)
}

/** Relative "deleted X ago" phrasing from a deletion timestamp. */
@Composable
private fun deletedAgoLabel(deletedAtMs: Long, nowMs: Long): String {
    val diff = (nowMs - deletedAtMs).coerceAtLeast(0L)
    val minutes = diff / 60_000L
    val hours = diff / 3_600_000L
    val days = diff / 86_400_000L
    return when {
        minutes < 1 -> stringResource(R.string.trash_just_now)
        minutes < 60 -> pluralStringResource(R.plurals.trash_minutes_ago, minutes.toInt(), minutes.toInt())
        hours < 24 -> pluralStringResource(R.plurals.trash_hours_ago, hours.toInt(), hours.toInt())
        days < 30 -> pluralStringResource(R.plurals.trash_days_ago, days.toInt(), days.toInt())
        else -> pluralStringResource(R.plurals.trash_days_ago, 30, 30)
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
                stringResource(R.string.calc_title),
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
                options = listOf(stringResource(R.string.calc_mode_days_between), stringResource(R.string.calc_mode_add_subtract)),
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
                CalcSectionLabel(stringResource(R.string.calc_date_range), palette)
                Spacer(modifier = Modifier.height(10.dp))
                CalcFieldGroup(palette) {
                    CalcDateRow(stringResource(R.string.event_from), fromDate, palette) { picker = CalcDateField.From }
                    HorizontalDivider(color = palette.line.copy(alpha = 0.4f), thickness = 1.dp)
                    CalcDateRow(stringResource(R.string.event_to), toDate, palette) { picker = CalcDateField.To }
                }
                Spacer(modifier = Modifier.height(24.dp))
                (result as? DateCalculatorViewModel.CalculatorResult.DaysBetween)?.let { r ->
                    CalcSectionLabel(stringResource(R.string.calc_result), palette)
                    Spacer(modifier = Modifier.height(10.dp))
                    CalcResultCard(palette) {
                        CalcResultHero("${r.totalDays}", pluralStringResource(R.plurals.calc_days_total, r.totalDays), palette)
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = palette.eventCardBorder, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        CalcResultLine(stringResource(R.string.calc_working_days), "${r.workingDays}", palette)
                        CalcResultLine(stringResource(R.string.calc_weekends), "${r.weekends}", palette)
                    }
                }
            } else {
                CalcSectionLabel(stringResource(R.string.calc_start_date), palette)
                Spacer(modifier = Modifier.height(10.dp))
                CalcFieldGroup(palette) {
                    CalcDateRow(stringResource(R.string.calc_start_date_row), startDate, palette) { picker = CalcDateField.Start }
                }
                Spacer(modifier = Modifier.height(24.dp))

                CalcSectionLabel(stringResource(R.string.calc_operation), palette)
                Spacer(modifier = Modifier.height(10.dp))
                TwoOptionSegmentedControl(
                    options = listOf(stringResource(R.string.calc_add), stringResource(R.string.calc_subtract)),
                    selectedIndex = if (isSubtract) 1 else 0,
                    palette = palette,
                    onSelected = { calcViewModel.setSubtract(it == 1) },
                )
                Spacer(modifier = Modifier.height(24.dp))

                CalcSectionLabel(stringResource(R.string.calc_number_of_days), palette)
                Spacer(modifier = Modifier.height(10.dp))
                CalcDaysStepper(
                    days = daysCount,
                    palette = palette,
                    onChange = { calcViewModel.setDaysCount(it) },
                )
                Spacer(modifier = Modifier.height(24.dp))

                (result as? DateCalculatorViewModel.CalculatorResult.AddSubtractResult)?.let { r ->
                    CalcSectionLabel(stringResource(R.string.calc_result), palette)
                    Spacer(modifier = Modifier.height(10.dp))
                    CalcResultCard(palette) {
                        Text(
                            // Whole sentence per plural so translators control word order,
                            // rather than concatenating "N days" + "before"/"after" in Kotlin.
                            pluralStringResource(
                                if (isSubtract) R.plurals.calc_days_before_start else R.plurals.calc_days_after_start,
                                daysCount,
                                daysCount,
                            ),
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
                CalcDateField.From -> stringResource(R.string.event_from)
                CalcDateField.To -> stringResource(R.string.event_to)
                CalcDateField.Start -> stringResource(R.string.calc_start_date_row)
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
    val formatter = detailDateFormatter
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
            // The old uppercase(Locale.getDefault()) is gone: locale-hostile (Turkish dotless i)
            // and the formatter already yields the locale's own casing.
            date?.format(formatter) ?: stringResource(R.string.calc_select_date),
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
        // Callers pass an already-uppercase resource: an uppercase() here would break Turkish
        // dotless i. Same rule as recurrenceDetailLabel — write the resource in its display case.
        text,
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
