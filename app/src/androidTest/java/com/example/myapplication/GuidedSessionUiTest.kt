package com.example.myapplication

import android.app.KeyguardManager
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.session.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GuidedSessionUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private var originalAwake: String? = null

    private fun shell(command: String): String {
        val descriptor = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(descriptor).bufferedReader().use { it.readText().trim() }
    }

    @Before fun keepScreenOnForThisShortObservation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertFalse("Unlock the phone before this short UI check",
            context.getSystemService(KeyguardManager::class.java).isKeyguardLocked)
        originalAwake = shell("settings get global stay_on_while_plugged_in")
        shell("svc power stayon usb")
    }

    @After fun restoreDeviceSetting() {
        originalAwake?.let {
            if (it == "null") shell("settings delete global stay_on_while_plugged_in")
            else {
                check(it.matches(Regex("\\d+")))
                shell("settings put global stay_on_while_plugged_in $it")
            }
        }
    }

    private fun click(text: String) {
        val node = compose.onNodeWithText(text)
        val nonScrollableActions = setOf(
            model().tr("btn_delete"),
            model().tr("session_confirm_btn"),
            model().tr("session_resume"),
            model().tr("session_stop")
        )
        if (text !in nonScrollableActions) {
            node.performScrollTo()
        }
        node.performClick()
    }
    private fun clickRestTile() {
        compose.onNodeWithTag("home_rest_tile").performScrollTo().performClick()
    }
    private fun model() = ViewModelProvider(compose.activity)[GuidedSessionViewModel::class.java]
    private fun reset() {
        compose.waitUntil(5000) { !model().busy }
        compose.runOnIdle { model().home() }
        compose.onNodeWithText(model().tr("nav_settings")).performClick()
        click(model().tr("settings_delete_btn"))
        click(model().tr("btn_delete"))
        compose.waitUntil(5000) { !model().busy && model().recordsReady }
        compose.onNodeWithText(model().tr("nav_home")).performClick()
    }

    @Test fun thirtySecondCompletionRequiresConfirmationPersistsAndDeletes() {
        reset()
        clickRestTile()
        compose.waitUntil(5000) { model().state.phase == SessionPhase.RUNNING }
        compose.waitUntil(40_000) { model().state.phase == SessionPhase.CONFIRM }
        compose.runOnIdle { repeat(3) { model().tick() } }
        assertTrue(model().records.isEmpty())
        click(model().tr("session_confirm_btn"))
        compose.waitUntil(5000) { model().state.phase == SessionPhase.COMPLETED }
        val storage = FileCompletionStorage(InstrumentationRegistry.getInstrumentation().targetContext)
        assertEquals(1, CompletionRepository(storage).records().size)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText(model().tr("session_completed_title")).assertIsDisplayed()
        reset()
        assertTrue(CompletionRepository(storage).records().isEmpty())
    }

    @Test fun rotationKeepsSessionAndBackgroundRequiresManualResume() {
        reset()
        clickRestTile()
        compose.waitUntil(5000) { model().state.phase == SessionPhase.RUNNING }
        val id = model().state.sessionId
        compose.activityRule.scenario.recreate()
        compose.waitUntil(5000) { model().state.phase == SessionPhase.RUNNING }
        assertEquals(id, model().state.sessionId)
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        assertEquals(SessionPhase.PAUSED, model().state.phase)
        val paused = model().state.elapsedMs
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        compose.waitForIdle()
        assertEquals(SessionPhase.PAUSED, model().state.phase)
        assertEquals(paused, model().state.elapsedMs)
        click(model().tr("session_resume"))
        click(model().tr("session_stop"))
        compose.onNodeWithText(model().tr("session_ended_title")).assertIsDisplayed()
        assertTrue(model().records.isEmpty())
        reset()
    }
}
