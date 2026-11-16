package com.example.myapplication.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.session.GuidedSessionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EyeGymGameScreen(
    sessionModel: GuidedSessionViewModel,
    onBack: () -> Unit,
    onGameCompleted: (String) -> Unit
) {
    var gameState by rememberSaveable { mutableStateOf("READY") }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(20) }
    var selectedMode by rememberSaveable { mutableIntStateOf(0) }
    var workoutId by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(sessionModel.activityVisible) {
        if (!sessionModel.activityVisible && gameState == "PLAYING") {
            gameState = "PAUSED"
        }
    }

    val modeNames = listOf(
        "🔍 Pulse & Zoom (맥동 줌)",
        "⚡ Rapid Flash (초고속 점멸)",
        "🌊 Wave Ripple (파동 리플)",
        "🎯 Dual-Target (교차 타겟)"
    )

    val modeIcons = listOf(
        R.drawable.ic_card_chart,
        R.drawable.ic_goal_streak,
        R.drawable.ic_card_lightbulb,
        R.drawable.ic_goal_battery
    )

    val scaleAnim = remember { Animatable(40f) }

    LaunchedEffect(gameState) {
        if (gameState == "PLAYING") {
            launch {
                while (gameState == "PLAYING") {
                    val duration = if (selectedMode == 1) 1000 else if (selectedMode == 2) 2500 else 2000
                    scaleAnim.animateTo(130f, animationSpec = tween(duration))
                    scaleAnim.animateTo(40f, animationSpec = tween(duration))
                }
            }
            while (remainingSeconds > 0 && gameState == "PLAYING") {
                delay(1000L)
                remainingSeconds--
            }
            if (remainingSeconds == 0) {
                gameState = "FINISHED"
                workoutId?.let(onGameCompleted)
            }
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onBack,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(sessionModel.tr("gym_back"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = sessionModel.tr("gym_screen_title"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (gameState) {
                "READY" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(sessionModel.tr("gym_ready_title"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(sessionModel.tr("gym_ready_desc"))

                            Text(sessionModel.tr("훈련 모드 선택:"), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                modeNames.forEachIndexed { idx, name ->
                                    val isSelected = selectedMode == idx
                                    Surface(
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedMode = idx }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = modeIcons[idx]),
                                                contentDescription = null,
                                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = sessionModel.tr(name),
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Button(enabled = sessionModel.recordsReady && !sessionModel.busy, onClick = {
                                workoutId = sessionModel.prepareWorkoutSession("eye_gym")
                                remainingSeconds = 20
                                gameState = "PLAYING"
                            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Text(sessionModel.tr("gym_start"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
                "PLAYING" -> {
                    Text(sessionModel.tr("${modeNames[selectedMode]} (${remainingSeconds}초)"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(30.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(320.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f

                            when (selectedMode) {
                                0, 1 -> {
                                    drawCircle(
                                        color = Color(0xFF00E676),
                                        radius = scaleAnim.value,
                                        center = Offset(centerX, centerY)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = scaleAnim.value * 0.3f,
                                        center = Offset(centerX, centerY)
                                    )
                                }
                                2 -> {
                                    val r = scaleAnim.value
                                    drawCircle(color = Color(0xFF00E676).copy(alpha = 0.3f), radius = r * 1.2f, center = Offset(centerX, centerY), style = Stroke(width = 4f))
                                    drawCircle(color = Color(0xFF00E676), radius = r, center = Offset(centerX, centerY))
                                    drawCircle(color = Color.White, radius = r * 0.4f, center = Offset(centerX, centerY))
                                }
                                else -> {
                                    val r = scaleAnim.value
                                    drawCircle(color = Color(0xFF00C853), radius = r, center = Offset(centerX - 80f, centerY))
                                    drawCircle(color = Color(0xFF29B6F6), radius = 140f - r, center = Offset(centerX + 80f, centerY), style = Stroke(width = 6f))
                                    drawCircle(color = Color.White, radius = 10f, center = Offset(centerX - 80f, centerY))
                                    drawCircle(color = Color.White, radius = 10f, center = Offset(centerX + 80f, centerY))
                                }
                            }
                        }
                        Text(sessionModel.tr("gym_gazing"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(onClick = { gameState = "READY" }, shape = RoundedCornerShape(12.dp)) {
                        Text(sessionModel.tr("gym_stop"), fontWeight = FontWeight.Bold)
                    }
                }
                "PAUSED" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(sessionModel.tr("workout_paused_title"), style = MaterialTheme.typography.titleLarge)
                            Button(onClick = { gameState = "PLAYING" }) {
                                Text(sessionModel.tr("workout_resume"))
                            }
                            TextButton(onClick = {
                                sessionModel.cancelWorkoutSession()
                                gameState = "READY"
                            }) {
                                Text(sessionModel.tr("workout_cancel"))
                            }
                        }
                    }
                }
                "FINISHED" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(sessionModel.tr("gym_finished"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            if (sessionModel.busy) Text("완료 기록을 저장하고 있어요.")
                            else if (sessionModel.workoutSaved) Text(sessionModel.tr("gym_finished_desc"))
                            else {
                                Text(sessionModel.error ?: "완료 기록을 저장하지 못했어요.")
                                Button(onClick = { workoutId?.let(onGameCompleted) }) { Text("저장 다시 시도") }
                            }
                            Button(enabled = !sessionModel.busy && sessionModel.workoutSaved, onClick = {
                                gameState = "READY"
                            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Text(sessionModel.tr("gym_restart"), fontWeight = FontWeight.Bold)
                            }
                            Button(onClick = onBack, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Text(sessionModel.tr("gym_home"), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
