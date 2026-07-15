package com.dotfield.dotcal.ui

import android.app.Activity
import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings as SettingsGearIcon
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
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
    onJumpToDate: (() -> Unit)? = null,
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
                onJumpToDate = onJumpToDate,
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
    onJumpToDate: (() -> Unit)? = null,
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
                                label = "Search",
                                subtitle = "Find events and tasks",
                                icon = Icons.Default.Search,
                                palette = palette,
                                onClick = {
                                    showOverflow = false
                                    onSearch()
                                },
                            )
                        }
                        ActionBarMenuItem(
                            label = "New Event",
                            subtitle = "Create an event quickly",
                            icon = Icons.Default.Add,
                            palette = palette,
                            onClick = {
                                showOverflow = false
                                onAdd()
                            },
                        )
                        if (onJumpToDate != null) {
                            ActionBarMenuItem(
                                label = "Go to date",
                                subtitle = "Jump without changing views",
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
                                label = "Calendar Sets",
                                subtitle = "Switch saved visibility sets",
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
                                label = "Shift Patterns",
                                subtitle = "Build rotating schedules",
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
                        Text("Pro", color = palette.accent, fontFamily = mono, fontWeight = FontWeight.SemiBold, fontSize = 10.sp, maxLines = 1)
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

internal enum class CalendarTab(val label: String, val shortLabel: String) {
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

internal enum class ScreenTab {
    Calendar,
    Tasks,
    Settings,
}
