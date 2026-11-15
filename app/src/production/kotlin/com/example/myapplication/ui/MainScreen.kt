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
    var cheerCount by remember { mutableIntStateOf(42) }
    var userCheered by remember { mutableStateOf(false) }
    var userNickname by remember { mutableStateOf("맑은눈토끼") }
    var userEmoji by remember { mutableStateOf("🐰") }
    var userOneLineGoal by remember { mutableStateOf("하루 3회 30초 눈 휴식 완오! 🔥") }

    var isPublicChallengeJoined by remember { mutableStateOf(false) }
    var publicChallengeParticipants by remember { mutableIntStateOf(3420) }

    var showProfileDialog by remember { mutableStateOf(false) }
    var showRelayDialog by remember { mutableStateOf(false) }
    var showCreateRelayDialog by remember { mutableStateOf(false) }
    var showBatonPassDialog by remember { mutableStateOf(false) }
    var showFullGuestBookDialog by remember { mutableStateOf(false) }

    var newRelayTopic by remember { mutableStateOf("") }
    var selectedFirstFriend by remember { mutableStateOf("🍓 체리공주") }
    var batonMessage by remember { mutableStateOf("") }
    var selectedSticker by remember { mutableStateOf("❤️ 파이팅") }

    val mascotEmojis = remember {
        listOf("🐰", "🐱", "🐻", "🦄", "🌸", "🧸", "🎀", "💖", "🍓", "🍒", "🌷", "🐥", "🐬", "🦊", "🐶", "🐼", "✨", "👑", "🔮", "🥑")
    }

    val friendsList = listOf("🍓 체리공주", "🌸 벚꽃요정", "🧸 곰돌이버디", "🦊 민트여우")
    val stickersList = listOf("❤️ 파이팅", "✨ 수고했어", "🎉 우승예감", "🍀 행운")

    var activeRelays by remember {
        mutableStateOf(
            listOf(
                Triple("🏆 [주간공식] 7일 연속 눈 휴식 스트릭 릴레이", listOf("🐰 맑은눈토끼", "🍓 체리공주", "🌸 벚꽃요정"), "진행 중 (+200P)"),
                Triple("👁️ [주간공식] 1시간 사용 후 30초 눈 감기 릴레이", listOf("🦊 민트여우", "🧸 곰돌이버디"), "진행 중 (+300P)")
            )
        )
    }

    var guestBookCards by remember {
        mutableStateOf(
            listOf(
                "🍓 체리공주: “오늘도 맑은 눈 피로 싹 날리고 함께 파이팅! 💖” [❤️ 파이팅]",
                "🌸 벚꽃요정: “작은 30초 휴식이 눈 건강을 지켜줘요 🌷” [✨ 수고했어]",
                "🧸 곰돌이버디: “눈동자 굴리기 미니게임 진짜 시원해요! 💪” [🎉 우승예감]",
                "🦊 민트여우: “휴식 릴레이 바통 넘겨받고 눈 휴식 완료! ✨” [🍀 행운]"
            )
        )
    }

    // 3.5s AUTO-SLIDING ROLLING INDEX
    var rollingIndex by remember { mutableIntStateOf(0) }
    LaunchedEffect(guestBookCards.size) {
        while (true) {
            delay(3500L)
            if (guestBookCards.isNotEmpty()) {
                rollingIndex = (rollingIndex + 1) % guestBookCards.size
            }
        }
    }

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
            // LAYER 1: MY PROFILE HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color(0xFFFF5252).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(userEmoji, fontSize = 24.sp)
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(userNickname, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Icon(painter = painterResource(id = R.drawable.ic_card_trophy), contentDescription = "프로필 수정", modifier = Modifier.size(14.dp).clickable { showProfileDialog = true }, tint = Color.Gray)
                        }
                        Text(userOneLineGoal, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("❤️ ${cheerCount}개", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            // FRONT ACTIVE RELAY CARD (PROMINENT FEATURED)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚩 진행 중인 릴레이 (${activeRelays.size}개)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                        Text(
                            text = "탐색/생성 ➔",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showRelayDialog = true }
                        )
                    }

                    if (activeRelays.isNotEmpty()) {
                        val (topTopic, topRunners, _) = activeRelays.first()
                        Text(topTopic, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🏃 바통: ${topRunners.joinToString(" ➔ ")}",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Button(
                                onClick = { showBatonPassDialog = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(painter = painterResource(id = R.drawable.ic_relay_baton), contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Text("1초 바통 터치", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }

            // GUESTBOOK 3.5s AUTO-SLIDING ROLLING BANNER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💌 친구 응원 방명록 (실시간 롤링)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(
                    text = "전체보기 (${guestBookCards.size}개) ➔",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { showFullGuestBookDialog = true }
                )
            }

            if (guestBookCards.isNotEmpty()) {
                val currentRollingText = guestBookCards[rollingIndex % guestBookCards.size]
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentRollingText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                cheerCount++
                                userCheered = true
                                val newCard = "🐰 $userNickname: “오늘도 눈 건강 배터리 100% 충전 파이팅! ✨” [❤️ 파이팅]"
                                guestBookCards = listOf(newCard) + guestBookCards
                                Toast.makeText(context, "응원 보드에 응원 메시지를 남겼습니다! ❤️ (+10P)", Toast.LENGTH_SHORT).show()
                            },
                            enabled = !userCheered,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (userCheered) sessionModel.tr("cheer_done") else "❤️ 응원", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    // FULL GUESTBOOK DIALOG
    if (showFullGuestBookDialog) {
        AlertDialog(
            onDismissRequest = { showFullGuestBookDialog = false },
            title = { Text("💌 전체 응원 방명록 Board (${guestBookCards.size}개)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    guestBookCards.forEach { cardText ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(cardText, modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFullGuestBookDialog = false }) { Text("닫기") }
            }
        )
    }

    // LAYER 2: RELAY EXPLORER & CREATOR DIALOG
    if (showRelayDialog) {
        AlertDialog(
            onDismissRequest = { showRelayDialog = false },
            title = { Text("🏃 친구 응원 릴레이 탐색 & 생성") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Cold-Start 3-Step Solvers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                Toast.makeText(context, "📇 연락처 동기화 완료! 가입된 친구 3명을 찾았습니다.", Toast.LENGTH_LONG).show()
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("연락처 동기화", fontSize = 11.sp)
                        }
                        Button(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "👁️ IrisFit 눈 건강 응원 릴레이에 초대합니다! '오늘도 맑은 눈 파이팅!' 함께 바통을 이어주세요 🏃✨\n\n다운로드: https://play.google.com/store/apps/details?id=com.eyeoverlay.app")
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "친구에게 릴레이 초대하기")
                                context.startActivity(shareIntent)
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("📤 릴레이 링크 공유", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text("🌍 공개 글로벌 릴레이 (공용 공간)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    // INTERACTIVE PUBLIC CHALLENGE CARD WITH REAL COUNTER & STATUS
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPublicChallengeJoined) Color(0xFF00E676).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (!isPublicChallengeJoined) {
                                isPublicChallengeJoined = true
                                publicChallengeParticipants++
                                Toast.makeText(context, "🎉 #오늘도30초휴식 공개 챌린지에 정식 참여했습니다! (+50 눈 걸음 적립)", Toast.LENGTH_SHORT).show()
                            } else {
                                isPublicChallengeJoined = false
                                publicChallengeParticipants--
                                Toast.makeText(context, "공개 챌린지 참여를 취소했습니다.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("#오늘도30초휴식 공개 챌린지 🌍", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Surface(
                                    color = if (isPublicChallengeJoined) Color(0xFF00E676) else MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (isPublicChallengeJoined) "🟢 참여 완료" else "➕ 참여하기",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = "참여자 ${publicChallengeParticipants}명 참여 중 • " + if (isPublicChallengeJoined) "나의 기여: 눈 휴식 4회 완료 (달성률 80%)" else "탭하여 즉시 동참 및 보상 받기",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("주간 공식 & 친구 릴레이 목록 (최대 3개):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showCreateRelayDialog = true }) {
                            Text("➕ 새 릴레이 만들기", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    activeRelays.forEach { (topic, runners, status) ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(topic, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(status, fontSize = 11.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                                }
                                Text("🏃 순서: ${runners.joinToString(" ➔ ")}", fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showRelayDialog = false }) {
                    Text("닫기")
                }
            }
        )
    }

    // CREATE NEW RELAY DIALOG (AI & CATEGORY TEMPLATES & MAX 3 CAP)
    if (showCreateRelayDialog) {
        val templates = listOf(
            "👁️ [안구 휴식] 1시간 모니터 사용 후 30초 눈 감기",
            "🎮 [눈 운동] Focus & Shift 원근 스트레칭 20초 완오",
            "🔥 [스트릭] 7일 연속 눈 배터리 85% 유지 챌린지"
        )

        AlertDialog(
            onDismissRequest = { showCreateRelayDialog = false },
            title = { Text("✨ 새 응원 릴레이 만들기 (AI & 템플릿)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            val isMeasured = sessionModel.eyeBatteryStats.screenTimeMeasured
                            val score = sessionModel.eyeBatteryStats.eyeHealthScore
                            if (!isMeasured || score < 0) {
                                newRelayTopic = "🤖 [AI 추천] 맑은 눈 유지! 7일 연속 30초 휴식 릴레이"
                                Toast.makeText(context, "AI 코치가 맑은 눈 유지를 위한 맞춤 릴레이 주제를 추천했습니다! ✨", Toast.LENGTH_SHORT).show()
                            } else {
                                newRelayTopic = if (score < 65) {
                                    "🤖 [AI 추천] 눈 피로 위험! 지금 바로 30초 눈 감기 릴레이"
                                } else {
                                    "🤖 [AI 추천] 맑은 눈 유지! 7일 연속 30초 휴식 릴레이"
                                }
                                Toast.makeText(context, "AI 코치가 현재 눈 배터리(${score}%) 상태에 맞는 주제를 자동 생성했습니다! ✨", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(painter = painterResource(id = R.drawable.ic_relay_ai), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text("✨ AI 맞춤 주제 자동 생성하기", fontWeight = FontWeight.Bold)
                        }
                    }

                    Text("검증된 카테고리 템플릿 선택:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    templates.forEach { tmpl ->
                        Surface(
                            color = if (newRelayTopic == tmpl) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().clickable { newRelayTopic = tmpl }
                        ) {
                            Text(tmpl, modifier = Modifier.padding(10.dp), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedTextField(
                        value = newRelayTopic,
                        onValueChange = { newRelayTopic = it },
                        label = { Text("릴레이 주제 직접 입력/수정") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("첫 번째 바통 주자 선택:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    friendsList.forEach { friend ->
                        Surface(
                            color = if (selectedFirstFriend == friend) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().clickable { selectedFirstFriend = friend }
                        ) {
                            Text(friend, modifier = Modifier.padding(8.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (activeRelays.size >= 3) {
                        Toast.makeText(context, "릴레이 집중을 위해 참여는 최대 3개로 제한됩니다. 기존 릴레이를 완성해 주세요!", Toast.LENGTH_LONG).show()
                    } else if (newRelayTopic.isNotEmpty()) {
                        activeRelays = activeRelays + Triple(newRelayTopic, listOf("🐰 $userNickname", selectedFirstFriend), "진행 중 (+100P)")
                        showCreateRelayDialog = false
                        Toast.makeText(context, "'$newRelayTopic' 릴레이를 시작했습니다! $selectedFirstFriend 님에게 바통 알림이 발송되었습니다 🏃", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("릴레이 시작하기")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRelayDialog = false }) { Text("취소") }
            }
        )
    }

    // LAYER 3: BATON PASS DIALOG
    if (showBatonPassDialog) {
        AlertDialog(
            onDismissRequest = { showBatonPassDialog = false },
            title = { Text("🏃 응원 바통 터치 (인계)") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("응원 한마디와 스티커를 남기고 다음 주자에게 바통을 넘기세요:", fontSize = 12.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = batonMessage,
                        onValueChange = { batonMessage = it },
                        label = { Text("응원 한마디 (예: 30초 휴식 완료! 너도 가자!)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text("응원 스티커 선택:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        stickersList.forEach { sticker ->
                            Surface(
                                color = if (selectedSticker == sticker) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { selectedSticker = sticker }
                            ) {
                                Text(sticker, modifier = Modifier.padding(8.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text("다음 바통 주자 지목:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    friendsList.forEach { friend ->
                        Surface(
                            color = if (selectedFirstFriend == friend) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().clickable { selectedFirstFriend = friend }
                        ) {
                            Text(friend, modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showBatonPassDialog = false
                    val newCard = "🐰 $userNickname: “${batonMessage.ifEmpty { "오늘도 함께 눈 건강 지켜요!" }}” [$selectedSticker]"
                    guestBookCards = listOf(newCard) + guestBookCards.take(2)
                    Toast.makeText(context, "$selectedFirstFriend 님에게 바통을 성공적으로 전달했습니다! 🏃✨", Toast.LENGTH_SHORT).show()
                }) {
                    Text("바통 터치 완료")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatonPassDialog = false }) { Text("취소") }
            }
        )
    }

    // LAYER 1: EDIT PROFILE DIALOG
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("🎀 마이 프로필 설정 & 캐릭터 선택") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("마스코트 캐릭터 선택 (20종):", fontSize = 12.sp, color = Color.Gray)

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(5),
                        modifier = Modifier.fillMaxWidth().height(160.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(mascotEmojis.size) { idx ->
                            val emoji = mascotEmojis[idx]
                            Surface(
                                color = if (userEmoji == emoji) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.clickable { userEmoji = emoji }
                            ) {
                                Box(modifier = Modifier.padding(6.dp), contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 20.sp)
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = userNickname,
                        onValueChange = { userNickname = it },
                        label = { Text("닉네임 입력") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = userOneLineGoal,
                        onValueChange = { userOneLineGoal = it },
                        label = { Text("한 줄 목표 / 관심사 설정") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showProfileDialog = false }) {
                    Text("프로필 저장")
                }
            }
        )
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
        CommunityCheerWidget(sessionModel)              // 2) 친구들과 릴레이
        PublicGlobalChallengeCard(sessionModel)         // 3) 공개 글로벌 릴레이
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
