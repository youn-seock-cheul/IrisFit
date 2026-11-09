package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences

object RelayPreferences {
    private const val PREF_NAME = "iris_relay_prefs_v1"
    private const val KEY_NICKNAME = "key_nickname"
    private const val KEY_EMOJI = "key_emoji"
    private const val KEY_GOAL = "key_goal"
    private const val KEY_CHEER_COUNT = "key_cheer_count"

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getNickname(context: Context, default: String = "맑은눈토끼"): String {
        return prefs(context).getString(KEY_NICKNAME, default) ?: default
    }

    fun setNickname(context: Context, value: String) {
        prefs(context).edit().putString(KEY_NICKNAME, value).apply()
    }

    fun getEmoji(context: Context, default: String = "🐰"): String {
        return prefs(context).getString(KEY_EMOJI, default) ?: default
    }

    fun setEmoji(context: Context, value: String) {
        prefs(context).edit().putString(KEY_EMOJI, value).apply()
    }

    fun getGoal(context: Context, default: String = "하루 3회 30초 눈 휴식 완오! 🔥"): String {
        return prefs(context).getString(KEY_GOAL, default) ?: default
    }

    fun setGoal(context: Context, value: String) {
        prefs(context).edit().putString(KEY_GOAL, value).apply()
    }

    fun getCheerCount(context: Context, default: Int = 42): Int {
        return prefs(context).getInt(KEY_CHEER_COUNT, default)
    }

    fun setCheerCount(context: Context, value: Int) {
        prefs(context).edit().putInt(KEY_CHEER_COUNT, value).apply()
    }
}
