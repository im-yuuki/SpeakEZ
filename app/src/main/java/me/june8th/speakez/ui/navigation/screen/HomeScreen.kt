package me.june8th.speakez.ui.navigation.screen

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as lazyGridItems
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import me.june8th.speakez.R
import me.june8th.speakez.domain.model.MulberryCategory
import me.june8th.speakez.domain.model.MulberrySymbol
import me.june8th.speakez.ui.home.HomeViewModel

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
    modifier: Modifier = Modifier
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var isSearchActive by remember { mutableStateOf(false) }
    val searchQuery = viewModel.searchQuery.collectAsState()
    val sentenceWords = viewModel.sentenceWords.collectAsState()

    val topBarBackground = Color(0xFF1E1E24)
    val buttonColor = MaterialTheme.colorScheme.primary
    val buttonTextColor = MaterialTheme.colorScheme.onPrimary

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
                if (isSearchActive) {
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
                        modifier = Modifier.size(width = 86.dp, height = 56.dp),
                        color = buttonColor,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu",
                                tint = buttonTextColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Danh mục",
                                style = MaterialTheme.typography.labelMedium,
                                color = buttonTextColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Center: White sentence box (Contains images + text cards)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        color = Color.White,
                        shape = MaterialTheme.shapes.medium,
                        border = BorderStroke(2.dp, Color.Black)
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
                                            color = Color.Gray,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                } else {
                                    lazyRowItems(sentenceWords.value) { symbol ->
                                        // Visual card inside Sentence Box
                                        Card(
                                            modifier = Modifier
                                                .height(48.dp)
                                                .width(52.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                            border = BorderStroke(1.dp, Color.LightGray)
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxSize().padding(2.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
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
                                                        fontSize = 8.sp
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
                                    tint = Color.Black,
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
                                    tint = Color.Black,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Right button: Search
                    Surface(
                        onClick = { isSearchActive = true },
                        modifier = Modifier.size(width = 86.dp, height = 56.dp),
                        color = buttonColor,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "Tìm kiếm",
                                tint = buttonTextColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Tìm kiếm",
                                style = MaterialTheme.typography.labelMedium,
                                color = buttonTextColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Category Chips Row - Full Width (Compact, height 44dp)
            CategoryRow(
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth(),
                isLandscape = true
            )

            // Vocabulary Grid - Fixed 6 columns, responsive heights
            SymbolGrid(
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                columns = 6,
                isLandscape = true
            )
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
                isLandscape = false
            )
            SymbolGrid(
                viewModel = viewModel,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                columns = 2,
                isLandscape = false
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
private fun CategoryRow(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
) {
    val categories = viewModel.categories.collectAsState()
    val selectedCategory = viewModel.selectedCategory.collectAsState()
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
                    isLandscape = isLandscape
                )
            }
            item(key = "categories_root") {
                CategoryChip(
                    title = "Danh mục",
                    count = categories.value.size,
                    selected = selectedCategory.value == null || selectedCategory.value == "CATEGORIES_ROOT",
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    onClick = { viewModel.selectCategory("CATEGORIES_ROOT") },
                    isLandscape = isLandscape
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
                    isLandscape = isLandscape
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
) {
    CategoryChip(
        title = category.title,
        count = category.symbolCount,
        selected = selected,
        containerColor = categoryColor(category.id),
        onClick = onClick,
        isLandscape = isLandscape
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
) {
    val width = if (isLandscape) 110.dp else 140.dp
    val height = if (isLandscape) 44.dp else 84.dp
    val padding = if (isLandscape) 4.dp else 12.dp

    Card(
        modifier = Modifier.size(width = width, height = height),
        border = BorderStroke(
            width = if (selected) 3.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isLandscape) Color.White else (if (selected) containerColor else containerColor.copy(alpha = 0.62f)),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 4.dp else 1.dp),
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = if (isLandscape) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.titleSmall,
                color = if (isLandscape) Color.Black else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = if (isLandscape) Color.DarkGray else MaterialTheme.colorScheme.onSurfaceVariant,
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
) {
    val gridColumns = if (columns < 1) 1 else columns
    val symbols = viewModel.filteredSymbols.collectAsState()
    val isLoading = viewModel.isLoading.collectAsState()
    val selectedCategory = viewModel.selectedCategory.collectAsState()
    val searchQuery = viewModel.searchQuery.collectAsState()

    val isRootFolders = (selectedCategory.value == null || selectedCategory.value == "CATEGORIES_ROOT") && searchQuery.value.isBlank()

    Column(modifier = modifier) {
        if (!isLandscape) {
            val titleText = if (isRootFolders) {
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
                        val rowCount = 4
                        val cardHeight = (maxHeight - (verticalSpacing * (rowCount - 1))) / rowCount

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(gridColumns),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
                        ) {
                            lazyGridItems(
                                items = symbols.value,
                                key = { symbol -> symbol.id },
                            ) { symbol ->
                                if (isRootFolders) {
                                    FolderCard(
                                        symbol = symbol,
                                        onClick = { viewModel.selectCategory(symbol.categoryId) },
                                        isLandscape = true,
                                        cardHeight = cardHeight
                                    )
                                } else if (symbol.id == "BACK_BUTTON") {
                                    BackCard(
                                        onClick = { viewModel.selectCategory(null) },
                                        isLandscape = true,
                                        cardHeight = cardHeight
                                    )
                                } else {
                                    SymbolCard(
                                        symbol = symbol,
                                        onClick = { viewModel.addWord(symbol) },
                                        isLandscape = true,
                                        cardHeight = cardHeight
                                    )
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
                        lazyGridItems(
                            items = symbols.value,
                            key = { symbol -> symbol.id },
                        ) { symbol ->
                            if (isRootFolders) {
                                FolderCard(
                                    symbol = symbol,
                                    onClick = { viewModel.selectCategory(symbol.categoryId) },
                                    isLandscape = false
                                )
                            } else if (symbol.id == "BACK_BUTTON") {
                                BackCard(
                                    onClick = { viewModel.selectCategory(null) },
                                    isLandscape = false
                                )
                            } else {
                                SymbolCard(
                                    symbol = symbol,
                                    onClick = { viewModel.addWord(symbol) },
                                    isLandscape = false
                                )
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
    val folderIconColor = Color(0xFFFFA000) // Beautiful Warm Amber/Gold folder color

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
