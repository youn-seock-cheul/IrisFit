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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

private val careHourLabels = listOf(R.string.care_hours_default, R.string.care_hours_workday, R.string.care_hours_all)

@OptIn(ExperimentalLayoutApi::class)
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

    val summary = if (enabled) {
        stringResource(R.string.care_minutes, interval) + " · " +
            stringResource(careHourLabels.getOrElse(hours) { careHourLabels.first() })
    } else {
        null
    }
    IrisSettingsRow(
        icon = R.drawable.ic_ms_notifications,
        title = stringResource(R.string.care_settings_title),
        subtitle = summary,
        trailing = {
            Switch(checked = enabled, onCheckedChange = { on ->
                if (!on) configure(false, interval, hours)
                else if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else if (notificationsAvailable) configure(true, interval, hours)
                else Toast.makeText(context, R.string.care_notifications_disabled, Toast.LENGTH_LONG).show()
            })
        }
    )
    if (!notificationsAvailable || enabled) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(start = 70.dp, end = 16.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (!notificationsAvailable) {
                Text(
                    stringResource(R.string.care_notifications_disabled),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = {
                    context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                }) { Text(stringResource(R.string.care_open_notification_settings), fontWeight = FontWeight.SemiBold) }
            }
            if (enabled) {
                Text(
                    stringResource(R.string.care_interval),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(20, 40, 60).forEach { minutes ->
                        FilterChip(selected = interval == minutes, onClick = { configure(enabled, minutes, hours) },
                            label = { Text(stringResource(R.string.care_minutes, minutes)) })
                    }
                }
                Text(
                    stringResource(R.string.care_hours),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    careHourLabels.forEachIndexed { index, label ->
                        FilterChip(selected = hours == index, onClick = { configure(enabled, interval, index) },
                            label = { Text(stringResource(label)) })
                    }
                }
                Text(
                    stringResource(R.string.care_settings_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}