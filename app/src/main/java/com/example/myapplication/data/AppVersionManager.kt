package com.example.myapplication.data

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri

object AppVersionManager {
    fun getCurrentVersionName(context: Context): String =
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "알 수 없음"

    /** The store determines availability; no invented remote version or APK sideloading. */
    fun openStoreListing(context: Context) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, "market://details?id=${context.packageName}".toUri())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW,
                    "https://play.google.com/store/apps/details?id=${context.packageName}".toUri())
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(context, "스토어를 열 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
