package me.june8th.speakez.ui.quick_phrases

import me.june8th.speakez.domain.model.ActionType
import me.june8th.speakez.domain.model.QuickPhrase

data class QuickPhraseUiState(
    val phrases: List<QuickPhrase> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val isEditorOpen: Boolean = false,
    val editingPhrase: QuickPhrase? = null,
    val draftText: String = "",
    val draftActionType: ActionType = ActionType.NONE,
    val draftActionPayload: String = "",
    val isDraftValid: Boolean = false,
)

sealed interface QuickPhraseIntent {
    data class OnPhraseClicked(val phrase: QuickPhrase) : QuickPhraseIntent
    data object StartAddPhrase : QuickPhraseIntent
    data class StartEditPhrase(val phrase: QuickPhrase) : QuickPhraseIntent
    data object DismissEditor : QuickPhraseIntent
    data class DraftTextChanged(val text: String) : QuickPhraseIntent
    data class DraftActionTypeChanged(val actionType: ActionType) : QuickPhraseIntent
    data class DraftPayloadChanged(val payload: String) : QuickPhraseIntent
    data object SaveDraft : QuickPhraseIntent
    data class DeletePhrase(val phraseId: String) : QuickPhraseIntent
}
