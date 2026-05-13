package me.june8th.speakez.ui.navigation.screen

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.june8th.speakez.R
import me.june8th.speakez.ui.settings.SettingsViewModel

@Composable
fun SettingsScreen(modifier: Modifier = Modifier) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val saveSuccessMessage = stringResource(R.string.settings_save_success)
    val volume by viewModel.volume.collectAsState()
    val speed by viewModel.speechRate.collectAsState()
    val enableHints by viewModel.showLabels.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.saveSettings()
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(saveSuccessMessage)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            ) {
                Text(text = stringResource(R.string.settings_save))
            }
        },
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingCard(
                            title = stringResource(R.string.settings_tts_title),
                            subtitle = stringResource(R.string.settings_tts_subtitle),
                            icon = Icons.Filled.Speaker,
                        ) {
                            Slider(value = volume, onValueChange = { viewModel.setVolume(it) })
                        }
                        SettingCard(
                            title = stringResource(R.string.settings_voice_speed_title),
                            subtitle = stringResource(R.string.settings_voice_speed_subtitle),
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                        ) {
                            Slider(value = speed, onValueChange = { viewModel.setSpeechRate(it) })
                        }
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SettingCard(
                            title = stringResource(R.string.settings_icon_management_title),
                            subtitle = stringResource(R.string.settings_icon_management_subtitle),
                            icon = Icons.Filled.Palette,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = stringResource(R.string.settings_show_labels))
                                Switch(checked = enableHints, onCheckedChange = { viewModel.setShowLabels(it) })
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SettingCard(
                        title = stringResource(R.string.settings_tts_title),
                        subtitle = stringResource(R.string.settings_tts_subtitle),
                        icon = Icons.Filled.Speaker,
                    ) {
                        Slider(value = volume, onValueChange = { viewModel.setVolume(it) })
                    }
                    SettingCard(
                        title = stringResource(R.string.settings_voice_speed_title),
                        subtitle = stringResource(R.string.settings_voice_speed_subtitle),
                        icon = Icons.AutoMirrored.Filled.VolumeUp,
                    ) {
                        Slider(value = speed, onValueChange = { viewModel.setSpeechRate(it) })
                    }
                    SettingCard(
                        title = stringResource(R.string.settings_icon_management_title),
                        subtitle = stringResource(R.string.settings_icon_management_subtitle),
                        icon = Icons.Filled.Palette,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = stringResource(R.string.settings_show_labels))
                            Switch(checked = enableHints, onCheckedChange = { viewModel.setShowLabels(it) })
                        }
                    }
                }
            }
        }
    }
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


