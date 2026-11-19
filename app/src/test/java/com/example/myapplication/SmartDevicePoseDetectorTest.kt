package com.example.myapplication

import android.content.Context
import android.content.ContextWrapper
import android.hardware.Sensor
import android.hardware.SensorManager
import com.example.myapplication.data.SmartDevicePoseDetector
import com.example.myapplication.data.PoseSensorCallback
import com.example.myapplication.data.PoseSensorGateway
import com.example.myapplication.data.PoseSensorSample
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SmartDevicePoseDetectorTest {

    private class FakeSensorGateway(
        private val registration: (Int) -> Boolean = { true }
    ) : PoseSensorGateway {
        val callbacks = mutableMapOf<Int, MutableList<PoseSensorCallback>>()
        var unregisterCount = 0

        override fun register(sensorType: Int, callback: PoseSensorCallback): Boolean {
            if (!registration(sensorType)) return false
            callbacks.getOrPut(sensorType) { mutableListOf() }.add(callback)
            return true
        }

        override fun unregisterAll() {
            unregisterCount++
        }

        fun emit(
            sensorType: Int,
            callbackIndex: Int,
            timestampNs: Long
        ) {
            callbacks.getValue(sensorType)[callbackIndex].onSample(
                PoseSensorSample(sensorType, floatArrayOf(0f, 0f, 9.8f), timestampNs, 20f)
            )
        }
    }

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

    @Test
    fun allSensorRegistrationsFail_remainsUnavailableAndNotFresh() {
        val gateway = FakeSensorGateway { false }
        val unavailableDetector = SmartDevicePoseDetector(gateway)
        unavailableDetector.monotonicClockMsProvider = { 1000L }

        unavailableDetector.startListening()

        assertTrue(unavailableDetector.isListening)
        assertFalse(unavailableDetector.hasRegisteredSensors)
        assertFalse(unavailableDetector.isFresh)

        unavailableDetector.stopListening()
        assertFalse(unavailableDetector.isFresh)
        assertTrue(gateway.unregisterCount > 0)
    }

    @Test
    fun callbackFromPreviousSessionCannotRefreshCurrentSession() {
        val gateway = FakeSensorGateway()
        val sessionDetector = SmartDevicePoseDetector(gateway)
        var nowMs = 1000L
        sessionDetector.monotonicClockMsProvider = { nowMs }

        sessionDetector.startListening()
        val oldSessionCallback = 0
        sessionDetector.stopListening()
        assertFalse(sessionDetector.isFresh)

        sessionDetector.startListening()
        gateway.emit(Sensor.TYPE_ACCELEROMETER, oldSessionCallback, 500_000_000L)
        assertFalse(sessionDetector.isFresh)

        gateway.emit(Sensor.TYPE_ACCELEROMETER, 1, 900_000_000L)
        assertTrue(sessionDetector.isFresh)

        nowMs = 7000L
        assertFalse(sessionDetector.isFresh)
        gateway.emit(Sensor.TYPE_ACCELEROMETER, oldSessionCallback, 6_900_000_000L)
        assertFalse(sessionDetector.isFresh)
    }
}
