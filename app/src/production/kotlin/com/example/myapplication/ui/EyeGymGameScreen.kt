package com.example.myapplication.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
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
    onGameCompleted: (String) -> Unit,
    isUnlocked: Boolean = true,
    onOpenPaywall: () -> Unit = {}
) {
    var gameState by rememberSaveable { mutableStateOf("READY") }
    var remainingSeconds by rememberSaveable { mutableIntStateOf(20) }
    var selectedMode by rememberSaveable { mutableIntStateOf(0) }
    var workoutId by rememberSaveable { mutableStateOf<String?>(null) }
    val flashAlpha = remember { Animatable(1f) }
    val rippleProgress = remember { Animatable(0f) }

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
        sessionModel.tr("focus_mode_pulse"),
        sessionModel.tr("focus_mode_flash"),
        sessionModel.tr("focus_mode_wave"),
        sessionModel.tr("focus_mode_dual")
    )

    val modeIcons = listOf(
        R.drawable.ic_ms_center_focus_strong,
        R.drawable.ic_ms_bolt,
        R.drawable.ic_ms_radio_button_unchecked,
        R.drawable.ic_ms_target
    )

    val scaleAnim = remember { Animatable(40f) }

    LaunchedEffect(gameState) {
        if (gameState == "PLAYING") {
            launch {
                when (selectedMode) {
                    1 -> {
                        flashAlpha.snapTo(0f)
                        while (gameState == "PLAYING") {
                            flashAlpha.animateTo(1f, animationSpec = tween(100))
                            delay(160)
                            flashAlpha.animateTo(0f, animationSpec = tween(100))
                            delay(240)
                        }
                    }
                    2 -> {
                        rippleProgress.snapTo(0f)
                        while (gameState == "PLAYING") {
                            rippleProgress.animateTo(1f, animationSpec = tween(1500))
                            rippleProgress.snapTo(0f)
                        }
                    }
                    else -> {
                        scaleAnim.snapTo(40f)
                        while (gameState == "PLAYING") {
                            scaleAnim.animateTo(130f, animationSpec = tween(2000))
                            scaleAnim.animateTo(40f, animationSpec = tween(2000))
                        }
                    }
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
            IrisTopBar(sessionModel.tr("gym_screen_title"), sessionModel.tr("gym_back"), ::leaveTraining)
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
                                icon = R.drawable.ic_ms_center_focus_strong,
                                title = sessionModel.tr("gym_ready_title"),
                                subtitle = sessionModel.tr("gym_focus_meta")
                            )
                            IrisBodyText(sessionModel.tr("gym_ready_desc"))

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
                            if (selectedMode == 1 || selectedMode == 2) {
                                IrisBodyText(sessionModel.tr(if (selectedMode == 1) "focus_mode_flash_desc" else "focus_mode_wave_desc"))
                            }

                            IrisPrimaryButton(
                                text = if (isUnlocked) sessionModel.tr("gym_start") else "${sessionModel.tr("gym_start")} 🔒",
                                icon = R.drawable.ic_ms_play_arrow,
                                enabled = sessionModel.recordsReady && !sessionModel.busy,
                                onClick = {
                                    if (isUnlocked) {
                                        workoutId = sessionModel.prepareWorkoutSession("eye_gym")
                                        remainingSeconds = 20
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
                    val tertiaryColor = MaterialTheme.colorScheme.tertiary
                    Text(sessionModel.tr("fmt_mode_seconds").format(modeNames[selectedMode], remainingSeconds), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(30.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(320.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                            .clipToBounds()
                            .testTag("focus_target"),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f

                            when (selectedMode) {
                                0 -> {
                                    drawCircle(
                                        color = primaryColor,
                                        radius = scaleAnim.value,
                                        center = Offset(centerX, centerY)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = scaleAnim.value * 0.3f,
                                        center = Offset(centerX, centerY)
                                    )
                                }
                                1 -> {
                                    drawCircle(
                                        color = primaryColor.copy(alpha = flashAlpha.value),
                                        radius = 30f,
                                        center = Offset(centerX, centerY)
                                    )
                                    drawCircle(
                                        color = Color.White.copy(alpha = flashAlpha.value),
                                        radius = 9f,
                                        center = Offset(centerX, centerY)
                                    )
                                }
                                2 -> {
                                    repeat(3) { index ->
                                        val phase = (rippleProgress.value + index / 3f) % 1f
                                        val radius = 24f + phase * 150f
                                        drawCircle(
                                            color = primaryColor.copy(alpha = (1f - phase) * 0.72f),
                                            radius = radius,
                                            center = Offset(centerX, centerY),
                                            style = Stroke(width = 5f)
                                        )
                                    }
                                    drawCircle(color = primaryColor, radius = 10f, center = Offset(centerX, centerY))
                                    drawCircle(color = Color.White, radius = 4f, center = Offset(centerX, centerY))
                                }
                                else -> {
                                    val r = scaleAnim.value
                                    drawCircle(color = primaryColor, radius = r, center = Offset(centerX - 80f, centerY))
                                    drawCircle(color = tertiaryColor, radius = 140f - r, center = Offset(centerX + 80f, centerY), style = Stroke(width = 6f))
                                    drawCircle(color = Color.White, radius = 10f, center = Offset(centerX - 80f, centerY))
                                    drawCircle(color = Color.White, radius = 10f, center = Offset(centerX + 80f, centerY))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        sessionModel.tr(
                            when (selectedMode) {
                                1 -> "focus_mode_flash_desc"
                                2 -> "focus_mode_wave_desc"
                                else -> "gym_gazing"
                            }
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.testTag("focus_guidance")
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
                            Text(sessionModel.tr("gym_finished"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                            if (sessionModel.busy) IrisBodyText(sessionModel.tr("workout_saving"))
                            else if (sessionModel.workoutSaved) IrisBodyText(sessionModel.tr("gym_finished_desc"))
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
