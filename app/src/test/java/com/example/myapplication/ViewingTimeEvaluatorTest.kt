package com.example.myapplication

import android.content.ContextWrapper
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
        val result = ViewingTimeEvaluator.evaluateViewingTime(
            context = testContext,
            hasPermission = false,
            isAppInForeground = true
        )
        assertTrue(result is ViewingTimeStatus.PermissionDenied)
    }

    @Test
    fun testEvaluate_BackgroundPaused_ReturnsBackgroundPausedStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTime(
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
        val result = ViewingTimeEvaluator.evaluateViewingTime(
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
        val result = ViewingTimeEvaluator.evaluateViewingTime(
            context = testContext,
            hasPermission = true,
            isAppInForeground = true,
            isScreenOn = true,
            poseDetector = null,
            isSensorFresh = false,
            mockUsageMinutes = 45
        )
        assertTrue(result is ViewingTimeStatus.SensorUnavailable)
    }

    @Test
    fun testEvaluate_MeasuredZeroMinutes_ReturnsMeasuredZeroStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTime(
            context = testContext,
            hasPermission = true,
            isAppInForeground = true,
            isScreenOn = true,
            poseDetector = null,
            isSensorFresh = true,
            mockUsageMinutes = 0
        )
        assertTrue(result is ViewingTimeStatus.MeasuredZero)
    }

    @Test
    fun testEvaluate_ConfigChanging_ReturnsConfigurationChangingStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTime(
            context = testContext,
            hasPermission = true,
            isAppInForeground = true,
            isConfigChanging = true
        )
        assertTrue(result is ViewingTimeStatus.ConfigurationChanging)
    }

    @Test
    fun testEvaluate_QueryException_ReturnsQueryErrorStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTime(
            context = testContext,
            hasPermission = true,
            isAppInForeground = true,
            mockQueryException = RuntimeException("Service Unavailable")
        )
        assertTrue(result is ViewingTimeStatus.QueryError)
        val error = result as ViewingTimeStatus.QueryError
        assertTrue(error.errorDetail.contains("Service Unavailable"))
    }

    @Test
    fun testEvaluate_NegativeMinutes_ReturnsQueryErrorStatus() {
        val result = ViewingTimeEvaluator.evaluateViewingTime(
            context = testContext,
            hasPermission = true,
            isAppInForeground = true,
            mockUsageMinutes = -10
        )
        assertTrue(result is ViewingTimeStatus.QueryError)
        val error = result as ViewingTimeStatus.QueryError
        assertTrue(error.errorDetail.contains("음수 사용시간"))
    }
}
