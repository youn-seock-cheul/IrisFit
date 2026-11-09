package com.example.myapplication.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.ai.ChatMessage
import com.example.myapplication.ai.GeminiNanoCoachManager
import com.example.myapplication.data.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AiCoachChatDialog(
    currentFatigueScore: Int,
    onStartExercise: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var userMessageText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "coach",
                message = "안녕하세요! On-Device Gemini Nano AI 눈 건강 코치 eyeoverlay 입니다 👁️✨\n\n" +
                        "현재 사용자의 AI 예측 눈 피로도는 $currentFatigueScore% 입니다.\n" +
                        "오늘 눈 상태나 뻑뻑함, 침침함 등에 대해 편하게 질문해 주세요!"
            )
        )
    }

    val quickPrompts = listOf(
        "💧 오늘 눈이 뻑뻑하고 건조해",
        "👁️ 시야가 침침하고 초점이 안 맞아",
        "⚠️ 눈 주변이 지끈거리고 아파"
    )

    fun sendMessage(text: String) {
        if (text.isBlank() || isThinking) return
        val userQuery = text.trim()
        chatMessages.add(ChatMessage("user", userQuery))
        userMessageText = ""
        isThinking = true

        coroutineScope.launch {
            val coachFullReply = GeminiNanoCoachManager.getCoachResponse(userQuery, currentFatigueScore)

            // Streaming Typing Animation Effect Simulation
            val coachMsgIndex = chatMessages.size
            chatMessages.add(ChatMessage("coach", ""))

            var currentTyped = ""
            for (char in coachFullReply) {
                currentTyped += char
                chatMessages[coachMsgIndex] = ChatMessage("coach", currentTyped)
                delay(20L)
            }
            isThinking = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1D26),
        titleContentColor = Color.White,
        textContentColor = Color.LightGray,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🤖 AI 눈 건강 코치 (Gemini Nano)", fontWeight = FontWeight.Bold, fontSize = 17.5.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Visually Enhanced Quick Keyword Tag Chips
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("💡 빠른 피로 증상 질문 태그:", fontSize = 11.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPrompts.forEach { prompt ->
                            Button(
                                onClick = { sendMessage(prompt) },
                                modifier = Modifier.height(32.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF262B38), contentColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(prompt, fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                // Chat Log Messages
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val isCoach = (msg.sender == "coach")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (isCoach) Arrangement.Start else Arrangement.End
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCoach) Color(0xFF262B38) else Color(0xFF3B82F6)
                                    ),
                                    shape = RoundedCornerShape(
                                        topStart = 14.dp,
                                        topEnd = 14.dp,
                                        bottomStart = if (isCoach) 2.dp else 14.dp,
                                        bottomEnd = if (isCoach) 14.dp else 2.dp
                                    ),
                                    border = if (isCoach) BorderStroke(1.dp, Color(0x1AFFFFFF)) else null
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        if (isCoach) {
                                            Text(
                                                text = "🤖 AI 코치 추천 가이드",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF3B82F6)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }

                                        Text(
                                            text = msg.message,
                                            color = Color.White,
                                            fontSize = 12.5.sp,
                                            lineHeight = 18.sp
                                        )

                                        // Action Link Button inside Coach Response Bubble!
                                        if (isCoach && msg.message.contains("운동")) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Button(
                                                onClick = {
                                                    onDismiss()
                                                    onStartExercise()
                                                },
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(10.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                            ) {
                                                Text("✨ 추천 눈 운동 바로 시작하기 ➔", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (isThinking) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        color = Color(0xFF3B82F6),
                                        strokeWidth = 2.dp
                                    )
                                    Text("Gemini Nano AI 코치가 답변을 생각하고 있습니다...", fontSize = 11.5.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // Input Box & Send
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = userMessageText,
                        onValueChange = { userMessageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("눈 피로나 증상 질문...", fontSize = 12.sp, color = Color.Gray) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage(userMessageText) })
                    )

                    Button(
                        onClick = { sendMessage(userMessageText) },
                        enabled = userMessageText.isNotBlank() && !isThinking,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                    ) {
                        Text("전송")
                    }
                }

                // Legal Medical Disclaimer Notice
                Text(
                    text = "* 본 AI 코칭은 안과 전문의의 진료를 대체할 수 없으며, 질환의 진단 및 치료는 반드시 전문 의료기관을 방문하시기 바랍니다.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    lineHeight = 13.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기", color = Color.Gray)
            }
        }
    )
}
