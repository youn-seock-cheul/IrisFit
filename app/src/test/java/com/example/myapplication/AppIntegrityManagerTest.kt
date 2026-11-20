package com.example.myapplication

import com.example.myapplication.data.AppIntegrityManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppIntegrityManagerTest {

    @Test
    fun testPlayStorePackageConstant() {
        assertEquals("com.android.vending", AppIntegrityManager.PLAY_STORE_PACKAGE)
    }

    @Test
    fun testIntegrityStatusVerifiedSealedClass() {
        val status: AppIntegrityManager.IntegrityStatus = AppIntegrityManager.IntegrityStatus.Verified
        assertTrue(status is AppIntegrityManager.IntegrityStatus.Verified)
    }

    @Test
    fun testIntegrityStatusUnlicensedSealedClass() {
        val status: AppIntegrityManager.IntegrityStatus = AppIntegrityManager.IntegrityStatus.Unlicensed("Test reason")
        assertTrue(status is AppIntegrityManager.IntegrityStatus.Unlicensed)
        assertEquals("Test reason", (status as AppIntegrityManager.IntegrityStatus.Unlicensed).reason)
    }
}
