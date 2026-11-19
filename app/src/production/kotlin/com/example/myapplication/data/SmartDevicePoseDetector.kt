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

internal data class PoseSensorSample(
    val sensorType: Int,
    val values: FloatArray,
    val timestampNs: Long,
    val maxRange: Float
)

internal interface PoseSensorCallback {
    fun onSample(sample: PoseSensorSample)
    fun onUnreliable()
}

internal interface PoseSensorGateway {
    fun register(sensorType: Int, callback: PoseSensorCallback): Boolean
    fun unregisterAll()
}

private class AndroidPoseSensorGateway(context: Context) : PoseSensorGateway {
    private fun logError(message: String, error: Throwable? = null) {
        try {
            if (error == null) Log.e("SmartDevicePoseDetector", message)
            else Log.e("SmartDevicePoseDetector", message, error)
        } catch (_: Throwable) {}
    }

    private fun logWarning(message: String) {
        try {
            Log.w("SmartDevicePoseDetector", message)
        } catch (_: Throwable) {}
    }

    private val sensorManager = try {
        context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    } catch (e: Exception) {
        logError("Failed to access SENSOR_SERVICE", e)
        null
    }
    private val listeners = mutableListOf<SensorEventListener>()

    override fun register(sensorType: Int, callback: PoseSensorCallback): Boolean {
        return try {
            val manager = sensorManager ?: return false
            val sensor = manager.getDefaultSensor(sensorType) ?: return false
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    if (event == null) return
                    val type = event.sensor?.type ?: return
                    val maxRange = event.sensor?.maximumRange ?: 0f
                    callback.onSample(
                        PoseSensorSample(type, event.values?.copyOf() ?: return, event.timestamp, maxRange)
                    )
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                    if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) callback.onUnreliable()
                }
            }
            if (manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)) {
                listeners += listener
                true
            } else {
                logWarning("Sensor registration failed for type: $sensorType")
                false
            }
        } catch (e: Exception) {
            logError("Sensor registration threw for type: $sensorType", e)
            false
        }
    }

    override fun unregisterAll() {
        val registered = listeners.toList()
        listeners.clear()
        registered.forEach { listener ->
            try {
                sensorManager?.unregisterListener(listener)
            } catch (e: Exception) {
                logError("Sensor unregistration failed", e)
            }
        }
    }
}

enum class DevicePose(val labelKey: String, val iconRes: Int) {
    HAND_HELD_ACTIVE("pose_handheld", R.drawable.ic_pose_handheld),
    DEVICE_TILTED("pose_tilted", R.drawable.ic_pose_handheld),
    DEVICE_FLAT("pose_flat", R.drawable.ic_pose_flat),
    DEVICE_FACE_DOWN("pose_facedown", R.drawable.ic_pose_facedown);
    val label: String get() = labelKey
}

class SmartDevicePoseDetector internal constructor(
    private val sensorGateway: PoseSensorGateway
) {
    constructor(context: Context) : this(AndroidPoseSensorGateway(context))

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

    private val classifier = PoseClassifier()
    private val handler by lazy {
        try {
            Handler(Looper.getMainLooper())
        } catch (e: Exception) {
            safeLogE("SmartDevicePoseDetector", "Failed to access MainLooper Handler: ${e.message}", e)
            null
        }
    }

    // Monotonic clock provider (elapsedRealtime domain only; no wall clock mixing)
    internal var monotonicClockMsProvider: () -> Long = {
        try {
            SystemClock.elapsedRealtime()
        } catch (_: Throwable) {
            0L // Fail-closed when Android SystemClock is not available (e.g. JVM unit test without clock provider)
        }
    }

    private var listening = false
    private var registeredSensorsCount = 0
    private var lastEventTimestampMs = 0L
    private var listenerGeneration = 0L
    private var _observedPose by mutableStateOf(DevicePose.HAND_HELD_ACTIVE)

    val isListening: Boolean get() = listening
    val hasRegisteredSensors: Boolean get() = registeredSensorsCount > 0
    val isFresh: Boolean get() {
        val nowMs = try { monotonicClockMsProvider() } catch (_: Throwable) { 0L }
        return listening && registeredSensorsCount > 0 && lastEventTimestampMs > 0L && nowMs > 0L && (nowMs - lastEventTimestampMs < 5000L)
    }

    val currentPose: DevicePose
        get() {
            if (!listening) return DevicePose.HAND_HELD_ACTIVE
            val nowMs = monotonicClockMsProvider()
            val evaluated = classifier.pose(if (nowMs > 0L) nowMs else 0L)
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
        val generation = ++listenerGeneration
        val callback = object : PoseSensorCallback {
            override fun onSample(sample: PoseSensorSample) {
                if (!listening || listenerGeneration != generation) return
                processSensorSample(sample.sensorType, sample.values, sample.timestampNs, sample.maxRange)
            }

            override fun onUnreliable() {
                if (!listening || listenerGeneration != generation) return
                invalidateFreshness()
            }
        }

        for (type in listOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_PROXIMITY, Sensor.TYPE_LIGHT)) {
            try {
                if (sensorGateway.register(type, callback)) {
                    registeredSensorsCount++
                } else {
                    safeLogW("SmartDevicePoseDetector", "Sensor unavailable or registration failed for type: $type")
                }
            } catch (e: Exception) {
                safeLogE("SmartDevicePoseDetector", "Sensor registration failed for type: $type", e)
            }
        }
        if (registeredSensorsCount > 0) handler?.post(freshnessCheck)
    }

    fun stopListening() {
        listening = false
        listenerGeneration++
        registeredSensorsCount = 0
        lastEventTimestampMs = 0L
        handler?.removeCallbacks(freshnessCheck)
        sensorGateway.unregisterAll()
        classifier.reset()
        _observedPose = DevicePose.HAND_HELD_ACTIVE
    }

    fun onSensorChanged(event: SensorEvent?) {
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
     * Sensor Payload 및 Monotonic Timestamp 유효성 검증 + Freshness 갱신 핵심 로직
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

        // 3. Validate monotonic timestamp range (Strict elapsedRealtime domain; Fail-closed if clock is 0L or throws)
        val nowMs = try { monotonicClockMsProvider() } catch (_: Throwable) { 0L }
        if (nowMs <= 0L) {
            classifier.reset()
            updatePose()
            return false // Fail-closed on clock domain error
        }

        val sampleTime = timestampNs / 1_000_000L
        if (sampleTime <= 0L || sampleTime > nowMs) {
            classifier.reset()
            updatePose()
            return false // Reject invalid or future timestamp events
        }

        // Freshness timestamp updated ONLY after payload length, value bounds, maximumRange, and timestamp verification pass
        lastEventTimestampMs = nowMs

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
        val nowMs = try { monotonicClockMsProvider() } catch (_: Throwable) { 0L }
        _observedPose = classifier.pose(if (nowMs > 0L) nowMs else 0L)
    }

    fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            invalidateFreshness()
        }
    }

    private fun invalidateFreshness() {
        classifier.reset()
        lastEventTimestampMs = 0L
        _observedPose = DevicePose.HAND_HELD_ACTIVE
    }

    // Test-helpers for deterministic simulation in JVM unit tests
    internal fun setTestRegisteredSensorsCount(count: Int) {
        registeredSensorsCount = count
        listening = count > 0
    }
}
