package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.example.myapplication.data.LanguageManager
import com.example.myapplication.session.AppLocalization

class HealthConnectRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val language = AppLocalization.resolveEffectiveLang(LanguageManager.effectiveLanguage(this))
        setContent {
            MaterialTheme {
                HealthConnectRationale(
                    title = AppLocalization.get(language, "health_connect_disclosure_title"),
                    body = AppLocalization.get(language, "health_connect_disclosure_body"),
                    dismissLabel = AppLocalization.get(language, "btn_close"),
                    onDismiss = ::finish
                )
            }
        }
    }
}

@Composable
private fun HealthConnectRationale(
    title: String,
    body: String,
    dismissLabel: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel) }
        }
    )
}
