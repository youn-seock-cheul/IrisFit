package com.example.myapplication.data

import android.util.Log

object AnalyticsManager {
    private const val TAG = "IrisFitAnalytics"

    fun logEvent(eventName: String, params: Map<String, Any> = emptyMap()) {
        Log.d(TAG, "Analytics Event: $eventName | Params: $params")
        // Production: FirebaseAnalytics.getInstance(context).logEvent(...)
    }

    fun recordException(throwable: Throwable) {
        Log.e(TAG, "Crashlytics Exception Captured: ${throwable.localizedMessage}", throwable)
        // Production: FirebaseCrashlytics.getInstance().recordException(throwable)
    }
}
