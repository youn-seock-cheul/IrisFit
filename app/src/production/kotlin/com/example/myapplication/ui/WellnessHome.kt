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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.BuildConfig
import com.example.myapplication.R
import com.example.myapplication.data.AppVersionManager
import com.example.myapplication.reminder.BreakReminderScheduler
import com.example.myapplication.session.EyeBatteryEngine
import com.example.myapplication.session.EyeFatigueStatus
import com.example.myapplication.session.GuidedSessionViewModel
import java.time.LocalDate

// Mockup text sizes; long translations shrink to fit instead of wrapping.
private val TileLabelAutoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 12.sp, stepSize = 0.5.sp)
private val TileValueAutoSize = TextAutoSize.StepBased(minFontSize = 9.sp, maxFontSize = 16.sp, stepSize = 0.5.sp)
private val TileEffectAutoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 11.sp, stepSize = 0.5.sp)

@Composable
fun EyePedometerCard(
    sessionModel: GuidedSessionViewModel,
    onNavigateToGym: () -> Unit = {},
    onNavigateToPursuit: () -> Unit = {}
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            sessionModel.refreshDate()
            sessionModel.refreshBattery()
        }
    }
    val stats = sessionModel.eyeBatteryStats

    // 4-Tier Container Colors
    val cardBgColor = when (stats.fatigueStatus) {
        EyeFatigueStatus.OPTIMAL -> Color(0xFF1B3A2B) // 🟢 Dark Forest Green (85~100%)
        EyeFatigueStatus.CAUTION -> Color(0xFF382B14) // 🟡 Dark Amber (65~84%)
        EyeFatigueStatus.WARNING -> Color(0xFF3D2513) // 🟠 Dark Orange (45~64%)
        EyeFatigueStatus.DANGER -> Color(0xFF3C1F1F)  // 🔴 Dark Rose Red (0~44%)
        EyeFatigueStatus.UNMEASURED -> Color(0xFF262626) // ⚪ Neutral Grey
    }
    val statusColor = when (stats.fatigueStatus) {
        EyeFatigueStatus.OPTIMAL -> Color(0xFF00E676) // Bright Green
        EyeFatigueStatus.CAUTION -> Color(0xFFFFC107) // Bright Yellow
        EyeFatigueStatus.WARNING -> Color(0xFFFF9800) // Bright Orange
        EyeFatigueStatus.DANGER -> Color(0xFFFF5252)  // Bright Red
        EyeFatigueStatus.UNMEASURED -> Color(0xFFA0A0A0) // Neutral Silver/Grey
    }

    val isUnmeasured = stats.fatigueStatus == EyeFatigueStatus.UNMEASURED

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
            containerColor = cardBgColor,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            .clip(CircleShape)
                            .background(Color(0xFF286452), CircleShape)
                            .testTag("battery_card_launcher_icon"),
                        contentAlignment = Alignment.Center
                    ) {
                        // App launcher icon: the 108dp adaptive foreground is cropped to its visible center.
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_irisfit_foreground),
                            contentDescription = "IrisFit",
                            modifier = Modifier.size(78.dp)
                        )
                    }
                    Column {
                        Text(sessionModel.tr("battery_card_name"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (sessionModel.healthConnectLinked) {
                            Text(
                                sessionModel.tr("health_connect_linked"),
                                modifier = Modifier.testTag("health_connect_linked"),
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB8DAD3)
                            )
                        }
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
                            tint = statusColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = sessionModel.tr(sessionModel.poseDetector.currentPose.labelKey),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
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
                    modifier = Modifier.weight(1f, fill = false).padding(end = 8.dp),
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IrisStatusDot(if (stats.fatigueStatus == EyeFatigueStatus.UNMEASURED) Color.Gray else statusColor, 8)
                        Text(
                            text = sessionModel.tr(if (isUnmeasured) "not_measured" else stats.fatigueStatus.labelKey),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }

                Text(
                    text = if (isUnmeasured || stats.eyeHealthScore < 0) "--%" else "${stats.eyeHealthScore}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            // GAUGE WITH 4-TIER LEGEND (Left 0% -> Right 100% Correct Order)
            val isGaugeMeasured = stats.fatigueStatus != EyeFatigueStatus.UNMEASURED && stats.eyeHealthScore >= 0
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LinearProgressIndicator(
                    progress = { if (isGaugeMeasured) (stats.eyeHealthScore.toFloat() / 100f).coerceIn(0f, 1.0f) else 0f },
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
                    GaugeLegend(sessionModel.tr("legend_danger"), Color(0xFFFF5252), stats.fatigueStatus == EyeFatigueStatus.DANGER)
                    GaugeLegend(sessionModel.tr("legend_warning"), Color(0xFFFF9800), stats.fatigueStatus == EyeFatigueStatus.WARNING)
                    GaugeLegend(sessionModel.tr("legend_caution"), Color(0xFFFFC107), stats.fatigueStatus == EyeFatigueStatus.CAUTION)
                    GaugeLegend(sessionModel.tr("legend_optimal"), Color(0xFF00E676), stats.fatigueStatus == EyeFatigueStatus.OPTIMAL)
                }
            }

            val healthSummary = sessionModel.healthBatterySummary
            val usageNotice = when (val usage = sessionModel.screenTimeResult) {
                is com.example.myapplication.data.ScreenTimeQueryResult.Success ->
                    if (usage.weightingAvailable) null else "screen_weights_unavailable"
                is com.example.myapplication.data.ScreenTimeQueryResult.QueryError -> "screen_usage_query_error"
                else -> null
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_ms_lightbulb), contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                    Column {
                        Text(
                            text = if (healthSummary.isEmpty()) sessionModel.tr(stats.fatigueStatus.descKey)
                                else healthSummary.joinToString("\n"),
                            modifier = if (healthSummary.isEmpty()) Modifier else Modifier.testTag("health_battery_summary"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        listOfNotNull(usageNotice, sessionModel.healthDataStatusKey).forEach { key ->
                            Text(sessionModel.tr(key), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            val gainFormat = sessionModel.tr("home_battery_gain")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. REST CARD (30s Rest)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("home_rest_tile")
                        .clickable { sessionModel.prepare() },
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_ms_self_improvement), contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
                        Text(sessionModel.tr("rest_completed"), style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, autoSize = TileLabelAutoSize)
                        val restFmt = sessionModel.tr("fmt_rest_completed")
                        val restTxt = String.format(java.util.Locale.getDefault(), restFmt, stats.restCount, stats.restCount * 30)
                        Text(restTxt, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = statusColor, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, autoSize = TileValueAutoSize)
                        Text(gainFormat.format(EyeBatteryEngine.REST_RECOVERY_PERCENT), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, autoSize = TileEffectAutoSize)
                    }
                }

                // 2. FOCUS GYM CARD (원근 조절 모양근 스트레칭)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToGym() },
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_ms_center_focus_strong), contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
                        Text(sessionModel.tr("home_card_training"), style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, autoSize = TileLabelAutoSize)
                        Text(sessionModel.tr("home_card_focus"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = statusColor, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, autoSize = TileValueAutoSize)
                        Text(gainFormat.format(EyeBatteryEngine.TRAINING_RECOVERY_PERCENT), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, autoSize = TileEffectAutoSize)
                    }
                }

                // 3. PURSUIT GAME CARD (부드러운 안구 궤적 훈련)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToPursuit() },
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_ms_all_inclusive), contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
                        Text(sessionModel.tr("home_card_training"), style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, autoSize = TileLabelAutoSize)
                        Text(sessionModel.tr("home_card_pursuit"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = statusColor, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, autoSize = TileValueAutoSize)
                        Text(gainFormat.format(EyeBatteryEngine.TRAINING_RECOVERY_PERCENT), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center, autoSize = TileEffectAutoSize)
                    }
                }
            }

            // PERMISSION WARNING BANNER IF USAGE STATS NOT PERMITTED
            var showUsageDisclosureDialog by remember { mutableStateOf(false) }
            val hasUsagePermission = com.example.myapplication.data.ScreenTimeManager.hasUsageStatsPermission(context)
            if (!hasUsagePermission) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable {
                        showUsageDisclosureDialog = true
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
                                painter = painterResource(id = R.drawable.ic_ms_lock),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(sessionModel.tr("usage_perm_title"), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                                Text(sessionModel.tr("usage_perm_desc"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f))
                            }
                        }
                        Button(
                            onClick = { showUsageDisclosureDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(sessionModel.tr("usage_perm_btn"), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Icon(painterResource(R.drawable.ic_ms_arrow_forward), contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }

            if (showUsageDisclosureDialog) UsageAccessDisclosureDialog(
                sessionModel = sessionModel,
                onDismiss = { showUsageDisclosureDialog = false },
                onOpenSettings = {
                    showUsageDisclosureDialog = false
                    com.example.myapplication.data.ScreenTimeManager.openUsageStatsSettings(context)
                }
            )

            // SHARE BUTTON
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        val shareMsg = if (stats.fatigueStatus == EyeFatigueStatus.UNMEASURED || stats.eyeHealthScore < 0) {
                            sessionModel.tr("share_unmeasured")
                        } else {
                            sessionModel.tr("share_measured").format(stats.eyeHealthScore, sessionModel.tr(stats.fatigueStatus.labelKey))
                        }
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "$shareMsg\n\n${sessionModel.tr("share_download")}: https://play.google.com/store/apps/details?id=com.eyeoverlay.app")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, sessionModel.tr("sns_share"))
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(shareIntent)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(painter = painterResource(id = R.drawable.ic_ms_share), contentDescription = null, tint = statusColor, modifier = Modifier.size(15.dp))
                        Text(sessionModel.tr("sns_share"), fontSize = 13.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GaugeLegend(label: String, color: Color, active: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        IrisStatusDot(color, 6)
        Text(
            label,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) color else Color.Gray
        )
    }
}
