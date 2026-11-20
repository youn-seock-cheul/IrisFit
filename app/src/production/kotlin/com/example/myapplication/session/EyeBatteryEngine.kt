package com.example.myapplication.session

import kotlin.math.roundToInt

data class HealthBatterySignals(
    val sleepMinutes: Int? = null,
    val deepSleepRatio: Float? = null,
    val steps: Long = 0L,
    val outdoorActivityMinutes: Int = 0
)

object EyeBatteryEngine {
    const val REST_RECOVERY_PERCENT = 12
    const val TRAINING_RECOVERY_PERCENT = 20

    fun morningBattery(sleepMinutes: Int?, deepSleepRatio: Float?): Int {
        if (sleepMinutes == null) return 100
        val durationFactor = sleepMinutes.coerceAtLeast(0).coerceAtMost(480) / 480f
        val deepFactor = deepSleepRatio?.coerceIn(0f, 1f)
        return if (deepFactor == null) {
            (60f + 40f * durationFactor).roundToInt()
        } else {
            (60f + 32f * durationFactor + 8f * (deepFactor / 0.2f).coerceIn(0f, 1f)).roundToInt()
        }.coerceIn(60, 100)
    }

    fun activityBonus(steps: Long, outdoorActivityMinutes: Int): Int {
        val stepBonus = (steps.coerceAtLeast(0L) / 2_000L).toInt()
        val outdoorBonus = outdoorActivityMinutes.coerceAtLeast(0) / 30
        return (stepBonus + outdoorBonus).coerceAtMost(5)
    }

    fun calculateBattery(
        screenTimeMinutes: Int,
        weightedScreenTimeMinutes: Int = screenTimeMinutes,
        restCount: Int,
        miniGameCount: Int,
        healthSignals: HealthBatterySignals = HealthBatterySignals(),
        screenTimeMeasured: Boolean = true
    ): EyeBatteryStats {
        if (!screenTimeMeasured) {
            return EyeBatteryStats(
                screenTimeMinutes = 0,
                screenTimeMeasured = false,
                restCount = restCount,
                miniGameCount = miniGameCount,
                eyeHealthScore = -1,
                fatigueStatus = EyeFatigueStatus.UNMEASURED
            )
        }

        val safeScreenMins = screenTimeMinutes.coerceIn(0, 1440)
        val safeWeightedMins = weightedScreenTimeMinutes.coerceIn(0, 2880)
        val restRecovery = (restCount.coerceAtLeast(0).toLong() * REST_RECOVERY_PERCENT).coerceAtMost(200L)
        val gameRecovery = (miniGameCount.coerceAtLeast(0).toLong() * TRAINING_RECOVERY_PERCENT).coerceAtMost(200L)
        val initialBattery = morningBattery(healthSignals.sleepMinutes, healthSignals.deepSleepRatio)
        val activityRecovery = activityBonus(healthSignals.steps, healthSignals.outdoorActivityMinutes)
        val drain = safeWeightedMins / 15f
        val score = (initialBattery - drain + restRecovery + gameRecovery + activityRecovery)
            .roundToInt()
            .coerceIn(0, 100)

        return EyeBatteryStats(
            screenTimeMinutes = safeScreenMins,
            screenTimeMeasured = true,
            restCount = restCount.coerceAtLeast(0),
            miniGameCount = miniGameCount.coerceAtLeast(0),
            eyeHealthScore = score,
            fatigueStatus = EyeFatigueStatus.fromScore(score)
        )
    }
}
