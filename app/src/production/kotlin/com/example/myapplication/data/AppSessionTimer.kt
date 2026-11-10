package com.example.myapplication.data

import android.content.Context
import java.time.LocalDate

object AppSessionTimer {
    private const val PREF_NAME = "iris_session_timer_v1"
    private const val KEY_DATE = "key_date"
    private const val KEY_ACCUMULATED_MINS = "key_accumulated_mins"

    private var sessionStartTime = System.currentTimeMillis()

    fun getActiveMinutes(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val today = LocalDate.now().toString()
        val savedDate = prefs.getString(KEY_DATE, "")

        val elapsedMs = System.currentTimeMillis() - sessionStartTime
        val elapsedMins = (elapsedMs / (1000 * 60)).toInt()

        if (savedDate != today) {
            prefs.edit()
                .putString(KEY_DATE, today)
                .putInt(KEY_ACCUMULATED_MINS, 0)
                .apply()
            sessionStartTime = System.currentTimeMillis()
            return elapsedMins.coerceAtLeast(1)
        }

        val baseMins = prefs.getInt(KEY_ACCUMULATED_MINS, 0)
        return (baseMins + elapsedMins).coerceAtLeast(5) // Ensures active drain even on fresh launch
    }
}
