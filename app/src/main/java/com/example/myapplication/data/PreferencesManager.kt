package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var level: Int
        get() = prefs.getInt(KEY_LEVEL, 1)
        set(value) = prefs.edit().putInt(KEY_LEVEL, value).apply()

    var exp: Int
        get() = prefs.getInt(KEY_EXP, 0)
        set(value) = prefs.edit().putInt(KEY_EXP, value).apply()

    var isNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    // 5-Point Calibration Offsets (Normalized differences)
    fun saveCalibrationData(
        centerX: Float, centerY: Float,
        topLeftX: Float, topLeftY: Float,
        topRightX: Float, topRightY: Float,
        bottomLeftX: Float, bottomLeftY: Float,
        bottomRightX: Float, bottomRightY: Float
    ) {
        prefs.edit()
            .putFloat(KEY_CALIB_CENTER_X, centerX)
            .putFloat(KEY_CALIB_CENTER_Y, centerY)
            .putFloat(KEY_CALIB_TL_X, topLeftX)
            .putFloat(KEY_CALIB_TL_Y, topLeftY)
            .putFloat(KEY_CALIB_TR_X, topRightX)
            .putFloat(KEY_CALIB_TR_Y, topRightY)
            .putFloat(KEY_CALIB_BL_X, bottomLeftX)
            .putFloat(KEY_CALIB_BL_Y, bottomLeftY)
            .putFloat(KEY_CALIB_BR_X, bottomRightX)
            .putFloat(KEY_CALIB_BR_Y, bottomRightY)
            .putBoolean(KEY_HAS_CALIBRATED, true)
            .apply()
    }

    val hasCalibrated: Boolean
        get() = prefs.getBoolean(KEY_HAS_CALIBRATED, false)

    // Ocular Capability Scores
    fun saveOcularCapabilityData(trackingSpeed: Int, accommodationDepth: Int, visualFieldRange: Int) {
        prefs.edit()
            .putInt(KEY_OCULAR_SPEED, trackingSpeed)
            .putInt(KEY_OCULAR_ACCOM, accommodationDepth)
            .putInt(KEY_OCULAR_FIELD, visualFieldRange)
            .apply()
    }

    val trackingSpeedScore: Int
        get() = prefs.getInt(KEY_OCULAR_SPEED, 85)

    val accommodationScore: Int
        get() = prefs.getInt(KEY_OCULAR_ACCOM, 90)

    val visualFieldScore: Int
        get() = prefs.getInt(KEY_OCULAR_FIELD, 92)

    var savedChatHistory: String
        get() = prefs.getString(KEY_CHAT_HISTORY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CHAT_HISTORY, value).apply()

    var selectedTrackModeIndex: Int
        get() = prefs.getInt(KEY_TRACK_MODE, 0)
        set(value) = prefs.edit().putInt(KEY_TRACK_MODE, value).apply()

    var isDmsEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_DMS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_DMS_ENABLED, value).apply()

    var isEyeExerciseEnabled: Boolean
        get() = prefs.getBoolean(KEY_IS_EXERCISE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_IS_EXERCISE_ENABLED, value).apply()

    fun getCalibrationCenter(): Pair<Float, Float> {
        return Pair(
            prefs.getFloat(KEY_CALIB_CENTER_X, 0.5f),
            prefs.getFloat(KEY_CALIB_CENTER_Y, 0.5f)
        )
    }

    companion object {
        private const val PREF_NAME = "eye_exercise_prefs"
        private const val KEY_LEVEL = "user_level"
        private const val KEY_EXP = "user_exp"
        private const val KEY_HAS_CALIBRATED = "has_calibrated"
        private const val KEY_NOTIFICATIONS_ENABLED = "key_notifications_enabled"

        private const val KEY_CALIB_CENTER_X = "calib_center_x"
        private const val KEY_CALIB_CENTER_Y = "calib_center_y"
        private const val KEY_CALIB_TL_X = "calib_tl_x"
        private const val KEY_CALIB_TL_Y = "calib_tl_y"
        private const val KEY_CALIB_TR_X = "calib_tr_x"
        private const val KEY_CALIB_TR_Y = "calib_tr_y"
        private const val KEY_CALIB_BL_X = "calib_bl_x"
        private const val KEY_CALIB_BL_Y = "calib_bl_y"
        private const val KEY_CALIB_BR_X = "calib_br_x"
        private const val KEY_CALIB_BR_Y = "calib_br_y"

        private const val KEY_OCULAR_SPEED = "ocular_speed"
        private const val KEY_OCULAR_ACCOM = "ocular_accom"
        private const val KEY_OCULAR_FIELD = "ocular_field"
        private const val KEY_CHAT_HISTORY = "chat_history"
        private const val KEY_TRACK_MODE = "track_mode_index"
        private const val KEY_IS_DMS_ENABLED = "is_dms_enabled"
        private const val KEY_IS_EXERCISE_ENABLED = "is_exercise_enabled"
    }
}
