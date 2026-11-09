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
        // Fallback baseline of 60 mins if usage stats permission is not yet granted, ensuring realistic active drain
        val effectiveMins = screenTimeMinutes.coerceAtLeast(60)

        // Penalty calculation: -1.5% per 15 minutes of screen time
        val penalty = ((effectiveMins.toFloat() / 15f) * 1.5f * poseMultiplier).toInt()

        // Recovery from completed rests and mini-games
        val restRecovery = restCount * 12
        val gameRecovery = miniGameCount * 20

        // Passive recharge when resting on desk / face down (+4% charging boost)
        val poseRecharge = if (isCharging) 4 else 0

        val score = (100 - penalty + restRecovery + gameRecovery + poseRecharge).coerceIn(20, 100)

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
