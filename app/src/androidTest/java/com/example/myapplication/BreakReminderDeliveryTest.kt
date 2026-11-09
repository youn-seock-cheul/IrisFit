package com.example.myapplication

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.SystemClock
import android.provider.Settings
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.reminder.BreakReminderReceiver
import com.example.myapplication.reminder.BreakReminderScheduler
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Device delivery/control contracts using injected due state, NOT a claim about
 * real 20-minute AlarmManager timing. Notification permission must be granted.
 */
@RunWith(AndroidJUnit4::class)
class BreakReminderDeliveryTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val notifications get() = context.getSystemService(NotificationManager::class.java)
    private val scheduler get() = BreakReminderScheduler(context)

    @Before fun prepare() {
        scheduler.cancel()
        // UTP can reinstall the target and reset the user's previous grant.
        InstrumentationRegistry.getInstrumentation().uiAutomation.grantRuntimePermission(
            context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        assertEquals(PackageManager.PERMISSION_GRANTED,
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS))
        assertTrue("Enable this app's notifications before delivery tests", notifications.areNotificationsEnabled())
    }

    @After fun cleanup() {
        scheduler.cancel()
    }

    @Test fun dueBroadcastPostsNotificationAndItsStopActionCancelsReminder() {
        context.getSharedPreferences("break_reminder", Context.MODE_PRIVATE).edit()
            .putLong("deadline", SystemClock.elapsedRealtime() - 1000L)
            .putInt("boot", Settings.Global.getInt(context.contentResolver, Settings.Global.BOOT_COUNT, -1))
            .commit()
        sendReminder()
        await { ownNotification() != null }
        val notification = ownNotification()!!
        assertEquals("잠시 화면에서 눈을 떼세요",
            notification.extras.getString(Notification.EXTRA_TITLE))
        notification.actions.single { it.title.toString() == "알림 종료" }.actionIntent.send()
        await { scheduler.deadline() == 0L && ownNotification() == null }
    }

    @Test fun earlyAndCancelledBroadcastsDoNotCreateNotifications() {
        scheduler.start()
        val originalDeadline = scheduler.deadline()
        sendReminder()
        SystemClock.sleep(750L)
        assertEquals(originalDeadline, scheduler.deadline())
        assertFalse(ownNotification() != null)
        scheduler.cancel()
        sendReminder()
        SystemClock.sleep(750L)
        assertEquals(0L, scheduler.deadline())
        assertFalse(ownNotification() != null)
    }

    private fun sendReminder() {
        context.sendBroadcast(Intent(context, BreakReminderReceiver::class.java)
            .setAction(BreakReminderScheduler.ACTION_REMIND))
    }

    private fun ownNotification(): Notification? =
        notifications.activeNotifications.firstOrNull { it.id == 2001 }?.notification

    private fun await(condition: () -> Boolean) {
        val until = SystemClock.elapsedRealtime() + 10_000L
        while (!condition() && SystemClock.elapsedRealtime() < until) SystemClock.sleep(100L)
        assertTrue("Notification/stop callback did not complete within 10 seconds", condition())
    }
}
