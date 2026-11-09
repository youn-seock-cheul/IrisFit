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
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R

/** One user-started, approximate reminder. No camera, exact alarm or foreground service. */
class BreakReminderScheduler(private val context: Context) {
    private val prefs = context.getSharedPreferences("break_reminder", Context.MODE_PRIVATE)
    private val alarms = context.getSystemService(AlarmManager::class.java)

    private fun bootCount() = Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1)

    fun deadline(): Long {
        if (prefs.getInt("boot", -2) != bootCount()) return 0L
        return prefs.getLong("deadline", 0L)
    }

    /** User reopening the app restores a cancelled OS alarm with the same deadline. */
    fun restorePendingReminder() {
        val savedDeadline = deadline()
        if (savedDeadline > SystemClock.elapsedRealtime()) {
            alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, savedDeadline, pendingIntent())
        }
    }

    fun notificationsEnabled(): Boolean {
        val manager = context.getSystemService(NotificationManager::class.java)
        return NotificationManagerCompat.from(context).areNotificationsEnabled() &&
            manager.getNotificationChannel(CHANNEL)?.importance != NotificationManager.IMPORTANCE_NONE
    }

    fun notificationSettingsIntent(): Intent {
        val appEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        val channelBlocked = context.getSystemService(NotificationManager::class.java)
            .getNotificationChannel(CHANNEL)?.importance == NotificationManager.IMPORTANCE_NONE
        return Intent(if (appEnabled && channelBlocked) Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS
            else Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL)
    }

    fun start() {
        // Make the channel configurable before the first reminder is delivered.
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "화면 휴식 알림", NotificationManager.IMPORTANCE_DEFAULT)
        )
        cancel()
        val deadline = SystemClock.elapsedRealtime() + INTERVAL_MS
        prefs.edit().putLong("deadline", deadline).putInt("boot", bootCount()).apply()
        alarms.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, deadline, pendingIntent())
    }

    fun cancel() {
        alarms.cancel(pendingIntent())
        prefs.edit().clear().apply()
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }

    fun sendBatteryStateChangeNotification(status: com.example.myapplication.session.EyeFatigueStatus) {
        val prefsManager = com.example.myapplication.data.PreferencesManager(context)
        if (!prefsManager.isNotificationsEnabled) return

        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, "화면 휴식 알림", NotificationManager.IMPORTANCE_DEFAULT)
        )
        val openRest = PendingIntent.getActivity(
            context, 20, Intent(context, MainActivity::class.java).putExtra("target_screen", "home"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val selectedLang = com.example.myapplication.data.LanguageManager.getSelectedLanguage(context)
        val localizedTitle = com.example.myapplication.session.AppLocalization.get(selectedLang, "noti_status_title")
        val localizedStatusLabel = com.example.myapplication.session.AppLocalization.get(selectedLang, status.labelKey)
        val localizedDesc = com.example.myapplication.session.AppLocalization.get(selectedLang, status.descKey)
        val localizedBody = "$localizedStatusLabel: $localizedDesc"

        try {
            manager.notify(
                2002,
                NotificationCompat.Builder(context, CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(localizedTitle)
                    .setContentText(localizedBody)
                    .setContentIntent(openRest)
                    .setAutoCancel(true)
                    .build()
            )
        } catch (_: Exception) {}
    }

    fun deliverIfDue() {
        val deadline = deadline()
        if (deadline == 0L || SystemClock.elapsedRealtime() < deadline) return
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED) return
        val notifications = context.getSystemService(NotificationManager::class.java)
        notifications.createNotificationChannel(
            NotificationChannel(CHANNEL, "화면 휴식 알림", NotificationManager.IMPORTANCE_DEFAULT)
        )
        // Switch icon to tired state when reminder is triggered
        try {
            com.example.myapplication.data.DynamicIconManager.switchAppIcon(context, 2)
        } catch (_: Exception) {}

        val openRest = PendingIntent.getActivity(
            context, 10, Intent(context, MainActivity::class.java).putExtra("target_screen", "home"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val openGym = PendingIntent.getActivity(
            context, 11, Intent(context, MainActivity::class.java).putExtra("target_screen", "gym"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getBroadcast(
            context, 12, Intent(context, BreakReminderReceiver::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            notifications.notify(
                NOTIFICATION_ID,
                NotificationCompat.Builder(context, CHANNEL)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle("눈이 패닉 상태예요! 😱 (IrisFit)")
                    .setContentText("오늘 눈 휴식을 너무 안 하셨어요! 20초 눈 운동하고 소중한 시력을 지키세요 👁️✨")
                    .setContentIntent(openRest)
                    .addAction(0, "🟢 30초 눈 휴식", openRest)
                    .addAction(0, "🎮 눈 운동 미니게임", openGym)
                    .addAction(0, "알림 종료", stop)
                    .setAutoCancel(true)
                    .build()
            )
        } catch (_: SecurityException) {
            // Permission can be revoked after the check.
        }
    }

    private fun pendingIntent() = PendingIntent.getBroadcast(
        context, 12, Intent(context, BreakReminderReceiver::class.java).setAction(ACTION_REMIND),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    companion object {
        const val INTERVAL_MS = 20 * 60 * 1000L
        const val ACTION_REMIND = "com.eyeoverlay.app.REMIND"
        const val ACTION_STOP = "com.eyeoverlay.app.STOP_REMINDER"
        private const val CHANNEL = "screen_breaks"
        private const val NOTIFICATION_ID = 2001
    }
}

class BreakReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val scheduler = BreakReminderScheduler(context)
        when (intent.action) {
            BreakReminderScheduler.ACTION_REMIND -> scheduler.deliverIfDue()
            BreakReminderScheduler.ACTION_STOP -> scheduler.cancel()
        }
    }
}
