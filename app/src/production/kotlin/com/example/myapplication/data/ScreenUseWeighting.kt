package com.example.myapplication.data

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

object ScreenUseWeighting {
    fun weightedDurationMillis(
        packageMultiplier: Float,
        startTimeMillis: Long,
        endTimeMillis: Long,
        zoneId: ZoneId
    ): Double {
        if (endTimeMillis <= startTimeMillis) return 0.0
        var cursor = Instant.ofEpochMilli(startTimeMillis).atZone(zoneId)
        val end = Instant.ofEpochMilli(endTimeMillis)
        var weightedMillis = 0.0
        while (cursor.toInstant().isBefore(end)) {
            val date = cursor.toLocalDate()
            val hour = cursor.hour
            val night = hour >= 22 || hour < 5
            val boundary = when {
                hour < 5 -> date.atTime(LocalTime.of(5, 0))
                hour < 22 -> date.atTime(LocalTime.of(22, 0))
                else -> date.plusDays(1).atTime(LocalTime.of(5, 0))
            }.atZone(zoneId).toInstant()
            val segmentEnd = minOf(boundary, end)
            val nightMultiplier = if (night) 1.25 else 1.0
            weightedMillis += java.time.Duration.between(cursor.toInstant(), segmentEnd).toMillis() *
                packageMultiplier.coerceAtLeast(1f) * nightMultiplier
            cursor = segmentEnd.atZone(zoneId)
        }
        return weightedMillis
    }
}
