package com.example.myapplication.data

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {
    private const val PREF_NAME = "iris_fit_language_pref"
    private const val KEY_LANG = "key_language"

    fun getSelectedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANG, "") ?: ""
    }

    fun setSelectedLanguage(context: Context, langCode: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANG, langCode).apply()
    }

    fun wrapContext(context: Context): Context {
        val langCode = getSelectedLanguage(context)
        if (langCode.isEmpty()) return context
        val locale = Locale.forLanguageTag(langCode)
        Locale.setDefault(locale)

        val res = context.resources
        val config = Configuration(res.configuration)
        config.setLocale(locale)
        res.updateConfiguration(config, res.displayMetrics)
        return context.createConfigurationContext(config)
    }
}
