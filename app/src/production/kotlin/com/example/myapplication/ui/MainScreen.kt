package com.example.myapplication.ui

import android.Manifest
import androidx.compose.foundation.layout.RowScope
import androidx.annotation.DrawableRes
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.R
import com.example.myapplication.data.HealthConnectManager
import com.example.myapplication.data.ScreenTimeManager
import com.example.myapplication.session.GuidedSessionViewModel
import com.example.myapplication.session.EyeBatteryCalculator
import com.example.myapplication.ui.theme.LegacyBatteryTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun MainScreen(
    sessionModel: GuidedSessionViewModel,
    onStartCameraSession: () -> Unit,
    onStopCameraSession: () -> Unit,
    onNavigateToExam: () -> Unit,
    onNavigateToGym: () -> Unit,
    onNavigateToPursuit: () -> Unit,
    onNavigateToPursuitMode: (Int) -> Unit = {},
    onNavigateToPursuitRandom: () -> Unit = onNavigateToPursuit,
    onNavigateToWorkoutRandom: () -> Unit = {}
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                IrisNavItem(selectedTab == 0, R.drawable.ic_ms_home, R.drawable.ic_ms_home_fill, sessionModel.tr("nav_home")) { selectedTab = 0 }
                IrisNavItem(selectedTab == 1, R.drawable.ic_ms_visibility, R.drawable.ic_ms_visibility_fill, sessionModel.tr("nav_gym")) { selectedTab = 1 }
                IrisNavItem(selectedTab == 2, R.drawable.ic_ms_shield, R.drawable.ic_ms_shield_fill, sessionModel.tr("nav_eye_comfort")) { selectedTab = 2 }
                IrisNavItem(selectedTab == 3, R.drawable.ic_ms_settings, R.drawable.ic_ms_settings_fill, sessionModel.tr("nav_settings")) { selectedTab = 3 }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.fillMaxSize().padding(innerPadding)
        when (selectedTab) {
            0 -> WellnessHomeTab(
                sessionModel = sessionModel,
                modifier = modifier,
                onNavigateToGym = onNavigateToGym,
                onNavigateToPursuit = onNavigateToPursuitRandom
            )
            1 -> GymAndExamTab(sessionModel, modifier, onNavigateToGym, onNavigateToPursuitMode, onNavigateToExam)
            2 -> EyeComfortSettingsTab(sessionModel, modifier)
            3 -> SettingsTab(sessionModel, modifier)
        }
    }
}

@Composable
private fun RowScope.IrisNavItem(
    selected: Boolean,
    @DrawableRes icon: Int,
    @DrawableRes selectedIcon: Int,
    label: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                painter = painterResource(id = if (selected) selectedIcon else icon),
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        },
        label = { Text(label, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis) }
    )
}

@Composable
fun BetaNoticeDialog(sessionModel: GuidedSessionViewModel, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(painterResource(R.drawable.ic_ms_info), contentDescription = null) },
        title = { Text(text = sessionModel.tr("beta_notice_title"), fontWeight = FontWeight.Bold) },
        text = { Text(text = sessionModel.tr("beta_notice_body")) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(sessionModel.tr("btn_ok")) }
        }
    )
}

@Composable
fun CommunityCheerCard(sessionModel: GuidedSessionViewModel) {
    var showBetaNoticeDialog by remember { mutableStateOf(false) }

    if (showBetaNoticeDialog) {
        BetaNoticeDialog(sessionModel) { showBetaNoticeDialog = false }
    }

    IrisCard {
        IrisCardHeader(
            icon = R.drawable.ic_ms_flag,
            title = sessionModel.tr("cheer_card_title"),
            subtitle = sessionModel.tr("cheer_card_sub"),
            badge = sessionModel.tr("badge_beta")
        )
        IrisBodyText(sessionModel.tr("cheer_card_body"))
        IrisPrimaryButton(
            text = sessionModel.tr("cheer_card_btn"),
            icon = R.drawable.ic_ms_notifications,
            onClick = { showBetaNoticeDialog = true }
        )
    }
}

@Composable
fun GoalAchievementCard(sessionModel: GuidedSessionViewModel) {
    val recordsAvailable = sessionModel.recordsReady && sessionModel.error == null
    val restCount = sessionModel.todayRestCount
    val stats = sessionModel.eyeBatteryStats
    val isMeasured = recordsAvailable && stats.screenTimeMeasured
    val batteryScore = stats.eyeHealthScore
    val streak = com.example.myapplication.session.RestHistory.currentRestStreak(
        sessionModel.records, java.time.LocalDate.parse(sessionModel.today)
    )
    val isGoal1Reached = isMeasured && batteryScore >= 85
    val isGoal2Reached = recordsAvailable && restCount >= 3
    val isGoal3Reached = recordsAvailable && streak >= 7
    val batteryText = if (isMeasured && batteryScore >= 0) "${batteryScore}%" else sessionModel.tr("not_measured")

    IrisCard {
        IrisCardHeader(
            icon = R.drawable.ic_ms_emoji_events,
            title = sessionModel.tr("goals_header"),
            subtitle = sessionModel.tr("goals_sub")
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GoalRow(
                sessionModel = sessionModel,
                icon = R.drawable.ic_ms_battery_saver,
                title = sessionModel.tr("goal_1"),
                detail = sessionModel.tr("goal_current_battery").format(batteryText),
                reached = isGoal1Reached,
                available = isMeasured
            )
            GoalRow(
                sessionModel = sessionModel,
                icon = R.drawable.ic_ms_timer,
                title = sessionModel.tr("goal_2"),
                detail = if (recordsAvailable) sessionModel.tr("goal_rest_progress").format(restCount) else sessionModel.tr("records_unavailable"),
                reached = isGoal2Reached,
                available = recordsAvailable
            )
            GoalRow(
                sessionModel = sessionModel,
                icon = R.drawable.ic_ms_local_fire_department,
                title = sessionModel.tr("goal_3"),
                detail = if (recordsAvailable) sessionModel.tr("goal_streak_progress").format(streak) else sessionModel.tr("records_unavailable"),
                reached = isGoal3Reached,
                available = recordsAvailable
            )
        }
    }
}

@Composable
private fun GoalRow(
    sessionModel: GuidedSessionViewModel,
    @DrawableRes icon: Int,
    title: String,
    detail: String,
    reached: Boolean,
    available: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (!available) {
            IrisPill(sessionModel.tr("goal_unavailable"))
        } else if (reached) {
            IrisPill(sessionModel.tr("goal_achieved"), MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimary)
        } else {
            IrisPill(sessionModel.tr("goal_progress"))
        }
    }
}

@Composable
fun GymAndExamTab(
    sessionModel: GuidedSessionViewModel,
    modifier: Modifier,
    onNavigateToGym: () -> Unit,
    onNavigateToPursuitMode: (Int) -> Unit = {},
    onNavigateToExam: () -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.padding(20.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IrisScreenHeader(sessionModel.tr("gym_header"), sessionModel.tr("gym_sub"))

        IrisInfoBanner(
            icon = R.drawable.ic_ms_check_circle,
            title = sessionModel.tr("gym_card_banner"),
            text = sessionModel.tr("gym_card_banner_sub")
        )

        IrisCard {
            IrisCardHeader(
                icon = R.drawable.ic_ms_center_focus_strong,
                title = sessionModel.tr("gym_focus_title"),
                subtitle = sessionModel.tr("gym_focus_meta")
            )
            IrisBodyText(sessionModel.tr("gym_focus_sub"))
            IrisPrimaryButton(
                text = sessionModel.tr("gym_focus_btn"),
                icon = R.drawable.ic_ms_play_arrow,
                onClick = onNavigateToGym
            )
        }

        IrisCard {
            IrisCardHeader(
                icon = R.drawable.ic_ms_all_inclusive,
                title = sessionModel.tr("gym_pursuit_title"),
                subtitle = sessionModel.tr("gym_pursuit_meta")
            )
            IrisBodyText(sessionModel.tr("gym_pursuit_sub"))
            val modes = listOf(
                R.drawable.ic_ms_all_inclusive to "pursuit_mode_infinity",
                R.drawable.ic_ms_gesture to "pursuit_mode_figure8",
                R.drawable.ic_ms_radio_button_unchecked to "pursuit_mode_circle",
                R.drawable.ic_ms_show_chart to "pursuit_mode_zigzag"
            )
            modes.chunked(2).forEachIndexed { rowIndex, pair ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEachIndexed { colIndex, (icon, key) ->
                        val mode = rowIndex * 2 + colIndex
                        IrisTonalButton(
                            text = sessionModel.tr(key),
                            icon = icon,
                            onClick = { onNavigateToPursuitMode(mode) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        IrisCard {
            IrisCardHeader(
                icon = R.drawable.ic_ms_ophthalmology,
                title = sessionModel.tr("gym_exam_title"),
                subtitle = sessionModel.tr("gym_exam_meta")
            )
            IrisBodyText(sessionModel.tr("gym_exam_sub"))
            IrisPrimaryButton(
                text = sessionModel.tr("gym_exam_btn"),
                icon = R.drawable.ic_ms_play_arrow,
                onClick = onNavigateToExam
            )
        }
    }
}

@Composable
fun EyeComfortSettingsTab(sessionModel: GuidedSessionViewModel, modifier: Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IrisScreenHeader(sessionModel.tr("eye_comfort_title"), sessionModel.tr("eye_comfort_desc"))

        IrisCard {
            IrisCardHeader(
                icon = R.drawable.ic_ms_light_mode,
                title = sessionModel.tr("eye_comfort_card_title")
            )
            IrisBodyText(sessionModel.tr("eye_comfort_card_desc"))
            IrisPrimaryButton(
                text = sessionModel.tr("eye_comfort_open_settings"),
                icon = R.drawable.ic_ms_arrow_forward,
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS))
                    } catch (_: Exception) {
                        Toast.makeText(
                            context,
                            sessionModel.tr("eye_comfort_open_failed"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }
}

@Composable
fun AiCoachTab(sessionModel: GuidedSessionViewModel, modifier: Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showUsageDisclosureDialog by remember { mutableStateOf(false) }

    val initialQueryResult = ScreenTimeManager.queryTodayScreenTime(context)
    var hasPermission by remember { mutableStateOf(initialQueryResult is com.example.myapplication.data.ScreenTimeQueryResult.Success) }
    var realMinutes by remember { mutableIntStateOf((initialQueryResult as? com.example.myapplication.data.ScreenTimeQueryResult.Success)?.minutes ?: 0) }

    LaunchedEffect(Unit) {
        while (true) {
            val res = ScreenTimeManager.queryTodayScreenTime(context)
            hasPermission = res is com.example.myapplication.data.ScreenTimeQueryResult.Success
            realMinutes = (res as? com.example.myapplication.data.ScreenTimeQueryResult.Success)?.minutes ?: 0
            delay(60_000L)
        }
    }

    val hours = realMinutes / 60
    val mins = realMinutes % 60
    val formattedTime = if (hasPermission) "${hours}시간 ${mins}분" else sessionModel.tr("미측정")

    val actualRests = sessionModel.todayRestCount

    val batteryScore = sessionModel.eyeBatteryStats.eyeHealthScore

    Column(
        modifier = modifier.padding(20.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(sessionModel.tr("ai_title"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("PRO", modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Text(sessionModel.tr("ai_desc"))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("AI 코치 (검토 중)", fontWeight = FontWeight.Bold)
                Text(sessionModel.tr("privacy_ai"), fontSize = 13.sp)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(sessionModel.tr("ai_analysis_title"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(sessionModel.tr("ai_screen_time"), color = Color.Gray)
                    Text(formattedTime, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(sessionModel.tr("ai_battery_remain"), color = Color.Gray)
                    val batteryScoreText = if (hasPermission && batteryScore >= 0) {
                        "${batteryScore}% (${sessionModel.tr(sessionModel.eyeBatteryStats.fatigueStatus.labelKey)})"
                    } else {
                        sessionModel.tr("screen_time_unmeasured")
                    }
                    Text(batteryScoreText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(sessionModel.tr("ai_compliance"), color = Color.Gray)
                    Text("${actualRests}회", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                if (!hasPermission) {
                    Text(
                        sessionModel.tr("screen_time_unmeasured"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(onClick = {
                        showUsageDisclosureDialog = true
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Text(sessionModel.tr("ai_permission_btn"))
                    }
                }
            }
        }

        if (showUsageDisclosureDialog) {
            UsageAccessDisclosureDialog(
                sessionModel = sessionModel,
                onDismiss = { showUsageDisclosureDialog = false },
                onOpenSettings = {
                    showUsageDisclosureDialog = false
                    ScreenTimeManager.openUsageStatsSettings(context)
                }
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(sessionModel.tr("IrisFit PRO 차세대 유료 플랜 혜택"), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text(sessionModel.tr("pro_demo_status"), style = MaterialTheme.typography.bodySmall)
                Text(sessionModel.tr("안구 훈련(Eye Care) 기능 기반의 독보적인 프리미엄 혜택:"))

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(sessionModel.tr("1. Gaze-Nav 시선 스크롤 지도"), fontWeight = FontWeight.Bold)
                        Text(sessionModel.tr("손대지 않고 눈동자 이동만으로 지도 스크롤 & 확대/축소"), fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(sessionModel.tr("2. Eye-Blink 힐링 사운드"), fontWeight = FontWeight.Bold)
                        Text(sessionModel.tr("내 깜빡임 주기와 동기화되어 안구 근육을 이완시키는 오디오"), fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(sessionModel.tr("3. Gaze-AI 머무름 감지 요약"), fontWeight = FontWeight.Bold)
                        Text(sessionModel.tr("오랫동안 쳐다보는 화면 영역을 LLM이 실시간 감지 및 브리핑"), fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Button(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(sessionModel.tr("pro_unavailable"), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

}

@Composable
fun SettingsTab(sessionModel: GuidedSessionViewModel, modifier: Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showColorToneDialog by remember { mutableStateOf(false) }
    var showHealthConnectDisclosure by remember { mutableStateOf(false) }
    var notificationsEnabled by remember {
        mutableStateOf(com.example.myapplication.data.PreferencesManager(context).isNotificationsEnabled)
    }
    val healthPermissionLauncher = rememberLauncherForActivityResult(
        HealthConnectManager.permissionRequestContract()
    ) { granted ->
        if (HealthConnectManager.hasAllPermissions(granted)) {
            sessionModel.updateHealthConnectEnabled(true)
        } else {
            sessionModel.updateHealthConnectEnabled(false)
            Toast.makeText(context, sessionModel.tr("health_permission_denied"), Toast.LENGTH_LONG).show()
        }
    }

    val sysLang = java.util.Locale.getDefault().displayLanguage

    Column(
        modifier = modifier.padding(20.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IrisScreenHeader(sessionModel.tr("settings_header"))

        IrisSectionLabel(sessionModel.tr("settings_section_noti"))
        IrisSettingsGroup {
            CareReminderSettings()
            IrisRowDivider()
            IrisSettingsRow(
                icon = R.drawable.ic_ms_notifications_active,
                title = sessionModel.tr("noti_settings_title"),
                subtitle = sessionModel.tr("noti_settings_desc"),
                trailing = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { checked ->
                            notificationsEnabled = checked
                            com.example.myapplication.data.PreferencesManager(context).isNotificationsEnabled = checked
                            Toast.makeText(context, if (checked) sessionModel.tr("noti_on_toast") else sessionModel.tr("noti_off_toast"), Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            )
        }

        IrisSectionLabel(sessionModel.tr("settings_section_general"))
        IrisSettingsGroup {
            IrisSettingsRow(
                icon = R.drawable.ic_ms_translate,
                title = sessionModel.tr("settings_lang_title"),
                subtitle = sessionModel.tr("settings_lang_sub").format(sysLang),
                trailing = { IrisTrailingText(sessionModel.tr("settings_lang_auto")) }
            )
            IrisRowDivider()
            IrisSettingsRow(
                icon = R.drawable.ic_ms_battery_saver,
                title = sessionModel.tr("health_connect_settings_title"),
                subtitle = sessionModel.tr("health_connect_settings_desc"),
                trailing = {
                    Switch(
                        checked = sessionModel.healthConnectEnabled,
                        onCheckedChange = { enabled ->
                            if (!enabled) {
                                sessionModel.updateHealthConnectEnabled(false)
                            } else if (HealthConnectManager.isAvailable(context)) {
                                showHealthConnectDisclosure = true
                            } else {
                                Toast.makeText(context, sessionModel.tr("health_connect_unavailable"), Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            )
            IrisRowDivider()
            IrisSettingsRow(
                icon = R.drawable.ic_ms_light_mode,
                title = sessionModel.tr("settings_color_tone"),
                subtitle = sessionModel.tr(sessionModel.colorTone.labelKey),
                onClick = { showColorToneDialog = true },
                trailing = { IrisChevron() }
            )
            IrisRowDivider()
            IrisSettingsRow(
                icon = R.drawable.ic_ms_battery_saver,
                title = sessionModel.tr("settings_battery_title"),
                subtitle = sessionModel.tr("settings_battery_sub"),
                onClick = {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        try {
                            val intent2 = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(intent2)
                        } catch (_: Exception) {
                            Toast.makeText(context, sessionModel.tr("settings_battery_open_failed"), Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                trailing = { IrisChevron() }
            )
        }

        IrisSectionLabel(sessionModel.tr("settings_section_support"))
        IrisSettingsGroup {
            IrisSettingsRow(
                icon = R.drawable.ic_ms_mail,
                title = sessionModel.tr("settings_feedback_title"),
                subtitle = sessionModel.tr("settings_feedback_sub"),
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@irisfit.app?subject=[IrisFit Bug Report / Feedback]&body=Device: ${android.os.Build.MODEL} (Android ${android.os.Build.VERSION.RELEASE})\nFeedback: "))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, sessionModel.tr("settings_feedback_no_app"), Toast.LENGTH_LONG).show()
                    }
                },
                trailing = { IrisChevron() }
            )
            IrisRowDivider()
            IrisSettingsRow(
                icon = R.drawable.ic_ms_privacy_tip,
                title = sessionModel.tr("settings_privacy_row"),
                onClick = { showPrivacyDialog = true },
                trailing = { IrisChevron() }
            )
            IrisRowDivider()
            IrisSettingsRow(
                icon = R.drawable.ic_ms_info,
                title = sessionModel.tr("settings_version_row"),
                trailing = { IrisTrailingText(com.example.myapplication.BuildConfig.VERSION_NAME) }
            )
        }

        IrisSectionLabel(sessionModel.tr("settings_section_data"))
        IrisSettingsGroup {
            IrisSettingsRow(
                icon = R.drawable.ic_ms_delete,
                title = sessionModel.tr("settings_delete_btn"),
                subtitle = sessionModel.tr("settings_delete_sub"),
                destructive = true,
                onClick = { showDeleteDialog = true },
                trailing = { IrisChevron() }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showColorToneDialog) {
        ColorToneDialog(sessionModel) { showColorToneDialog = false }
    }
    if (showHealthConnectDisclosure) {
        AlertDialog(
            onDismissRequest = { showHealthConnectDisclosure = false },
            title = { Text(sessionModel.tr("health_connect_disclosure_title")) },
            text = { Text(sessionModel.tr("health_connect_disclosure_body")) },
            confirmButton = {
                TextButton(onClick = {
                    showHealthConnectDisclosure = false
                    healthPermissionLauncher.launch(HealthConnectManager.requiredPermissions)
                }) {
                    Text(sessionModel.tr("usage_access_open_settings"))
                }
            },
            dismissButton = {
                TextButton(onClick = { showHealthConnectDisclosure = false }) {
                    Text(sessionModel.tr("btn_cancel"))
                }
            }
        )
    }
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            icon = { Icon(painterResource(R.drawable.ic_ms_privacy_tip), contentDescription = null) },
            title = { Text(sessionModel.tr("privacy_policy_title")) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(sessionModel.tr("privacy_usage"), fontSize = 12.sp)
                    Text(sessionModel.tr("health_connect_disclosure_body"), fontSize = 12.sp)
                    Text(sessionModel.tr("privacy_sensor"), fontSize = 12.sp)
                    Text(sessionModel.tr("privacy_storage"), fontSize = 12.sp)
                    Text(sessionModel.tr("privacy_ai"), fontSize = 12.sp)
                    Text(sessionModel.tr("privacy_contact"), fontSize = 12.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) { Text(sessionModel.tr("btn_ok")) }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(painterResource(R.drawable.ic_ms_delete), contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(sessionModel.tr("settings_delete_dialog_title")) },
            text = { Text(sessionModel.tr("settings_delete_dialog_body")) },
            confirmButton = {
                TextButton(onClick = {
                    sessionModel.clearAll { showDeleteDialog = false }
                }) { Text(sessionModel.tr("btn_delete"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(sessionModel.tr("btn_cancel")) }
            }
        )
    }
}

@Composable
fun WellnessHomeTab(
    sessionModel: GuidedSessionViewModel,
    modifier: Modifier,
    onNavigateToGym: () -> Unit = {},
    onNavigateToPursuit: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.padding(20.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(sessionModel.tr("home_title"), style = MaterialTheme.typography.headlineMedium)
        val screenTime = sessionModel.screenTimeResult?.let {
            ScreenTimeManager.formatScreenTime(it, sessionModel.currentLang)
        } ?: sessionModel.tr("not_measured")
        Text(
            sessionModel.tr("screen_usage_today").format(screenTime),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        LegacyBatteryTheme {
            EyePedometerCard(sessionModel, onNavigateToGym, onNavigateToPursuit)
        }
        RestHistoryCard(sessionModel)
        GoalAchievementCard(sessionModel)
    }

    if (sessionModel.state.phase != com.example.myapplication.session.SessionPhase.HOME) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.9f).wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Box(modifier = Modifier.padding(20.dp)) {
                    GuidedSessionCard(sessionModel)
                }
            }
        }
    }
}
