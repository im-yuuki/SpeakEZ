package me.june8th.speakez.ui.navigation.screen

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import me.june8th.speakez.R
import me.june8th.speakez.domain.model.MulberryCategory
import me.june8th.speakez.domain.model.MulberrySymbol
import me.june8th.speakez.ui.home.HomeViewModel
import androidx.compose.foundation.lazy.grid.items as lazyGridItems
import androidx.compose.foundation.lazy.grid.itemsIndexed as lazyGridItemsIndexed
import androidx.compose.foundation.lazy.items as lazyRowItems

private val categoryPalette = listOf(
    Color(0xFFDDF7F4),
    Color(0xFFFFE8D6),
    Color(0xFFDDE8FF),
    Color(0xFFF3E0F8),
    Color(0xFFFFF0C2),
    Color(0xFFE8F5E9),
    Color(0xFFFFE0E6),
    Color(0xFFE0F2F1),
)

private fun categoryColor(categoryId: String): Color {
    val index = (categoryId.toIntOrNull() ?: categoryId.sumOf { it.code }) % categoryPalette.size
    return categoryPalette[index]
}

@Composable
fun HomeScreen(
    onMenuClick: () -> Unit,
    onQuickPhrasesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = hiltViewModel(
        viewModelStoreOwner = context as androidx.lifecycle.ViewModelStoreOwner
    )
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val sharedPrefs = remember(context) { context.getSharedPreferences("SpeakEZ_Prefs", Context.MODE_PRIVATE) }
    val gridChoice = sharedPrefs.getString("grid_choice", "4x6") ?: "4x6"
    val (gridRows, gridCols) = remember(gridChoice) {
        when (gridChoice) {
            "3x5" -> Pair(3, 5)
            "4x6" -> Pair(4, 6)
            "5x8" -> Pair(5, 8)
            else -> Pair(4, 6)
        }
    }

    androidx.compose.runtime.LaunchedEffect(gridChoice) {
        viewModel.refreshGridSize()
    }

    var isSearchActive by remember { mutableStateOf(false) }
    val searchQuery = viewModel.searchQuery.collectAsState()
    val sentenceWords = viewModel.sentenceWords.collectAsState()

    val isEditMode by viewModel.isEditMode.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedIndices = remember { androidx.compose.runtime.mutableStateListOf<Int>() }
    androidx.compose.runtime.LaunchedEffect(isEditMode) {
        selectedIndices.clear()
    }
    var showSymbolPicker by remember { mutableStateOf(false) }
    var pendingPlaceholderIndex by remember { mutableStateOf(-1) }

    val topBarBackground = MaterialTheme.colorScheme.surfaceVariant
    val buttonColor = MaterialTheme.colorScheme.primary
    val buttonTextColor = MaterialTheme.colorScheme.onPrimary

    // Symbol Picker Dialog
    if (showSymbolPicker && isEditMode) {
        val recSymbols by viewModel.recommendationSymbols.collectAsState()
        val favSymbols by viewModel.favoriteSymbols.collectAsState()
        val currentEditList = if (selectedCategory == "FAVORITES") favSymbols else recSymbols
        val existingIds = remember(currentEditList) {
            currentEditList.filter { !it.id.startsWith("PLACEHOLDER") }.map { it.id }.toSet()
        }

        SymbolPickerDialog(
            viewModel = viewModel,
            existingIds = existingIds,
            onSymbolSelected = { symbol ->
                if (selectedCategory == "FAVORITES") {
                    viewModel.addSymbolToFavorites(pendingPlaceholderIndex, symbol)
                } else {
                    viewModel.addSymbolToRecommendation(pendingPlaceholderIndex, symbol)
                }
                showSymbolPicker = false
            },
            onDismiss = { showSymbolPicker = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isLandscape) {
            // Header bar for landscape (Merged TopBar & Visual SentenceBar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(topBarBackground, shape = MaterialTheme.shapes.medium)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (isEditMode) {
                    // Edit mode Header: Hủy (left), Nút "Ưa thích" (middle), Câu nhanh, Lưu (right)
                    Surface(
                        onClick = { viewModel.setEditMode(false) },
                        modifier = Modifier.height(56.dp).widthIn(min = 104.dp),
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        TopBarActionContent(
                            icon = Icons.Default.Close,
                            text = "Hủy",
                            contentColor = MaterialTheme.colorScheme.onError,
                        )
                    }

                    val selectedCategory by viewModel.selectedCategory.collectAsState()
                    val isEditingFavorites = selectedCategory == "FAVORITES"

                    Spacer(modifier = Modifier.width(2.dp))

                    Surface(
                        onClick = {
                            if (isEditingFavorites) {
                                viewModel.selectCategory("RECOMMENDATION")
                            } else {
                                viewModel.selectCategory("FAVORITES")
                            }
                            selectedIndices.clear()
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        color = if (isEditingFavorites) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isEditingFavorites) Icons.Default.Home else Icons.Default.Favorite,
                                    contentDescription = if (isEditingFavorites) "Chỉnh sửa Đề xuất" else "Chỉnh sửa Ưa thích",
                                    tint = if (isEditingFavorites) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSecondary
                                )
                                Text(
                                    text = if (isEditingFavorites) "Chỉnh sửa Đề xuất" else "Chỉnh sửa Ưa thích",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isEditingFavorites) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    Surface(
                        onClick = onQuickPhrasesClick,
                        modifier = Modifier.height(56.dp).widthIn(min = 104.dp),
                        color = buttonColor,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        TopBarActionContent(
                            icon = Icons.Filled.Bolt,
                            text = "Câu nhanh",
                            contentColor = buttonTextColor,
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    Surface(
                        onClick = { viewModel.saveEditChanges() },
                        modifier = Modifier.height(56.dp).widthIn(min = 104.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        TopBarActionContent(
                            icon = Icons.Default.Save,
                            text = "Lưu",
                            contentColor = buttonTextColor,
                        )
                    }
                } else if (isSearchActive) {
                    // Search layout active
                    IconButton(onClick = {
                        isSearchActive = false
                        viewModel.updateSearchQuery("")
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Đóng",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    OutlinedTextField(
                        value = searchQuery.value,
                        onValueChange = viewModel::updateSearchQuery,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text(text = "Tìm kiếm biểu tượng...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.value.isNotBlank()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear text"
                                    )
                                }
                            }
                        }
                    )
                } else {
                    // Normal mode
                    // Left button: Hamburger Menu
                    Surface(
                        onClick = onMenuClick,
                        modifier = Modifier.height(56.dp).widthIn(min = 104.dp),
                        color = buttonColor,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        TopBarActionContent(
                            icon = Icons.Filled.Menu,
                            text = "Menu",
                            contentColor = buttonTextColor,
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Center: White sentence box (Contains images + text cards)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LazyRow(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (sentenceWords.value.isEmpty()) {
                                    item {
                                        Text(
                                            text = stringResource(R.string.sentence_placeholder),
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                } else {
                                    lazyRowItems(sentenceWords.value) { symbol ->
                                        // Visual card inside Sentence Box
                                        Card(
                                            modifier = Modifier
                                                .height(48.dp)
                                                .widthIn(min = 92.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                            border = BorderStroke(1.dp, Color.LightGray)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                AsyncImage(
                                                    model = symbol.assetPath,
                                                    contentDescription = symbol.symbolVi,
                                                    modifier = Modifier.size(24.dp),
                                                    contentScale = ContentScale.Fit
                                                )
                                                Text(
                                                    text = symbol.symbolVi,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    ),
                                                    color = Color.Black,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Action buttons inside the white box
                            IconButton(
                                onClick = { viewModel.removeLastWord() },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = stringResource(R.string.delete_last_word),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(
                                onClick = { viewModel.speakSentence() },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = stringResource(R.string.speak_sentence),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Button: Quick Phrases (Câu nhanh)
                    Surface(
                        onClick = onQuickPhrasesClick,
                        modifier = Modifier.height(56.dp).widthIn(min = 104.dp),
                        color = buttonColor,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        TopBarActionContent(
                            icon = Icons.Filled.Bolt,
                            text = "Câu nhanh",
                            contentColor = buttonTextColor,
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Right button: Search
                    Surface(
                        onClick = { isSearchActive = true },
                        modifier = Modifier.height(56.dp).widthIn(min = 104.dp),
                        color = buttonColor,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        TopBarActionContent(
                            icon = Icons.Filled.Search,
                            text = "Tìm kiếm",
                            contentColor = buttonTextColor,
                        )
                    }
                }
            }

            // Category Chips Row - Full Width (Compact, height 44dp)
            CategoryRow(
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth(),
                isLandscape = true,
                isEditMode = isEditMode
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Vocabulary Grid - Fixed 6 columns, responsive heights
                SymbolGrid(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f),
                    columns = gridCols,
                    rows = gridRows,
                    isLandscape = true,
                    isEditMode = isEditMode,
                    selectedIndices = selectedIndices,
                    onPlaceholderClick = { index ->
                        pendingPlaceholderIndex = index
                        showSymbolPicker = true
                    }
                )

                // Control Column on the right
                ControlColumn(
                    viewModel = viewModel,
                    isEditMode = isEditMode,
                    selectedIndices = selectedIndices.toList(),
                    onDeleteClick = {
                        if (selectedCategory == "FAVORITES") {
                            viewModel.deleteFavoriteSymbols(selectedIndices.toList())
                        } else {
                            viewModel.deleteRecommendedSymbols(selectedIndices.toList())
                        }
                        selectedIndices.clear()
                    },
                    onAddToFavoritesClick = {
                        viewModel.addRecommendedToFavorites(selectedIndices.toList())
                        selectedIndices.clear()
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .fillMaxHeight()
                )
            }
        } else {
            // Portrait
            SentenceBar(
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth(),
                isLandscape = false
            )
            SymbolSearchBar(
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth()
            )
            CategoryRow(
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth(),
                isLandscape = false,
                isEditMode = isEditMode
            )
            SymbolGrid(
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                columns = 2,
                isLandscape = false,
                isEditMode = isEditMode,
                selectedIndices = selectedIndices,
                onPlaceholderClick = { index ->
                    pendingPlaceholderIndex = index
                    showSymbolPicker = true
                }
            )
        }
    }
}

@Composable
private fun SentenceBar(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
) {
    val sentenceWords = viewModel.sentenceWords.collectAsState()
    val verticalPadding = if (isLandscape) 6.dp else 14.dp
    val buttonSize = if (isLandscape) 48.dp else 64.dp
    val iconSize = if (isLandscape) 24.dp else 28.dp

    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = verticalPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (sentenceWords.value.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.sentence_placeholder),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        )
                    }
                } else {
                    lazyRowItems(sentenceWords.value) { symbol ->
                        Card(
                            modifier = Modifier.height(56.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxHeight().padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                              ) {
                                AsyncImage(
                                    model = symbol.assetPath,
                                    contentDescription = symbol.symbolVi,
                                    modifier = Modifier.size(36.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = symbol.symbolVi,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            IconButton(
                onClick = { viewModel.removeLastWord() },
                modifier = Modifier.size(buttonSize),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.delete_last_word),
                    modifier = Modifier.size(iconSize),
                )
            }
            IconButton(
                onClick = { viewModel.speakSentence() },
                modifier = Modifier.size(buttonSize),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = stringResource(R.string.speak_sentence),
                    modifier = Modifier.size(iconSize),
                )
            }
        }
    }
}

@Composable
private fun SymbolSearchBar(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val searchQuery = viewModel.searchQuery.collectAsState()

    OutlinedTextField(
        value = searchQuery.value,
        onValueChange = viewModel::updateSearchQuery,
        modifier = modifier,
        singleLine = true,
        label = { Text(text = stringResource(R.string.symbol_search_label)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
            )
        },
        trailingIcon = {
            if (searchQuery.value.isNotBlank()) {
                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(R.string.symbol_search_clear),
                    )
                }
            }
        },
    )
}

@Composable
private fun TopBarActionContent(
    icon: ImageVector,
    text: String,
    contentColor: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun CategoryRow(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
    isEditMode: Boolean = false,
) {
    val categories = viewModel.categories.collectAsState()
    val selectedCategory = viewModel.selectedCategory.collectAsState()
    val favoriteSymbols = viewModel.favoriteSymbols.collectAsState()
    val totalSymbols = categories.value.sumOf { it.symbolCount }

    Column(modifier = modifier) {
        if (!isLandscape) {
            Text(
                text = stringResource(R.string.categories_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "all") {
                CategoryChip(
                    title = stringResource(R.string.category_all),
                    count = totalSymbols,
                    selected = selectedCategory.value == "ALL_SYMBOLS",
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    onClick = { viewModel.selectCategory("ALL_SYMBOLS") },
                    isLandscape = isLandscape,
                    enabled = !isEditMode
                )
            }
            item(key = "categories_root") {
                CategoryChip(
                    title = "Danh mục",
                    count = categories.value.size,
                    selected = selectedCategory.value == null || selectedCategory.value == "CATEGORIES_ROOT",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = { viewModel.selectCategory("CATEGORIES_ROOT") },
                    isLandscape = isLandscape,
                    enabled = !isEditMode
                )
            }
            item(key = "recommendation") {
                CategoryChip(
                    title = "Đề xuất",
                    count = 120,
                    selected = selectedCategory.value == "RECOMMENDATION",
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    onClick = { viewModel.selectCategory("RECOMMENDATION") },
                    isLandscape = isLandscape,
                    enabled = !isEditMode || selectedCategory.value == "RECOMMENDATION"
                )
            }
            item(key = "favorites") {
                val actualFavCount = favoriteSymbols.value.count { !it.id.startsWith("PLACEHOLDER") }
                CategoryChip(
                    title = "Yêu thích",
                    count = actualFavCount,
                    selected = selectedCategory.value == "FAVORITES",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = { viewModel.selectCategory("FAVORITES") },
                    isLandscape = isLandscape,
                    enabled = !isEditMode || selectedCategory.value == "FAVORITES"
                )
            }
            lazyRowItems(
                items = categories.value,
                key = { category -> category.id },
            ) { category ->
                CategoryChip(
                    category = category,
                    selected = selectedCategory.value == category.id,
                    onClick = {
                        viewModel.selectCategory(
                            if (selectedCategory.value == category.id) "CATEGORIES_ROOT" else category.id,
                        )
                    },
                    isLandscape = isLandscape,
                    enabled = !isEditMode
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: MulberryCategory,
    selected: Boolean,
    onClick: () -> Unit,
    isLandscape: Boolean = false,
    enabled: Boolean = true,
) {
    CategoryChip(
        title = category.title,
        count = category.symbolCount,
        selected = selected,
        containerColor = categoryColor(category.id),
        onClick = onClick,
        isLandscape = isLandscape,
        enabled = enabled
    )
}

@Composable
private fun CategoryChip(
    title: String,
    count: Int,
    selected: Boolean,
    containerColor: Color,
    onClick: () -> Unit,
    isLandscape: Boolean = false,
    enabled: Boolean = true,
) {
    val height = if (isLandscape) 44.dp else 84.dp
    val padding = if (isLandscape) 4.dp else 12.dp
    val cardModifier = if (isLandscape) {
        Modifier
            .height(height)
            .widthIn(min = 110.dp)
    } else {
        Modifier.size(width = 140.dp, height = height)
    }

    Card(
        modifier = cardModifier
            .alpha(if (enabled) 1f else 0.4f),
        border = BorderStroke(
            width = if (selected) 3.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isLandscape) Color.White else (if (selected) containerColor else containerColor.copy(alpha = 0.62f)),
            disabledContainerColor = if (isLandscape) Color.White else (if (selected) containerColor else containerColor.copy(alpha = 0.62f)),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        onClick = onClick,
        enabled = enabled,
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp, vertical = padding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    maxLines = 1,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlaceholderCard(
    isLandscape: Boolean = false,
    cardHeight: androidx.compose.ui.unit.Dp = 156.dp,
    onClick: (() -> Unit)? = null,
) {
    val height = if (isLandscape) cardHeight else 156.dp
    Surface(
        onClick = onClick ?: {},
        color = Color.LightGray.copy(alpha = 0.15f),
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(
                BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
                MaterialTheme.shapes.medium
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Thêm thẻ",
                tint = Color.LightGray,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun SymbolGrid(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    columns: Int,
    isLandscape: Boolean = false,
    rows: Int = 4,
    isEditMode: Boolean = false,
    selectedIndices: androidx.compose.runtime.snapshots.SnapshotStateList<Int> = remember { androidx.compose.runtime.mutableStateListOf() },
    onPlaceholderClick: (Int) -> Unit = {},
) {
    val gridColumns = if (columns < 1) 1 else columns
    val symbols = if (isLandscape) {
        viewModel.paginatedSymbols.collectAsState()
    } else {
        viewModel.filteredSymbols.collectAsState()
    }
    val isLoading = viewModel.isLoading.collectAsState()
    val selectedCategory = viewModel.selectedCategory.collectAsState()
    val searchQuery = viewModel.searchQuery.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val itemsPerPage by viewModel.itemsPerPage.collectAsState()

    val isRootFolders = (selectedCategory.value == null || selectedCategory.value == "CATEGORIES_ROOT") && searchQuery.value.isBlank()
    val isRecommendation = selectedCategory.value == "RECOMMENDATION"
    val isFavorites = selectedCategory.value == "FAVORITES"
    val isRecommendationOrFavorites = isRecommendation || isFavorites

    Column(modifier = modifier) {
        if (!isLandscape) {
            val titleText = if (isRecommendation) {
                "Gợi ý đề xuất (${symbols.value.size})"
            } else if (isFavorites) {
                "Yêu thích (${symbols.value.size})"
            } else if (isRootFolders) {
                "Danh sách thư mục (${symbols.value.size})"
            } else {
                stringResource(R.string.vocabulary_grid_title, symbols.value.size)
            }
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        when {
            isLoading.value -> CenterMessage(text = stringResource(R.string.symbols_loading))
            symbols.value.isEmpty() -> CenterMessage(text = stringResource(R.string.empty_vocabulary))
            else -> {
                if (isLandscape) {
                    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                        val verticalSpacing = 4.dp
                        val rowCount = rows
                        val cardHeight = (maxHeight - (verticalSpacing * (rowCount - 1))) / rowCount

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridColumns),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                            userScrollEnabled = false
                        ) {
                            lazyGridItemsIndexed(
                                items = symbols.value,
                                key = { _, symbol -> symbol.id },
                            ) { pageIndex, symbol ->
                                val overallIndex = currentPage * itemsPerPage + pageIndex
                                val isPlaceholder = symbol.id.startsWith("PLACEHOLDER")

                                if (isPlaceholder) {
                                    PlaceholderCard(
                                        isLandscape = true,
                                        cardHeight = cardHeight,
                                        onClick = if (isEditMode) { { onPlaceholderClick(overallIndex) } } else null
                                    )
                                } else {
                                    val showAsFolder = if (isRecommendationOrFavorites) symbol.isRepresentative else isRootFolders
                                    Box(modifier = Modifier.fillMaxWidth().height(cardHeight)) {
                                        if (showAsFolder) {
                                            FolderCard(
                                                symbol = symbol,
                                                onClick = {
                                                    if (isEditMode) {
                                                        if (selectedIndices.contains(overallIndex)) {
                                                            selectedIndices.remove(overallIndex)
                                                        } else {
                                                            selectedIndices.add(overallIndex)
                                                        }
                                                    } else {
                                                        viewModel.selectCategory(symbol.categoryId)
                                                    }
                                                },
                                                isLandscape = true,
                                                cardHeight = cardHeight
                                            )
                                        } else {
                                            SymbolCard(
                                                symbol = symbol,
                                                onClick = {
                                                    if (isEditMode) {
                                                        if (selectedIndices.contains(overallIndex)) {
                                                            selectedIndices.remove(overallIndex)
                                                        } else {
                                                            selectedIndices.add(overallIndex)
                                                        }
                                                    } else {
                                                        viewModel.addWord(symbol)
                                                    }
                                                },
                                                isLandscape = true,
                                                cardHeight = cardHeight
                                            )
                                        }

                                        if (isEditMode) {
                                            val isChecked = selectedIndices.contains(overallIndex)
                                            Surface(
                                                onClick = {
                                                    if (isChecked) selectedIndices.remove(overallIndex) else selectedIndices.add(overallIndex)
                                                },
                                                modifier = Modifier
                                                    .align(Alignment.TopStart)
                                                    .padding(8.dp)
                                                    .size(24.dp),
                                                color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                                shape = androidx.compose.foundation.shape.CircleShape,
                                                border = BorderStroke(
                                                    width = 2.dp,
                                                    color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                                )
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    if (isChecked) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected",
                                                            tint = MaterialTheme.colorScheme.onPrimary,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        lazyGridItemsIndexed(
                            items = symbols.value,
                            key = { _, symbol -> symbol.id },
                        ) { pageIndex, symbol ->
                            val overallIndex = pageIndex
                            val isPlaceholder = symbol.id.startsWith("PLACEHOLDER")

                            if (isPlaceholder) {
                                PlaceholderCard(
                                    isLandscape = false,
                                    onClick = if (isEditMode) { { onPlaceholderClick(overallIndex) } } else null
                                )
                            } else {
                                val showAsFolder = if (isRecommendationOrFavorites) symbol.isRepresentative else isRootFolders
                                Box(modifier = Modifier.fillMaxWidth().height(156.dp)) {
                                    if (showAsFolder) {
                                        FolderCard(
                                            symbol = symbol,
                                            onClick = {
                                                if (isEditMode) {
                                                    if (selectedIndices.contains(overallIndex)) {
                                                        selectedIndices.remove(overallIndex)
                                                    } else {
                                                        selectedIndices.add(overallIndex)
                                                    }
                                                } else {
                                                    viewModel.selectCategory(symbol.categoryId)
                                                }
                                            },
                                            isLandscape = false
                                        )
                                    } else {
                                        SymbolCard(
                                            symbol = symbol,
                                            onClick = {
                                                if (isEditMode) {
                                                    if (selectedIndices.contains(overallIndex)) {
                                                        selectedIndices.remove(overallIndex)
                                                    } else {
                                                        selectedIndices.add(overallIndex)
                                                    }
                                                } else {
                                                    viewModel.addWord(symbol)
                                                }
                                            },
                                            isLandscape = false
                                        )
                                    }

                                    if (isEditMode) {
                                        val isChecked = selectedIndices.contains(overallIndex)
                                        Surface(
                                            onClick = {
                                                if (isChecked) selectedIndices.remove(overallIndex) else selectedIndices.add(overallIndex)
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(8.dp)
                                                .size(24.dp),
                                            color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                            shape = androidx.compose.foundation.shape.CircleShape,
                                            border = BorderStroke(
                                                width = 2.dp,
                                                color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            )
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                if (isChecked) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected",
                                                        tint = MaterialTheme.colorScheme.onPrimary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderCard(
    symbol: MulberrySymbol,
    onClick: () -> Unit,
    isLandscape: Boolean = false,
    cardHeight: androidx.compose.ui.unit.Dp = 156.dp,
) {
    val height = if (isLandscape) cardHeight else 156.dp
    val imageSize = if (isLandscape) {
        val scaled = cardHeight * 0.45f
        if (scaled > 38.dp) 38.dp else scaled
    } else {
        54.dp
    }
    val verticalSpacer = if (isLandscape) Arrangement.spacedBy(2.dp) else Arrangement.SpaceBetween
    val padding = if (isLandscape) 4.dp else 12.dp

    val folderBgColor = if (isLandscape) Color.White else categoryColor(symbol.categoryId)
    val folderIconColor = Color(0xFFFFA000)

    Surface(
        color = folderBgColor,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(
                BorderStroke(
                    width = 2.dp,
                    color = if (isLandscape) folderIconColor.copy(alpha = 0.8f) else Color.LightGray.copy(alpha = 0.5f)
                ),
                MaterialTheme.shapes.medium,
            ),
        onClick = onClick,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = folderIconColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp)
                    .size(16.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = verticalSpacer,
            ) {
                AsyncImage(
                    model = symbol.assetPath,
                    contentDescription = symbol.categoryVi,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(imageSize),
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = symbol.categoryVi,
                    style = if (isLandscape) {
                        if (cardHeight < 60.dp) {
                            MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        } else {
                            MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        }
                    } else {
                        MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    },
                    color = if (isLandscape) Color.Black else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                
                if (!isLandscape) {
                    Text(
                        text = "Thư mục",
                        style = MaterialTheme.typography.bodySmall,
                        color = folderIconColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun BackCard(
    onClick: () -> Unit,
    isLandscape: Boolean = false,
    cardHeight: androidx.compose.ui.unit.Dp = 156.dp,
) {
    val height = if (isLandscape) cardHeight else 156.dp
    val iconSize = if (isLandscape) {
        val scaled = cardHeight * 0.4f
        if (scaled > 32.dp) 32.dp else scaled
    } else {
        48.dp
    }
    val verticalSpacer = if (isLandscape) Arrangement.spacedBy(2.dp) else Arrangement.SpaceBetween
    val padding = if (isLandscape) 4.dp else 12.dp

    val backColor = if (isLandscape) Color.White else MaterialTheme.colorScheme.surfaceVariant
    val arrowColor = if (isLandscape) Color.DarkGray else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        color = backColor,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = Color.LightGray.copy(alpha = 0.8f)
                ),
                MaterialTheme.shapes.medium,
            ),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = verticalSpacer,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = arrowColor,
                modifier = Modifier.size(iconSize),
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = "Quay lại",
                style = if (isLandscape) {
                    if (cardHeight < 60.dp) {
                        MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    } else {
                        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    }
                } else {
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                },
                color = if (isLandscape) Color.Black else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            
            if (!isLandscape) {
                Text(
                    text = "Trở về trước",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}


@Composable
private fun CenterMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SymbolCard(
    symbol: MulberrySymbol,
    onClick: () -> Unit,
    isLandscape: Boolean = false,
    cardHeight: androidx.compose.ui.unit.Dp = 156.dp,
) {
    val height = if (isLandscape) cardHeight else 156.dp
    val imageSize = if (isLandscape) {
        val scaled = cardHeight * 0.5f
        if (scaled > 40.dp) 40.dp else scaled
    } else {
        58.dp
    }
    val verticalSpacer = if (isLandscape) Arrangement.spacedBy(2.dp) else Arrangement.SpaceBetween
    val padding = if (isLandscape) 4.dp else 12.dp

    Surface(
        color = if (isLandscape) Color.White else categoryColor(symbol.categoryId),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (isLandscape) Color.LightGray else Color.LightGray.copy(alpha = 0.5f)
                ),
                MaterialTheme.shapes.medium,
            ),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = verticalSpacer,
        ) {
            AsyncImage(
                model = symbol.assetPath,
                contentDescription = symbol.symbolVi,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(imageSize),
            )
            Text(
                text = symbol.symbolVi,
                style = if (isLandscape) {
                    if (cardHeight < 60.dp) {
                        MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    } else {
                        MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    }
                } else {
                    MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                },
                color = if (isLandscape) Color.Black else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!isLandscape) {
                Text(
                    text = symbol.categoryVi,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun UnfavoriteIcon(tint: Color) {
    val errorColor = MaterialTheme.colorScheme.error
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = tint
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = errorColor,
                start = Offset(2.dp.toPx(), 2.dp.toPx()),
                end = Offset(size.width - 2.dp.toPx(), size.height - 2.dp.toPx()),
                strokeWidth = 3.dp.toPx()
            )
        }
    }
}

@Composable
private fun ControlColumn(
    viewModel: me.june8th.speakez.ui.home.HomeViewModel,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    selectedIndices: List<Int> = emptyList(),
    onDeleteClick: () -> Unit = {},
    onAddToFavoritesClick: () -> Unit = {}
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()

    val canGoBack = selectedCategory != "CATEGORIES_ROOT" && selectedCategory != null
    val canGoPrev = currentPage > 0
    val canGoNext = currentPage < totalPages - 1
    val isEditingFavorites = selectedCategory == "FAVORITES"

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isEditMode) {
            if (isEditingFavorites) {
                // Edit Favorites mode: Bỏ yêu thích
                ControlButton(
                    icon = null,
                    text = "Bỏ yêu",
                    enabled = selectedIndices.isNotEmpty(),
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    iconContent = {
                        UnfavoriteIcon(
                            tint = if (selectedIndices.isNotEmpty()) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.35f)
                            }
                        )
                    }
                )
            } else {
                // Edit Recommendation mode: Xóa
                ControlButton(
                    icon = Icons.Default.Delete,
                    text = "Xóa",
                    enabled = selectedIndices.isNotEmpty(),
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f)
                )
            }

            ControlButton(
                icon = Icons.Default.Home,
                text = "Trang chủ",
                enabled = true,
                onClick = {
                    viewModel.setEditMode(false)
                    viewModel.selectCategory("CATEGORIES_ROOT")
                    viewModel.updateSearchQuery("")
                },
                modifier = Modifier.weight(1f)
            )

            if (isEditingFavorites) {
                ControlButton(
                    icon = Icons.Default.Favorite,
                    text = "Ưa thích",
                    enabled = false,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            } else {
                // Edit Recommendation mode: add selected items to Favorites
                ControlButton(
                    icon = Icons.Default.Favorite,
                    text = "Thêm Yêu",
                    enabled = selectedIndices.isNotEmpty(),
                    onClick = onAddToFavoritesClick,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            ControlButton(
                icon = Icons.AutoMirrored.Filled.Undo,
                text = "Quay lại",
                enabled = canGoBack,
                onClick = { viewModel.selectCategory("CATEGORIES_ROOT") },
                modifier = Modifier.weight(1f)
            )
            ControlButton(
                icon = Icons.Default.Home,
                text = "Trang chủ",
                enabled = true,
                onClick = {
                    viewModel.selectCategory("CATEGORIES_ROOT")
                    viewModel.updateSearchQuery("")
                },
                modifier = Modifier.weight(1f)
            )
            ControlButton(
                icon = Icons.Default.Favorite,
                text = "Ưa thích",
                enabled = true,
                onClick = { viewModel.selectCategory("FAVORITES") },
                modifier = Modifier.weight(1f)
            )
        }
        ControlButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            text = "Trước",
            enabled = canGoPrev,
            onClick = { viewModel.previousPage() },
            modifier = Modifier.weight(1f)
        )
        ControlButton(
            icon = Icons.AutoMirrored.Filled.ArrowForward,
            text = "Tiếp theo",
            enabled = canGoNext,
            onClick = { viewModel.nextPage() },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconContent: @Composable (() -> Unit)? = null
) {
    val backgroundColor = if (enabled) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    }
    val contentColor = if (enabled) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.35f)
    }

    Surface(
        onClick = { if (enabled) onClick() },
        modifier = modifier.fillMaxWidth(),
        color = backgroundColor,
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(2.dp)
        ) {
            if (iconContent != null) {
                iconContent()
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SymbolPickerDialog(
    viewModel: HomeViewModel,
    existingIds: Set<String>,
    onSymbolSelected: (MulberrySymbol) -> Unit,
    onDismiss: () -> Unit,
) {
    val allSymbols by viewModel.allSymbols.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var dialogCategory by remember { mutableStateOf<String?>("CATEGORIES_ROOT") }
    val isInCategory = dialogCategory != "CATEGORIES_ROOT" && dialogCategory != null && searchQuery.isBlank()

    val displaySymbols = when {
        searchQuery.isNotBlank() -> {
            allSymbols.filter { sym ->
                !sym.id.startsWith("PLACEHOLDER") &&
                !existingIds.contains(sym.id) &&
                (sym.symbolVi.contains(searchQuery, ignoreCase = true) ||
                 sym.symbolEn.contains(searchQuery, ignoreCase = true) ||
                 sym.categoryVi.contains(searchQuery, ignoreCase = true))
            }.sortedBy { it.symbolVi.lowercase() }
        }
        dialogCategory == "CATEGORIES_ROOT" || dialogCategory == null -> {
            allSymbols.filter { it.isRepresentative }
                .distinctBy { it.categoryId }
                .sortedBy { it.categoryVi.lowercase() }
        }
        else -> {
            allSymbols.filter { sym ->
                sym.categoryId == dialogCategory &&
                !sym.id.startsWith("PLACEHOLDER") &&
                !existingIds.contains(sym.id)
            }.sortedBy { it.symbolVi.lowercase() }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .fillMaxHeight(0.94f),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Search bar row (replaces header)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isInCategory) {
                        IconButton(
                            onClick = { dialogCategory = "CATEGORIES_ROOT" },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            if (it.isNotBlank()) dialogCategory = "CATEGORIES_ROOT"
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = if (isInCategory) {
                                    val cat = categories.firstOrNull { it.id == dialogCategory }
                                    "Tìm trong: ${cat?.title ?: "Danh mục"}..."
                                } else {
                                    "Tìm kiếm biểu tượng..."
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Close, "Xóa", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Grid
                if (displaySymbols.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "Không tìm thấy kết quả" else "Danh mục trống",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        contentPadding = PaddingValues(bottom = 4.dp)
                    ) {
                        lazyGridItems(
                            items = displaySymbols,
                            key = { it.id }
                        ) { symbol ->
                            val isFolderNav = symbol.isRepresentative &&
                                dialogCategory == "CATEGORIES_ROOT" &&
                                searchQuery.isBlank()

                            if (isFolderNav) {
                                FolderCard(
                                    symbol = symbol,
                                    onClick = { dialogCategory = symbol.categoryId },
                                    isLandscape = true,
                                    cardHeight = 72.dp
                                )
                            } else {
                                SymbolCard(
                                    symbol = symbol,
                                    onClick = { onSymbolSelected(symbol) },
                                    isLandscape = true,
                                    cardHeight = 72.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
