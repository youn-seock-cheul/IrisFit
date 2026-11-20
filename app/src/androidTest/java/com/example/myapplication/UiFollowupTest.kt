package com.example.myapplication

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.LanguageManager
import com.example.myapplication.session.ColorTonePreferences
import com.example.myapplication.session.CompletionRepository
import com.example.myapplication.session.EyeFatigueStatus
import com.example.myapplication.session.FileCompletionStorage
import com.example.myapplication.session.GuidedSessionViewModel
import com.example.myapplication.ui.theme.AppColorTone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiFollowupTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val model get() = ViewModelProvider(compose.activity)[GuidedSessionViewModel::class.java]
    private fun tr(key: String) = model.tr(key)

    @Test fun betaHomeHidesUnreleasedEntryPointsAndLabelsBatteryEstimate() {
        compose.waitUntil(5000) { model.recordsReady && !model.busy }
        compose.waitUntil(10_000) { model.screenTimeResult != null }
        val usageAccessGranted = com.example.myapplication.data.ScreenTimeManager
            .hasUsageStatsPermission(compose.activity)

        compose.runOnIdle {
            val result = model.screenTimeResult
            if (usageAccessGranted) {
                assertTrue("Granted Usage Access must produce a measured query result", result is com.example.myapplication.data.ScreenTimeQueryResult.Success)
            }
            if (result is com.example.myapplication.data.ScreenTimeQueryResult.Success) {
                assertTrue(model.eyeBatteryStats.screenTimeMeasured)
                assertTrue(model.eyeBatteryStats.eyeHealthScore in 0..100)
                assertNotEquals(EyeFatigueStatus.UNMEASURED, model.eyeBatteryStats.fatigueStatus)
            } else {
                assertFalse(model.eyeBatteryStats.screenTimeMeasured)
                assertEquals(-1, model.eyeBatteryStats.eyeHealthScore)
                assertEquals(EyeFatigueStatus.UNMEASURED, model.eyeBatteryStats.fatigueStatus)
            }
        }
        compose.onNodeWithText(tr("nav_ai")).assertDoesNotExist()
        compose.onNodeWithText(tr("cheer_card_title")).assertDoesNotExist()
        compose.onNodeWithText(tr("global_challenge_title")).assertDoesNotExist()
        compose.onNodeWithText(tr("home_title")).assertIsDisplayed()
        compose.onNodeWithText(tr("battery_card_name")).assertIsDisplayed()
        if (model.healthConnectLinked) {
            compose.onNodeWithTag("health_connect_linked").assertIsDisplayed()
        }
        listOf("legend_danger", "legend_warning", "legend_caution", "legend_optimal").forEach {
            compose.onNodeWithText(tr(it)).assertIsDisplayed()
        }
        compose.onNodeWithTag("home_rest_tile").performScrollTo().assertIsDisplayed()
        listOf(
            "battery_estimate_summary",
            "battery_estimate_details",
            "battery_formula_start",
            "battery_formula_use",
            "battery_formula_recovery"
        ).forEach { compose.onNodeWithText(tr(it)).assertDoesNotExist() }
        compose.onNodeWithTag("battery_card_launcher_icon").assertIsDisplayed()
        compose.onNodeWithTag("battery_card_mascot").assertDoesNotExist()
    }

    @Test fun supportedLocalesAndColorTonesKeepHomeUsable() {
        val context = compose.activity
        val originalLanguage = LanguageManager.getSelectedLanguage(context)
        val originalTone = model.colorTone
        val languages = listOf("ko", "en", "ja", "es")
        try {
            languages.forEach { language ->
                LanguageManager.setSelectedLanguage(context, language)
                compose.activityRule.scenario.recreate()
                compose.waitUntil(5000) { !model.colorToneBusy && model.recordsReady }

                val homeLabel = tr("nav_home")
                assertNotEquals("nav_home", homeLabel)
                compose.onNodeWithText(homeLabel).assertIsDisplayed()
                if (model.healthConnectLinked) {
                    compose.onNodeWithTag("health_connect_linked").assertIsDisplayed()
                } else {
                    compose.onNodeWithText(tr("battery_card_name")).assertIsDisplayed()
                }
                compose.onNodeWithTag("home_rest_tile").performScrollTo().assertIsDisplayed()
                compose.onNodeWithText(tr("battery_formula_start")).assertDoesNotExist()
                compose.onNodeWithText(tr("nav_ai")).assertDoesNotExist()

                AppColorTone.entries.forEach { tone ->
                    model.selectColorTone(tone)
                    compose.waitUntil(5000) { !model.colorToneBusy }
                    compose.runOnIdle { assertEquals(tone, model.colorTone) }
                    compose.onNodeWithText(homeLabel).assertIsDisplayed()
                }

            }
        } finally {
            LanguageManager.setSelectedLanguage(context, originalLanguage)
            model.selectColorTone(originalTone)
            compose.waitUntil(5000) { !model.colorToneBusy }
            compose.activityRule.scenario.recreate()
        }
    }

    @Test fun healthConnectIsOptionalAndOffByDefault() {
        compose.waitUntil(5000) { model.recordsReady && !model.busy }
        assertFalse(model.healthConnectEnabled)
        compose.onNodeWithTag("health_battery_summary").assertDoesNotExist()
        compose.onNodeWithTag("health_connect_linked").assertDoesNotExist()
        compose.onNodeWithText(tr("nav_settings")).performClick()
        compose.onNodeWithText(tr("health_connect_settings_title")).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(tr("health_connect_settings_desc")).performScrollTo().assertIsDisplayed()
        assertFalse(model.healthConnectEnabled)
    }

    @Test fun installedAppResolvesHealthPermissionRationale() {
        val context = compose.activity
        val intent = if (android.os.Build.VERSION.SDK_INT >= 34) {
            android.content.Intent(android.content.Intent.ACTION_VIEW_PERMISSION_USAGE)
                .addCategory("android.intent.category.HEALTH_PERMISSIONS")
        } else {
            android.content.Intent("androidx.health.ACTION_SHOW_PERMISSIONS_RATIONALE")
        }.setPackage(context.packageName)
        val info = requireNotNull(context.packageManager.resolveActivity(intent, 0)).activityInfo
        if (android.os.Build.VERSION.SDK_INT >= 34) {
            assertEquals(HealthConnectRationaleActivity::class.java.name, info.targetActivity)
            assertEquals("android.permission.START_VIEW_PERMISSION_USAGE", info.permission)
        } else {
            assertEquals(HealthConnectRationaleActivity::class.java.name, info.name)
        }
    }

    @Test fun eyeGymAndPursuitCompletionPersistOneRecordEach() {
        val context = compose.activity
        val storage = FileCompletionStorage(context)
        val originalLedger = storage.read()
        val originalRecords = CompletionRepository(storage).records()
        try {
            compose.waitUntil(5000) { model.recordsReady && !model.busy }

            compose.onNode(hasText(tr("nav_gym")) and isSelectable()).performClick()
            compose.onNodeWithText(tr("gym_focus_btn")).performScrollTo().performClick()
            compose.onNodeWithText(tr("gym_start")).assertIsDisplayed()
            compose.mainClock.autoAdvance = false
            compose.onNodeWithText(tr("gym_start"))
                .performSemanticsAction(SemanticsActions.OnClick) { it() }
            compose.mainClock.advanceTimeBy(21_000L)
            compose.waitUntil(10_000) { !model.busy && model.workoutSaved }
            compose.mainClock.autoAdvance = true

            var persisted = CompletionRepository(storage).records()
            assertEquals(originalRecords.size + 1, persisted.size)
            assertEquals(1, persisted.count { it.routineId == "eye_gym" })
            assertEquals("user_confirmed", persisted.last().confirmation)

            compose.onNodeWithText(tr("gym_home")).performClick()
            compose.onNodeWithText(tr("home_card_pursuit"))
                .performSemanticsAction(SemanticsActions.OnClick) { it() }
            compose.onNodeWithText(tr("pursuit_start")).assertIsDisplayed()
            compose.mainClock.autoAdvance = false
            compose.onNodeWithText(tr("pursuit_start"))
                .performSemanticsAction(SemanticsActions.OnClick) { it() }
            compose.mainClock.advanceTimeBy(21_000L)
            compose.waitUntil(10_000) { !model.busy && model.workoutSaved }
            compose.mainClock.autoAdvance = true

            persisted = CompletionRepository(storage).records()
            assertEquals(originalRecords.size + 2, persisted.size)
            assertEquals(originalRecords.count { it.routineId == "eye_gym" } + 1, persisted.count { it.routineId == "eye_gym" })
            assertEquals(originalRecords.count { it.routineId == "smooth_pursuit" } + 1, persisted.count { it.routineId == "smooth_pursuit" })
            assertTrue(persisted.takeLast(2).all { it.plannedDurationSeconds == 20 && it.confirmation == "user_confirmed" })
        } finally {
            compose.mainClock.autoAdvance = true
            storage.write(originalLedger)
            model.reloadRecords()
            compose.waitUntil(5000) { !model.busy }
        }
    }

    @Test fun colorChoiceIsPersistedAndAppliedAfterActivityRecreation() {
        val storage = FileCompletionStorage(compose.activity, "appearance_v1.txt")
        val original = storage.read()
        try {
            compose.waitUntil(5000) { !model.colorToneBusy }
            compose.onNodeWithText(tr("nav_settings")).performClick()
            compose.onNodeWithText(tr("settings_color_tone")).performScrollTo().performClick()
            AppColorTone.entries.forEach { tone ->
                compose.onNode(hasText(tr(tone.labelKey)) and isSelectable()).performClick()
                compose.waitUntil(5000) { !model.colorToneBusy }
                compose.runOnIdle { assertEquals(tone, model.colorTone) }
                assertEquals(tone, ColorTonePreferences(storage).read())
            }
            compose.onNode(hasText(tr("tone_blue")) and isSelectable()).performClick()
            compose.waitUntil(5000) { !model.colorToneBusy }
            compose.onNodeWithText(tr("btn_ok")).performClick()
            compose.activityRule.scenario.recreate()
            compose.waitUntil(5000) { !model.colorToneBusy }
            compose.runOnIdle { assertEquals(AppColorTone.BLUE, model.colorTone) }
            compose.onNode(hasText(tr("nav_settings")) and isSelectable()).assertIsSelected()
            compose.onNodeWithText(tr("tone_blue")).performScrollTo().assertIsDisplayed()
        } finally {
            storage.write(original)
        }
    }

    @Test fun uncheckedSummaryDoesNotInventResultsAndSelectedResponsesSurviveNavigation() {
        compose.onNode(hasText(tr("nav_gym")) and isSelectable()).performClick()
        compose.onNodeWithText(tr("gym_exam_btn")).performScrollTo().performClick()
        compose.onNodeWithText(tr("check_summary")).performClick()
        compose.onAllNodesWithText(tr("check_unanswered")).assertCountEquals(6)
        compose.onNodeWithText(tr("check_experience")).performScrollTo().performClick()
        compose.onNodeWithText(tr("check_lines")).performScrollTo().performClick()
        compose.onNodeWithText(tr("check_lines_equal")).performScrollTo().performClick()
        compose.onNodeWithText(tr("check_next")).performScrollTo().performClick()
        compose.onNodeWithText(tr("check_colors_red")).performScrollTo().performClick()
        compose.onNodeWithText(tr("check_number")).performScrollTo().performClick()
        compose.onNodeWithText("8").performScrollTo().performClick()
        compose.onNodeWithText(tr("check_show_summary")).performScrollTo().performClick()
        compose.onNodeWithText(tr("check_lines_equal")).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(tr("check_colors_red")).performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(tr("check_selected").format("8")).performScrollTo().assertIsDisplayed()
        compose.onAllNodesWithText(tr("check_unanswered")).assertCountEquals(3)
        compose.activityRule.scenario.recreate()
        compose.onAllNodesWithText(tr("check_unanswered")).assertCountEquals(3)
        compose.onNodeWithText(tr("check_restart")).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onNode(hasText(tr("check_summary")) and isSelectable()).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onAllNodesWithText(tr("check_unanswered")).assertCountEquals(6)
    }

    @Test fun pulseGuidanceDoesNotOverlapTarget() = assertGuidanceOutside("focus_mode_pulse")
    @Test fun flashGuidanceDoesNotOverlapTarget() = assertGuidanceOutside("focus_mode_flash")
    @Test fun waveGuidanceDoesNotOverlapTarget() = assertGuidanceOutside("focus_mode_wave")
    @Test fun dualGuidanceDoesNotOverlapTarget() = assertGuidanceOutside("focus_mode_dual")

    private fun assertGuidanceOutside(mode: String) {
        compose.onNode(hasText(tr("nav_gym")) and isSelectable()).performClick()
        compose.onNodeWithText(tr("gym_focus_btn")).performScrollTo().performClick()
        compose.waitUntil(5000) { model.recordsReady && !model.busy }
        compose.onNodeWithText(tr(mode)).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText(tr("gym_start")).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.mainClock.advanceTimeBy(2500)
        val target = compose.onNodeWithTag("focus_target").fetchSemanticsNode().boundsInRoot
        val guidance = compose.onNodeWithTag("focus_guidance").fetchSemanticsNode().boundsInRoot
        assertTrue("Guidance must be below the entire target area", guidance.top > target.bottom)
        compose.onNodeWithText(tr("gym_stop")).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText(tr("gym_start")).assertIsDisplayed()
        compose.onNodeWithText(tr("gym_start")).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithTag("focus_target").assertIsDisplayed()
        compose.onNodeWithText(tr("gym_stop")).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.autoAdvance = true
    }

    @Test fun pursuitCanRestartAfterStopAndUsesSatelliteOrbitCanvas() {
        compose.waitUntil(5000) { model.recordsReady && !model.busy }
        compose.onNodeWithText(tr("home_card_pursuit")).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.onNodeWithText(tr("pursuit_start")).assertIsDisplayed()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText(tr("pursuit_start")).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.mainClock.advanceTimeBy(200)
        compose.onNodeWithTag("satellite_orbit_canvas").assertIsDisplayed()
        val planetKeys = listOf(
            "pursuit_planet_earth",
            "pursuit_planet_jupiter",
            "pursuit_planet_saturn",
            "pursuit_planet_venus"
        )
        assertTrue(planetKeys.any { key ->
            compose.onAllNodesWithText(tr(key)).fetchSemanticsNodes().isNotEmpty()
        })
        compose.onNodeWithText(tr("gym_stop")).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.mainClock.advanceTimeByFrame()
        compose.onNodeWithText(tr("pursuit_start")).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.mainClock.advanceTimeBy(200)
        compose.onNodeWithTag("satellite_orbit_canvas").assertIsDisplayed()
        compose.onNodeWithText(tr("gym_stop")).performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.mainClock.advanceTimeByFrame()
        compose.mainClock.autoAdvance = true
    }

}
