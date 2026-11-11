package com.dotfield.dotcal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.dotfield.dotcal.data.tasks.RepairMyDayPreview
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
internal fun RepairMyDayScreen(
    candidates: List<CalendarEvent>,
    preview: RepairMyDayPreview?,
    isLoading: Boolean,
    palette: DotCalPalette,
    onBack: () -> Unit,
    onPreview: (Set<String>) -> Unit,
    onClearPreview: () -> Unit,
    onConfirm: (RepairMyDayPreview) -> Unit,
) {
    var selectedIds by remember(candidates) { mutableStateOf(emptySet<String>()) }

    Column(modifier = Modifier.fillMaxSize().background(palette.background)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).background(palette.topBarSurface),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = if (preview == null) onBack else onClearPreview, modifier = Modifier.size(48.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.action_back),
                    tint = palette.primaryText,
                )
            }
            Text(
                stringResource(R.string.repair_my_day_title),
                modifier = Modifier.weight(1f),
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        if (preview == null) {
            RepairSelection(
                candidates = candidates,
                selectedIds = selectedIds,
                isLoading = isLoading,
                palette = palette,
                onToggle = { taskId ->
                    selectedIds = if (taskId in selectedIds) selectedIds - taskId else selectedIds + taskId
                },
                onPreview = { onPreview(selectedIds) },
            )
        } else {
            RepairPreview(
                preview = preview,
                isLoading = isLoading,
                palette = palette,
                onConfirm = { onConfirm(preview) },
                onClearPreview = onClearPreview,
            )
        }
    }
}

@Composable
private fun RepairSelection(
    candidates: List<CalendarEvent>,
    selectedIds: Set<String>,
    isLoading: Boolean,
    palette: DotCalPalette,
    onToggle: (String) -> Unit,
    onPreview: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Text(
            stringResource(R.string.repair_my_day_intro),
            color = palette.secondaryText,
            fontFamily = mono,
            fontSize = 13.sp,
            lineHeight = 19.sp,
        )
        Spacer(modifier = Modifier.height(18.dp))
        if (candidates.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.repair_my_day_empty),
                    color = palette.secondaryText,
                    fontFamily = mono,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(candidates, key = CalendarEvent::id) { task ->
                    val selected = task.id in selectedIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(palette.eventCardSurface)
                            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(28.dp))
                            .clickable { onToggle(task.id) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = selected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(
                                checkedColor = palette.accent,
                                uncheckedColor = palette.secondaryText,
                            ),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                task.title,
                                color = palette.primaryText,
                                fontFamily = LocalHeadingFont.current,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                            )
                            Text(
                                task.taskDueDetailLabel(),
                                color = palette.secondaryText,
                                fontFamily = mono,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
        }
        Button(
            onClick = onPreview,
            enabled = selectedIds.isNotEmpty() && !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = palette.onAccent, strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.repair_my_day_preview_action), fontFamily = mono, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RepairPreview(
    preview: RepairMyDayPreview,
    isLoading: Boolean,
    palette: DotCalPalette,
    onConfirm: () -> Unit,
    onClearPreview: () -> Unit,
) {
    val formatter = remember { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT) }
    val zoneId = remember(preview.zoneId) { ZoneId.of(preview.zoneId) }
    Column(
        modifier = Modifier.fillMaxSize().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 20.dp),
    ) {
        Text(
            stringResource(R.string.repair_my_day_preview_title),
            color = palette.primaryText,
            fontFamily = LocalHeadingFont.current,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
        )
        Spacer(modifier = Modifier.height(16.dp))
        if (preview.isUnchanged) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.repair_my_day_unchanged),
                    color = palette.secondaryText,
                    fontFamily = mono,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(preview.changes, key = { it.taskId }) { change ->
                    val original = Instant.ofEpochMilli(change.originalStartTimeMs).atZone(zoneId).toLocalTime().format(formatter)
                    val proposed = change.proposedStart.toLocalTime().format(formatter)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(28.dp))
                            .background(palette.eventCardSurface)
                            .border(1.dp, palette.eventCardBorder, RoundedCornerShape(28.dp))
                            .padding(18.dp),
                    ) {
                        Text(change.title, color = palette.primaryText, fontFamily = LocalHeadingFont.current, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            stringResource(R.string.repair_my_day_time_change, original, proposed),
                            color = palette.secondaryText,
                            fontFamily = mono,
                            fontSize = 13.sp,
                        )
                    }
                }
                if (preview.unplannedTaskIds.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(R.string.repair_my_day_unplanned),
                            color = palette.accent,
                            fontFamily = mono,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }
        TextButton(onClick = onClearPreview, enabled = !isLoading, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text(stringResource(R.string.repair_my_day_back_selection), color = palette.secondaryText, fontFamily = mono)
        }
        Button(
            onClick = onConfirm,
            enabled = preview.canApply && !isLoading,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = palette.onAccent, strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.repair_my_day_confirm), fontFamily = mono, fontWeight = FontWeight.Bold)
            }
        }
    }
}
