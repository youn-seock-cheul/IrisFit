package com.example.myapplication.session

data class EyeBatteryStats(
    val screenTimeMinutes: Int,
    val restCount: Int,
    val miniGameCount: Int,
    val eyeHealthScore: Int, // 10% to 100%
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
    DANGER("status_danger", "🔴", "desc_danger");

    val label: String get() = labelKey
    val description: String get() = descKey
}

object EyeBatteryCalculator {
    fun calculateBattery(
        screenTimeMinutes: Int,
        restCount: Int,
        miniGameCount: Int,
        poseMultiplier: Float = 1.0f,
        isCharging: Boolean = false
    ): EyeBatteryStats {
        // Effective screen minutes scaled smoothly
        val effectiveMins = screenTimeMinutes.coerceAtLeast(30)

        // Base screen time drain: -1.0% per 15 minutes (or smooth linear scale)
        val baseDrain = (effectiveMins.toFloat() / 15f) * 1.0f

        // Pose posture adjustment (gentle relative delta, avoiding massive jumps)
        val poseDelta = when {
            isCharging -> 3.0f          // +3% bonus for On Desk / Face Down resting posture
            poseMultiplier > 1.2f -> -3.0f // -3% slight penalty for Lying Down strain
            else -> 0.0f
        }

        // Recovery from completed rests and mini-games
        val restRecovery = restCount * 12
        val gameRecovery = miniGameCount * 20

        val score = (100 - baseDrain.toInt() + restRecovery + gameRecovery + poseDelta.toInt()).coerceIn(20, 100)

        val status = when {
            score >= 85 -> EyeFatigueStatus.OPTIMAL
            score >= 65 -> EyeFatigueStatus.CAUTION
            score >= 45 -> EyeFatigueStatus.WARNING
            else -> EyeFatigueStatus.DANGER
        }

        return EyeBatteryStats(
            screenTimeMinutes = screenTimeMinutes,
            restCount = restCount,
            miniGameCount = miniGameCount,
            eyeHealthScore = score,
            fatigueStatus = status
        )
    }
}
