package me.june8th.speakez.ui.quick_phrases

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.june8th.speakez.domain.model.ActionType
import me.june8th.speakez.domain.model.QuickPhrase
import me.june8th.speakez.domain.usecase.quickphrase.AddQuickPhraseUseCase
import me.june8th.speakez.domain.usecase.quickphrase.DeleteQuickPhraseUseCase
import me.june8th.speakez.domain.usecase.quickphrase.ExecuteEmergencyActionUseCase
import me.june8th.speakez.domain.usecase.quickphrase.GetQuickPhrasesUseCase
import me.june8th.speakez.domain.usecase.quickphrase.UpdateQuickPhraseUseCase
import me.june8th.speakez.tts.TextSpeaker
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class QuickPhrasesViewModel @Inject constructor(
    getQuickPhrasesUseCase: GetQuickPhrasesUseCase,
    private val addQuickPhraseUseCase: AddQuickPhraseUseCase,
    private val updateQuickPhraseUseCase: UpdateQuickPhraseUseCase,
    private val deleteQuickPhraseUseCase: DeleteQuickPhraseUseCase,
    private val executeEmergencyActionUseCase: ExecuteEmergencyActionUseCase,
    private val textSpeaker: TextSpeaker,
) : ViewModel() {
    private val editorState = MutableStateFlow(QuickPhraseEditorState())

    private val phraseListState = getQuickPhrasesUseCase()
        .map<List<QuickPhrase>, QuickPhraseListState> { phrases ->
            QuickPhraseListState(
                phrases = phrases,
                isLoading = false,
            )
        }
        .catch { throwable ->
            emit(
                QuickPhraseListState(
                    isLoading = false,
                    errorMessage = throwable.message ?: "Không thể tải câu nhanh",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = QuickPhraseListState(),
        )

    val uiState: StateFlow<QuickPhraseUiState> = combine(
        phraseListState,
        editorState,
    ) { phraseState, editorState ->
        QuickPhraseUiState(
            phrases = phraseState.phrases,
            isLoading = phraseState.isLoading,
            errorMessage = editorState.errorMessage ?: phraseState.errorMessage,
            editingPhrase = editorState.editingPhrase,
            draftText = editorState.draftText,
            draftActionType = editorState.draftActionType,
            draftActionPayload = editorState.draftActionPayload,
            isDraftValid = editorState.isDraftValid,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = QuickPhraseUiState(),
    )

    fun onIntent(intent: QuickPhraseIntent) {
        when (intent) {
            is QuickPhraseIntent.OnPhraseClicked -> handlePhraseClick(intent.phrase)
            QuickPhraseIntent.StartAddPhrase -> startAddPhrase()
            is QuickPhraseIntent.StartEditPhrase -> startEditPhrase(intent.phrase)
            QuickPhraseIntent.DismissEditor -> dismissEditor()
            is QuickPhraseIntent.DraftTextChanged -> updateDraftText(intent.text)
            is QuickPhraseIntent.DraftActionTypeChanged -> updateDraftActionType(intent.actionType)
            is QuickPhraseIntent.DraftPayloadChanged -> updateDraftPayload(intent.payload)
            QuickPhraseIntent.SaveDraft -> saveDraft()
            is QuickPhraseIntent.DeletePhrase -> deletePhrase(intent.phraseId)
        }
    }

    private fun handlePhraseClick(phrase: QuickPhrase) {
        textSpeaker.speak(phrase.text)
        executeEmergencyActionUseCase(phrase.actionType, phrase.actionPayload)
    }

    private fun startAddPhrase() {
        editorState.value = QuickPhraseEditorState()
    }

    private fun startEditPhrase(phrase: QuickPhrase) {
        editorState.value = QuickPhraseEditorState(
            editingPhrase = phrase,
            draftText = phrase.text,
            draftActionType = phrase.actionType,
            draftActionPayload = phrase.actionPayload.orEmpty(),
        ).validated()
    }

    private fun dismissEditor() {
        editorState.value = QuickPhraseEditorState()
    }

    private fun updateDraftText(text: String) {
        editorState.update { state -> state.copy(draftText = text, errorMessage = null).validated() }
    }

    private fun updateDraftActionType(actionType: ActionType) {
        editorState.update { state ->
            state.copy(
                draftActionType = actionType,
                draftActionPayload = if (actionType == ActionType.NONE) "" else state.draftActionPayload,
                errorMessage = null,
            ).validated()
        }
    }

    private fun updateDraftPayload(payload: String) {
        editorState.update { state -> state.copy(draftActionPayload = payload, errorMessage = null).validated() }
    }

    private fun saveDraft() {
        val state = editorState.value.validated()
        if (!state.isDraftValid) {
            editorState.value = state.copy(errorMessage = "Vui lòng nhập đầy đủ nội dung câu nhanh")
            return
        }

        val phrase = QuickPhrase(
            id = state.editingPhrase?.id ?: UUID.randomUUID().toString(),
            text = state.draftText.trim(),
            actionType = state.draftActionType,
            actionPayload = state.normalizedPayload(),
        )

        viewModelScope.launch {
            runCatching {
                if (state.editingPhrase == null) {
                    addQuickPhraseUseCase(phrase)
                } else {
                    updateQuickPhraseUseCase(phrase)
                }
            }.onSuccess {
                editorState.value = QuickPhraseEditorState()
            }.onFailure { throwable ->
                editorState.update { current ->
                    current.copy(errorMessage = throwable.message ?: "Không thể lưu câu nhanh")
                }
            }
        }
    }

    private fun deletePhrase(phraseId: String) {
        viewModelScope.launch {
            runCatching {
                deleteQuickPhraseUseCase(phraseId)
            }.onFailure { throwable ->
                editorState.update { state ->
                    state.copy(errorMessage = throwable.message ?: "Không thể xóa câu nhanh")
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        textSpeaker.stop()
    }
}

private data class QuickPhraseListState(
    val phrases: List<QuickPhrase> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

private data class QuickPhraseEditorState(
    val editingPhrase: QuickPhrase? = null,
    val draftText: String = "",
    val draftActionType: ActionType = ActionType.NONE,
    val draftActionPayload: String = "",
    val isDraftValid: Boolean = false,
    val errorMessage: String? = null,
) {
    fun validated(): QuickPhraseEditorState {
        return copy(isDraftValid = draftText.trim().isNotEmpty() && hasRequiredPayload())
    }

    fun normalizedPayload(): String? {
        return if (draftActionType == ActionType.NONE) null else draftActionPayload.trim()
    }

    private fun hasRequiredPayload(): Boolean {
        return draftActionType == ActionType.NONE || draftActionPayload.trim().isNotEmpty()
    }
}
