package com.example.myapplication.session

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

data class EyePedometerStats(
    val totalRestCount: Int,
    val totalEyeSteps: Int,
    val totalPoints: Int
)

object EyePedometerCalculator {
    fun calculateSessionReward(record: CompletionRecord): Pair<Int, Int> {
        val hour = LocalTime.ofInstant(
            Instant.ofEpochMilli(record.completedAtEpochMillis),
            ZoneId.systemDefault()
        ).hour

        val isNightGuard = hour >= 22 || hour < 6
        val baseSteps = when (record.routineId) {
            "smooth_pursuit" -> 250
            "eye_gym" -> 200
            else -> 100
        }
        val basePoints = when (record.routineId) {
            "smooth_pursuit" -> 120
            "eye_gym" -> 100
            else -> 50
        }

        val steps = if (isNightGuard) (baseSteps * 1.5).toInt() else baseSteps
        val points = if (isNightGuard) (basePoints * 1.5).toInt() else basePoints
        return steps to points
    }

    fun calculateStats(records: List<CompletionRecord>, today: String): EyePedometerStats {
        val todayRecords = records.filter { it.completedLocalDate == today }
        var totalSteps = 0
        var totalPoints = 0

        for (record in todayRecords) {
            val (steps, points) = calculateSessionReward(record)
            totalSteps += steps
            totalPoints += points
        }

        return EyePedometerStats(
            totalRestCount = todayRecords.size,
            totalEyeSteps = totalSteps,
            totalPoints = totalPoints
        )
    }
}
