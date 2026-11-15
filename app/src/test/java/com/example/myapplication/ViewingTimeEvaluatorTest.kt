package com.example.myapplication

import android.content.ContextWrapper
import com.example.myapplication.data.ScreenTimeQueryResult
import com.example.myapplication.data.TargetDeviceModel
import com.example.myapplication.data.ViewingTimeEvaluator
import com.example.myapplication.data.ViewingTimeStatus
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
}
