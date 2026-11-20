package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.myapplication.session.GuidedSessionViewModel
import com.example.myapplication.ui.theme.AppColorTone
import com.example.myapplication.ui.theme.irisColorScheme

@Composable
fun ColorToneDialog(
    model: GuidedSessionViewModel,
    onDismiss: () -> Unit,
    onOpenPaywall: () -> Unit = {}
) {
    val dark = isSystemInDarkTheme()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(model.tr("settings_color_tone")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()).selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(model.tr("tone_description"), style = MaterialTheme.typography.bodyMedium)
                AppColorTone.entries.forEachIndexed { index, tone ->
                    val isUnlocked = model.entitlementManager.isColorToneUnlocked(index)
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .selectable(
                                selected = tone == model.colorTone,
                                enabled = !model.colorToneBusy,
                                role = Role.RadioButton,
                                onClick = {
                                    if (isUnlocked) {
                                        model.selectColorTone(tone)
                                    } else {
                                        onOpenPaywall()
                                    }
                                }
                            )
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = tone == model.colorTone, onClick = null, enabled = !model.colorToneBusy)
                        Box(Modifier.size(24.dp).background(irisColorScheme(tone, dark).primary, CircleShape))
                        Text(
                            text = if (isUnlocked) model.tr(tone.labelKey) else "${model.tr(tone.labelKey)} 🔒",
                            modifier = Modifier.weight(1f)
                        )
                        if (!isUnlocked) {
                            Icon(Icons.Default.Lock, contentDescription = "유료 잠금", tint = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
                model.colorToneError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(model.tr("btn_ok")) } }
    )
}
