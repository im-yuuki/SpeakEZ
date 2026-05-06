package me.june8th.speakez

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import me.june8th.speakez.ui.theme.AppTheme
import me.june8th.speakez.ui.SpeakEZApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                SpeakEZApp()
            }
        }
    }
}
