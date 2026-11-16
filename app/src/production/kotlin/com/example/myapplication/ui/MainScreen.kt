package com.example.myapplication.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.R
import com.example.myapplication.data.ScreenTimeManager
import com.example.myapplication.session.GuidedSessionViewModel
import com.example.myapplication.session.EyeBatteryCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun MainScreen(
    sessionModel: GuidedSessionViewModel,
    onStartCameraSession: () -> Unit,
    onStopCameraSession: () -> Unit,
    onNavigateToExam: () -> Unit,
    onNavigateToGym: () -> Unit,
    onNavigateToPursuit: () -> Unit,
    onNavigateToPursuitMode: (Int) -> Unit = {},
    onNavigateToPursuitRandom: () -> Unit = onNavigateToPursuit,
    onNavigateToWorkoutRandom: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showBetaNoticeDialog by remember { mutableStateOf(false) }

    if (showBetaNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showBetaNoticeDialog = false },
            title = { Text(text = "알림", fontWeight = FontWeight.Bold) },
            text = { Text(text = "베타 준비 중입니다.") },
            confirmButton = {
                TextButton(onClick = { showBetaNoticeDialog = false }) {
                    Text("확인")
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(painter = painterResource(id = R.drawable.ic_nav_home), contentDescription = sessionModel.tr("nav_home"), modifier = Modifier.size(22.dp)) },
                    label = { Text(sessionModel.tr("nav_home"), fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(painter = painterResource(id = R.drawable.ic_nav_gym), contentDescription = sessionModel.tr("nav_gym"), modifier = Modifier.size(22.dp)) },
                    label = { Text(sessionModel.tr("nav_gym"), fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(painter = painterResource(id = R.drawable.ic_nav_eye_comfort), contentDescription = sessionModel.tr("nav_eye_comfort"), modifier = Modifier.size(22.dp)) },
                    label = { Text(sessionModel.tr("nav_eye_comfort"), fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { showBetaNoticeDialog = true },
                    icon = { Icon(painter = painterResource(id = R.drawable.ic_nav_ai), contentDescription = sessionModel.tr("nav_ai"), modifier = Modifier.size(22.dp)) },
                    label = { Text(sessionModel.tr("nav_ai"), fontWeight = FontWeight.SemiBold) }
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(painter = painterResource(id = R.drawable.ic_nav_settings), contentDescription = sessionModel.tr("nav_settings"), modifier = Modifier.size(22.dp)) },
                    label = { Text(sessionModel.tr("nav_settings"), fontWeight = FontWeight.SemiBold) }
                )
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding)
        when (selectedTab) {
            0 -> WellnessHomeTab(
                sessionModel = sessionModel,
                modifier = modifier,
                onNavigateToGym = onNavigateToGym,
                onNavigateToPursuit = onNavigateToPursuitRandom
            )
            1 -> GymAndExamTab(sessionModel, modifier, onNavigateToGym, onNavigateToPursuitMode, onNavigateToExam)
            2 -> EyeComfortSettingsTab(sessionModel, modifier)
            3 -> AiCoachTab(sessionModel, modifier)
            4 -> SettingsTab(sessionModel, modifier)
        }
    }
}

@Composable
fun CommunityCheerCard(sessionModel: GuidedSessionViewModel) {
    val context = LocalContext.current
    var showBetaNoticeDialog by remember { mutableStateOf(false) }

    if (showBetaNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showBetaNoticeDialog = false },
            title = { Text(text = "알림", fontWeight = FontWeight.Bold) },
            text = { Text(text = "베타 준비 중입니다.") },
            confirmButton = {
                TextButton(onClick = { showBetaNoticeDialog = false }) {
                    Text("확인")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showBetaNoticeDialog = true },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            .background(Color(0xFFFF9800).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🚩", fontSize = 18.sp)
                    }
                    Column {
                        Text("눈 건강 응원 릴레이 & 커뮤니티", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("친구들과 함께 이어가는 눈 휴식 릴레이", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "[베타 준비 중]",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "릴레이 탐색, 바통 인계 및 소셜 방명록 기능은 현재 정식 베타 출시를 위해 준비 중입니다.",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Button(
                onClick = { showBetaNoticeDialog = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("베타 알림 확인하기", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GoalAchievementCard(sessionModel: GuidedSessionViewModel) {
    val restCount = sessionModel.todayRestCount
    val isMeasured = sessionModel.eyeBatteryStats.screenTimeMeasured
    val batteryScore = sessionModel.eyeBatteryStats.eyeHealthScore
    val isGoal1Reached = isMeasured && batteryScore >= 85
    val isGoal2Reached = restCount >= 3
    val isGoal3Reached = true

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
                            .background(Color(0xFFFFB300).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_card_trophy), contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(sessionModel.tr("goals_header"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(sessionModel.tr("goals_sub"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Goal 1
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f).padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(painter = painterResource(id = R.drawable.ic_goal_battery), contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(22.dp))
                            Column {
                                Text(sessionModel.tr("goal_1"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(if (isMeasured && batteryScore >= 0) "현재 잔량: ${batteryScore}%" else "현재 잔량: 미측정", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (isGoal1Reached) {
                                Icon(painter = painterResource(id = R.drawable.ic_goal_achieved), contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                            }
                            Text(if (isGoal1Reached) sessionModel.tr("goal_achieved") else sessionModel.tr("goal_progress"), fontWeight = FontWeight.Bold, color = if (isGoal1Reached) Color(0xFF00E676) else Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                // Goal 2
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f).padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(painter = painterResource(id = R.drawable.ic_goal_rest), contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(22.dp))
                            Column {
                                Text(sessionModel.tr("goal_2"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("현재 진행: ${restCount} / 3회", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (isGoal2Reached) {
                                Icon(painter = painterResource(id = R.drawable.ic_goal_achieved), contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                            }
                            Text(if (isGoal2Reached) sessionModel.tr("goal_achieved") else sessionModel.tr("goal_progress"), fontWeight = FontWeight.Bold, color = if (isGoal2Reached) Color(0xFF00E676) else Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                // Goal 3
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(modifier = Modifier.weight(1f).padding(end = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(painter = painterResource(id = R.drawable.ic_goal_streak), contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(22.dp))
                            Column {
                                Text(sessionModel.tr("goal_3"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("연속 7일째 달성 중!", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (isGoal3Reached) {
                                Icon(painter = painterResource(id = R.drawable.ic_goal_achieved), contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(16.dp))
                            }
                            Text(if (isGoal3Reached) sessionModel.tr("goal_achieved") else sessionModel.tr("goal_progress"), fontWeight = FontWeight.Bold, color = if (isGoal3Reached) Color(0xFF00E676) else Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GymAndExamTab(
    sessionModel: GuidedSessionViewModel,
    modifier: Modifier,
    onNavigateToGym: () -> Unit,
    onNavigateToPursuitMode: (Int) -> Unit = {},
    onNavigateToExam: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.padding(20.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(sessionModel.tr("gym_header"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(sessionModel.tr("gym_sub"))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🔋", fontSize = 20.sp)
                    Text(sessionModel.tr("gym_card_banner"), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Text(sessionModel.tr("gym_card_banner_sub"))
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(sessionModel.tr("gym_focus_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(sessionModel.tr("gym_focus_sub"))
                Button(onClick = onNavigateToGym, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(sessionModel.tr("gym_focus_btn"), fontWeight = FontWeight.Bold)
                }
            }
        }

        // PURSUIT MODES CARD (4 Distinct Pursuit Modes: Infinity, Figure-8, Circle, Zig-Zag)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(sessionModel.tr("gym_pursuit_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(sessionModel.tr("gym_pursuit_sub"))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { onNavigateToPursuitMode(0) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                        Text(sessionModel.tr("♾️ Infinity (∞)"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { onNavigateToPursuitMode(1) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                        Text(sessionModel.tr("🔀 Figure-8 (8자)"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { onNavigateToPursuitMode(2) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                        Text(sessionModel.tr("⭕ Circle (원형)"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(onClick = { onNavigateToPursuitMode(3) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                        Text(sessionModel.tr("〰️ Zig-Zag"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(sessionModel.tr("gym_exam_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(sessionModel.tr("gym_exam_sub"))
                OutlinedButton(onClick = onNavigateToExam, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(sessionModel.tr("gym_exam_btn"))
                }
            }
        }
    }
}

@Composable
fun EyeComfortSettingsTab(sessionModel: GuidedSessionViewModel, modifier: Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier.padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            sessionModel.tr("eye_comfort_title"),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(sessionModel.tr("eye_comfort_desc"))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    sessionModel.tr("eye_comfort_card_title"),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    sessionModel.tr("eye_comfort_card_desc"),
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = {
                        try {
                            context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                        } catch (_: Exception) {
                            Toast.makeText(
                                context,
                                sessionModel.tr("eye_comfort_open_failed"),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(sessionModel.tr("eye_comfort_open_settings"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AiCoachTab(sessionModel: GuidedSessionViewModel, modifier: Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showUsageDisclosureDialog by remember { mutableStateOf(false) }

    val initialQueryResult = ScreenTimeManager.queryTodayScreenTime(context)
    var hasPermission by remember { mutableStateOf(initialQueryResult is com.example.myapplication.data.ScreenTimeQueryResult.Success) }
    var realMinutes by remember { mutableIntStateOf((initialQueryResult as? com.example.myapplication.data.ScreenTimeQueryResult.Success)?.minutes ?: 0) }

    LaunchedEffect(Unit) {
        while (true) {
            val res = ScreenTimeManager.queryTodayScreenTime(context)
            hasPermission = res is com.example.myapplication.data.ScreenTimeQueryResult.Success
            realMinutes = (res as? com.example.myapplication.data.ScreenTimeQueryResult.Success)?.minutes ?: 0
            delay(60_000L)
        }
    }

    val hours = realMinutes / 60
    val mins = realMinutes % 60
    val formattedTime = if (hasPermission) "${hours}시간 ${mins}분" else sessionModel.tr("미측정")

    val actualRests = sessionModel.todayRestCount

    val batteryScore = sessionModel.eyeBatteryStats.eyeHealthScore

    Column(
        modifier = modifier.padding(20.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(sessionModel.tr("ai_title"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("👑 PRO 멤버십", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(sessionModel.tr("ai_desc"))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AI 코치 (검토 중)", fontWeight = FontWeight.Bold)
                Text(sessionModel.tr("privacy_ai"), fontSize = 13.sp)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(sessionModel.tr("ai_analysis_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(sessionModel.tr("ai_screen_time"), color = Color.Gray)
                    Text(formattedTime, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(sessionModel.tr("ai_battery_remain"), color = Color.Gray)
                    val batteryScoreText = if (hasPermission && batteryScore >= 0) {
                        "${batteryScore}% (${sessionModel.tr(sessionModel.eyeBatteryStats.fatigueStatus.labelKey)})"
                    } else {
                        sessionModel.tr("screen_time_unmeasured")
                    }
                    Text(batteryScoreText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(sessionModel.tr("ai_compliance"), color = Color.Gray)
                    Text("${actualRests}회", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                if (!hasPermission) {
                    Text(
                        sessionModel.tr("screen_time_unmeasured"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = {
                        showUsageDisclosureDialog = true
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text(sessionModel.tr("ai_permission_btn"))
                    }
                }
            }
        }

        if (showUsageDisclosureDialog) {
            UsageAccessDisclosureDialog(
                sessionModel = sessionModel,
                onDismiss = { showUsageDisclosureDialog = false },
                onOpenSettings = {
                    showUsageDisclosureDialog = false
                    ScreenTimeManager.openUsageStatsSettings(context)
                }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(sessionModel.tr("IrisFit PRO 차세대 유료 플랜 혜택"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(sessionModel.tr("pro_demo_status"), style = MaterialTheme.typography.bodySmall)
                Text(sessionModel.tr("시선 추적(Gaze Tracking) 기술 기반의 독보적인 프리미엄 기능:"))

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(sessionModel.tr("1. Gaze-Nav 시선 스크롤 지도"), fontWeight = FontWeight.Bold)
                        Text(sessionModel.tr("손대지 않고 눈동자 이동만으로 지도 스크롤 & 확대/축소"), fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(sessionModel.tr("2. Eye-Blink 힐링 사운드"), fontWeight = FontWeight.Bold)
                        Text(sessionModel.tr("내 깜빡임 주기와 동기화되어 안구 근육을 이완시키는 오디오"), fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(sessionModel.tr("3. Gaze-AI 머무름 감지 요약"), fontWeight = FontWeight.Bold)
                        Text(sessionModel.tr("오랫동안 쳐다보는 화면 영역을 LLM이 실시간 감지 및 브리핑"), fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(sessionModel.tr("pro_unavailable"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

}

@Composable
fun SettingsTab(sessionModel: GuidedSessionViewModel, modifier: Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val sysLang = java.util.Locale.getDefault().displayLanguage

    Column(
        modifier = modifier.padding(20.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(sessionModel.tr("settings_header"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(sessionModel.tr("스마트폰 언어 자동 연동"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(sessionModel.tr("현재 스마트폰 시스템 언어"), fontSize = 12.sp, color = Color.Gray)
                        Text(sysLang, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("🟢 " + sessionModel.tr("실시간 자동 번역 중"), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        var notificationsEnabled by remember {
            mutableStateOf(com.example.myapplication.data.PreferencesManager(context).isNotificationsEnabled)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sessionModel.tr("noti_settings_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(sessionModel.tr("noti_settings_desc"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    androidx.compose.material3.Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { checked ->
                            notificationsEnabled = checked
                            com.example.myapplication.data.PreferencesManager(context).isNotificationsEnabled = checked
                            Toast.makeText(context, if (checked) sessionModel.tr("noti_on_toast") else sessionModel.tr("noti_off_toast"), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(sessionModel.tr("유저 피드백 & 버그 제보 (CS 센터)"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(sessionModel.tr("앱 사용 중 버그가 발생하거나 개선 아이디어가 있으시면 언제든지 의견을 남겨주세요!"), fontSize = 12.sp, color = Color.Gray)
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@irisfit.app?subject=[IrisFit Bug Report / Feedback]&body=Device: ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})\nFeedback: "))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "지원되는 이메일 앱이 없습니다. support@irisfit.app 으로 문의해 주세요.", Toast.LENGTH_LONG).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(sessionModel.tr("개발자에게 피드백 보내기 (Email)"), fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(sessionModel.tr("백그라운드 강제종료 방지 (배터리 최적화 예외)"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(sessionModel.tr("삼성, 샤오미 등 일부 기기에서 백그라운드 실시간 눈 건강 모니터링이 멈추지 않도록 [배터리 최적화 예외 앱]으로 등록해 주세요."), fontSize = 12.sp, color = Color.Gray)
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            try {
                                val intent2 = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                                context.startActivity(intent2)
                            } catch (_: Exception) {
                                Toast.makeText(context, "설정 화면을 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(sessionModel.tr("배터리 최적화 예외 설정 열기"), fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(sessionModel.tr("settings_version_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("IrisFit v1.0.0 (Google Play Release Ready)")
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(sessionModel.tr("settings_data_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(sessionModel.tr("settings_data_desc"))
                OutlinedButton(onClick = { showDeleteDialog = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(sessionModel.tr("settings_delete_btn"), color = MaterialTheme.colorScheme.error)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(sessionModel.tr("개인정보 처리방침 및 이용약관"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(sessionModel.tr("구글 플레이 스토어 등록 필수 법적 문서 및 로컬 데이터 보호 정책"), fontSize = 12.sp, color = Color.Gray)
                OutlinedButton(
                    onClick = { showPrivacyDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(sessionModel.tr("개인정보 처리방침 전문 보기"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(sessionModel.tr("privacy_policy_title")) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(sessionModel.tr("privacy_usage"), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(sessionModel.tr("privacy_sensor"), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(sessionModel.tr("privacy_storage"), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(sessionModel.tr("privacy_ai"), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("3. 문의 사항\n- 이메일: support@irisfit.app", fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) { Text("확인") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("기기 기록 전체 삭제") },
            text = { Text("저장된 모든 눈 건강 마일리지와 완료 기록이 삭제됩니다. 계속하시겠습니까?") },
            confirmButton = {
                TextButton(onClick = {
                    sessionModel.clearAll { showDeleteDialog = false }
                }) { Text(sessionModel.tr("btn_delete"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(sessionModel.tr("btn_cancel")) }
            }
        )
    }
}

@Composable
fun WellnessHomeTab(
    sessionModel: GuidedSessionViewModel,
    modifier: Modifier,
    onNavigateToGym: () -> Unit = {},
    onNavigateToPursuit: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.padding(20.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("IrisFit", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(sessionModel.tr("home_subtitle"), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

        EyePedometerCard(sessionModel, onNavigateToGym, onNavigateToPursuit)
        BatteryTrendCard(sessionModel)
        GoalAchievementCard(sessionModel)               // 1) 오늘의 눈 건강 목표
        CommunityCheerCard(sessionModel)                // 2) 친구들과 릴레이 (베타 준비 알림 카드)
        PublicGlobalChallengeCard(sessionModel)         // 3) 공개 글로벌 릴레이 (베타 준비 알림 카드)
    }

    if (sessionModel.state.phase != com.example.myapplication.session.SessionPhase.HOME) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(20.dp)) {
                    GuidedSessionCard(sessionModel)
                }
            }
        }
    }
}
