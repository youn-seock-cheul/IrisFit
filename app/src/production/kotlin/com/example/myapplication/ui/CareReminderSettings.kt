package com.example.myapplication.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.myapplication.R
import com.example.myapplication.reminder.CareReminderScheduler

@Composable
fun CareReminderSettings() {
    val context = LocalContext.current
    val scheduler = remember { CareReminderScheduler(context.applicationContext) }
    val owner = LocalLifecycleOwner.current
    var enabled by remember { mutableStateOf(scheduler.enabled) }
    var interval by remember { mutableIntStateOf(scheduler.intervalMinutes) }
    var hours by remember { mutableIntStateOf(scheduler.hours) }
    fun notificationsAllowed(): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            context.getSystemService(NotificationManager::class.java)
                .getNotificationChannel(CareReminderScheduler.CHANNEL)?.importance != NotificationManager.IMPORTANCE_NONE
    var notificationsAvailable by remember { mutableStateOf(notificationsAllowed()) }

    fun configure(on: Boolean, minutes: Int, window: Int) {
        try {
            scheduler.configure(on, minutes, window)
            enabled = scheduler.enabled
            interval = scheduler.intervalMinutes
            hours = scheduler.hours
        } catch (error: RuntimeException) {
            Toast.makeText(context, R.string.care_schedule_error, Toast.LENGTH_LONG).show()
            android.util.Log.e("CareReminder", "Unable to update schedule", error)
            enabled = scheduler.enabled
        }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notificationsAvailable = notificationsAllowed()
        if (granted && notificationsAvailable) configure(true, interval, hours)
    }
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = scheduler.enabled
                notificationsAvailable = notificationsAllowed()
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.care_settings_title), modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Switch(checked = enabled, onCheckedChange = { on ->
                    if (!on) configure(false, interval, hours)
                    else if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else if (notificationsAvailable) configure(true, interval, hours)
                    else Toast.makeText(context, R.string.care_notifications_disabled, Toast.LENGTH_LONG).show()
                })
            }
            Text(stringResource(R.string.care_settings_description), style = MaterialTheme.typography.bodySmall)
            if (!notificationsAvailable) {
                Text(stringResource(R.string.care_notifications_disabled), color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                }) { Text(stringResource(R.string.care_open_notification_settings)) }
            }
            Text(stringResource(R.string.care_interval))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(20, 40, 60).forEach { minutes ->
                    FilterChip(selected = interval == minutes, onClick = { configure(enabled, minutes, hours) },
                        label = { Text(stringResource(R.string.care_minutes, minutes)) })
                }
            }
            Text(stringResource(R.string.care_hours))
            listOf(R.string.care_hours_default, R.string.care_hours_workday, R.string.care_hours_all).forEachIndexed { index, label ->
                FilterChip(selected = hours == index, onClick = { configure(enabled, interval, index) },
                    label = { Text(stringResource(label)) })
            }
        }
    }
}
