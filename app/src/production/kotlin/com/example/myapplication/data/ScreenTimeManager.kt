package com.example.myapplication.data

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.roundToInt
import com.example.myapplication.session.AppLocalization

sealed class ScreenTimeQueryResult {
    data class Success(
        val minutes: Int,
        val weightedMinutes: Int = minutes,
        val weightingAvailable: Boolean = true
    ) : ScreenTimeQueryResult()
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

        var totalMs = 0L
        for ((packageName, stats) in aggregateStats.orEmpty()) {
            if (packageName == context.packageName) continue
            if (stats.lastTimeUsed >= startOfDay && stats.totalTimeInForeground > 0) {
                val timeSinceStartOfDay = (now - startOfDay).coerceAtLeast(0L)
                totalMs += stats.totalTimeInForeground.coerceAtMost(timeSinceStartOfDay)
            }
        }

        val minutesSinceMidnight = ((now - startOfDay).coerceAtLeast(0L) / (1000 * 60)).toInt()
        val aggregateMinutes = (totalMs / (1000 * 60)).toInt().coerceAtMost(minutesSinceMidnight)
        if (aggregateMinutes == 0) return ScreenTimeQueryResult.Success(0, 0)
        val weightedUsage = queryWeightedForegroundUsage(
            context = context,
            usageStatsManager = usageStatsManager,
            startOfDay = startOfDay,
            now = now
        )

        return if (weightedUsage == null || weightedUsage.minutes == 0) {
            ScreenTimeQueryResult.Success(
                minutes = aggregateMinutes,
                weightedMinutes = aggregateMinutes,
                weightingAvailable = false
            )
        } else {
            val multiplier = weightedUsage.weightedMinutes.toFloat() / weightedUsage.minutes
            ScreenTimeQueryResult.Success(
                minutes = aggregateMinutes,
                weightedMinutes = (aggregateMinutes * multiplier).roundToInt().coerceAtMost(minutesSinceMidnight * 2),
                weightingAvailable = true
            )
        }
    }

    private data class WeightedUsage(val minutes: Int, val weightedMinutes: Int)

    private fun queryWeightedForegroundUsage(
        context: Context,
        usageStatsManager: UsageStatsManager,
        startOfDay: Long,
        now: Long
    ): WeightedUsage? {
        return try {
            val events = usageStatsManager.queryEvents(startOfDay - 24L * 60L * 60L * 1000L, now)
                ?: return null
            val event = UsageEvents.Event()
            var screenInteractive = true
            var activePackage: String? = null
            var activeSince: Long? = null
            var totalMs = 0L
            var weightedMs = 0.0
            var eventCount = 0

            fun closeInterval(endTime: Long) {
                val packageName = activePackage
                val intervalStart = activeSince
                if (packageName != null && intervalStart != null) {
                    val clippedStart = maxOf(intervalStart, startOfDay)
                    val clippedEnd = minOf(endTime, now)
                    if (clippedEnd > clippedStart) {
                        totalMs += clippedEnd - clippedStart
                        weightedMs += ScreenUseWeighting.weightedDurationMillis(
                            packageMultiplier = packageMultiplier(context, packageName),
                            startTimeMillis = clippedStart,
                            endTimeMillis = clippedEnd,
                            zoneId = ZoneId.systemDefault()
                        )
                    }
                }
                activeSince = null
            }

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                eventCount++
                when (event.eventType) {
                    UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                        if (screenInteractive) closeInterval(event.timeStamp)
                        screenInteractive = false
                    }
                    UsageEvents.Event.SCREEN_INTERACTIVE -> {
                        screenInteractive = true
                        if (activePackage != null) activeSince = event.timeStamp
                    }
                    UsageEvents.Event.ACTIVITY_RESUMED,
                    UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        if (screenInteractive) closeInterval(event.timeStamp)
                        activePackage = event.packageName?.takeIf { it != context.packageName }
                        if (screenInteractive && activePackage != null) activeSince = event.timeStamp
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED,
                    UsageEvents.Event.ACTIVITY_STOPPED,
                    UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        if (event.packageName == activePackage) {
                            if (screenInteractive) closeInterval(event.timeStamp)
                            activePackage = null
                        }
                    }
                }
            }
            if (screenInteractive) closeInterval(now)
            if (eventCount == 0) null
            else WeightedUsage(
                minutes = (totalMs / 60_000L).toInt(),
                weightedMinutes = (weightedMs / 60_000.0).toInt()
            )
        } catch (_: RuntimeException) {
            null
        }
    }

    private fun packageMultiplier(context: Context, packageName: String): Float {
        val category = try {
            @Suppress("DEPRECATION")
            context.packageManager.getApplicationInfo(packageName, 0).category
        } catch (_: android.content.pm.PackageManager.NameNotFoundException) {
            android.content.pm.ApplicationInfo.CATEGORY_UNDEFINED
        }
        return when (category) {
            android.content.pm.ApplicationInfo.CATEGORY_GAME,
            android.content.pm.ApplicationInfo.CATEGORY_VIDEO -> 1.25f
            android.content.pm.ApplicationInfo.CATEGORY_SOCIAL -> 1.1f
            else -> 1f
        }
    }

    @Deprecated("Use queryTodayScreenTime(context) to handle ScreenTimeQueryResult sealed class directly and avoid unmeasured 0-minute confusion.")
    fun getTodayScreenTimeMinutes(context: Context): Int {
        return when (val result = queryTodayScreenTime(context)) {
            is ScreenTimeQueryResult.Success -> result.minutes
            else -> -1
        }
    }

    fun getTodayScreenTimeFormatted(context: Context): String {
        return formatScreenTime(queryTodayScreenTime(context), LanguageManager.effectiveLanguage(context))
    }

    fun formatScreenTime(result: ScreenTimeQueryResult, lang: String): String {
        return when (result) {
            is ScreenTimeQueryResult.Success -> {
                val minutes = result.minutes
                val hours = minutes / 60
                val mins = minutes % 60
                if (hours > 0) AppLocalization.get(lang, "duration_hours_minutes").format(hours, mins)
                else AppLocalization.get(lang, "duration_minutes").format(mins)
            }
            is ScreenTimeQueryResult.PermissionDenied,
            is ScreenTimeQueryResult.QueryError -> AppLocalization.get(lang, "not_measured")
        }
    }
}
