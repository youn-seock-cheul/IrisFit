package com.example.myapplication.data

import android.content.Context
import android.os.Build

/**
 * Android 13+ (API 33+) 타겟 실제 시청시간 측정 및 타당성 분석 결과 상태
 */
sealed class ViewingTimeStatus {
    /**
     * 이론적 타당성 추정 모델 결과 (실제 시선 추적이 아닌 자세 기반 참고 수치)
     */
    data class FeasibilityEstimate(
        val baselineUsageMinutes: Int,
        val estimatedViewingMinutes: Int,
        val poseConfidencePercent: Int,
        val theoreticalBatteryCostPercentPerHour: Float,
        val targetDeviceProfile: String,
        val isFoldableUnfolded: Boolean = false,
        val isMultiWindow: Boolean = false,
        val disclaimer: String = "본 수치는 카메라 시선 추적이 아닌 기기 자세 센서 기반 이론적 추정 모델입니다."
    ) : ViewingTimeStatus()

    /**
     * 측정이 정상 수행되었으나 오늘의 누적 화면 사용 시간이 0분인 상태
     */
    data class MeasuredZero(
        val message: String = "오늘 누적 화면 사용 시간 0분"
    ) : ViewingTimeStatus()

    /**
     * UsageStats 조회 중 오류가 발생하였거나 데이터를 수집할 수 없는 상태
     */
    data class QueryError(
        val errorDetail: String
    ) : ViewingTimeStatus()

    /**
     * 사용 정보 접근 권한이 없는 상태
     */
    object PermissionDenied : ViewingTimeStatus()

    /**
     * 자세 및 근접 센서를 사용할 수 없거나 데이터가 오래됨(stale)
     */
    data class SensorUnavailable(
        val reason: String = "센서 데이터 수신 불가"
    ) : ViewingTimeStatus()

    /**
     * 앱이 백그라운드 상태이거나 화면이 꺼진 상태
     */
    data class BackgroundPaused(
        val reason: String = "앱 백그라운드 또는 화면 꺼짐"
    ) : ViewingTimeStatus()

    /**
     * 폴더블 펼침/접힘 전환 또는 화면 분할 상태 변경 중
     */
    data class ConfigurationChanging(
        val reason: String
    ) : ViewingTimeStatus()
}

/**
 * Target Device Configuration Profiles for Feasibility Benchmarking
 */
enum class TargetDeviceModel(val deviceName: String, val isFoldable: Boolean) {
    GALAXY_S23("Samsung Galaxy S23 (SM-S911)", false),
    GALAXY_Z_FOLD7("Samsung Galaxy Z Fold7 (SM-F966)", true),
    GENERIC_ANDROID_13("Android 13+ Device", false);

    companion object {
        fun detectDevice(modelName: String? = try { Build.MODEL } catch (_: Throwable) { null }): TargetDeviceModel {
            val safeModel = modelName ?: "Generic"
            return when {
                safeModel.contains("S911", ignoreCase = true) || safeModel.contains("S23", ignoreCase = true) -> GALAXY_S23
                safeModel.contains("F966", ignoreCase = true) || safeModel.contains("Fold7", ignoreCase = true) -> GALAXY_Z_FOLD7
                else -> GENERIC_ANDROID_13
            }
        }
    }
}

/**
 * 실제 시청시간 평가 및 타당성 분석기 (Android 13+ / API 33+ Target)
 *
 * 주의: 임의의 보정치와 고정 배터리 상수는 실측값이 아닌 이론적 추정치이며,
 * 사용자 UI에는 실측값으로 왜곡 노출되지 않도록 안전 캡슐화되어 있습니다.
 */
object ViewingTimeEvaluator {

    /**
     * 운영(Production) 환경용 시청시간 타당성 평가 함수
     */
    fun evaluateViewingTime(
        context: Context,
        isAppInForeground: Boolean,
        isScreenOn: Boolean = true,
        poseDetector: SmartDevicePoseDetector? = null,
        isMultiWindow: Boolean = false,
        isConfigChanging: Boolean = false,
        screenWidthDp: Int = 360,
        screenHeightDp: Int = 800
    ): ViewingTimeStatus {
        val isSensorFresh = poseDetector != null && poseDetector.hasRegisteredSensors && poseDetector.isFresh
        return evaluateViewingTimeInternal(
            context = context,
            hasPermission = ScreenTimeManager.hasUsageStatsPermission(context),
            isAppInForeground = isAppInForeground,
            isScreenOn = isScreenOn,
            poseDetector = poseDetector,
            isSensorFresh = isSensorFresh,
            isMultiWindow = isMultiWindow,
            isConfigChanging = isConfigChanging,
            mockQueryResult = null,
            screenWidthDp = screenWidthDp,
            screenHeightDp = screenHeightDp
        )
    }

    /**
     * 테스트 및 내부 시뮬레이션 전용 평가 함수
     */
    internal fun evaluateViewingTimeInternal(
        context: Context,
        hasPermission: Boolean,
        isAppInForeground: Boolean,
        isScreenOn: Boolean = true,
        poseDetector: SmartDevicePoseDetector? = null,
        isSensorFresh: Boolean = true,
        isMultiWindow: Boolean = false,
        isConfigChanging: Boolean = false,
        mockQueryResult: ScreenTimeQueryResult? = null,
        screenWidthDp: Int = 360,
        screenHeightDp: Int = 800
    ): ViewingTimeStatus {
        // 1. 권한 거부 상태 분기
        if (!hasPermission) {
            return ViewingTimeStatus.PermissionDenied
        }

        // 2. 백그라운드 전환 및 화면 꺼짐 분기
        if (!isAppInForeground || !isScreenOn) {
            return ViewingTimeStatus.BackgroundPaused(
                reason = if (!isAppInForeground) "앱 백그라운드 전환" else "화면 꺼짐"
            )
        }

        // 3. 폼팩터/회전/분할 화면 구성 변경 분기
        if (isConfigChanging) {
            return ViewingTimeStatus.ConfigurationChanging("화면 회전 또는 Fold 폼팩터 전환 중")
        }

        // 4. 사용시간 데이터 수집 (테스트 모크 또는 실제 ScreenTimeManager queryTodayScreenTime)
        val queryResult = mockQueryResult ?: ScreenTimeManager.queryTodayScreenTime(context)

        val usageMinutes = when (queryResult) {
            is ScreenTimeQueryResult.PermissionDenied -> return ViewingTimeStatus.PermissionDenied
            is ScreenTimeQueryResult.QueryError -> return ViewingTimeStatus.QueryError(queryResult.reason)
            is ScreenTimeQueryResult.Success -> queryResult.minutes
        }

        // 5. 비정상 음수 수치 검증
        if (usageMinutes < 0) {
            return ViewingTimeStatus.QueryError("비정상 음수 사용시간 데이터: $usageMinutes")
        }

        // 6. 조회 성공했으나 0분인 측정 상태 분기 (0분 사용 시 센서 보정 불필요)
        if (usageMinutes == 0) {
            return ViewingTimeStatus.MeasuredZero()
        }

        // 7. 센서 가용성 및 신선도(freshness) 검증
        if (!isSensorFresh) {
            return ViewingTimeStatus.SensorUnavailable(
                reason = if (poseDetector == null) "센서 미등록" else "센서 데이터 갱신 중단(Stale)"
            )
        }

        // 8. 기기 식별 및 Z Fold7 펼침/접힘 폼팩터 판별
        val targetDevice = TargetDeviceModel.detectDevice()
        val isUnfolded = targetDevice.isFoldable && (screenWidthDp > 600 || screenHeightDp > 1000)
        val profileName = if (targetDevice.isFoldable) {
            "${targetDevice.deviceName} [${if (isUnfolded) "펼침(Inner)" else "접힘(Outer)"}]"
        } else {
            targetDevice.deviceName
        }

        // 9. 자세 센서 이론적 모델 보정치
        val currentPose = poseDetector?.currentPose ?: DevicePose.HAND_HELD_ACTIVE
        val poseConfidence = when (currentPose) {
            DevicePose.HAND_HELD_ACTIVE -> 95
            DevicePose.DEVICE_TILTED -> 85
            DevicePose.DEVICE_FLAT -> 50
            DevicePose.DEVICE_FACE_DOWN -> 0
        }

        val estimatedMinutes = (usageMinutes * (poseConfidence / 100f)).toInt()
        val theoreticalBatteryCost = 0.12f

        return ViewingTimeStatus.FeasibilityEstimate(
            baselineUsageMinutes = usageMinutes,
            estimatedViewingMinutes = estimatedMinutes,
            poseConfidencePercent = poseConfidence,
            theoreticalBatteryCostPercentPerHour = theoreticalBatteryCost,
            targetDeviceProfile = profileName,
            isFoldableUnfolded = isUnfolded,
            isMultiWindow = isMultiWindow
        )
    }
}
