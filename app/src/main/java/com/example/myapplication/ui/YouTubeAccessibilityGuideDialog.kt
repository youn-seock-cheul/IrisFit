package com.example.myapplication.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun YouTubeAccessibilityGuideDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1D26),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = {
            Text("📺 안드로이드 13+ 접근성 제한 해제 방법", fontWeight = FontWeight.Bold, fontSize = 17.5.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "수동 APK 설치 시 안드로이드 보안 정책으로 인해 접근성이 일시 차단(제한된 설정)될 수 있습니다. 아래 10초 해결 방법을 따라 하시면 즉시 해제됩니다:",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF262B38))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("1단계: 앱 정보 '제한된 설정' 해제", fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("① 하단 [앱 정보 설정 바로가기] 버튼 클릭\n② 상단 오른쪽 점 3개(⋮) 메뉴 클릭\n③ '제한된 설정 허용' 클릭 및 지문/비밀번호 인증", fontSize = 11.5.sp, color = Color.LightGray)
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF262B38))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("2단계: 접근성 서비스 켜기", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("① [접근성 설정으로 이동] 클릭\n② [EyeOverlay AI - 유튜브 자동 눈 보호] 선택 후 '사용함' 스위치 켜기", fontSize = 11.5.sp, color = Color.LightGray)
                    }
                }

                Text(
                    "* 구글 플레이 스토어 정식 출시 버전 설치 시에는 이 차단 문구가 뜨지 않고 한 번에 켜집니다.",
                    fontSize = 10.5.sp,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("1. 앱 정보 설정", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("2. 접근성 이동", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기", color = Color.Gray)
            }
        }
    )
}
