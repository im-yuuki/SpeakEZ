package me.june8th.speakez.ui.navigation.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.june8th.speakez.R
import me.june8th.speakez.domain.model.VocabularyItem
import me.june8th.speakez.ui.settings.SettingsViewModel

private val defaultEmojis = listOf("🍚", "💊", "⚽", "😊", "🖐️")

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val saveSuccessMessage = androidx.compose.ui.res.stringResource(R.string.settings_save_success)

    val volume by viewModel.volume.collectAsState()
    val speed by viewModel.speechRate.collectAsState()
    val pitch by viewModel.pitch.collectAsState()
    val enableHints by viewModel.showLabels.collectAsState()
    val vocabulary by viewModel.vocabularyItems.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var selectedImageItemId by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        val targetId = selectedImageItemId
        if (uri != null && targetId != null) {
            viewModel.updateVocabularyImage(targetId, uri.toString())
        }
        selectedImageItemId = null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.saveSettings()
                    coroutineScope.launch { snackbarHostState.showSnackbar(saveSuccessMessage) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(text = androidx.compose.ui.res.stringResource(R.string.settings_save))
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SettingCard(
                    title = "Cài đặt Phát âm",
                    subtitle = "Điều chỉnh tốc độ và cao độ giọng đọc",
                    icon = Icons.Filled.Speaker,
                ) {
                    Text(text = "Tốc độ đọc: ${"%.2f".format(speed)}")
                    Slider(value = speed, onValueChange = viewModel::setSpeechRate, valueRange = 0.5f..2.0f)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Cao độ: ${"%.2f".format(pitch)}")
                    Slider(value = pitch, onValueChange = viewModel::setPitch, valueRange = 0.5f..2.0f)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.testAudio() }) { Text("Nghe thử") }
                }
            }
            item {
                SettingCard(
                    title = androidx.compose.ui.res.stringResource(R.string.settings_tts_title),
                    subtitle = androidx.compose.ui.res.stringResource(R.string.settings_tts_subtitle),
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                ) {
                    Slider(value = volume, onValueChange = { viewModel.setVolume(it) })
                }
            }
            item {
                SettingCard(
                    title = androidx.compose.ui.res.stringResource(R.string.settings_icon_management_title),
                    subtitle = androidx.compose.ui.res.stringResource(R.string.settings_icon_management_subtitle),
                    icon = Icons.Filled.Palette,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = androidx.compose.ui.res.stringResource(R.string.settings_show_labels))
                        Switch(checked = enableHints, onCheckedChange = { viewModel.setShowLabels(it) })
                    }
                }
            }
            item {
                SettingCard(title = "Quản lý Từ vựng", subtitle = "Thêm/ẩn hiện/đổi ảnh từ vựng", icon = Icons.Filled.Add) {
                    Button(onClick = { showAddDialog = true }) { Text("Thêm từ mới") }
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        vocabulary.forEach { item ->
                            VocabularyRow(
                                item = item,
                                onToggleVisibility = { viewModel.toggleVocabularyVisibility(item.id) },
                                onPickImage = {
                                    selectedImageItemId = item.id
                                    imagePicker.launch("image/*")
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddVocabularyDialog(
            onDismiss = { showAddDialog = false },
            onSubmit = { label, emoji ->
                viewModel.addVocabulary(label, emoji)
                showAddDialog = false
            },
        )
    }
}

@Composable
private fun VocabularyRow(
    item: VocabularyItem,
    onToggleVisibility: () -> Unit,
    onPickImage: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = item.title, modifier = Modifier.weight(1f))
        TextButton(onClick = onPickImage) { Text("Đổi ảnh") }
        TextButton(onClick = onToggleVisibility) { Text(if (item.isVisible) "Hide" else "Show") }
    }
}

@Composable
private fun AddVocabularyDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit,
) {
    var label by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(defaultEmojis.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm từ mới") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Nhãn từ vựng") },
                )
                Text("Emoji")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    defaultEmojis.forEach { candidate ->
                        TextButton(onClick = { emoji = candidate }) {
                            Text(if (emoji == candidate) "[$candidate]" else candidate)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(label, emoji) }, enabled = label.isNotBlank()) {
                Text("Thêm")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } },
    )
}

@Composable
private fun SettingCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(imageVector = icon, contentDescription = null)
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
