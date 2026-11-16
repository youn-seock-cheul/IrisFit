package com.example.myapplication.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myapplication.R

enum class DevicePose(val labelKey: String, val iconRes: Int) {
    HAND_HELD_ACTIVE("pose_handheld", R.drawable.ic_pose_handheld),
    DEVICE_TILTED("pose_tilted", R.drawable.ic_pose_handheld),
    DEVICE_FLAT("pose_flat", R.drawable.ic_pose_flat),
    DEVICE_FACE_DOWN("pose_facedown", R.drawable.ic_pose_facedown);
    val label: String get() = labelKey
}

class SmartDevicePoseDetector(context: Context) : SensorEventListener {
    private fun safeLogE(tag: String, msg: String, e: Throwable? = null) {
        try {
            if (e != null) Log.e(tag, msg, e) else Log.e(tag, msg)
        } catch (_: Throwable) {}
    }

    private fun safeLogW(tag: String, msg: String) {
        try {
            Log.w(tag, msg)
        } catch (_: Throwable) {}
    }

    private val sensorManager = try {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    } catch (e: Exception) {
        safeLogE("SmartDevicePoseDetector", "Failed to access SENSOR_SERVICE: ${e.message}", e)
        null
    }
    private val classifier = PoseClassifier()
    private val handler by lazy {
        try {
            Handler(Looper.getMainLooper())
        } catch (e: Exception) {
            safeLogE("SmartDevicePoseDetector", "Failed to access MainLooper Handler: ${e.message}", e)
            null
        }
    }
    private var listening = false
    private var registeredSensorsCount = 0
    private var lastEventTimestampMs = 0L
    private var _observedPose by mutableStateOf(DevicePose.HAND_HELD_ACTIVE)

    val isListening: Boolean get() = listening
    val hasRegisteredSensors: Boolean get() = registeredSensorsCount > 0
    val isFresh: Boolean get() = listening && registeredSensorsCount > 0 && (lastEventTimestampMs > 0L && ((try { SystemClock.elapsedRealtime() } catch (_: Throwable) { System.currentTimeMillis() }) - lastEventTimestampMs) < 5000L)

    val currentPose: DevicePose
        get() {
            if (!listening) return DevicePose.HAND_HELD_ACTIVE
            val evaluated = classifier.pose(SystemClock.elapsedRealtime())
            if (_observedPose != evaluated) {
                _observedPose = evaluated
            }
            return evaluated
        }

    private val freshnessCheck = object : Runnable {
        override fun run() {
            if (!listening) return
            updatePose()
            handler?.postDelayed(this, 500L)
        }
    }

    fun startListening() {
        if (listening) return
        classifier.reset()
        _observedPose = DevicePose.HAND_HELD_ACTIVE
        listening = true
        registeredSensorsCount = 0
        lastEventTimestampMs = 0L // Must receive at least 1 validated real sensor event!

        for (type in listOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_PROXIMITY, Sensor.TYPE_LIGHT)) {
            val sensor = sensorManager?.getDefaultSensor(type) ?: continue
            if (sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI) == true) {
                registeredSensorsCount++
            } else {
                safeLogW("SmartDevicePoseDetector", "Sensor registration failed for type: $type")
            }
        }
        handler?.post(freshnessCheck)
    }

    fun stopListening() {
        listening = false
        registeredSensorsCount = 0
        lastEventTimestampMs = 0L
        handler?.removeCallbacks(freshnessCheck)
        sensorManager?.unregisterListener(this)
        classifier.reset()
        _observedPose = DevicePose.HAND_HELD_ACTIVE
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!listening || event == null) return
        val values = event.values ?: return
        val sensorType = event.sensor?.type ?: return
        val maxRange = event.sensor?.maximumRange ?: 0f

        processSensorSample(
            sensorType = sensorType,
            values = values,
            timestampNs = event.timestamp,
            maxRange = maxRange
        )
    }

    /**
     * Sensor Payload 및 Timestamp 유효성 검증 + Freshness 갱신 핵심 로직
     */
    internal fun processSensorSample(
        sensorType: Int,
        values: FloatArray,
        timestampNs: Long,
        maxRange: Float
    ): Boolean {
        if (!listening || values.isEmpty()) return false

        // 1. Validate payload finiteness (Reject NaN, Infinite or empty sensor arrays)
        for (v in values) {
            if (v.isNaN() || v.isInfinite()) {
                classifier.reset()
                updatePose()
                return false
            }
        }

        // 2. Validate sensor-type specific payload bounds, array length & maximumRange requirements
        val isPayloadValid = when (sensorType) {
            Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE -> values.size >= 3
            Sensor.TYPE_LIGHT -> values.size >= 1 && values[0] >= 0f
            Sensor.TYPE_PROXIMITY -> values.size >= 1 && values[0] >= 0f && maxRange > 0f && !maxRange.isNaN() && !maxRange.isInfinite() && values[0] <= maxRange
            else -> false // Unknown or unregistered sensor types MUST NOT update freshness!
        }

        if (!isPayloadValid) {
            classifier.reset()
            updatePose()
            return false
        }

        // 3. Validate timestamp range
        val now = try { SystemClock.elapsedRealtime() } catch (_: Throwable) { System.currentTimeMillis() }
        val sampleTime = timestampNs / 1_000_000L
        if (sampleTime <= 0L || sampleTime > now) {
            classifier.reset()
            updatePose()
            return false // Reject invalid timestamp events
        }

        // Freshness timestamp updated ONLY after payload length, value bounds, maximumRange, and timestamp verification pass
        lastEventTimestampMs = now

        when (sensorType) {
            Sensor.TYPE_ACCELEROMETER -> classifier.accelerometer(sampleTime, values)
            Sensor.TYPE_GYROSCOPE -> classifier.gyroscope(sampleTime, values)
            Sensor.TYPE_LIGHT -> classifier.light(sampleTime, values[0])
            Sensor.TYPE_PROXIMITY -> classifier.proximity(sampleTime, values[0], maxRange)
        }
        updatePose()
        return true
    }

    private fun updatePose() {
        val nowMs = try { SystemClock.elapsedRealtime() } catch (_: Throwable) { System.currentTimeMillis() }
        _observedPose = classifier.pose(nowMs)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            classifier.reset()
            lastEventTimestampMs = 0L // Invalidate freshness on unreliable sensor status
            _observedPose = DevicePose.HAND_HELD_ACTIVE
        }
    }

    // Test-helper for simulate sensor registration in JVM unit tests
    internal fun setTestRegisteredSensorsCount(count: Int) {
        registeredSensorsCount = count
        listening = count > 0
    }
}
