package me.june8th.speakez.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.june8th.speakez.data.settings.AppSettings
import me.june8th.speakez.data.settings.AppSettingsRepository
import me.june8th.speakez.di.ApplicationScope
import java.util.Locale
import javax.inject.Singleton

@Singleton
class TtsManager(
    private val context: Context,
    private val appSettingsRepository: AppSettingsRepository,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var settingsJob: Job? = null

    private val _vietnameseVoices = MutableStateFlow<List<SystemVoiceOption>>(emptyList())
    val vietnameseVoices: StateFlow<List<SystemVoiceOption>> = _vietnameseVoices.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.language = VIETNAMESE_LOCALE
                refreshVietnameseVoices()
                observeSettings()
            }
        }
    }

    fun speak(
        text: String,
        speechRate: Float? = null,
        pitch: Float? = null,
    ) {
        if (!isInitialized || text.isBlank()) return
        speechRate?.let { setSpeed(it) }
        pitch?.let { setPitch(it) }
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stop() {
        tts?.stop()
    }

    fun setSpeed(speed: Float) {
        tts?.setSpeechRate(speed.coerceIn(0.5f, 2.0f))
    }

    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }

    fun setVoiceConfig(speechRate: Float, pitch: Float) {
        setSpeed(speechRate)
        setPitch(pitch)
    }

    fun refreshVietnameseVoices() {
        val voices = tts?.voices.orEmpty()
        _vietnameseVoices.value = voices
            .filter { voice -> voice.isLocalVietnameseVoice() }
            .sortedWith(compareBy<Voice> { it.locale.toLanguageTag() }.thenBy { it.name })
            .map { voice ->
                SystemVoiceOption(
                    id = voice.name,
                    label = voice.readableLabel(),
                    localeTag = voice.locale.toLanguageTag(),
                )
            }
    }

    fun shutdown() {
        settingsJob?.cancel()
        tts?.shutdown()
        isInitialized = false
    }

    private fun observeSettings() {
        settingsJob?.cancel()
        settingsJob = applicationScope.launch {
            appSettingsRepository.settings.collect { settings ->
                applySettings(settings)
            }
        }
    }

    private fun applySettings(settings: AppSettings) {
        if (!isInitialized) return
        setSpeed(settings.speechRate)
        setPitch(settings.pitch)
        applyVoice(settings.selectedVoiceId)
    }

    private fun applyVoice(voiceId: String) {
        if (voiceId.isBlank()) {
            tts?.language = VIETNAMESE_LOCALE
            return
        }

        val matchingVoice = tts?.voices.orEmpty()
            .firstOrNull { voice -> voice.name == voiceId && voice.isLocalVietnameseVoice() }

        if (matchingVoice != null) {
            tts?.voice = matchingVoice
        } else {
            tts?.language = VIETNAMESE_LOCALE
        }
    }

    private fun Voice.isLocalVietnameseVoice(): Boolean {
        return !isNetworkConnectionRequired && locale.language.equals("vi", ignoreCase = true)
    }

    private fun Voice.readableLabel(): String {
        val localeLabel = locale.getDisplayName(VIETNAMESE_LOCALE)
        return "$localeLabel - $name"
    }

    companion object {
        private val VIETNAMESE_LOCALE = Locale.forLanguageTag("vi-VN")
    }
}

data class SystemVoiceOption(
    val id: String,
    val label: String,
    val localeTag: String,
)

@Module
@InstallIn(SingletonComponent::class)
object TtsModule {
    @Singleton
    @Provides
    fun provideTtsManager(
        @ApplicationContext context: Context,
        appSettingsRepository: AppSettingsRepository,
        @ApplicationScope applicationScope: CoroutineScope,
    ): TtsManager {
        return TtsManager(
            context = context,
            appSettingsRepository = appSettingsRepository,
            applicationScope = applicationScope,
        )
    }
}
