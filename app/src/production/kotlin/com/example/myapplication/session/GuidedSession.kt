package com.example.myapplication.session

enum class SessionPhase { HOME, READY, RUNNING, PAUSED, CONFIRM, COMPLETED, ENDED }

data class SessionState(
    val phase: SessionPhase = SessionPhase.HOME,
    val sessionId: String = "",
    val elapsedMs: Long = 0L,
    val durationMs: Long = 30_000L
) {
    val remainingSeconds: Long get() = ((durationMs - elapsedMs).coerceAtLeast(0L) + 999L) / 1000L
    val step: Int get() = when {
        elapsedMs < 5_000L -> 0
        elapsedMs < 25_000L -> 1
        else -> 2
    }
}

/** Counts only active segments using a monotonic clock; no saved in-progress session. */
class GuidedSession(private val clock: () -> Long, private val newId: () -> String) {
    var state = SessionState()
        private set
    private var segmentStart = 0L
    private var accumulated = 0L

    fun prepare(durationMs: Long = 30_000L) {
        require(durationMs == 20_000L || durationMs == 30_000L)
        accumulated = 0L
        state = SessionState(SessionPhase.READY, durationMs = durationMs)
    }

    fun start() {
        if (state.phase != SessionPhase.READY) return
        accumulated = 0L
        segmentStart = clock()
        state = SessionState(SessionPhase.RUNNING, newId(), durationMs = state.durationMs)
    }

    fun tick() {
        if (state.phase != SessionPhase.RUNNING) return
        val elapsed = (accumulated + (clock() - segmentStart).coerceAtLeast(0L)).coerceAtMost(state.durationMs)
        state = state.copy(
            elapsedMs = elapsed,
            phase = if (elapsed == state.durationMs) SessionPhase.CONFIRM else SessionPhase.RUNNING
        )
    }

    fun pause() {
        tick()
        if (state.phase != SessionPhase.RUNNING) return
        accumulated = state.elapsedMs
        state = state.copy(phase = SessionPhase.PAUSED)
    }

    fun resume() {
        if (state.phase != SessionPhase.PAUSED) return
        segmentStart = clock()
        state = state.copy(phase = SessionPhase.RUNNING)
    }

    fun end() { state = state.copy(phase = SessionPhase.ENDED) }
    fun home() { state = SessionState() }
    fun saved() {
        if (state.phase == SessionPhase.CONFIRM) state = state.copy(phase = SessionPhase.COMPLETED)
    }
}
