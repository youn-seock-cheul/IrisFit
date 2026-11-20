package com.example.myapplication.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.session.GuidedSessionViewModel
import com.example.myapplication.session.RingDirection
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EyeExamScreen(sessionModel: GuidedSessionViewModel, onBack: () -> Unit) {
    val tr: (String) -> String = sessionModel::tr
    var showSummary by rememberSaveable { mutableStateOf(false) }
    var step by rememberSaveable { mutableIntStateOf(0) }
    var leftLevel by rememberSaveable { mutableStateOf<Int?>(null) }
    var rightLevel by rememberSaveable { mutableStateOf<Int?>(null) }
    var lines by rememberSaveable { mutableStateOf<String?>(null) }
    var cross by rememberSaveable { mutableStateOf<String?>(null) }
    var colors by rememberSaveable { mutableStateOf<String?>(null) }
    var number by rememberSaveable { mutableStateOf<String?>(null) }
    var attempt by rememberSaveable { mutableIntStateOf(0) }
    val ringState = rememberSaveableStateHolder()

    Scaffold(
        topBar = { IrisTopBar(tr("check_title"), tr("gym_back"), onBack) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IrisInfoBanner(R.drawable.ic_ms_info, tr("check_disclaimer_title"), tr("check_disclaimer"))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(!showSummary, { showSummary = false }, label = { Text(tr("check_experience")) })
                FilterChip(showSummary, { showSummary = true }, label = { Text(tr("check_summary")) })
            }
            if (showSummary) {
                IrisCard {
                    IrisCardHeader(R.drawable.ic_ms_analytics, tr("check_summary"), tr("check_summary_desc"))
                    val rows = listOf(
                        tr("check_left") to leftLevel?.let { tr("check_last_level").format(it) },
                        tr("check_right") to rightLevel?.let { tr("check_last_level").format(it) },
                        tr("check_radial") to lines?.let(tr),
                        tr("check_cross") to cross?.let(tr),
                        tr("check_red_green") to colors?.let(tr),
                        tr("check_number") to number?.let { tr("check_selected").format(tr(it)) }
                    )
                    rows.forEach { (label, response) ->
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(label, fontWeight = FontWeight.SemiBold)
                            IrisBodyText(response ?: tr("check_unanswered"))
                        }
                    }
                }
                IrisInfoBanner(R.drawable.ic_ms_lightbulb, tr("check_habit_title"), tr("check_habit_body"))
                IrisPrimaryButton(tr("check_restart"), R.drawable.ic_ms_replay, onClick = {
                    leftLevel = null
                    rightLevel = null
                    lines = null
                    cross = null
                    colors = null
                    number = null
                    step = 0
                    ringState.removeState(attempt)
                    attempt++
                    showSummary = false
                })
            } else {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("check_ring", "check_lines", "check_colors").forEachIndexed { index, key ->
                        FilterChip(step == index, { step = index }, label = { Text(tr(key)) })
                    }
                }
                when (step) {
                    0 -> ringState.SaveableStateProvider(attempt) {
                        RingExperience(tr, attempt, onLeftCompleted = { leftLevel = it }, onCompleted = { left, right ->
                            leftLevel = left
                            rightLevel = right
                            step = 1
                        })
                    }
                    1 -> {
                        LineExperience(tr, lines, cross, { lines = it }, { cross = it })
                        IrisPrimaryButton(tr("check_next"), R.drawable.ic_ms_arrow_forward, onClick = { step = 2 })
                    }
                    else -> ColorExperience(tr, colors, number, { colors = it }, { number = it })
                }
                IrisTonalButton(tr("check_show_summary"), R.drawable.ic_ms_analytics, onClick = { showSummary = true })
            }
        }
    }
}

@Composable
private fun RingExperience(
    tr: (String) -> String, attempt: Int,
    onLeftCompleted: (Int) -> Unit, onCompleted: (Int?, Int?) -> Unit
) {
    var eye by rememberSaveable(attempt) { mutableIntStateOf(0) }
    var level by rememberSaveable(attempt) { mutableIntStateOf(0) }
    var trial by rememberSaveable(attempt) { mutableIntStateOf(0) }
    var correct by rememberSaveable(attempt) { mutableIntStateOf(0) }
    var left by rememberSaveable(attempt) { mutableStateOf<Int?>(null) }
    var direction by rememberSaveable(attempt) { mutableStateOf(RingDirection.UP) }
    var chartMode by rememberSaveable(attempt) { mutableStateOf(false) }
    val sizes = listOf(140, 100, 70, 48, 32, 24, 18, 12)
    val targetSize by animateDpAsState(sizes[level].dp, label = "ringSize")

    IrisCard {
        IrisCardHeader(R.drawable.ic_ms_visibility, tr("check_ring"))
        IrisBodyText(tr(if (eye == 0) "check_cover_right" else "check_cover_left"))
        IrisTonalButton(
            tr(if (chartMode) "check_interactive" else "check_chart"),
            R.drawable.ic_ms_description,
            onClick = { chartMode = !chartMode }
        )
        if (chartMode) {
            Column(
                modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(tr("check_chart_demo"), color = Color.Black)
                listOf("4 7", "2 7 6", "6 3 5 8", "8 4 2 7 9", "3 6 7 9 2 4").forEachIndexed { index, text ->
                    Text(text, color = Color.Black, fontSize = (32 - index * 4).sp)
                }
            }
            IrisBodyText(tr("check_chart_note"))
            IrisTonalButton(tr("check_skip"), R.drawable.ic_ms_arrow_forward, onClick = { onCompleted(left, null) })
        } else {
            Text(tr("check_trial").format(level + 1, trial + 1), style = MaterialTheme.typography.labelLarge)
            Box(
                modifier = Modifier.fillMaxWidth().height(180.dp).background(Color.White, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(Modifier.size(targetSize)) {
                    val stroke = size.width * 0.2f
                    drawArc(
                        color = Color.Black,
                        startAngle = direction.startAngle,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = Offset(stroke / 2, stroke / 2),
                        size = Size(size.width - stroke, size.height - stroke),
                        style = Stroke(stroke)
                    )
                }
            }
            IrisBodyText(tr("check_direction"))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RingDirection.entries.forEach { answer ->
                    Button(onClick = {
                        val totalCorrect = correct + if (answer == direction) 1 else 0
                        if (trial < 2) {
                            correct = totalCorrect
                            trial++
                        } else {
                            if (totalCorrect >= 2 && level < sizes.lastIndex) {
                                level++
                            } else if (eye == 0) {
                                left = level + 1
                                onLeftCompleted(level + 1)
                                eye = 1
                                level = 0
                            } else {
                                onCompleted(left, level + 1)
                            }
                            trial = 0
                            correct = 0
                        }
                        direction = RingDirection.entries.random()
                    }) { Text(tr(answer.labelKey)) }
                }
            }
        }
    }
}

@Composable
private fun LineExperience(
    tr: (String) -> String, lines: String?, cross: String?,
    onLines: (String) -> Unit, onCross: (String) -> Unit
) {
    var method by rememberSaveable { mutableIntStateOf(0) }
    IrisCard {
        IrisCardHeader(R.drawable.ic_ms_visibility, tr("check_lines"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(method == 0, { method = 0 }, label = { Text(tr("check_radial")) })
            FilterChip(method == 1, { method = 1 }, label = { Text(tr("check_cross")) })
        }
        IrisBodyText(tr("check_lines_prompt"))
        Box(
            Modifier.fillMaxWidth().height(210.dp).background(Color.White, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(Modifier.size(180.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                if (method == 0) {
                    repeat(12) { index ->
                        val angle = Math.toRadians(index * 30.0)
                        drawLine(Color.Black, center, Offset(
                            center.x + (size.width * 0.45f * cos(angle)).toFloat(),
                            center.y + (size.height * 0.45f * sin(angle)).toFloat()
                        ), strokeWidth = 3f)
                    }
                } else {
                    drawLine(Color.Black, Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 4f)
                    drawLine(Color.Black, Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth = 4f)
                }
            }
        }
        if (method == 0) {
            ObservationChoices(tr, listOf("check_lines_equal", "check_lines_diagonal", "check_lines_horizontal"), lines, onLines)
        } else {
            ObservationChoices(tr, listOf("check_cross_equal", "check_cross_vertical", "check_cross_horizontal"), cross, onCross)
        }
    }
}

@Composable
private fun ColorExperience(
    tr: (String) -> String, colors: String?, number: String?,
    onColors: (String) -> Unit, onNumber: (String) -> Unit
) {
    var method by rememberSaveable { mutableIntStateOf(0) }
    IrisCard {
        IrisCardHeader(R.drawable.ic_ms_visibility, tr("check_colors"))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(method == 0, { method = 0 }, label = { Text(tr("check_red_green")) })
            FilterChip(method == 1, { method = 1 }, label = { Text(tr("check_number")) })
        }
        if (method == 0) {
            IrisBodyText(tr("check_colors_prompt"))
            Row(Modifier.fillMaxWidth().height(140.dp)) {
                listOf(Color(0xFFDD2C00), Color(0xFF00C853)).forEach { color ->
                    Box(Modifier.weight(1f).fillMaxSize().background(color), contentAlignment = Alignment.Center) {
                        Text("E", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 36.sp)
                    }
                }
            }
            ObservationChoices(tr, listOf("check_colors_equal", "check_colors_red", "check_colors_green"), colors, onColors)
        } else {
            IrisBodyText(tr("check_number_prompt"))
            Box(
                Modifier.fillMaxWidth().height(160.dp).background(Color(0xFFFFB300), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) { Text("3", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C)) }
            ObservationChoices(tr, listOf("3", "8", "5", "check_not_visible"), number, onNumber)
        }
    }
}

@Composable
private fun ObservationChoices(tr: (String) -> String, keys: List<String>, selected: String?, onSelect: (String) -> Unit) {
    keys.forEach { key ->
        OutlinedButton(
            onClick = { onSelect(key) },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                containerColor = if (key == selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            )
        ) { Text(tr(key)) }
    }
}
