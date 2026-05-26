package me.june8th.speakez.data.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import me.june8th.speakez.di.IoDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreAppSettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AppSettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.data.map { preferences ->
        AppSettings(
            speechRate = preferences[Keys.SPEECH_RATE]?.coerceIn(MIN_SPEECH_RATE, MAX_SPEECH_RATE)
                ?: DEFAULT_SPEECH_RATE,
            pitch = preferences[Keys.PITCH]?.coerceIn(MIN_PITCH, MAX_PITCH) ?: DEFAULT_PITCH,
            fontScale = preferences[Keys.FONT_SCALE]?.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE)
                ?: DEFAULT_FONT_SCALE,
            selectedVoiceId = preferences[Keys.SELECTED_VOICE_ID] ?: DEFAULT_SELECTED_VOICE_ID,
        )
    }

    override val speechRate: Flow<Float> = settings.map { it.speechRate }
    override val pitch: Flow<Float> = settings.map { it.pitch }
    override val fontScale: Flow<Float> = settings.map { it.fontScale }
    override val selectedVoiceId: Flow<String> = settings.map { it.selectedVoiceId }

    override suspend fun setSpeechRate(value: Float) {
        updateFloat(Keys.SPEECH_RATE, value.coerceIn(MIN_SPEECH_RATE, MAX_SPEECH_RATE))
    }

    override suspend fun setPitch(value: Float) {
        updateFloat(Keys.PITCH, value.coerceIn(MIN_PITCH, MAX_PITCH))
    }

    override suspend fun setFontScale(value: Float) {
        updateFloat(Keys.FONT_SCALE, value.coerceIn(MIN_FONT_SCALE, MAX_FONT_SCALE))
    }

    override suspend fun setSelectedVoiceId(value: String) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                preferences[Keys.SELECTED_VOICE_ID] = value
            }
        }
    }

    private suspend fun updateFloat(key: Preferences.Key<Float>, value: Float) {
        withContext(ioDispatcher) {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    private object Keys {
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val PITCH = floatPreferencesKey("pitch")
        val FONT_SCALE = floatPreferencesKey("font_scale")
        val SELECTED_VOICE_ID = stringPreferencesKey("selected_voice_id")
    }
}
