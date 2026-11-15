package com.example.myapplication.ai

import android.content.Context
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerativeModel
import kotlinx.coroutines.flow.collect

data class ChatMessage(
    val sender: String,
    val message: String,
    val timestampMs: Long = System.currentTimeMillis()
)

/** Actual on-device Gemini Nano coach. No cloud Gemini API or API key is used. */
object GeminiNanoCoachManager {
    enum class ModelState { UNKNOWN, AVAILABLE, DOWNLOADABLE, DOWNLOADING, UNAVAILABLE, ERROR }

    @Volatile private var model: GenerativeModel? = null
    @Volatile private var state: ModelState = ModelState.UNKNOWN

    @Synchronized
    fun initModel(context: Context) {
        if (model == null) {
            // ML Kit routes inference to Android AICore; context is retained only by the SDK client.
            model = Generation.getClient()
        }
    }

    suspend fun checkAvailability(context: Context): ModelState {
        return try {
            initModel(context.applicationContext)
            when (model?.checkStatus()) {
                FeatureStatus.AVAILABLE -> ModelState.AVAILABLE
                FeatureStatus.DOWNLOADABLE -> ModelState.DOWNLOADABLE
                FeatureStatus.DOWNLOADING -> ModelState.DOWNLOADING
                FeatureStatus.UNAVAILABLE -> ModelState.UNAVAILABLE
                else -> ModelState.ERROR
            }.also { state = it }
        } catch (_: Exception) {
            state = ModelState.ERROR
            state
        }
    }

    suspend fun downloadModel(
        context: Context,
        onProgress: (downloadedBytes: Long, totalBytes: Long) -> Unit = { _, _ -> }
    ): ModelState {
        return try {
            initModel(context.applicationContext)
            state = ModelState.DOWNLOADING
            model!!.download().collect { progress ->
                when (progress) {
                    is DownloadStatus.DownloadStarted -> onProgress(0L, progress.bytesToDownload)
                    is DownloadStatus.DownloadProgress -> onProgress(progress.totalBytesDownloaded, 0L)
                    is DownloadStatus.DownloadCompleted -> state = ModelState.AVAILABLE
                    is DownloadStatus.DownloadFailed -> throw progress.e
                }
            }
            checkAvailability(context)
        } catch (_: Exception) {
            state = ModelState.ERROR
            state
        }
    }

    suspend fun getCoachResponse(userQuery: String, currentFatigueScore: Int): String {
        val query = userQuery.trim().take(600)
        if (query.isBlank()) return "궁금한 점을 한 문장으로 입력해 주세요."

        // Local safety routing takes precedence over generation for urgent red-flag symptoms.
        if (containsUrgentSymptom(query)) {
            return "갑작스러운 시력 저하·시야가 가려짐·심한 눈 통증·눈 외상 또는 화학물질 노출이 있다면 AI 안내를 기다리지 말고 즉시 응급실이나 안과 진료를 받으세요. 눈을 비비거나 임의로 약을 사용하지 마세요."
        }

        val activeModel = model ?: return unavailableMessage()
        return try {
            val prompt = """
                당신은 IrisFit의 한국어 눈 건강 생활습관 코치입니다. 온디바이스 AI로 생성하는 일반 웰니스 정보만 제공합니다.
                반드시 지킬 규칙:
                - 질환을 진단하거나 질환 가능성을 단정하지 마세요.
                - 검사 결과처럼 말하거나 약·치료법을 처방하지 마세요.
                - 사용자가 제공하지 않은 증상이나 건강정보를 추측하지 마세요.
                - 심한 통증, 갑작스러운 시력 변화, 시야 가림, 눈 외상, 화학물질 노출은 즉시 의료기관에 문의하도록 안내하세요.
                - 부담 없는 한국어로 3~6문장 이내로 답하고, 실행 가능한 생활습관 한두 가지를 제안하세요.
                - 앱 점수는 의료 측정값이 아닌 참고 지표라고 분명히 하세요.

                현재 앱 참고 피로 점수: ${currentFatigueScore.coerceIn(0, 100)}점 (의학적 측정값 아님)
                사용자 질문: $query
            """.trimIndent()
            val response = activeModel.generateContent(prompt)
            response.candidates.firstOrNull()?.text?.trim()?.takeIf { it.isNotBlank() }
                ?: "답변을 만들지 못했습니다. 질문을 조금 바꿔 다시 시도해 주세요."
        } catch (_: Exception) {
            "Gemini Nano 응답을 생성하지 못했습니다. 잠시 후 다시 시도해 주세요. 기기 지원 상태나 AICore 업데이트를 확인해 주세요."
        }
    }

    fun currentState(): ModelState = state

    @Synchronized
    fun close() {
        model?.close()
        model = null
        state = ModelState.UNKNOWN
    }

    private fun unavailableMessage(): String = when (state) {
        ModelState.DOWNLOADABLE -> "이 기기에서 Gemini Nano를 사용할 수 있지만 모델 다운로드가 필요합니다. 먼저 모델 다운로드를 진행해 주세요."
        ModelState.DOWNLOADING -> "Gemini Nano 모델을 다운로드하고 있습니다. 완료 후 다시 질문해 주세요."
        ModelState.UNAVAILABLE -> "현재 기기는 Gemini Nano를 지원하지 않습니다. 이 기기에서는 온디바이스 AI 코치를 사용할 수 없습니다."
        else -> "Gemini Nano 상태를 확인할 수 없습니다. Google Play 서비스와 AICore를 업데이트한 뒤 다시 확인해 주세요."
    }

    private fun containsUrgentSymptom(query: String): Boolean {
        val q = query.lowercase()
        val terms = listOf("갑자기 안 보여", "갑자기 안보여", "시력 상실", "시야가 가려", "시야가 안 보여", "시야가 안보여", "심한 눈 통증", "눈을 다쳤", "눈 외상", "화학물질", "세제가 들어", "산이 들어", "알칼리")
        return terms.any(q::contains)
    }
}
