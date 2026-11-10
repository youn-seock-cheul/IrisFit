package com.example.myapplication.ui

import androidx.activity.compose.BackHandler
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
fun GuidedSessionCard(model: GuidedSessionViewModel) {
    val state = model.state
    BackHandler(enabled = state.phase != SessionPhase.HOME) { model.back() }
    LaunchedEffect(state.phase) {
        while (state.phase == SessionPhase.RUNNING) {
            model.tick()
            delay(if (model.isMarker) 16L else 200L)
        }
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (state.phase) {
                SessionPhase.HOME -> {
                    Text("👁️ 즉시 눈 휴식 & 마일리지 적립", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Button(onClick = model::prepare, enabled = model.recordsReady && !model.busy, modifier = Modifier.fillMaxWidth()) {
                        Text("🟢 30초 눈 휴식 시작 (+100 Steps)", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(onClick = model::prepareMarker, enabled = model.recordsReady && !model.busy, modifier = Modifier.fillMaxWidth()) {
                        Text("🔵 20초 마커 따라보기 체험")
                    }
                }
                SessionPhase.READY -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (model.isMarker) "👁️ 20초 마커 따라보기 모드" else "🔋 눈 건강 배터리 충전 모드",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = if (model.isMarker) "편한 범위에서 화면 속 점을 따라 움직여 보세요." else "스마트폰에서 시선을 떼고 먼 곳을 바라보며 눈 배터리를 급속 충전하세요!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Button(
                            onClick = model::start,
                            enabled = !model.busy,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("🟢 휴식 & 충전 시작하기", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        OutlinedButton(
                            onClick = model::home,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("나중에 하기")
                        }
                    }
                }
                SessionPhase.RUNNING, SessionPhase.PAUSED -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
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
                                    Text(model.tr("${state.remainingSeconds}s"), style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(model.tr("눈 휴식 중"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                }
                            }
                        }

                        Text(
                            text = if (state.phase == SessionPhase.PAUSED) model.tr("안내를 잠시 멈췄어요.")
                            else if (model.isMarker) model.tr("점을 편한 범위에서 따라보세요")
                            else model.tr("화면에서 시선을 떼고 편안히 쉬어보세요 🌿"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (state.phase == SessionPhase.RUNNING) {
                                OutlinedButton(onClick = model::pause, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                                    Text(model.tr("일시정지"))
                                }
                            } else {
                                Button(onClick = model::resume, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                                    Text(model.tr("계속하기"))
                                }
                            }
                            Button(onClick = model::end, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                                Text(model.tr("그만하기"))
                            }
                        }
                    }
                }
                SessionPhase.CONFIRM -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (model.isMarker) {
                            Text(model.tr("마커 체험 안내가 끝났어요."), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(model.tr("시선 추적이나 건강 측정 결과가 아니며 휴식 완료 기록에 합산하지 않습니다."), textAlign = TextAlign.Center)
                            Button(onClick = model::home, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text(model.tr("홈으로")) }
                        } else {
                            Text(model.tr("🔋 눈 건강 배터리 충전 완료!"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text(model.tr("30초 휴식이 완료되었습니다.\n+100 눈 걸음 수 & +50 포인트 충전!"), textAlign = TextAlign.Center)

                            Button(
                                onClick = model::confirm,
                                enabled = !model.busy,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(if (model.busy) model.tr("저장 중…") else model.tr("마일리지 적립 & 완료"), fontWeight = FontWeight.Bold)
                            }
                            TextButton(onClick = model::end, enabled = !model.busy) {
                                Text(model.tr("기록 남기지 않기"), color = Color.Gray)
                            }
                        }
                    }
                }
                SessionPhase.COMPLETED, SessionPhase.ENDED -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(if (state.phase == SessionPhase.COMPLETED) model.tr("🎉 휴식 완료 & 충전 성공!") else model.tr("안내를 종료했어요."), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(model.tr("오늘도 건강한 눈 습관을 만드셨습니다."))
                        Button(
                            onClick = model::home,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(model.tr("홈으로 돌아가기"), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            model.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
