package me.june8th.speakez.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import me.june8th.speakez.notification.EmergencyAlertNotifier
import me.june8th.speakez.ui.alerts.EmergencyAlertViewModel
import me.june8th.speakez.ui.navigation.SpeakEZNavHost

@Composable
fun SpeakEZApp(
    modifier: Modifier = Modifier,
    alertViewModel: EmergencyAlertViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val alerts by alertViewModel.unreadAlerts.collectAsStateWithLifecycle()
    val notifiedAlertIds = androidx.compose.runtime.remember { mutableStateSetOf<String>() }
    val context = LocalContext.current
    val notifier = androidx.compose.runtime.remember(context) {
        EmergencyAlertNotifier(context.applicationContext)
    }

    LaunchedEffect(alerts) {
        alerts.forEach { alert ->
            if (notifiedAlertIds.add(alert.id)) {
                notifier.show(alert)
            }
        }
    }

    SpeakEZNavHost(navController = navController, modifier = modifier)
}
