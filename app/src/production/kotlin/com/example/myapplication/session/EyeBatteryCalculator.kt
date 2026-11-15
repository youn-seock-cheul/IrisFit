package com.example.myapplication.session

import kotlin.math.roundToInt

data class EyeBatteryStats(
    val screenTimeMinutes: Int,
    val screenTimeMeasured: Boolean,
    val restCount: Int,
    val miniGameCount: Int,
    val eyeHealthScore: Int, // 20% to 100% Clamped Wellness Metric, or -1 if not measured
    val fatigueStatus: EyeFatigueStatus
)

enum class EyeFatigueStatus(
    val labelKey: String,
    val emoji: String,
    val descKey: String
) {
    OPTIMAL("status_optimal", "🟢", "desc_optimal"),
    CAUTION("status_caution", "🟡", "desc_caution"),
    WARNING("status_warning", "🟠", "desc_warning"),
    DANGER("status_danger", "🔴", "desc_danger"),
    UNMEASURED("screen_time_unmeasured", "⚪", "desc_unmeasured");
}

object EyeBatteryCalculator {
    fun calculateBattery(
        screenTimeMinutes: Int,
        restCount: Int,
        miniGameCount: Int,
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

        val safeScreenMins = screenTimeMinutes.coerceAtLeast(0).coerceAtMost(1440)
        val safeRestCount = restCount.coerceAtLeast(0).toLong()
        val safeGameCount = miniGameCount.coerceAtLeast(0).toLong()

        val baseDrainFloat = (safeScreenMins.toFloat() / 15f) * 1.0f

        val restRecovery = (safeRestCount * 12L).coerceAtMost(200L)
        val gameRecovery = (safeGameCount * 20L).coerceAtMost(200L)

        val rawScoreFloat = 100f - baseDrainFloat + restRecovery.toFloat() + gameRecovery.toFloat()
        val score = rawScoreFloat.roundToInt().coerceIn(20, 100)

        val status = when {
            score >= 85 -> EyeFatigueStatus.OPTIMAL
            score >= 65 -> EyeFatigueStatus.CAUTION
            score >= 45 -> EyeFatigueStatus.WARNING
            else -> EyeFatigueStatus.DANGER
        }

        return EyeBatteryStats(
            screenTimeMinutes = safeScreenMins,
            screenTimeMeasured = true,
            restCount = restCount,
            miniGameCount = miniGameCount,
            eyeHealthScore = score,
            fatigueStatus = status
        )
    }
}
