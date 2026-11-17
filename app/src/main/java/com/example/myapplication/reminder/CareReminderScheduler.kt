package com.example.myapplication.reminder

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/** Opt-in, inexact reminders. This is a clock-based schedule, not usage or fatigue detection. */
class CareReminderScheduler(private val context: Context) {
    private val prefs = context.getSharedPreferences("care_reminder", Context.MODE_PRIVATE)
    private val alarms = context.getSystemService(AlarmManager::class.java)
    val enabled: Boolean get() = prefs.getBoolean("enabled", false)
    val intervalMinutes: Int get() = prefs.getInt("interval", 60)
    val hours: Int get() = prefs.getInt("hours", 0)

    fun configure(enabled: Boolean, interval: Int, hours: Int) {
        require(interval in setOf(20, 40, 60))
        require(hours in 0..2)
        check(prefs.edit().putBoolean("enabled", enabled).putInt("interval", interval).putInt("hours", hours).commit()) {
            "Unable to save reminder settings"
        }
        alarms.cancel(alarmIntent())
        if (enabled) {
            try {
                scheduleNext()
            } catch (error: RuntimeException) {
                prefs.edit().putBoolean("enabled", false).remove("deadline").commit()
                throw error
            }
        } else {
            prefs.edit().remove("deadline").apply()
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }
    }

    fun restore() {
        if (enabled) scheduleNext()
    }

    private fun scheduleNext(delayMinutes: Int = intervalMinutes) {
        val now = ZonedDateTime.now()
        val target = nextAllowedTime(now.plusMinutes(delayMinutes.toLong()), hours)
        val delay = ChronoUnit.MILLIS.between(now.toInstant(), target.toInstant()).coerceAtLeast(0)
        val deadline = SystemClock.elapsedRealtime() + delay
        check(prefs.edit().putLong("deadline", deadline)
            .putInt("boot", Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)).commit()) {
            "Unable to save reminder deadline"
        }
        alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, deadline, alarmIntent())
    }

    fun deliver() {
        if (!enabled || prefs.getInt("boot", -2) != Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1) ||
            prefs.getLong("deadline", 0L) == 0L || SystemClock.elapsedRealtime() < prefs.getLong("deadline", 0L)) return
        val now = ZonedDateTime.now()
        if (nextAllowedTime(now, hours).toInstant() <= now.toInstant() &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED &&
            NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(NotificationChannel(
                CHANNEL, context.getString(R.string.care_channel), NotificationManager.IMPORTANCE_LOW))
            val home = PendingIntent.getActivity(context, 31,
                Intent(context, MainActivity::class.java).putExtra("target_screen", "home"),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val gym = PendingIntent.getActivity(context, 32,
                Intent(context, MainActivity::class.java).putExtra("target_screen", "gym"),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val stop = PendingIntent.getBroadcast(context, 33,
                Intent(context, CareReminderReceiver::class.java).setAction(ACTION_STOP),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val later = PendingIntent.getBroadcast(context, 34,
                Intent(context, CareReminderReceiver::class.java).setAction(ACTION_LATER),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            try {
                manager.notify(NOTIFICATION_ID, NotificationCompat.Builder(context, CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(context.getString(R.string.care_notification_title))
                    .setContentText(context.getString(R.string.care_notification_body))
                    .setContentIntent(home)
                    .addAction(0, context.getString(R.string.care_open_rest), home)
                    .addAction(0, context.getString(R.string.care_open_exercise), gym)
                    .addAction(0, context.getString(R.string.care_later), later)
                    .addAction(0, context.getString(R.string.care_stop), stop)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .build())
            } catch (error: SecurityException) {
                Log.w("CareReminder", "Notification permission was revoked", error)
            }
        }
        scheduleNext()
    }

    fun later() {
        if (!enabled) return
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        scheduleNext(20)
    }

    private fun alarmIntent() = PendingIntent.getBroadcast(context, 30,
        Intent(context, CareReminderReceiver::class.java).setAction(ACTION_REMIND),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    companion object {
        const val ACTION_REMIND = "com.eyeoverlay.app.CARE_REMIND"
        const val ACTION_STOP = "com.eyeoverlay.app.CARE_STOP"
        const val ACTION_LATER = "com.eyeoverlay.app.CARE_LATER"
        const val CHANNEL = "care_reminders"
        private const val NOTIFICATION_ID = 2010

        fun nextAllowedTime(candidate: ZonedDateTime, hours: Int): ZonedDateTime {
            require(hours in 0..2)
            if (hours == 2) return candidate
            val start = if (hours == 0) 8 else 9
            val end = if (hours == 0) 22 else 18
            return when {
                candidate.hour < start -> candidate.toLocalDate().atTime(start, 0).atZone(candidate.zone)
                candidate.hour >= end -> candidate.toLocalDate().plusDays(1).atTime(start, 0).atZone(candidate.zone)
                else -> candidate
            }
        }
    }
}

class CareReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduler = CareReminderScheduler(context)
        when (intent.action) {
            CareReminderScheduler.ACTION_REMIND -> scheduler.deliver()
            CareReminderScheduler.ACTION_STOP -> scheduler.configure(false, scheduler.intervalMinutes, scheduler.hours)
            CareReminderScheduler.ACTION_LATER -> scheduler.later()
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED -> scheduler.restore()
        }
    }
}
