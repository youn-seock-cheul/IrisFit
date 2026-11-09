package com.eyeoverlay.releasevalidation;

import android.app.KeyguardManager;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** Opt-in real elapsed-time observation. Never changes clocks or app deadlines. */
@RunWith(AndroidJUnit4.class)
public class NaturalReminderStopTest {
    private static final String APP = "com.eyeoverlay.app";
    private static final String TITLE = "잠시 화면에서 눈을 떼세요";
    private UiDevice device;
    private String originalAwake;
    private Boolean originalPermission;

    @Before public void prepare() throws Exception {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.wakeUp();
        KeyguardManager keyguard = InstrumentationRegistry.getInstrumentation().getContext()
            .getSystemService(KeyguardManager.class);
        assertFalse("Unlock the phone before starting the real-time test", keyguard.isKeyguardLocked());
        originalAwake = device.executeShellCommand("settings get global stay_on_while_plugged_in").trim();
        String permissions = device.executeShellCommand("dumpsys package " + APP);
        Matcher permission = Pattern.compile("android.permission.POST_NOTIFICATIONS: granted=(true|false)")
            .matcher(permissions);
        assertTrue("Cannot capture the target notification permission", permission.find());
        originalPermission = Boolean.parseBoolean(permission.group(1));
        device.executeShellCommand("svc power stayon usb");
        device.executeShellCommand("pm grant " + APP + " android.permission.POST_NOTIFICATIONS");
        launch();
        click("이 기기의 앱 기록 전체 삭제");
        click("전체 삭제");
    }

    @After public void cleanup() throws Exception {
        if (device == null) return;
        try {
            device.executeShellCommand("am force-stop " + APP);
        } finally {
            try {
                if (Boolean.FALSE.equals(originalPermission)) {
                    device.executeShellCommand("pm revoke " + APP + " android.permission.POST_NOTIFICATIONS");
                }
            } finally {
                if (originalAwake != null) {
                    if ("null".equals(originalAwake)) {
                        device.executeShellCommand("settings delete global stay_on_while_plugged_in");
                    } else {
                        assertTrue("Unexpected original awake setting", originalAwake.matches("\\d+"));
                        device.executeShellCommand("settings put global stay_on_while_plugged_in " + originalAwake);
                    }
                }
            }
        }
    }

    @Test public void realTwentyMinuteReminderStopsFromItsNotificationAction() throws Exception {
        click("20분 휴식 알림 시작");
        String warning = "시스템 알림이 꺼져 있습니다. 타이머는 앱 화면에서 확인할 수 있습니다.";
        assertTrue("Enable the reminder channel before this observation",
            device.wait(Until.gone(By.text(warning)), 5000));
        long startElapsed = SystemClock.elapsedRealtime();
        long startWall = System.currentTimeMillis();
        Long deadline = registeredDeadline();
        assertNotNull("No reminder registered", deadline);
        assertTrue("The reminder must still have at least 19 real minutes remaining",
            deadline - startElapsed >= 19 * 60 * 1000L);
        Log.i("P0_NATURAL", "startedWall=" + startWall + " startedElapsed=" + startElapsed
            + " originalDeadline=" + deadline + " originalAwake=" + originalAwake);
        device.pressHome();

        long timeout = startElapsed + 40 * 60 * 1000L;
        while (!hasOwnNotification() && SystemClock.elapsedRealtime() < timeout) {
            SystemClock.sleep(15000);
        }
        assertTrue("No OS notification observed within 40 minutes; inspect delay conditions",
            hasOwnNotification());
        long observed = SystemClock.elapsedRealtime();
        Log.i("P0_NATURAL", "observedWall=" + System.currentTimeMillis()
            + " observedElapsed=" + observed + " observedDelayMs=" + (observed - deadline)
            + " pollResolutionMs=15000");
        device.openNotification();
        UiObject2 title = device.wait(Until.findObject(By.text(TITLE)), 5000);
        if (title == null) {
            new UiScrollable(new UiSelector().scrollable(true)).scrollTextIntoView(TITLE);
            title = device.wait(Until.findObject(By.text(TITLE)), 5000);
        }
        assertNotNull("Registered notification title is not visible", title);
        UiObject2 stop = ownStopAction(false);
        if (stop == null) {
            ownStopAction(true);
            SystemClock.sleep(500);
            stop = ownStopAction(false);
        }
        assertNotNull("Notification arrived but its own stop action could not be located", stop);
        stop.click();
        long stopUntil = SystemClock.elapsedRealtime() + 10000;
        while (hasOwnNotification() && SystemClock.elapsedRealtime() < stopUntil) SystemClock.sleep(200);
        assertFalse("The stop action did not remove the notification", hasOwnNotification());
        assertNull("The stop action left an OS alarm", registeredDeadline());
        launch();
        scrollTop();
        assertTrue("The stop action did not clear the timer",
            device.wait(Until.hasObject(By.text("휴식 알림을 시작해 보세요")), 10000));
        Log.i("P0_NATURAL", "STOP_OK notificationAbsent=true alarmAbsent=true timerInitial=true");
    }

    // Never search actions outside the notification card containing our unique title.
    private UiObject2 ownStopAction(boolean expand) {
        UiObject2 node = device.findObject(By.text(TITLE));
        for (int depth = 0; node != null && depth < 8; depth++, node = node.getParent()) {
            Rect bounds = node.getVisibleBounds();
            if (bounds.height() > device.getDisplayHeight() * 0.8 ||
                node.findObjects(By.res("android", "title")).size() > 1) break;
            UiObject2 action = node.findObject(By.text("알림 종료"));
            if (action != null) return action;
            UiObject2 button = node.findObject(By.res("android", "expand_button"));
            if (expand && button != null) {
                button.click();
                return null;
            }
        }
        return null;
    }

    private boolean hasOwnNotification() throws Exception {
        String keys = device.executeShellCommand("cmd notification list");
        for (String line : keys.split("\\r?\\n")) {
            if (line.trim().startsWith("0|" + APP + "|2001|")) return true;
        }
        return false;
    }

    private Long registeredDeadline() throws Exception {
        Matcher match = Pattern.compile(
            "(?m)^\\s*ELAPSED_WAKEUP #[^\\n]*origWhen (\\d+)[^\\n]* com\\.eyeoverlay\\.app\\}")
            .matcher(device.executeShellCommand("dumpsys alarm"));
        return match.find() ? Long.parseLong(match.group(1)) : null;
    }

    private void launch() throws Exception {
        String output = device.executeShellCommand("am start -W --activity-clear-top -n "
            + APP + "/com.example.myapplication.MainActivity");
        assertTrue(output, output.contains("Status: ok"));
        assertTrue("Target app not visible", device.wait(Until.hasObject(By.pkg(APP)), 10000));
    }

    private void scrollTop() throws Exception {
        if (device.hasObject(By.scrollable(true))) {
            new UiScrollable(new UiSelector().scrollable(true)).scrollToBeginning(10);
        }
    }

    private void click(String text) throws Exception {
        UiObject2 object = device.wait(Until.findObject(By.text(text)), 1000);
        if (object == null) {
            new UiScrollable(new UiSelector().scrollable(true)).scrollTextIntoView(text);
            object = device.wait(Until.findObject(By.text(text)), 5000);
        }
        assertNotNull("Missing control: " + text, object);
        object.click();
        device.waitForIdle();
    }
}
