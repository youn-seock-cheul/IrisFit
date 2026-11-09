package com.eyeoverlay.releasevalidation;

import android.os.SystemClock;
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

/** Black-box checks: no app classes, private preferences or injected deadlines. */
@RunWith(AndroidJUnit4.class)
public class ProductionUiTest {
    private static final String APP = "com.eyeoverlay.app";
    private static final String INITIAL = "휴식 알림을 시작해 보세요";
    private static final String START = "권한 없이 화면 내 타이머 시작";
    private static final Pattern REMAINING = Pattern.compile("다음 휴식까지 (\\d+)분 (\\d+)초");
    private UiDevice device;
    private String originalAutoRotation;
    private String originalRotation;

    @Before public void prepare() throws Exception {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.wakeUp();
        originalAutoRotation = device.executeShellCommand("settings get system accelerometer_rotation").trim();
        originalRotation = device.executeShellCommand("settings get system user_rotation").trim();
        launchApp();
        clickText("이 기기의 앱 기록 전체 삭제");
        clickText("전체 삭제");
        scrollTop();
        assertTrue(device.wait(Until.hasObject(By.text(INITIAL)), 10000));
    }

    @After public void cleanup() throws Exception {
        if (device == null) return;
        try {
            device.executeShellCommand("am force-stop " + APP);
        } finally {
            device.unfreezeRotation();
            restoreSetting("accelerometer_rotation", originalAutoRotation);
            restoreSetting("user_rotation", originalRotation);
        }
    }

    @Test public void deniedPermissionTimerSurvivesRotationAndHome() throws Exception {
        device.executeShellCommand("pm revoke " + APP + " android.permission.POST_NOTIFICATIONS");
        launchApp();
        assertTrue(device.wait(Until.hasObject(By.text(
            "시스템 알림이 꺼져 있습니다. 타이머는 앱 화면에서 확인할 수 있습니다.")), 10000));
        clickText(START);
        int initial = remainingSeconds();
        SystemClock.sleep(2200);
        device.setOrientationLeft();
        int landscape = remainingSeconds();
        assertTrue("Rotation reset or lost the timer", landscape < initial && landscape >= initial - 90);
        device.setOrientationNatural();
        device.pressHome();
        launchApp();
        int returned = remainingSeconds();
        assertTrue("Home return lost the countdown", returned <= landscape && returned >= landscape - 90);
        clickText("타이머와 알림 종료");
        scrollTop();
        assertTrue(device.wait(Until.hasObject(By.text(INITIAL)), 10000));
    }

    @Test public void processDeathAndForceStopRestoreTheSameDeadline() throws Exception {
        clickText(START);
        int initial = remainingSeconds();
        Long originalDeadline = registeredDeadline();
        assertNotNull("No OS reminder registered", originalDeadline);
        device.pressHome();
        SystemClock.sleep(500);
        device.executeShellCommand("am kill " + APP);
        long until = SystemClock.elapsedRealtime() + 10000;
        while (!device.executeShellCommand("pidof " + APP).trim().isEmpty()
                && SystemClock.elapsedRealtime() < until) SystemClock.sleep(200);
        assertTrue("The background process was not actually killed",
            device.executeShellCommand("pidof " + APP).trim().isEmpty());
        assertEquals("Ordinary death cancelled the OS reminder", originalDeadline, registeredDeadline());
        launchApp();
        int resumed = remainingSeconds();
        assertTrue("Ordinary process death lost the reminder", resumed <= initial && resumed >= initial - 90);
        assertEquals("Reopening reset the deadline", originalDeadline, registeredDeadline());

        device.executeShellCommand("am force-stop " + APP);
        assertNull("Force-stop did not cancel the OS reminder", registeredDeadline());
        launchApp();
        int restored = remainingSeconds();
        assertTrue("Force-stop recovery reset the countdown", restored <= resumed && restored >= resumed - 90);
        assertEquals("Opening the app did not restore its original OS deadline",
            originalDeadline, registeredDeadline());
        clickText(START);
        assertTrue("A fresh reminder could not be started", remainingSeconds() >= 1170);
    }

    @Test public void blockedChannelShowsWarningAndOpensItsRecoverySettings() throws Exception {
        device.executeShellCommand("pm grant " + APP + " android.permission.POST_NOTIFICATIONS");
        launchApp();
        clickText(START);
        clickText("타이머와 알림 종료");
        openReminderChannel();
        UiObject2 toggle = channelToggle();
        boolean originalEnabled = toggle.isChecked();
        try {
            if (!originalEnabled) toggle.click();
            assertTrue(device.wait(Until.hasObject(
                By.clazz("android.widget.Switch").desc("알림 허용").checked(true)), 5000));
            channelToggle().click();
            assertTrue(device.wait(Until.hasObject(
                By.clazz("android.widget.Switch").desc("알림 허용").checked(false)), 5000));
            launchApp();
            scrollTop();
            String warning = "시스템 알림이 꺼져 있습니다. 타이머는 앱 화면에서 확인할 수 있습니다.";
            assertTrue("A blocked channel was not detected",
                device.wait(Until.hasObject(By.text(warning)), 10000));
            clickText("알림 설정 열기");
            assertTrue("App did not open the reminder channel settings",
                device.wait(Until.hasObject(By.text("화면 휴식 알림")), 10000));
            channelToggle().click();
            assertTrue(device.wait(Until.hasObject(
                By.clazz("android.widget.Switch").desc("알림 허용").checked(true)), 5000));
            // Settings was opened from this app; use Back to return to its Activity.
            device.pressBack();
            assertTrue("Back from channel settings did not return to the app",
                device.wait(Until.hasObject(By.pkg(APP)), 10000));
            assertTrue("Channel recovery was not reflected in the app",
                device.wait(Until.gone(By.text(warning)), 10000));
        } finally {
            openReminderChannel();
            UiObject2 current = channelToggle();
            if (current.isChecked() != originalEnabled) current.click();
            assertTrue("Could not restore the channel setting",
                device.wait(Until.hasObject(By.clazz("android.widget.Switch")
                    .desc("알림 허용").checked(originalEnabled)), 5000));
        }
    }

    private void openReminderChannel() throws Exception {
        device.executeShellCommand("am start -a android.settings.CHANNEL_NOTIFICATION_SETTINGS "
            + "--es android.provider.extra.APP_PACKAGE " + APP
            + " --es android.provider.extra.CHANNEL_ID screen_breaks");
        assertTrue(device.wait(Until.hasObject(By.text("화면 휴식 알림")), 10000));
    }

    private UiObject2 channelToggle() {
        UiObject2 toggle = device.wait(Until.findObject(
            By.clazz("android.widget.Switch").desc("알림 허용")), 5000);
        assertNotNull("Expected the Korean device's channel permission switch", toggle);
        return toggle;
    }

    private void launchApp() throws Exception {
        String result = device.executeShellCommand("am start -W -a android.intent.action.MAIN "
            + "-c android.intent.category.LAUNCHER -n " + APP + "/com.example.myapplication.MainActivity");
        assertTrue("Target launch failed: " + result, result.contains("Status: ok"));
        assertTrue("Target UI is not foreground; inspect keyguard and the task stack",
            device.wait(Until.hasObject(By.pkg(APP)), 10000));
    }

    private void scrollTop() throws Exception {
        if (device.hasObject(By.scrollable(true))) {
            new UiScrollable(new UiSelector().scrollable(true)).scrollToBeginning(10);
        }
    }

    private void clickText(String text) throws Exception {
        UiObject2 object = device.wait(Until.findObject(By.text(text)), 1000);
        if (object == null) {
            new UiScrollable(new UiSelector().scrollable(true)).scrollTextIntoView(text);
            object = device.wait(Until.findObject(By.text(text)), 5000);
        }
        assertNotNull("Missing control: " + text, object);
        object.click();
        device.waitForIdle();
    }

    private Long registeredDeadline() throws Exception {
        String alarms = device.executeShellCommand("dumpsys alarm");
        Matcher matcher = Pattern.compile(
            "(?m)^\\s*ELAPSED_WAKEUP #[^\\n]*origWhen (\\d+)[^\\n]* com\\.eyeoverlay\\.app\\}")
            .matcher(alarms);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }

    private int remainingSeconds() throws Exception {
        scrollTop();
        UiObject2 label = device.wait(Until.findObject(By.text(REMAINING)), 10000);
        assertNotNull("Missing countdown", label);
        Matcher matcher = REMAINING.matcher(label.getText());
        assertTrue(matcher.matches());
        return Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2));
    }

    private void restoreSetting(String key, String value) throws Exception {
        if (value == null) return;
        if ("null".equals(value)) device.executeShellCommand("settings delete system " + key);
        else {
            assertTrue("Unexpected original setting", value.matches("\\d+"));
            device.executeShellCommand("settings put system " + key + " " + value);
        }
    }
}
