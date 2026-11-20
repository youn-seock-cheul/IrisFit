package com.example.myapplication.data.model

/**
 * 넷플릭스형 1:N 공유 구독 서비스 플랜 타입
 */
enum class PlanType {
    /** 무료 플랜 (기본 기능만 사용 가능) */
    FREE,

    /** 설치 3개월 무료 체험 플랜 */
    TRIAL,

    /** 방장 (Host) - Google Play 구독 주 결제자 (최대 4명 그룹 생성) */
    HOST,

    /** 멤버 (Guest) - 방장의 6자리 초대 코드로 합류한 공유 구독자 */
    FAMILY_MEMBER
}

/**
 * 넷플릭스형 1:N 공유 구독 전체 상태 모델
 */
data class SubscriptionStatus(
    val planType: PlanType = PlanType.FREE,
    val isProUnlocked: Boolean = false,
    val inviteCode: String? = null,
    val maxMembers: Int = 4,
    val currentMembers: Int = 1,
    val memberEmails: List<String> = emptyList(),
    val expiryTimestamp: Long = 0L,
    val isTrialActive: Boolean = false,
    val trialDaysRemaining: Int = 0
) {
    /** Pro 전용 기능 (원근 조절, 시선 추적, 헬스 연동, 색톤 4종) 접근 허용 여부 */
    val canAccessProFeatures: Boolean
        get() = isProUnlocked || isTrialActive || planType == PlanType.HOST || planType == PlanType.FAMILY_MEMBER

    /** 그룹 정원 (4명) 초과 여부 */
    val isFull: Boolean
        get() = currentMembers >= maxMembers
}

/**
 * 백엔드 서버 그룹 처리 결과
 */
sealed class GroupActionResult {
    data class Success(val status: SubscriptionStatus, val message: String) : GroupActionResult()
    data class Error(val errorMessage: String) : GroupActionResult()
}
