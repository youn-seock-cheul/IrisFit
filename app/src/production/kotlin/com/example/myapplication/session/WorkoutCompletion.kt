package com.example.myapplication.session

import java.util.UUID

class WorkoutCompletion(private val repository: CompletionRepository) {
    data class Result(val records: List<CompletionRecord>, val newlySaved: Boolean)
    private var activeId: String? = null
    private var routine: String? = null
    private var pending: CompletionRecord? = null
    private var saved = false

    @Synchronized fun start(routineId: String): String {
        require(routineId == "eye_gym" || routineId == "smooth_pursuit")
        return UUID.randomUUID().toString().also {
            activeId = it
            routine = routineId
            pending = null
            saved = false
        }
    }

    @Synchronized fun save(id: String, routineId: String, completedAt: Long, localDate: String): Result {
        require(routineId == "eye_gym" || routineId == "smooth_pursuit")
        require(UUID.fromString(id).toString() == id)
        val persisted = repository.records().firstOrNull { it.sessionId == id }
        if (persisted != null) {
            require(persisted.routineId == routineId) { "Workout ID is already recorded for another routine." }
            return Result(repository.records(), false)
        }
        if (activeId == null) {
            activeId = id
            routine = routineId
        }
        require(id == activeId && routineId == routine) { "Workout session is no longer active." }
        val record = pending ?: CompletionRecord(
            id, completedAt, localDate, requireNotNull(routine), 1, 20
        ).also { pending = it }
        val alreadySaved = repository.records().any { it.sessionId == id }
        val records = repository.save(record)
        saved = true
        return Result(records, !alreadySaved)
    }

    @Synchronized fun isSaved(id: String): Boolean = id == activeId && saved
}
