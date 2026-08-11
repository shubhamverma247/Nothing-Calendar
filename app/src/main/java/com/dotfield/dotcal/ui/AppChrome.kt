package com.dotfield.dotcal.ui

import android.app.Activity
import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.dotfield.dotcal.R
import com.dotfield.dotcal.ui.theme.NWhite

@Composable
internal fun SystemBarColorSync(palette: DotCalPalette) {
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
        window.isNavigationBarContrastEnforced = false
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !palette.isDark
        controller.isAppearanceLightNavigationBars = !palette.isDark
    }
}

@Composable
internal fun CalendarTabContainer(
    title: String,
    activeCalendarTab: CalendarTab,
    palette: DotCalPalette,
    onTitleClick: () -> Unit,
    onTitleLongClick: () -> Unit,
    onAdd: () -> Unit,
    onTemplates: (() -> Unit)? = null,
    onQuickAdd: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    onScanQr: (() -> Unit)? = null,
    canScanQr: Boolean = true,
    onJumpToDate: (() -> Unit)? = null,
    onAvailability: (() -> Unit)? = null,
    onCalendarSets: (() -> Unit)? = null,
    onTimeInsights: (() -> Unit)? = null,
    onDateCalculator: (() -> Unit)? = null,
    onShiftPatterns: (() -> Unit)? = null,
    showProBadges: Boolean = true,
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
                onTitleLongClick = onTitleLongClick,
                onAdd = onAdd,
                onTemplates = onTemplates,
                onQuickAdd = onQuickAdd,
                onSearch = onSearch,
                onScanQr = onScanQr,
                canScanQr = canScanQr,
                onJumpToDate = onJumpToDate,
                onAvailability = onAvailability,
                onCalendarSets = onCalendarSets,
                onTimeInsights = onTimeInsights,
                onDateCalculator = onDateCalculator,
                onShiftPatterns = onShiftPatterns,
                showProBadges = showProBadges,
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
internal fun CalendarActionBar(
    title: String,
    palette: DotCalPalette,
    onTitleClick: () -> Unit,
    onTitleLongClick: () -> Unit,
    onAdd: () -> Unit,
    onTemplates: (() -> Unit)? = null,
    onQuickAdd: (() -> Unit)? = null,
    onSearch: (() -> Unit)? = null,
    onScanQr: (() -> Unit)? = null,
    canScanQr: Boolean = true,
    onJumpToDate: (() -> Unit)? = null,
    onAvailability: (() -> Unit)? = null,
    onCalendarSets: (() -> Unit)? = null,
    onTimeInsights: (() -> Unit)? = null,
    onDateCalculator: (() -> Unit)? = null,
    onShiftPatterns: (() -> Unit)? = null,
    showProBadges: Boolean = true,
) {
    val topIconTint = if (palette.isDark) NWhite else palette.accent
    val haptic = LocalHapticFeedback.current
    var showOverflow by remember { mutableStateOf(false) }
    val hasOverflow = onSearch != null ||
        onJumpToDate != null ||
        onAvailability != null ||
        onQuickAdd != null ||
        onTemplates != null ||
        onCalendarSets != null ||
        onTimeInsights != null ||
        onDateCalculator != null ||
        onShiftPatterns != null
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
            fontFamily = LocalHeadingFont.current,
            fontWeight = FontWeight.Bold,
            fontSize = if (title.length <= 4) 30.sp else 28.sp,
            modifier = Modifier
                .padding(start = 8.dp)
                .pointerInput(title) {
                    detectTapGestures(
                        onTap = { onTitleClick() },
                        onLongPress = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTitleLongClick()
                        },
                    )
                },
            maxLines = 1,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onScanQr != null && canScanQr) {
                IconButton(
                    onClick = onScanQr,
                    modifier = Modifier.size(44.dp),
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan event QR", tint = topIconTint)
                }
            }
            IconButton(
                onClick = onAdd,
                modifier = Modifier.size(44.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add event", tint = topIconTint)
            }
            if (hasOverflow) {
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
                        modifier = Modifier.width(244.dp),
                    ) {
                        if (onSearch != null) {
                            ActionBarMenuItem(
                                label = stringResource(R.string.menu_search),
                                subtitle = stringResource(R.string.menu_search_subtitle),
                                icon = Icons.Default.Search,
                                palette = palette,
                                onClick = {
                                    showOverflow = false
                                    onSearch()
                                },
                            )
                        }
                        ActionBarMenuItem(
                            label = stringResource(R.string.menu_new_event),
                            subtitle = stringResource(R.string.menu_new_event_subtitle),
                            icon = Icons.Default.Add,
                            palette = palette,
                            onClick = {
                                showOverflow = false
                                onAdd()
                            },
                        )
                        if (onJumpToDate != null) {
                            ActionBarMenuItem(
                                label = stringResource(R.string.menu_go_to_date),
                                subtitle = stringResource(R.string.menu_go_to_date_subtitle),
                                icon = Icons.Default.CalendarMonth,
                                palette = palette,
                                onClick = {
                                    showOverflow = false
                                    onJumpToDate()
                                },
                            )
                        }
                        if (onQuickAdd != null) {
                            ActionBarMenuItem(
                                label = stringResource(R.string.menu_quick_add),
                                subtitle = stringResource(R.string.menu_quick_add_subtitle),
                                icon = Icons.Default.AutoAwesome,
                                palette = palette,
                                onClick = {
                                    showOverflow = false
                                    onQuickAdd()
                                },
                            )
                        }
                        if (onAvailability != null) {
                            ActionBarMenuItem(
                                label = stringResource(R.string.menu_share_availability),
                                subtitle = stringResource(R.string.menu_share_availability_subtitle),
                                icon = Icons.Default.Share,
                                isPro = showProBadges,
                                palette = palette,
                                onClick = {
                                    showOverflow = false
                                    onAvailability()
                                },
                            )
                        }
                        if (onTemplates != null) {
                            ActionBarMenuItem(
                                label = stringResource(R.string.menu_templates),
                                subtitle = stringResource(R.string.menu_templates_subtitle),
                                icon = Icons.Default.Description,
                                isPro = showProBadges,
                                palette = palette,
                                onClick = {
                                    showOverflow = false
                                    onTemplates()
                                },
                            )
                        }
                        if (onCalendarSets != null) {
                            ActionBarMenuItem(
                                label = stringResource(R.string.menu_calendar_sets),
                                subtitle = stringResource(R.string.menu_calendar_sets_subtitle),
                                icon = Icons.Default.CalendarMonth,
                                isPro = showProBadges,
                                palette = palette,
                                onClick = {
                                    showOverflow = false
                                    onCalendarSets()
                                },
                            )
                        }
                        if (onShiftPatterns != null) {
                            ActionBarMenuItem(
                                label = stringResource(R.string.menu_shift_patterns),
                                subtitle = stringResource(R.string.menu_shift_patterns_subtitle),
                                icon = Icons.Default.EventRepeat,
                                isPro = showProBadges,
                                palette = palette,
                                onClick = {
                                    showOverflow = false
                                    onShiftPatterns()
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
    isPro: Boolean = false,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        label,
                        color = palette.primaryText,
                        fontFamily = mono,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (isPro) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.badge_pro), color = palette.accent, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, maxLines = 1)
                    }
                }
                Text(subtitle, color = palette.secondaryText, fontFamily = mono, fontSize = 11.sp)
            }
        },
    )
}

@Composable
internal fun DotCalBottomNav(
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
    val calendarLabel = stringResource(R.string.settings_panel_calendar)
    val tasksLabel = stringResource(R.string.tasks_title)
    val settingsLabel = stringResource(R.string.settings_title)
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
        ) {
            BottomNavItem(
                selected = selected == ScreenTab.Calendar,
                label = calendarLabel,
                activeColor = active,
                inactiveColor = inactive,
                icon = { tint -> BottomCalendarIcon(tint) },
                onClick = onCalendar,
                modifier = Modifier.weight(1f),
            )
            BottomNavItem(
                selected = selected == ScreenTab.Tasks,
                label = tasksLabel,
                activeColor = active,
                inactiveColor = inactive,
                icon = { tint -> BottomTaskIcon(tint) },
                onClick = onTasks,
                modifier = Modifier.weight(1f),
            )
            BottomNavItem(
                selected = selected == ScreenTab.Settings,
                label = settingsLabel,
                activeColor = active,
                inactiveColor = inactive,
                icon = { tint -> BottomSettingsIcon(tint) },
                onClick = onSettings,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun BottomNavItem(
    selected: Boolean,
    label: String,
    activeColor: Color,
    inactiveColor: Color,
    icon: @Composable (Color) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val isCurrent = selected
    val selectedState = stringResource(R.string.a11y_selected)
    val notSelectedState = stringResource(R.string.a11y_not_selected)
    val tint by animateColorAsState(
        targetValue = if (isCurrent) activeColor else inactiveColor,
        animationSpec = tween(200),
        label = "navTint",
    )
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                // 48dp is the Android minimum touch target. The icon keeps its own smaller size —
                // only the clickable box grows, inside the 68dp pill.
                .size(48.dp)
                .clip(CircleShape)
                .semantics {
                    contentDescription = label
                    role = Role.Tab
                    stateDescription = if (isCurrent) selectedState else notSelectedState
                    this.selected = isCurrent
                }
                .noRippleClickable {
                    // The pill has no ripple, so this tap is otherwise silent. VIRTUAL_KEY is the
                    // platform constant for a button press — Compose's TextHandleMove is a
                    // text-cursor tick that many devices suppress entirely, so it was not felt.
                    // flags = IGNORE_VIEW_SETTING is deliberately NOT set: the system haptic
                    // preference must still win.
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    onClick()
                },
            contentAlignment = Alignment.Center,
        ) {
            icon(tint)
        }
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

/** Shared by all three bottom-nav icons so they stay optically matched. */
private val NAV_ICON_SIZE = 26.dp
private val NAV_ICON_STROKE = 1.85.dp

@Composable
private fun BottomCalendarIcon(tint: Color) {
    Canvas(modifier = Modifier.size(NAV_ICON_SIZE)) {
        val stroke = Stroke(width = NAV_ICON_STROKE.toPx())
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
    // 26dp canvas with a 17dp glyph box, matching BottomCalendarIcon so the three nav icons read
    // optically even. Was a 28dp canvas with an 18dp glyph.
    Canvas(modifier = Modifier.size(NAV_ICON_SIZE)) {
        val strokePx = NAV_ICON_STROKE.toPx()
        val stroke = Stroke(width = strokePx)
        drawRoundRect(
            color = tint,
            topLeft = Offset(4.5.dp.toPx(), 4.5.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(17.dp.toPx(), 17.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()),
            style = stroke,
        )
        drawLine(tint, Offset(8.5.dp.toPx(), 10.5.dp.toPx()), Offset(10.5.dp.toPx(), 12.5.dp.toPx()), strokeWidth = strokePx)
        drawLine(tint, Offset(10.5.dp.toPx(), 12.5.dp.toPx()), Offset(13.5.dp.toPx(), 8.5.dp.toPx()), strokeWidth = strokePx)
        drawLine(tint, Offset(15.dp.toPx(), 10.5.dp.toPx()), Offset(18.5.dp.toPx(), 10.5.dp.toPx()), strokeWidth = strokePx)
        drawLine(tint, Offset(8.5.dp.toPx(), 16.5.dp.toPx()), Offset(18.5.dp.toPx(), 16.5.dp.toPx()), strokeWidth = strokePx)
    }
}

@Composable
private fun BottomSettingsIcon(tint: Color) {
    // Hand-drawn to match the Calendar and Tasks icons. Was Material Icons.Filled.Settings, whose
    // solid fill read heavier than the 1.8dp strokes either side of it.
    Canvas(modifier = Modifier.size(NAV_ICON_SIZE)) {
        val strokePx = NAV_ICON_STROKE.toPx()
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = tint, radius = 5.2.dp.toPx(), center = center, style = Stroke(width = strokePx))
        drawCircle(color = tint, radius = 2.1.dp.toPx(), center = center, style = Stroke(width = strokePx))
        val toothInner = 5.2.dp.toPx()
        val toothOuter = 7.6.dp.toPx()
        repeat(8) { index ->
            rotate(degrees = index * 45f, pivot = center) {
                drawLine(
                    color = tint,
                    start = Offset(center.x, center.y - toothInner),
                    end = Offset(center.x, center.y - toothOuter),
                    strokeWidth = strokePx,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
internal fun Modifier.noRippleClickable(
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

/**
 * Display text lives as resource ids, not literals: these entries are declared outside any
 * composable, so they cannot call [stringResource] themselves. [label] and [shortLabel] resolve
 * them at the call site. [name] stays the persisted value — never localize it.
 */
internal enum class CalendarTab(
    @StringRes val labelRes: Int,
    @StringRes val shortLabelRes: Int,
) {
    Year(R.string.tab_year_label, R.string.tab_year_short),
    Month(R.string.tab_month_label, R.string.tab_month_short),
    Week(R.string.tab_week_label, R.string.tab_week_short),
    Day(R.string.tab_day_label, R.string.tab_day_short),
    ThreeDay(R.string.tab_three_day_label, R.string.tab_three_day_short),
    Agenda(R.string.tab_agenda_label, R.string.tab_agenda_short);

    val label: String
        @Composable get() = stringResource(labelRes)

    val shortLabel: String
        @Composable get() = stringResource(shortLabelRes)

    companion object {
        val pickerEntries = listOf(Year, Month, Week, Day, Agenda)

        fun fromStorage(value: String?): CalendarTab {
            val stored = entries.firstOrNull { it.name == value } ?: Month
            return if (stored == ThreeDay) Month else stored
        }
    }
}

internal enum class ScreenTab {
    Calendar,
    Tasks,
    Settings,
}
