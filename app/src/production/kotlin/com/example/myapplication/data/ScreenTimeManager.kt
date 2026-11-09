package com.example.myapplication.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.time.LocalDate
import java.time.ZoneId

object ScreenTimeManager {

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageStatsSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: Exception) {
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun getTodayScreenTimeMinutes(context: Context): Int {
        if (!hasUsageStatsPermission(context)) return 0
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return 0

        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val now = System.currentTimeMillis()

        val aggregateStats = usageStatsManager.queryAndAggregateUsageStats(startOfDay, now)
        if (aggregateStats.isNullOrEmpty()) return 0

        var totalMs = 0L
        for ((_, stats) in aggregateStats) {
            if (stats.lastTimeUsed >= startOfDay && stats.totalTimeInForeground > 0) {
                val timeSinceStartOfDay = (now - startOfDay).coerceAtLeast(0L)
                totalMs += stats.totalTimeInForeground.coerceAtMost(timeSinceStartOfDay)
            }
        }

        val totalMinutes = (totalMs / (1000 * 60)).toInt()
        val minutesSinceMidnight = ((now - startOfDay).coerceAtLeast(0L) / (1000 * 60)).toInt()

        // Strict cap at actual minutes elapsed since 00:00 AM local time today!
        return totalMinutes.coerceAtMost(minutesSinceMidnight)
    }
}
