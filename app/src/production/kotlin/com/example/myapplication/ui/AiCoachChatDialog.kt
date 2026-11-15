package com.example.myapplication.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.ai.ChatMessage
import com.example.myapplication.ai.GeminiNanoCoachManager
import kotlinx.coroutines.launch

@Composable
fun AiCoachChatDialog(
    currentFatigueScore: Int,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val messages = remember { mutableStateListOf<ChatMessage>() }
    var modelState by remember { mutableStateOf(GeminiNanoCoachManager.ModelState.UNKNOWN) }
    var input by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("기기에서 Gemini Nano 지원 여부를 확인하고 있습니다.") }

    LaunchedEffect(Unit) {
        modelState = GeminiNanoCoachManager.checkAvailability(context)
        statusText = when (modelState) {
            GeminiNanoCoachManager.ModelState.AVAILABLE -> "Gemini Nano 사용 가능 · 답변은 기기 내에서 생성됩니다."
            GeminiNanoCoachManager.ModelState.DOWNLOADABLE -> "이 기기는 지원됩니다. 최초 1회 모델 다운로드가 필요합니다."
            GeminiNanoCoachManager.ModelState.DOWNLOADING -> "모델 다운로드가 진행 중입니다."
            GeminiNanoCoachManager.ModelState.UNAVAILABLE -> "이 기기는 Gemini Nano를 지원하지 않습니다."
            else -> "AICore 상태를 확인하지 못했습니다. 기기 업데이트 후 다시 확인해 주세요."
        }
        messages.add(ChatMessage("coach", "안녕하세요. 저는 기기 내 Gemini Nano를 사용하는 IrisFit 생활습관 코치입니다. 증상에 대한 진단이 아닌 일반적인 눈 건강 관리 정보를 안내합니다."))
    }

    fun send() {
        val question = input.trim()
        if (question.isEmpty() || busy) return
        input = ""
        messages.add(ChatMessage("user", question))
        busy = true
        scope.launch {
            val answer = GeminiNanoCoachManager.getCoachResponse(question, currentFatigueScore)
            messages.add(ChatMessage("coach", answer))
            busy = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gemini Nano 눈 건강 코치") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(statusText, style = MaterialTheme.typography.bodySmall)
                if (modelState == GeminiNanoCoachManager.ModelState.DOWNLOADABLE) {
                    Button(
                        enabled = !busy,
                        onClick = {
                            busy = true
                            scope.launch {
                                modelState = GeminiNanoCoachManager.downloadModel(context) { downloaded, total ->
                                    statusText = if (total > 0L) "모델 다운로드 시작 (${total / 1_000_000} MB)" else "모델 다운로드 중 (${downloaded / 1_000_000} MB)"
                                }
                                statusText = if (modelState == GeminiNanoCoachManager.ModelState.AVAILABLE) "Gemini Nano 준비 완료 · 기기 내에서 답변합니다." else "모델 다운로드에 실패했습니다. 네트워크와 AICore 상태를 확인해 주세요."
                                busy = false
                            }
                        }
                    ) { Text("Gemini Nano 모델 다운로드") }
                } else if (modelState == GeminiNanoCoachManager.ModelState.ERROR) {
                    TextButton(enabled = !busy, onClick = {
                        busy = true
                        scope.launch {
                            modelState = GeminiNanoCoachManager.checkAvailability(context)
                            statusText = when (modelState) {
                                GeminiNanoCoachManager.ModelState.AVAILABLE -> "Gemini Nano 사용 가능 · 기기 내에서 답변합니다."
                                GeminiNanoCoachManager.ModelState.DOWNLOADABLE -> "모델 다운로드가 필요합니다."
                                GeminiNanoCoachManager.ModelState.UNAVAILABLE -> "이 기기는 Gemini Nano를 지원하지 않습니다."
                                else -> "AICore 상태를 다시 확인하지 못했습니다."
                            }
                            busy = false
                        }
                    }) { Text("지원 상태 다시 확인") }
                }
                LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(messages) { message ->
                        Text(
                            text = (if (message.sender == "user") "나: " else "코치: ") + message.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                if (busy) CircularProgressIndicator()
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.take(600) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("눈 건강 관련 질문") },
                    enabled = !busy && modelState == GeminiNanoCoachManager.ModelState.AVAILABLE,
                    maxLines = 3
                )
                Button(onClick = { send() }, enabled = !busy && modelState == GeminiNanoCoachManager.ModelState.AVAILABLE && input.isNotBlank(), modifier = Modifier.fillMaxWidth()) {
                    Text("질문 보내기")
                }
                Text("참고: 앱 점수와 AI 답변은 의료 진단이 아닙니다. 갑작스러운 시력 변화나 심한 통증은 즉시 진료받으세요.", style = MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("닫기") } }
    )
}
