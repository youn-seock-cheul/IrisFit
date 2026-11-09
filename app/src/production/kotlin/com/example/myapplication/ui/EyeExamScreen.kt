package com.example.myapplication.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EyeExamScreen(
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    var measuredLeftAcuity by remember { mutableFloatStateOf(1.0f) }
    var measuredRightAcuity by remember { mutableFloatStateOf(1.0f) }
    var astigmatismResult by remember { mutableStateOf("정상 (방사선 및 JCC 난시 소견 없음)") }
    var duochromeResult by remember { mutableStateOf("정상 (적녹 굴절 균형 및 색각 정상)") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("안과식 정밀 눈 검사 & 건강 리포트", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    Button(onClick = onBack) {
                        Text("← 뒤로")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("👁️ 전문 안과식 시표 검사", fontWeight = FontWeight.SemiBold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("📊 종합 안구 건강 리포트", fontWeight = FontWeight.SemiBold) }
                )
            }

            if (selectedTab == 0) {
                ProfessionalEyeExamTestSection(
                    onExamCompleted = { leftAcuity, rightAcuity, astigmatism, duochrome ->
                        measuredLeftAcuity = leftAcuity
                        measuredRightAcuity = rightAcuity
                        astigmatismResult = astigmatism
                        duochromeResult = duochrome
                        selectedTab = 1
                    }
                )
            } else {
                ProfessionalEyeHealthReportSection(
                    leftAcuity = measuredLeftAcuity,
                    rightAcuity = measuredRightAcuity,
                    astigmatism = astigmatismResult,
                    duochrome = duochromeResult,
                    onRetest = { selectedTab = 0 }
                )
            }
        }
    }
}

@Composable
fun ProfessionalEyeExamTestSection(
    onExamCompleted: (leftAcuity: Float, rightAcuity: Float, astigmatism: String, duochrome: String) -> Unit
) {
    var examStep by remember { mutableIntStateOf(1) }
    var testEye by remember { mutableStateOf("좌안(왼쪽 눈)") }
    val acuityLevels = remember { listOf(0.1f, 0.2f, 0.4f, 0.6f, 0.8f, 1.0f, 1.2f, 1.5f) }
    var currentAcuityIndex by remember { mutableIntStateOf(0) }
    var trialIndex by remember { mutableIntStateOf(0) }
    var correctCountInLevel by remember { mutableIntStateOf(0) }

    var resultLeftAcuity by remember { mutableFloatStateOf(1.0f) }
    var resultRightAcuity by remember { mutableFloatStateOf(1.0f) }

    val directions = remember { listOf("상", "하", "좌", "우") }
    var currentTargetDirection by remember { mutableStateOf("상") }
    var isChartMode by remember { mutableStateOf(false) }

    var astigmatismMethod by remember { mutableIntStateOf(0) }
    var colorVisionMethod by remember { mutableIntStateOf(0) }

    var selectedAstigResult by remember { mutableStateOf("정상 (방사선 난시 소견 없음)") }
    var selectedColorResult by remember { mutableStateOf("정상 (적녹 굴절 균형 양호)") }
    var ishiharaAnswer by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Clean Step Chips without numbers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FilterChip(selected = examStep == 1, onClick = { examStep = 1 }, label = { Text("시력 검사") })
            FilterChip(selected = examStep == 2, onClick = { examStep = 2 }, label = { Text("난시 검사") })
            FilterChip(selected = examStep == 3, onClick = { examStep = 3 }, label = { Text("적녹 & 색각 검사") })
        }

        when (examStep) {
            1 -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("안과 표준 동일 위치 시표 축소 시력 검사", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (testEye == "좌안(왼쪽 눈)") "오른쪽 눈을 가리고 [왼쪽 눈]으로 검사하세요."
                            else "왼쪽 눈을 가리고 [오른쪽 눈]으로 검사하세요.",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { isChartMode = !isChartMode },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isChartMode) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isChartMode) "👁️ 란돌트 고리 인터랙티브 검사로 전환" else "📋 전통 시력검사표(5m) 보기", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isChartMode) {
                            StandardOptotypeChartCard()
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    if (testEye == "좌안(왼쪽 눈)") {
                                        testEye = "우안(오른쪽 눈)"
                                    } else {
                                        examStep = 2
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("다음 단계(난시 검사)로 이동하기", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            val targetAcuity = acuityLevels[currentAcuityIndex]
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("측정 대상: $testEye", fontWeight = FontWeight.SemiBold)
                                Text("시표 단계: $targetAcuity (시행: ${trialIndex + 1}/3)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                acuityLevels.forEach { lvl ->
                                    val isCurrent = (lvl == targetAcuity)
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isCurrent) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$lvl",
                                            fontSize = 11.sp,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) Color.White else Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            val targetSizeDp = (14f / targetAcuity).coerceIn(8f, 140f).dp
                            val animatedOptotypeSize by animateDpAsState(
                                targetValue = targetSizeDp,
                                animationSpec = tween(durationMillis = 300),
                                label = "optotypeSize"
                            )

                            Box(
                                modifier = Modifier
                                    .size(180.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(2.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(animatedOptotypeSize)) {
                                    val strokeWidthPx = size.width * 0.2f
                                    val ringDiameter = size.width
                                    val startAngle = when (currentTargetDirection) {
                                        "우" -> 45f
                                        "상" -> 135f
                                        "좌" -> 225f
                                        else -> 315f
                                    }
                                    drawArc(
                                        color = Color.Black,
                                        startAngle = startAngle,
                                        sweepAngle = 270f,
                                        useCenter = false,
                                        topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                                        size = Size(ringDiameter - strokeWidthPx, ringDiameter - strokeWidthPx),
                                        style = Stroke(width = strokeWidthPx)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "단계 통과 성공: $correctCountInLevel / 3 (2회 이상 정답 시 시표 축소)",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(14.dp))
                            Text("란돌트 고리의 뚫린 방향은 어디인가요?", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf("상", "하", "좌", "우").forEach { dir ->
                                    Button(
                                        onClick = {
                                            val isCorrect = (dir == currentTargetDirection)
                                            if (isCorrect) correctCountInLevel++

                                            if (trialIndex < 2) {
                                                trialIndex++
                                                currentTargetDirection = directions.random()
                                            } else {
                                                if (correctCountInLevel >= 2 && currentAcuityIndex < acuityLevels.lastIndex) {
                                                    currentAcuityIndex++
                                                    trialIndex = 0
                                                    correctCountInLevel = 0
                                                    currentTargetDirection = directions.random()
                                                } else {
                                                    val finalEyeAcuity = acuityLevels[currentAcuityIndex]
                                                    if (testEye == "좌안(왼쪽 눈)") {
                                                        resultLeftAcuity = finalEyeAcuity
                                                        testEye = "우안(오른쪽 눈)"
                                                        currentAcuityIndex = 0
                                                        trialIndex = 0
                                                        correctCountInLevel = 0
                                                        currentTargetDirection = directions.random()
                                                    } else {
                                                        resultRightAcuity = finalEyeAcuity
                                                        examStep = 2
                                                    }
                                                }
                                            }
                                        }
                                    ) {
                                        Text(dir, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("정밀 난시 검사 (Astigmatism Test)", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            FilterChip(selected = astigmatismMethod == 0, onClick = { astigmatismMethod = 0 }, label = { Text("1. 방사선 시표 (Clock Dial)") })
                            FilterChip(selected = astigmatismMethod == 1, onClick = { astigmatismMethod = 1 }, label = { Text("2. 크로스실린더 (JCC)") })
                        }

                        if (astigmatismMethod == 0) {
                            Text("시계바늘 방사선 중 어느 방향 선이 가장 굵고 진하게 보이시나요?", fontSize = 12.sp, color = Color.Gray)
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(180.dp)) {
                                    val center = Offset(size.width / 2, size.height / 2)
                                    val ringRadius = size.width / 2 * 0.9f
                                    for (i in 0 until 12) {
                                        val angle = Math.toRadians((i * 30).toDouble())
                                        val endX = (center.x + ringRadius * cos(angle)).toFloat()
                                        val endY = (center.y + ringRadius * sin(angle)).toFloat()
                                        drawLine(
                                            color = Color.Black,
                                            start = center,
                                            end = Offset(endX, endY),
                                            strokeWidth = 3f
                                        )
                                    }
                                }
                            }

                            val clockChoices = listOf(
                                "정상 (모든 방향의 선이 동일하게 보임)" to "모든 선이 균일함 (난시 없음)",
                                "1시 - 7시 방향 선이 가장 진하고 선명함" to "1시-7시 축 난시 소견",
                                "3시 - 9시 방향 선이 가장 진하고 선명함" to "3시-9시 축 난시 소견"
                            )

                            clockChoices.forEach { (resVal, label) ->
                                OutlinedButton(
                                    onClick = {
                                        selectedAstigResult = resVal
                                        examStep = 3
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            Text("크로스 실린더 십자 격자점들의 선명도를 비교하세요.", fontSize = 12.sp, color = Color.Gray)
                            Box(
                                modifier = Modifier
                                    .size(200.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.size(180.dp)) {
                                    val center = Offset(size.width / 2, size.height / 2)
                                    drawLine(Color.Black, Offset(0f, center.y), Offset(size.width, center.y), strokeWidth = 4f)
                                    drawLine(Color.Black, Offset(center.x, 0f), Offset(center.x, size.height), strokeWidth = 4f)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    selectedAstigResult = "정상 (크로스 실린더 난시 소견 없음)"
                                    examStep = 3
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("가로/세로선 균일함 (정상)")
                            }
                            OutlinedButton(
                                onClick = {
                                    selectedAstigResult = "직난시 소견 (수직 축 난시 굴절 보정 필요)"
                                    examStep = 3
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("수직선(세로선)이 더 또렷함 (직난시)")
                            }
                        }
                    }
                }
            }

            3 -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("적녹 다이크롬 및 색각(Ishihara) 검사", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            FilterChip(selected = colorVisionMethod == 0, onClick = { colorVisionMethod = 0 }, label = { Text("1. 적녹 굴절 균형") })
                            FilterChip(selected = colorVisionMethod == 1, onClick = { colorVisionMethod = 1 }, label = { Text("2. 색각 이상 검사") })
                        }

                        if (colorVisionMethod == 0) {
                            Text("적색(빨강)과 녹색(초록) 바탕 속 시표의 선명도를 비교하세요.", fontSize = 12.sp, color = Color.Gray)
                            Row(modifier = Modifier.fillMaxWidth().height(140.dp)) {
                                Box(modifier = Modifier.weight(1f).fillMaxSize().background(Color(0xFFDD2C00), RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)), contentAlignment = Alignment.Center) {
                                    Text("RED (적색)", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.weight(1f).fillMaxSize().background(Color(0xFF00C853), RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)), contentAlignment = Alignment.Center) {
                                    Text("GREEN (녹색)", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            val duochromeOptions = listOf(
                                "정상 (적녹 굴절 균형 균등)" to "양쪽 바탕의 글자가 비슷하게 선명함",
                                "근시 경향성 (적색 바탕 글자가 더 진함)" to "적색(빨강) 바탕 속 글자가 더 진함",
                                "원시 경향성 (녹색 바탕 글자가 더 진함)" to "녹색(초록) 바탕 속 글자가 더 진함"
                            )

                            duochromeOptions.forEach { (resVal, label) ->
                                OutlinedButton(
                                    onClick = { selectedColorResult = resVal },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = if (selectedColorResult == resVal) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                ) {
                                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        } else {
                            Text("원형 판 속에서 숨겨진 숫자를 찾아 선택하세요 (정답: 3)", fontSize = 12.sp, color = Color.Gray)
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .background(Color(0xFFFFB300), RoundedCornerShape(80.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("3", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB71C1C))
                            }

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("3", "8", "5", "안 보임").forEach { num ->
                                    Button(
                                        onClick = { ishiharaAnswer = num },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(containerColor = if (ishiharaAnswer == num) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Text(num, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            if (ishiharaAnswer.isNotEmpty()) {
                                Text(if (ishiharaAnswer == "3") "정상 (색각 정상 판정)" else "색각 이상 의심 소견", fontWeight = FontWeight.Bold, color = if (ishiharaAnswer == "3") Color(0xFF00E676) else Color(0xFFFF5252))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                val finalColorRes = if (colorVisionMethod == 1 && ishiharaAnswer != "3") "색각 이상 의심 소견" else selectedColorResult
                                onExamCompleted(resultLeftAcuity, resultRightAcuity, selectedAstigResult, finalColorRes)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("종합 안구 건강 리포트 생성하기 📊", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StandardOptotypeChartCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("📋 표준 안과 시력검사표 (5m 거리 측정용)", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
            Text("스마트폰을 벽에 걸어두거나 5미터 거리에서 눈을 가리고 읽어보세요.", fontSize = 11.sp, color = Color.DarkGray)

            val chartRows = listOf(
                "0.1" to "4       ㄱ",
                "0.2" to "2  7    ㄴ  가",
                "0.3" to "6  3  5    ㄷ  나",
                "0.4" to "8  4  2    ㄹ  다  가",
                "0.5" to "3  6  7  9    ㅁ  라  나",
                "0.6" to "5  2  8  4    ㅂ  마  다  가",
                "0.8" to "7  4  5  8    ㅇ  사  마  다",
                "1.0" to "3  7  8  5  2    ㅊ  자  사  마",
                "1.2" to "6  2  9  4  8    ㅋ  차  자  바",
                "1.5" to "5  8  3  7  6    ㅌ  카  차  사",
                "2.0" to "4  9  2  6  5    ㅍ  타  카  자"
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                chartRows.forEach { (acuity, text) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(acuity, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
                        }
                        Text(text, fontWeight = FontWeight.Bold, fontSize = (14 * acuity.toFloat()).coerceIn(11f, 22f).sp, color = Color.Black, letterSpacing = 2.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfessionalEyeHealthReportSection(
    leftAcuity: Float,
    rightAcuity: Float,
    astigmatism: String,
    duochrome: String,
    onRetest: () -> Unit
) {
    val scrollState = rememberScrollState()

    val avgAcuity = (leftAcuity + rightAcuity) / 2f
    val doctorTitle = when {
        avgAcuity >= 1.0f -> "🩺 [AI 안과 주치의 소견] 최상급 시력 및 안구 조절력 유지 중"
        avgAcuity >= 0.7f -> "🩺 [AI 안과 주치의 소견] 양호한 시력이나 모니터 피로 주의 필요"
        else -> "🩺 [AI 안과 주치의 소견] 시력 교정 및 적극적인 30초 눈 휴식 필요"
    }

    val doctorSummary = buildString {
        append("측정된 양안 평균 시력은 ${String.format(java.util.Locale.getDefault(), "%.1f", avgAcuity)}로 일반인 평균 기준(1.0) 대비 ")
        append(if (avgAcuity >= 1.0f) "매우 훌륭한 상태입니다. " else "약간의 시력 조절 긴장이 관찰됩니다. ")
        append("정밀 굴절 검사 결과 [난시: $astigmatism], [적녹/색각: $duochrome] 상태를 보이고 있습니다. ")
        append(if (astigmatism.contains("난시") || duochrome.contains("경향성")) "장시간 디지털 기기 사용 시 블루라이트 차단과 30초 눈 휴식 루틴을 철저히 지키시는 것이 좋습니다." else "현재의 맑은 눈 상태를 오래 유지하기 위해 꾸준한 안구 운동을 권장합니다.")
    }

    val prescriptions = mutableListOf<String>().apply {
        add("• 20-20-20 규칙 준수: 20분마다 20피트(약 6m) 앞을 20초간 응시")
        add("• IrisFit 30초 눈 휴식 습관: 배터리가 65% 미만으로 떨어지기 전 30초 휴식 실시")
        add("• 스마트폰 내장 '시력 보호 화면(Eye Comfort Shield)' 상시 활성화")
        if (astigmatism.contains("난시")) add("• 난시 교정용 안경 또는 블루라이트 차단 렌즈 착용 권장")
        if (duochrome.contains("근시")) add("• 원거리 조절 운동(Focus & Shift) 미니게임 주 3회 이상 수행")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📊 안과 전문 AI 주치의 종합 리포트", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("진료 완료 🩺", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                Text(doctorSummary, fontSize = 13.sp, lineHeight = 18.sp)

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📋 정밀 측정 결과 비교표", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("검사 항목", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("측정 결과", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("평균 기준", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("좌안(왼쪽 눈) 시력", fontSize = 12.sp)
                            Text("$leftAcuity", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            Text("1.0 이상", fontSize = 12.sp, color = Color.Gray)
                        }
                        LinearProgressIndicator(
                            progress = { (leftAcuity / 2.0f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("우안(오른쪽 눈) 시력", fontSize = 12.sp)
                            Text("$rightAcuity", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            Text("1.0 이상", fontSize = 12.sp, color = Color.Gray)
                        }
                        LinearProgressIndicator(
                            progress = { (rightAcuity / 2.0f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(2.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("난시 굴절 검사", fontSize = 12.sp)
                            Text(astigmatism, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("적녹 & 색각 검사", fontSize = 12.sp)
                            Text(duochrome, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(doctorTitle, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                Text("💡 맞춤형 안구 건강 홈케어 처방전:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                prescriptions.forEach { prescription ->
                    Text(prescription, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Button(
            onClick = onRetest,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("🔄 정밀 눈 검사 다시 하기", fontWeight = FontWeight.Bold)
        }
    }
}
