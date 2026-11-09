package com.example.myapplication.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Retained only as a migration stub. No manifest registration or app-event collection.
 * Never start camera services from accessibility events.
 */
class YouTubeDetectionAccessibilityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        disableSelf()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit
    override fun onInterrupt() = Unit
}
