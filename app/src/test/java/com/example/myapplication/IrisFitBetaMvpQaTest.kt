package com.example.myapplication

import com.example.myapplication.session.CompletionRecord
import com.example.myapplication.session.CompletionRepository
import com.example.myapplication.session.CompletionStorage
import com.example.myapplication.session.EyeBatteryCalculator
import com.example.myapplication.session.EyeFatigueStatus
import com.example.myapplication.session.WorkoutCompletion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * QA 검수용 IrisFit 베타 MVP 핵심 비즈니스 로직 및 엣지케이스 단위 테스트
 */
class IrisFitBetaMvpQaTest {

    private class FakeCompletionStorage : CompletionStorage {
        private var content: String = ""
        override fun read(): String = content
        override fun write(value: String): Boolean {
            content = value
            return true
        }
    }

    private lateinit var storage: FakeCompletionStorage
    private lateinit var repository: CompletionRepository
    private lateinit var workoutCompletion: WorkoutCompletion

    @Before
    fun setUp() {
        storage = FakeCompletionStorage()
        repository = CompletionRepository(storage)
        workoutCompletion = WorkoutCompletion(repository)
    }

    // 1. 권한 거부/조회 실패 시 0분이 아닌 UNMEASURED(-1)로 명확히 처리되는지 검증
    @Test
    fun screenTimePermissionDenied_returnsUnmeasuredStatus() {
        val stats = EyeBatteryCalculator.calculateBattery(
            screenTimeMinutes = 0,
            restCount = 0,
            miniGameCount = 0,
            screenTimeMeasured = false
        )

        assertEquals(EyeFatigueStatus.UNMEASURED, stats.fatigueStatus)
        assertEquals(-1, stats.eyeHealthScore)
        assertFalse(stats.screenTimeMeasured)
    }

    // 2. 동일한 세션 ID로 운동 완료 시 중복 저장 및 보상 이중 적립이 차단되는지 검증 (멱등성)
    @Test
    fun duplicateWorkoutSessionId_preventsDuplicateSave() {
        val sessionId = workoutCompletion.start("eye_gym")
        val todayStr = LocalDate.now().toString()

        val firstSave = workoutCompletion.save(sessionId, "eye_gym", System.currentTimeMillis(), todayStr)
        assertTrue(firstSave.newlySaved)
        assertEquals(1, firstSave.records.size)

        // 동일 sessionId 재전송 시 중복 저장 거부
        val secondSave = workoutCompletion.save(sessionId, "eye_gym", System.currentTimeMillis(), todayStr)
        assertFalse(secondSave.newlySaved)
        assertEquals(1, secondSave.records.size)
    }

    // 3. 미니게임 및 휴식 완료 기록이 최대 100건을 초과할 때 바운디드 레저(Bounded Ledger) 동작 검증
    @Test
    fun repositoryExceeding100Records_maintainsMax100Limit() {
        val todayStr = LocalDate.now().toString()
        for (i in 1..105) {
            val uuid = java.util.UUID.randomUUID().toString()
            repository.save(CompletionRecord(uuid, System.currentTimeMillis(), todayStr, "screen_rest"))
        }

        val records = repository.records()
        assertEquals(100, records.size)
    }

    // 4. 세션 ID 형식이 유효한 UUID인지 검증
    @Test
    fun workoutSession_generatesValidUuidSessionId() {
        val sessionId = workoutCompletion.start("smooth_pursuit")
        val parsedUuid = java.util.UUID.fromString(sessionId)
        assertEquals(sessionId, parsedUuid.toString())
    }
}
