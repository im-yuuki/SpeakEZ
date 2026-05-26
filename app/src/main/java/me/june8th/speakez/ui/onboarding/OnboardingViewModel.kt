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

    var isSignUp = mutableStateOf(false)
    var fullName = mutableStateOf("")
    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var confirmPassword = mutableStateOf("")
    var gridChoice = mutableStateOf("4x6")
    var speechRate = mutableFloatStateOf(1.0f)
    var pitch = mutableFloatStateOf(1.0f)

    fun isAuthFormValid(): Boolean {
        return if (isSignUp.value) {
            fullName.value.isNotBlank() &&
                email.value.isNotBlank() &&
                password.value.isNotBlank() &&
                password.value == confirmPassword.value
        } else {
            email.value.isNotBlank() && password.value.isNotBlank()
        }
    }

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
            .putString("user_email", email.value)
            .putString("user_name", fullName.value.ifBlank { email.value })
            .apply()
        ttsManager.setVoiceConfig(speechRate.floatValue, pitch.floatValue)
    }
}
