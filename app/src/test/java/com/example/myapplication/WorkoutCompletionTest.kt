package com.example.myapplication

import com.example.myapplication.session.CompletionRepository
import com.example.myapplication.session.CompletionStorage
import com.example.myapplication.session.WorkoutCompletion
import org.junit.Assert.*
import org.junit.Test

class WorkoutCompletionTest {
    private class Storage : CompletionStorage {
        var text = ""
        var fail = false
        override fun read() = text
        override fun write(value: String): Boolean {
            if (fail) return false
            text = value
            return true
        }
    }

    @Test fun duplicateCompletionAndNewGameAreDistinct() {
        val repo = CompletionRepository(Storage())
        val workout = WorkoutCompletion(repo)
        val first = workout.start("eye_gym")
        assertTrue(workout.save(first, "eye_gym", 1, "2026-11-14").newlySaved)
        assertFalse(workout.save(first, "eye_gym", 2, "2026-11-15").newlySaved)
        assertTrue(workout.isSaved(first))
        assertEquals(1, repo.records().size)
        val second = workout.start("eye_gym")
        assertNotEquals(first, second)
        assertFalse(workout.save(first, "eye_gym", 3, "2026-11-15").newlySaved)
        workout.save(second, "eye_gym", 4, "2026-11-15")
        assertEquals(listOf(first, second), repo.records().map { it.sessionId })
    }

    @Test fun failedSaveRetriesOriginalIdAndDate() {
        val storage = Storage()
        val repo = CompletionRepository(storage)
        val workout = WorkoutCompletion(repo)
        val id = workout.start("smooth_pursuit")
        storage.fail = true
        assertThrows(IllegalStateException::class.java) { workout.save(id, "smooth_pursuit", 1, "2026-11-14") }
        assertFalse(workout.isSaved(id))
        storage.fail = false
        workout.save(id, "smooth_pursuit", 2, "2026-11-15")
        assertEquals(id, repo.records().single().sessionId)
        assertEquals("2026-11-14", repo.records().single().completedLocalDate)
    }

    @Test fun restoredControllerDoesNotDuplicatePersistedSession() {
        val repo = CompletionRepository(Storage())
        val original = WorkoutCompletion(repo)
        val id = original.start("eye_gym")
        original.save(id, "eye_gym", 1, "2026-11-14")
        val reopened = WorkoutCompletion(repo)
        reopened.start("smooth_pursuit")
        assertFalse(reopened.save(id, "eye_gym", 2, "2026-11-15").newlySaved)
        assertEquals(1, repo.records().size)
    }

    @Test fun reusingWorkoutIdForDifferentRoutineIsRejected() {
        val workout = WorkoutCompletion(CompletionRepository(Storage()))
        val id = workout.start("eye_gym")
        workout.save(id, "eye_gym", 1, "2026-11-14")
        assertThrows(IllegalArgumentException::class.java) {
            workout.save(id, "smooth_pursuit", 2, "2026-11-14")
        }
    }
}
