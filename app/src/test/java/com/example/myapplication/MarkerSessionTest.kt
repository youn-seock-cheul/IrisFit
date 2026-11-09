package com.example.myapplication

import com.example.myapplication.session.*
import org.junit.Assert.*
import org.junit.Test

class MarkerSessionTest {
    @Test fun markerEndsAtTwentySecondsWithoutMarkingCompleted() {
        var now = 0L
        val session = GuidedSession({ now }) { "marker" }
        session.prepare(20_000)
        session.start()
        now = 19_999; session.tick()
        assertEquals(SessionPhase.RUNNING, session.state.phase)
        assertEquals(1L, session.state.remainingSeconds)
        now = 20_000; session.tick()
        assertEquals(SessionPhase.CONFIRM, session.state.phase)
        assertEquals(20_000L, session.state.durationMs)
    }

    @Test fun pauseFreezesTimeAndKeepsPositionAcrossResume() {
        var now = 0L
        val session = GuidedSession({ now }) { "marker" }
        session.prepare(20_000); session.start()
        now = 3_000; session.pause()
        val position = MarkerPath.fraction(session.state.elapsedMs)
        now = 80_000; session.tick()
        assertEquals(position, MarkerPath.fraction(session.state.elapsedMs), 0f)
        session.resume(); now = 81_000; session.tick()
        assertEquals(4_000L, session.state.elapsedMs)
        session.end()
        assertEquals(SessionPhase.ENDED, session.state.phase)
    }

    @Test fun pathStaysInsideInsetsAndRepeatsSmoothly() {
        for (time in 0L..20_000L step 10) {
            val x = MarkerPath.centerX(time, 320f, 28f)
            assertTrue(x in 28f..292f)
        }
        assertEquals(160f, MarkerPath.centerX(0, 320f, 28f), 0.001f)
        assertEquals(292f, MarkerPath.centerX(2500, 320f, 28f), 0.001f)
        assertEquals(28f, MarkerPath.centerX(7500, 320f, 28f), 0.001f)
        assertEquals(MarkerPath.fraction(0), MarkerPath.fraction(10_000), 0.001f)
    }

    @Test fun slowerAndNarrowerOptionsReduceSpeedAndAmplitude() {
        assertEquals(1f, MarkerPath.fraction(4000, speedStep = 0), 0.001f)
        assertTrue(MarkerPath.fraction(2500, speedStep = 0) < MarkerPath.fraction(2500))
        for (time in 0L..32_000L step 10) {
            assertTrue(MarkerPath.fraction(time, speedStep = 0, rangeStep = 0) in 0.2f..0.8f)
        }
        assertEquals(0.5f, MarkerPath.fraction(16_000, speedStep = 0), 0.001f)
    }

    @Test fun allFiveStepsHaveOrderedSpeedAndBoundedRange() {
        for (step in 0..4) {
            assertEquals(16_000L - step * 1500L, MarkerPath.cycleMs(step))
            assertEquals(60 + step * 10, MarkerPath.rangePercent(step))
            for (time in 0L..32_000L step 100) {
                val value = MarkerPath.fraction(time, step, step)
                assertTrue(value in 0f..1f)
            }
        }
        assertEquals(16_000L, MarkerPath.cycleMs(-1))
        assertEquals(100, MarkerPath.rangePercent(99))
    }

    @Test fun narrowOrZeroAreaHasNoOutOfBoundsCenter() {
        assertEquals(10f, MarkerPath.centerX(2500, 20f, 28f), 0f)
        assertEquals(0f, MarkerPath.centerX(2500, 0f, 28f), 0f)
    }
}
