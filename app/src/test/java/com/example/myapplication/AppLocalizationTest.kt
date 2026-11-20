package com.example.myapplication

import com.example.myapplication.session.AppLocalization
import com.example.myapplication.session.EyeFatigueStatus
import com.example.myapplication.ui.theme.AppColorTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppLocalizationTest {
    private val locales = mapOf(
        "ko" to AppLocalization.stringsKo, "en" to AppLocalization.stringsEn,
        "ja" to AppLocalization.stringsJa, "es" to AppLocalization.stringsEs
    )

    @Test fun allSupportedLanguagesHaveEveryKeyAndMatchingFormatArguments() {
        val format = Regex("%[ds]")
        locales.forEach { (lang, strings) ->
            assertEquals(lang, AppLocalization.stringsKo.keys, strings.keys)
            strings.forEach { (key, value) ->
                assertTrue("$lang:$key", value.isNotBlank())
                assertEquals("$lang:$key", value, AppLocalization.get(lang, key))
                assertEquals("$lang:$key",
                    format.findAll(AppLocalization.stringsKo.getValue(key)).map { it.value }.toList(),
                    format.findAll(value).map { it.value }.toList())
                if (lang != "ko") assertFalse("$lang:$key", Regex("[\\uAC00-\\uD7A3]").containsMatchIn(value))
            }
        }
    }

    @Test fun dynamicStatusAndToneKeysResolveAndStreakIsFormattedFromData() {
        locales.forEach { (lang, strings) ->
            (EyeFatigueStatus.entries.flatMap { listOf(it.labelKey, it.descKey) } +
                AppColorTone.entries.map { it.labelKey }).forEach { key ->
                assertTrue("$lang:$key", strings.containsKey(key))
            }
            assertTrue(AppLocalization.get(lang, "goal_streak_progress").format(0).startsWith("0"))
            assertTrue(AppLocalization.get(lang, "goal_streak_progress").format(12).startsWith("12"))
            assertFalse(AppLocalization.get(lang, "global_challenge_body").contains("3,420"))
        }
    }

    @Test fun healthCopyUsesPlainLanguageWithoutTheEmptyRecordsNotice() {
        val shortTips = mapOf(
            "ko" to "휴식 습관이 좋습니다.",
            "en" to "Your rest habits look good.",
            "ja" to "休憩習慣は良好です。",
            "es" to "Tus hábitos de descanso van bien."
        )
        locales.forEach { (lang, strings) ->
            assertEquals(lang, shortTips.getValue(lang), strings.getValue("desc_optimal"))
            assertFalse(lang, strings.containsKey("health_data_no_records"))
            strings.forEach { (key, value) ->
                assertFalse("$lang:$key", value.contains("Health Connect", ignoreCase = true))
            }
            listOf("health_data_read_failed", "health_permission_denied").forEach { key ->
                assertTrue("$lang:$key", strings.getValue(key).isNotBlank())
            }
        }
    }
}
