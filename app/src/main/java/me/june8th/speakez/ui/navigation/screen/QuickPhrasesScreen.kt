package me.june8th.speakez.ui.navigation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.june8th.speakez.domain.model.ActionType
import me.june8th.speakez.domain.model.QuickPhrase
import me.june8th.speakez.ui.quick_phrases.QuickPhraseIntent
import me.june8th.speakez.ui.quick_phrases.QuickPhraseUiState
import me.june8th.speakez.ui.quick_phrases.QuickPhrasesViewModel

@Composable
fun QuickPhrasesScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: QuickPhrasesViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isLandscape) {
            // Top Bar in landscape - Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onBackClick,
                    modifier = Modifier.height(56.dp).widthIn(min = 104.dp),
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Quay lại",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            QuickPhraseLandscapeContent(
                uiState = uiState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                onPhraseClick = { phrase ->
                    viewModel.onIntent(QuickPhraseIntent.OnPhraseClicked(phrase))
                },
            )
        } else {
            QuickPhrasePortraitContent(
                uiState = uiState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                onPhraseClick = { phrase ->
                    viewModel.onIntent(QuickPhraseIntent.OnPhraseClicked(phrase))
                },
            )
        }
    }
}

@Composable
private fun QuickPhrasePortraitContent(
    uiState: QuickPhraseUiState,
    onPhraseClick: (QuickPhrase) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> StatusText(text = "Đang tải câu nhanh", modifier = modifier)
        uiState.errorMessage != null -> StatusText(text = uiState.errorMessage, modifier = modifier)
        uiState.phrases.isEmpty() -> StatusText(text = "Chưa có câu nhanh", modifier = modifier)
        else -> {
            LazyColumn(
                modifier = modifier,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(uiState.phrases, key = QuickPhrase::id) { phrase ->
                    EmergencyButton(
                        text = phrase.text,
                        icon = phrase.actionIcon(),
                        containerColor = phrase.containerColor(),
                        contentColor = phrase.contentColor(),
                        enabled = phrase.actionType != ActionType.CALL || uiState.activeCallPhraseId == null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 140.dp),
                        onClick = { onPhraseClick(phrase) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickPhraseLandscapeContent(
    uiState: QuickPhraseUiState,
    onPhraseClick: (QuickPhrase) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> StatusText(text = "Đang tải câu nhanh", modifier = modifier)
        uiState.errorMessage != null -> StatusText(text = uiState.errorMessage, modifier = modifier)
        uiState.phrases.isEmpty() -> StatusText(text = "Chưa có câu nhanh", modifier = modifier)
        else -> {
            LazyRow(
                modifier = modifier,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                items(uiState.phrases, key = QuickPhrase::id) { phrase ->
                    EmergencyButton(
                        text = phrase.text,
                        icon = phrase.actionIcon(),
                        containerColor = phrase.containerColor(),
                        contentColor = phrase.contentColor(),
                        enabled = phrase.actionType != ActionType.CALL || uiState.activeCallPhraseId == null,
                        modifier = Modifier
                            .width(260.dp)
                            .fillMaxHeight(),
                        onClick = { onPhraseClick(phrase) },
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmergencyButton(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = text,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun QuickPhrase.actionIcon(): ImageVector {
    return when (actionType) {
        ActionType.CALL -> Icons.Filled.Call
        ActionType.PUSH_NOTI -> Icons.Filled.Notifications
        ActionType.NONE -> Icons.Filled.Warning
    }
}

@Composable
private fun QuickPhrase.containerColor(): Color {
    return when (actionType) {
        ActionType.CALL -> MaterialTheme.colorScheme.tertiaryContainer
        ActionType.PUSH_NOTI -> MaterialTheme.colorScheme.errorContainer
        ActionType.NONE -> MaterialTheme.colorScheme.secondaryContainer
    }
}

@Composable
private fun QuickPhrase.contentColor(): Color {
    return when (actionType) {
        ActionType.CALL -> MaterialTheme.colorScheme.onTertiaryContainer
        ActionType.PUSH_NOTI -> MaterialTheme.colorScheme.onErrorContainer
        ActionType.NONE -> MaterialTheme.colorScheme.onSecondaryContainer
    }
}
