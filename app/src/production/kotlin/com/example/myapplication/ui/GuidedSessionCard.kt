package com.example.myapplication.ui

import androidx.activity.compose.BackHandler
import com.example.myapplication.R
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.foundation.background
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.ui.geometry.Offset
import com.example.myapplication.session.MarkerPath
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.session.GuidedSessionViewModel
import com.example.myapplication.session.SessionPhase
import androidx.compose.material3.Slider
import kotlin.math.roundToInt
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.delay

@Composable
private fun MarkerSurface(elapsedMs: Long, speedStep: Int, rangeStep: Int, preview: Boolean = false) {
    val color = MaterialTheme.colorScheme.primary
    Canvas(Modifier.fillMaxWidth().height(180.dp).testTag("marker_surface")) {
        val radius = minOf(10.dp.toPx(), size.width / 2f, size.height / 2f)
        if (preview) {
            val cycle = MarkerPath.cycleMs(speedStep)
            val left = MarkerPath.centerX(cycle * 3 / 4, size.width, 28.dp.toPx(), speedStep, rangeStep)
            val right = MarkerPath.centerX(cycle / 4, size.width, 28.dp.toPx(), speedStep, rangeStep)
            drawLine(color.copy(alpha = 0.3f), Offset(left, size.height / 2f),
                Offset(right, size.height / 2f), strokeWidth = 4.dp.toPx())
        }
        drawCircle(color, radius, Offset(
            MarkerPath.centerX(elapsedMs, size.width, 28.dp.toPx(), speedStep, rangeStep), size.height / 2f))
    }
}

@Composable
private fun SessionIcon(@DrawableRes icon: Int) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(painterResource(icon), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
    }
}

@Composable
fun GuidedSessionCard(model: GuidedSessionViewModel) {
    val state = model.state
    BackHandler(enabled = state.phase != SessionPhase.HOME) { model.back() }
    LaunchedEffect(state.phase) {
        while (state.phase == SessionPhase.RUNNING) {
            model.tick()
            delay(if (model.isMarker) 16L else 200L)
        }
    }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        when (state.phase) {
            SessionPhase.HOME -> {
                IrisCardHeader(icon = R.drawable.ic_ms_self_improvement, title = model.tr("session_home_title"))
                IrisPrimaryButton(
                    text = model.tr("session_home_rest_btn"),
                    icon = R.drawable.ic_ms_play_arrow,
                    onClick = model::prepare,
                    enabled = model.recordsReady && !model.busy
                )
                IrisTonalButton(
                    text = model.tr("session_home_marker_btn"),
                    icon = R.drawable.ic_ms_visibility,
                    onClick = { if (model.recordsReady && !model.busy) model.prepareMarker() },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            SessionPhase.READY -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SessionIcon(if (model.isMarker) R.drawable.ic_ms_visibility else R.drawable.ic_ms_self_improvement)
                    Text(
                        text = if (model.isMarker) model.tr("session_ready_marker_title") else model.tr("session_ready_rest_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (model.isMarker) model.tr("session_ready_marker_desc") else model.tr("session_ready_rest_desc"),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    IrisPrimaryButton(
                        text = model.tr("session_start_btn"),
                        icon = R.drawable.ic_ms_play_arrow,
                        onClick = model::start,
                        enabled = !model.busy
                    )
                    TextButton(onClick = model::home) {
                        Text(model.tr("session_later"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            SessionPhase.RUNNING, SessionPhase.PAUSED -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (model.isMarker) {
                        MarkerSurface(state.elapsedMs, model.markerSpeedStep, model.markerRangeStep)
                    } else {
                        Box(
                            modifier = Modifier.size(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { state.remainingSeconds.toFloat() / 30f },
                                modifier = Modifier.fillMaxSize(),
                                strokeWidth = 10.dp,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                strokeCap = StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${state.remainingSeconds}s", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text(model.tr("session_resting"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Text(
                        text = if (state.phase == SessionPhase.PAUSED) model.tr("session_paused")
                        else if (model.isMarker) model.tr("session_marker_hint")
                        else model.tr("session_rest_hint"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (state.phase == SessionPhase.RUNNING) {
                            IrisTonalButton(text = model.tr("session_pause"), icon = null, onClick = model::pause, modifier = Modifier.weight(1f))
                        } else {
                            IrisTonalButton(text = model.tr("session_resume"), icon = R.drawable.ic_ms_play_arrow, onClick = model::resume, modifier = Modifier.weight(1f))
                        }
                        Button(
                            onClick = model::end,
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(model.tr("session_stop"), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            SessionPhase.CONFIRM -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (model.isMarker) {
                        SessionIcon(R.drawable.ic_ms_check_circle)
                        Text(model.tr("session_marker_done_title"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(model.tr("session_marker_done_desc"), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        IrisPrimaryButton(text = model.tr("session_home_btn"), icon = R.drawable.ic_ms_home, onClick = model::home)
                    } else {
                        SessionIcon(R.drawable.ic_ms_check_circle)
                        Text(model.tr("session_confirm_title"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        Text(model.tr("session_confirm_desc"), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                        IrisPrimaryButton(
                            text = if (model.busy) model.tr("session_saving") else model.tr("session_confirm_btn"),
                            icon = R.drawable.ic_ms_check_circle,
                            onClick = model::confirm,
                            enabled = !model.busy
                        )
                        TextButton(onClick = model::end, enabled = !model.busy) {
                            Text(model.tr("session_skip_record"), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            SessionPhase.COMPLETED, SessionPhase.ENDED -> {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    SessionIcon(if (state.phase == SessionPhase.COMPLETED) R.drawable.ic_ms_celebration else R.drawable.ic_ms_info)
                    Text(
                        if (state.phase == SessionPhase.COMPLETED) model.tr("session_completed_title") else model.tr("session_ended_title"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(model.tr("session_completed_desc"), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    IrisPrimaryButton(text = model.tr("session_back_home"), icon = R.drawable.ic_ms_home, onClick = model::home)
                }
            }
        }
        model.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
