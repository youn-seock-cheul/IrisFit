package com.example.myapplication.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.example.myapplication.MainActivity
import com.example.myapplication.R

class BlueLightFilterService : Service() {
    private var overlayView: View? = null
    private val windowManager by lazy { getSystemService(WINDOW_SERVICE) as WindowManager }

    companion object {
        var isRunning = false
        var filterIntensity = 0.4f // 0.1f to 0.8f
        private const val CHANNEL = "bluelight_filter_channel"
        private const val NOTIFICATION_ID = 2002
        private const val ACTION_STOP = "com.eyeoverlay.app.STOP_BLUELIGHT_FILTER"

        fun start(context: Context, intensity: Float = 0.4f) {
            filterIntensity = intensity.coerceIn(0.1f, 0.8f)
            if (!Settings.canDrawOverlays(context)) {
                Toast.makeText(context, "블루라이트 필터를 켜려면 [다른 앱 위에 그리기] 권한이 필요합니다.", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${context.packageName}"))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            }
            val intent = Intent(context, BlueLightFilterService::class.java).putExtra("intensity", filterIntensity)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, BlueLightFilterService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }

    private fun updateIntensity(intensity: Float) {
        filterIntensity = intensity.coerceIn(0.1f, 0.8f)
        overlayView?.let { view ->
            val alpha = (filterIntensity * 180).toInt().coerceIn(30, 200)
            view.setBackgroundColor(Color.argb(alpha, 255, 140, 20))
            try {
                windowManager.updateViewLayout(view, view.layoutParams)
            } catch (_: Exception) {}
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        val notifications = getSystemService(NotificationManager::class.java)
        notifications.createNotificationChannel(
            NotificationChannel(CHANNEL, "블루라이트 차단 필터", NotificationManager.IMPORTANCE_LOW)
        )
        startForeground(NOTIFICATION_ID, notification())

        try {
            val view = View(this).apply {
                val alpha = (filterIntensity * 180).toInt().coerceIn(30, 200)
                setBackgroundColor(Color.argb(alpha, 255, 140, 20))
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }
            windowManager.addView(view, params)
            overlayView = view
        } catch (e: Exception) {
            Toast.makeText(this, "필터를 실행하지 못했습니다: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            stopSelf()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_STOP) {
            stopSelf()
        } else {
            intent?.getFloatExtra("intensity", -1f)?.let {
                if (it > 0f) updateIntensity(it)
            }
        }
        return START_STICKY
    }

    private fun notification(): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, BlueLightFilterService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("IrisFit 블루라이트 필터 작동 중 🛡️")
            .setContentText("눈 보호 필터가 켜져 있습니다. (강도: ${(filterIntensity * 100).toInt()}%)")
            .setContentIntent(open)
            .addAction(0, "필터 끄기", stop)
            .setOngoing(true).build()
    }

    override fun onDestroy() {
        isRunning = false
        overlayView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        overlayView = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }
}
