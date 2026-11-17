package com.example.myapplication

import com.example.myapplication.session.*
import java.util.UUID
import org.junit.Assert.*
import org.junit.Test

class CompletionRepositoryTest {
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
    private fun record(index: Int = 1) = CompletionRecord(UUID(0, index.toLong()).toString(),
        index.toLong(), "2026-11-01")

    @Test fun recordsRoundTripAndDuplicateConfirmationCountsOnce() {
        val storage = Storage()
        val repo = CompletionRepository(storage)
        val first = record()
        repo.save(first)
        repo.save(first.copy(completedLocalDate = "2026-11-02"))
        assertEquals(listOf(first), CompletionRepository(storage).records())
    }

    @Test fun failedSaveDoesNotPublishSuccessAndCanBeRetried() {
        val storage = Storage()
        val repo = CompletionRepository(storage)
        storage.fail = true
        assertThrows(IllegalStateException::class.java) { repo.save(record()) }
        assertTrue(repo.records().isEmpty())
        storage.fail = false
        assertEquals(1, repo.save(record()).size)
        assertEquals(1, CompletionRepository(storage).records().size)
    }

    @Test fun retainsMoreThanHundredAndPreservesOriginalLocalDate() {
        val storage = Storage()
        val repo = CompletionRepository(storage)
        for (i in 1..101) repo.save(record(i))
        val rows = CompletionRepository(storage).records()
        assertEquals(101, rows.size)
        assertEquals(record(1), rows.first())
        assertEquals("2026-11-01", rows.last().completedLocalDate)
    }

    @Test fun corruptedRecordsAreNotSilentlyOverwrittenButCanBeExplicitlyDeleted() {
        val storage = Storage().apply { text = "unrecognized format" }
        val repo = CompletionRepository(storage)
        assertThrows(IllegalArgumentException::class.java) { repo.save(record()) }
        assertEquals("unrecognized format", storage.text)
        repo.clear()
        assertTrue(CompletionRepository(storage).records().isEmpty())
    }

    @Test fun failedDeleteDoesNotDiscardExistingRecords() {
        val storage = Storage()
        val repo = CompletionRepository(storage)
        repo.save(record())
        storage.fail = true
        assertThrows(IllegalStateException::class.java) { repo.clear() }
        assertEquals(1, repo.records().size)
        storage.fail = false
        repo.clear()
        assertTrue(CompletionRepository(storage).records().isEmpty())
    }
}
