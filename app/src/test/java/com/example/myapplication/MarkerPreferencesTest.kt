package com.example.myapplication

import com.example.myapplication.session.*
import org.junit.Assert.*
import org.junit.Test

class MarkerPreferencesTest {
    private class Storage : CompletionStorage {
        var value = ""
        var fail = false
        override fun read() = value
        override fun write(value: String): Boolean {
            if (fail) return false
            this.value = value
            return true
        }
    }
    @Test fun onlyExplicitSaveSurvivesNewRepository() {
        val storage = Storage()
        assertNull(MarkerPreferences(storage).read())
        MarkerPreferences(storage).save(MarkerPreference(1, 0))
        assertEquals(MarkerPreference(1, 0), MarkerPreferences(storage).read())
    }
    @Test fun failedSavePreservesPreviousValues() {
        val storage = Storage()
        val prefs = MarkerPreferences(storage)
        prefs.save(MarkerPreference(1, 0))
        storage.fail = true
        assertThrows(IllegalStateException::class.java) { prefs.save(MarkerPreference(4, 4)) }
        assertEquals(MarkerPreference(1, 0), prefs.read())
    }
    @Test fun explicitDeletionRestoresNoSavedPreference() {
        val prefs = MarkerPreferences(Storage())
        prefs.save(MarkerPreference(1, 0))
        prefs.clear()
        assertNull(prefs.read())
    }
    @Test fun malformedValuesAreNotAccepted() {
        val storage = Storage().apply { value = "v1,99,0" }
        assertThrows(IllegalArgumentException::class.java) { MarkerPreferences(storage).read() }
        assertThrows(IllegalArgumentException::class.java) { MarkerPreference(-1, 0) }
    }
}
