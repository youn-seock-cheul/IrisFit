package com.example.myapplication.session

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.reminder.BreakReminderScheduler
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GuidedSessionViewModel(application: Application) : AndroidViewModel(application) {
    private val session = GuidedSession(SystemClock::elapsedRealtime) { UUID.randomUUID().toString() }
    private val repository = FileCompletionStorage.repository(application)
    private val markerPreferences = MarkerPreferences(FileCompletionStorage(application, "marker_preferences_v1.txt"))
    private var preferencesLoaded = false

    val currentLang: String
        get() = AppLocalization.resolveEffectiveLang("")

    fun setLanguage(lang: String) {}

    fun tr(key: String): String {
        return AppLocalization.get(currentLang, key)
    }

    val poseDetector = com.example.myapplication.data.SmartDevicePoseDetector(application)

    init {
        poseDetector.startListening()
    }

    override fun onCleared() {
        super.onCleared()
        poseDetector.stopListening()
    }
    var savedMarker by mutableStateOf<MarkerPreference?>(null)
        private set
    var markerSettingsMessage by mutableStateOf<String?>(null)
        private set
    var state by mutableStateOf(session.state)
        private set
    var records by mutableStateOf<List<CompletionRecord>>(emptyList())
        private set
    var busy by mutableStateOf(false)
        private set
    var recordsReady by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    val today: String
        get() = LocalDate.now().toString()
    fun refreshDate() {}

    val pedometerStats: EyePedometerStats
        get() = EyePedometerCalculator.calculateStats(records, today)

    private var lastFatigueStatus: EyeFatigueStatus? = null

    val eyeBatteryStats: EyeBatteryStats
        get() {
            val hasUsagePermission = com.example.myapplication.data.ScreenTimeManager.hasUsageStatsPermission(getApplication())
            val screenMinutes = if (hasUsagePermission) {
                com.example.myapplication.data.ScreenTimeManager.getTodayScreenTimeMinutes(getApplication())
            } else {
                com.example.myapplication.data.AppSessionTimer.getActiveMinutes(getApplication())
            }
            val rests = records.count { it.completedLocalDate == today && it.routineId == "screen_rest" }
            val miniGames = records.count { it.completedLocalDate == today && (it.routineId == "eye_gym" || it.routineId == "smooth_pursuit") }

            val pose = poseDetector.currentPose
            val stats = EyeBatteryCalculator.calculateBattery(
                screenTimeMinutes = screenMinutes,
                restCount = rests,
                miniGameCount = miniGames,
                poseMultiplier = pose.drainMultiplier,
                isCharging = pose.isCharging
            )

            // Check status change for notification
            if (lastFatigueStatus == null) {
                lastFatigueStatus = stats.fatigueStatus
            } else if (lastFatigueStatus != stats.fatigueStatus) {
                lastFatigueStatus = stats.fatigueStatus
                try {
                    BreakReminderScheduler(getApplication()).sendBatteryStateChangeNotification(stats.fatigueStatus)
                } catch (_: Exception) {}
            }

            return stats
        }

    val todayRestCount: Int
        get() = pedometerStats.totalRestCount

    val todayRestSeconds: Int
        get() = todayRestCount * 30

    val todayPoints: Int
        get() = pedometerStats.totalPoints

    val todayEyeSteps: Int
        get() = pedometerStats.totalEyeSteps

    val dailyGoalCount: Int = 5

    val dailyGoalProgress: Float
        get() = minOf(1.0f, todayRestCount.toFloat() / dailyGoalCount.toFloat())
    private var pausedByLifecycle = false
    private var resumeAfterConfiguration = false
    private var pendingRecord: CompletionRecord? = null
    var signalMode by mutableStateOf(EndSignalMode.SILENT)
        private set
    var onEndSignal: ((EndSignalMode) -> Unit)? = null
    fun selectSignal(mode: EndSignalMode) {
        if (state.phase == SessionPhase.READY) signalMode = mode
    }
    fun previewSignal() {
        if (state.phase == SessionPhase.READY) onEndSignal?.invoke(signalMode)
    }

    init { reloadRecords() }

    fun reloadRecords() {
        if (busy) return
        busy = true
        viewModelScope.launch {
            try {
                val loaded = withContext(Dispatchers.IO) { repository.records() to markerPreferences.read() }
                records = loaded.first
                savedMarker = loaded.second
                if (!preferencesLoaded) {
                    loaded.second?.let {
                        markerSpeedStep = it.speedStep
                        markerRangeStep = it.rangeStep
                    }
                    preferencesLoaded = true
                }
                recordsReady = true
                error = null
            } catch (_: Exception) {
                error = "완료 기록 또는 저장한 설정을 읽지 못했어요. 다시 읽거나 전체 삭제 후 시작해 주세요."
            } finally { busy = false }
        }
    }

    var markerSpeedStep by mutableStateOf(4)
        private set
    var markerRangeStep by mutableStateOf(4)
        private set
    fun setMarkerComfort(speedStep: Int, rangeStep: Int) {
        if (!busy && isMarker && state.phase == SessionPhase.READY) {
            markerSpeedStep = speedStep.coerceIn(0, 4)
            markerRangeStep = rangeStep.coerceIn(0, 4)
            markerSettingsMessage = null
        }
    }
    fun resetMarkerComfort() { setMarkerComfort(4, 4) }
    fun loadSavedMarker() {
        savedMarker?.let { setMarkerComfort(it.speedStep, it.rangeStep) }
    }
    fun saveMarkerComfort() {
        if (busy || !isMarker || state.phase != SessionPhase.READY) return
        val value = MarkerPreference(markerSpeedStep, markerRangeStep)
        busy = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { markerPreferences.save(value) }
                savedMarker = value
                markerSettingsMessage = "이 기기에 설정을 저장했어요."
            } catch (_: Exception) {
                markerSettingsMessage = "설정을 저장하지 못했어요. 다시 시도해 주세요."
            } finally { busy = false }
        }
    }
    var isMarker by mutableStateOf(false)
        private set
    fun prepareMarker() {
        if (busy || !recordsReady) return
        pendingRecord = null
        error = null
        isMarker = true
        session.prepare(20_000L)
        publish()
    }

    fun prepare() {
        if (busy || !recordsReady) return
        pendingRecord = null
        error = null
        isMarker = false
        session.prepare()
        session.start()
        publish()
    }
    fun start() { if (!busy) { session.start(); publish() } }
    fun tick() {
        val wasRunning = state.phase == SessionPhase.RUNNING
        session.tick()
        publish()
        if (wasRunning && state.phase == SessionPhase.CONFIRM) onEndSignal?.invoke(signalMode)
    }
    fun pause() { session.pause(); publish() }
    fun resume() {
        pausedByLifecycle = false
        resumeAfterConfiguration = false
        session.resume()
        publish()
    }
    fun end() {
        if (busy) return
        pendingRecord = null
        error = null
        session.end()
        publish()
    }
    fun home() {
        if (busy) return
        pendingRecord = null
        error = null
        session.home()
        publish()
    }
    fun back() {
        if (busy) return
        when (state.phase) {
            SessionPhase.RUNNING, SessionPhase.PAUSED, SessionPhase.CONFIRM -> end()
            else -> home()
        }
    }

    fun activityPaused() {
        pausedByLifecycle = state.phase == SessionPhase.RUNNING
        resumeAfterConfiguration = false
        session.pause()
        publish()
    }
    fun activityStopped(changingConfigurations: Boolean) {
        resumeAfterConfiguration = changingConfigurations && pausedByLifecycle &&
            state.phase == SessionPhase.PAUSED
    }
    fun activityResumed() {
        refreshDate()
        if (recordsReady) reloadRecords()
        if (resumeAfterConfiguration) session.resume()
        pausedByLifecycle = false
        resumeAfterConfiguration = false
        publish()
        com.example.myapplication.widget.IrisFitWidgetProvider.updateAllWidgets(getApplication())
    }

    fun confirm() {
        if (busy || isMarker || state.phase != SessionPhase.CONFIRM) return
        val record = pendingRecord ?: CompletionRecord(state.sessionId,
            System.currentTimeMillis(), LocalDate.now().toString()).also { pendingRecord = it }
        busy = true
        viewModelScope.launch {
            try {
                records = withContext(Dispatchers.IO) { repository.save(record) }
                error = null
                session.saved()
                publish()
                com.example.myapplication.widget.IrisFitWidgetProvider.updateAllWidgets(getApplication())
            } catch (_: Exception) {
                error = "완료 기록을 저장하지 못했어요."
            } finally { busy = false }
        }
    }

    fun completeEyeGym(onSuccess: () -> Unit) {
        if (busy) return
        val record = CompletionRecord(
            sessionId = UUID.randomUUID().toString(),
            completedAtEpochMillis = System.currentTimeMillis(),
            completedLocalDate = LocalDate.now().toString(),
            routineId = "eye_gym",
            routineVersion = 1,
            plannedDurationSeconds = 20,
            confirmation = "user_confirmed"
        )
        busy = true
        viewModelScope.launch {
            try {
                records = withContext(Dispatchers.IO) { repository.save(record) }
                error = null
                onSuccess()
                com.example.myapplication.widget.IrisFitWidgetProvider.updateAllWidgets(getApplication())
            } catch (_: Exception) {
                error = "미니게임 완료 기록을 저장하지 못했어요."
            } finally { busy = false }
        }
    }

    fun completeSmoothPursuit(onSuccess: () -> Unit) {
        if (busy) return
        val record = CompletionRecord(
            sessionId = UUID.randomUUID().toString(),
            completedAtEpochMillis = System.currentTimeMillis(),
            completedLocalDate = LocalDate.now().toString(),
            routineId = "smooth_pursuit",
            routineVersion = 1,
            plannedDurationSeconds = 20,
            confirmation = "user_confirmed"
        )
        busy = true
        viewModelScope.launch {
            try {
                records = withContext(Dispatchers.IO) { repository.save(record) }
                error = null
                onSuccess()
                com.example.myapplication.widget.IrisFitWidgetProvider.updateAllWidgets(getApplication())
            } catch (_: Exception) {
                error = "시선 추적 미니게임 완료 기록을 저장하지 못했어요."
            } finally { busy = false }
        }
    }

    fun clearAll(onSuccess: () -> Unit) {
        if (busy) return
        session.pause()
        publish()
        busy = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    markerPreferences.clear()
                    repository.clear()
                }
                savedMarker = null
                preferencesLoaded = true
                markerSettingsMessage = null
                markerSpeedStep = 4
                markerRangeStep = 4
                records = emptyList()
                recordsReady = true
                pendingRecord = null
                session.home()
                publish()
                error = null
                onSuccess()
            } catch (_: Exception) {
                error = "완료 기록을 삭제하지 못했어요. 다시 시도해 주세요."
            } finally { busy = false }
        }
    }

    private fun publish() { state = session.state }
}
