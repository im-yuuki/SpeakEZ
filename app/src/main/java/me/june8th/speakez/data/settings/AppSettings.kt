package me.june8th.speakez.data.settings

data class AppSettings(
    val speechRate: Float = DEFAULT_SPEECH_RATE,
    val pitch: Float = DEFAULT_PITCH,
    val fontScale: Float = DEFAULT_FONT_SCALE,
    val selectedVoiceId: String = DEFAULT_SELECTED_VOICE_ID,
)

const val DEFAULT_SPEECH_RATE = 1.0f
const val DEFAULT_PITCH = 1.0f
const val DEFAULT_FONT_SCALE = 1.0f
const val DEFAULT_SELECTED_VOICE_ID = ""

const val MIN_SPEECH_RATE = 0.5f
const val MAX_SPEECH_RATE = 2.0f
const val MIN_PITCH = 0.5f
const val MAX_PITCH = 2.0f
const val MIN_FONT_SCALE = 0.8f
const val MAX_FONT_SCALE = 1.6f
