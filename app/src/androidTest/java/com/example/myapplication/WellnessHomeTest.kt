package com.example.myapplication

import android.Manifest
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.session.CompletionRecord
import com.example.myapplication.session.FileCompletionStorage
import com.example.myapplication.session.GuidedSessionViewModel
import com.example.myapplication.session.MarkerPreference
import com.example.myapplication.session.MarkerPreferences
import com.example.myapplication.session.SessionPhase
import java.time.LocalDate
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WellnessHomeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun model() = ViewModelProvider(compose.activity)[GuidedSessionViewModel::class.java]

    @Test fun betaHomeStartsAndStopsGuidedRestWithoutLegacyTimerOrCamera() {
        val viewModel = model()
        compose.waitUntil(5000) { viewModel.recordsReady && !viewModel.busy }
        val originalRecords = viewModel.records
        val cameraPermissionBefore = context.checkSelfPermission(Manifest.permission.CAMERA)

        compose.onNodeWithText("권한 없이 화면 내 타이머 시작").assertDoesNotExist()
        compose.onNodeWithTag("home_rest_tile").performScrollTo().performClick()
        compose.waitUntil(5000) { viewModel.state.phase == SessionPhase.RUNNING }
        compose.onNodeWithText(viewModel.tr("session_stop")).performClick()
        compose.waitUntil(5000) { viewModel.state.phase == SessionPhase.ENDED }

        assertEquals(originalRecords, viewModel.records)
        assertEquals(cameraPermissionBefore, context.checkSelfPermission(Manifest.permission.CAMERA))
    }

    @Test fun deletingLocalRecordsClearsLedgerAndMarkerPreferences() {
        val viewModel = model()
        compose.waitUntil(5000) { viewModel.recordsReady && !viewModel.busy }
        val repository = FileCompletionStorage.repository(context)
        val originalRecords = repository.records()
        val markerStorage = FileCompletionStorage(context, "marker_preferences_v1.txt")
        val originalMarker = markerStorage.read()

        try {
            repository.save(
                CompletionRecord(
                    sessionId = UUID.randomUUID().toString(),
                    completedAtEpochMillis = System.currentTimeMillis(),
                    completedLocalDate = LocalDate.now().toString()
                )
            )
            MarkerPreferences(markerStorage).save(MarkerPreference(2, 3))
            viewModel.reloadRecords()
            compose.waitUntil(5000) { !viewModel.busy && viewModel.records.size == originalRecords.size + 1 }

            compose.onNodeWithText(viewModel.tr("nav_settings")).performClick()
            compose.onNodeWithText(viewModel.tr("settings_delete_btn")).performScrollTo().performClick()
            compose.onNodeWithText(viewModel.tr("btn_delete")).performClick()
            compose.waitUntil(5000) { !viewModel.busy && viewModel.records.isEmpty() }

            assertTrue(repository.records().isEmpty())
            assertNull(MarkerPreferences(markerStorage).read())
        } finally {
            repository.clear()
            originalRecords.forEach(repository::save)
            check(markerStorage.write(originalMarker))
            viewModel.reloadRecords()
            compose.waitUntil(5000) { !viewModel.busy }
        }
    }
}
