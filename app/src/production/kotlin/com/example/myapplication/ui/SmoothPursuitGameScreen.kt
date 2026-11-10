package com.example.myapplication.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.session.GuidedSessionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmoothPursuitGameScreen(
    sessionModel: GuidedSessionViewModel,
    initialMode: Int = -1,
    onBack: () -> Unit,
    onGameCompleted: () -> Unit
) {
    var gameState by remember { mutableStateOf("READY") }
    var remainingSeconds by remember { mutableIntStateOf(20) }
    var angle by remember { mutableFloatStateOf(0f) }
    var selectedMode by remember { mutableIntStateOf(if (initialMode in 0..3) initialMode else 0) }

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
                onGameCompleted()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sessionModel.tr("pursuit_screen_title"), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Button(onClick = onBack) { Text(sessionModel.tr("gym_back")) }
                }
            )
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
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
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

                            Button(onClick = {
                                remainingSeconds = 20
                                angle = 0f
                                gameState = "PLAYING"
                            }, modifier = Modifier.fillMaxWidth()) {
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
                            .fillMaxWidth()
                            .height(300.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp)),
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
                                    // Infinity (∞)
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
                                    // Figure-8 (8자)
                                    for (i in 0..100) {
                                        val theta = i * 2f * Math.PI.toFloat() / 100f
                                        val x = centerX + rx * sin(theta)
                                        val y = centerY + ry * sin(2 * theta)
                                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }
                                    dotOffset = Offset(centerX + rx * sin(t), centerY + ry * sin(2 * t))
                                }
                                2 -> {
                                    // Circle (원형)
                                    for (i in 0..100) {
                                        val theta = i * 2f * Math.PI.toFloat() / 100f
                                        val x = centerX + rx * cos(theta)
                                        val y = centerY + ry * sin(theta)
                                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                    }
                                    dotOffset = Offset(centerX + rx * cos(t), centerY + ry * sin(t))
                                }
                                else -> {
                                    // Zig-Zag (지그재그) - Properly bounded inside canvas box
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
                    Button(onClick = { gameState = "READY" }) {
                        Text(sessionModel.tr("gym_stop"))
                    }
                }
                "FINISHED" -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(sessionModel.tr("pursuit_finished"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(sessionModel.tr("pursuit_finished_desc"))
                            Button(onClick = {
                                gameState = "READY"
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(sessionModel.tr("gym_restart"))
                            }
                            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                                Text(sessionModel.tr("gym_home"))
                            }
                        }
                    }
                }
            }
        }
    }
}
