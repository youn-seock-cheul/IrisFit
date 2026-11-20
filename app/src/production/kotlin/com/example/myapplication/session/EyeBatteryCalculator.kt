package com.example.myapplication.session

data class EyeBatteryStats(
    val screenTimeMinutes: Int,
    val screenTimeMeasured: Boolean,
    val restCount: Int,
    val miniGameCount: Int,
    val eyeHealthScore: Int, // 0% to 100% estimate, or -1 if not measured
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

    companion object {
        fun fromScore(score: Int): EyeFatigueStatus = when {
            score < 0 -> UNMEASURED
            score >= 85 -> OPTIMAL
            score >= 65 -> CAUTION
            score >= 45 -> WARNING
            else -> DANGER
        }
    }
}

object EyeBatteryCalculator {
    fun calculateBattery(
        screenTimeMinutes: Int,
        restCount: Int,
        miniGameCount: Int,
        screenTimeMeasured: Boolean = true,
        weightedScreenTimeMinutes: Int = screenTimeMinutes,
        healthSignals: HealthBatterySignals = HealthBatterySignals()
    ): EyeBatteryStats {
        return EyeBatteryEngine.calculateBattery(
            screenTimeMinutes = screenTimeMinutes,
            weightedScreenTimeMinutes = weightedScreenTimeMinutes,
            restCount = restCount,
            miniGameCount = miniGameCount,
            healthSignals = healthSignals,
            screenTimeMeasured = screenTimeMeasured
        )
    }
}
