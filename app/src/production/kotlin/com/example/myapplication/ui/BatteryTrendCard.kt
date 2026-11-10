package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.session.GuidedSessionViewModel
import java.time.LocalDate

@Composable
fun BatteryTrendCard(sessionModel: GuidedSessionViewModel) {
    var selectedPeriod by remember { mutableIntStateOf(1) } // 0: 일간(오늘), 1: 주간(7일), 2: 월간(4주)
    val records = sessionModel.records
    val today = LocalDate.now()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_card_chart), contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            when (selectedPeriod) {
                                0 -> sessionModel.tr("trend_daily_title")
                                1 -> sessionModel.tr("trend_weekly_title")
                                else -> sessionModel.tr("trend_monthly_title")
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            when (selectedPeriod) {
                                0 -> sessionModel.tr("trend_daily_sub")
                                1 -> sessionModel.tr("trend_weekly_sub")
                                else -> sessionModel.tr("trend_monthly_sub")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = selectedPeriod == 0,
                    onClick = { selectedPeriod = 0 },
                    label = { Text(sessionModel.tr("tab_daily"), fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = selectedPeriod == 1,
                    onClick = { selectedPeriod = 1 },
                    label = { Text(sessionModel.tr("tab_weekly"), fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilterChip(
                    selected = selectedPeriod == 2,
                    onClick = { selectedPeriod = 2 },
                    label = { Text(sessionModel.tr("tab_monthly"), fontSize = 11.sp) },
                    shape = RoundedCornerShape(8.dp)
                )
            }

            Box(
                modifier = Modifier.fillMaxWidth().height(190.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                when (selectedPeriod) {
                    0 -> DailyStockChartView(sessionModel)
                    1 -> {
                        val past7Days = (6 downTo 0).map { today.minusDays(it.toLong()).toString() }
                        val dailyScores = past7Days.associateWith { date ->
                            val dayRecords = records.filter { it.completedLocalDate == date }
                            val rests = dayRecords.count { it.routineId == "screen_rest" }
                            val miniGames = dayRecords.count { it.routineId == "eye_gym" || it.routineId == "smooth_pursuit" }
                            (70 + (rests * 15) + (miniGames * 25)).coerceIn(30, 100)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            dailyScores.forEach { (dateStr, score) ->
                                val (monthDay, dayName) = try {
                                    val parsed = LocalDate.parse(dateStr)
                                    val md = "${parsed.monthValue}/${parsed.dayOfMonth}"
                                    val dn = parsed.dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, java.util.Locale.KOREAN)
                                    md to dn
                                } catch (_: Exception) {
                                    "" to ""
                                }
                                val heightFraction = (score.toFloat() / 100f).coerceIn(0.2f, 1.0f)
                                val barColor = when {
                                    score >= 80 -> Color(0xFF00C853)
                                    score >= 50 -> Color(0xFFFFB300)
                                    else -> Color(0xFFFF5252)
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("$score%", fontSize = 10.sp, color = barColor, fontWeight = FontWeight.Bold)
                                    Box(
                                        modifier = Modifier
                                            .size(width = 28.dp, height = (60 * heightFraction).dp)
                                            .background(
                                                if (dateStr == sessionModel.today) barColor
                                                else barColor.copy(alpha = 0.3f),
                                                RoundedCornerShape(8.dp)
                                            )
                                    )
                                    Text(dayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (dateStr == sessionModel.today) MaterialTheme.colorScheme.onSurface else Color.Gray)
                                    Text(monthDay, fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                    else -> {
                        val weeklyScores = (3 downTo 0).map { weekIndex ->
                            val endDay = today.minusDays((weekIndex * 7).toLong())
                            val startDay = endDay.minusDays(6)
                            val weekDates = (0..6).map { startDay.plusDays(it.toLong()).toString() }

                            val scores = weekDates.map { dateStr ->
                                val dayRecords = records.filter { it.completedLocalDate == dateStr }
                                val rests = dayRecords.count { it.routineId == "screen_rest" }
                                val miniGames = dayRecords.count { it.routineId == "eye_gym" || it.routineId == "smooth_pursuit" }
                                (70 + (rests * 15) + (miniGames * 25)).coerceIn(30, 100)
                            }
                            val avgScore = scores.average().toInt()
                            val label = if (weekIndex == 0) "이번 주" else "${weekIndex}주 전"
                            label to avgScore
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            weeklyScores.forEach { (weekLabel, score) ->
                                val heightFraction = (score.toFloat() / 100f).coerceIn(0.2f, 1.0f)
                                val barColor = when {
                                    score >= 80 -> Color(0xFF00C853)
                                    score >= 50 -> Color(0xFFFFB300)
                                    else -> Color(0xFFFF5252)
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("$score%", fontSize = 11.sp, color = barColor, fontWeight = FontWeight.Bold)
                                    Box(
                                        modifier = Modifier
                                            .size(width = 44.dp, height = (60 * heightFraction).dp)
                                            .background(barColor, RoundedCornerShape(8.dp))
                                    )
                                    Text(weekLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyStockChartView(sessionModel: GuidedSessionViewModel) {
    val records = sessionModel.records
    val today = sessionModel.today
    val currentScore = sessionModel.eyeBatteryStats.eyeHealthScore

    val now = java.time.LocalTime.now()
    val currentHour = now.hour
    val currentMinute = now.minute
    val currentTotalMinutes = currentHour * 60 + currentMinute

    val points = remember(records, currentScore, currentTotalMinutes) {
        val list = mutableListOf<Triple<String, Int, Boolean>>()
        val hours = (0..currentHour).filter { it % 3 == 0 }.toMutableList()
        if (!hours.contains(currentHour)) {
            hours.add(currentHour)
        }

        hours.forEach { h ->
            val m = if (h == currentHour) currentMinute else 0
            val label = String.format(java.util.Locale.getDefault(), "%02d:%02d", h, m)
            val totalM = h * 60 + m

            val cumulativeScreenTime = if (currentTotalMinutes > 0) {
                ((sessionModel.eyeBatteryStats.screenTimeMinutes * totalM) / currentTotalMinutes).coerceAtLeast(0)
            } else 0

            val cumulativeRests = records.count {
                it.completedLocalDate == today &&
                        (it.completedAtEpochMillis % 86400000) / 60000 <= totalM &&
                        it.routineId == "screen_rest"
            }

            val cumulativeGames = records.count {
                it.completedLocalDate == today &&
                        (it.completedAtEpochMillis % 86400000) / 60000 <= totalM &&
                        (it.routineId == "eye_gym" || it.routineId == "smooth_pursuit")
            }

            val statsAtHour = com.example.myapplication.session.EyeBatteryCalculator.calculateBattery(
                screenTimeMinutes = cumulativeScreenTime,
                restCount = cumulativeRests,
                miniGameCount = cumulativeGames
            )

            val finalScore = if (h == currentHour) currentScore else statsAtHour.eyeHealthScore

            list.add(Triple(label, finalScore, h == currentHour))
        }
        list
    }

    val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("M월 d일")
    val formattedDate = java.time.LocalDate.now().format(dateFormatter)

    Column(
        modifier = Modifier.fillMaxWidth().height(210.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "$formattedDate ${sessionModel.tr("trend_daily_title")}",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFF161F28), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxHeight().padding(end = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    listOf("100%", "80%", "60%", "40%", "20%").forEach { pct ->
                        Text(pct, fontSize = 9.sp, color = Color.Gray)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height
                        val stepX = width / (points.size - 1).coerceAtLeast(1)

                        for (i in 0..4) {
                            val y = height * (i / 4f)
                            drawLine(
                                color = Color.DarkGray.copy(alpha = 0.3f),
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        val path = androidx.compose.ui.graphics.Path()
                        points.forEachIndexed { index, (_, score, _) ->
                            val x = index * stepX
                            val normalizedScore = (score - 20f) / 80f
                            val y = height * (1f - normalizedScore.coerceIn(0f, 1f))
                            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }

                        drawPath(
                            path = path,
                            color = Color(0xFF00E676),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                        )

                        points.forEachIndexed { index, (_, score, isCurrent) ->
                            val x = index * stepX
                            val normalizedScore = (score - 20f) / 80f
                            val y = height * (1f - normalizedScore.coerceIn(0f, 1f))

                            drawCircle(
                                color = Color.White,
                                radius = if (isCurrent) 6.dp.toPx() else 4.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                            drawCircle(
                                color = Color(0xFF00E676),
                                radius = if (isCurrent) 4.dp.toPx() else 2.5.dp.toPx(),
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayPoints = if (points.size > 5) {
                listOf(points.first(), points[points.size / 2], points.last())
            } else points
            displayPoints.forEach { (label, score, _) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$score%", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00E676))
                    Text(label, fontSize = 9.sp, color = Color.Gray)
                }
            }
        }
    }
}
