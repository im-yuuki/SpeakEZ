package me.june8th.speakez.ui.navigation.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.june8th.speakez.R

private data class LoginProfile(
    val name: String,
    val colors: List<Color>,
    val initials: String,
)

private val demoProfiles = listOf(
    LoginProfile("Bé Na", listOf(Color(0xFFB3E5FC), Color(0xFF1976D2)), "BN"),
    LoginProfile("Ông Nội", listOf(Color(0xFFD1C4E9), Color(0xFF673AB7)), "ON"),
    LoginProfile("Chị Lan", listOf(Color(0xFFC8E6C9), Color(0xFF2E7D32)), "CL"),
)

@Composable
fun LoginScreen(
    onAvatarSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = stringResource(R.string.login_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Start,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.login_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                )
            }
            
            LazyRow(
                modifier = Modifier.weight(1.5f),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items(demoProfiles) { profile ->
                    AvatarCard(
                        profile = profile,
                        onClick = onAvatarSelected,
                        isLandscape = true,
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.login_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.login_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(32.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                items(demoProfiles) { profile ->
                    AvatarCard(
                        profile = profile,
                        onClick = onAvatarSelected,
                        isLandscape = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun AvatarCard(
    profile: LoginProfile,
    onClick: () -> Unit,
    isLandscape: Boolean,
) {
    val cardWidth = if (isLandscape) 150.dp else 180.dp
    val cardHeight = if (isLandscape) 210.dp else 250.dp
    val avatarSize = if (isLandscape) 100.dp else 140.dp
    val initialStyle = if (isLandscape) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall
    val nameStyle = if (isLandscape) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
    val spacerHeight = if (isLandscape) 10.dp else 18.dp
    val cardPadding = if (isLandscape) 12.dp else 18.dp

    Card(
        onClick = onClick,
        modifier = Modifier.size(width = cardWidth, height = cardHeight),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(cardPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(profile.colors),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = profile.initials,
                    style = initialStyle,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.height(spacerHeight))
            Text(
                text = profile.name,
                style = nameStyle,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}


