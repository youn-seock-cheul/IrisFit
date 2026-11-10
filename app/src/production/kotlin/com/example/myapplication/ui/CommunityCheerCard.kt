package com.example.myapplication.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.R
import com.example.myapplication.data.RelayPreferences
import com.example.myapplication.session.GuidedSessionViewModel
import kotlinx.coroutines.delay

@Composable
fun CommunityCheerWidget(sessionModel: GuidedSessionViewModel) {
    val context = LocalContext.current
    var cheerCount by remember { mutableIntStateOf(RelayPreferences.getCheerCount(context)) }
    var userCheered by remember { mutableStateOf(false) }
    var userNickname by remember { mutableStateOf(RelayPreferences.getNickname(context)) }
    var userEmoji by remember { mutableStateOf(RelayPreferences.getEmoji(context)) }
    var userOneLineGoal by remember { mutableStateOf(RelayPreferences.getGoal(context)) }

    var isMyTurn by remember { mutableStateOf(true) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showRelayDialog by remember { mutableStateOf(false) }
    var showCreateRelayDialog by remember { mutableStateOf(false) }
    var showBatonPassDialog by remember { mutableStateOf(false) }
    var showFullGuestBookDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    var newFriendName by remember { mutableStateOf("") }
    var newRelayTopic by remember { mutableStateOf("") }
    var selectedFirstFriend by remember { mutableStateOf("🍓 체리공주") }
    var batonMessage by remember { mutableStateOf("") }
    var selectedSticker by remember { mutableStateOf("❤️ 파이팅") }

    val mascotEmojis = remember {
        listOf("🐰", "🐱", "🐻", "🦄", "🌸", "🧸", "🎀", "💖", "🍓", "🍒", "🌷", "🐥", "🐬", "🦊", "🐶", "🐼", "✨", "👑", "🔮", "🥑")
    }

    var friendsList = remember {
        mutableStateListOf("🍓 체리공주", "🌸 벚꽃요정", "🧸 곰돌이버디", "🦊 민트여우")
    }
    val stickersList = listOf("❤️ 파이팅", "✨ 수고했어", "🎉 우승예감", "🍀 행운")

    var activeRelays = remember {
        mutableStateListOf(
            Triple("🏆 [주간공식] 7일 연속 눈 휴식 스트릭 릴레이", mutableListOf("🐰 $userNickname", "🍓 체리공주", "🌸 벚꽃요정"), "진행 중 (+200P)"),
            Triple("👁️ [주간공식] 1시간 사용 후 30초 눈 감기 릴레이", mutableListOf("🦊 민트여우", "🧸 곰돌이버디"), "진행 중 (+300P)")
        )
    }

    var guestBookCards = remember {
        mutableStateListOf(
            "🍓 체리공주: “오늘도 맑은 눈 피로 싹 날리고 함께 파이팅! 💖” [❤️ 파이팅]",
            "🌸 벚꽃요정: “작은 30초 휴식이 눈 건강을 지켜줘요 🌷” [✨ 수고했어]",
            "🧸 곰돌이버디: “눈동자 굴리기 미니게임 진짜 시원해요! 💪” [🎉 우승예감]",
            "🦊 민트여우: “휴식 릴레이 바통 넘겨받고 눈 휴식 완료! ✨” [🍀 행운]"
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
            // LAYER 1: MY PROFILE HEADER (CLEAN STYLING)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(userEmoji, fontSize = 22.sp)
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(sessionModel.tr(userNickname), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Icon(painter = painterResource(id = R.drawable.ic_card_trophy), contentDescription = "Edit Profile", modifier = Modifier.size(14.dp).clickable { showProfileDialog = true }, tint = Color.Gray)
                        }
                        Text(sessionModel.tr(userOneLineGoal), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(sessionModel.tr("${cheerCount}개"), modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            // FRONT ACTIVE RELAY CARD (CLEAN DECLUTTERED TWO-ROW LAYOUT)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = sessionModel.tr("${sessionModel.tr("relay_title")} (${activeRelays.size}개)"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                        Text(
                            text = sessionModel.tr("relay_explore"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showRelayDialog = true }
                        )
                    }

                    if (activeRelays.isNotEmpty()) {
                        val (topTopic, topRunners, _) = activeRelays.first()
                        val currentHolder = topRunners.lastOrNull() ?: userNickname
                        Text(sessionModel.tr(topTopic), fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        // ROW 2: BATON HOLDER PATH (CLEAN SEPARATE LINE)
                        val localizedRunners = topRunners.map { sessionModel.tr(it) }.joinToString(" ➔ ")
                        Text(
                            text = sessionModel.tr("${sessionModel.tr("relay_baton")}: [${sessionModel.tr(currentHolder)}] ($localizedRunners)"),
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // ROW 3: UNIFORM FULL-WIDTH ACTION BUTTON OR WAITING BADGE
                        if (isMyTurn) {
                            Button(
                                onClick = { showBatonPassDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(painter = painterResource(id = R.drawable.ic_relay_baton), contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text(sessionModel.tr("1초 바통 터치 (+50 눈 걸음)"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Surface(
                                color = Color.Gray.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = sessionModel.tr("대기 중 (${sessionModel.tr(currentHolder)} 차례)"),
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // GUESTBOOK 3.5s AUTO-SLIDING ROLLING BANNER WITH EXPLICIT TARGET CHEER BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(sessionModel.tr("guestbook_title"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(
                    text = "${sessionModel.tr("guestbook_more")} (${guestBookCards.size}개) ➔",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { showFullGuestBookDialog = true }
                )
            }

            if (guestBookCards.isNotEmpty()) {
                val currentRollingText = guestBookCards[rollingIndex % guestBookCards.size]
                val targetFriendName = currentRollingText.substringBefore(":").ifEmpty { "친구" }
                val localizedTarget = sessionModel.tr(targetFriendName)

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
                            text = sessionModel.tr(currentRollingText),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Button(
                            onClick = {
                                cheerCount++
                                RelayPreferences.setCheerCount(context, cheerCount)
                                userCheered = true
                                val newCard = "$userEmoji ${sessionModel.tr(userNickname)}: “$localizedTarget 님 오늘도 눈 피로 싹 날리고 파이팅! ✨” [❤️ 파이팅]"
                                guestBookCards.add(0, newCard)
                                Toast.makeText(context, sessionModel.tr("$localizedTarget 님에게 응원을 전달했습니다! ❤️ (+10P)"), Toast.LENGTH_SHORT).show()
                            },
                            enabled = !userCheered,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(if (userCheered) sessionModel.tr("cheer_done") else sessionModel.tr("❤️ $localizedTarget 응원"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            title = { Text(sessionModel.tr("전체 응원 방명록 Board (${guestBookCards.size}개)")) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 350.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    guestBookCards.forEach { cardText ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(sessionModel.tr(cardText), modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFullGuestBookDialog = false }) { Text(sessionModel.tr("btn_close")) }
            }
        )
    }

    // LAYER 2: RELAY EXPLORER & CREATOR DIALOG
    if (showRelayDialog) {
        AlertDialog(
            onDismissRequest = { showRelayDialog = false },
            title = { Text(sessionModel.tr("친구 응원 릴레이 탐색 & 생성")) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { showAddFriendDialog = true },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(sessionModel.tr("실제 친구 추가"), fontSize = 11.sp)
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
                            Text(sessionModel.tr("릴레이 링크 공유"), fontSize = 11.sp)
                        }
                    }

                    Text(sessionModel.tr("현재 등록된 친구 목록 (${friendsList.size}명):"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    friendsList.forEach { f ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("👤 ${sessionModel.tr(f)}", modifier = Modifier.padding(8.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sessionModel.tr("주간 공식 & 친구 릴레이 목록 (${activeRelays.size}개):"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        TextButton(onClick = { showCreateRelayDialog = true }) {
                            Text(sessionModel.tr("새 릴레이 만들기"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                    Text(sessionModel.tr(topic), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(sessionModel.tr(status), fontSize = 11.sp, color = Color(0xFF00E676), fontWeight = FontWeight.Bold)
                                }
                                Text(sessionModel.tr("순서: ${runners.map { sessionModel.tr(it) }.joinToString(" ➔ ")}"), fontSize = 11.sp, color = Color.Gray)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showRelayDialog = false }) { Text(sessionModel.tr("btn_close")) }
            }
        )
    }

    // ADD REAL FRIEND DIALOG
    if (showAddFriendDialog) {
        AlertDialog(
            onDismissRequest = { showAddFriendDialog = false },
            title = { Text(sessionModel.tr("실제 친구 직접 추가")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newFriendName,
                        onValueChange = { newFriendName = it },
                        label = { Text(sessionModel.tr("친구 닉네임 / 이메일 입력")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newFriendName.isNotBlank()) {
                        friendsList.add("🧑 $newFriendName")
                        selectedFirstFriend = "🧑 $newFriendName"
                        newFriendName = ""
                        showAddFriendDialog = false
                        Toast.makeText(context, sessionModel.tr("새로운 친구가 정상적으로 추가되었습니다! 👥"), Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(sessionModel.tr("친구 추가"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFriendDialog = false }) { Text(sessionModel.tr("btn_cancel")) }
            }
        )
    }

    // CREATE NEW RELAY DIALOG
    if (showCreateRelayDialog) {
        AlertDialog(
            onDismissRequest = { showCreateRelayDialog = false },
            title = { Text(sessionModel.tr("새 응원 릴레이 만들기")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newRelayTopic,
                        onValueChange = { newRelayTopic = it },
                        label = { Text(sessionModel.tr("릴레이 주제 직접 입력/수정")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(sessionModel.tr("첫 번째 바통 주자 선택:"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    friendsList.forEach { friend ->
                        Surface(
                            color = if (selectedFirstFriend == friend) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().clickable { selectedFirstFriend = friend }
                        ) {
                            Text(sessionModel.tr(friend), modifier = Modifier.padding(8.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (activeRelays.size >= 5) {
                        Toast.makeText(context, sessionModel.tr("릴레이 집중을 위해 최대 5개로 제한됩니다."), Toast.LENGTH_SHORT).show()
                    } else if (newRelayTopic.isNotBlank()) {
                        activeRelays.add(0, Triple("🏆 $newRelayTopic", mutableListOf("🐰 $userNickname", selectedFirstFriend), "진행 중 (+150P)"))
                        showCreateRelayDialog = false
                        newRelayTopic = ""
                        Toast.makeText(context, sessionModel.tr("릴레이를 시작했습니다!"), Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(sessionModel.tr("릴레이 시작"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateRelayDialog = false }) { Text(sessionModel.tr("btn_cancel")) }
            }
        )
    }

    // BATON PASS DIALOG
    if (showBatonPassDialog) {
        AlertDialog(
            onDismissRequest = { showBatonPassDialog = false },
            title = { Text(sessionModel.tr("응원 바통 터치 (인계)")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(sessionModel.tr("응원 한마디와 스티커를 남기고 다음 주자에게 바통을 넘기세요:"), fontSize = 12.sp, color = Color.Gray)

                    OutlinedTextField(
                        value = batonMessage,
                        onValueChange = { batonMessage = it },
                        label = { Text(sessionModel.tr("응원 한마디 입력")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Text(sessionModel.tr("응원 스티커 선택:"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        stickersList.forEach { sticker ->
                            Surface(
                                color = if (selectedSticker == sticker) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { selectedSticker = sticker }
                            ) {
                                Text(sessionModel.tr(sticker), modifier = Modifier.padding(8.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(sessionModel.tr("다음 바통 주자 지목:"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    friendsList.forEach { friend ->
                        Surface(
                            color = if (selectedFirstFriend == friend) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().clickable { selectedFirstFriend = friend }
                        ) {
                            Text(sessionModel.tr(friend), modifier = Modifier.padding(10.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showBatonPassDialog = false
                    if (activeRelays.isNotEmpty()) {
                        val (topTopic, topRunners, status) = activeRelays[0]
                        topRunners.add(selectedFirstFriend)
                        activeRelays[0] = Triple(topTopic, topRunners, status)
                    }
                    val newCard = "$userEmoji ${sessionModel.tr(userNickname)}: “${batonMessage.ifEmpty { sessionModel.tr("오늘도 함께 눈 건강 지켜요!") }}” [${sessionModel.tr(selectedSticker)}]"
                    guestBookCards.add(0, newCard)
                    isMyTurn = false
                    batonMessage = ""
                    Toast.makeText(context, sessionModel.tr("바통 전달 완료!"), Toast.LENGTH_SHORT).show()
                }) {
                    Text(sessionModel.tr("바통 터치 완료"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatonPassDialog = false }) { Text(sessionModel.tr("btn_cancel")) }
            }
        )
    }

    // EDIT PROFILE DIALOG
    if (showProfileDialog) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text(sessionModel.tr("마이 프로필 설정 & 캐릭터 선택")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(sessionModel.tr("마스코트 캐릭터 선택 (20종):"), fontSize = 12.sp, color = Color.Gray)

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
                        label = { Text(sessionModel.tr("닉네임 입력")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = userOneLineGoal,
                        onValueChange = { userOneLineGoal = it },
                        label = { Text(sessionModel.tr("한 줄 목표 / 관심사 설정")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    showProfileDialog = false
                    RelayPreferences.setNickname(context, userNickname)
                    RelayPreferences.setEmoji(context, userEmoji)
                    RelayPreferences.setGoal(context, userOneLineGoal)
                    Toast.makeText(context, sessionModel.tr("프로필이 성공적으로 저장되었습니다! ✨"), Toast.LENGTH_SHORT).show()
                }) {
                    Text(sessionModel.tr("프로필 저장"))
                }
            }
        )
    }
}
