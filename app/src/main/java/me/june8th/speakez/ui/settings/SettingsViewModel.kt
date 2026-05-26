package me.june8th.speakez.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.june8th.speakez.data.mock.MockVocabularyRepository
import me.june8th.speakez.data.settings.AppSettingsRepository
import me.june8th.speakez.data.settings.DEFAULT_FONT_SCALE
import me.june8th.speakez.data.settings.DEFAULT_PITCH
import me.june8th.speakez.data.settings.DEFAULT_SELECTED_VOICE_ID
import me.june8th.speakez.data.settings.DEFAULT_SPEECH_RATE
import me.june8th.speakez.domain.model.VocabularyItem
import me.june8th.speakez.tts.SystemVoiceOption
import me.june8th.speakez.tts.TtsManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository,
    private val ttsManager: TtsManager,
) : ViewModel() {

    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _showLabels = MutableStateFlow(true)
    val showLabels: StateFlow<Boolean> = _showLabels.asStateFlow()
    val vocabularyItems: StateFlow<List<VocabularyItem>> = MockVocabularyRepository.allVocabulary

    val uiState: StateFlow<SettingsUiState> = combine(
        appSettingsRepository.settings,
        ttsManager.vietnameseVoices,
        _volume,
        _showLabels,
    ) { settings, voices, volume, showLabels ->
        SettingsUiState(
            speechRate = settings.speechRate,
            pitch = settings.pitch,
            fontScale = settings.fontScale,
            selectedVoiceId = settings.selectedVoiceId,
            voiceOptions = voices,
            volume = volume,
            showLabels = showLabels,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    fun setSpeechRate(rate: Float) {
        viewModelScope.launch {
            appSettingsRepository.setSpeechRate(rate)
        }
    }

    fun setPitch(pitch: Float) {
        viewModelScope.launch {
            appSettingsRepository.setPitch(pitch)
        }
    }

    fun setFontScale(fontScale: Float) {
        viewModelScope.launch {
            appSettingsRepository.setFontScale(fontScale)
        }
    }

    fun setSelectedVoiceId(voiceId: String) {
        viewModelScope.launch {
            appSettingsRepository.setSelectedVoiceId(voiceId)
        }
    }

    fun setVolume(volume: Float) {
        _volume.value = volume
        // Volume is typically managed by system settings, but can be extended
    }

    fun setShowLabels(show: Boolean) {
        _showLabels.value = show
    }

    fun saveSettings() {
        setVolume(_volume.value)
        setShowLabels(_showLabels.value)
    }

    fun testAudio() {
        ttsManager.speak(
            text = "Xin chào, đây là giọng đọc thử nghiệm",
        )
    }

    fun toggleVocabularyVisibility(itemId: String) {
        MockVocabularyRepository.toggleVisibility(itemId)
    }

    fun addVocabulary(label: String, emoji: String) {
        MockVocabularyRepository.addVocabulary(label = label, emoji = emoji)
    }

    fun updateVocabularyImage(itemId: String, imageUri: String) {
        MockVocabularyRepository.updateCustomImage(itemId = itemId, imageUri = imageUri)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.stop()
    }
}

data class SettingsUiState(
    val speechRate: Float = DEFAULT_SPEECH_RATE,
    val pitch: Float = DEFAULT_PITCH,
    val fontScale: Float = DEFAULT_FONT_SCALE,
    val selectedVoiceId: String = DEFAULT_SELECTED_VOICE_ID,
    val voiceOptions: List<SystemVoiceOption> = emptyList(),
    val volume: Float = 0.8f,
    val showLabels: Boolean = true,
)
