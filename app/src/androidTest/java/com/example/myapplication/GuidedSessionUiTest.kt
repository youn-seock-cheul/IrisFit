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
        if (text != "전체 삭제") node.performScrollTo()
        node.performClick()
    }
    private fun model() = ViewModelProvider(compose.activity)[GuidedSessionViewModel::class.java]
    private fun reset() {
        compose.waitUntil(5000) { !model().busy }
        compose.runOnIdle { model().home() }
        click("이 기기의 앱 기록 전체 삭제")
        click("전체 삭제")
        compose.waitUntil(5000) { !model().busy && model().recordsReady }
    }

    @Test fun thirtySecondCompletionRequiresConfirmationPersistsAndDeletes() {
        reset()
        click("30초 휴식 시작")
        assertEquals(EndSignalMode.SILENT, model().signalMode)
        click("소리와 진동")
        var signals = 0
        compose.runOnIdle { model().onEndSignal = { signals++ } }
        click("선택한 신호 미리 확인")
        assertEquals(1, signals)
        signals = 0
        click("안내 시작")
        compose.waitUntil(40_000) { model().state.phase == SessionPhase.CONFIRM }
        assertEquals(1, signals)
        compose.runOnIdle { repeat(3) { model().tick() } }
        assertEquals(1, signals)
        assertTrue(model().records.isEmpty())
        click("따라 했어요 — 완료로 남기기")
        compose.waitUntil(5000) { model().state.phase == SessionPhase.COMPLETED }
        val storage = FileCompletionStorage(InstrumentationRegistry.getInstrumentation().targetContext)
        assertEquals(1, CompletionRepository(storage).records().size)
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("휴식 안내 1회를 완료로 남겼어요.").performScrollTo().assertIsDisplayed()
        reset()
        assertTrue(CompletionRepository(storage).records().isEmpty())
    }

    @Test fun markerSettingsAreExplicitAndReloadable() {
        reset()
        click("마커 따라보기 체험 20초")
        compose.onNodeWithTag("marker_speed").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(1f) }
        compose.onNodeWithTag("marker_range").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(0f) }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val stored = MarkerPreferences(FileCompletionStorage(context, "marker_preferences_v1.txt"))
        assertNull(stored.read())
        click("이 설정 기억")
        compose.waitUntil(5000) { !model().busy && model().savedMarker != null }
        assertEquals(MarkerPreference(1, 0), stored.read())
        click("기본값으로 초기화")
        assertEquals(4, model().markerSpeedStep)
        click("저장한 설정 불러오기")
        assertEquals(1, model().markerSpeedStep)
        assertEquals(0, model().markerRangeStep)
        val fresh = compose.runOnIdle {
            ViewModelProvider(compose.activity).get("fresh_marker_preferences", GuidedSessionViewModel::class.java)
        }
        compose.waitUntil(5000) { fresh.recordsReady && !fresh.busy }
        assertEquals(1, fresh.markerSpeedStep)
        assertEquals(0, fresh.markerRangeStep)
        reset()
        assertNull(stored.read())
    }

    @Test fun markerCompletesWithoutAddingRestRecords() {
        reset()
        click("마커 따라보기 체험 20초")
        compose.onNodeWithText("안내 시작").assertIsDisplayed()
        compose.onNodeWithText("나중에").assertIsDisplayed()
        compose.onNodeWithTag("marker_speed").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(0f) }
        compose.onNodeWithTag("marker_range").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(0f) }
        assertEquals(SessionPhase.READY, model().state.phase)
        assertEquals(0, model().markerSpeedStep)
        assertEquals(0, model().markerRangeStep)
        click("기본값으로 초기화")
        assertEquals(4, model().markerSpeedStep)
        assertEquals(4, model().markerRangeStep)
        compose.onNodeWithTag("marker_speed").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(1f) }
        compose.onNodeWithTag("marker_range").performScrollTo()
            .performSemanticsAction(SemanticsActions.SetProgress) { it(2f) }
        compose.onNodeWithTag("marker_surface").performScrollTo().assertIsDisplayed()
        click("안내 시작")
        compose.onNodeWithTag("marker_surface").assertIsDisplayed()
        val id = model().state.sessionId
        click("일시정지")
        val frozen = model().state.elapsedMs
        compose.activityRule.scenario.recreate()
        assertEquals(id, model().state.sessionId)
        assertEquals(SessionPhase.PAUSED, model().state.phase)
        assertEquals(frozen, model().state.elapsedMs)
        click("계속하기")
        compose.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        compose.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        assertEquals(SessionPhase.PAUSED, model().state.phase)
        click("계속하기")
        compose.waitUntil(30_000) { model().state.phase == SessionPhase.CONFIRM }
        compose.onNodeWithText("마커 체험 안내가 끝났어요.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("따라 했어요 — 완료로 남기기").assertDoesNotExist()
        compose.runOnIdle { model().confirm() }
        assertTrue(model().records.isEmpty())
        reset()
    }

    @Test fun rotationKeepsSessionAndBackgroundRequiresManualResume() {
        reset()
        click("30초 휴식 시작")
        click("안내 시작")
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
        click("계속하기")
        click("그만하기")
        compose.onNodeWithText("안내를 종료했어요. 완료 기록은 남기지 않았습니다.")
            .performScrollTo().assertIsDisplayed()
        assertTrue(model().records.isEmpty())
        reset()
    }
}
