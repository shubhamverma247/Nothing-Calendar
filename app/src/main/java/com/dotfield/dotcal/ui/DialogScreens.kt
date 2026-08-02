package com.dotfield.dotcal.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.dotfield.dotcal.R

@Composable
internal fun ConfirmDeleteDialog(
    deleteSeries: Boolean,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    ConfirmDeleteDialog(
        title = stringResource(
            if (deleteSeries) R.string.dialog_delete_series_title else R.string.dialog_delete_event_title,
        ),
        confirmLabel = stringResource(
            if (deleteSeries) R.string.dialog_delete_series_confirm else R.string.action_delete,
        ),
        palette = palette,
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
internal fun DragConflictDialog(
    conflictCount: Int,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        titleContentColor = palette.primaryText,
        textContentColor = palette.secondaryText,
        title = { Text(stringResource(R.string.dialog_schedule_conflict)) },
        text = {
            Text(
                pluralStringResource(R.plurals.dialog_conflict_overlaps, conflictCount, conflictCount),
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.dialog_move_anyway), color = palette.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = palette.primaryText)
            }
        },
    )
}

@Composable
internal fun ConfirmDeleteDialog(
    title: String,
    confirmLabel: String,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        titleContentColor = palette.primaryText,
        textContentColor = palette.secondaryText,
        title = { Text(title) },
        text = { Text(stringResource(R.string.dialog_cannot_be_undone)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = palette.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = palette.primaryText)
            }
        },
    )
}

@Composable
internal fun TemplateNameDialog(
    defaultName: String,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    title: String = stringResource(R.string.dialog_save_as_template),
) {
    var name by remember { mutableStateOf(defaultName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        titleContentColor = palette.primaryText,
        textContentColor = palette.secondaryText,
        title = { Text(title, fontFamily = mono) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        stringResource(R.string.dialog_template_name),
                        fontFamily = mono,
                        color = palette.secondaryText,
                    )
                },
                colors = dotCalTextFieldColors(palette),
                textStyle = TextStyle(color = palette.primaryText, fontFamily = mono),
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank(),
            ) {
                Text(
                    stringResource(R.string.action_save),
                    color = if (name.isNotBlank()) palette.accent else palette.disabledText,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel), color = palette.primaryText)
            }
        },
    )
}

@Composable
internal fun UpdateAvailableDialog(
    palette: DotCalPalette,
    onUpdate: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        titleContentColor = palette.primaryText,
        textContentColor = palette.secondaryText,
        title = { Text(stringResource(R.string.dialog_update_available)) },
        text = { Text(stringResource(R.string.dialog_update_available_body)) },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text(stringResource(R.string.action_update), color = palette.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_not_now), color = palette.primaryText)
            }
        },
    )
}

@Composable
internal fun UpdateReadyDialog(
    palette: DotCalPalette,
    onRestart: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = palette.dialogSurface,
        titleContentColor = palette.primaryText,
        textContentColor = palette.secondaryText,
        title = { Text(stringResource(R.string.dialog_update_ready)) },
        text = { Text(stringResource(R.string.dialog_update_ready_body)) },
        confirmButton = {
            TextButton(onClick = onRestart) {
                Text(stringResource(R.string.action_restart), color = palette.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_later), color = palette.primaryText)
            }
        },
    )
}
