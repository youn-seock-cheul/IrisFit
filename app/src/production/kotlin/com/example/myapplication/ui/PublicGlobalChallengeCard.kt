package com.example.myapplication.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.session.GuidedSessionViewModel

@Composable
fun PublicGlobalChallengeCard(sessionModel: GuidedSessionViewModel) {
    val context = LocalContext.current
    var isPublicChallengeJoined by remember { mutableStateOf(false) }
    var publicChallengeParticipants by remember { mutableIntStateOf(3420) }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                            .background(Color(0xFF00E676).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🌍", fontSize = 20.sp)
                    }
                    Column {
                        Text(sessionModel.tr("global_challenge_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(sessionModel.tr("global_challenge_sub"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isPublicChallengeJoined) Color(0xFF00E676).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
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
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#오늘도30초휴식 공개 챌린지 🌍", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Button(
                            onClick = {
                                if (!isPublicChallengeJoined) {
                                    isPublicChallengeJoined = true
                                    publicChallengeParticipants++
                                    Toast.makeText(context, "🎉 #오늘도30초휴식 공개 챌린지에 정식 참여했습니다! (+50 눈 걸음 적립)", Toast.LENGTH_SHORT).show()
                                } else {
                                    isPublicChallengeJoined = false
                                    publicChallengeParticipants--
                                    Toast.makeText(context, "공개 챌린지 참여를 취소했습니다.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isPublicChallengeJoined) sessionModel.tr("global_challenge_joined") else sessionModel.tr("global_challenge_join"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
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
        }
    }
}
