package me.june8th.speakez.ui.navigation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.june8th.speakez.R
import me.june8th.speakez.ui.home.HomeViewModel

// Helper to parse hex color strings like "0xFFDDF7F4" into Compose Color
private fun parseHexToColor(hex: String): Color {
    val cleaned = hex.removePrefix("0x").removePrefix("#")
    // android.graphics.Color.parseColor expects a leading '#', supports ARGB (#AARRGGBB)
    val intColor = android.graphics.Color.parseColor("#${cleaned}")
    return Color(intColor)
}

private data class DemoCategory(
    val title: String,
    val iconTint: Color,
    val containerColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)

private val demoCategories = listOf(
    DemoCategory("Ăn uống", Color(0xFF0B7A75), Color(0xFFDDF7F4), Icons.Filled.Restaurant),
    DemoCategory("Y tế", Color(0xFFB54708), Color(0xFFFFE8D6), Icons.Filled.LocalHospital),
    DemoCategory("Hoạt động", Color(0xFF2F5AA8), Color(0xFFDDE8FF), Icons.Filled.SportsSoccer),
    DemoCategory("Cảm xúc", Color(0xFF8E3B9E), Color(0xFFF3E0F8), Icons.Filled.Favorite),
    DemoCategory("Cơ thể", Color(0xFF7A5B00), Color(0xFFFFF0C2), Icons.Filled.Mood),
)

/**
 * Get icon for category name
 */
private fun getCategoryIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (category) {
        "Food" -> Icons.Filled.Restaurant
        "Health" -> Icons.Filled.LocalHospital
        "Activity" -> Icons.Filled.SportsSoccer
        "Emotion" -> Icons.Filled.Favorite
        "Body" -> Icons.Filled.Mood
        else -> Icons.Filled.Restaurant
    }
}

/**
 * Get icon for vocabulary item category
 */
private fun getItemIcon(category: String): androidx.compose.ui.graphics.vector.ImageVector {
    return getCategoryIcon(category)
}

@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val viewModel: HomeViewModel = hiltViewModel()
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(0.42f),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SentenceBar(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    CategoryRow(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                IconGrid(
                    viewModel = viewModel,
                    modifier = Modifier.weight(0.58f),
                    columns = 3,
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SentenceBar(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth(),
                )
                CategoryRow(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxWidth()
                )
                IconGrid(
                    viewModel = viewModel,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    columns = 2,
                )
            }
        }
    }
}

@Composable
private fun SentenceBar(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val sentenceWords = viewModel.sentenceWords.collectAsState()
    val displayText = if (sentenceWords.value.isEmpty()) {
        stringResource(R.string.sentence_placeholder)
    } else {
        sentenceWords.value.joinToString(" ")
    }

    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Auto-scroll text when overflow
            LazyRow(
                modifier = Modifier
                    .weight(1f),
                content = {
                    item {
                        Text(
                            text = displayText,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
            IconButton(onClick = { viewModel.removeLastWord() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.delete_last_word),
                )
            }
            IconButton(onClick = { viewModel.speakSentence() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = stringResource(R.string.speak_sentence),
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val categories = viewModel.categories.collectAsState()
    val selectedCategory = viewModel.selectedCategory.collectAsState()

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.categories_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            lazyRowItems(categories.value) { category ->
                val isSelected = selectedCategory.value == category.category
                    Card(
                    modifier = Modifier.size(width = 128.dp, height = 88.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) {
                            parseHexToColor(category.containerColorHex)
                        } else {
                            parseHexToColor(category.containerColorHex).copy(alpha = 0.6f)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = if (isSelected) 4.dp else 2.dp
                    ),
                    onClick = {
                        viewModel.selectCategory(
                            if (isSelected) null else category.category
                        )
                    }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(category.category),
                            contentDescription = null,
                            tint = parseHexToColor(category.iconColorHex),
                        )
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IconGrid(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    columns: Int,
) {
    val gridColumns = if (columns < 1) 1 else columns
    val filteredVocabulary = viewModel.filteredVocabulary.collectAsState()

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.vocabulary_grid_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (filteredVocabulary.value.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.empty_vocabulary),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(filteredVocabulary.value) { item ->
                    Surface(
                        color = parseHexToColor(item.containerColorHex),
                        shape = MaterialTheme.shapes.extraLarge,
                        tonalElevation = 2.dp,
                        modifier = Modifier.fillMaxSize(),
                        onClick = { viewModel.addWord(item.title) },
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Icon(
                                imageVector = getItemIcon(item.category),
                                contentDescription = null,
                                tint = parseHexToColor(item.iconColorHex),
                                modifier = Modifier.size(36.dp),
                            )
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = stringResource(R.string.tap_to_speak),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}







