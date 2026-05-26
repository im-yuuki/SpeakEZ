package me.june8th.speakez.data.settings

import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    val settings: Flow<AppSettings>
    val speechRate: Flow<Float>
    val pitch: Flow<Float>
    val fontScale: Flow<Float>
    val selectedVoiceId: Flow<String>

    suspend fun setSpeechRate(value: Float)
    suspend fun setPitch(value: Float)
    suspend fun setFontScale(value: Float)
    suspend fun setSelectedVoiceId(value: String)
}
