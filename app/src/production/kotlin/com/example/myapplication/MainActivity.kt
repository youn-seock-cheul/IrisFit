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
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.session.GuidedSessionViewModel
import com.example.myapplication.session.EndSignalPlayer
import com.example.myapplication.reminder.ProcessStartReminderRecovery
import com.example.myapplication.ui.MainScreen
import com.example.myapplication.ui.EyeExamScreen
import com.example.myapplication.ui.EyeGymGameScreen
import com.example.myapplication.ui.SmoothPursuitGameScreen
import com.example.myapplication.ui.theme.MyApplicationTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    private val sessionModel by lazy { ViewModelProvider(this)[GuidedSessionViewModel::class.java] }

    private val signalPlayer by lazy { EndSignalPlayer(this) }

    override fun attachBaseContext(newBase: android.content.Context) {
        val wrapped = com.example.myapplication.data.LanguageManager.wrapContext(newBase)
        super.attachBaseContext(wrapped)
    }

    override fun onResume() {
        super.onResume()
        sessionModel.activityResumed()
        sessionModel.onEndSignal = { mode ->
            if (!signalPlayer.play(mode)) Toast.makeText(this,
                "기기 설정이나 지원 여부로 종료 신호가 제한될 수 있습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        sessionModel.onEndSignal = null
        signalPlayer.stop()
        sessionModel.activityPaused()
        super.onPause()
    }

    override fun onStop() {
        sessionModel.activityStopped(isChangingConfigurations)
        super.onStop()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ProcessStartReminderRecovery.observe(this)
        val initialScreen = intent?.getStringExtra("target_screen") ?: "home"
        
        try {
            if (sessionModel.todayRestCount > 0) {
                com.example.myapplication.data.DynamicIconManager.switchAppIcon(this, 1)
            }
        } catch (_: Exception) {}

        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var currentScreen by rememberSaveable { mutableStateOf(initialScreen) }
                    var pursuitModeIdx by rememberSaveable { mutableStateOf(0) }

                    when (currentScreen) {
                        "exam" -> EyeExamScreen(onBack = { currentScreen = "home" })
                        "gym" -> EyeGymGameScreen(
                            sessionModel = sessionModel,
                            onBack = { currentScreen = "home" },
                            onGameCompleted = { id ->
                                sessionModel.completeWorkout(id, "eye_gym") {
                                    Toast.makeText(this, "미니게임 완료 기록을 저장했습니다.", Toast.LENGTH_SHORT).show()
                                    try { com.example.myapplication.data.DynamicIconManager.switchAppIcon(this@MainActivity, 1) } catch (_: Exception) {}
                                }
                            }
                        )
                        "pursuit" -> SmoothPursuitGameScreen(
                            sessionModel = sessionModel,
                            initialMode = pursuitModeIdx,
                            onBack = { currentScreen = "home" },
                            onGameCompleted = { id ->
                                sessionModel.completeWorkout(id, "smooth_pursuit") {
                                    Toast.makeText(this, "미니게임 완료 기록을 저장했습니다.", Toast.LENGTH_SHORT).show()
                                    try { com.example.myapplication.data.DynamicIconManager.switchAppIcon(this@MainActivity, 1) } catch (_: Exception) {}
                                }
                            }
                        )
                        else -> MainScreen(
                            sessionModel = sessionModel,
                            onStartCameraSession = { startCameraResearch() },
                            onStopCameraSession = {
                                stopService(Intent().setClassName(this, "com.example.myapplication.service.EyeExerciseOverlayService"))
                            },
                            onNavigateToGym = {
                                currentScreen = "gym"
                            },
                            onNavigateToWorkoutRandom = {
                                if ((0..1).random() == 0) {
                                    currentScreen = "gym"
                                } else {
                                    pursuitModeIdx = (0..3).random()
                                    currentScreen = "pursuit"
                                }
                            },
                            onNavigateToPursuit = {
                                pursuitModeIdx = (0..3).random()
                                currentScreen = "pursuit"
                            },
                            onNavigateToPursuitMode = { modeIdx ->
                                pursuitModeIdx = modeIdx
                                currentScreen = "pursuit"
                            },
                            onNavigateToExam = { currentScreen = "exam" }
                        )
                    }
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
            startForegroundService(Intent().setClassName(this, "com.example.myapplication.service.EyeExerciseOverlayService"))
        } catch (_: RuntimeException) {
            Toast.makeText(this, "세션을 시작하지 못했습니다. 앱을 연 상태에서 다시 시도해 주세요.", Toast.LENGTH_LONG).show()
        }
    }
}
