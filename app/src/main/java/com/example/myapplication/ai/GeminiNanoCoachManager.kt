package com.example.myapplication.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ChatMessage(
    val sender: String, // "user" or "coach"
    val message: String,
    val timestampMs: Long = System.currentTimeMillis()
)

object GeminiNanoCoachManager {

    fun initModel(context: Context) {
        // Initialization for local AI coach engine
    }

    suspend fun getCoachResponse(userQuery: String, currentFatigueScore: Int): String = withContext(Dispatchers.IO) {
        generateLocalOphthalmologistResponse(userQuery, currentFatigueScore)
    }

    private fun generateLocalOphthalmologistResponse(query: String, fatigueScore: Int): String {
        val q = query.trim()
        return when {
            q.contains("뻑뻑") || q.contains("건조") || q.contains("마르") -> {
                "[공감 및 진단] 스마트폰 집중 시청으로 눈 깜빡임 빈도가 떨어져 안구 눈물막 지질층이 마른 안구건조증 상태입니다.\n\n" +
                        "[처방 가이드] IrisFit 20초 마커 따라보기 및 30초 눈 휴식을 시작하시고, 눈을 3초간 감았다가 '찡긋' 5회 깜빡여보세요.\n\n" +
                        "[의사 권고] 실내 습도를 50% 이상으로 유지하시고 온열 마사지를 병행해 주세요."
            }
            q.contains("침침") || q.contains("흐릿") || q.contains("초점") -> {
                "[공감 및 진단] 근거리 화면 집중으로 수정체 조절근(섬모체)이 일시적 과수축 피로 상태입니다.\n\n" +
                        "[처방 가이드] IrisFit [원근 조절 미니게임 (Eye Gym)]을 가동하여 수정체 조절근 피로를 해소해 보세요.\n\n" +
                        "[의사 권고] 20분 화면 시청 후 20초간 먼 곳을 주시하는 20-20-20 예방 수칙을 실천해 보세요."
            }
            q.contains("통증") || q.contains("아파") || q.contains("두통") -> {
                "[공감 및 진단] 안구를 조절하는 6개 외안근의 지속적 과수축 및 시신경 자극이 누적되어 안통이 발생했습니다.\n\n" +
                        "[처방 가이드] IrisFit [부드러운 시선 추적 훈련 (Infinity Pursuit)]을 통해 6개 안근육을 다각도로 유연하게 풀어주세요.\n\n" +
                        "[의사 권고] 극심한 안통이 지속되거나 급격한 시력 저하 동반 시 즉시 안과 병원에 방문하셔야 합니다."
            }
            else -> {
                "[공감 및 진단] 화면 장시간 응시로 안구 주변 외안근 및 조절근에 피로가 축적된 상황입니다.\n\n" +
                        "[처방 가이드] IrisFit [30초 눈 휴식]과 [시선 추적 미니게임]을 가동하여 피로를 완화해 보세요.\n\n" +
                        "[의사 권고] 수분을 자주 섭취하시고 조명을 밝게 유지하는 것이 도움이 됩니다."
            }
        }
    }

    fun close() {
        // Cleanup resources
    }
}
