package me.june8th.speakez.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import me.june8th.speakez.ui.navigation.SpeakEZNavHost

@Composable
fun SpeakEZApp(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    SpeakEZNavHost(navController = navController, modifier = modifier)
}

