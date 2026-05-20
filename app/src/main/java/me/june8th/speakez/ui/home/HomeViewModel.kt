package me.june8th.speakez.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.june8th.speakez.data.mock.MockVocabularyRepository
import me.june8th.speakez.domain.model.VocabularyItem
import me.june8th.speakez.tts.TtsManager
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ttsManager: TtsManager,
) : ViewModel() {
    private val _sentenceWords = MutableStateFlow<List<String>>(emptyList())
    val sentenceWords: StateFlow<List<String>> = _sentenceWords.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _categories = MutableStateFlow<List<VocabularyItem>>(emptyList())
    val categories: StateFlow<List<VocabularyItem>> = _categories.asStateFlow()

    private val allVocabularyFlow = MockVocabularyRepository.allVocabulary

    // Filtered vocabulary based on selected category
    val filteredVocabulary: StateFlow<List<VocabularyItem>> = combine(
        _selectedCategory,
        allVocabularyFlow,
    ) { selectedCat, allVocabulary ->
        val visible = allVocabulary.filter { it.isVisible }
        if (selectedCat == null) visible else visible.filter { it.category == selectedCat }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList()
    )

    init { _categories.value = MockVocabularyRepository.getCategories() }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun addWord(word: String) {
        _sentenceWords.value = _sentenceWords.value + word
        ttsManager.speak(word)
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

    fun speakSentence() {
        val sentence = getSentence()
        if (sentence.isNotBlank()) {
            ttsManager.speak(sentence)
        }
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.stop()
    }
}


