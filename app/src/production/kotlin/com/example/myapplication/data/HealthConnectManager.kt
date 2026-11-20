package com.example.myapplication.data

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.example.myapplication.session.HealthBatterySignals
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object HealthConnectManager {
    private const val PROVIDER_PACKAGE = "com.google.android.apps.healthdata"

    val requiredPermissions = setOf(
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class)
    )

    fun sdkStatus(context: Context): Int =
        HealthConnectClient.getSdkStatus(context, PROVIDER_PACKAGE)

    fun isAvailable(context: Context): Boolean =
        sdkStatus(context) == HealthConnectClient.SDK_AVAILABLE

    fun permissionRequestContract() =
        androidx.health.connect.client.PermissionController.createRequestPermissionResultContract()

    fun hasAllPermissions(granted: Set<String>): Boolean =
        granted.containsAll(requiredPermissions)

    suspend fun grantedPermissions(context: Context): Set<String> =
        HealthConnectClient.getOrCreate(context).permissionController.getGrantedPermissions()

    suspend fun readDailySignals(context: Context): HealthBatterySignals {
        val client = HealthConnectClient.getOrCreate(context)
        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val todayStart = LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val sleepStart = todayStart.minus(Duration.ofHours(6))

        val sleep = client.readRecords(
            ReadRecordsRequest(
                recordType = SleepSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(sleepStart, now)
            )
        ).records
            .filter { it.endTime <= now }
            .maxByOrNull { it.endTime }

        val sleepMinutes = sleep?.let {
            val stagedSleepMs = it.stages
                .filter { stage -> stage.stage != SleepSessionRecord.STAGE_TYPE_AWAKE }
                .sumOf { stage ->
                    Duration.between(stage.startTime, stage.endTime).toMillis().coerceAtLeast(0L)
                }
            val totalSleepMs = stagedSleepMs.takeIf { duration -> duration > 0L }
                ?: Duration.between(it.startTime, it.endTime).toMillis()
            (totalSleepMs / 60_000L).toInt().coerceAtLeast(0)
        }
        val deepRatio = sleep?.let { record ->
            val totalSleepStageMs = record.stages
                .filter { it.stage != SleepSessionRecord.STAGE_TYPE_AWAKE }
                .sumOf { Duration.between(it.startTime, it.endTime).toMillis().coerceAtLeast(0L) }
            val deepSleepMs = record.stages
                .filter { it.stage == SleepSessionRecord.STAGE_TYPE_DEEP }
                .sumOf { Duration.between(it.startTime, it.endTime).toMillis().coerceAtLeast(0L) }
            if (totalSleepStageMs > 0L) (deepSleepMs.toFloat() / totalSleepStageMs).coerceIn(0f, 1f)
            else null
        }

        val steps = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(todayStart, now)
            )
        )[StepsRecord.COUNT_TOTAL] ?: 0L

        val outdoorSessions = client.readRecords(
            ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(todayStart, now)
            )
        ).records.filter { it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_HIKING }
        val outdoorMinutes = outdoorSessions.sumOf {
            Duration.between(it.startTime, it.endTime).toMinutes().toInt().coerceAtLeast(0)
        }

        return HealthBatterySignals(
            sleepMinutes = sleepMinutes,
            deepSleepRatio = deepRatio,
            steps = steps,
            outdoorActivityMinutes = outdoorMinutes
        )
    }
}
