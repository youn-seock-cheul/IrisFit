package com.example.myapplication.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.session.GuidedSessionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EyeGymGameScreen(
    sessionModel: GuidedSessionViewModel,
    onBack: () -> Unit,
    onGameCompleted: () -> Unit
) {
    var gameState by remember { mutableStateOf("READY") } // READY, PLAYING, FINISHED
    var remainingSeconds by remember { mutableIntStateOf(20) }

    val scaleAnim = remember { Animatable(40f) }

    LaunchedEffect(gameState) {
        if (gameState == "PLAYING") {
            launch {
                while (gameState == "PLAYING") {
                    scaleAnim.animateTo(130f, animationSpec = tween(2000))
                    scaleAnim.animateTo(40f, animationSpec = tween(2000))
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
                title = { Text(sessionModel.tr("gym_screen_title"), fontWeight = FontWeight.Bold) },
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
                            Text(sessionModel.tr("gym_ready_title"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(sessionModel.tr("gym_ready_desc"))
                            Button(onClick = {
                                remainingSeconds = 20
                                gameState = "PLAYING"
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text(sessionModel.tr("gym_start"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
                "PLAYING" -> {
                    Text("${sessionModel.tr("gym_playing")} (${remainingSeconds}초)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(40.dp))
                    Box(
                        modifier = Modifier
                            .size(260.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = Color(0xFF00C853),
                                radius = scaleAnim.value,
                                center = Offset(size.width / 2f, size.height / 2f)
                            )
                        }
                        Text(sessionModel.tr("gym_gazing"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(40.dp))
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
                            Text(sessionModel.tr("gym_finished"), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text(sessionModel.tr("gym_finished_desc"))
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
