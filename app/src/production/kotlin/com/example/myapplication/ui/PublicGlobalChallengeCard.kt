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
        modifier = Modifier.fillMaxWidth().clickable { showBetaNoticeDialog = true },
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
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().clickable {
                    showBetaNoticeDialog = true
                }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(sessionModel.tr("#오늘도30초휴식 공개 챌린지 🌍"), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Button(
                            onClick = {
                                showBetaNoticeDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = sessionModel.tr("global_challenge_join"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Text(
                        text = "참여자 3,420명 참여 중 • 탭하여 베타 준비 알림 확인",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}
