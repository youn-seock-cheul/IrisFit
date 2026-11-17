package com.example.myapplication

import com.example.myapplication.session.CompletionRecord
import com.example.myapplication.session.RestHistory
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Test

class RestHistoryTest {
    private fun record(id: Int, date: String, routine: String) =
        CompletionRecord(UUID(0, id.toLong()).toString(), id.toLong(), date, routine)

    @Test fun monthSeparatesRestAndExerciseAndExcludesFutureAndOtherMonths() {
        val records = listOf(
            record(1, "2024-02-01", "screen_rest"),
            record(2, "2024-02-01", "eye_gym"),
            record(3, "2024-02-29", "smooth_pursuit"),
            record(4, "2024-03-01", "screen_rest")
        )
        val result = RestHistory.month(records, YearMonth.of(2024, 2), LocalDate.parse("2024-02-28"))
        assertEquals(1, result.rests)
        assertEquals(1, result.exercises)
        assertEquals(1, result.activeDays)
        assertEquals(2, RestHistory.month(records, YearMonth.of(2024, 2), LocalDate.parse("2024-02-29")).activeDays)
    }

    @Test fun segmentsEndTodayAndCountBoundaryDaysOnce() {
        val records = listOf(record(1, "2024-02-07", "screen_rest"),
            record(2, "2024-02-08", "eye_gym"), record(3, "2024-02-09", "eye_gym"))
        val segments = RestHistory.monthSegments(records, LocalDate.parse("2024-02-08"))
        assertEquals(listOf(1..7, 8..8), segments.map { it.first })
        assertEquals(listOf(1, 1), segments.map { it.second.rests + it.second.exercises })
    }
}
