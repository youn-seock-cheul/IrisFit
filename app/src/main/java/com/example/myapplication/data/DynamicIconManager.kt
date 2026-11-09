package com.example.myapplication.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

data class AppIconTheme(
    val index: Int,
    val aliasName: String,
    val title: String,
    val description: String,
    val emoji: String
)

object DynamicIconManager {

    val iconThemes = listOf(
        AppIconTheme(1, "com.example.myapplication.MainActivityDefault", "기본 정상 아이콘", "기본 시안 네온 아이콘", "👁️"),
        AppIconTheme(2, "com.example.myapplication.MainActivityTired", "피곤한 충혈 눈", "오래 안 사용하여 피곤한 눈", "😴"),
        AppIconTheme(3, "com.example.myapplication.MainActivityFire", "불타는 눈 스트릭", "연속 운동 달성 상태", "🔥"),
        AppIconTheme(4, "com.example.myapplication.MainActivityGold", "황금 챔피언", "Lv.3 달성 황금 왕관 테마", "🏆"),
        AppIconTheme(5, "com.example.myapplication.MainActivityCyber", "사이버 네온", "네온 핑크 & 바이올렛 테마", "⚡"),
        AppIconTheme(6, "com.example.myapplication.MainActivityBlink", "깜빡임 마스터", "눈 깜빡임 달성 테마", "👁️"),
        AppIconTheme(7, "com.example.myapplication.MainActivityEmerald", "에메랄드 포레스트", "자연 이완 그린 테마", "🌲"),
        AppIconTheme(8, "com.example.myapplication.MainActivityAmethyst", "아메지스트 자수정", "자수정 크리스탈 테마", "🔮"),
        AppIconTheme(9, "com.example.myapplication.MainActivityRuby", "루비 레드 집중", "강렬한 레드 딥 포커스", "🔴"),
        AppIconTheme(10, "com.example.myapplication.MainActivityGalaxy", "시리우스 은하", "우주 갤럭시 스페이스 테마", "🌌")
    )

    fun switchAppIcon(context: Context, targetThemeIndex: Int) {
        val pm = context.packageManager
        val selectedTheme = iconThemes.find { it.index == targetThemeIndex } ?: iconThemes[0]

        // Enable target alias and disable all other aliases
        iconThemes.forEach { theme ->
            val state = if (theme.index == selectedTheme.index) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }

            try {
                pm.setComponentEnabledSetting(
                    ComponentName(context, theme.aliasName),
                    state,
                    PackageManager.DONT_KILL_APP
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
