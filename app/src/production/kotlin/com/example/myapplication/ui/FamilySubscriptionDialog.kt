package com.example.myapplication.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.FamilySubscriptionManager
import com.example.myapplication.data.model.GroupActionResult
import com.example.myapplication.data.model.PlanType

@Composable
fun FamilySubscriptionDialog(
    onDismiss: () -> Unit,
    onPurchaseClicked: () -> Unit
) {
    val context = LocalContext.current
    val statusState by FamilySubscriptionManager.subscriptionStatus.collectAsState()
    var inviteCodeInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "가족/동반자 1:4 공유 구독",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 플랜 상태 카드
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = when (statusState.planType) {
                                PlanType.HOST -> "👑 방장 (Host) - 정기 구독 결제 중"
                                PlanType.FAMILY_MEMBER -> "👥 공유 멤버 (Guest) - Pro 이용 중"
                                PlanType.TRIAL -> "🎁 3개월 무료 체험 진행 중 (${statusState.trialDaysRemaining}일 남음)"
                                PlanType.FREE -> "🌱 무료 플랜 (기본 기능 제공)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (statusState.planType == PlanType.HOST && statusState.inviteCode != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "내 6자리 초대 코드",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusState.inviteCode!!,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("IrisFit Invite Code", statusState.inviteCode)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "초대 코드가 복사되었습니다!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "코드 복사"
                                    )
                                }
                            }
                            Text(
                                text = "동반자 포함 최대 4명까지 이용 가능합니다.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // 멤버 (Guest) 6자리 초대 코드 입력 섹션
                if (statusState.planType == PlanType.FREE || statusState.planType == PlanType.TRIAL) {
                    HorizontalDivider()
                    Text(
                        text = "초대 받은 6자리 코드가 있나요?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    OutlinedTextField(
                        value = inviteCodeInput,
                        onValueChange = { if (it.length <= 6) inviteCodeInput = it.uppercase() },
                        label = { Text("6자리 초대 코드 입력") },
                        placeholder = { Text("예: 8F3K9A") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            FamilySubscriptionManager.joinGroupWithCode(context, inviteCodeInput) { result ->
                                when (result) {
                                    is GroupActionResult.Success -> {
                                        statusMessage = result.message
                                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                                    }
                                    is GroupActionResult.Error -> {
                                        statusMessage = result.errorMessage
                                    }
                                }
                            }
                        },
                        enabled = inviteCodeInput.length == 6,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("초대 코드로 멤버 합류하기")
                    }
                }

                if (statusMessage.isNotBlank()) {
                    Text(
                        text = statusMessage,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (statusState.planType != PlanType.HOST) {
                Button(onClick = onPurchaseClicked) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("매달 $4/월 구독하기 (방장)")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}
