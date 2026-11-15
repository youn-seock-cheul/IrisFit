package com.example.myapplication

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenTimeFormattedTest {

    @Test
    fun testScreenTimeFormatting_HoursAndMinutes() {
        val minutes = 135 // 2 hours 15 mins
        val hours = minutes / 60
        val mins = minutes % 60
        val formatted = if (hours > 0) "${hours}시간 ${mins}분" else "${mins}분"

        assertEquals("2시간 15분", formatted)
    }

    @Test
    fun testScreenTimeFormatting_OnlyMinutes() {
        val minutes = 45
        val hours = minutes / 60
        val mins = minutes % 60
        val formatted = if (hours > 0) "${hours}시간 ${mins}분" else "${mins}분"

        assertEquals("45분", formatted)
    }

    @Test
    fun testScreenTimeFormatting_ZeroMinutes() {
        val minutes = 0
        val hours = minutes / 60
        val mins = minutes % 60
        val formatted = if (hours > 0) "${hours}시간 ${mins}분" else "${mins}분"

        assertEquals("0분", formatted)
    }
}
