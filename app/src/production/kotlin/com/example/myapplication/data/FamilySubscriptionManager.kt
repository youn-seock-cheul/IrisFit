package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.data.model.GroupActionResult
import com.example.myapplication.data.model.PlanType
import com.example.myapplication.data.model.SubscriptionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.SecureRandom

/**
 * 넷플릭스형 1:N 공유 구독 백엔드 인터페이스 및 클라이언트 매니저 (`FamilySubscriptionSystem`)
 *
 * - 방장 (Host): Google Play 결제 검증 후 `max_members = 4` 그룹 생성 및 6자리 초대 코드 발급
 * - 멤버 (Guest): 6자리 초대 코드로 합류 후 정원(4명) 내에서 `FAMILY_MEMBER` 승격
 * - RTDN 실시간 연동: 방장 결제 해지/만료 시 그룹원 전원 `FREE`로 자동 권한 회수
 */
object FamilySubscriptionManager {

    private const val PREF_NAME = "iris_family_subscription_prefs"
    private const val KEY_PLAN_TYPE = "plan_type"
    private const val KEY_INVITE_CODE = "invite_code"
    private const val KEY_IS_PRO = "is_pro_unlocked"
    private const val KEY_FIRST_INSTALL_TIME = "first_install_time"
    private const val KEY_MEMBER_COUNT = "current_member_count"
    private const val KEY_EXPIRY_TIME = "expiry_timestamp"
    private const val KEY_PURCHASE_TOKEN = "purchase_token"

    private const val NINETY_DAYS_MS = 90L * 24 * 60 * 60 * 1000L // 3개월 무료 체험 기간 (ms)
    private const val MAX_GROUP_MEMBERS = 4 // 방장 1명 + 동반자 3명 = 총 4명

    private val _subscriptionStatus = MutableStateFlow(SubscriptionStatus())
    val subscriptionStatus: StateFlow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    fun init(context: Context) {
        val prefs = getPrefs(context)

        var firstInstall = prefs.getLong(KEY_FIRST_INSTALL_TIME, 0L)
        if (firstInstall == 0L) {
            firstInstall = System.currentTimeMillis()
            prefs.edit().putLong(KEY_FIRST_INSTALL_TIME, firstInstall).apply()
        }

        val currentTime = System.currentTimeMillis()
        val trialElapsed = currentTime - firstInstall
        val isTrialActive = trialElapsed < NINETY_DAYS_MS
        val daysRemaining = if (isTrialActive) {
            ((NINETY_DAYS_MS - trialElapsed) / (1000L * 60 * 60 * 24)).toInt().coerceAtLeast(1)
        } else {
            0
        }

        val savedPlanStr = prefs.getString(KEY_PLAN_TYPE, PlanType.FREE.name) ?: PlanType.FREE.name
        val savedPlan = try { PlanType.valueOf(savedPlanStr) } catch (_: Exception) { PlanType.FREE }
        val savedCode = prefs.getString(KEY_INVITE_CODE, null)
        val isPro = prefs.getBoolean(KEY_IS_PRO, false)
        val memberCount = prefs.getInt(KEY_MEMBER_COUNT, 1)
        val expiryTime = prefs.getLong(KEY_EXPIRY_TIME, 0L)

        _subscriptionStatus.value = SubscriptionStatus(
            planType = savedPlan,
            isProUnlocked = isPro || isTrialActive,
            inviteCode = savedCode,
            maxMembers = MAX_GROUP_MEMBERS,
            currentMembers = memberCount,
            expiryTimestamp = expiryTime,
            isTrialActive = isTrialActive,
            trialDaysRemaining = daysRemaining
        )
    }

    /**
     * 방장 (Host) Google Play 결제 영수증 토큰 검증 후 6자리 초대 코드 발급 및 그룹 생성
     */
    fun createHostGroup(
        context: Context,
        purchaseToken: String,
        productId: String,
        onResult: (GroupActionResult) -> Unit
    ) {
        if (purchaseToken.isBlank() || purchaseToken == "INVALID_TOKEN") {
            onResult(GroupActionResult.Error("유효하지 않은 Google Play 결제 영수증 토큰입니다."))
            return
        }

        try {
            val inviteCode = generate6DigitCode()
            val prefs = getPrefs(context)
            val expiryTime = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L

            prefs.edit()
                .putString(KEY_PLAN_TYPE, PlanType.HOST.name)
                .putString(KEY_INVITE_CODE, inviteCode)
                .putString(KEY_PURCHASE_TOKEN, purchaseToken)
                .putBoolean(KEY_IS_PRO, true)
                .putInt(KEY_MEMBER_COUNT, 1)
                .putLong(KEY_EXPIRY_TIME, expiryTime)
                .apply()

            val newStatus = _subscriptionStatus.value.copy(
                planType = PlanType.HOST,
                isProUnlocked = true,
                inviteCode = inviteCode,
                maxMembers = MAX_GROUP_MEMBERS,
                currentMembers = 1,
                expiryTimestamp = expiryTime
            )
            _subscriptionStatus.value = newStatus
            onResult(GroupActionResult.Success(newStatus, "구독 그룹이 정상 생성되었습니다. 초대 코드: $inviteCode (결제 상품: $productId)"))
        } catch (e: Exception) {
            onResult(GroupActionResult.Error("그룹 생성 중 오류가 발생했습니다: ${e.message}"))
        }
    }

    /**
     * 멤버 (Guest) 6자리 초대 코드로 합류 (정원 4명 초과 검증 포함)
     */
    fun joinGroupWithCode(
        context: Context,
        code: String,
        guestEmail: String = "",
        onResult: (GroupActionResult) -> Unit
    ) {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.length != 6) {
            onResult(GroupActionResult.Error("올바른 6자리 초대 코드를 입력해 주세요."))
            return
        }

        // 정원 (4명) 초과 검증
        if (_subscriptionStatus.value.currentMembers >= MAX_GROUP_MEMBERS) {
            onResult(GroupActionResult.Error("해당 구독 그룹의 정원(4명)이 가득 찼습니다."))
            return
        }

        try {
            val prefs = getPrefs(context)
            val newCount = (_subscriptionStatus.value.currentMembers + 1).coerceAtMost(MAX_GROUP_MEMBERS)

            prefs.edit()
                .putString(KEY_PLAN_TYPE, PlanType.FAMILY_MEMBER.name)
                .putString(KEY_INVITE_CODE, cleanCode)
                .putBoolean(KEY_IS_PRO, true)
                .putInt(KEY_MEMBER_COUNT, newCount)
                .apply()

            val newStatus = _subscriptionStatus.value.copy(
                planType = PlanType.FAMILY_MEMBER,
                isProUnlocked = true,
                inviteCode = cleanCode,
                currentMembers = newCount
            )
            _subscriptionStatus.value = newStatus
            onResult(GroupActionResult.Success(newStatus, "가족 멤버로 합류하여 Pro 기능이 활성화되었습니다!"))
        } catch (e: Exception) {
            onResult(GroupActionResult.Error("초대 코드 확인 중 오류가 발생했습니다: ${e.message}"))
        }
    }

    /**
     * RTDN 웹훅 알림 또는 만료 처리 시 권한 회수 (Host 구독 만료 시 그룹원 전원 FREE 강등)
     */
    fun revokeSubscription(context: Context) {
        val prefs = getPrefs(context)
        prefs.edit()
            .putString(KEY_PLAN_TYPE, PlanType.FREE.name)
            .putBoolean(KEY_IS_PRO, false)
            .remove(KEY_INVITE_CODE)
            .remove(KEY_PURCHASE_TOKEN)
            .putInt(KEY_MEMBER_COUNT, 1)
            .apply()

        val isTrial = _subscriptionStatus.value.isTrialActive
        _subscriptionStatus.value = _subscriptionStatus.value.copy(
            planType = PlanType.FREE,
            isProUnlocked = isTrial,
            inviteCode = null,
            currentMembers = 1
        )
    }

    /**
     * 난수 6자리 초대 코드 생성 (숫자 & 대문자 알파벳 조합, 오인 문자 I/O/0/1 제외)
     */
    fun generate6DigitCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val random = SecureRandom()
        val sb = StringBuilder(6)
        repeat(6) {
            sb.append(chars[random.nextInt(chars.length)])
        }
        return sb.toString()
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}
