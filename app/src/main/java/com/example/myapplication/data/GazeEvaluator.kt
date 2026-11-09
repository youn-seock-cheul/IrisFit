package com.example.myapplication.data

import kotlin.math.hypot

object GazeEvaluator {

    /**
     * Calculates screen diagonal length in pixels.
     */
    fun calculateDiagonal(widthPx: Float, heightPx: Float): Float {
        return hypot(widthPx.toDouble(), heightPx.toDouble()).toFloat()
    }

    /**
     * Calculates the distance threshold based on screen diagonal.
     * Default tolerance is 10% of diagonal length (FR-302).
     */
    fun calculateThreshold(widthPx: Float, heightPx: Float, ratio: Float = 0.10f): Float {
        return calculateDiagonal(widthPx, heightPx) * ratio
    }

    /**
     * Evaluates whether gaze coordinates (gazeX, gazeY) are within tolerance distance
     * of target marker coordinates (markerX, markerY).
     */
    fun evaluateHit(
        markerX: Float,
        markerY: Float,
        gazeX: Float,
        gazeY: Float,
        screenWidthPx: Float,
        screenHeightPx: Float,
        toleranceRatio: Float = 0.10f
    ): Boolean {
        val distance = hypot(
            (markerX - gazeX).toDouble(),
            (markerY - gazeY).toDouble()
        ).toFloat()
        val threshold = calculateThreshold(screenWidthPx, screenHeightPx, toleranceRatio)
        return distance <= threshold
    }
}
