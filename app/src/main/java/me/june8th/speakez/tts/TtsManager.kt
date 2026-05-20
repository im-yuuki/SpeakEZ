package me.june8th.speakez.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Singleton
class TtsManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    init {
        initTts()
    }

    private fun initTts() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                // Set default language to Vietnamese
                tts?.language = java.util.Locale("vi", "VN")
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
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null)
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

    fun shutdown() {
        tts?.shutdown()
        isInitialized = false
    }
}

@Module
@InstallIn(SingletonComponent::class)
object TtsModule {
    @Singleton
    @Provides
    fun provideTtsManager(@ApplicationContext context: Context): TtsManager {
        return TtsManager(context)
    }
}
