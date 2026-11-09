package com.example.myapplication.ui

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.DynamicIconManager
import com.example.myapplication.data.PreferencesManager

@Composable
fun DynamicIconSelectorDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }
    var selectedIconIdx by remember { mutableIntStateOf(prefsManager.selectedTrackModeIndex + 1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1D26),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = {
            Text("🎨 듀오링고 스타일 10가지 동적 앱 아이콘 테마", fontWeight = FontWeight.Bold, fontSize = 17.5.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "사용 내역이나 피로도에 따라 스마트폰 홈 화면 아이콘이 다채롭게 변경됩니다! 원하는 아이콘 테마를 직접 선택해 보세요.",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DynamicIconManager.iconThemes) { theme ->
                        val isSelected = (theme.index == selectedIconIdx)
                        Card(
                            onClick = {
                                selectedIconIdx = theme.index
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF3B82F6) else Color(0xFF262B38)
                            ),
                            border = BorderStroke(1.dp, if (isSelected) Color.White else Color(0x1AFFFFFF))
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(theme.emoji, fontSize = 20.sp)
                                    Text(theme.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(theme.description, fontSize = 10.sp, color = Color.LightGray)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    DynamicIconManager.switchAppIcon(context, selectedIconIdx)
                    Toast.makeText(context, "앱 아이콘 테마가 적용되었습니다! (홈 화면에서 확인 가능)", Toast.LENGTH_LONG).show()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
            ) {
                Text("아이콘 테마 적용하기")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소", color = Color.Gray)
            }
        }
    )
}
