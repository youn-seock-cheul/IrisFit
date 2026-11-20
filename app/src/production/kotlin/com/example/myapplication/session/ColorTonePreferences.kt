package com.example.myapplication.session

import com.example.myapplication.ui.theme.AppColorTone
import java.io.IOException

class ColorTonePreferences(private val storage: CompletionStorage) {
    fun read(): AppColorTone {
        val value = storage.read()
        if (value.isEmpty()) return AppColorTone.GREEN
        return requireNotNull(AppColorTone.entries.firstOrNull { value == "v1,${it.storageId}" }) {
            "Invalid color tone preference"
        }
    }

    fun save(tone: AppColorTone) {
        if (!storage.write("v1,${tone.storageId}")) throw IOException("Color tone preference was not saved")
    }
}
