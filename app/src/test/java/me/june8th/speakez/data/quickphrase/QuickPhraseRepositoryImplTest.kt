package me.june8th.speakez.data.quickphrase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.june8th.speakez.domain.model.ActionType
import me.june8th.speakez.domain.model.QuickPhrase
import org.junit.Assert.assertEquals
import org.junit.Test

class QuickPhraseRepositoryImplTest {
    private val existingPhrase = QuickPhrase(
        id = "existing",
        text = "Có dữ liệu sẵn",
        actionType = ActionType.NONE,
        actionPayload = null,
    )

    @Test
    fun init_seedsDefaultPhrasesWhenDaoIsEmpty() = runBlocking {
        val dao = FakeQuickPhraseDao()

        QuickPhraseRepositoryImpl(
            quickPhraseDao = dao,
            applicationScope = CoroutineScope(Dispatchers.Unconfined),
        )

        assertEquals(3, dao.observeQuickPhrases().first().size)
    }

    @Test
    fun init_doesNotSeedDefaultPhrasesWhenDaoHasData() = runBlocking {
        val dao = FakeQuickPhraseDao(listOf(existingPhrase.toEntity()))

        QuickPhraseRepositoryImpl(
            quickPhraseDao = dao,
            applicationScope = CoroutineScope(Dispatchers.Unconfined),
        )

        assertEquals(listOf(existingPhrase), dao.observeQuickPhrases().first().map(QuickPhraseEntity::toDomain))
    }

    @Test
    fun getQuickPhrases_mapsDaoEntitiesToDomain() = runBlocking {
        val dao = FakeQuickPhraseDao(listOf(existingPhrase.toEntity()))
        val repository = QuickPhraseRepositoryImpl(
            quickPhraseDao = dao,
            applicationScope = CoroutineScope(Dispatchers.Unconfined),
        )

        assertEquals(listOf(existingPhrase), repository.getQuickPhrases().first())
    }

    @Test
    fun addQuickPhrase_upsertsEntityInDao() = runBlocking {
        val dao = FakeQuickPhraseDao(listOf(existingPhrase.toEntity()))
        val repository = QuickPhraseRepositoryImpl(
            quickPhraseDao = dao,
            applicationScope = CoroutineScope(Dispatchers.Unconfined),
        )
        val newPhrase = QuickPhrase(
            id = "new",
            text = "Con cần giúp đỡ",
            actionType = ActionType.PUSH_NOTI,
            actionPayload = "helper",
        )

        repository.addQuickPhrase(newPhrase)

        assertEquals(listOf(existingPhrase, newPhrase), repository.getQuickPhrases().first())
    }

    @Test
    fun updateQuickPhrase_replacesMatchingEntity() = runBlocking {
        val dao = FakeQuickPhraseDao(listOf(existingPhrase.toEntity()))
        val repository = QuickPhraseRepositoryImpl(
            quickPhraseDao = dao,
            applicationScope = CoroutineScope(Dispatchers.Unconfined),
        )
        val updatedPhrase = existingPhrase.copy(text = "Đã cập nhật")

        repository.updateQuickPhrase(updatedPhrase)

        assertEquals(listOf(updatedPhrase), repository.getQuickPhrases().first())
    }

    @Test
    fun deleteQuickPhrase_removesMatchingEntity() = runBlocking {
        val dao = FakeQuickPhraseDao(listOf(existingPhrase.toEntity()))
        val repository = QuickPhraseRepositoryImpl(
            quickPhraseDao = dao,
            applicationScope = CoroutineScope(Dispatchers.Unconfined),
        )

        repository.deleteQuickPhrase(existingPhrase.id)

        assertEquals(emptyList<QuickPhrase>(), repository.getQuickPhrases().first())
    }
}

private class FakeQuickPhraseDao(
    initialPhrases: List<QuickPhraseEntity> = emptyList(),
) : QuickPhraseDao {
    private val quickPhrases = MutableStateFlow(initialPhrases)

    override fun observeQuickPhrases(): Flow<List<QuickPhraseEntity>> = quickPhrases

    override suspend fun countQuickPhrases(): Int = quickPhrases.value.size

    override suspend fun upsertQuickPhrase(quickPhrase: QuickPhraseEntity) {
        quickPhrases.value = quickPhrases.value
            .filterNot { existing -> existing.id == quickPhrase.id } + quickPhrase
    }

    override suspend fun upsertQuickPhrases(quickPhrases: List<QuickPhraseEntity>) {
        quickPhrases.forEach { quickPhrase -> upsertQuickPhrase(quickPhrase) }
    }

    override suspend fun deleteQuickPhrase(id: String) {
        quickPhrases.value = quickPhrases.value.filterNot { quickPhrase -> quickPhrase.id == id }
    }
}
