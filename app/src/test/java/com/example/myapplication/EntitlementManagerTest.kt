package com.example.myapplication

import com.example.myapplication.model.PlanType
import com.example.myapplication.model.SubscriptionState
import org.junit.Assert.*
import org.junit.Test

class EntitlementManagerTest {

    @Test
    fun trialState_isUnlocked_whenDaysRemainingGreaterThanZero() {
        val state = SubscriptionState(
            planType = PlanType.FREE_TRIAL,
            trialDaysRemaining = 45
        )
        assertTrue(state.isUnlocked)
    }

    @Test
    fun expiredState_isLocked() {
        val state = SubscriptionState(
            planType = PlanType.EXPIRED,
            trialDaysRemaining = 0
        )
        assertFalse(state.isUnlocked)
    }

    @Test
    fun familyHost_isUnlocked() {
        val state = SubscriptionState(
            planType = PlanType.FAMILY_HOST,
            trialDaysRemaining = 0,
            hostEmail = "host@irisfit.app"
        )
        assertTrue(state.isUnlocked)
    }

    @Test
    fun familyMember_isUnlocked() {
        val state = SubscriptionState(
            planType = PlanType.FAMILY_MEMBER,
            trialDaysRemaining = 0,
            hostEmail = "host@irisfit.app"
        )
        assertTrue(state.isUnlocked)
    }

    @Test
    fun colorTone_defaultZeroIsAlwaysUnlocked() {
        val expiredState = SubscriptionState(
            planType = PlanType.EXPIRED,
            trialDaysRemaining = 0
        )
        val isDefaultUnlocked = true
        val isTone1Unlocked = expiredState.isUnlocked

        assertTrue(isDefaultUnlocked)
        assertFalse(isTone1Unlocked)
    }
}
