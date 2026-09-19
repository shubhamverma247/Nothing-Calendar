package com.dotfield.dotcal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dotfield.dotcal.R
import com.dotfield.dotcal.data.readiness.EventReadinessItem
import com.dotfield.dotcal.data.readiness.EventReadinessStore

@Composable
internal fun EventReadinessSummary(
    items: List<EventReadinessItem>,
    attachedFileCount: Int,
    palette: DotCalPalette,
) {
    val completedCount = items.count(EventReadinessItem::isCompleted)
    Text(
        text = if (items.isEmpty()) {
            stringResource(R.string.event_readiness_empty)
        } else {
            stringResource(R.string.event_readiness_progress, completedCount, items.size)
        },
        color = palette.secondaryText,
        fontFamily = mono,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )
    if (attachedFileCount > 0) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.event_readiness_files, attachedFileCount),
            color = palette.secondaryText,
            fontFamily = mono,
            fontSize = 12.sp,
            lineHeight = 17.sp,
        )
    }
}

@Composable
internal fun EventReadinessRow(
    item: EventReadinessItem,
    palette: DotCalPalette,
    canEdit: Boolean,
    showDivider: Boolean,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onRemove: () -> Unit,
) {
    val toggleLabel = stringResource(
        if (item.isCompleted) R.string.event_readiness_mark_not_ready else R.string.event_readiness_mark_ready,
        item.title,
    )
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = item.title,
            color = if (item.isCompleted) palette.secondaryText else palette.primaryText,
            fontFamily = mono,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f),
        )
        IconButton(
            onClick = onToggle,
            enabled = canEdit,
            modifier = Modifier
                .size(44.dp)
                .semantics {
                    contentDescription = toggleLabel
                    role = Role.Checkbox
                },
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (item.isCompleted) palette.accent else Color.Transparent)
                    .border(1.5.dp, if (item.isCompleted) palette.accent else palette.line, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (item.isCompleted) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
        if (canEdit) {
            IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.action_edit),
                    tint = palette.secondaryText,
                    modifier = Modifier.size(17.dp),
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = stringResource(R.string.action_delete),
                    tint = palette.secondaryText,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
    if (showDivider) {
        HorizontalDivider(color = palette.line.copy(alpha = 0.35f), thickness = 1.dp)
    }
}

@Composable
internal fun EventReadinessAddAction(palette: DotCalPalette, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Add, contentDescription = null, tint = palette.accent, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.event_readiness_add),
            color = palette.accent,
            fontFamily = mono,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadinessItemEditorSheet(
    item: EventReadinessItem?,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var title by remember(item?.id) { mutableStateOf(item?.title.orEmpty()) }
    val save = {
        title.trim().takeIf { it.isNotEmpty() }?.let(onSave)
        Unit
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = palette.dialogSurface,
        dragHandle = { BottomSheetDragHandle(palette) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
        ) {
            Text(
                text = stringResource(
                    if (item == null) R.string.event_readiness_add_title else R.string.event_readiness_edit_title,
                ),
                color = palette.primaryText,
                fontFamily = LocalHeadingFont.current,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { value ->
                    if (value.length <= EventReadinessStore.MaxTitleLength) title = value
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.event_readiness_item_hint)) },
                singleLine = true,
                colors = dotCalTextFieldColors(palette),
                textStyle = TextStyle(color = palette.primaryText, fontFamily = mono),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save() }),
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = secondaryActionBorder(palette),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = secondaryActionContainer(palette),
                        contentColor = secondaryActionContent(palette),
                    ),
                ) {
                    Text(stringResource(R.string.action_cancel), fontFamily = mono, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = save,
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.accent,
                        contentColor = palette.onAccent,
                        disabledContainerColor = palette.line,
                        disabledContentColor = palette.secondaryText,
                    ),
                ) {
                    Text(stringResource(R.string.action_save), fontFamily = mono, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
