package me.june8th.speakez

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import me.june8th.speakez.data.settings.AppSettingsRepository
import me.june8th.speakez.data.settings.DEFAULT_FONT_SCALE
import me.june8th.speakez.ui.theme.AppTheme
import me.june8th.speakez.ui.SpeakEZApp
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appSettingsRepository: AppSettingsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()

        // Disable multi-touch event splitting globally
        (window.decorView as? android.view.ViewGroup)?.isMotionEventSplittingEnabled = false

        setContent {
            val fontScale by appSettingsRepository.fontScale.collectAsStateWithLifecycle(
                initialValue = DEFAULT_FONT_SCALE,
            )
            val currentDensity = LocalDensity.current
            val scaledDensity = remember(currentDensity, fontScale) {
                Density(
                    density = currentDensity.density,
                    fontScale = currentDensity.fontScale * fontScale,
                )
            }

            CompositionLocalProvider(LocalDensity provides scaledDensity) {
                AppTheme {
                    SpeakEZApp()
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }
}
