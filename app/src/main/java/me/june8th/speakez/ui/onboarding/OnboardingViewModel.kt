package me.june8th.speakez.ui.onboarding

import android.content.Context
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import me.june8th.speakez.domain.model.AccountGender
import me.june8th.speakez.domain.repository.AuthRepository
import me.june8th.speakez.tts.TtsManager
import me.june8th.speakez.ui.common.toUserMessage
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val ttsManager: TtsManager,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    var selectedProfile = mutableStateOf<String?>(null)
    var displayName = mutableStateOf(authRepository.profileState.value?.displayName ?: "Người dùng")
    var dateOfBirth = mutableStateOf(authRepository.profileState.value?.dateOfBirth.orEmpty())
    var gender = mutableStateOf(authRepository.profileState.value?.gender ?: AccountGender.UNSPECIFIED)
    var gridChoice = mutableStateOf("4x6")
    var speechRate = mutableFloatStateOf(1.0f)
    var pitch = mutableFloatStateOf(1.0f)
    var isFinishing = mutableStateOf(false)
        private set
    var errorMessage = mutableStateOf<String?>(null)
        private set

    val shouldShowPersonalInfoStep: Boolean = authRepository.profileState.value?.isGuest != false

    fun testVoice() {
        ttsManager.speak(
            text = "Xin chào, đây là giọng đọc thử nghiệm",
            speechRate = speechRate.floatValue,
            pitch = pitch.floatValue,
        )
    }

    fun finish(
        savePersonalInfo: Boolean,
        onFinished: () -> Unit,
    ) {
        if (isFinishing.value) return
        errorMessage.value = null
        isFinishing.value = true
        val prefs = context.getSharedPreferences("SpeakEZ_Prefs", Context.MODE_PRIVATE)
        fun saveLocalSettings() {
            prefs.edit()
                .putBoolean("onboarding_complete", true)
                .putString("grid_choice", gridChoice.value)
                .putFloat("speech_rate", speechRate.floatValue)
                .putFloat("pitch", pitch.floatValue)
                .putString("profile_name", displayName.value)
                .apply()
        }

        ttsManager.setVoiceConfig(speechRate.floatValue, pitch.floatValue)
        if (!savePersonalInfo) {
            saveLocalSettings()
            isFinishing.value = false
            onFinished()
            return
        }
        viewModelScope.launch {
            runCatching {
                authRepository.saveProfile(
                    displayName = displayName.value,
                    dateOfBirth = dateOfBirth.value,
                    gender = gender.value,
                )
            }.onSuccess {
                saveLocalSettings()
                isFinishing.value = false
                onFinished()
            }.onFailure { throwable ->
                isFinishing.value = false
                errorMessage.value = throwable.toUserMessage("Không thể lưu hồ sơ")
            }
        }
    }
}
