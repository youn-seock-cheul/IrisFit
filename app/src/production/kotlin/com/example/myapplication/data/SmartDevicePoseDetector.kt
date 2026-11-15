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
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val classifier = PoseClassifier()
    private val handler = Handler(Looper.getMainLooper())
    private var listening = false
    private var registeredSensorsCount = 0
    private var lastEventTimestampMs = 0L
    private var _observedPose by mutableStateOf(DevicePose.HAND_HELD_ACTIVE)

    val isListening: Boolean get() = listening
    val hasRegisteredSensors: Boolean get() = registeredSensorsCount > 0
    val isFresh: Boolean get() = listening && registeredSensorsCount > 0 && (lastEventTimestampMs > 0L && SystemClock.elapsedRealtime() - lastEventTimestampMs < 5000L)

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
            handler.postDelayed(this, 500L)
        }
    }

    fun startListening() {
        if (listening) return
        classifier.reset()
        _observedPose = DevicePose.HAND_HELD_ACTIVE
        listening = true
        registeredSensorsCount = 0
        lastEventTimestampMs = 0L // Must receive at least 1 real sensor event to become fresh!

        for (type in listOf(Sensor.TYPE_ACCELEROMETER, Sensor.TYPE_GYROSCOPE, Sensor.TYPE_PROXIMITY, Sensor.TYPE_LIGHT)) {
            val sensor = sensorManager?.getDefaultSensor(type) ?: continue
            if (sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI) == true) {
                registeredSensorsCount++
            } else {
                Log.w("IrisFitPose", "Sensor registration failed: type=$type")
            }
        }
        handler.post(freshnessCheck)
    }

    fun stopListening() {
        listening = false
        registeredSensorsCount = 0
        lastEventTimestampMs = 0L
        handler.removeCallbacks(freshnessCheck)
        sensorManager?.unregisterListener(this)
        classifier.reset()
        _observedPose = DevicePose.HAND_HELD_ACTIVE
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (!listening || event == null) return
        val now = SystemClock.elapsedRealtime()
        lastEventTimestampMs = now
        val sampleTime = event.timestamp / 1_000_000L
        if (sampleTime <= 0L || sampleTime > now) {
            classifier.reset()
            updatePose()
            return
        }
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> classifier.accelerometer(sampleTime, event.values)
            Sensor.TYPE_GYROSCOPE -> classifier.gyroscope(sampleTime, event.values)
            Sensor.TYPE_LIGHT -> if (event.values.isNotEmpty()) classifier.light(sampleTime, event.values[0])
            Sensor.TYPE_PROXIMITY -> if (event.values.isNotEmpty()) {
                classifier.proximity(sampleTime, event.values[0], event.sensor.maximumRange)
            }
        }
        updatePose()
    }

    private fun updatePose() {
        _observedPose = classifier.pose(SystemClock.elapsedRealtime())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            classifier.reset()
            _observedPose = DevicePose.HAND_HELD_ACTIVE
        }
    }
}
