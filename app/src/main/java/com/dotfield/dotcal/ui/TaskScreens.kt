package com.dotfield.dotcal.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.EventReminder
import com.dotfield.dotcal.data.TaskEditorData
import com.dotfield.dotcal.data.baseEventId
import com.dotfield.dotcal.data.templates.EventTemplate
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TaskDetailScreen(
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
    var showActions by remember { mutableStateOf(false) }
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
            modifier = Modifier.fillMaxSize().background(palette.background),
            contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 26.dp, bottom = 28.dp),
        ) {
            item {
                Text(
                    task.title,
                    color = palette.primaryText,
                    fontFamily = LocalHeadingFont.current,
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
                    Text(
                        if (task.isCompleted == 1) "Reopen Task" else "Mark Complete",
                        color = palette.accent,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable(onClick = onComplete)
                            .padding(vertical = 12.dp),
                    )
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
        if (showActions) {
            val actions = buildList {
                add(CompactActionItem("Edit") {
                    showActions = false
                    onEdit()
                })
                if (task.isCompleted != 1) {
                    add(CompactActionItem("Add to Calendar") {
                        showActions = false
                        onTimeBlock()
                    })
                }
                add(CompactActionItem(if (isPrivate) "Restore From Private Vault" else "Move to Private Vault") {
                    showActions = false
                    if (isPrivate) onRestoreFromPrivate() else onMoveToPrivate()
                })
            }
            ModalBottomSheet(
                onDismissRequest = { showActions = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = palette.dialogSurface,
                dragHandle = { BottomSheetDragHandle(palette) },
            ) {
                CompactActionSheetContent(
                    title = "Task Options",
                    actions = actions,
                    palette = palette,
                )
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
internal fun TasksScreen(
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
    val remindersByEventId = remember(reminders) {
        buildMap {
            reminders.forEach { reminder -> putIfAbsent(reminder.eventId, reminder) }
        }
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
                                    reminder = remindersByEventId[task.baseEventId()],
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
            onTitleLongClick = {},
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
    val filters = TaskFilter.entries
    Layout(
        content = {
            filters.forEach { option ->
                val isSelected = selected == option
                val segBg by animateColorAsState(
                    targetValue = if (isSelected) segmentSelected else segmentSurface,
                    animationSpec = tween(180),
                    label = "segBg",
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(segBg)
                        .noRippleClickable { onSelected(option) }
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        option.label,
                        fontFamily = mono,
                        color = if (isSelected) palette.primaryText else inactiveText,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        },
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
    ) { measurables, constraints ->
        val verticalInsetPx = 4.dp.roundToPx()
        val count = measurables.size.coerceAtLeast(1)
        val segmentHeight = (constraints.maxHeight - verticalInsetPx * 2).coerceAtLeast(0)
        val minGapPx = 4.dp.roundToPx()
        val placeables = measurables.map { measurable ->
            measurable.measure(
                androidx.compose.ui.unit.Constraints(
                    minWidth = 0,
                    maxWidth = constraints.maxWidth,
                    minHeight = segmentHeight,
                    maxHeight = segmentHeight,
                )
            )
        }
        val totalSegmentWidth = placeables.sumOf { it.width }
        val evenGapPx = ((constraints.maxWidth - totalSegmentWidth) / (count + 1)).coerceAtLeast(minGapPx)
        layout(constraints.maxWidth, constraints.maxHeight) {
            var x = evenGapPx
            placeables.forEach { placeable ->
                placeable.placeRelative(x, verticalInsetPx)
                x += placeable.width + evenGapPx
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
internal fun TaskEditorSheet(
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
            if (!granted) showDotCalToast(context, palette, "Task saved without reminder")
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
                    fontFamily = LocalHeadingFont.current,
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
                showDotCalToast(context, palette, "Template saved")
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
            Text(title, color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontSize = 20.sp)
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
