package com.example.myapplication

import android.content.Context
import android.content.ContextWrapper
import android.hardware.Sensor
import android.hardware.SensorManager
import com.example.myapplication.data.SmartDevicePoseDetector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SmartDevicePoseDetectorTest {

    private class TestContext : ContextWrapper(null) {
        override fun getSystemService(name: String): Any? = null
        override fun getApplicationContext(): Context = this
    }

    private lateinit var detector: SmartDevicePoseDetector

    @Before
    fun setUp() {
        detector = SmartDevicePoseDetector(TestContext())
        detector.setTestRegisteredSensorsCount(1)
    }

    @Test
    fun clockThrowingException_returnsFailClosedNotFresh() {
        detector.monotonicClockMsProvider = { throw RuntimeException("Hardware Clock Exception") }
        val sampleNs = 100_000_000L
        val processed = detector.processSensorSample(
            sensorType = Sensor.TYPE_ACCELEROMETER,
            values = floatArrayOf(0f, 0f, 9.8f),
            timestampNs = sampleNs,
            maxRange = 20f
        )
        assertFalse(processed)
        assertFalse(detector.isFresh)
    }

    @Test
    fun timestampZeroAndNegative_rejectedFailClosed() {
        detector.monotonicClockMsProvider = { 1000L }

        val zeroProcessed = detector.processSensorSample(
            sensorType = Sensor.TYPE_ACCELEROMETER,
            values = floatArrayOf(0f, 0f, 9.8f),
            timestampNs = 0L,
            maxRange = 20f
        )
        assertFalse(zeroProcessed)

        val negativeProcessed = detector.processSensorSample(
            sensorType = Sensor.TYPE_ACCELEROMETER,
            values = floatArrayOf(0f, 0f, 9.8f),
            timestampNs = -1_000_000L,
            maxRange = 20f
        )
        assertFalse(negativeProcessed)
        assertFalse(detector.isFresh)
    }

    @Test
    fun infinityAndEmptyAndShortArray_rejectedFailClosed() {
        detector.monotonicClockMsProvider = { 1000L }

        // Infinity
        val infinityProcessed = detector.processSensorSample(
            sensorType = Sensor.TYPE_ACCELEROMETER,
            values = floatArrayOf(0f, Float.POSITIVE_INFINITY, 9.8f),
            timestampNs = 500_000_000L,
            maxRange = 20f
        )
        assertFalse(infinityProcessed)

        // Empty Array
        val emptyProcessed = detector.processSensorSample(
            sensorType = Sensor.TYPE_ACCELEROMETER,
            values = floatArrayOf(),
            timestampNs = 500_000_000L,
            maxRange = 20f
        )
        assertFalse(emptyProcessed)

        // Short Array for Accelerometer (needs at least 3)
        val shortProcessed = detector.processSensorSample(
            sensorType = Sensor.TYPE_ACCELEROMETER,
            values = floatArrayOf(0f, 9.8f),
            timestampNs = 500_000_000L,
            maxRange = 20f
        )
        assertFalse(shortProcessed)
        assertFalse(detector.isFresh)
    }

    @Test
    fun unknownSensorType_rejectedFailClosed() {
        detector.monotonicClockMsProvider = { 1000L }
        val unknownProcessed = detector.processSensorSample(
            sensorType = 9999, // Unknown sensor type
            values = floatArrayOf(1f, 2f, 3f),
            timestampNs = 500_000_000L,
            maxRange = 20f
        )
        assertFalse(unknownProcessed)
        assertFalse(detector.isFresh)
    }

    @Test
    fun proximityExactMaxRangeBoundary_acceptedAndValid() {
        detector.monotonicClockMsProvider = { 1000L }
        // Proximity values[0] == maxRange (5f == 5f)
        val validBoundary = detector.processSensorSample(
            sensorType = Sensor.TYPE_PROXIMITY,
            values = floatArrayOf(5f),
            timestampNs = 500_000_000L,
            maxRange = 5f
        )
        assertTrue(validBoundary)
        assertTrue(detector.isFresh)
    }

    @Test
    fun stopListeningAndUnreliableAccuracy_invalidatesFreshness() {
        detector.monotonicClockMsProvider = { 1000L }
        val validSample = detector.processSensorSample(
            sensorType = Sensor.TYPE_ACCELEROMETER,
            values = floatArrayOf(0f, 0f, 9.8f),
            timestampNs = 500_000_000L,
            maxRange = 20f
        )
        assertTrue(validSample)
        assertTrue(detector.isFresh)

        // Unreliable accuracy invalidates freshness
        detector.onAccuracyChanged(null, SensorManager.SENSOR_STATUS_UNRELIABLE)
        assertFalse(detector.isFresh)

        // Re-validate freshness
        detector.processSensorSample(
            sensorType = Sensor.TYPE_ACCELEROMETER,
            values = floatArrayOf(0f, 0f, 9.8f),
            timestampNs = 600_000_000L,
            maxRange = 20f
        )
        assertTrue(detector.isFresh)

        // Stop listening invalidates freshness
        detector.stopListening()
        assertFalse(detector.isFresh)
    }
}
