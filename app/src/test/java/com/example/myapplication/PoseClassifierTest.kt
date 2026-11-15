package com.example.myapplication

import com.example.myapplication.data.DevicePose
import com.example.myapplication.data.PoseClassifier
import org.junit.Assert.assertEquals
import org.junit.Test

class PoseClassifierTest {
    private val flat = floatArrayOf(0f, 0f, 9.8f)
    private val still = floatArrayOf(0f, 0f, 0f)

    @Test fun missingGyroAndStaleEventsFallback() {
        val c = PoseClassifier()
        c.accelerometer(0, flat)
        assertEquals(DevicePose.HAND_HELD_ACTIVE, c.pose(2600))
        c.gyroscope(0, still)
        c.gyroscope(2500, still)
        c.accelerometer(2500, flat)
        assertEquals(DevicePose.DEVICE_FLAT, c.pose(2500))
        c.accelerometer(5000, flat)
        assertEquals(DevicePose.HAND_HELD_ACTIVE, c.pose(5501))
    }

    @Test fun resumingGyroMustEstablishStationarityAgain() {
        val c = PoseClassifier()
        c.accelerometer(0, flat)
        c.gyroscope(0, still)
        c.gyroscope(2500, still)
        c.accelerometer(2500, flat)
        assertEquals(DevicePose.DEVICE_FLAT, c.pose(2500))
        c.accelerometer(6000, flat)
        c.gyroscope(6000, still)
        assertEquals(DevicePose.HAND_HELD_ACTIVE, c.pose(6000))
        c.gyroscope(8500, still)
        c.accelerometer(8500, flat)
        assertEquals(DevicePose.DEVICE_FLAT, c.pose(8500))
        c.reset()
        assertEquals(DevicePose.HAND_HELD_ACTIVE, c.pose(8500))
    }

    @Test fun invalidReadingsAndStaleAuxiliarySensorsAreNotTrusted() {
        val c = PoseClassifier()
        c.light(0, 0f)
        c.proximity(0, 0f, 5f)
        c.gyroscope(4000, still)
        c.accelerometer(4000, floatArrayOf(0f, 0f, -9f))
        assertEquals(DevicePose.DEVICE_TILTED, c.pose(4000))
        c.gyroscope(4001, floatArrayOf(Float.NaN, 0f, 0f))
        assertEquals(DevicePose.HAND_HELD_ACTIVE, c.pose(4001))
    }

    @Test fun staleGyroscopeCannotKeepStationaryPosture() {
        val c = PoseClassifier()
        c.accelerometer(1000, flat)
        c.gyroscope(1000, still)
        c.gyroscope(3500, still)
        c.accelerometer(3500, flat)
        assertEquals(DevicePose.DEVICE_FLAT, c.pose(3500))
        assertEquals(DevicePose.HAND_HELD_ACTIVE, c.pose(6501))
    }
}
