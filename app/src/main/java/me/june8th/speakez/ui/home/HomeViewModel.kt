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
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val ttsManager: TtsManager,
    private val mulberrySymbolRepository: MulberrySymbolRepository,
    @ApplicationContext private val context: Context,
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
    val allSymbols: StateFlow<List<MulberrySymbol>> = _symbols.asStateFlow()

    private val _recommendationSymbols = MutableStateFlow<List<MulberrySymbol>>(emptyList())
    val recommendationSymbols: StateFlow<List<MulberrySymbol>> = _recommendationSymbols.asStateFlow()

    private val _favoriteSymbols = MutableStateFlow<List<MulberrySymbol>>(emptyList())
    val favoriteSymbols: StateFlow<List<MulberrySymbol>> = _favoriteSymbols.asStateFlow()

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    fun setEditMode(enabled: Boolean) {
        _isEditMode.value = enabled
    }

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _itemsPerPage = MutableStateFlow(24)
    val itemsPerPage: StateFlow<Int> = _itemsPerPage.asStateFlow()

    val filteredSymbols: StateFlow<List<MulberrySymbol>> = combine(
        _selectedCategory,
        _searchQuery,
        _symbols,
        _recommendationSymbols,
        _favoriteSymbols,
    ) { selectedCategory, searchQuery, symbols, recSymbols, favSymbols ->
        val viLocale = Locale.forLanguageTag("vi-VN")
        
        val sourceSymbols = when (selectedCategory) {
            "RECOMMENDATION" -> recSymbols
            "FAVORITES" -> favSymbols
            else -> symbols
        }
        
        if (searchQuery.isNotBlank()) {
            return@combine mulberrySymbolRepository.filterSymbols(
                symbols = sourceSymbols,
                query = searchQuery,
                categoryId = if (selectedCategory == "ALL_SYMBOLS" || selectedCategory == "CATEGORIES_ROOT" || selectedCategory == "RECOMMENDATION" || selectedCategory == "FAVORITES") null else selectedCategory,
            )
        }
        
        when (selectedCategory) {
            "RECOMMENDATION", "FAVORITES" -> {
                sourceSymbols
            }
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
        _currentPage,
        _itemsPerPage
    ) { symbols, page, limit ->
        val startIndex = page * limit
        val endIndex = minOf(startIndex + limit, symbols.size)
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

    val totalPages: StateFlow<Int> = combine(filteredSymbols, _itemsPerPage) { symbols, limit ->
        if (symbols.isEmpty()) 1 else (symbols.size + limit - 1) / limit
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = 1,
    )

    fun refreshGridSize() {
        val sharedPrefs = context.getSharedPreferences("SpeakEZ_Prefs", Context.MODE_PRIVATE)
        val gridChoice = sharedPrefs.getString("grid_choice", "4x6") ?: "4x6"
        _itemsPerPage.value = when (gridChoice) {
            "3x5" -> 15
            "4x6" -> 24
            "5x8" -> 40
            else -> 24
        }
    }

    init {
        // Load initial grid size from preferences
        refreshGridSize()

        viewModelScope.launch {
            val symbols = mulberrySymbolRepository.getSymbols()
            _symbols.value = symbols
            _categories.value = mulberrySymbolRepository.getCategories(symbols)

            val sharedPrefs = context.getSharedPreferences("SpeakEZ_Prefs", Context.MODE_PRIVATE)
            val savedIdsString = sharedPrefs.getString("recommended_ids", null)
            if (savedIdsString != null) {
                val savedIds = savedIdsString.split(",")
                val seenIds = mutableSetOf<String>()
                val mapped = savedIds.mapIndexed { index, id ->
                    if (id.startsWith("PLACEHOLDER") || seenIds.contains(id)) {
                        MulberrySymbol(
                            id = "PLACEHOLDER_$index",
                            categoryId = "",
                            grammar = "",
                            rated = 0,
                            tags = "",
                            symbolEn = "",
                            categoryEn = "",
                            categoryVi = "",
                            symbolVi = "",
                            assetPath = "",
                            isRepresentative = false
                        )
                    } else {
                        seenIds.add(id)
                        symbols.firstOrNull { it.id == id } ?: MulberrySymbol(
                            id = "PLACEHOLDER_$index",
                            categoryId = "",
                            grammar = "",
                            rated = 0,
                            tags = "",
                            symbolEn = "",
                            categoryEn = "",
                            categoryVi = "",
                            symbolVi = "",
                            assetPath = "",
                            isRepresentative = false
                        )
                    }
                }
                _recommendationSymbols.value = mapped
            } else {
                // Randomly select both folders and normal symbols to put into recommendation tab
                val folderSymbols = symbols.filter { it.isRepresentative }
                val normalSymbols = symbols.filter { !it.isRepresentative }
                // Get exactly 120 mixed symbols (e.g., 20 folders and 100 normal cards)
                val mixed = (folderSymbols.shuffled().take(20) + normalSymbols.shuffled().take(100)).shuffled()
                _recommendationSymbols.value = mixed
            }

            // Restore Favorite Symbols
            val savedFavIdsString = sharedPrefs.getString("favorite_ids", null)
            if (savedFavIdsString != null) {
                val savedFavIds = savedFavIdsString.split(",")
                val seenFavIds = mutableSetOf<String>()
                val mappedFav = savedFavIds.mapIndexed { index, id ->
                    if (id.startsWith("PLACEHOLDER") || seenFavIds.contains(id)) {
                        MulberrySymbol(
                            id = "PLACEHOLDER_$index",
                            categoryId = "",
                            grammar = "",
                            rated = 0,
                            tags = "",
                            symbolEn = "",
                            categoryEn = "",
                            categoryVi = "",
                            symbolVi = "",
                            assetPath = "",
                            isRepresentative = false
                        )
                    } else {
                        seenFavIds.add(id)
                        symbols.firstOrNull { it.id == id } ?: MulberrySymbol(
                            id = "PLACEHOLDER_$index",
                            categoryId = "",
                            grammar = "",
                            rated = 0,
                            tags = "",
                            symbolEn = "",
                            categoryEn = "",
                            categoryVi = "",
                            symbolVi = "",
                            assetPath = "",
                            isRepresentative = false
                        )
                    }
                }
                _favoriteSymbols.value = mappedFav
            } else {
                val initialFavs = List(120) { index ->
                    MulberrySymbol(
                        id = "PLACEHOLDER_$index",
                        categoryId = "",
                        grammar = "",
                        rated = 0,
                        tags = "",
                        symbolEn = "",
                        categoryEn = "",
                        categoryVi = "",
                        symbolVi = "",
                        assetPath = "",
                        isRepresentative = false
                    )
                }
                _favoriteSymbols.value = initialFavs
            }

            _isLoading.value = false
        }
    }

    fun deleteRecommendedSymbols(indices: List<Int>) {
        val currentList = _recommendationSymbols.value.toMutableList()
        for (index in indices) {
            if (index in currentList.indices) {
                currentList[index] = MulberrySymbol(
                    id = "PLACEHOLDER_$index",
                    categoryId = "",
                    grammar = "",
                    rated = 0,
                    tags = "",
                    symbolEn = "",
                    categoryEn = "",
                    categoryVi = "",
                    symbolVi = "",
                    assetPath = "",
                    isRepresentative = false
                )
            }
        }
        _recommendationSymbols.value = currentList
    }

    fun addSymbolToRecommendation(index: Int, symbol: MulberrySymbol): Boolean {
        val currentList = _recommendationSymbols.value.toMutableList()
        if (index !in currentList.indices) return false
        if (!currentList[index].id.startsWith("PLACEHOLDER")) return false
        if (currentList.any { it.id == symbol.id }) return false
        currentList[index] = symbol
        _recommendationSymbols.value = currentList
        return true
    }

    fun addRecommendedToFavorites(indices: List<Int>) {
        val currentRec = _recommendationSymbols.value
        val currentFav = _favoriteSymbols.value.toMutableList()
        
        // Find symbols to add, filtering out any duplicates that are already in favorites
        val symbolsToAdd = indices.mapNotNull { index ->
            if (index in currentRec.indices) {
                val sym = currentRec[index]
                if (!sym.id.startsWith("PLACEHOLDER")) sym else null
            } else null
        }.filter { sym ->
            currentFav.none { fav -> fav.id == sym.id }
        }
        
        if (symbolsToAdd.isEmpty()) return
        
        // Place in first available placeholders
        var addIndex = 0
        for (i in currentFav.indices) {
            if (addIndex >= symbolsToAdd.size) break
            if (currentFav[i].id.startsWith("PLACEHOLDER")) {
                currentFav[i] = symbolsToAdd[addIndex]
                addIndex++
            }
        }
        
        _favoriteSymbols.value = currentFav
    }

    fun deleteFavoriteSymbols(indices: List<Int>) {
        val currentList = _favoriteSymbols.value.toMutableList()
        for (index in indices) {
            if (index in currentList.indices) {
                currentList[index] = MulberrySymbol(
                    id = "PLACEHOLDER_$index",
                    categoryId = "",
                    grammar = "",
                    rated = 0,
                    tags = "",
                    symbolEn = "",
                    categoryEn = "",
                    categoryVi = "",
                    symbolVi = "",
                    assetPath = "",
                    isRepresentative = false
                )
            }
        }
        _favoriteSymbols.value = currentList
    }

    fun addSymbolToFavorites(index: Int, symbol: MulberrySymbol): Boolean {
        val currentList = _favoriteSymbols.value.toMutableList()
        if (index !in currentList.indices) return false
        if (!currentList[index].id.startsWith("PLACEHOLDER")) return false
        if (currentList.any { it.id == symbol.id }) return false
        currentList[index] = symbol
        _favoriteSymbols.value = currentList
        return true
    }

    fun saveEditChanges() {
        val sharedPrefs = context.getSharedPreferences("SpeakEZ_Prefs", Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        
        val recIdsString = _recommendationSymbols.value.map { it.id }.joinToString(",")
        editor.putString("recommended_ids", recIdsString)
        
        val favIdsString = _favoriteSymbols.value.map { it.id }.joinToString(",")
        editor.putString("favorite_ids", favIdsString)
        
        editor.apply()
        _isEditMode.value = false
    }

    private var lastCategorySelectTime = 0L
    private var lastAddWordTime = 0L
    private var lastPageChangeTime = 0L

    fun selectCategory(category: String?) {
        val now = System.currentTimeMillis()
        if (now - lastCategorySelectTime < 300) return
        lastCategorySelectTime = now

        // Refresh dynamic grid size choice
        refreshGridSize()

        _selectedCategory.value = category
        _currentPage.value = 0
    }

    fun updateSearchQuery(query: String) {
        // Refresh dynamic grid size choice
        refreshGridSize()

        _searchQuery.value = query
        _currentPage.value = 0
    }

    fun nextPage() {
        val now = System.currentTimeMillis()
        if (now - lastPageChangeTime < 250) return
        lastPageChangeTime = now
        val total = totalPages.value
        if (_currentPage.value < total - 1) {
            _currentPage.value += 1
        }
    }

    fun previousPage() {
        val now = System.currentTimeMillis()
        if (now - lastPageChangeTime < 150) return
        lastPageChangeTime = now
        if (_currentPage.value > 0) {
            _currentPage.value -= 1
        }
    }

    fun addWord(symbol: MulberrySymbol) {
        val now = System.currentTimeMillis()
        if (now - lastAddWordTime < 150) return
        lastAddWordTime = now
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
