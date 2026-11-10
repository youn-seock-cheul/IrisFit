package com.example.myapplication.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.data.AppVersionManager
import com.example.myapplication.reminder.BreakReminderScheduler
import com.example.myapplication.session.EyeFatigueStatus
import com.example.myapplication.session.GuidedSessionViewModel
import java.time.LocalDate

@Composable
fun EyePedometerCard(
    sessionModel: GuidedSessionViewModel,
    onNavigateToPursuit: () -> Unit = {}
) {
    val context = LocalContext.current
    val stats = sessionModel.eyeBatteryStats

    // 4-Tier Container Colors
    val cardBgColor = when (stats.fatigueStatus) {
        EyeFatigueStatus.OPTIMAL -> Color(0xFF1B3A2B) // 🟢 Dark Forest Green (85~100%)
        EyeFatigueStatus.CAUTION -> Color(0xFF382B14) // 🟡 Dark Amber (65~84%)
        EyeFatigueStatus.WARNING -> Color(0xFF3D2513) // 🟠 Dark Orange (45~64%)
        EyeFatigueStatus.DANGER -> Color(0xFF3C1F1F)  // 🔴 Dark Rose Red (10~44%)
    }
    val statusColor = when (stats.fatigueStatus) {
        EyeFatigueStatus.OPTIMAL -> Color(0xFF00E676) // Bright Green
        EyeFatigueStatus.CAUTION -> Color(0xFFFFC107) // Bright Yellow
        EyeFatigueStatus.WARNING -> Color(0xFFFF9800) // Bright Orange
        EyeFatigueStatus.DANGER -> Color(0xFFFF5252)  // Bright Red
    }

    val mascotRes = when (stats.fatigueStatus) {
        EyeFatigueStatus.OPTIMAL -> R.drawable.ic_mascot_happy
        EyeFatigueStatus.CAUTION -> R.drawable.ic_mascot_tired
        EyeFatigueStatus.WARNING -> R.drawable.ic_mascot_tired
        EyeFatigueStatus.DANGER -> R.drawable.ic_mascot_panicked
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mascot_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardBgColor
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // TOP HEADER ROW: Mascot + Title + Localized Subtitle Description + TOP RIGHT POSE BADGE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale)
                            .background(statusColor.copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = mascotRes),
                            contentDescription = "IrisFit Mascot",
                            modifier = Modifier.size(38.dp)
                        )
                    }
                    Column {
                        Text(sessionModel.tr("battery_card_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(sessionModel.tr("battery_card_sub"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }

                // TOP RIGHT POSE BADGE (Single-line, un-wrapped, localized)
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = sessionModel.poseDetector.currentPose.iconRes),
                            contentDescription = null,
                            tint = if (!sessionModel.poseDetector.currentPose.isCharging) statusColor else Color(0xFF00E676),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = sessionModel.tr(sessionModel.poseDetector.currentPose.labelKey),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (!sessionModel.poseDetector.currentPose.isCharging) statusColor else Color(0xFF00E676)
                        )
                    }
                }
            }

            // STATS ROW: Status Label + Percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "${stats.fatigueStatus.emoji} ${sessionModel.tr(stats.fatigueStatus.labelKey)}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = statusColor
                    )
                }

                Text(
                    text = "${stats.eyeHealthScore}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            // GAUGE WITH 4-TIER LEGEND (Left 0% -> Right 100% Correct Order)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LinearProgressIndicator(
                    progress = { (stats.eyeHealthScore.toFloat() / 100f).coerceIn(0f, 1.0f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.2f),
                    strokeCap = StrokeCap.Round
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🔴 ${sessionModel.tr("legend_danger")}", fontSize = 9.sp, fontWeight = if (stats.eyeHealthScore < 45) FontWeight.Bold else FontWeight.Normal, color = if (stats.eyeHealthScore < 45) Color(0xFFFF5252) else Color.Gray)
                    Text("🟠 ${sessionModel.tr("legend_warning")}", fontSize = 9.sp, fontWeight = FontWeight.Bold.takeIf { stats.eyeHealthScore in 45..64 } ?: FontWeight.Normal, color = if (stats.eyeHealthScore in 45..64) Color(0xFFFF9800) else Color.Gray)
                    Text("🟡 ${sessionModel.tr("legend_caution")}", fontSize = 9.sp, fontWeight = FontWeight.Bold.takeIf { stats.eyeHealthScore in 65..84 } ?: FontWeight.Normal, color = if (stats.eyeHealthScore in 65..84) Color(0xFFFFC107) else Color.Gray)
                    Text("🟢 ${sessionModel.tr("legend_optimal")}", fontSize = 9.sp, fontWeight = if (stats.eyeHealthScore >= 85) FontWeight.Bold else FontWeight.Normal, color = if (stats.eyeHealthScore >= 85) Color(0xFF00E676) else Color.Gray)
                }
            }

            // CLICKABLE REST & EYE EXERCISE CARDS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // REST CARD (Click to start 30s Rest Popup!)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { sessionModel.prepare() },
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_nav_bluelight), contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                        Column {
                            Text(sessionModel.tr("rest_completed"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            val restFmt = sessionModel.tr("fmt_rest_completed")
                            val restTxt = String.format(java.util.Locale.getDefault(), restFmt, stats.restCount, stats.restCount * 30)
                            Text(restTxt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = statusColor)
                            Text(sessionModel.tr("tap_to_rest"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        }
                    }
                }

                // EYE EXERCISE CARD (Click to start Pursuit Game!)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToPursuit() },
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_nav_gym), contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                        Column {
                            Text(sessionModel.tr("eye_exercise"), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            val exerciseFmt = sessionModel.tr("fmt_eye_exercise")
                            val exerciseTxt = String.format(java.util.Locale.getDefault(), exerciseFmt, stats.miniGameCount)
                            Text(exerciseTxt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = statusColor)
                            Text(sessionModel.tr("tap_to_exercise"), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = statusColor)
                        }
                    }
                }
            }

            // TIP CARD (Localized Message!)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_card_lightbulb), contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                    Text(
                        text = sessionModel.tr(stats.fatigueStatus.descKey),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // PERMISSION WARNING BANNER IF USAGE STATS NOT PERMITTED
            val hasUsagePermission = com.example.myapplication.data.ScreenTimeManager.hasUsageStatsPermission(context)
            if (!hasUsagePermission) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        com.example.myapplication.data.ScreenTimeManager.openUsageStatsSettings(context)
                    },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_permission_lock),
                                contentDescription = "Permission Lock",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(sessionModel.tr("사용 정보 접근 권한 필요"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text(sessionModel.tr("실시간 화면 사용 시간 측정을 위해 권한을 허용해 주세요."), fontSize = 10.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f))
                            }
                        }
                        Button(
                            onClick = { com.example.myapplication.data.ScreenTimeManager.openUsageStatsSettings(context) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(sessionModel.tr("권한 설정 바로가기 ➔"), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // SHARE BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "오늘 IrisFit 눈 건강 배터리 컨디션은 ${stats.eyeHealthScore}% (${sessionModel.tr(stats.fatigueStatus.labelKey)})입니다! 🔋✨ 함께 눈 피로를 충전해요!\n\n📲 IrisFit 앱 다운로드: https://play.google.com/store/apps/details?id=com.eyeoverlay.app")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "눈 건강 배터리 공유하기")
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(shareIntent)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_card_share), contentDescription = null, tint = statusColor, modifier = Modifier.size(15.dp))
                        Text(sessionModel.tr("sns_share"), fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
