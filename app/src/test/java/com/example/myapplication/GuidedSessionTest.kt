package com.example.myapplication

import com.example.myapplication.session.GuidedSession
import com.example.myapplication.session.SessionPhase
import org.junit.Assert.*
import org.junit.Test

class GuidedSessionTest {
    private var now = 0L
    private val session = GuidedSession({ now }) { "session-id" }

    @Test fun stagesEndAtThirtySecondsButNeedExplicitConfirmation() {
        session.prepare()
        session.start()
        assertEquals(0, session.state.step)
        now = 5_000
        session.tick()
        assertEquals(1, session.state.step)
        now = 25_000
        session.tick()
        assertEquals(2, session.state.step)
        now = 30_000
        session.tick()
        assertEquals(SessionPhase.CONFIRM, session.state.phase)
        assertEquals(0L, session.state.remainingSeconds)
        session.saved()
        assertEquals(SessionPhase.COMPLETED, session.state.phase)
    }

    @Test fun backgroundTimeDoesNotCountAndMultipleSegmentsAccumulate() {
        session.prepare(); session.start()
        now = 4_000; session.pause()
        now = 100_000; session.tick()
        assertEquals(4_000L, session.state.elapsedMs)
        session.resume()
        now = 106_000; session.pause()
        assertEquals(10_000L, session.state.elapsedMs)
        now = 200_000; session.resume()
        now = 220_000; session.tick()
        assertEquals(SessionPhase.CONFIRM, session.state.phase)
    }

    @Test fun latePauseAtDeadlineGoesToConfirmationRatherThanRestarting() {
        session.prepare(); session.start()
        now = 40_000; session.pause()
        assertEquals(SessionPhase.CONFIRM, session.state.phase)
        session.resume()
        assertEquals(SessionPhase.CONFIRM, session.state.phase)
    }

    @Test fun abandonedSessionCannotBecomeCompletedAndNextStartIsFresh() {
        session.prepare(); session.start()
        now = 3_000; session.end(); session.saved()
        assertEquals(SessionPhase.ENDED, session.state.phase)
        session.prepare(); session.start()
        assertEquals(0L, session.state.elapsedMs)
        session.home()
        assertEquals(SessionPhase.HOME, session.state.phase)
    }

    @Test fun repeatedStartDoesNotResetAnActiveDeadline() {
        session.prepare(); session.start()
        now = 10_000; session.start(); session.tick()
        assertEquals(10_000L, session.state.elapsedMs)
    }
}
