package com.example.myapplication.session

import java.time.LocalDate
import java.util.UUID

data class CompletionRecord(
    val sessionId: String,
    val completedAtEpochMillis: Long,
    val completedLocalDate: String,
    val routineId: String = "screen_rest",
    val routineVersion: Int = 1,
    val plannedDurationSeconds: Int = 30,
    val confirmation: String = "user_confirmed"
)

interface CompletionStorage {
    fun read(): String
    fun write(value: String): Boolean
}

/** Counts are derived from the ledger. IO runs off the UI thread. */
class CompletionRepository(private val storage: CompletionStorage) {
    private var cached: List<CompletionRecord>? = null

    @Synchronized fun records(): List<CompletionRecord> {
        cached?.let { return it }
        val text = storage.read()
        val records = if (text.isEmpty()) emptyList() else {
            val lines = text.lineSequence().toList()
            require(lines.first() == "v1") { "Unknown completion record format" }
            lines.drop(1).filter { it.isNotEmpty() }.map { line ->
                val parts = line.split('\t')
                require(parts.size == 7) { "Invalid completion record" }
                CompletionRecord(parts[0], parts[1].toLong(), parts[2], parts[3],
                    parts[4].toInt(), parts[5].toInt(), parts[6]).also(::validate)
            }.also { rows ->
                require(rows.map { it.sessionId }.distinct().size == rows.size)
            }
        }
        return records.toList().also { cached = it }
    }

    @Synchronized fun save(record: CompletionRecord): List<CompletionRecord> {
        validate(record)
        val current = records()
        if (current.any { it.sessionId == record.sessionId }) return current
        val updated = current + record
        check(storage.write(encode(updated))) { "Completion record was not saved" }
        cached = updated
        return updated
    }

    @Synchronized fun clear() {
        check(storage.write("")) { "Completion records were not deleted" }
        cached = emptyList()
    }

    private fun validate(record: CompletionRecord) {
        require(UUID.fromString(record.sessionId).toString() == record.sessionId)
        LocalDate.parse(record.completedLocalDate)
        require(record.completedAtEpochMillis >= 0L && (record.routineId == "screen_rest" || record.routineId == "eye_gym" || record.routineId == "smooth_pursuit") &&
            record.routineVersion == 1 && (record.plannedDurationSeconds == 30 || record.plannedDurationSeconds == 20) &&
            record.confirmation == "user_confirmed")
    }

    private fun encode(records: List<CompletionRecord>) = "v1\n" + records.joinToString("\n") {
        listOf(it.sessionId, it.completedAtEpochMillis, it.completedLocalDate, it.routineId,
            it.routineVersion, it.plannedDurationSeconds, it.confirmation).joinToString("\t")
    }
}
