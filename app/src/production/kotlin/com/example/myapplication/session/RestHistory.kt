package com.example.myapplication.session

import java.time.LocalDate
import java.time.YearMonth

data class RestCounts(val rests: Int, val exercises: Int, val activeDays: Int)

object RestHistory {
    fun currentRestStreak(records: List<CompletionRecord>, today: LocalDate): Int {
        val restDays = records.asSequence()
            .filter { it.routineId == "screen_rest" }
            .map { LocalDate.parse(it.completedLocalDate) }
            .filter { !it.isAfter(today) }
            .toSet()
        var day = if (today in restDays) today else today.minusDays(1)
        var streak = 0
        while (day in restDays) {
            streak++
            day = day.minusDays(1)
        }
        return streak
    }

    fun counts(records: List<CompletionRecord>, from: LocalDate, through: LocalDate): RestCounts {
        require(!through.isBefore(from))
        val selected = records.filter {
            val date = LocalDate.parse(it.completedLocalDate)
            !date.isBefore(from) && !date.isAfter(through)
        }
        return RestCounts(
            rests = selected.count { it.routineId == "screen_rest" },
            exercises = selected.count { it.routineId != "screen_rest" },
            activeDays = selected.map { it.completedLocalDate }.distinct().size
        )
    }

    fun month(records: List<CompletionRecord>, month: YearMonth, through: LocalDate): RestCounts =
        counts(records, month.atDay(1), minOf(month.atEndOfMonth(), through))

    fun monthSegments(records: List<CompletionRecord>, today: LocalDate): List<Pair<IntRange, RestCounts>> {
        return (1..today.dayOfMonth step 7).map { first ->
            val last = minOf(first + 6, today.dayOfMonth)
            (first..last) to counts(records, today.withDayOfMonth(first), today.withDayOfMonth(last))
        }
    }
}
