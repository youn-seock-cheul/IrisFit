package com.example.myapplication.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.data.LanguageManager
import com.example.myapplication.data.ScreenTimeManager
import com.example.myapplication.session.AppLocalization
import com.example.myapplication.session.EyeBatteryCalculator
import com.example.myapplication.session.EyeFatigueStatus
import com.example.myapplication.session.FileCompletionStorage
import java.time.LocalDate

class IrisFitWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            try {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, IrisFitWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
                // Keep every registered bitmap widget in sync with newly saved activity records.
                try { BitmapWidgetProvider.updateAll(context) } catch (_: Exception) {}
            } catch (_: Exception) {}
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            try {
                val intent = Intent(context, MainActivity::class.java)
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val options = try { appWidgetManager.getAppWidgetOptions(appWidgetId) } catch (_: Exception) { null }
                val minWidth = options?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0) ?: 0
                val isLargeWidget = minWidth >= 250

                val layoutRes = if (isLargeWidget) R.layout.iris_fit_widget_large else R.layout.iris_fit_widget
                val views = RemoteViews(context.packageName, layoutRes)
                views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

                val queryResult = try { ScreenTimeManager.queryTodayScreenTime(context) } catch (_: Exception) { com.example.myapplication.data.ScreenTimeQueryResult.QueryError("Widget Error") }
                val (screenTime, isMeasured) = when (queryResult) {
                    is com.example.myapplication.data.ScreenTimeQueryResult.Success -> Pair(queryResult.minutes, true)
                    else -> Pair(0, false)
                }
                val records = try { FileCompletionStorage.repository(context).records() } catch (_: Exception) { emptyList() }
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
                val localizedStatusLabel = AppLocalization.get(selectedLang, stats.fatigueStatus.labelKey)

                val mascotRes = when (stats.fatigueStatus) {
                    EyeFatigueStatus.OPTIMAL -> R.drawable.ic_mascot_happy
                    EyeFatigueStatus.CAUTION -> R.drawable.ic_mascot_tired
                    EyeFatigueStatus.WARNING -> R.drawable.ic_mascot_tired
                    EyeFatigueStatus.DANGER -> R.drawable.ic_mascot_panicked
                    EyeFatigueStatus.UNMEASURED -> R.drawable.ic_mascot_happy
                }

                if (isLargeWidget) {
                    val localizedTitle = when (effectiveLang) {
                        "en" -> "IrisFit Eye Health Dashboard"
                        "ja" -> "IrisFit 目の健康ダッシュボード"
                        "es" -> "Panel IrisFit"
                        else -> "IrisFit 눈 건강 대시보드"
                    }
                    val localizedScoreText = if (stats.fatigueStatus == EyeFatigueStatus.UNMEASURED || stats.eyeHealthScore < 0) {
                        AppLocalization.get(selectedLang, "screen_time_unmeasured")
                    } else {
                        "${stats.eyeHealthScore}% ($localizedStatusLabel)"
                    }
                    views.setImageViewResource(R.id.widget_large_mascot, mascotRes)
                    views.setTextViewText(R.id.widget_large_title, localizedTitle)
                    views.setTextViewText(R.id.widget_large_score, localizedScoreText)
                } else {
                    val localizedTitle = when (effectiveLang) {
                        "en" -> "IrisFit Eye Health Battery"
                        "ja" -> "IrisFit 目の健康バッテリー"
                        "es" -> "Batería Ocular IrisFit"
                        else -> "IrisFit 눈 건강 배터리"
                    }
                    val localizedBatteryText = if (stats.fatigueStatus == EyeFatigueStatus.UNMEASURED || stats.eyeHealthScore < 0) {
                        AppLocalization.get(selectedLang, "screen_time_unmeasured")
                    } else {
                        when (effectiveLang) {
                            "en" -> "Battery: ${stats.eyeHealthScore}% ($localizedStatusLabel)"
                            "ja" -> "バッテリー: ${stats.eyeHealthScore}% ($localizedStatusLabel)"
                            "es" -> "Batería: ${stats.eyeHealthScore}% ($localizedStatusLabel)"
                            else -> "배터리: ${stats.eyeHealthScore}% ($localizedStatusLabel)"
                        }
                    }
                    views.setImageViewResource(R.id.widget_mascot_icon, mascotRes)
                    views.setTextViewText(R.id.widget_title_text, localizedTitle)
                    views.setTextViewText(R.id.widget_status_text, localizedBatteryText)
                }

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
