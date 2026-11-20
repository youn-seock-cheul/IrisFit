package com.example.myapplication

import com.example.myapplication.session.EyeBatteryCalculator
import com.example.myapplication.session.EyeBatteryEngine
import com.example.myapplication.session.EyeFatigueStatus
import com.example.myapplication.session.HealthBatterySignals
import com.example.myapplication.session.HealthBatterySummaryFormatter
import com.example.myapplication.data.ScreenUseWeighting
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class EyeBatteryCalculatorTest {
    @Test fun zeroScreenTimeDoesNotCreateArtificialDrain() {
        assertEquals(100, EyeBatteryCalculator.calculateBattery(0, 0, 0).eyeHealthScore)
    }

    @Test fun screenTimeDrainIsLinearPerFifteenMinutes() {
        assertEquals(99, EyeBatteryCalculator.calculateBattery(15, 0, 0).eyeHealthScore)
        assertEquals(98, EyeBatteryCalculator.calculateBattery(30, 0, 0).eyeHealthScore)
        assertEquals(97, EyeBatteryCalculator.calculateBattery(45, 0, 0).eyeHealthScore)
    }

    @Test fun eightMinutesRoundsScoreToNinetyNine() {
        assertEquals(100, EyeBatteryCalculator.calculateBattery(7, 0, 0).eyeHealthScore)
        assertEquals(99, EyeBatteryCalculator.calculateBattery(8, 0, 0).eyeHealthScore)
    }

    @Test fun restAndGameRecoveryAreAppliedAndCapped() {
        assertEquals(100, EyeBatteryCalculator.calculateBattery(30, 1, 0).eyeHealthScore)
        assertEquals(100, EyeBatteryCalculator.calculateBattery(30, 5, 3).eyeHealthScore)
    }

    @Test fun negativeInputsCannotIncreaseDrainOrRecovery() {
        assertEquals(100, EyeBatteryCalculator.calculateBattery(-20, -1, -1).eyeHealthScore)
    }

    @Test fun veryLargeRewardCountsCannotOverflow() {
        assertEquals(100, EyeBatteryCalculator.calculateBattery(0, Int.MAX_VALUE, Int.MAX_VALUE).eyeHealthScore)
    }

    @Test fun unmeasuredScreenTimeReturnsUnmeasuredStatusAndNegativeScore() {
        val result = EyeBatteryCalculator.calculateBattery(0, 0, 0, screenTimeMeasured = false)
        assertEquals(-1, result.eyeHealthScore)
        assertEquals(false, result.screenTimeMeasured)
        assertEquals(EyeFatigueStatus.UNMEASURED, result.fatigueStatus)
    }

    @Test fun statusUsesCalculatedScoreThresholds() {
        assertEquals(EyeFatigueStatus.OPTIMAL, EyeBatteryCalculator.calculateBattery(0, 0, 0).fatigueStatus)
        assertEquals(EyeFatigueStatus.DANGER, EyeBatteryCalculator.calculateBattery(5000, 0, 0).fatigueStatus)
    }

    @Test fun weightedScoresKeepTheirValueAndUseTheOriginalFourBands() {
        val bands = mapOf(
            0..44 to EyeFatigueStatus.DANGER,
            45..64 to EyeFatigueStatus.WARNING,
            65..84 to EyeFatigueStatus.CAUTION,
            85..100 to EyeFatigueStatus.OPTIMAL
        )
        for ((range, status) in bands) {
            for (score in range) {
                val weightedMinutes = (100 - score) * 15
                val stats = EyeBatteryEngine.calculateBattery(weightedMinutes / 2, weightedMinutes, 0, 0)
                assertEquals(score, stats.eyeHealthScore)
                assertEquals("$score%", status, stats.fatigueStatus)
            }
        }
        assertEquals(EyeFatigueStatus.UNMEASURED, EyeFatigueStatus.fromScore(-1))
    }

    @Test fun sleepAndActivitySignalsAreBoundedAndDeterministic() {
        assertEquals(60, EyeBatteryEngine.morningBattery(0, 0f))
        assertEquals(100, EyeBatteryEngine.morningBattery(480, 0.2f))
        assertEquals(5, EyeBatteryEngine.activityBonus(20_000, 240))
        assertEquals(2, EyeBatteryEngine.activityBonus(4_000, 0))
    }

    @Test fun healthSummaryExplainsTheActualSleepScoreAndActivityBonus() {
        val summaries = HealthBatterySummaryFormatter.format(
            HealthBatterySignals(
                sleepMinutes = 300,
                deepSleepRatio = 0.1f,
                steps = 4_000,
                outdoorActivityMinutes = 30
            ),
            "ko"
        )

        assertEquals(
            listOf(
                "지난밤 수면 5시간 0분 · 깊은 수면 10% → 시작 잔량 84%",
                "오늘 4000걸음 · 등산 30분 → 활동 보너스 +3%"
            ),
            summaries
        )
    }

    @Test fun healthSummaryExplainsTheDefaultWhenSleepIsUnavailable() {
        assertEquals(
            listOf("수면 기록 없음 → 시작 잔량 100%"),
            HealthBatterySummaryFormatter.format(HealthBatterySignals(), "ko")
        )
    }

    @Test fun nighttimeUsageGetsOnlyTheNightSegmentWeight() {
        val start = Instant.parse("2026-11-19T12:30:00Z").toEpochMilli()
        val end = Instant.parse("2026-11-19T13:30:00Z").toEpochMilli()
        val weighted = ScreenUseWeighting.weightedDurationMillis(
            packageMultiplier = 1f,
            startTimeMillis = start,
            endTimeMillis = end,
            zoneId = ZoneId.of("Asia/Seoul")
        )
        assertEquals(67.5 * 60_000.0, weighted, 1.0)
    }
}
