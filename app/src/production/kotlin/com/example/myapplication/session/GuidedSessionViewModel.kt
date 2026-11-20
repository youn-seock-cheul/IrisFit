package com.example.myapplication.session

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.theme.AppColorTone
import com.example.myapplication.data.HealthConnectManager
import com.example.myapplication.data.ScreenTimeManager
import com.example.myapplication.data.ScreenTimeQueryResult
import java.io.IOException
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private enum class HealthReadState(val messageKey: String? = null, val connected: Boolean = false) {
    UNCHECKED, READY(connected = true), EMPTY(connected = true),
    PERMISSION_MISSING("health_permission_denied"), FAILED("health_data_read_failed")
}

class GuidedSessionViewModel(application: Application) : AndroidViewModel(application) {
    private val session = GuidedSession(SystemClock::elapsedRealtime) { UUID.randomUUID().toString() }
    private val repository = FileCompletionStorage.repository(application)
    private val markerPreferences = MarkerPreferences(FileCompletionStorage(application, "marker_preferences_v1.txt"))
    private val colorTonePreferences = ColorTonePreferences(FileCompletionStorage(application, "appearance_v1.txt"))
    private val measurementPreferences = application.getSharedPreferences("eye_battery_measurement_v1", Context.MODE_PRIVATE)
    private var batteryRefreshJob: Job? = null
    private var healthReadState by mutableStateOf(HealthReadState.UNCHECKED)
    private var healthSignals by mutableStateOf(HealthBatterySignals())
    var screenTimeResult by mutableStateOf<ScreenTimeQueryResult?>(null)
        private set
    var healthConnectEnabled by mutableStateOf(
        measurementPreferences.getBoolean("health_connect_enabled", false)
    )
        private set
    val healthDataStatusKey: String? get() = healthReadState.messageKey
    val healthConnectLinked: Boolean get() = healthConnectEnabled && healthReadState.connected
    val healthBatterySummary: List<String>
        get() = if (!healthConnectEnabled || healthReadState != HealthReadState.READY) {
            emptyList()
        } else {
            HealthBatterySummaryFormatter.format(healthSignals, currentLang)
        }
    private var preferencesLoaded = false
    var colorTone by mutableStateOf(AppColorTone.GREEN)
        private set
    var colorToneBusy by mutableStateOf(true)
        private set
    var colorToneError by mutableStateOf<String?>(null)
        private set

    fun selectColorTone(tone: AppColorTone) {
        if (colorToneBusy) return
        val previous = colorTone
        colorTone = tone
        colorToneBusy = true
        colorToneError = null
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { colorTonePreferences.save(tone) }
            } catch (failure: IOException) {
                colorTone = previous
                reportColorToneError("tone_save_failed", failure)
            } finally {
                colorToneBusy = false
            }
        }
    }

    private fun reportColorToneError(key: String, failure: Exception) {
        colorToneError = tr(key)
        android.util.Log.e("Appearance", key, failure)
    }

    val currentLang: String
        get() {
            val application = getApplication<Application>()
            return AppLocalization.resolveEffectiveLang(
                com.example.myapplication.data.LanguageManager.effectiveLanguage(application)
            )
        }

    fun setLanguage(lang: String) {}

    fun tr(key: String): String {
        return AppLocalization.get(currentLang, key)
    }

    val poseDetector = com.example.myapplication.data.SmartDevicePoseDetector(application)

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(dateChangeReceiver)
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
    private var currentDate by mutableStateOf(LocalDate.now())
    val today: String
        get() = currentDate.toString()

    private val dateChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            refreshDate()
        }
    }

    fun refreshDate() {
        val newDate = LocalDate.now()
        if (currentDate != newDate) {
            currentDate = newDate
            refreshBattery(refreshHealthData = true)
        }
    }

    val pedometerStats: EyePedometerStats
        get() = EyePedometerCalculator.calculateStats(records, today)

    val eyeBatteryStats: EyeBatteryStats
        get() {
            val rests = records.count { it.completedLocalDate == today && it.routineId == "screen_rest" }
            val miniGames = records.count { it.completedLocalDate == today && (it.routineId == "eye_gym" || it.routineId == "smooth_pursuit") }
            val result = screenTimeResult
            if (result !is ScreenTimeQueryResult.Success) {
                return EyeBatteryEngine.calculateBattery(
                    screenTimeMinutes = 0,
                    restCount = rests,
                    miniGameCount = miniGames,
                    screenTimeMeasured = false
                )
            }
            return EyeBatteryEngine.calculateBattery(
                screenTimeMinutes = result.minutes,
                weightedScreenTimeMinutes = result.weightedMinutes,
                restCount = rests,
                miniGameCount = miniGames,
                healthSignals = healthSignals
            )
        }

    fun updateHealthConnectEnabled(enabled: Boolean) {
        saveHealthConnectEnabled(enabled)
        healthSignals = HealthBatterySignals()
        healthReadState = HealthReadState.UNCHECKED
        refreshBattery(refreshHealthData = true)
    }

    private fun saveHealthConnectEnabled(enabled: Boolean) {
        healthConnectEnabled = enabled
        measurementPreferences.edit().putBoolean("health_connect_enabled", enabled).apply()
    }

    fun refreshBattery(refreshHealthData: Boolean = false) {
        batteryRefreshJob?.cancel()
        if (refreshHealthData && healthConnectEnabled) healthReadState = HealthReadState.UNCHECKED
        val application = getApplication<Application>()
        batteryRefreshJob = viewModelScope.launch {
            val usageResult = withContext(Dispatchers.IO) {
                ScreenTimeManager.queryTodayScreenTime(application)
            }
            if (usageResult is ScreenTimeQueryResult.QueryError) {
                android.util.Log.e("EyeBattery", "Screen-use query failed: ${usageResult.reason}")
            }
            screenTimeResult = usageResult

            if (healthConnectEnabled && healthReadState == HealthReadState.UNCHECKED) {
                healthSignals = HealthBatterySignals()
                try {
                    val granted = withContext(Dispatchers.IO) {
                        HealthConnectManager.grantedPermissions(application)
                    }
                    if (!HealthConnectManager.hasAllPermissions(granted)) {
                        saveHealthConnectEnabled(false)
                        healthReadState = HealthReadState.PERMISSION_MISSING
                    } else {
                        healthSignals = withContext(Dispatchers.IO) {
                            HealthConnectManager.readDailySignals(application)
                        }
                        healthReadState = if (
                            healthSignals.sleepMinutes == null &&
                            healthSignals.steps == 0L &&
                            healthSignals.outdoorActivityMinutes == 0
                        ) HealthReadState.EMPTY else HealthReadState.READY
                    }
                } catch (failure: CancellationException) {
                    throw failure
                } catch (failure: Exception) {
                    android.util.Log.w("EyeBattery", "Health Connect read failed; using screen-use estimate only.", failure)
                    healthReadState = HealthReadState.FAILED
                }
            }
            val stats = eyeBatteryStats
            EyeBatterySnapshotStore.save(application, stats)
            com.example.myapplication.widget.BitmapWidgetProvider.updateWidget1(application)
        }
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

    init {
        getApplication<Application>().registerReceiver(
            dateChangeReceiver,
            IntentFilter().apply {
                addAction(Intent.ACTION_DATE_CHANGED)
                addAction(Intent.ACTION_TIME_CHANGED)
                addAction(Intent.ACTION_TIMEZONE_CHANGED)
            },
            Context.RECEIVER_NOT_EXPORTED
        )
        reloadRecords()
        refreshBattery(refreshHealthData = true)
        viewModelScope.launch {
            try {
                colorTone = withContext(Dispatchers.IO) { colorTonePreferences.read() }
            } catch (failure: IOException) {
                reportColorToneError("tone_load_failed", failure)
            } catch (failure: IllegalArgumentException) {
                reportColorToneError("tone_load_failed", failure)
            } finally {
                colorToneBusy = false
            }
        }
    }

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
                error = tr("records_load_failed")
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
                markerSettingsMessage = tr("marker_saved")
            } catch (_: Exception) {
                markerSettingsMessage = tr("marker_save_failed")
            } finally { busy = false }
        }
    }
    var isMarker by mutableStateOf(false)
        private set
    fun prepareMarker() {
        if (busy || !recordsReady || activeWorkoutRoutineId != null) return
        pendingRecord = null
        error = null
        isMarker = true
        session.prepare(20_000L)
        publish()
    }

    fun prepare() {
        if (busy || !recordsReady || activeWorkoutRoutineId != null) return
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
        activityVisible = false
        poseDetector.stopListening()
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
        activityVisible = true
        poseDetector.startListening()
        refreshDate()
        refreshBattery(refreshHealthData = true)
        if (recordsReady) reloadRecords()
        if (resumeAfterConfiguration) session.resume()
        pausedByLifecycle = false
        resumeAfterConfiguration = false
        publish()
        com.example.myapplication.widget.BitmapWidgetProvider.updateWidget1(getApplication())
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
                com.example.myapplication.widget.BitmapWidgetProvider.updateWidget1(getApplication())
            } catch (_: Exception) {
                error = tr("workout_save_failed")
            } finally { busy = false }
        }
    }

    private val workoutCompletion = WorkoutCompletion(repository)
    var workoutSaved by mutableStateOf(false)
        private set
    var activityVisible by mutableStateOf(false)
        private set
    var activeWorkoutRoutineId by mutableStateOf<String?>(null)
        private set

    fun prepareWorkoutSession(routineId: String): String {
        check(!busy && recordsReady) { "Records are not ready." }
        check(state.phase == SessionPhase.HOME || state.phase == SessionPhase.READY) { "휴식 세션 진행 중에는 훈련을 시작할 수 없습니다." }
        check(activeWorkoutRoutineId == null) { "이미 진행 중인 훈련 세션이 있습니다." }
        workoutSaved = false
        error = null
        activeWorkoutRoutineId = routineId
        return workoutCompletion.start(routineId)
    }

    fun cancelWorkoutSession() {
        activeWorkoutRoutineId = null
    }

    fun completeWorkout(sessionId: String, routineId: String, onSuccess: () -> Unit) {
        if (busy || workoutCompletion.isSaved(sessionId)) return
        busy = true
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    workoutCompletion.save(sessionId, routineId, System.currentTimeMillis(), LocalDate.now().toString())
                }
                records = result.records
                workoutSaved = true
                activeWorkoutRoutineId = null
                error = null
                if (result.newlySaved) onSuccess()
                com.example.myapplication.widget.BitmapWidgetProvider.updateWidget1(getApplication())
            } catch (_: Exception) {
                error = tr("workout_save_failed")
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
                error = tr("records_delete_failed")
            } finally { busy = false }
        }
    }

    private fun publish() { state = session.state }
}
