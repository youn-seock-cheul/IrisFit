package com.example.myapplication

import com.example.myapplication.session.EyeBatteryCalculator
import com.example.myapplication.session.EyeFatigueStatus
import org.junit.Assert.assertEquals
import org.junit.Test

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
}
