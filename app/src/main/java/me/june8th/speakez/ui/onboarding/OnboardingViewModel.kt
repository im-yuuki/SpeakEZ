package me.june8th.speakez.ui.onboarding

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import me.june8th.speakez.tts.TtsManager
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val ttsManager: TtsManager,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    var selectedProfile = mutableStateOf<String?>(null)
    var gridChoice = mutableStateOf("4x6")
    var speechRate = mutableFloatStateOf(1.0f)
    var pitch = mutableFloatStateOf(1.0f)

    fun testVoice() {
        ttsManager.speak(
            text = "Xin chào, đây là giọng đọc thử nghiệm",
            speechRate = speechRate.floatValue,
            pitch = pitch.floatValue,
        )
    }

    fun finish() {
        val prefs = context.getSharedPreferences("SpeakEZ_Prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("onboarding_complete", true)
            .putString("grid_choice", gridChoice.value)
            .putFloat("speech_rate", speechRate.floatValue)
            .putFloat("pitch", pitch.floatValue)
            .putString("profile_name", selectedProfile.value)
            .apply()
        ttsManager.setVoiceConfig(speechRate.floatValue, pitch.floatValue)
    }
}
