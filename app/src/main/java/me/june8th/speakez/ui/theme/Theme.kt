package me.june8th.speakez.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SpeakEZBlueLight = lightColorScheme(
    primary = Color(0xFF0060B0),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD4E3FF),
    onPrimaryContainer = Color(0xFF001A38),
    secondary = Color(0xFF006A6A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF9DEEEE),
    onSecondaryContainer = Color(0xFF003737),
    tertiary = Color(0xFF665983),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE3D4FF),
    onTertiaryContainer = Color(0xFF20143A),
    background = Color(0xFFF9FBFF),
    onBackground = Color(0xFF1A1C1E),
    surface = Color(0xFFF9FBFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE2E8F2),
    onSurfaceVariant = Color(0xFF414751),
    outline = Color(0xFF727783),
)

private val SpeakEZBlueDark = darkColorScheme(
    primary = Color(0xFFA5C8FF),
    onPrimary = Color(0xFF001C3A),
    primaryContainer = Color(0xFF004887),
    onPrimaryContainer = Color(0xFFD4E3FF),
    secondary = Color(0xFF84D4D3),
    onSecondary = Color(0xFF002020),
    secondaryContainer = Color(0xFF004F4F),
    onSecondaryContainer = Color(0xFF9DEEEE),
    tertiary = Color(0xFFCFBFEF),
    onTertiary = Color(0xFF20143A),
    tertiaryContainer = Color(0xFF4C4068),
    onTertiaryContainer = Color(0xFFE3D4FF),
    background = Color(0xFF101417),
    onBackground = Color(0xFFE2E8F2),
    surface = Color(0xFF101417),
    onSurface = Color(0xFFE2E8F2),
    surfaceVariant = Color(0xFF414751),
    onSurfaceVariant = Color(0xFFC1C6D3),
    outline = Color(0xFF8C929E),
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) SpeakEZBlueDark else SpeakEZBlueLight

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}