package me.june8th.speakez.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.june8th.speakez.data.auth.FirebaseAuthRepository
import me.june8th.speakez.data.local.SpeakEZDatabase
import me.june8th.speakez.data.quickphrase.QuickPhraseDao
import me.june8th.speakez.data.quickphrase.QuickPhraseRepositoryImpl
import me.june8th.speakez.data.settings.AppSettingsRepository
import me.june8th.speakez.data.settings.DataStoreAppSettingsRepository
import me.june8th.speakez.domain.repository.QuickPhraseRepository
import me.june8th.speakez.domain.repository.AuthRepository
import me.june8th.speakez.tts.TextSpeaker
import me.june8th.speakez.tts.TtsManager
import javax.inject.Qualifier
import javax.inject.Singleton

private val Context.appSettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_settings",
)

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {
    @Binds
    @Singleton
    abstract fun bindAppSettingsRepository(
        repository: DataStoreAppSettingsRepository,
    ): AppSettingsRepository

    @Binds
    @Singleton
    abstract fun bindQuickPhraseRepository(
        repository: QuickPhraseRepositoryImpl,
    ): QuickPhraseRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        repository: FirebaseAuthRepository,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTextSpeaker(
        ttsManager: TtsManager,
    ): TextSpeaker
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideAppSettingsDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> = context.appSettingsDataStore

    @Provides
    @Singleton
    fun provideSpeakEZDatabase(
        @ApplicationContext context: Context,
    ): SpeakEZDatabase {
        return Room.databaseBuilder(
            context,
            SpeakEZDatabase::class.java,
            "speakez.db",
        ).build()
    }

    @Provides
    fun provideQuickPhraseDao(database: SpeakEZDatabase): QuickPhraseDao {
        return database.quickPhraseDao()
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }
}
