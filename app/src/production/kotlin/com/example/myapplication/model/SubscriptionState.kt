package com.example.myapplication.model

enum class PlanType {
    FREE,
    FREE_TRIAL,
    FAMILY_HOST,
    FAMILY_MEMBER,
    EXPIRED
}

data class SubscriptionState(
    val planType: PlanType = PlanType.FREE_TRIAL,
    val installTimestamp: Long = System.currentTimeMillis(),
    val trialDaysRemaining: Int = 90,
    val hostEmail: String? = null,
    val memberEmails: List<String> = emptyList(),
    val inviteCode: String? = null
) {
    val isUnlocked: Boolean
        get() = planType == PlanType.FAMILY_HOST ||
                planType == PlanType.FAMILY_MEMBER ||
                (planType == PlanType.FREE_TRIAL && trialDaysRemaining > 0)
}
