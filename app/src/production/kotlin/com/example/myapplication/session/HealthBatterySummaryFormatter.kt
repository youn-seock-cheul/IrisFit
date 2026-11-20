package com.example.myapplication.session

import java.util.Locale
import kotlin.math.roundToInt

object HealthBatterySummaryFormatter {
    fun format(signals: HealthBatterySignals, language: String): List<String> {
        val initialBattery = EyeBatteryEngine.morningBattery(
            signals.sleepMinutes,
            signals.deepSleepRatio
        )
        val sleepSummary = when {
            signals.sleepMinutes == null -> localized(
                language,
                "health_sleep_missing_summary",
                initialBattery
            )
            signals.deepSleepRatio != null -> localized(
                language,
                "health_sleep_deep_summary",
                signals.sleepMinutes / 60,
                signals.sleepMinutes % 60,
                (signals.deepSleepRatio.coerceIn(0f, 1f) * 100).roundToInt(),
                initialBattery
            )
            else -> localized(
                language,
                "health_sleep_summary",
                signals.sleepMinutes / 60,
                signals.sleepMinutes % 60,
                initialBattery
            )
        }
        return buildList {
            add(sleepSummary)
            if (signals.steps > 0 || signals.outdoorActivityMinutes > 0) {
                add(
                    localized(
                        language,
                        "health_activity_summary",
                        signals.steps,
                        signals.outdoorActivityMinutes,
                        EyeBatteryEngine.activityBonus(
                            signals.steps,
                            signals.outdoorActivityMinutes
                        )
                    )
                )
            }
        }
    }

    private fun localized(language: String, key: String, vararg arguments: Any): String =
        String.format(
            Locale.forLanguageTag(language),
            AppLocalization.get(language, key),
            *arguments
        )
}
