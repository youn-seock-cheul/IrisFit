package com.example.myapplication.ui

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
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
fun SmoothPursuitGameScreen(
    sessionModel: GuidedSessionViewModel,
    initialMode: Int = -1,
    onBack: () -> Unit,
    onGameCompleted: (String) -> Unit,
    isUnlocked: Boolean = true,
    onOpenPaywall: () -> Unit = {}
) {
    var gameState by rememberSaveable { mutableStateOf("READY") }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(20) }
    var workoutId by rememberSaveable { mutableStateOf<String?>(null) }
    var angle by remember { mutableFloatStateOf(0f) }
    var selectedMode by rememberSaveable { mutableIntStateOf(if (initialMode in 0..3) initialMode else 0) }
    fun leaveTraining() {
        sessionModel.cancelWorkoutSession()
        gameState = "READY"
        workoutId = null
        onBack()
    }

    BackHandler(onBack = ::leaveTraining)

    LaunchedEffect(sessionModel.activityVisible) {
        if (!sessionModel.activityVisible && gameState == "PLAYING") {
            gameState = "PAUSED"
        }
    }

    val modeNames = listOf(
        sessionModel.tr("pursuit_mode_infinity"),
        sessionModel.tr("pursuit_mode_figure8"),
        sessionModel.tr("pursuit_mode_circle"),
        sessionModel.tr("pursuit_mode_s_curve")
    )

    val modeIcons = listOf(
        R.drawable.ic_ms_all_inclusive,
        R.drawable.ic_ms_gesture,
        R.drawable.ic_ms_radio_button_unchecked,
        R.drawable.ic_ms_show_chart
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
            IrisTopBar(sessionModel.tr("pursuit_screen_title"), sessionModel.tr("gym_back"), ::leaveTraining)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .then(if (gameState == "PLAYING") Modifier.verticalScroll(rememberScrollState()) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (gameState) {
                "READY" -> {
                    IrisCard {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            IrisCardHeader(
                                icon = modeIcons[selectedMode],
                                title = sessionModel.tr("pursuit_ready_title"),
                                subtitle = if (initialMode < 0) sessionModel.tr("gym_pursuit_meta") else modeNames[selectedMode]
                            )
                            IrisBodyText(sessionModel.tr("pursuit_ready_desc"))

                            if (initialMode < 0) {
                                Text(sessionModel.tr("training_mode_select"), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    modeNames.forEachIndexed { idx, name ->
                                        IrisModeOption(
                                            icon = modeIcons[idx],
                                            label = name,
                                            selected = selectedMode == idx,
                                            onClick = { selectedMode = idx }
                                        )
                                    }
                                }
                            }

                            IrisPrimaryButton(
                                text = if (isUnlocked) sessionModel.tr("pursuit_start") else "${sessionModel.tr("pursuit_start")} 🔒",
                                icon = R.drawable.ic_ms_play_arrow,
                                enabled = sessionModel.recordsReady && !sessionModel.busy,
                                onClick = {
                                    if (isUnlocked) {
                                        workoutId = sessionModel.prepareWorkoutSession("smooth_pursuit")
                                        remainingSeconds = 20
                                        angle = 0.6f
                                        gameState = "PLAYING"
                                    } else {
                                        onOpenPaywall()
                                    }
                                }
                            )
                        }
                    }
                }
                "PLAYING" -> {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    Text(sessionModel.tr("fmt_mode_seconds").format(modeNames[selectedMode], remainingSeconds), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(
                        sessionModel.tr(pursuitPlanetLocalizationKey(selectedMode)),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(320.dp)
                            .background(Color(0xFF07111F), RoundedCornerShape(20.dp))
                            .clipToBounds(),
                        contentAlignment = Alignment.Center
                    ) {
                        SatelliteOrbitCanvas(
                            angle = angle,
                            mode = selectedMode,
                            orbitColor = primaryColor,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        sessionModel.tr("pursuit_playing"),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(
                        onClick = {
                            sessionModel.cancelWorkoutSession()
                            workoutId = null
                            remainingSeconds = 20
                            gameState = "READY"
                        },
                        modifier = Modifier.height(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(sessionModel.tr("gym_stop"), fontWeight = FontWeight.SemiBold)
                    }
                }
                "PAUSED" -> {
                    IrisCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(sessionModel.tr("workout_paused_title"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            IrisPrimaryButton(
                                text = sessionModel.tr("workout_resume"),
                                icon = R.drawable.ic_ms_play_arrow,
                                onClick = { gameState = "PLAYING" }
                            )
                            TextButton(onClick = {
                                sessionModel.cancelWorkoutSession()
                                gameState = "READY"
                            }) {
                                Text(sessionModel.tr("workout_cancel"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                "FINISHED" -> {
                    IrisCard {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_ms_check_circle_fill),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(sessionModel.tr("pursuit_finished"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            if (sessionModel.busy) IrisBodyText(sessionModel.tr("workout_saving"))
                            else if (sessionModel.workoutSaved) IrisBodyText(sessionModel.tr("pursuit_finished_desc"))
                            else {
                                Text(sessionModel.error ?: sessionModel.tr("workout_save_failed"), color = MaterialTheme.colorScheme.error)
                                IrisTonalButton(
                                    text = sessionModel.tr("workout_retry_save"),
                                    icon = null,
                                    onClick = { workoutId?.let(onGameCompleted) }
                                )
                            }
                            IrisPrimaryButton(
                                text = sessionModel.tr("gym_restart"),
                                icon = R.drawable.ic_ms_replay,
                                onClick = { gameState = "READY" },
                                enabled = !sessionModel.busy && sessionModel.workoutSaved
                            )
                            IrisTonalButton(
                                text = sessionModel.tr("gym_home"),
                                icon = R.drawable.ic_ms_home,
                                onClick = ::leaveTraining,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    }
}
