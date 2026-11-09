package com.eyeoverlay.releasevalidation;

import android.app.KeyguardManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.*;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

/** One real 30-second session against the full-R8 app; no clock/deadline injection. */
@RunWith(AndroidJUnit4.class)
public class GuidedSessionSmokeTest {
    private static final String APP = "com.eyeoverlay.app";
    private UiDevice device;
    private String awake;

    @Before public void prepare() throws Exception {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.wakeUp();
        assertFalse("Unlock the phone before the short release check",
            InstrumentationRegistry.getInstrumentation().getContext()
                .getSystemService(KeyguardManager.class).isKeyguardLocked());
        awake = device.executeShellCommand("settings get global stay_on_while_plugged_in").trim();
        device.executeShellCommand("svc power stayon usb");
        launch();
        deleteRecords();
    }

    @After public void cleanup() throws Exception {
        if (device == null) return;
        try {
            device.executeShellCommand("am force-stop " + APP);
        } finally {
            if (awake != null) {
                if ("null".equals(awake)) {
                    device.executeShellCommand("settings delete global stay_on_while_plugged_in");
                } else {
                    assertTrue(awake.matches("\\d+"));
                    device.executeShellCommand("settings put global stay_on_while_plugged_in " + awake);
                }
            }
        }
    }

    @Test public void confirmedSessionSurvivesProcessRestartAndCanBeDeleted() throws Exception {
        click("30초 휴식 시작");
        click("안내 시작");
        assertTrue("Thirty-second session did not reach confirmation",
            device.wait(Until.hasObject(By.text("안내 시간이 끝났어요.")), 40000));
        assertTrue(device.hasObject(By.text("오늘 완료로 남긴 안내 0회")));
        click("따라 했어요 — 완료로 남기기");
        assertTrue(device.wait(Until.hasObject(By.text("휴식 안내 1회를 완료로 남겼어요.")), 5000));
        device.executeShellCommand("am force-stop " + APP);
        launch();
        assertTrue(device.wait(Until.hasObject(By.text("오늘 완료로 남긴 안내 1회")), 5000));
        deleteRecords();
        assertTrue(device.wait(Until.hasObject(By.text("오늘 완료로 남긴 안내 0회")), 5000));
    }

    private void deleteRecords() throws Exception {
        click("이 기기의 앱 기록 전체 삭제");
        click("전체 삭제");
        assertTrue(device.wait(Until.gone(By.text("전체 삭제")), 5000));
        new UiScrollable(new UiSelector().scrollable(true)).scrollToBeginning(10);
    }

    private void launch() throws Exception {
        String output = device.executeShellCommand("am start -W --activity-clear-top -n "
            + APP + "/com.example.myapplication.MainActivity");
        assertTrue(output, output.contains("Status: ok"));
        assertTrue(device.wait(Until.hasObject(By.pkg(APP)), 10000));
    }

    private void click(String text) throws Exception {
        UiObject2 button = device.wait(Until.findObject(By.text(text).enabled(true)), 1000);
        if (button == null) {
            new UiScrollable(new UiSelector().scrollable(true)).scrollTextIntoView(text);
            button = device.wait(Until.findObject(By.text(text).enabled(true)), 5000);
        }
        assertNotNull("Missing enabled control: " + text, button);
        button.click();
        device.waitForIdle();
    }
}
