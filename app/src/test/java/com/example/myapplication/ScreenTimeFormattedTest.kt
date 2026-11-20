package com.example.myapplication

import org.junit.Assert.assertEquals
import org.junit.Test
import com.example.myapplication.data.ScreenTimeManager
import com.example.myapplication.data.ScreenTimeQueryResult
import com.example.myapplication.session.AppLocalization

class ScreenTimeFormattedTest {

    @Test
    fun testScreenTimeFormatting_HoursAndMinutes() {
        val formatted = ScreenTimeManager.formatScreenTime(ScreenTimeQueryResult.Success(135), "ko")

        assertEquals("2시간 15분", formatted)
    }

    @Test
    fun testScreenTimeFormatting_OnlyMinutes() {
        val formatted = ScreenTimeManager.formatScreenTime(ScreenTimeQueryResult.Success(45), "ko")

        assertEquals("45분", formatted)
    }

    @Test
    fun testScreenTimeFormatting_ZeroMinutes() {
        val formatted = ScreenTimeManager.formatScreenTime(ScreenTimeQueryResult.Success(0), "ko")

        assertEquals("0분", formatted)
    }

    @Test fun allLocalesFormatTimeAndDoNotTurnMissingDataIntoZeroMinutes() {
        mapOf("en" to "2 h 15 min", "ja" to "2時間15分", "es" to "2 h 15 min").forEach { (lang, expected) ->
            assertEquals(expected, ScreenTimeManager.formatScreenTime(ScreenTimeQueryResult.Success(135), lang))
        }
        listOf("ko", "en", "ja", "es").forEach { lang ->
            listOf(ScreenTimeQueryResult.PermissionDenied, ScreenTimeQueryResult.QueryError("test")).forEach { result ->
                assertEquals(AppLocalization.get(lang, "not_measured"), ScreenTimeManager.formatScreenTime(result, lang))
            }
        }
    }
}
