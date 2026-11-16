package com.example.myapplication

import android.content.ContextWrapper
import android.hardware.Sensor
import com.example.myapplication.data.ScreenTimeQueryResult
import com.example.myapplication.data.SmartDevicePoseDetector
import com.example.myapplication.data.TargetDeviceModel
import com.example.myapplication.data.ViewingTimeEvaluator
import com.example.myapplication.data.ViewingTimeStatus
import com.example.myapplication.session.EyeBatteryCalculator
import com.example.myapplication.session.EyeFatigueStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ViewingTimeEvaluatorTest {

    private val testContext = ContextWrapper(null)

    @Test
    fun testTargetDeviceDetection_GalaxyS23() {
        val model = TargetDeviceModel.detectDevice("SM-S911N")
        assertEquals(TargetDeviceModel.GALAXY_S23, model)
        assertFalse(model.isFoldable)
    }

    @Test
    fun testTargetDeviceDetection_GalaxyS23Plus() {
        val model = TargetDeviceModel.detectDevice("SM-S916N")
        assertEquals(TargetDeviceModel.GALAXY_S23, model)
        assertFalse(model.isFoldable)
    }

    @Test
    fun testTargetDeviceDetection_GalaxyZFold7() {
        val model = TargetDeviceModel.detectDevice("SM-F966N")
        assertEquals(TargetDeviceModel.GALAXY_Z_FOLD7, model)
        assertTrue(model.isFoldable)
    }

    @Test
    fun testEvaluate_PermissionDenied_ReturnsPermissionDeniedStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTimeInternal(
            context = testContext,
            hasPermission = false,
            isAppInForeground = true
        )
        assertTrue(result is ViewingTimeStatus.PermissionDenied)
    }

    @Test
    fun testEvaluate_BackgroundPaused_ReturnsBackgroundPausedStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTimeInternal(
            context = testContext,
            hasPermission = true,
            isAppInForeground = false
        )
        assertTrue(result is ViewingTimeStatus.BackgroundPaused)
        val pausedState = result as ViewingTimeStatus.BackgroundPaused
        assertEquals("앱 백그라운드 전환", pausedState.reason)
    }

    @Test
    fun testEvaluate_ScreenOff_ReturnsBackgroundPausedStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTimeInternal(
            context = testContext,
            hasPermission = true,
            isAppInForeground = true,
            isScreenOn = false
        )
        assertTrue(result is ViewingTimeStatus.BackgroundPaused)
        val pausedState = result as ViewingTimeStatus.BackgroundPaused
        assertEquals("화면 꺼짐", pausedState.reason)
    }

    @Test
    fun testEvaluate_SensorStaleOrNull_ReturnsSensorUnavailableStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTimeInternal(
            context = testContext,
            hasPermission = true,
            isAppInForeground = true,
            isScreenOn = true,
            poseDetector = null,
            isSensorFresh = false,
            mockQueryResult = ScreenTimeQueryResult.Success(45)
        )
        assertTrue(result is ViewingTimeStatus.SensorUnavailable)
    }

    @Test
    fun testEvaluate_MeasuredZeroMinutes_ReturnsMeasuredZeroStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTimeInternal(
            context = testContext,
            hasPermission = true,
            isAppInForeground = true,
            isScreenOn = true,
            poseDetector = null,
            isSensorFresh = true,
            mockQueryResult = ScreenTimeQueryResult.Success(0)
        )
        assertTrue(result is ViewingTimeStatus.MeasuredZero)
    }

    @Test
    fun testEvaluate_ConfigChanging_ReturnsConfigurationChangingStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTimeInternal(
            context = testContext,
            hasPermission = true,
            isAppInForeground = true,
            isConfigChanging = true
        )
        assertTrue(result is ViewingTimeStatus.ConfigurationChanging)
    }

    @Test
    fun testEvaluate_QueryError_ReturnsQueryErrorStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTimeInternal(
            context = testContext,
            hasPermission = true,
            isAppInForeground = true,
            mockQueryResult = ScreenTimeQueryResult.QueryError("UsageStatsService 오류")
        )
        assertTrue(result is ViewingTimeStatus.QueryError)
        val error = result as ViewingTimeStatus.QueryError
        assertTrue(error.errorDetail.contains("UsageStatsService 오류"))
    }

    @Test
    fun testEvaluate_NegativeMinutes_ReturnsQueryErrorStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTimeInternal(
            context = testContext,
            hasPermission = true,
            isAppInForeground = true,
            mockQueryResult = ScreenTimeQueryResult.Success(-10)
        )
        assertTrue(result is ViewingTimeStatus.QueryError)
        val error = result as ViewingTimeStatus.QueryError
        assertTrue(error.errorDetail.contains("음수 사용시간"))
    }

    @Test
    fun testEvaluate_ValidInputs_ReturnsFeasibilityEstimateStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTimeInternal(
            context = testContext,
            hasPermission = true,
            isAppInForeground = true,
            isScreenOn = true,
            poseDetector = null,
            isSensorFresh = true,
            mockQueryResult = ScreenTimeQueryResult.Success(120)
        )
        assertTrue(result is ViewingTimeStatus.FeasibilityEstimate)
        val estimate = result as ViewingTimeStatus.FeasibilityEstimate
        assertEquals(120, estimate.baselineUsageMinutes)
        assertTrue(estimate.disclaimer.contains("이론적 추정 모델"))
    }

    @Test
    fun testPoseDetectorInitialState_NotFreshBeforeStartListening() {
        val detector = SmartDevicePoseDetector(testContext)
        assertFalse("시작 전 detector는 listening이 false이어야 함", detector.isListening)
        assertFalse("시작 전 detector는 isFresh가 false이어야 함", detector.isFresh)
    }

    @Test
    fun testEyeBatteryCalculator_UnmeasuredStateReturnsUnmeasuredStatus() {
        val stats = EyeBatteryCalculator.calculateBattery(
            screenTimeMinutes = 0,
            restCount = 0,
            miniGameCount = 0,
            screenTimeMeasured = false
        )
        assertEquals(-1, stats.eyeHealthScore)
        assertEquals(EyeFatigueStatus.UNMEASURED, stats.fatigueStatus)
        assertFalse(stats.screenTimeMeasured)
    }

    @Test
    fun testPoseDetectorPayloadValidation_NaNValueRejection() {
        val detector = SmartDevicePoseDetector(testContext)
        val currentMonotonicTimeMs = 1000L
        detector.monotonicClockMsProvider = { currentMonotonicTimeMs }
        detector.setTestRegisteredSensorsCount(1)

        val nanPayload = floatArrayOf(Float.NaN, 0f, 9.8f)
        val accepted = detector.processSensorSample(
            sensorType = Sensor.TYPE_ACCELEROMETER,
            values = nanPayload,
            timestampNs = currentMonotonicTimeMs * 1_000_000L,
            maxRange = 100f
        )
        assertFalse("NaN 파이로드는 거부되어야 함", accepted)
        assertFalse("NaN 파이로드 수신 후 isFresh는 false이어야 함", detector.isFresh)
    }

    @Test
    fun testPoseDetectorPayloadValidation_ProximityExceedsMaxRangeRejection() {
        val detector = SmartDevicePoseDetector(testContext)
        val currentMonotonicTimeMs = 1000L
        detector.monotonicClockMsProvider = { currentMonotonicTimeMs }
        detector.setTestRegisteredSensorsCount(1)

        val outOfBoundsProximity = floatArrayOf(15f) // > maxRange (5f)
        val accepted = detector.processSensorSample(
            sensorType = Sensor.TYPE_PROXIMITY,
            values = outOfBoundsProximity,
            timestampNs = currentMonotonicTimeMs * 1_000_000L,
            maxRange = 5f
        )
        assertFalse("maxRange 초과 근접 센서 데이터는 거부되어야 함", accepted)
        assertFalse("maxRange 초과 수신 후 isFresh는 false이어야 함", detector.isFresh)
    }

    @Test
    fun testPoseDetectorPayloadValidation_ValidSampleUpdatesFreshness() {
        val detector = SmartDevicePoseDetector(testContext)
        val currentMonotonicTimeMs = 1000L
        detector.monotonicClockMsProvider = { currentMonotonicTimeMs }
        detector.setTestRegisteredSensorsCount(1)

        val validAccelerometerPayload = floatArrayOf(0f, 0f, 9.8f)
        val accepted = detector.processSensorSample(
            sensorType = Sensor.TYPE_ACCELEROMETER,
            values = validAccelerometerPayload,
            timestampNs = currentMonotonicTimeMs * 1_000_000L,
            maxRange = 100f
        )
        assertTrue("유효한 가속도계 파이로드는 승인되어야 함", accepted)
        assertTrue("유효 파이로드 수신 후 isFresh는 true이어야 함", detector.isFresh)
    }
}
