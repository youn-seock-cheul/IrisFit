package com.example.myapplication.ui

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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.session.GuidedSessionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SmoothPursuitGameScreen(
    sessionModel: GuidedSessionViewModel,
    initialMode: Int = -1,
    onBack: () -> Unit,
    onGameCompleted: (String) -> Unit
) {
    var gameState by rememberSaveable { mutableStateOf("READY") }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(20) }
    var workoutId by rememberSaveable { mutableStateOf<String?>(null) }
    var angle by rememberSaveable { mutableFloatStateOf(0f) }
    var selectedMode by rememberSaveable { mutableIntStateOf(if (initialMode in 0..3) initialMode else 0) }
    LaunchedEffect(sessionModel.activityVisible) {
        if (!sessionModel.activityVisible && gameState == "PLAYING") {
            gameState = "PAUSED"
        }
    }

    val modeNames = listOf(
        "♾️ Infinity (∞) 궤적",
        "🔀 Figure-8 (8자) 궤적",
        "⭕ Circle (원형) 회전",
        "〰️ Zig-Zag (지그재그) 추적"
    )

    LaunchedEffect(gameState) {
        if (gameState == "PLAYING") {
            launch {
                while (gameState == "PLAYING") {
                    delay(16L)
                    angle += 0.03f
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
                        text = sessionModel.tr("pursuit_screen_title"),
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
                            Text(sessionModel.tr("pursuit_ready_title"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(sessionModel.tr("pursuit_ready_desc"))

                            if (initialMode < 0) {
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
                                            Text(
                                                text = sessionModel.tr(name),
                                                modifier = Modifier.padding(12.dp),
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }

                            Button(enabled = sessionModel.recordsReady && !sessionModel.busy, onClick = {
                                workoutId = sessionModel.prepareWorkoutSession("smooth_pursuit")
                                remainingSeconds = 20
                                angle = 0f
                                gameState = "PLAYING"
                            }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                Text(sessionModel.tr("pursuit_start"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
                "PLAYING" -> {
                    Text(sessionModel.tr("${modeNames[selectedMode]} (${remainingSeconds}초)"), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(20.dp))
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
                            val rx = size.width * 0.38f
                            val ry = size.height * 0.28f
                            val t = angle

                            val path = Path()
                            val dotOffset: Offset

                            when (selectedMode) {
                                0 -> {
                                    for (i in 0..100) {
                                        val theta = i * 2f * Math.PI.toFloat() / 100f
                                        val scale = 1 / (1 + sin(theta) * sin(theta))
                                        val x = centerX + rx * scale * cos(theta)
                                        val y = centerY + ry * scale * sin(theta) * cos(theta)
                                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }
                                    val scale = 1 / (1 + sin(t) * sin(t))
                                    dotOffset = Offset(centerX + rx * scale * cos(t), centerY + ry * scale * sin(t) * cos(t))
                                }
                                1 -> {
                                    for (i in 0..100) {
                                        val theta = i * 2f * Math.PI.toFloat() / 100f
                                        val x = centerX + rx * sin(theta)
                                        val y = centerY + ry * sin(2 * theta)
                                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }
                                    dotOffset = Offset(centerX + rx * sin(t), centerY + ry * sin(2 * t))
                                }
                                2 -> {
                                    for (i in 0..100) {
                                        val theta = i * 2f * Math.PI.toFloat() / 100f
                                        val x = centerX + rx * cos(theta)
                                        val y = centerY + ry * sin(theta)
                                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }
                                    dotOffset = Offset(centerX + rx * cos(t), centerY + ry * sin(t))
                                }
                                else -> {
                                    val padding = 40f
                                    val graphWidth = size.width - (padding * 2)
                                    val topY = centerY - ry * 0.7f
                                    val botY = centerY + ry * 0.7f

                                    for (i in 0..100) {
                                        val progress = i / 100f
                                        val x = padding + graphWidth * progress
                                        val y = if ((i / 25) % 2 == 0) topY else botY
                                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }

                                    val cycle = (t % (2 * Math.PI)).toFloat() / (2 * Math.PI).toFloat()
                                    val zx = padding + graphWidth * cycle
                                    val segmentIdx = (cycle * 4).toInt().coerceIn(0, 3)
                                    val zy = if (segmentIdx % 2 == 0) topY else botY
                                    dotOffset = Offset(zx, zy)
                                }
                            }

                            drawPath(path, color = Color.Gray.copy(alpha = 0.5f), style = Stroke(width = 4f))
                            drawCircle(color = Color(0xFF29B6F6), radius = 16f, center = dotOffset)
                            drawCircle(color = Color.White, radius = 6f, center = dotOffset)
                        }
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
                            Text(sessionModel.tr("pursuit_finished"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            if (sessionModel.busy) Text("완료 기록을 저장하고 있어요.")
                            else if (sessionModel.workoutSaved) Text(sessionModel.tr("pursuit_finished_desc"))
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
