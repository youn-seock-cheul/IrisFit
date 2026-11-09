package com.example.myapplication.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myapplication.R
import kotlin.math.abs
import kotlin.math.sqrt

enum class DevicePose(val labelKey: String, val drainMultiplier: Float, val isCharging: Boolean, val iconRes: Int) {
    HAND_HELD_ACTIVE("pose_handheld", 1.0f, false, R.drawable.ic_pose_handheld),
    LYING_DOWN_VIEWING("pose_lying", 1.5f, false, R.drawable.ic_pose_handheld),
    FLAT_ON_DESK("pose_flat", 0.2f, true, R.drawable.ic_pose_flat),
    TRUE_REST_FACE_DOWN("pose_facedown", 0.5f, true, R.drawable.ic_pose_facedown);

    val label: String get() = labelKey
}

class SmartDevicePoseDetector(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val proximitySensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    private val lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

    var currentPose by mutableStateOf(DevicePose.HAND_HELD_ACTIVE)
        private set

    private var lastGyroMoveTime = System.currentTimeMillis()
    private var isProximityCovered = false
    private var currentLightLux = 50.0f

    fun startListening() {
        accelSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        gyroSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        proximitySensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        lightSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
    }

    fun stopListening() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        when (event.sensor.type) {
            Sensor.TYPE_PROXIMITY -> {
                val distance = event.values[0]
                val maxRange = event.sensor.maximumRange
                isProximityCovered = distance < maxRange && distance < 2.0f
            }
            Sensor.TYPE_LIGHT -> {
                currentLightLux = event.values[0]
            }
            Sensor.TYPE_GYROSCOPE -> {
                val gx = event.values[0]
                val gy = event.values[1]
                val gz = event.values[2]
                val omega = sqrt(gx * gx + gy * gy + gz * gz)
                if (omega > 0.08f) {
                    lastGyroMoveTime = System.currentTimeMillis()
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                // 1. Check Face Down
                if (z < -7.0f) {
                    val isStationary = (System.currentTimeMillis() - lastGyroMoveTime) > 2000L
                    if (isProximityCovered || currentLightLux < 5.0f || isStationary) {
                        currentPose = DevicePose.TRUE_REST_FACE_DOWN
                    } else {
                        currentPose = DevicePose.LYING_DOWN_VIEWING
                    }
                    return
                }

                // 2. Check Flat on Desk
                val isFlat = z > 8.0f && abs(x) < 2.5f && abs(y) < 2.5f
                val isStationary = (System.currentTimeMillis() - lastGyroMoveTime) > 2500L

                if (isFlat && isStationary) {
                    currentPose = DevicePose.FLAT_ON_DESK
                    return
                }

                // 3. Default Hand-Held Active
                currentPose = DevicePose.HAND_HELD_ACTIVE
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
