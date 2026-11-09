package com.example.myapplication.session

import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Foreground-only, short notification-style cue. Never changes system settings. */
class EndSignalPlayer(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    fun play(mode: EndSignalMode): Boolean {
        stop()
        if (mode == EndSignalMode.SILENT) return true
        val notifications = context.getSystemService(NotificationManager::class.java)
        val audio = context.getSystemService(AudioManager::class.java)
        if (notifications.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL ||
            audio.ringerMode == AudioManager.RINGER_MODE_SILENT) return false
        var available = true
        try {
            if (mode.sound) {
                if (audio.ringerMode == AudioManager.RINGER_MODE_NORMAL &&
                    audio.getStreamVolume(AudioManager.STREAM_NOTIFICATION) > 0) {
                    ringtone = RingtoneManager.getRingtone(context,
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    ringtone?.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                    ringtone?.isLooping = false
                    ringtone?.play()
                    if (ringtone == null) available = false
                } else available = false
            }
            if (mode.vibration) {
                vibrator = context.getSystemService(VibratorManager::class.java).defaultVibrator
                if (vibrator?.hasVibrator() == true) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE),
                        VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION))
                } else available = false
            }
            handler.postDelayed({ stop() }, 600L)
        } catch (_: RuntimeException) {
            stop()
            return false
        }
        return available
    }

    fun stop() {
        handler.removeCallbacksAndMessages(null)
        ringtone?.stop()
        ringtone = null
        vibrator?.cancel()
        vibrator = null
    }
}
