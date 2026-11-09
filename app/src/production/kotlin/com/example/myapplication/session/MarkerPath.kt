package com.example.myapplication.session

import kotlin.math.PI
import kotlin.math.sin

/** Five steps, bounded by the original speed and range. */
object MarkerPath {
    fun cycleMs(speedStep: Int = 4): Long = 16_000L - speedStep.coerceIn(0, 4) * 1_500L
    fun rangePercent(rangeStep: Int = 4): Int = 60 + rangeStep.coerceIn(0, 4) * 10

    fun fraction(elapsedMs: Long, speedStep: Int = 4, rangeStep: Int = 4): Float {
        val cycle = cycleMs(speedStep)
        val amplitude = rangePercent(rangeStep) / 200.0
        return (0.5 + amplitude * sin((elapsedMs.coerceAtLeast(0L) % cycle) / cycle.toDouble() * 2 * PI)).toFloat()
    }

    fun centerX(elapsedMs: Long, width: Float, inset: Float,
                speedStep: Int = 4, rangeStep: Int = 4): Float {
        val margin = inset.coerceIn(0f, width.coerceAtLeast(0f) / 2f)
        return margin + (width - 2f * margin).coerceAtLeast(0f) * fraction(elapsedMs, speedStep, rangeStep)
    }
}
