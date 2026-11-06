package com.dotfield.dotcal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotfield.dotcal.R
import com.dotfield.dotcal.data.CalendarEvent
import com.dotfield.dotcal.data.tasks.EveningTaskReviewAction
import com.dotfield.dotcal.data.tasks.eveningTaskReviewCandidates
import com.dotfield.dotcal.data.tasks.eveningTaskReviewProgress
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EveningTaskReviewScreen(
    tasks: List<CalendarEvent>,
    palette: DotCalPalette,
    reopenTaskId: String?,
    onBack: () -> Unit,
    onReopenHandled: () -> Unit,
    onAction: (CalendarEvent, EveningTaskReviewAction, (Boolean) -> Unit) -> Unit,
) {
    var processedIds by remember { mutableStateOf(emptySet<String>()) }
    var pendingDelete by remember { mutableStateOf<CalendarEvent?>(null) }
    var choosingDateFor by remember { mutableStateOf<CalendarEvent?>(null) }
    var actionInProgress by remember { mutableStateOf(false) }
    val candidates = remember(tasks) { eveningTaskReviewCandidates(tasks) }
    val current = candidates.firstOrNull { it.id !in processedIds }
    val progress = eveningTaskReviewProgress(tasks, processedIds)

    LaunchedEffect(reopenTaskId) {
        reopenTaskId?.let { taskId ->
            processedIds = processedIds - taskId
            onReopenHandled()
        }
    }

    fun apply(task: CalendarEvent, action: EveningTaskReviewAction) {
        if (actionInProgress) return
        if (action == EveningTaskReviewAction.Skip) {
            processedIds = processedIds + task.id
        } else {
            actionInProgress = true
            onAction(task, action) { success ->
                actionInProgress = false
                if (success) processedIds = processedIds + task.id
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).background(palette.topBarSurface),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back), tint = palette.primaryText)
            }
            Text(
                stringResource(R.string.evening_review_title),
                modifier = Modifier.weight(1f),
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        if (current == null) {
            EveningReviewComplete(palette = palette, onDone = onBack)
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 24.dp)) {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Text(
                        stringResource(R.string.evening_review_progress, progress.completed + 1, progress.total),
                        color = palette.secondaryText,
                        fontFamily = mono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(30.dp))
                            .background(palette.eventCardSurface)
                            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(30.dp))
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                    ) {
                        Text(
                            current.title,
                            color = palette.primaryText,
                            fontFamily = LocalHeadingFont.current,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 30.sp,
                            lineHeight = 36.sp,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(current.taskDueDetailLabel(), color = palette.secondaryText, fontFamily = mono, fontSize = 13.sp)
                    }
                }

                Column(
                    modifier = Modifier.navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Button(
                        onClick = { apply(current, EveningTaskReviewAction.Complete) },
                        enabled = !actionInProgress,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                    ) {
                        Text(stringResource(R.string.task_mark_complete), fontFamily = mono, fontWeight = FontWeight.Bold)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { apply(current, EveningTaskReviewAction.Tomorrow) },
                            enabled = !actionInProgress,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Text(stringResource(R.string.evening_review_tomorrow), color = palette.primaryText, fontFamily = mono)
                        }
                        OutlinedButton(
                            onClick = { choosingDateFor = current },
                            enabled = !actionInProgress,
                            modifier = Modifier.weight(1f).height(56.dp),
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Text(stringResource(R.string.snooze_picker_choose_date), color = palette.primaryText, fontFamily = mono)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { apply(current, EveningTaskReviewAction.Unscheduled) },
                            enabled = !actionInProgress,
                            modifier = Modifier.weight(1f).height(56.dp),
                        ) {
                            Text(stringResource(R.string.evening_review_unscheduled), color = palette.primaryText, fontFamily = mono)
                        }
                        TextButton(
                            onClick = { pendingDelete = current },
                            enabled = !actionInProgress,
                            modifier = Modifier.weight(1f).height(56.dp),
                        ) {
                            Text(stringResource(R.string.task_delete), color = palette.accent, fontFamily = mono)
                        }
                        TextButton(
                            onClick = { apply(current, EveningTaskReviewAction.Skip) },
                            enabled = !actionInProgress,
                            modifier = Modifier.weight(1f).height(56.dp),
                        ) {
                            Text(stringResource(R.string.shift_preview_skipped), color = palette.secondaryText, fontFamily = mono)
                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { task ->
        ConfirmDeleteDialog(
            title = stringResource(R.string.evening_review_delete_title),
            confirmLabel = stringResource(R.string.action_delete),
            palette = palette,
            onDismiss = { pendingDelete = null },
            onConfirm = {
                pendingDelete = null
                apply(task, EveningTaskReviewAction.Delete)
            },
        )
    }
    choosingDateFor?.let { task ->
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = LocalDate.now().plusDays(1).toDatePickerUtcMillis())
        DatePickerDialog(
            onDismissRequest = { choosingDateFor = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { selected ->
                            apply(task, EveningTaskReviewAction.ChooseDate(datePickerDateFromMillis(selected)))
                        }
                        choosingDateFor = null
                    },
                ) { Text(stringResource(R.string.action_done), color = palette.accent) }
            },
            dismissButton = {
                TextButton(onClick = { choosingDateFor = null }) {
                    Text(stringResource(R.string.action_cancel), color = palette.secondaryText)
                }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun EveningReviewComplete(palette: DotCalPalette, onDone: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.evening_review_complete_title),
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontWeight = FontWeight.SemiBold,
                fontSize = 30.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                stringResource(R.string.evening_review_complete_subtitle),
                color = palette.secondaryText,
                fontFamily = mono,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
            ) {
                Text(stringResource(R.string.action_done), fontFamily = mono, fontWeight = FontWeight.Bold)
            }
        }
    }
}
