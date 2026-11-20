package com.example.myapplication

import com.example.myapplication.data.FamilySubscriptionManager
import com.example.myapplication.data.model.GroupActionResult
import com.example.myapplication.data.model.PlanType
import com.example.myapplication.data.model.SubscriptionStatus
import org.junit.Assert.*
import org.junit.Test

/**
 * 넷플릭스형 1:N 공유 구독 (FamilySubscriptionManager) QA 단위 테스트
 */
class FamilySubscriptionManagerTest {

    @Test
    fun testGenerate6DigitCode_formatAndCharSet() {
        val code = FamilySubscriptionManager.generate6DigitCode()

        assertEquals("초대 코드는 반드시 6자리여야 합니다.", 6, code.length)
        assertTrue("초대 코드는 대문자 알파벳과 숫자로만 구성되어야 합니다.", code.matches(Regex("^[A-Z0-9]{6}$")))
        assertFalse("오인하기 쉬운 문자(I, O, 0, 1)가 포함되어서는 안 됩니다.", code.contains("I") || code.contains("O") || code.contains("0") || code.contains("1"))
    }

    @Test
    fun testSubscriptionStatus_canAccessProFeatures() {
        val freeStatus = SubscriptionStatus(planType = PlanType.FREE, isProUnlocked = false)
        assertFalse(freeStatus.canAccessProFeatures)

        val hostStatus = SubscriptionStatus(planType = PlanType.HOST, isProUnlocked = true)
        assertTrue(hostStatus.canAccessProFeatures)

        val memberStatus = SubscriptionStatus(planType = PlanType.FAMILY_MEMBER, isProUnlocked = true)
        assertTrue(memberStatus.canAccessProFeatures)

        val trialStatus = SubscriptionStatus(planType = PlanType.TRIAL, isTrialActive = true)
        assertTrue(trialStatus.canAccessProFeatures)
    }

    @Test
    fun testSubscriptionStatus_isFull_whenCurrentMembersReachesMax() {
        val notFull = SubscriptionStatus(maxMembers = 4, currentMembers = 3)
        assertFalse(notFull.isFull)

        val full = SubscriptionStatus(maxMembers = 4, currentMembers = 4)
        assertTrue(full.isFull)
    }

    @Test
    fun testGroupActionResult_types() {
        val success = GroupActionResult.Success(SubscriptionStatus(), "Success message")
        assertEquals("Success message", success.message)

        val error = GroupActionResult.Error("Error message")
        assertEquals("Error message", error.errorMessage)
    }
}
