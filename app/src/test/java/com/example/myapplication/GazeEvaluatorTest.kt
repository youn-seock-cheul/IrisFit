package com.example.myapplication

import com.example.myapplication.data.GazeEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.hypot

class GazeEvaluatorTest {

    @Test
    fun `calculateDiagonal returns correct Euclidean diagonal`() {
        // 1080 x 1920
        val expected = hypot(1080.0, 1920.0).toFloat()
        val actual = GazeEvaluator.calculateDiagonal(1080f, 1920f)
        assertEquals(expected, actual, 0.01f)
    }

    @Test
    fun `evaluateHit returns true when gaze is within 10 percent of screen diagonal`() {
        val width = 1080f
        val height = 2400f
        val diagonal = GazeEvaluator.calculateDiagonal(width, height) // ~2632 px
        val threshold = diagonal * 0.10f // ~263.2 px

        // Point within threshold (100 px away)
        val isHit = GazeEvaluator.evaluateHit(
            markerX = 500f,
            markerY = 500f,
            gazeX = 550f,
            gazeY = 550f,
            screenWidthPx = width,
            screenHeightPx = height,
            toleranceRatio = 0.10f
        )
        assertTrue(isHit)
    }

    @Test
    fun `evaluateHit returns false when gaze exceeds 10 percent of screen diagonal`() {
        val width = 1080f
        val height = 2400f

        // Point outside threshold (500 px away > 263.2 px)
        val isHit = GazeEvaluator.evaluateHit(
            markerX = 100f,
            markerY = 100f,
            gazeX = 600f,
            gazeY = 600f,
            screenWidthPx = width,
            screenHeightPx = height,
            toleranceRatio = 0.10f
        )
        assertFalse(isHit)
    }
}
