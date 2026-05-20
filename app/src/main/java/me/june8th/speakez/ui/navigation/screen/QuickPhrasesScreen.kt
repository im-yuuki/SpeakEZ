package me.june8th.speakez.ui.navigation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import me.june8th.speakez.R
import me.june8th.speakez.ui.quick_phrases.QuickPhrasesViewModel

@Composable
fun QuickPhrasesScreen(modifier: Modifier = Modifier) {
    val viewModel: QuickPhrasesViewModel = hiltViewModel()

    // Get string resources at the Composable level
    val quickHelpText = stringResource(R.string.quick_help)
    val quickPainText = stringResource(R.string.quick_pain)
    val quickCallFamilyText = stringResource(R.string.quick_call_family)

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
                EmergencyButton(
                    text = quickHelpText,
                    icon = Icons.Filled.Warning,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    onClick = { viewModel.speakQuickPhrase(quickHelpText) },
                )
                EmergencyButton(
                    text = quickPainText,
                    icon = Icons.Filled.LocalHospital,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    onClick = { viewModel.speakQuickPhrase(quickPainText) },
                )
                EmergencyButton(
                    text = quickCallFamilyText,
                    icon = Icons.Filled.Call,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    onClick = { viewModel.speakQuickPhrase(quickCallFamilyText) },
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                EmergencyButton(
                    text = quickHelpText,
                    icon = Icons.Filled.Warning,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .heightIn(min = 140.dp),
                    onClick = { viewModel.speakQuickPhrase(quickHelpText) },
                )
                EmergencyButton(
                    text = quickPainText,
                    icon = Icons.Filled.LocalHospital,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .heightIn(min = 140.dp),
                    onClick = { viewModel.speakQuickPhrase(quickPainText) },
                )
                EmergencyButton(
                    text = quickCallFamilyText,
                    icon = Icons.Filled.Call,
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .heightIn(min = 140.dp),
                    onClick = { viewModel.speakQuickPhrase(quickCallFamilyText) },
                )
            }
        }
    }
}

@Composable
private fun EmergencyButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
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


