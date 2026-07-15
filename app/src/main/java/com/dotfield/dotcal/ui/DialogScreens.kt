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
import androidx.compose.ui.text.TextStyle

@Composable
internal fun ConfirmDeleteDialog(
    deleteSeries: Boolean,
    palette: DotCalPalette,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    ConfirmDeleteDialog(
        title = if (deleteSeries) "Delete series?" else "Delete event?",
        confirmLabel = if (deleteSeries) "Delete series" else "Delete",
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
        title = { Text("Schedule conflict") },
        text = {
            Text(
                if (conflictCount == 1) {
                    "This time overlaps another event."
                } else {
                    "This time overlaps $conflictCount other events."
                },
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Move anyway", color = palette.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.primaryText)
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
        text = { Text("This cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = palette.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.primaryText)
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
    title: String = "Save as template",
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
                placeholder = { Text("Template name", fontFamily = mono, color = palette.secondaryText) },
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
                Text("Save", color = if (name.isNotBlank()) palette.accent else palette.disabledText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = palette.primaryText)
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
        title = { Text("Update available") },
        text = { Text("A new version of DotCal is available. Update to get the latest improvements.") },
        confirmButton = {
            TextButton(onClick = onUpdate) {
                Text("Update", color = palette.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not now", color = palette.primaryText)
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
        title = { Text("Update ready") },
        text = { Text("The update has been downloaded. Restart DotCal to apply it.") },
        confirmButton = {
            TextButton(onClick = onRestart) {
                Text("Restart", color = palette.accent)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Later", color = palette.primaryText)
            }
        },
    )
}
