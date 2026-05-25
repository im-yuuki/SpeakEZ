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
import kotlinx.coroutines.flow.map
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

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

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
                // Inside a category: show symbols in this category
                symbols.filter { it.categoryId == selectedCategory }
                    .sortedWith(compareBy<MulberrySymbol> { it.symbolVi.lowercase(viLocale) }.thenBy { it.id.toIntOrNull() ?: Int.MAX_VALUE })
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList(),
    )

    val paginatedSymbols: StateFlow<List<MulberrySymbol>> = combine(
        filteredSymbols,
        _currentPage
    ) { symbols, page ->
        val itemsPerPage = 24
        val startIndex = page * itemsPerPage
        val endIndex = minOf(startIndex + itemsPerPage, symbols.size)
        if (startIndex < symbols.size && startIndex >= 0) {
            symbols.subList(startIndex, endIndex)
        } else {
            emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = emptyList(),
    )

    val totalPages: StateFlow<Int> = filteredSymbols
        .map { symbols ->
            val itemsPerPage = 24
            if (symbols.isEmpty()) 1 else (symbols.size + itemsPerPage - 1) / itemsPerPage
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = 1,
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
        _currentPage.value = 0
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _currentPage.value = 0
    }

    fun nextPage() {
        val total = totalPages.value
        if (_currentPage.value < total - 1) {
            _currentPage.value += 1
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value -= 1
        }
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
