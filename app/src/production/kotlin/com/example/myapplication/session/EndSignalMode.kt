package com.example.myapplication.session

enum class EndSignalMode(val label: String, val sound: Boolean, val vibration: Boolean) {
    SILENT("무음", false, false),
    SOUND("짧은 소리", true, false),
    VIBRATION("짧은 진동", false, true),
    BOTH("소리와 진동", true, true)
}
