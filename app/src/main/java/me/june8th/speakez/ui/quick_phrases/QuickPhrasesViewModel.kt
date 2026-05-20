package me.june8th.speakez.ui.quick_phrases

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.june8th.speakez.tts.TtsManager
import javax.inject.Inject

@HiltViewModel
class QuickPhrasesViewModel @Inject constructor(
    private val ttsManager: TtsManager,
) : ViewModel() {

    fun speakQuickPhrase(phrase: String) {
        ttsManager.speak(phrase)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.stop()
    }
}

