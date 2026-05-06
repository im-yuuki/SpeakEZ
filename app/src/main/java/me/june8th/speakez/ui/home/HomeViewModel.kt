package me.june8th.speakez.ui.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class VocabularyItem(
    val id: String,
    val title: String,
    val iconTint: String,
    val containerColor: String,
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _sentenceWords = MutableStateFlow<List<String>>(emptyList())
    val sentenceWords: StateFlow<List<String>> = _sentenceWords.asStateFlow()

    private val _vocabulary = MutableStateFlow<List<VocabularyItem>>(emptyList())
    val vocabulary: StateFlow<List<VocabularyItem>> = _vocabulary.asStateFlow()

    init {
        // Load mock vocabulary
        loadMockVocabulary()
    }

    private fun loadMockVocabulary() {
        val mockItems = listOf(
            VocabularyItem("1", "Ăn uống", "0xFF0B7A75", "0xFFDDF7F4"),
            VocabularyItem("2", "Y tế", "0xFFB54708", "0xFFFFE8D6"),
            VocabularyItem("3", "Hoạt động", "0xFF2F5AA8", "0xFFDDE8FF"),
            VocabularyItem("4", "Cảm xúc", "0xFF8E3B9E", "0xFFF3E0F8"),
            VocabularyItem("5", "Cơ thể", "0xFF7A5B00", "0xFFFFF0C2"),
        )
        _vocabulary.value = mockItems
    }

    fun addWord(word: String) {
        _sentenceWords.value = _sentenceWords.value + word
    }

    fun removeLastWord() {
        _sentenceWords.value = if (_sentenceWords.value.isNotEmpty()) {
            _sentenceWords.value.dropLast(1)
        } else {
            emptyList()
        }
    }

    fun clearSentence() {
        _sentenceWords.value = emptyList()
    }

    fun getSentence(): String = _sentenceWords.value.joinToString(" ")
}



