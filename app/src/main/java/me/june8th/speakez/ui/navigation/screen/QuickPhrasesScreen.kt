package me.june8th.speakez.ui.navigation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import me.june8th.speakez.R

@Composable
fun QuickPhrasesScreen(modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                EmergencyButton(
                    text = stringResource(R.string.quick_help),
                    icon = Icons.Filled.Warning,
                    containerColor = Color(0xFFD32F2F),
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    onClick = { },
                )
                EmergencyButton(
                    text = stringResource(R.string.quick_pain),
                    icon = Icons.Filled.LocalHospital,
                    containerColor = Color(0xFFF57C00),
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    onClick = { },
                )
                EmergencyButton(
                    text = stringResource(R.string.quick_call_family),
                    icon = Icons.Filled.Call,
                    containerColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f).fillMaxSize(),
                    onClick = { },
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                EmergencyButton(
                    text = stringResource(R.string.quick_help),
                    icon = Icons.Filled.Warning,
                    containerColor = Color(0xFFD32F2F),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .heightIn(min = 140.dp),
                    onClick = { },
                )
                EmergencyButton(
                    text = stringResource(R.string.quick_pain),
                    icon = Icons.Filled.LocalHospital,
                    containerColor = Color(0xFFF57C00),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .heightIn(min = 140.dp),
                    onClick = { },
                )
                EmergencyButton(
                    text = stringResource(R.string.quick_call_family),
                    icon = Icons.Filled.Call,
                    containerColor = Color(0xFF2E7D32),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .heightIn(min = 140.dp),
                    onClick = { },
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
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = containerColor),
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


