package com.example.myapplication

import com.example.myapplication.session.AppLocalization
import com.example.myapplication.session.CompletionRecord
import com.example.myapplication.session.CompletionRepository
import com.example.myapplication.session.CompletionStorage
import com.example.myapplication.session.EyeBatteryCalculator
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class IrisFitQaRegressionTest {
    @Test
    fun japaneseWarningMessageMustNotContainKorean() {
        val translated = AppLocalization.get("ja", "desc_warning")
        assertFalse("Japanese message contains Korean: $translated",
            Regex("[\\uAC00-\\uD7A3]").containsMatchIn(translated))
    }

    @Test
    fun localizedUsageDisclosureDoesNotMakeUnverifiedNetworkClaims() {
        val japanese = AppLocalization.get("ja", "usage_access_body")
        val spanish = AppLocalization.get("es", "usage_access_body")
        assertFalse(Regex("[\\uAC00-\\uD7A3]").containsMatchIn(japanese))
        assertFalse(Regex("[\\uAC00-\\uD7A3]").containsMatchIn(spanish))
        assertFalse(japanese.contains("外部サーバーに送信されません"))
        assertFalse(spanish.contains("nunca sale de tu dispositivo"))
    }

    @Test
    fun recoveryMustNotDecreaseWhenRestCountIsVeryLarge() {
        assertEquals(100,
            EyeBatteryCalculator.calculateBattery(0, Int.MAX_VALUE, 0).eyeHealthScore)
    }

    @Test
    fun duplicateSessionIdIsNotSavedAgainAfterRepositoryRecreation() {
        val storage = object : CompletionStorage {
            var value = ""
            override fun read(): String = value
            override fun write(value: String): Boolean {
                this.value = value
                return true
            }
        }
        val record = CompletionRecord(
            sessionId = UUID(0, 1).toString(),
            completedAtEpochMillis = 1L,
            completedLocalDate = "2026-11-14"
        )
        CompletionRepository(storage).save(record)
        val reopened = CompletionRepository(storage)
        reopened.save(record)
        assertEquals(listOf(record), reopened.records())
    }
}
