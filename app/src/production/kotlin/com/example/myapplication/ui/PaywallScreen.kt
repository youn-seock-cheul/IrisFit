package com.example.myapplication.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.model.PlanType
import com.example.myapplication.model.SubscriptionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    subscriptionState: SubscriptionState,
    onSubscribeClicked: (String) -> Unit,
    onOpenCompanionGroupClicked: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FamilySubscriptionSystem 넷플릭스형 유료 패스", fontWeight = FontWeight.Bold, fontSize = 17.sp) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Status Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = when (subscriptionState.planType) {
                        PlanType.FAMILY_HOST, PlanType.FAMILY_MEMBER -> MaterialTheme.colorScheme.primaryContainer
                        PlanType.FREE_TRIAL -> MaterialTheme.colorScheme.secondaryContainer
                        PlanType.EXPIRED, PlanType.FREE -> MaterialTheme.colorScheme.errorContainer
                    }
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (subscriptionState.planType) {
                            PlanType.FAMILY_HOST, PlanType.FAMILY_MEMBER -> Icons.Default.CheckCircle
                            PlanType.FREE_TRIAL -> Icons.Default.Schedule
                            PlanType.EXPIRED, PlanType.FREE -> Icons.Default.Lock
                        },
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = when (subscriptionState.planType) {
                                PlanType.FAMILY_HOST -> "가족 패스 방장(HOST) 구독 중 ($4/월)"
                                PlanType.FAMILY_MEMBER -> "가족 패스 멤버(MEMBER) 이용 중"
                                PlanType.FREE_TRIAL -> "3개월 무료 체험 진행 중"
                                PlanType.EXPIRED, PlanType.FREE -> "무료 회원 (Pro 유료 잠금 중)"
                            },
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = when (subscriptionState.planType) {
                                PlanType.FAMILY_HOST -> "모든 Pro 기능 및 가족/연인 3명 공유 권한이 활성화되어 있습니다."
                                PlanType.FAMILY_MEMBER -> "방장(${subscriptionState.hostEmail ?: "Host"}) 님의 상속 권한으로 모든 Pro 기능을 이용 중입니다."
                                PlanType.FREE_TRIAL -> "설치 후 3개월(90일) 동안 모든 기능을 비용없이 사용 가능합니다. (남은 기한: ${subscriptionState.trialDaysRemaining}일)"
                                PlanType.EXPIRED, PlanType.FREE -> "유료 결제 시 원근조절, 시선추적, 헬스연동 및 프리미엄 색톤이 해제됩니다."
                            },
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "유료화 제공 4가지 Pro 핵심 기능",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            PaywallFeatureRow(
                icon = Icons.Default.Visibility,
                title = "1) 원근 조절 훈련 (Eye Gym Focus Shift)",
                description = "수정체 모양체근 수축/이완을 유도하는 입체 3D 원근 조절 훈련"
            )
            PaywallFeatureRow(
                icon = Icons.Default.Gesture,
                title = "2) 시선 추적 훈련 (Smooth Pursuit)",
                description = "안구 운동 근육 유연성과 가목성 추적 훈련"
            )
            PaywallFeatureRow(
                icon = Icons.Default.Favorite,
                title = "3) 설정 - 건강 데이터 연동 (Health Connect)",
                description = "수면, 걸음 수, 체력 연동으로 시력 배터리 정밀 산식 제공"
            )
            PaywallFeatureRow(
                icon = Icons.Default.Palette,
                title = "4) 앱 색톤 전체 이용 (프리미엄 4종)",
                description = "기본 색톤 외 다크/세라믹/티구안/바이올렛 프리미엄 색톤 선택"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pricing Options Box
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "월 $4.00 / 월간 패스 (또는 년 $40.00)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "🎁 넷플릭스형 1:N 공유: 방장(Host) 1명 결제 시 가족/연인 멤버(Guest) 3명까지 무료 합류 (총 4명)",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Button(
                onClick = { onSubscribeClicked("family_pass_monthly") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (subscriptionState.isUnlocked) "구독 관리 / 패밀리 패스" else "월간 패스 구독하기 ($4/월)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { onSubscribeClicked("family_pass_yearly") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "연간 패스 결제 ($40/년 - 2개월 할인)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onOpenCompanionGroupClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.GroupAdd, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "가족 3명 초대 / 멤버 초대 코드 입력",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PaywallFeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
