package com.example.myapplication

import com.example.myapplication.session.ColorTonePreferences
import com.example.myapplication.session.CompletionStorage
import com.example.myapplication.ui.theme.AppColorTone
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ColorTonePreferencesTest {
    private class MemoryStorage(var value: String = "", var writable: Boolean = true) : CompletionStorage {
        override fun read() = value
        override fun write(value: String): Boolean {
            if (!writable) return false
            this.value = value
            return true
        }
    }

    @Test fun defaultIsGreenAndAllFiveChoicesSurviveANewPreferencesInstance() {
        val storage = MemoryStorage()
        assertEquals(AppColorTone.GREEN, ColorTonePreferences(storage).read())
        assertEquals(5, AppColorTone.entries.size)
        AppColorTone.entries.forEach { tone ->
            ColorTonePreferences(storage).save(tone)
            assertEquals(tone, ColorTonePreferences(storage).read())
        }
    }

    @Test fun invalidSettingsAndFailedWritesAreNotReportedAsSuccess() {
        listOf("v2,blue", "v1,unknown", "blue", "v1,BLUE").forEach {
            assertThrows(IllegalArgumentException::class.java) { ColorTonePreferences(MemoryStorage(it)).read() }
        }
        val storage = MemoryStorage("v1,blue", writable = false)
        assertThrows(IOException::class.java) { ColorTonePreferences(storage).save(AppColorTone.PEACH) }
        assertEquals(AppColorTone.BLUE, ColorTonePreferences(storage).read())
    }
}
