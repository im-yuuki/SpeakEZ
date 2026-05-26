package me.june8th.speakez.ui.quick_phrases

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import me.june8th.speakez.domain.model.ActionType
import me.june8th.speakez.domain.model.QuickPhrase
import me.june8th.speakez.domain.repository.QuickPhraseRepository
import me.june8th.speakez.domain.usecase.quickphrase.AddQuickPhraseUseCase
import me.june8th.speakez.domain.usecase.quickphrase.DeleteQuickPhraseUseCase
import me.june8th.speakez.domain.usecase.quickphrase.ExecuteEmergencyActionUseCase
import me.june8th.speakez.domain.usecase.quickphrase.GetQuickPhrasesUseCase
import me.june8th.speakez.domain.usecase.quickphrase.UpdateQuickPhraseUseCase
import me.june8th.speakez.tts.TextSpeaker
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuickPhrasesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_emitsPhrasesFromRepository() = runTest {
        val phrase = quickPhrase(id = "phrase-1")
        val repository = FakeQuickPhraseRepository(listOf(phrase))
        val viewModel = createViewModel(repository = repository)
        collectUiState(viewModel)

        advanceUntilIdle()

        assertEquals(listOf(phrase), viewModel.uiState.value.phrases)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun onPhraseClicked_speaksAndExecutesEmergencyAction() = runTest {
        val phrase = quickPhrase(
            text = "Gọi người thân",
            actionType = ActionType.CALL,
            actionPayload = "0900000000",
        )
        val textSpeaker = FakeTextSpeaker()
        val emergencyActionUseCase = RecordingExecuteEmergencyActionUseCase()
        val viewModel = createViewModel(
            textSpeaker = textSpeaker,
            executeEmergencyActionUseCase = emergencyActionUseCase,
        )

        viewModel.onIntent(QuickPhraseIntent.OnPhraseClicked(phrase))

        assertEquals("Gọi người thân", textSpeaker.spokenTexts.single())
        assertEquals(ActionType.CALL to "0900000000", emergencyActionUseCase.calls.single())
    }

    @Test
    fun saveDraft_withBlankText_doesNotAddPhrase() = runTest {
        val repository = FakeQuickPhraseRepository()
        val viewModel = createViewModel(repository = repository)
        collectUiState(viewModel)

        viewModel.onIntent(QuickPhraseIntent.StartAddPhrase)
        viewModel.onIntent(QuickPhraseIntent.DraftTextChanged("   "))
        viewModel.onIntent(QuickPhraseIntent.SaveDraft)
        advanceUntilIdle()

        assertEquals(emptyList<QuickPhrase>(), repository.currentPhrases)
        assertFalse(viewModel.uiState.value.isDraftValid)
        assertNotNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun saveDraft_addsTrimmedPhraseWithNormalizedPayload() = runTest {
        val repository = FakeQuickPhraseRepository()
        val viewModel = createViewModel(repository = repository)
        collectUiState(viewModel)

        viewModel.onIntent(QuickPhraseIntent.StartAddPhrase)
        viewModel.onIntent(QuickPhraseIntent.DraftTextChanged("  Gọi người thân  "))
        viewModel.onIntent(QuickPhraseIntent.DraftActionTypeChanged(ActionType.CALL))
        viewModel.onIntent(QuickPhraseIntent.DraftPayloadChanged("  0900000000  "))
        viewModel.onIntent(QuickPhraseIntent.SaveDraft)
        advanceUntilIdle()

        val savedPhrase = repository.currentPhrases.single()
        assertEquals("Gọi người thân", savedPhrase.text)
        assertEquals(ActionType.CALL, savedPhrase.actionType)
        assertEquals("0900000000", savedPhrase.actionPayload)
        assertTrue(savedPhrase.id.isNotBlank())
        assertNull(viewModel.uiState.value.editingPhrase)
    }

    @Test
    fun saveDraft_updatesExistingPhraseId() = runTest {
        val existingPhrase = quickPhrase(id = "existing", text = "Cũ")
        val repository = FakeQuickPhraseRepository(listOf(existingPhrase))
        val viewModel = createViewModel(repository = repository)
        collectUiState(viewModel)

        viewModel.onIntent(QuickPhraseIntent.StartEditPhrase(existingPhrase))
        viewModel.onIntent(QuickPhraseIntent.DraftTextChanged("Mới"))
        viewModel.onIntent(QuickPhraseIntent.SaveDraft)
        advanceUntilIdle()

        assertEquals(
            listOf(existingPhrase.copy(text = "Mới")),
            repository.currentPhrases,
        )
    }

    @Test
    fun deletePhrase_delegatesToRepository() = runTest {
        val existingPhrase = quickPhrase(id = "existing")
        val repository = FakeQuickPhraseRepository(listOf(existingPhrase))
        val viewModel = createViewModel(repository = repository)
        collectUiState(viewModel)

        viewModel.onIntent(QuickPhraseIntent.DeletePhrase(existingPhrase.id))
        advanceUntilIdle()

        assertEquals(emptyList<QuickPhrase>(), repository.currentPhrases)
        assertEquals(existingPhrase.id, repository.deletedIds.single())
    }

    private fun TestScope.collectUiState(viewModel: QuickPhrasesViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect {}
        }
    }

    private fun createViewModel(
        repository: FakeQuickPhraseRepository = FakeQuickPhraseRepository(),
        textSpeaker: FakeTextSpeaker = FakeTextSpeaker(),
        executeEmergencyActionUseCase: ExecuteEmergencyActionUseCase = RecordingExecuteEmergencyActionUseCase(),
    ): QuickPhrasesViewModel {
        return QuickPhrasesViewModel(
            getQuickPhrasesUseCase = GetQuickPhrasesUseCase(repository),
            addQuickPhraseUseCase = AddQuickPhraseUseCase(repository),
            updateQuickPhraseUseCase = UpdateQuickPhraseUseCase(repository),
            deleteQuickPhraseUseCase = DeleteQuickPhraseUseCase(repository),
            executeEmergencyActionUseCase = executeEmergencyActionUseCase,
            textSpeaker = textSpeaker,
        )
    }

    private fun quickPhrase(
        id: String = "phrase",
        text: String = "Con cần giúp đỡ",
        actionType: ActionType = ActionType.PUSH_NOTI,
        actionPayload: String? = "helper",
    ): QuickPhrase {
        return QuickPhrase(
            id = id,
            text = text,
            actionType = actionType,
            actionPayload = actionPayload,
        )
    }
}

private class FakeQuickPhraseRepository(
    initialPhrases: List<QuickPhrase> = emptyList(),
) : QuickPhraseRepository {
    private val quickPhrases = MutableStateFlow(initialPhrases)
    val deletedIds = mutableListOf<String>()

    val currentPhrases: List<QuickPhrase>
        get() = quickPhrases.value

    override fun getQuickPhrases(): Flow<List<QuickPhrase>> = quickPhrases

    override suspend fun addQuickPhrase(quickPhrase: QuickPhrase) {
        quickPhrases.value = quickPhrases.value + quickPhrase
    }

    override suspend fun updateQuickPhrase(quickPhrase: QuickPhrase) {
        quickPhrases.value = quickPhrases.value.map { existingPhrase ->
            if (existingPhrase.id == quickPhrase.id) quickPhrase else existingPhrase
        }
    }

    override suspend fun deleteQuickPhrase(id: String) {
        deletedIds += id
        quickPhrases.value = quickPhrases.value.filterNot { quickPhrase -> quickPhrase.id == id }
    }
}

private class FakeTextSpeaker : TextSpeaker {
    val spokenTexts = mutableListOf<String>()
    var stopCount = 0

    override fun speak(text: String, speechRate: Float?, pitch: Float?) {
        spokenTexts += text
    }

    override fun stop() {
        stopCount += 1
    }
}

private class RecordingExecuteEmergencyActionUseCase : ExecuteEmergencyActionUseCase() {
    val calls = mutableListOf<Pair<ActionType, String?>>()

    override fun invoke(actionType: ActionType, actionPayload: String?) {
        calls += actionType to actionPayload
    }
}
