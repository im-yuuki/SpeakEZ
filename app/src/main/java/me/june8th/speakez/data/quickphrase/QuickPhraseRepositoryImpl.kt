package me.june8th.speakez.data.quickphrase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.june8th.speakez.di.ApplicationScope
import me.june8th.speakez.domain.model.ActionType
import me.june8th.speakez.domain.model.QuickPhrase
import me.june8th.speakez.domain.repository.QuickPhraseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuickPhraseRepositoryImpl @Inject constructor(
    private val quickPhraseDao: QuickPhraseDao,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) : QuickPhraseRepository {
    init {
        applicationScope.launch {
            seedDefaultsIfEmpty()
        }
    }

    override fun getQuickPhrases(): Flow<List<QuickPhrase>> {
        return quickPhraseDao.observeQuickPhrases()
            .map { entities -> entities.map(QuickPhraseEntity::toDomain) }
    }

    override suspend fun addQuickPhrase(quickPhrase: QuickPhrase) {
        quickPhraseDao.upsertQuickPhrase(quickPhrase.toEntity())
    }

    override suspend fun updateQuickPhrase(quickPhrase: QuickPhrase) {
        quickPhraseDao.upsertQuickPhrase(quickPhrase.toEntity())
    }

    override suspend fun deleteQuickPhrase(id: String) {
        quickPhraseDao.deleteQuickPhrase(id)
    }

    private suspend fun seedDefaultsIfEmpty() {
        if (quickPhraseDao.countQuickPhrases() > 0) return
        quickPhraseDao.upsertQuickPhrases(defaultQuickPhrases.map(QuickPhrase::toEntity))
    }

    private companion object {
        val defaultQuickPhrases = listOf(
            QuickPhrase(
                id = "default_help",
                text = "Con cần giúp đỡ",
                actionType = ActionType.PUSH_NOTI,
                actionPayload = "helper",
            ),
            QuickPhrase(
                id = "default_pain",
                text = "Con bị đau",
                actionType = ActionType.NONE,
                actionPayload = null,
            ),
            QuickPhrase(
                id = "default_call_family",
                text = "Gọi người thân",
                actionType = ActionType.CALL,
                actionPayload = "0900000000",
            ),
        )
    }
}
