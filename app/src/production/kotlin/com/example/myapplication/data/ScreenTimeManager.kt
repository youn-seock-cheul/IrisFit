package com.example.myapplication.data

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.time.LocalDate
import java.time.ZoneId

sealed class ScreenTimeQueryResult {
    data class Success(val minutes: Int) : ScreenTimeQueryResult()
    object PermissionDenied : ScreenTimeQueryResult()
    data class QueryError(val reason: String) : ScreenTimeQueryResult()
}

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

    fun queryTodayScreenTime(context: Context): ScreenTimeQueryResult {
        if (!hasUsageStatsPermission(context)) return ScreenTimeQueryResult.PermissionDenied
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return ScreenTimeQueryResult.QueryError("UsageStatsService를 시작할 수 없습니다.")

        val startOfDay = try {
            LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            return ScreenTimeQueryResult.QueryError("자정 시간 계산 오류: ${e.message}")
        }
        val now = System.currentTimeMillis()

        val aggregateStats = try {
            usageStatsManager.queryAndAggregateUsageStats(startOfDay, now)
        } catch (e: Exception) {
            return ScreenTimeQueryResult.QueryError("UsageStats 쿼리 실패: ${e.message}")
        }

        if (aggregateStats.isNullOrEmpty()) {
            return ScreenTimeQueryResult.QueryError("집계된 UsageStats 데이터가 비어 있습니다.")
        }

        var totalMs = 0L
        for ((_, stats) in aggregateStats) {
            if (stats.lastTimeUsed >= startOfDay && stats.totalTimeInForeground > 0) {
                val timeSinceStartOfDay = (now - startOfDay).coerceAtLeast(0L)
                totalMs += stats.totalTimeInForeground.coerceAtMost(timeSinceStartOfDay)
            }
        }

        val totalMinutes = (totalMs / (1000 * 60)).toInt()
        val minutesSinceMidnight = ((now - startOfDay).coerceAtLeast(0L) / (1000 * 60)).toInt()
        val cappedMinutes = totalMinutes.coerceAtMost(minutesSinceMidnight)

        return ScreenTimeQueryResult.Success(cappedMinutes)
    }

    @Deprecated("Use queryTodayScreenTime(context) to handle ScreenTimeQueryResult sealed class directly and avoid unmeasured 0-minute confusion.")
    fun getTodayScreenTimeMinutes(context: Context): Int {
        return when (val result = queryTodayScreenTime(context)) {
            is ScreenTimeQueryResult.Success -> result.minutes
            else -> -1
        }
    }

    fun getTodayScreenTimeFormatted(context: Context): String {
        return when (val result = queryTodayScreenTime(context)) {
            is ScreenTimeQueryResult.Success -> {
                val minutes = result.minutes
                val hours = minutes / 60
                val mins = minutes % 60
                if (hours > 0) "${hours}시간 ${mins}분" else "${mins}분"
            }
            is ScreenTimeQueryResult.PermissionDenied,
            is ScreenTimeQueryResult.QueryError -> "미측정"
        }
    }
}
