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
import java.io.File
import java.time.LocalDate

/**
 * QA 검수용 IrisFit 베타 MVP 핵심 비즈니스 로직 및 엣지케이스 단위 테스트
 */
class IrisFitBetaMvpQaTest {

    private class TestFileStorage(private val file: File) : CompletionStorage {
        override fun read(): String = if (file.exists()) file.readText(Charsets.UTF_8) else ""
        override fun write(value: String): Boolean {
            file.writeText(value, Charsets.UTF_8)
            return file.exists() && file.readText(Charsets.UTF_8) == value
        }
    }

    private lateinit var testFile: File
    private lateinit var testStorage: TestFileStorage
    private lateinit var repository: CompletionRepository
    private lateinit var workoutCompletion: WorkoutCompletion

    @Before
    fun setUp() {
        testFile = File(System.getProperty("java.io.tmpdir"), "test_file_completion_ledger_${System.currentTimeMillis()}.txt")
        testFile.deleteOnExit()
        testStorage = TestFileStorage(testFile)
        repository = CompletionRepository(testStorage)
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

    // 3. 디스크 레저 파일을 통해 1,505건 혼합(휴식 905건 + 운동 600건) 데이터를 영구 저장 후 새 저장소 및 새 Repository 인스턴스에서 재로드 시 필드 정합성과 월별/종류별 집계가 정확한지 검증
    @Test
    fun repositoryExceeding1001RecordsAcrossMonths_preservesExactCountsInNewInstance() {
        val novDateStr = "2026-11-15"
        val octDateStr = "2026-10-15"

        // 10월 기록 500건 저장 (휴식 300건 + 안구운동 200건)
        for (i in 1..300) {
            val uuid = java.util.UUID.randomUUID().toString()
            repository.save(CompletionRecord(uuid, System.currentTimeMillis(), octDateStr, "screen_rest"))
        }
        for (i in 1..100) {
            val uuid = java.util.UUID.randomUUID().toString()
            repository.save(CompletionRecord(uuid, System.currentTimeMillis(), octDateStr, "eye_gym"))
        }
        for (i in 1..100) {
            val uuid = java.util.UUID.randomUUID().toString()
            repository.save(CompletionRecord(uuid, System.currentTimeMillis(), octDateStr, "smooth_pursuit"))
        }

        // 11월 기록 1,005건 저장 (한 달 1,001건 초과! / 휴식 605건 + 안구운동 400건)
        for (i in 1..605) {
            val uuid = java.util.UUID.randomUUID().toString()
            repository.save(CompletionRecord(uuid, System.currentTimeMillis(), novDateStr, "screen_rest"))
        }
        for (i in 1..200) {
            val uuid = java.util.UUID.randomUUID().toString()
            repository.save(CompletionRecord(uuid, System.currentTimeMillis(), novDateStr, "eye_gym"))
        }
        for (i in 1..200) {
            val uuid = java.util.UUID.randomUUID().toString()
            repository.save(CompletionRecord(uuid, System.currentTimeMillis(), novDateStr, "smooth_pursuit"))
        }

        // 앱 재시작 상황 시뮬레이션: 동일 디스크 파일에서 새 TestFileStorage 및 새 CompletionRepository 인스턴스로 로드
        val newDiskStorage = TestFileStorage(testFile)
        val newFileRepo = CompletionRepository(newDiskStorage)
        val loadedRecords = newFileRepo.records()
        assertEquals(1505, loadedRecords.size)

        // 모든 필드 구조 검증 (샘플 레코드 필드 정합성)
        val sampleRecord = loadedRecords.first()
        assertEquals("screen_rest", sampleRecord.routineId)
        assertEquals(1, sampleRecord.routineVersion)
        assertEquals(30, sampleRecord.plannedDurationSeconds)
        assertEquals("user_confirmed", sampleRecord.confirmation)

        // 월별/종류별(휴식/운동) 집계 정확성 실측 검증
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

        // 11월 검증
        assertEquals(605, novStats.rests)
        assertEquals(400, novStats.exercises)

        // 10월 검증
        assertEquals(300, octStats.rests)
        assertEquals(200, octStats.exercises)
    }

    // 4. 세션 ID 형식이 유효한 UUID인지 검증
    @Test
    fun workoutSession_generatesValidUuidSessionId() {
        val sessionId = workoutCompletion.start("smooth_pursuit")
        val parsedUuid = java.util.UUID.fromString(sessionId)
        assertEquals(sessionId, parsedUuid.toString())
    }
}
