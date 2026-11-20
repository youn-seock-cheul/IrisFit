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
import com.example.myapplication.session.AppLocalization
import com.example.myapplication.session.EyeBatteryCalculator
import com.example.myapplication.session.EyeBatterySnapshotStore
import com.example.myapplication.session.EyeBatteryStats
import com.example.myapplication.session.EyeFatigueStatus
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

            val recordsResult = try {
                Pair(FileCompletionStorage.repository(context).records(), true)
            } catch (_: Exception) {
                Pair(emptyList(), false)
            }
            val records = recordsResult.first
            val recordsSuccess = recordsResult.second

            val today = LocalDate.now().toString()
            val rests = records.count { it.completedLocalDate == today && it.routineId == "screen_rest" }
            val miniGames = records.count { it.completedLocalDate == today && (it.routineId == "eye_gym" || it.routineId == "smooth_pursuit") }

            val snapshot = EyeBatterySnapshotStore.readToday(context)
            val stats = if (snapshot == null) EyeBatteryCalculator.calculateBattery(
                screenTimeMinutes = 0,
                restCount = rests,
                miniGameCount = miniGames,
                screenTimeMeasured = false
            ) else EyeBatteryStats(
                screenTimeMinutes = snapshot.screenTimeMinutes,
                screenTimeMeasured = true,
                restCount = rests,
                miniGameCount = miniGames,
                eyeHealthScore = snapshot.score,
                fatigueStatus = EyeFatigueStatus.fromScore(snapshot.score)
            )
            val selectedLang = LanguageManager.effectiveLanguage(context)
            val effectiveLang = AppLocalization.resolveEffectiveLang(selectedLang)

            val bitmap = renderMasterpieceWidgetBitmap(context, widgetIndex, stats.eyeHealthScore, rests, miniGames, effectiveLang, recordsSuccess)
            views.setImageViewBitmap(R.id.widget_image, bitmap)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun renderMasterpieceWidgetBitmap(context: Context, index: Int, score: Int, rests: Int, games: Int, lang: String, recordsSuccess: Boolean = true): Bitmap {
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
                val greeting = when (lang) {
                    "en" -> "Good work today!"
                    "ja" -> "今日も一日お疲れ様でした！"
                    "es" -> "¡Buen trabajo hoy!"
                    else -> "오늘도 수고했어요!"
                }
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(greeting, width - 42f, 62f, paint)

                // 4-Tier Dynamic Color depending on Eye Health Battery Score (or Neutral Grey if unmeasured)
                val statusColor = when (EyeFatigueStatus.fromScore(score)) {
                    EyeFatigueStatus.UNMEASURED -> Color.parseColor("#A0A0A0")
                    EyeFatigueStatus.OPTIMAL -> Color.parseColor("#00E676")
                    EyeFatigueStatus.CAUTION -> Color.parseColor("#FFC107")
                    EyeFatigueStatus.WARNING -> Color.parseColor("#FF9800")
                    EyeFatigueStatus.DANGER -> Color.parseColor("#FF5252")
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
                val scoreText = if (score < 0) AppLocalization.get(lang, "미측정") else "${score}%"
                canvas.drawText(scoreText, cx, cy + 12f, paint)

                paint.color = Color.parseColor("#B0BEC5")
                paint.textSize = 20f
                paint.typeface = Typeface.DEFAULT
                val batteryLabel = when (lang) {
                    "en" -> "Eye Health Battery"
                    "ja" -> "目の健康バッテリー"
                    "es" -> "Batería de salud ocular"
                    else -> "눈 건강 배터리"
                }
                canvas.drawText(batteryLabel, cx, cy + 48f, paint)

                // Divider
                paint.color = Color.parseColor("#344253")
                canvas.drawRect(310f, 100f, 312f, 335f, paint)

                // Real completion rate from today's rest/exercise goals (Independent from ScreenTime query failure)
                val unmeasuredText = AppLocalization.get(lang, "미측정")
                val restProgress = (rests.coerceAtLeast(0).coerceAtMost(5) / 5f)
                val gameProgress = (games.coerceAtLeast(0).coerceAtMost(3) / 3f)
                val completionRate = (((restProgress + gameProgress) / 2f) * 100f).toInt()

                val labelX = 355f
                val valueX = 748f
                val rowYs = listOf(155f, 225f, 295f)
                val labels = when (lang) {
                    "en" -> listOf("Rest completed", "Eye training", "Daily completion")
                    "ja" -> listOf("休憩完了", "目のトレーニング", "今日の達成率")
                    "es" -> listOf("Descanso completado", "Entrenamiento ocular", "Meta diaria")
                    else -> listOf("휴식 완료", "눈동자 훈련 완료", "오늘 목표 달성률")
                }
                val values = if (!recordsSuccess) {
                    listOf(unmeasuredText, unmeasuredText, unmeasuredText)
                } else when (lang) {
                    "en" -> listOf("$rests / 5", "$games / 3", "$completionRate%")
                    "ja" -> listOf("$rests / 5回", "$games / 3回", "$completionRate%")
                    "es" -> listOf("$rests / 5", "$games / 3", "$completionRate%")
                    else -> listOf("$rests / 5회", "$games / 3회", "$completionRate%")
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
            else -> error("Only the today-summary widget is supported: $index")
        }

        return bitmap
    }
}

class IrisFitWidget1Provider : BitmapWidgetProvider(R.layout.iris_fit_widget_1, 1)
