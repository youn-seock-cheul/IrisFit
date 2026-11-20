package com.example.myapplication.session

import android.content.Context
import java.time.LocalDate

data class EyeBatterySnapshot(
    val score: Int,
    val screenTimeMinutes: Int
)

object EyeBatterySnapshotStore {
    private const val PREFERENCES = "eye_battery_measurement_v1"

    fun save(context: Context, stats: EyeBatteryStats) {
        context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE).edit()
            .putString("snapshot_day", LocalDate.now().toString())
            .putBoolean("snapshot_measured", stats.screenTimeMeasured)
            .putInt("snapshot_score", stats.eyeHealthScore)
            .putInt("snapshot_screen_minutes", stats.screenTimeMinutes)
            .apply()
    }

    fun readToday(context: Context): EyeBatterySnapshot? {
        val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        if (!preferences.getBoolean("snapshot_measured", false) ||
            preferences.getString("snapshot_day", null) != LocalDate.now().toString()
        ) return null
        val score = preferences.getInt("snapshot_score", -1)
        if (score !in 0..100) return null
        return EyeBatterySnapshot(score, preferences.getInt("snapshot_screen_minutes", 0).coerceAtLeast(0))
    }
}
