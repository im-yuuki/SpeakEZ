package me.june8th.speakez.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.june8th.speakez.data.mock.MockVocabularyRepository
import me.june8th.speakez.domain.model.VocabularyItem
import me.june8th.speakez.tts.TtsManager
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val ttsManager: TtsManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    init {
        val prefs = context.getSharedPreferences("SpeakEZ_Prefs", Context.MODE_PRIVATE)
        _speechRate.value = prefs.getFloat("speech_rate", 1.0f)
        _pitch.value = prefs.getFloat("pitch", 1.0f)
        ttsManager.setVoiceConfig(_speechRate.value, _pitch.value)
    }

    private val _volume = MutableStateFlow(0.8f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _showLabels = MutableStateFlow(true)
    val showLabels: StateFlow<Boolean> = _showLabels.asStateFlow()
    val vocabularyItems: StateFlow<List<VocabularyItem>> = MockVocabularyRepository.allVocabulary

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate
    }

    fun setPitch(pitch: Float) {
        _pitch.value = pitch
    }

    fun setVolume(volume: Float) {
        _volume.value = volume
        // Volume is typically managed by system settings, but can be extended
    }

    fun setShowLabels(show: Boolean) {
        _showLabels.value = show
    }

    fun saveSettings() {
        ttsManager.setVoiceConfig(
            speechRate = _speechRate.value,
            pitch = _pitch.value,
        )
        setVolume(_volume.value)
        setShowLabels(_showLabels.value)
        context.getSharedPreferences("SpeakEZ_Prefs", Context.MODE_PRIVATE)
            .edit()
            .putFloat("speech_rate", _speechRate.value)
            .putFloat("pitch", _pitch.value)
            .apply()
    }

    fun testAudio() {
        ttsManager.speak(
            text = "Xin chào, đây là giọng đọc thử nghiệm",
            speechRate = _speechRate.value,
            pitch = _pitch.value,
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
