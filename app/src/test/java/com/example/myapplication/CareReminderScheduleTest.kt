package com.example.myapplication

import com.example.myapplication.reminder.CareReminderScheduler
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class CareReminderScheduleTest {
    private fun time(value: String) = ZonedDateTime.parse(value)

    @Test fun defaultWindowIncludesStartButExcludesEnd() {
        val start = time("2026-11-17T08:00:00+09:00[Asia/Seoul]")
        assertEquals(start, CareReminderScheduler.nextAllowedTime(start, 0))
        assertEquals(start, CareReminderScheduler.nextAllowedTime(start.minusMinutes(1), 0))
        val end = time("2026-11-17T22:00:00+09:00[Asia/Seoul]")
        assertEquals(start.plusDays(1), CareReminderScheduler.nextAllowedTime(end, 0))
    }

    @Test fun otherWindowsAndDaylightSavingUseLocalTime() {
        val night = time("2026-03-07T22:00:00-05:00[America/New_York]")
        assertEquals(time("2026-03-08T09:00:00-04:00[America/New_York]"),
            CareReminderScheduler.nextAllowedTime(night, 1))
        assertEquals(night, CareReminderScheduler.nextAllowedTime(night, 2))
    }
}
