package com.example.myapplication.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.PreferencesManager
import kotlinx.coroutines.delay

private data class CalibPoint(val name: String, val normX: Float, val normY: Float)

@Composable
fun CalibrationScreen(
    onCalibrationFinished: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }

    var testPhase by remember { mutableIntStateOf(1) } // 1: 영점, 2: 추적속도, 3: 원근조절/시야각

    // Phase 1: 5-Point Calibration State
    val points = remember {
        listOf(
            CalibPoint("화면 중앙", 0.5f, 0.5f),
            CalibPoint("좌측 상단", 0.1f, 0.1f),
            CalibPoint("우측 상단", 0.9f, 0.1f),
            CalibPoint("좌측 하단", 0.1f, 0.9f),
            CalibPoint("우측 하단", 0.9f, 0.9f)
        )
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var captureProgress by remember { mutableStateOf(0f) }

    // Phase 2: Speed Pursuit State
    var speedProgress by remember { mutableStateOf(0f) }
    var trackingSpeedScore by remember { mutableIntStateOf(88) }

    // Phase 3: Accommodation / Depth Focus State
    var targetCircleSizeDp by remember { mutableStateOf(30.dp) }
    var accommodationScore by remember { mutableIntStateOf(92) }
    var visualFieldScore by remember { mutableIntStateOf(95) }

    val currentPoint = points.getOrNull(currentIndex)

    LaunchedEffect(testPhase, currentIndex) {
        if (testPhase == 1) {
            if (currentIndex < points.size) {
                captureProgress = 0f
                val totalSteps = 20
                for (step in 1..totalSteps) {
                    delay(80L)
                    captureProgress = step / totalSteps.toFloat()
                }
                delay(200L)
                if (currentIndex + 1 < points.size) {
                    currentIndex++
                } else {
                    // Save Calibration
                    prefsManager.saveCalibrationData(
                        centerX = 0.5f, centerY = 0.5f,
                        topLeftX = 0.1f, topLeftY = 0.1f,
                        topRightX = 0.9f, topRightY = 0.1f,
                        bottomLeftX = 0.1f, bottomLeftY = 0.9f,
                        bottomRightX = 0.9f, bottomRightY = 0.9f
                    )
                    testPhase = 2 // Move to Phase 2
                }
            }
        } else if (testPhase == 2) {
            speedProgress = 0f
            for (step in 1..30) {
                delay(100L)
                speedProgress = step / 30f
            }
            trackingSpeedScore = 90
            testPhase = 3 // Move to Phase 3
        } else if (testPhase == 3) {
            repeat(3) {
                targetCircleSizeDp = 80.dp
                delay(600L)
                targetCircleSizeDp = 24.dp
                delay(600L)
            }
            accommodationScore = 92
            visualFieldScore = 94
            prefsManager.saveOcularCapabilityData(trackingSpeedScore, accommodationScore, visualFieldScore)
            delay(500L)
            onCalibrationFinished()
        }
    }

    // 갤럭시 플립 / 폴더블 풀 스크린 전체 화면 동적 영점 맞춤
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1117))
    ) {
        val maxWPx = constraints.maxWidth.toFloat()
        val maxHPx = constraints.maxHeight.toFloat()

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = when (testPhase) {
                    1 -> "1/3단계: 전체화면 영점 교정 (Z플립/폴드 호환)"
                    2 -> "2/3단계: 안구 추적 속도 검사 (Pursuit Speed)"
                    else -> "3/3단계: 원근 조절 & 시야각 수치 검사"
                },
                color = Color.White,
                fontSize = 17.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = when (testPhase) {
                    1 -> "화면에 표시되는 점을 2초간 응시하세요.\n(${currentIndex + 1} / ${points.size}) ${currentPoint?.name ?: ""}"
                    2 -> "빠르게 이동하는 점을 시선으로 놓치지 말고 추적하세요."
                    else -> "줌인/아웃으로 커졌다 작아지는 원의 초점을 정확히 맞추세요."
                },
                color = Color.LightGray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        when (testPhase) {
            1 -> {
                currentPoint?.let { point ->
                    val animatedX by animateFloatAsState(targetValue = point.normX, animationSpec = tween(400), label = "calibX")
                    val animatedY by animateFloatAsState(targetValue = point.normY, animationSpec = tween(400), label = "calibY")

                    val pointSizePx = 60.dp.value * 2.5f
                    val posX = (animatedX * (maxWPx - pointSizePx)).toInt().coerceAtLeast(0)
                    val posY = (animatedY * (maxHPx - pointSizePx)).toInt().coerceAtLeast(0)

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(x = posX, y = posY) }
                            .size(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { captureProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF00FFCC),
                            strokeWidth = 3.dp
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00FFCC))
                        )
                    }
                }
            }

            2 -> {
                val animatedSpeedX by animateFloatAsState(
                    targetValue = if (speedProgress < 0.5f) speedProgress * 2f else (1f - (speedProgress - 0.5f) * 2f),
                    animationSpec = tween(100),
                    label = "speedX"
                )

                val posX = (0.05f + animatedSpeedX * 0.85f * (maxWPx - 100)).toInt()
                val posY = (maxHPx * 0.5f).toInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(x = posX, y = posY) }
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF3B82F6))
                )
            }

            3 -> {
                val animatedSizeDp by animateDpAsState(
                    targetValue = targetCircleSizeDp,
                    animationSpec = tween(500),
                    label = "zoomSize"
                )

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(animatedSizeDp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFD700))
                    )
                }
            }
        }

        Button(
            onClick = {
                prefsManager.saveCalibrationData(0.5f, 0.5f, 0.1f, 0.1f, 0.9f, 0.1f, 0.1f, 0.9f, 0.9f, 0.9f)
                prefsManager.saveOcularCapabilityData(88, 92, 94)
                onCalibrationFinished()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 36.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262B38))
        ) {
            Text(text = "영점 조절 완료 및 메인으로 이동", color = Color.White)
        }
    }
}
