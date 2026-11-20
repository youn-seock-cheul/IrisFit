package com.example.myapplication.billing

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.model.PlanType
import com.example.myapplication.model.SubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class EntitlementManager(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _subscriptionState = MutableStateFlow(computeInitialState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    init {
        refreshState()
    }

    private fun getInstallTimestamp(): Long {
        val savedTime = prefs.getLong(KEY_FIRST_INSTALL_TIME, 0L)
        if (savedTime > 0L) return savedTime

        val installTime = try {
            context.packageManager.getPackageInfo(context.packageName, 0).firstInstallTime
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
        prefs.edit().putLong(KEY_FIRST_INSTALL_TIME, installTime).apply()
        return installTime
    }

    private fun computeInitialState(): SubscriptionState {
        val installTime = getInstallTimestamp()
        val elapsedMillis = System.currentTimeMillis() - installTime
        val elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMillis).toInt()
        val daysRemaining = (TRIAL_DURATION_DAYS - elapsedDays).coerceAtLeast(0)

        val isHost = prefs.getBoolean(KEY_IS_HOST, false)
        val isMember = prefs.getBoolean(KEY_IS_MEMBER, false)
        val isRevoked = prefs.getBoolean(KEY_IS_REVOKED, false)

        val planType = when {
            isRevoked -> PlanType.FREE
            isHost -> PlanType.FAMILY_HOST
            isMember -> PlanType.FAMILY_MEMBER
            daysRemaining > 0 -> PlanType.FREE_TRIAL
            else -> PlanType.EXPIRED
        }

        return SubscriptionState(
            planType = planType,
            installTimestamp = installTime,
            trialDaysRemaining = daysRemaining,
            hostEmail = prefs.getString(KEY_HOST_EMAIL, null),
            inviteCode = prefs.getString(KEY_INVITE_CODE, null)
        )
    }

    fun refreshState() {
        _subscriptionState.value = computeInitialState()
    }

    fun updateFamilySubscriptionStatus(
        isHost: Boolean = false,
        isMember: Boolean = false,
        hostEmail: String? = null,
        inviteCode: String? = null,
        isRevoked: Boolean = false
    ) {
        prefs.edit()
            .putBoolean(KEY_IS_HOST, isHost)
            .putBoolean(KEY_IS_MEMBER, isMember)
            .putBoolean(KEY_IS_REVOKED, isRevoked)
            .putString(KEY_HOST_EMAIL, hostEmail)
            .putString(KEY_INVITE_CODE, inviteCode)
            .apply()
        refreshState()
    }

    /**
     * RTDN / 방장 구독 만료 시 그룹 전체 멤버 권한 FREE로 즉시 자동 회수
     */
    fun revokeFamilySubscription() {
        updateFamilySubscriptionStatus(isRevoked = true)
    }

    fun updateSubscriptionStatus(isSubscribed: Boolean, isCompanion: Boolean = false, primaryEmail: String? = null, ownerEmail: String? = null) {
        updateFamilySubscriptionStatus(
            isHost = isSubscribed,
            isMember = isCompanion,
            hostEmail = ownerEmail ?: primaryEmail
        )
    }

    // --- Feature Gating Functions ---

    fun isFocusShiftUnlocked(): Boolean = _subscriptionState.value.isUnlocked

    fun isSmoothPursuitUnlocked(): Boolean = _subscriptionState.value.isUnlocked

    fun isHealthConnectUnlocked(): Boolean = _subscriptionState.value.isUnlocked

    fun isColorToneUnlocked(toneIndex: Int): Boolean {
        return if (toneIndex == 0) true else _subscriptionState.value.isUnlocked
    }

    companion object {
        private const val PREFS_NAME = "irisfit_subscription_prefs"
        private const val KEY_FIRST_INSTALL_TIME = "first_install_time"
        private const val KEY_IS_HOST = "is_family_host"
        private const val KEY_IS_MEMBER = "is_family_member"
        private const val KEY_IS_REVOKED = "is_revoked"
        private const val KEY_HOST_EMAIL = "host_email"
        private const val KEY_INVITE_CODE = "invite_code"
        const val TRIAL_DURATION_DAYS = 90
    }
}
