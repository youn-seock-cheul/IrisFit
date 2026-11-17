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

    // 3. 완료 기록이 1,001건 이상 누적되어도 절삭 없이 전량 보관되며 새 저장소 인스턴스에서 월별 집계가 정확한지 검증
    @Test
    fun repositoryExceeding1001RecordsAcrossMonths_preservesExactCountsInNewInstance() {
        val novDateStr = "2026-11-15"
        val octDateStr = "2026-10-15"

        // 10월 기록 500건 저장
        for (i in 1..500) {
            val uuid = java.util.UUID.randomUUID().toString()
            repository.save(CompletionRecord(uuid, System.currentTimeMillis(), octDateStr, "screen_rest"))
        }

        // 11월 기록 1,005건 저장 (한 달 1,001건 초과!)
        for (i in 1..1005) {
            val uuid = java.util.UUID.randomUUID().toString()
            repository.save(CompletionRecord(uuid, System.currentTimeMillis(), novDateStr, "screen_rest"))
        }

        // 새 저장소 인스턴스로 동기화 로드
        val newRepo = CompletionRepository(storage)
        val loadedRecords = newRepo.records()
        assertEquals(1505, loadedRecords.size)

        // 월별 집계 정확성 실측 검증
        val novStats = com.example.myapplication.session.RestHistory.month(
            loadedRecords,
            java.time.YearMonth.of(2026, 11),
            LocalDate.parse("2026-11-30")
        )
        val octStats = com.example.myapplication.session.RestHistory.month(
            loadedRecords,
            java.time.YearMonth.of(2026, 10),
            LocalDate.parse("2026-10-31")
        )

        assertEquals(1005, novStats.rests)
        assertEquals(500, octStats.rests)
    }

    // 4. 세션 ID 형식이 유효한 UUID인지 검증
    @Test
    fun workoutSession_generatesValidUuidSessionId() {
        val sessionId = workoutCompletion.start("smooth_pursuit")
        val parsedUuid = java.util.UUID.fromString(sessionId)
        assertEquals(sessionId, parsedUuid.toString())
    }
}
