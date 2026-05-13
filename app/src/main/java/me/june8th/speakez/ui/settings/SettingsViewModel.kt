package me.june8th.speakez.ui.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.june8th.speakez.tts.TtsManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val ttsManager: TtsManager,
) : ViewModel() {

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _showLabels = MutableStateFlow(true)
    val showLabels: StateFlow<Boolean> = _showLabels.asStateFlow()

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
        ttsManager.setSpeed(rate)
    }

    fun setPitch(pitch: Float) {
        _pitch.value = pitch
        ttsManager.setPitch(pitch)
    }

    fun setVolume(volume: Float) {
        _volume.value = volume
        // Volume is typically managed by system settings, but can be extended
    }

    fun setShowLabels(show: Boolean) {
        _showLabels.value = show
    }

    fun saveSettings() {
        // Settings are applied immediately; this hook keeps a single entry point for persistence.
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.stop()
    }
}

