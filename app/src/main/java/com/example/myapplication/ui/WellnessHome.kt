package com.example.myapplication.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.AppVersionManager
import com.example.myapplication.reminder.BreakReminderScheduler
import kotlinx.coroutines.delay

@Composable
fun WellnessHome(onStartCameraSession: () -> Unit, onStopCameraSession: () -> Unit) {
    val context = LocalContext.current
    val scheduler = remember { BreakReminderScheduler(context) }
    var deadline by remember { mutableLongStateOf(scheduler.deadline()) }
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var showPrivacy by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var showCameraDisclosure by remember { mutableStateOf(false) }
    var showOverlayDisclosure by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun startReminder() {
        try {
            scheduler.start()
            deadline = scheduler.deadline()
            error = null
        } catch (_: RuntimeException) {
            scheduler.cancel()
            deadline = 0L
            error = "알림을 예약하지 못했습니다. 다시 시도해 주세요."
        }
    }

    val notificationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startReminder()
        else error = "알림 권한 없이도 화면 내 타이머를 사용할 수 있습니다."
    }
    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) error = "카메라 권한을 허용하지 않았습니다. 휴식 타이머는 계속 사용할 수 있습니다."
        // Start always requires another explicit button press from the visible Activity.
    }

    LaunchedEffect(Unit) {
        while (true) {
            now = SystemClock.elapsedRealtime()
            deadline = scheduler.deadline()
            notificationsEnabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
            delay(1000L)
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("EyeOverlay", style = MaterialTheme.typography.headlineMedium)
            Text("화면 휴식 습관", style = MaterialTheme.typography.titleMedium)
            Text("계정이나 카메라 없이 시작하세요. 약 20분 후 한 번 휴식을 알려드립니다.")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val remaining = ((deadline - now).coerceAtLeast(0L) + 999L) / 1000L
                    Text(
                        when {
                            deadline == 0L -> "휴식 알림을 시작해 보세요"
                            remaining == 0L -> "휴식 시간입니다. 화면에서 눈을 떼고 편안하게 먼 곳을 바라보세요."
                            else -> "다음 휴식까지 ${remaining / 60}분 ${remaining % 60}초"
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    Button(onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                            PackageManager.PERMISSION_GRANTED) startReminder()
                        else notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }) { Text(if (deadline == 0L) "20분 휴식 알림 시작" else "다음 20분 알림 시작") }
                    OutlinedButton(onClick = { startReminder() }) { Text("권한 없이 화면 내 타이머 시작") }
                    if (deadline != 0L) {
                        OutlinedButton(onClick = {
                            scheduler.cancel()
                            deadline = 0L
                        }) { Text("타이머와 알림 종료") }
                    }
                    Text("절전 상태에서는 시스템이 알림을 늦출 수 있습니다. 정확한 시각 알람이 아니며 자동 반복하지 않습니다. 재부팅·강제 종료 후에는 다시 시작해 주세요.")
                    if (!notificationsEnabled) {
                        Text("시스템 알림이 꺼져 있습니다. 타이머는 앱 화면에서 확인할 수 있습니다.")
                        TextButton(onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                            } catch (_: RuntimeException) {
                                error = "알림 설정을 열 수 없습니다."
                            }
                        }) { Text("알림 설정 열기") }
                    }
                }
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Text("이 앱은 의료기기가 아니며 질환을 진단·치료·완치·예방하지 않습니다. 시력·안구건조·졸음·운전 안전을 측정하지 않습니다. 불편이나 시야 변화가 있다면 사용을 중단하고 의료 전문가와 상담하세요. 운전 중에는 조작하지 마세요.")
            TextButton(onClick = { showPrivacy = true }) { Text("개인정보 처리 안내") }
            TextButton(onClick = { showDelete = true }) { Text("이 기기의 앱 기록 전체 삭제") }
            TextButton(onClick = { AppVersionManager.openStoreListing(context) }) {
                Text("Google Play에서 업데이트 확인")
            }
            Text("버전 ${AppVersionManager.getCurrentVersionName(context)}")

            if (BuildConfig.DEBUG) {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("개발용 카메라·오버레이 실험", style = MaterialTheme.typography.titleMedium)
                        Text("정식 판매 빌드에는 포함되지 않습니다. 얼굴/눈 신호 확인만 하며 시선·피로도·졸음을 측정하지 않습니다. 최대 2분 후 종료됩니다. 운전 중 사용하지 마세요.")
                        Button(onClick = { showCameraDisclosure = true }) { Text("카메라 실험 준비") }
                        OutlinedButton(onClick = { showOverlayDisclosure = true }) { Text("오버레이 실험 준비") }
                        Button(onClick = {
                            onStartCameraSession()
                        }) { Text("카메라 실험 세션 시작") }
                        OutlinedButton(onClick = onStopCameraSession) { Text("카메라 실험 세션 종료") }
                    }
                }
            }
        }
    }

    if (showPrivacy) PrivacyPolicyDialog(onDismiss = { showPrivacy = false })
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("이 기기의 앱 기록을 삭제할까요?") },
            text = { Text("타이머, 기존 보정값·점수·대화 기록을 삭제하고 카메라 세션을 종료합니다. 삭제한 기록은 복구할 수 없습니다.") },
            confirmButton = {
                TextButton(onClick = {
                    onStopCameraSession()
                    scheduler.cancel()
                    context.getSharedPreferences("eye_exercise_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                    java.io.File(context.cacheDir, "eyeoverlay-update.apk").delete()
                    deadline = 0L
                    showDelete = false
                    Toast.makeText(context, "기기 내 기록을 삭제했습니다.", Toast.LENGTH_SHORT).show()
                }) { Text("전체 삭제") }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("취소") } }
        )
    }
    if (BuildConfig.DEBUG && showCameraDisclosure) {
        AlertDialog(
            onDismissRequest = { showCameraDisclosure = false },
            title = { Text("카메라 실험 데이터 안내") },
            text = { Text("사용자가 세션을 시작한 동안 전면 카메라 프레임에서 얼굴/눈 신호를 기기 내 분석합니다. 앱은 프레임을 파일로 저장하거나 서버로 업로드하지 않습니다. 화면이 꺼지거나 2분이 지나면 종료합니다. 알림과 앱에서 즉시 중지할 수 있습니다. 거절해도 휴식 타이머는 사용할 수 있습니다.") },
            confirmButton = { TextButton(onClick = {
                showCameraDisclosure = false
                cameraPermission.launch(Manifest.permission.CAMERA)
            }) { Text("계속해서 권한 선택") } },
            dismissButton = { TextButton(onClick = { showCameraDisclosure = false }) { Text("사용하지 않기") } }
        )
    }
    if (BuildConfig.DEBUG && showOverlayDisclosure) {
        AlertDialog(
            onDismissRequest = { showOverlayDisclosure = false },
            title = { Text("다른 앱 위 표시 안내") },
            text = { Text("실험 세션이 켜져 있음을 알리는 작은 배너를 다른 앱 위에 표시합니다. 다른 앱의 내용이나 입력을 읽지 않습니다. 세션은 앱이나 알림에서 종료할 수 있습니다.") },
            confirmButton = { TextButton(onClick = {
                showOverlayDisclosure = false
                try {
                    context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")))
                } catch (_: RuntimeException) {
                    error = "오버레이 설정을 열 수 없습니다."
                }
            }) { Text("설정에서 선택") } },
            dismissButton = { TextButton(onClick = { showOverlayDisclosure = false }) { Text("사용하지 않기") } }
        )
    }
}
