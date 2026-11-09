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
            } catch (_: Exception) {}
        }

        fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val views = RemoteViews(context.packageName, R.layout.iris_fit_widget)
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            val screenTime = try { ScreenTimeManager.getTodayScreenTimeMinutes(context) } catch (_: Exception) { 0 }
            val records = try { FileCompletionStorage.repository(context).records() } catch (_: Exception) { emptyList() }
            val today = LocalDate.now().toString()
            val rests = records.count { it.completedLocalDate == today && it.routineId == "screen_rest" }
            val miniGames = records.count { it.completedLocalDate == today && (it.routineId == "eye_gym" || it.routineId == "smooth_pursuit") }

            val stats = EyeBatteryCalculator.calculateBattery(screenTime, rests, miniGames)

            val mascotRes = when (stats.fatigueStatus) {
                EyeFatigueStatus.OPTIMAL -> R.drawable.ic_mascot_happy
                EyeFatigueStatus.CAUTION -> R.drawable.ic_mascot_tired
                EyeFatigueStatus.WARNING -> R.drawable.ic_mascot_tired
                EyeFatigueStatus.DANGER -> R.drawable.ic_mascot_panicked
            }

            val selectedLang = LanguageManager.getSelectedLanguage(context)
            val localizedStatusLabel = AppLocalization.get(selectedLang, stats.fatigueStatus.labelKey)
            val localizedBatteryText = when (AppLocalization.resolveEffectiveLang(selectedLang)) {
                "en" -> "Battery: ${stats.eyeHealthScore}% ($localizedStatusLabel)"
                "ja" -> "バッテリー: ${stats.eyeHealthScore}% ($localizedStatusLabel)"
                "es" -> "Batería: ${stats.eyeHealthScore}% ($localizedStatusLabel)"
                else -> "배터리: ${stats.eyeHealthScore}% ($localizedStatusLabel)"
            }

            views.setImageViewResource(R.id.widget_mascot_icon, mascotRes)
            views.setTextViewText(R.id.widget_status_text, localizedBatteryText)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
