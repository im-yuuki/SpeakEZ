package me.june8th.speakez.ui.navigation.screen

import android.content.res.Configuration
import android.content.Context
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.june8th.speakez.R
import me.june8th.speakez.domain.model.VocabularyItem
import me.june8th.speakez.ui.settings.SettingsViewModel

private val defaultEmojis = listOf("🍚", "💊", "⚽", "😊", "🖐️")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val context = LocalContext.current
    val sharedPrefs = remember(context) { context.getSharedPreferences("SpeakEZ_Prefs", Context.MODE_PRIVATE) }
    var gridChoice by remember { mutableStateOf(sharedPrefs.getString("grid_choice", "4x6") ?: "4x6") }
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

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (!isLandscape) {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.settings_title),
                            fontWeight = FontWeight.Bold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (!isLandscape) {
                Button(
                    onClick = {
                        sharedPrefs.edit().putString("grid_choice", gridChoice).apply()
                        viewModel.saveSettings()
                        coroutineScope.launch { snackbarHostState.showSnackbar(saveSuccessMessage) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(text = androidx.compose.ui.res.stringResource(R.string.settings_save))
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (isLandscape) {
                // Top Bar in landscape: Quay lại button (left) and Lưu button (right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Surface(
                        onClick = onBackClick,
                        modifier = Modifier.size(width = 86.dp, height = 56.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Quay lại",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Quay lại",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = androidx.compose.ui.res.stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Surface(
                        onClick = {
                            sharedPrefs.edit().putString("grid_choice", gridChoice).apply()
                            viewModel.saveSettings()
                            coroutineScope.launch { snackbarHostState.showSnackbar(saveSuccessMessage) }
                        },
                        modifier = Modifier.size(width = 86.dp, height = 56.dp),
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Save,
                                contentDescription = "Lưu",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Lưu",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Scrollable column of settings cards
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    SettingCard(
                        title = "Cài đặt Phát âm",
                        subtitle = "Điều chỉnh tốc độ và cao độ giọng đọc",
                        icon = Icons.Filled.Speaker,
                    ) {
                        Text(
                            text = "Tốc độ đọc: ${"%.2f".format(speed)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Chậm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = speed,
                                onValueChange = viewModel::setSpeechRate,
                                valueRange = 0.5f..2.0f,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Nhanh",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Cao độ: ${"%.2f".format(pitch)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Trầm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = pitch,
                                onValueChange = viewModel::setPitch,
                                valueRange = 0.5f..2.0f,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Bổng",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeDown,
                                contentDescription = "Âm lượng nhỏ",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Slider(
                                value = volume,
                                onValueChange = { viewModel.setVolume(it) },
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Âm lượng lớn",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                    SettingCard(
                        title = "Khung hiển thị",
                        subtitle = "Số lượng biểu tượng hiển thị trên một trang (chế độ ngang)",
                        icon = Icons.Filled.Palette,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("3x5", "4x6", "5x8").forEach { option ->
                                val isSelected = gridChoice == option
                                Surface(
                                    onClick = { gridChoice = option },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = MaterialTheme.shapes.medium,
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    SettingCard(
                        title = androidx.compose.ui.res.stringResource(R.string.settings_mulberry_title),
                        subtitle = androidx.compose.ui.res.stringResource(R.string.settings_mulberry_subtitle),
                        icon = Icons.Filled.Info,
                    ) {
                        Text(text = androidx.compose.ui.res.stringResource(R.string.settings_mulberry_attribution))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = androidx.compose.ui.res.stringResource(R.string.settings_mulberry_license_url),
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
                item {
                    SettingCard(
                        title = "Quản lý Từ vựng",
                        subtitle = "Thêm/ẩn hiện/đổi ảnh từ vựng",
                        icon = Icons.Filled.Add
                    ) {
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
