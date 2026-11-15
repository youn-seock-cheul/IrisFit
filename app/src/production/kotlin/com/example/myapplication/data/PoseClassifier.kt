package com.example.myapplication.data

import kotlin.math.abs
import kotlin.math.sqrt

internal class PoseClassifier {
    private var accelAt: Long? = null
    private var gyroAt: Long? = null
    private var lightAt: Long? = null
    private var proximityAt: Long? = null
    private var stationarySince: Long? = null
    private var x = 0f
    private var y = 0f
    private var z = 0f
    private var light = 50f
    private var covered = false

    fun reset() {
        accelAt = null
        gyroAt = null
        lightAt = null
        proximityAt = null
        stationarySince = null
    }

    fun accelerometer(now: Long, values: FloatArray) {
        if (values.size < 3 || values.take(3).any { !it.isFinite() }) {
            accelAt = null
            return
        }
        x = values[0]; y = values[1]; z = values[2]; accelAt = now
    }

    fun gyroscope(now: Long, values: FloatArray) {
        if (values.size < 3 || values.take(3).any { !it.isFinite() }) {
            gyroAt = null
            stationarySince = null
            return
        }
        if (!fresh(gyroAt, now)) stationarySince = null
        val omega = sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2])
        if (omega > 0.08f || stationarySince == null) stationarySince = now
        gyroAt = now
    }

    fun light(now: Long, value: Float) {
        lightAt = if (value.isFinite() && value >= 0f) now else null
        light = value
    }

    fun proximity(now: Long, value: Float, maxRange: Float) {
        proximityAt = if (value.isFinite() && maxRange.isFinite() && maxRange > 0f) now else null
        covered = value < maxRange && value < 2f
    }

    fun pose(now: Long): DevicePose {
        if (!fresh(accelAt, now) || !fresh(gyroAt, now)) return DevicePose.HAND_HELD_ACTIVE
        val stationary = stationarySince?.let { now - it >= 2500L } == true
        if (z < -7f) {
            val isCovered = fresh(proximityAt, now) && covered
            val isDark = fresh(lightAt, now) && light < 5f
            return if (stationary || isCovered || isDark) DevicePose.DEVICE_FACE_DOWN
            else DevicePose.DEVICE_TILTED
        }
        if (z > 8f && abs(x) < 2.5f && abs(y) < 2.5f && stationary) {
            return DevicePose.DEVICE_FLAT
        }
        return DevicePose.HAND_HELD_ACTIVE
    }

    private fun fresh(at: Long?, now: Long) = at != null && now >= at && now - at <= 3000L
}
