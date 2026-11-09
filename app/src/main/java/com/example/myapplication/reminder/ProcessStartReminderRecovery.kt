package com.example.myapplication.reminder

import android.content.Context
import android.widget.Toast

/** Re-register a remaining reminder when the user opens the app, without resetting its time. */
object ProcessStartReminderRecovery {
    private var restoredThisProcess = false

    // MainActivity calls this before reading the timer UI; rotation must not re-register it.
    fun observe(context: Context) {
        if (restoredThisProcess) return
        restoredThisProcess = true
        val scheduler = BreakReminderScheduler(context.applicationContext)
        try {
            scheduler.restorePendingReminder()
        } catch (_: RuntimeException) {
            scheduler.cancel()
            Toast.makeText(context.applicationContext,
                "이전 알림을 복원하지 못했습니다. 타이머를 다시 시작해 주세요.",
                Toast.LENGTH_LONG).show()
        }
    }
}
