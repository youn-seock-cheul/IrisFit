package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.myapplication.service.EyeExerciseOverlayService
import com.example.myapplication.ui.WellnessHome
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WellnessHome(
                        onStartCameraSession = { startCameraResearch() },
                        onStopCameraSession = {
                            stopService(Intent(this, EyeExerciseOverlayService::class.java))
                        }
                    )
                }
            }
        }
    }

    private fun startCameraResearch() {
        if (!BuildConfig.DEBUG) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
            !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "실험 준비에서 카메라 및 오버레이 권한을 선택해 주세요.", Toast.LENGTH_LONG).show()
            return
        }
        try {
            // User action in this visible Activity; never started by app-detection events.
            startForegroundService(Intent(this, EyeExerciseOverlayService::class.java))
        } catch (_: RuntimeException) {
            Toast.makeText(this, "세션을 시작하지 못했습니다. 앱을 연 상태에서 다시 시도해 주세요.", Toast.LENGTH_LONG).show()
        }
    }
}
