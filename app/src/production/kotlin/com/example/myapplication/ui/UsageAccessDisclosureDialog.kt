package com.example.myapplication.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.myapplication.session.GuidedSessionViewModel

@Composable
internal fun UsageAccessDisclosureDialog(
    sessionModel: GuidedSessionViewModel,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(sessionModel.tr("usage_access_title")) },
        text = { Text(sessionModel.tr("usage_access_body")) },
        confirmButton = {
            Button(onClick = onOpenSettings) {
                Text(sessionModel.tr("usage_access_open_settings"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(sessionModel.tr("btn_cancel"))
            }
        }
    )
}
