package com.example.myapplication

import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.SmartDevicePoseDetector
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SensorLifecycleInstrumentedTest {
    @Test
    fun physicalSensorRegistrationProducesFreshSamplesAndStopInvalidates() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val detector = SmartDevicePoseDetector(context)
        try {
            detector.startListening()
            assertTrue("No supported motion/environment sensors registered", detector.hasRegisteredSensors)

            val deadline = SystemClock.elapsedRealtime() + 5_000L
            while (!detector.isFresh && SystemClock.elapsedRealtime() < deadline) {
                Thread.sleep(25L)
            }
            assertTrue("No validated physical sensor callback within five seconds", detector.isFresh)
        } finally {
            detector.stopListening()
        }
        assertFalse(detector.isFresh)
    }
}
