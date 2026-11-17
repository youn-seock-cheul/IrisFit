package com.example.myapplication.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.widget.RemoteViews
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.LanguageManager
import com.example.myapplication.data.ScreenTimeManager
import com.example.myapplication.session.AppLocalization
import com.example.myapplication.session.EyeBatteryCalculator
import com.example.myapplication.session.FileCompletionStorage
import java.time.LocalDate

open class BitmapWidgetProvider(private val layoutResId: Int, private val widgetIndex: Int) : AppWidgetProvider() {

    companion object {
        /** Refresh the first (today summary) widget after app data changes. */
        fun updateWidget1(context: Context) {
            updateProvider(context, IrisFitWidget1Provider::class.java) { IrisFitWidget1Provider() }
        }

        /** Refresh the only retained home-screen widget (Widget 1). */
        fun updateAll(context: Context) = updateWidget1(context)

        private fun <T : BitmapWidgetProvider> updateProvider(
            context: Context,
            providerClass: Class<T>,
            factory: () -> T
        ) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, providerClass))
            val provider = factory()
            ids.forEach { id -> provider.updateSingleWidget(context, manager, id) }
        }
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateSingleWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle?) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateSingleWidget(context, appWidgetManager, appWidgetId)
    }

    internal fun updateSingleWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        try {
            val targetScreen = if (widgetIndex == 3) "gym" else "home"
            val intent = Intent(context, MainActivity::class.java).apply {
                putExtra("target_screen", targetScreen)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context, widgetIndex, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, layoutResId)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            val queryResult = try { ScreenTimeManager.queryTodayScreenTime(context) } catch (_: Exception) { com.example.myapplication.data.ScreenTimeQueryResult.QueryError("Widget Error") }
            val recordsResult = try {
                Pair(FileCompletionStorage.repository(context).records(), true)
            } catch (_: Exception) {
                Pair(emptyList(), false)
            }
            val records = recordsResult.first
            val recordsSuccess = recordsResult.second

            val (screenTime, isMeasured) = when (queryResult) {
                is com.example.myapplication.data.ScreenTimeQueryResult.Success -> Pair(queryResult.minutes, recordsSuccess)
                else -> Pair(0, false)
            }
            val today = LocalDate.now().toString()
            val rests = records.count { it.completedLocalDate == today && it.routineId == "screen_rest" }
            val miniGames = records.count { it.completedLocalDate == today && (it.routineId == "eye_gym" || it.routineId == "smooth_pursuit") }

            val stats = EyeBatteryCalculator.calculateBattery(
                screenTimeMinutes = screenTime,
                restCount = rests,
                miniGameCount = miniGames,
                screenTimeMeasured = isMeasured
            )
            val selectedLang = LanguageManager.getSelectedLanguage(context)
            val effectiveLang = AppLocalization.resolveEffectiveLang(selectedLang)

            val bitmap = renderMasterpieceWidgetBitmap(context, widgetIndex, stats.eyeHealthScore, rests, miniGames, effectiveLang)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun renderMasterpieceWidgetBitmap(context: Context, index: Int, score: Int, rests: Int, games: Int, lang: String): Bitmap {
        val width = 800
        val height = if (index in listOf(1, 2, 3, 4)) 380 else 180
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply { isAntiAlias = true }

        // Dark Navy Glassmorphism Background Card
        paint.color = Color.parseColor("#121924")
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), 36f, 36f, paint)

        when (index) {
            1 -> { // Widget 1: 오늘의 눈 건강 요약 (중형, 단색/평면 디자인)
                // 단색 네이비 카드: 그림자, 그라데이션, 애니메이션을 사용하지 않습니다.
                paint.style = Paint.Style.FILL
                paint.textAlign = Paint.Align.LEFT

                // Header
                paint.color = Color.WHITE
                paint.textSize = 38f
                paint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText("IrisFit", 42f, 62f, paint)

                paint.color = Color.parseColor("#B0BEC5")
                paint.textSize = 23f
                paint.typeface = Typeface.DEFAULT
                val greeting = if (lang == "en") "Good work today!" else "오늘도 수고했어요!"
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(greeting, width - 42f, 62f, paint)

                // 4-Tier Dynamic Color depending on Eye Health Battery Score (or Neutral Grey if unmeasured)
                val statusColor = when {
                    score < 0 -> Color.parseColor("#A0A0A0")   // Neutral Grey (Unmeasured)
                    score >= 85 -> Color.parseColor("#00E676") // Bright Green (Optimal 85~100%)
                    score >= 65 -> Color.parseColor("#FFC107") // Yellow (Caution 65~84%)
                    score >= 45 -> Color.parseColor("#FF9800") // Orange (Warning 45~64%)
                    else -> Color.parseColor("#FF5252")        // Rose Red (Danger 10~44%)
                }

                // Battery gauge: simple flat circular progress
                val cx = 165f
                val cy = 218f
                val radius = 88f
                val ringRect = RectF(cx - radius, cy - radius, cx + radius, cy + radius)

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 16f
                paint.strokeCap = Paint.Cap.ROUND
                paint.color = Color.parseColor("#344253")
                canvas.drawArc(ringRect, -90f, 360f, false, paint)

                paint.color = statusColor
                if (score >= 0) {
                    canvas.drawArc(ringRect, -90f, score.coerceIn(0, 100) * 3.6f, false, paint)
                }

                paint.style = Paint.Style.FILL
                paint.textAlign = Paint.Align.CENTER
                paint.color = Color.WHITE
                paint.textSize = if (score < 0) 32f else 48f
                paint.typeface = Typeface.DEFAULT_BOLD
                val scoreText = if (score < 0) AppLocalization.get(lang, "screen_time_unmeasured") else "${score}%"
                canvas.drawText(scoreText, cx, cy + 12f, paint)

                paint.color = Color.parseColor("#B0BEC5")
                paint.textSize = 20f
                paint.typeface = Typeface.DEFAULT
                val batteryLabel = if (lang == "en") "Eye battery" else "눈 건강 배터리"
                canvas.drawText(batteryLabel, cx, cy + 48f, paint)

                // Divider
                paint.color = Color.parseColor("#344253")
                canvas.drawRect(310f, 100f, 312f, 335f, paint)

                // Real completion rate from today's rest/exercise goals.
                val restProgress = (rests.coerceAtLeast(0).coerceAtMost(5) / 5f)
                val gameProgress = (games.coerceAtLeast(0).coerceAtMost(3) / 3f)
                val completionRate = (((restProgress + gameProgress) / 2f) * 100f).toInt()

                val labelX = 355f
                val valueX = 748f
                val rowYs = listOf(155f, 225f, 295f)
                val labels = if (lang == "en") {
                    listOf("Rest completed", "Eye exercise", "Daily completion")
                } else {
                    listOf("휴식 완료", "눈 운동 완료", "오늘 목표 달성률")
                }
                val values = if (lang == "en") {
                    listOf("$rests / 5", "$games / 3", "$completionRate%")
                } else {
                    listOf("$rests / 5회", "$games / 3회", "$completionRate%")
                }

                labels.forEachIndexed { i, label ->
                    paint.textAlign = Paint.Align.LEFT
                    paint.textSize = 25f
                    paint.typeface = Typeface.DEFAULT
                    paint.color = Color.parseColor("#D5DCE6")
                    canvas.drawText(label, labelX, rowYs[i], paint)

                    paint.textAlign = Paint.Align.RIGHT
                    paint.textSize = 29f
                    paint.typeface = Typeface.DEFAULT_BOLD
                    paint.color = if (i == 2) Color.parseColor("#00D084") else Color.WHITE
                    canvas.drawText(values[i], valueX, rowYs[i], paint)

                    if (i < 2) {
                        paint.color = Color.parseColor("#293545")
                        canvas.drawRect(labelX, rowYs[i] + 19f, valueX, rowYs[i] + 21f, paint)
                    }
                }

                // Small progress bars under the two activity counts
                val barLeft = labelX
                val barRight = valueX
                val barWidth = barRight - barLeft
                val barHeight = 7f
                val barY1 = 178f
                val barY2 = 248f

                paint.color = Color.parseColor("#344253")
                canvas.drawRoundRect(RectF(barLeft, barY1, barRight, barY1 + barHeight), 4f, 4f, paint)
                canvas.drawRoundRect(RectF(barLeft, barY2, barRight, barY2 + barHeight), 4f, 4f, paint)

                paint.color = Color.parseColor("#29B6F6")
                canvas.drawRoundRect(
                    RectF(barLeft, barY1, barLeft + barWidth * restProgress, barY1 + barHeight),
                    4f, 4f, paint
                )
                canvas.drawRoundRect(
                    RectF(barLeft, barY2, barLeft + barWidth * gameProgress, barY2 + barHeight),
                    4f, 4f, paint
                )
            }
            2 -> { // Widget 2: 주간 눈 건강 트렌드 (중형)
                paint.color = Color.WHITE
                paint.textSize = 34f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textAlign = Paint.Align.LEFT
                val tTitle = if (lang == "en") "📊 Weekly Eye Health Trend" else "📊 주간 눈 건강 트렌드"
                canvas.drawText(tTitle, 44f, 65f, paint)

                paint.color = Color.parseColor("#90A4AE")
                paint.textSize = 24f
                paint.typeface = Typeface.DEFAULT
                val subText = if (lang == "en") "This Week >" else "이번 주 >"
                canvas.drawText(subText, width - 200f, 65f, paint)

                val scores = listOf(70, 70, 70, 70, 100, 100, 70)
                val days = if (lang == "en") listOf("M", "T", "W", "T", "F", "S", "S") else listOf("월", "화", "수", "목", "금", "토", "일")

                val bWidth = 46f
                val gap = (width - 80f - (bWidth * 7)) / 8f
                val chartBottom = 270f

                scores.forEachIndexed { i, s ->
                    val x = 40f + gap + i * (bWidth + gap)
                    val bh = (s / 100f) * 140f
                    val top = chartBottom - bh

                    // Percentage on top of bar
                    paint.color = Color.parseColor("#B0BEC5")
                    paint.textSize = 18f
                    paint.textAlign = Paint.Align.CENTER
                    canvas.drawText("${s}%", x + bWidth / 2f, top - 8f, paint)

                    // Bar
                    paint.color = if (s >= 90) Color.parseColor("#00E676") else Color.parseColor("#29B6F6")
                    canvas.drawRoundRect(RectF(x, top, x + bWidth, chartBottom), 10f, 10f, paint)

                    // Day label
                    paint.color = Color.parseColor("#90A4AE")
                    paint.textSize = 22f
                    canvas.drawText(days[i], x + bWidth / 2f, 310f, paint)
                }

                paint.color = Color.parseColor("#00E676")
                paint.textSize = 24f
                paint.textAlign = Paint.Align.CENTER
                val tComp = if (lang == "en") "📈 +20% vs Last Week" else "📈 지난주 대비 +20%"
                canvas.drawText(tComp, width / 2f, 355f, paint)
            }
            3 -> { // Widget 3: 원터치 눈 운동 (중형 - 디자인 3)
                // Left Title & Subtitle
                paint.color = Color.WHITE
                paint.textSize = 38f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textAlign = Paint.Align.LEFT
                val tMain = if (lang == "en") "Start Eye Gym!" else "눈 운동 시작!"
                canvas.drawText(tMain, 140f, 180f, paint)

                paint.color = Color.parseColor("#90A4AE")
                paint.textSize = 24f
                paint.typeface = Typeface.DEFAULT
                val tSub = if (lang == "en") "Start right now" else "지금 바로 시작해보세요"
                canvas.drawText(tSub, 140f, 225f, paint)

                // Large Eye Icon on Left
                paint.color = Color.parseColor("#29B6F6")
                canvas.drawCircle(80f, 195f, 36f, paint)
                paint.color = Color.WHITE
                canvas.drawCircle(80f, 195f, 16f, paint)

                // Right Blue Action Button: ▶ 눈 운동 시작
                val btnL = 460f
                val btnT = 150f
                val btnR = 750f
                val btnB = 240f
                paint.color = Color.parseColor("#29B6F6")
                canvas.drawRoundRect(RectF(btnL, btnT, btnR, btnB), 20f, 20f, paint)

                paint.color = Color.WHITE
                paint.textSize = 28f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textAlign = Paint.Align.CENTER
                val btnText = if (lang == "en") "▶ Start Eye Gym" else "▶ 눈 운동 시작"
                canvas.drawText(btnText, btnL + ((btnR - btnL) / 2f), btnT + 58f, paint)
            }
            4 -> { // Widget 4: 연속 휴식 스트릭 (중형)
                // Flame Badge
                paint.color = Color.parseColor("#FF9800")
                paint.textSize = 32f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textAlign = Paint.Align.LEFT
                val t1 = if (lang == "en") "🔥 Rest Streak" else "🔥 연속 휴식 스트릭"
                canvas.drawText(t1, 44f, 80f, paint)

                paint.color = Color.WHITE
                paint.textSize = 64f
                val t2 = if (lang == "en") "7 Days!" else "7일째!"
                canvas.drawText(t2, 44f, 175f, paint)

                paint.color = Color.parseColor("#90A4AE")
                paint.textSize = 24f
                paint.typeface = Typeface.DEFAULT
                val t3 = if (lang == "en") "Consistent rest protects vision!" else "꾸준한 눈 관리가\n눈 건강을 지켜줘요!"
                canvas.drawText(t3, 44f, 250f, paint)

                // Right Golden Trophy & Next Target Badge
                paint.color = Color.parseColor("#FFC107")
                val trophyL = 580f
                val trophyT = 80f
                canvas.drawRoundRect(RectF(trophyL, trophyT, trophyL + 140f, trophyT + 120f), 28f, 28f, paint)
                canvas.drawRect(RectF(trophyL + 40f, trophyT + 120f, trophyL + 100f, trophyT + 160f), paint)
                canvas.drawRect(RectF(trophyL + 20f, trophyT + 160f, trophyL + 120f, trophyT + 180f), paint)

                paint.color = Color.parseColor("#90A4AE")
                paint.textSize = 22f
                paint.textAlign = Paint.Align.CENTER
                val tNext = if (lang == "en") "Next Goal" else "다음 목표"
                canvas.drawText(tNext, trophyL + 70f, 240f, paint)

                paint.color = Color.parseColor("#FFC107")
                paint.textSize = 32f
                paint.typeface = Typeface.DEFAULT_BOLD
                val t14 = if (lang == "en") "14 Days" else "14일"
                canvas.drawText(t14, trophyL + 70f, 285f, paint)
            }
            5 -> { // Widget 5: 오늘의 눈 건강 미션 (소형 - Design 5)
                // Left Green Check Circle
                paint.color = Color.parseColor("#00E676")
                canvas.drawCircle(70f, 90f, 32f, paint)
                paint.color = Color.WHITE
                paint.textSize = 32f
                paint.textAlign = Paint.Align.CENTER
                canvas.drawText("✓", 70f, 101f, paint)

                // Center Title & Subtitle
                paint.textAlign = Paint.Align.LEFT
                paint.color = Color.WHITE
                paint.textSize = 28f
                paint.typeface = Typeface.DEFAULT_BOLD
                val tTitle = if (lang == "en") "Today's Mission" else "오늘의 미션"
                canvas.drawText(tTitle, 130f, 75f, paint)

                paint.color = Color.parseColor("#B0BEC5")
                paint.textSize = 22f
                paint.typeface = Typeface.DEFAULT
                val tSub = if (lang == "en") "3 PM: 30s Eye Rest" else "오후 3시, 눈 휴식하기"
                canvas.drawText(tSub, 130f, 120f, paint)

                // Right Completed Badge
                val badgeL = 620f
                paint.color = Color.parseColor("#1B3A2B")
                canvas.drawRoundRect(RectF(badgeL, 45f, 750f, 135f), 20f, 20f, paint)
                paint.color = Color.parseColor("#00E676")
                paint.textSize = 24f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textAlign = Paint.Align.CENTER
                val t3 = if (lang == "en") "Done!" else "완료!"
                canvas.drawText(t3, badgeL + 65f, 100f, paint)
            }
            else -> { // Widget 6: 스마트폰 미니 위젯 (소형 - Design 6)
                // Left Eye Icon
                paint.color = Color.parseColor("#29B6F6")
                canvas.drawCircle(60f, 90f, 24f, paint)
                paint.color = Color.WHITE
                canvas.drawCircle(60f, 90f, 10f, paint)

                // Score Text
                paint.color = Color.WHITE
                paint.textSize = 46f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textAlign = Paint.Align.LEFT
                canvas.drawText("${score}%", 100f, 95f, paint)

                paint.color = Color.parseColor("#B0BEC5")
                paint.textSize = 20f
                paint.typeface = Typeface.DEFAULT
                val tSub = if (lang == "en") "Eye Battery" else "눈 건강 배터리"
                canvas.drawText(tSub, 100f, 132f, paint)

                // Right Blue Button: 눈 운동
                val btnL = 540f
                val btnT = 45f
                val btnR = 750f
                val btnB = 135f
                paint.color = Color.parseColor("#29B6F6")
                canvas.drawRoundRect(RectF(btnL, btnT, btnR, btnB), 20f, 20f, paint)

                paint.color = Color.WHITE
                paint.textSize = 26f
                paint.typeface = Typeface.DEFAULT_BOLD
                paint.textAlign = Paint.Align.CENTER
                val btnText = if (lang == "en") "Eye Gym" else "눈 운동"
                canvas.drawText(btnText, btnL + 105f, 100f, paint)
            }
        }

        return bitmap
    }
}

class IrisFitWidget1Provider : BitmapWidgetProvider(R.layout.iris_fit_widget_1, 1)
