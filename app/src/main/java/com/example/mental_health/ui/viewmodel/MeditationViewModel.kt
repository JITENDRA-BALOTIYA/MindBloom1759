package com.example.mental_health.ui.viewmodel

import android.app.Application
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.mental_health.data.model.MeditationSession
import com.example.mental_health.data.repository.MeditationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

// ─────────────────────────────────────────────────────────────────────────────
// UI & Session States
// ─────────────────────────────────────────────────────────────────────────────
data class MeditationUiState(
    val sessions:          List<MeditationSession> = emptyList(),
    val filteredSessions:  List<MeditationSession> = emptyList(),
    val categories:        List<String>            = listOf("All", "Breathing", "Mindfulness", "Physical", "Focus", "Sleep"),
    val selectedCategory:  String                  = "All",
    val searchQuery:       String                  = "",
    val isLoading:         Boolean                 = false,
    val error:             String?                 = null,
    val favorites:         Set<String>             = emptySet()
)

data class ActiveSessionState(
    val session:       MeditationSession? = null,
    val status:        SessionStatus      = SessionStatus.IDLE,
    val countdown:     Int                = 0,
    val elapsedSeconds: Int               = 0,
    val totalSeconds:  Int                = 0,
    val progress:      Float              = 0f,
    val phase:         BreathingPhase     = BreathingPhase.IDLE,
    val phaseProgress: Float              = 0f,
    val ambientSound:  AmbientSound       = AmbientSound.NONE
)

enum class SessionStatus  { IDLE, COUNTDOWN, RUNNING, PAUSED, COMPLETED }
enum class BreathingPhase { IDLE, INHALE, HOLD, EXHALE, RECOVER }
enum class AmbientSound(val label: String) {
    NONE("None"), RAIN("Rain"), OCEAN("Ocean"), FOREST("Forest"), FIRE("Fire")
}

@HiltViewModel
class MeditationViewModel @Inject constructor(
    application: Application,
    private val repository: MeditationRepository,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val _uiState = MutableStateFlow(MeditationUiState())
    val uiState = _uiState.asStateFlow()

    private val _activeSession = MutableStateFlow(ActiveSessionState())
    val activeSession = _activeSession.asStateFlow()

    private val _streakHeatmap    = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val streakHeatmap = _streakHeatmap.asStateFlow()

    private val _dailyGoalProgress = MutableStateFlow(0.7f)
    val dailyGoalProgress = _dailyGoalProgress.asStateFlow()

    private var timerJob:  Job? = null
    private var breathJob: Job? = null

    // ── TTS: lazily init, safe to use only in MeditationScreen ────────────
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    // ── FIX: isTtsEnabled — Dashboard cards pe TTS off ───────────────────
    // Set to true only when MeditationScreen is open
    var isTtsEnabled: Boolean = false

    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (application.getSystemService(VibratorManager::class.java)).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Vibrator::class.java)
    }

    private val breathingPatterns = mapOf(
        "Deep Breathing"    to listOf(4, 4, 4, 2),
        "Box Breathing"     to listOf(4, 4, 4, 4),
        "Mindful Breathing" to listOf(5, 0, 5, 0),
        "Relaxation"        to listOf(4, 2, 6, 2)
    )

    init {
        loadSessions()
        _streakHeatmap.value = (1..30).associateWith {
            if (it % 3 == 0) 0 else (1..3).random()
        }
    }

    // ── Init TTS only when MeditationScreen opens ─────────────────────────
    fun initTts() {
        if (tts == null) {
            tts = TextToSpeech(getApplication(), this)
        }
        isTtsEnabled = true
    }

    // ── Call when leaving MeditationScreen ───────────────────────────────
    fun disableTts() {
        isTtsEnabled = false
    }

    fun loadSessions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getMeditationSessions()
                .onSuccess { sessions ->
                    _uiState.update {
                        it.copy(sessions = sessions, filteredSessions = sessions, isLoading = false)
                    }
                }
                .onFailure { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onCategorySelect(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    private fun applyFilters() {
        val current = _uiState.value
        val filtered = current.sessions.filter {
            (current.selectedCategory == "All" || it.category == current.selectedCategory) &&
                    it.title.contains(current.searchQuery, ignoreCase = true)
        }
        _uiState.update { it.copy(filteredSessions = filtered) }
    }

    fun toggleFavorite(id: String) {
        _uiState.update {
            val newFavs = it.favorites.toMutableSet()
            if (newFavs.contains(id)) newFavs.remove(id) else newFavs.add(id)
            it.copy(favorites = newFavs)
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Session Logic
    // ─────────────────────────────────────────────────────────────────────────────

    // ── FIX: startSession — no TTS, no crash-prone coroutine on Dashboard ─
    fun startSession(session: MeditationSession) {
        // Cancel any previous session safely
        timerJob?.cancel()
        breathJob?.cancel()

        val totalSeconds = session.durationMinutes * 60

        _activeSession.value = ActiveSessionState(
            session      = session,
            status       = SessionStatus.COUNTDOWN,
            countdown    = 3,
            totalSeconds = totalSeconds
        )

        triggerHapticSafe(100L)

        viewModelScope.launch {
            // ── FIX: speak only if TTS is enabled (MeditationScreen) ──────
            if (isTtsEnabled) speak("Prepare yourself")

            for (i in 3 downTo 1) {
                _activeSession.update { it.copy(countdown = i) }
                triggerHapticSafe(50L)
                delay(1000L)
            }
            beginActualSession()
        }
    }

    private fun beginActualSession() {
        val state = _activeSession.value
        _activeSession.update { it.copy(status = SessionStatus.RUNNING, countdown = 0) }
        startTimer(state.totalSeconds)
        startBreathingCycle(state.session?.title ?: "")
    }

    fun pauseSession() {
        timerJob?.cancel()
        breathJob?.cancel()
        _activeSession.update { it.copy(status = SessionStatus.PAUSED) }
        triggerHapticSafe(50L)
    }

    fun resumeSession() {
        val state = _activeSession.value
        _activeSession.update { it.copy(status = SessionStatus.RUNNING) }
        startTimer(state.totalSeconds, state.elapsedSeconds)
        startBreathingCycle(state.session?.title ?: "")
        triggerHapticSafe(50L)
    }

    fun stopSession() {
        timerJob?.cancel()
        breathJob?.cancel()
        _activeSession.value = ActiveSessionState()
    }

    private fun startTimer(total: Int, offset: Int = 0) {
        timerJob = viewModelScope.launch {
            for (tick in offset..total) {
                _activeSession.update {
                    it.copy(
                        elapsedSeconds = tick,
                        progress       = if (total > 0) tick.toFloat() / total else 0f
                    )
                }
                if (tick == total) {
                    completeSession()
                    break
                }
                delay(1000L)
            }
        }
    }

    private fun startBreathingCycle(exercise: String) {
        val pattern = breathingPatterns[exercise] ?: listOf(4, 2, 4, 2)
        breathJob = viewModelScope.launch {
            while (isActive) {
                executePhase(BreathingPhase.INHALE,  pattern[0])
                if (pattern[1] > 0) executePhase(BreathingPhase.HOLD,    pattern[1])
                executePhase(BreathingPhase.EXHALE,  pattern[2])
                if (pattern[3] > 0) executePhase(BreathingPhase.RECOVER, pattern[3])
            }
        }
    }

    private suspend fun executePhase(phase: BreathingPhase, duration: Int) {
        _activeSession.update { it.copy(phase = phase) }
        triggerHapticSafe(80L)

        // ── FIX: speak only if TTS enabled ────────────────────────────────
        if (isTtsEnabled) speak(phase.name.lowercase())

        val steps = duration * 20
        for (i in 0..steps) {
            _activeSession.update { it.copy(phaseProgress = i.toFloat() / steps) }
            delay(50L)
        }
    }

    private fun completeSession() {
        _activeSession.update { it.copy(status = SessionStatus.COMPLETED) }
        if (isTtsEnabled) speak("Session complete. Well done.")
        triggerHapticSafe(500L)
        viewModelScope.launch {
            val session = _activeSession.value.session ?: return@launch
            repository.saveCompletedSession(session.id, session.title, session.durationMinutes)
        }
    }

    fun setAmbientSound(sound: AmbientSound) {
        _activeSession.update { it.copy(ambientSound = sound) }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Utils
    // ─────────────────────────────────────────────────────────────────────────────

    // ── FIX: vibrator null-safe wrapper ──────────────────────────────────
    private fun triggerHapticSafe(ms: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(ms)
            }
        } catch (e: Exception) {
            // Ignore haptic errors — never crash for vibration
        }
    }

    private fun speak(text: String) {
        if (ttsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ttsReady = true
            tts?.language  = Locale.US
            tts?.setPitch(0.85f)
            tts?.setSpeechRate(0.9f)
        }
    }

    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        timerJob?.cancel()
        breathJob?.cancel()
    }
}