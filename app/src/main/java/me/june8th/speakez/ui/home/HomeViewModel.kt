package me.june8th.speakez.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import me.june8th.speakez.data.mulberry.MulberrySymbolRepository
import me.june8th.speakez.domain.model.MulberryCategory
import me.june8th.speakez.domain.model.MulberrySymbol
import me.june8th.speakez.tts.TtsManager
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ttsManager: TtsManager,
    private val mulberrySymbolRepository: MulberrySymbolRepository,
) : ViewModel() {
    private val _sentenceWords = MutableStateFlow<List<MulberrySymbol>>(emptyList())
    val sentenceWords: StateFlow<List<MulberrySymbol>> = _sentenceWords.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>("CATEGORIES_ROOT")
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _categories = MutableStateFlow<List<MulberryCategory>>(emptyList())
    val categories: StateFlow<List<MulberryCategory>> = _categories.asStateFlow()

    private val _symbols = MutableStateFlow<List<MulberrySymbol>>(emptyList())

    val filteredSymbols: StateFlow<List<MulberrySymbol>> = combine(
        _selectedCategory,
        _searchQuery,
        _symbols,
    ) { selectedCategory, searchQuery, symbols ->
        val viLocale = Locale.forLanguageTag("vi-VN")
        
        if (searchQuery.isNotBlank()) {
            return@combine mulberrySymbolRepository.filterSymbols(
                symbols = symbols,
                query = searchQuery,
                categoryId = if (selectedCategory == "ALL_SYMBOLS" || selectedCategory == "CATEGORIES_ROOT") null else selectedCategory,
            )
        }
        
        when (selectedCategory) {
            "ALL_SYMBOLS" -> {
                symbols.sortedWith(compareBy<MulberrySymbol> { it.symbolVi.lowercase(viLocale) }.thenBy { it.id.toIntOrNull() ?: Int.MAX_VALUE })
            }
            null, "CATEGORIES_ROOT" -> {
                // Root level: show category folders
                symbols.filter { it.isRepresentative }
                    .distinctBy { it.categoryId }
                    .sortedBy { it.categoryVi.lowercase(viLocale) }
            }
            else -> {
                // Inside a category: show back button + symbols in this category
                val catSymbols = symbols.filter { it.categoryId == selectedCategory }
                    .sortedWith(compareBy<MulberrySymbol> { it.symbolVi.lowercase(viLocale) }.thenBy { it.id.toIntOrNull() ?: Int.MAX_VALUE })
                
                val backSymbol = MulberrySymbol(
                    id = "BACK_BUTTON",
                    categoryId = "",
                    grammar = "",
                    rated = 0,
                    tags = "",
                    symbolEn = "",
                    categoryEn = "",
                    categoryVi = "",
                    symbolVi = "Quay lại",
                    assetPath = ""
                )
                listOf(backSymbol) + catSymbols
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList(),
    )

    init {
        viewModelScope.launch {
            val symbols = mulberrySymbolRepository.getSymbols()
            _symbols.value = symbols
            _categories.value = mulberrySymbolRepository.getCategories(symbols)
            _isLoading.value = false
        }
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addWord(symbol: MulberrySymbol) {
        _sentenceWords.value = _sentenceWords.value + symbol
        ttsManager.speak(symbol.symbolVi)
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

    fun getSentence(): String = _sentenceWords.value.joinToString(" ") { it.symbolVi }

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
