package com.example.myapplication

import android.content.Context
import android.os.SystemClock
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.reminder.BreakReminderScheduler
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WellnessHomeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun resetReminder() {
        BreakReminderScheduler(context).cancel()
    }

    @After fun cleanup() {
        BreakReminderScheduler(context).cancel()
        context.getSharedPreferences("eye_exercise_prefs", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun timerNeedsNoCameraAndSurvivesActivityRecreation() {
        compose.onNodeWithText("권한 없이 화면 내 타이머 시작").performScrollTo().performClick()
        val scheduler = BreakReminderScheduler(context)
        compose.waitUntil(5000) { scheduler.deadline() > SystemClock.elapsedRealtime() }
        val originalDeadline = scheduler.deadline()

        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("타이머와 알림 종료").performScrollTo().assertIsDisplayed()
        assertEquals(originalDeadline, scheduler.deadline())
        compose.onNodeWithText("타이머와 알림 종료").performClick()
        assertEquals(0L, scheduler.deadline())
    }

    @Test fun deletingLocalRecordsCancelsReminderAndRemovesLegacyData() {
        context.getSharedPreferences("eye_exercise_prefs", Context.MODE_PRIVATE)
            .edit().putString("chat_history", "test-only legacy data").commit()
        compose.onNodeWithText("권한 없이 화면 내 타이머 시작").performScrollTo().performClick()
        compose.onNodeWithText("이 기기의 앱 기록 전체 삭제").performScrollTo().performClick()
        compose.onNodeWithText("전체 삭제").performClick()
        compose.waitUntil(5000) { BreakReminderScheduler(context).deadline() == 0L }
        assertTrue(context.getSharedPreferences("eye_exercise_prefs", Context.MODE_PRIVATE).all.isEmpty())
    }
}
