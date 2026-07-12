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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventDetailScreen(
    event: CalendarEvent,
    reminders: List<EventReminder>,
    account: CalendarAccount?,
    palette: DotCalPalette,
    isPrivate: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onShare: () -> Unit,
    onDuplicate: () -> Unit,
    onCopyToDate: () -> Unit,
    onMoveToPrivate: () -> Unit,
    onRestoreFromPrivate: () -> Unit,
    onDelete: () -> Unit,
) {
    val isReadOnly = event.source == "BIRTHDAY" || event.source == "HOLIDAY"
    val imageUris = remember(event.imageUris) { parseJsonStringArray(event.imageUris) }
    var previewImageUri by remember { mutableStateOf<String?>(null) }
    var showActions by remember { mutableStateOf(false) }
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
                    fontFamily = LocalHeadingFont.current,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
                Box(modifier = Modifier.width(48.dp).height(48.dp), contentAlignment = Alignment.Center) {
                    IconButton(onClick = { showActions = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = palette.primaryText)
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
                        fontFamily = LocalHeadingFont.current,
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
        if (showActions) {
            val actions = buildList {
                if (!isReadOnly) {
                    add(CompactActionItem("Edit") {
                        showActions = false
                        onEdit()
                    })
                }
                add(CompactActionItem("Share") {
                    showActions = false
                    onShare()
                })
                if (!isReadOnly) {
                    add(CompactActionItem("Duplicate") {
                        showActions = false
                        onDuplicate()
                    })
                    add(CompactActionItem("Copy to date") {
                        showActions = false
                        onCopyToDate()
                    })
                    add(CompactActionItem(if (isPrivate) "Restore From Private Vault" else "Move to Private Vault") {
                        showActions = false
                        if (isPrivate) onRestoreFromPrivate() else onMoveToPrivate()
                    })
                }
            }
            ModalBottomSheet(
                onDismissRequest = { showActions = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = palette.dialogSurface,
                dragHandle = { BottomSheetDragHandle(palette) },
            ) {
                CompactActionSheetContent(
                    title = "Event Options",
                    actions = actions,
                    palette = palette,
                )
            }
        }
    }
}

internal data class CompactActionItem(
    val label: String,
    val onClick: () -> Unit,
)

@Composable
internal fun CompactActionSheetContent(
    title: String,
    actions: List<CompactActionItem>,
    palette: DotCalPalette,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.dialogSurface)
            .padding(horizontal = 20.dp)
            .padding(bottom = 22.dp),
    ) {
        Text(
            title,
            color = palette.primaryText,
            fontFamily = LocalHeadingFont.current,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
        actions.forEach { action ->
            Text(
                action.label,
                color = palette.primaryText,
                fontFamily = mono,
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = action.onClick)
                    .padding(vertical = 16.dp),
            )
            HorizontalDivider(color = palette.line.copy(alpha = 0.45f), thickness = 1.dp)
        }
    }
}

@Composable
internal fun DetailSection(label: String, palette: DotCalPalette, content: @Composable () -> Unit) {
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
internal fun DetailDivider(palette: DotCalPalette) {
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
internal fun dotCalTextFieldColors(palette: DotCalPalette) = OutlinedTextFieldDefaults.colors(
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
            if (permissionDenied) "MIC PERMISSION DENIED - TAP TO ENABLE" else "TAP TO RECORD",
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
internal fun EventEditorScreen(
    event: CalendarEvent?,
    editorSessionKey: String,
    selectedDate: LocalDate,
    selectedTime: LocalTime,
    initialReminderMinutes: Int?,
    defaultEventDurationMinutes: Int = 60,
    accounts: List<CalendarAccount>,
    lastSelectedAccountId: String?,
    palette: DotCalPalette,
    isPro: Boolean,
    conflictWarnings: List<CalendarEvent> = emptyList(),
    use24HourFormat: Boolean = true,
    onConflictRangeChanged: (CalendarEvent?, LocalDate, LocalDate, LocalTime, LocalTime, Boolean) -> Unit = { _, _, _, _, _, _ -> },
    onRequestPro: () -> Unit,
    onDismiss: () -> Unit,
    onSave: (EventEditorData, RecurringEditScope) -> Unit,
    onDelete: ((RecurringEditScope) -> Unit)?,
    prefill: QuickAddResult? = null,
    draftPrefill: EventEditorData? = null,
    templatePrefill: EventTemplate? = null,
    onSaveTemplate: ((EventTemplate) -> Unit)? = null,
) {
    // Quick-add prefill only seeds a brand-new event, never an existing one being edited.
    val draft = if (event == null) draftPrefill else null
    val seed = if (event == null && draft == null) prefill else null
    // Template prefill likewise only seeds a brand-new event. Applied to the current date.
    val tpl = if (event == null && draft == null) templatePrefill else null
    val tplStartTime: LocalTime? = tpl?.startMinuteOfDay?.let { LocalTime.of(it / 60, it % 60) }
    val editorDate = event?.localDate() ?: draft?.date ?: seed?.date ?: selectedDate
    val initialStart = event?.startLocalTime() ?: draft?.startTime ?: seed?.startTime ?: tplStartTime ?: selectedTime
    val tplEndTime: LocalTime? = if (tpl != null && tplStartTime != null) {
        tplStartTime.plusMinutes(tpl.durationMinutes.toLong())
    } else null
    val tplEndDate: LocalDate? = if (tpl != null && tpl.startMinuteOfDay != null) {
        editorDate.plusDays(((tpl.startMinuteOfDay + tpl.durationMinutes) / (24 * 60)).toLong())
    } else null
    val defaultEndDateTime = editorDate.atTime(initialStart).plusMinutes(defaultEventDurationMinutes.toLong())
    val initialEnd = event?.endLocalTime() ?: draft?.endTime ?: seed?.endTime ?: tplEndTime ?: defaultEndDateTime.toLocalTime()
    val initialEndDate = event?.endLocalDateForEditor() ?: draft?.endDate ?: seed?.endDate ?: tplEndDate ?: defaultEndDateTime.toLocalDate()
    val editorStateKey = event?.id ?: editorSessionKey
    val draftEventId = remember(editorStateKey) {
        if (event == null || event.isRecurrenceOccurrence()) UUID.randomUUID().toString() else event.baseEventId()
    }
    var title by remember(editorStateKey) { mutableStateOf(event?.title ?: draft?.title ?: seed?.title ?: tpl?.title ?: "") }
    var description by remember(editorStateKey) { mutableStateOf(event?.description ?: draft?.description ?: tpl?.description ?: "") }
    var location by remember(editorStateKey) { mutableStateOf(event?.location ?: draft?.location ?: tpl?.location ?: "") }
    var startDate by remember(editorStateKey) { mutableStateOf(editorDate) }
    var endDate by remember(editorStateKey) { mutableStateOf(maxOf(editorDate, initialEndDate)) }
    var startTime by remember(editorStateKey) { mutableStateOf(initialStart) }
    var endTime by remember(editorStateKey) {
        mutableStateOf(if (initialEndDate > editorDate) initialEnd else coerceEndAfterStart(initialStart, initialEnd))
    }
    var allDay by remember(editorStateKey) { mutableStateOf(event?.let { it.isAllDay == 1 } ?: draft?.isAllDay ?: seed?.isAllDay ?: tpl?.isAllDay ?: false) }
    val draftReminderMinutes = remember(editorStateKey) { draft?.reminderMinutesList?.distinct()?.sorted() }
    var reminderEdited by remember(editorStateKey) { mutableStateOf(false) }
    var reminderMinutes by remember(editorStateKey, initialReminderMinutes) {
        mutableStateOf(if (tpl != null) tpl.reminderMinutes else draft?.reminderMinutes ?: draftReminderMinutes?.firstOrNull() ?: initialReminderMinutes)
    }
    var recurrenceRule by remember(editorStateKey) { mutableStateOf(event?.rrule ?: draft?.rrule ?: seed?.rrule ?: tpl?.rrule) }
    var imageUris by remember(editorStateKey) { mutableStateOf(parseJsonStringArray(event?.imageUris ?: "[]")) }
    var voiceNotePath by remember(editorStateKey) { mutableStateOf(event?.voiceNotePath) }
    val writableAccounts = accounts
    var selectedAccountId by remember(editorStateKey, writableAccounts, lastSelectedAccountId) {
        mutableStateOf(event?.accountId?.takeIf { id -> writableAccounts.any { it.id == id } }
            ?: draft?.accountId?.takeIf { id -> writableAccounts.any { it.id == id } }
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
            val data = if (granted) pending.first else pending.first.copy(reminderMinutes = null, reminderMinutesList = null)
            onSave(data, pending.second)
            pendingPermissionSave = null
            if (!granted) showDotCalToast(context, palette, "Event saved without reminder")
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
            reminderMinutesList = if (!reminderEdited) draftReminderMinutes else null,
            rrule = recurrenceRule,
            imageUris = imageUris.toJsonStringArray(),
            voiceNotePath = voiceNotePath,
            colorHex = draft?.colorHex ?: event?.colorHex,
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
    LaunchedEffect(event?.id, startDate, endDate, startTime, endTime, allDay) {
        onConflictRangeChanged(event, startDate, endDate, startTime, endTime, allDay)
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
                fontFamily = LocalHeadingFont.current,
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
                DotCalSwitch(
                    checked = allDay,
                    palette = palette,
                    onCheckedChange = {
                        clearEditorFocus()
                        allDay = it
                    },
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
            ConflictWarningSection(
                conflicts = conflictWarnings,
                use24HourFormat = use24HourFormat,
                palette = palette,
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
                showDotCalToast(context, palette, "Template saved")
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
                reminderEdited = true
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
private fun ConflictWarningSection(
    conflicts: List<CalendarEvent>,
    use24HourFormat: Boolean,
    palette: DotCalPalette,
) {
    if (conflicts.isEmpty()) return
    val visibleConflicts = conflicts.take(3)
    val extraCount = (conflicts.size - visibleConflicts.size).coerceAtLeast(0)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(palette.accent.copy(alpha = if (palette.isDark) 0.14f else 0.08f))
            .drawBehind {
                drawRoundRect(
                    color = palette.accent.copy(alpha = 0.45f),
                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        visibleConflicts.forEach { conflict ->
            Text(
                text = "Overlaps with ${conflict.title.ifBlank { "Untitled" }} ${conflict.conflictTimeRangeLabel(use24HourFormat)}",
                color = palette.primaryText,
                fontFamily = mono,
                fontSize = 12.sp,
                lineHeight = 16.sp,
            )
        }
        if (extraCount > 0) {
            Text(
                text = "+$extraCount more",
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 12.sp,
            )
        }
    }
}

private fun CalendarEvent.conflictTimeRangeLabel(use24HourFormat: Boolean): String {
    val formatter = if (use24HourFormat) timeFormatter else editorTimeFormatter
    val start = Instant.ofEpochMilli(startTimeMs).atZone(ZoneId.systemDefault())
    val end = Instant.ofEpochMilli(normalizedEndTimeMs()).atZone(ZoneId.systemDefault())
    val startLabel = start.toLocalTime().format(formatter)
    val endLabel = end.toLocalTime().format(formatter)
    return if (use24HourFormat) "$startLabel-$endLabel" else "$startLabel-$endLabel".lowercase(Locale.US)
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
internal fun EditorValueRow(
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
internal fun DateTimeChoiceSheet(
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
            Text(title, color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontSize = 20.sp)
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
internal fun <T> WheelColumn(
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
internal fun ReminderChoiceSheet(selected: Int?, palette: DotCalPalette, onDismiss: () -> Unit, onSelected: (Int?) -> Unit) {
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
internal fun RepeatChoiceSheet(
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
            Text("Repeat", color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
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
                label = if (isCustomSelected) (selectedRule?.humanLabel() ?: "Custom...") else "Custom...",
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
            Text("Custom Repeat", color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
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
                CalcStepperButton("-", palette) { interval = (interval - 1).coerceAtLeast(1) }
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
                    CalcStepperButton("-", palette) { countN = (countN - 1).coerceAtLeast(1) }
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
internal fun ApplyScopeChoiceSheet(
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
internal fun <T> ChoiceSheetContent(
    title: String,
    items: List<T>,
    selected: T,
    label: (T) -> String,
    palette: DotCalPalette,
    onSelected: (T) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().background(palette.dialogSurface).padding(horizontal = 20.dp).padding(bottom = 16.dp)) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(title, color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
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
internal fun BottomSheetDragHandle(palette: DotCalPalette) {
    Box(
        modifier = Modifier
            .padding(top = 12.dp, bottom = 8.dp)
            .size(width = 36.dp, height = 4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(palette.dragHandle),
    )
}
