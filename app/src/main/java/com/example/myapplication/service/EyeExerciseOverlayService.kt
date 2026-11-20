package com.example.myapplication.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.example.myapplication.BuildConfig
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.PreferencesManager
import com.example.myapplication.vision.GazeTracker
import java.util.concurrent.Executors

/** Debug-only observation session. No fatigue, gaze accuracy, DMS or medical inference. */
class EyeExerciseOverlayService : LifecycleService() {
    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var analysis: ImageAnalysis? = null
    private var tracker: GazeTracker? = null
    private var banner: TextView? = null
    private var receiverRegistered = false
    @Volatile private var closing = false
    @Volatile private var lastSignalAt = 0L

    private val screenOff = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF) stopSelf()
        }
    }
    private val updateStatus = object : Runnable {
        override fun run() {
            if (closing) return
            if (ContextCompat.checkSelfPermission(this@EyeExerciseOverlayService, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED || !Settings.canDrawOverlays(this@EyeExerciseOverlayService)) {
                stopSelf()
                return
            }
            val status = if (lastSignalAt != 0L && SystemClock.elapsedRealtime() - lastSignalAt < 3000L)
                "얼굴/눈 신호 확인 · 의료 측정 아님"
            else "얼굴/눈 신호 미확인 · 감지 불가"
            banner?.text = "개발 실험 · 운전 중 사용 금지\n$status"
            getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification(status))
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (!BuildConfig.DEBUG || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=
            PackageManager.PERMISSION_GRANTED || !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        val notifications = getSystemService(NotificationManager::class.java)
        notifications.createNotificationChannel(
            NotificationChannel(CHANNEL, "개발용 카메라 실험", NotificationManager.IMPORTANCE_LOW)
        )
        try {
            startForeground(NOTIFICATION_ID, notification("카메라 준비 중"))
            val view = TextView(this).apply {
                text = "개발용 카메라 실험 · 운전 중 사용 금지"
                setTextColor(Color.WHITE)
                setBackgroundColor(Color.rgb(32, 32, 40))
                setPadding(16, 8, 16, 8)
                textSize = 12f
            }
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                y = (48 * resources.displayMetrics.density).toInt()
                alpha = 0.7f
            }
            getSystemService(WindowManager::class.java).addView(view, params)
            banner = view
            ContextCompat.registerReceiver(this, screenOff, IntentFilter(Intent.ACTION_SCREEN_OFF),
                ContextCompat.RECEIVER_NOT_EXPORTED)
            receiverRegistered = true
            tracker = GazeTracker(this, PreferencesManager(this)) { _, _, signal, _ ->
                if (!closing) lastSignalAt = if (signal) SystemClock.elapsedRealtime() else 0L
            }
            setupCamera()
            handler.post(updateStatus)
            handler.postDelayed({ stopSelf() }, 120_000L)
        } catch (_: RuntimeException) {
            failAndStop()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        if (intent?.action == ACTION_STOP || intent == null) stopSelf()
        return START_NOT_STICKY
    }

    private fun setupCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            if (closing) return@addListener
            try {
                val provider = future.get()
                cameraProvider = provider
                if (!provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                    failAndStop()
                    return@addListener
                }
                val useCase = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                analysis = useCase
                useCase.setAnalyzer(executor) { frame ->
                    val currentTracker = tracker
                    if (closing || currentTracker == null) frame.close()
                    else currentTracker.processImageProxy(frame, resources.displayMetrics.widthPixels,
                        resources.displayMetrics.heightPixels)
                }
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_FRONT_CAMERA, useCase)
            } catch (_: Exception) {
                failAndStop()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun notification(status: String): Notification {
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val stop = PendingIntent.getService(this, 1,
            Intent(this, EyeExerciseOverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_irisfit)
            .setContentTitle("카메라 개발 실험 · 최대 2분")
            .setContentText(status)
            .setContentIntent(open)
            .addAction(0, "즉시 종료", stop)
            .setOngoing(true).build()
    }

    private fun failAndStop() {
        Toast.makeText(this, "카메라 실험을 계속할 수 없어 종료합니다.", Toast.LENGTH_LONG).show()
        stopSelf()
    }

    override fun onDestroy() {
        closing = true
        handler.removeCallbacksAndMessages(null)
        analysis?.let {
            it.clearAnalyzer()
            cameraProvider?.unbind(it)
        }
        tracker?.close()
        tracker = null
        executor.shutdown()
        if (receiverRegistered) unregisterReceiver(screenOff)
        banner?.let { getSystemService(WindowManager::class.java).removeView(it) }
        banner = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL = "camera_research"
        private const val NOTIFICATION_ID = 1001
        private const val ACTION_STOP = "com.eyeoverlay.app.STOP_CAMERA_RESEARCH"
    }
}
