package me.june8th.speakez.ui

import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateSetOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.june8th.speakez.domain.model.EmergencyAlert
import me.june8th.speakez.domain.model.EmergencyAlertType
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
    val acknowledgedCallRequests by alertViewModel.acknowledgedCallRequests.collectAsStateWithLifecycle()
    val notifiedAlertIds = androidx.compose.runtime.remember { mutableStateSetOf<String>() }
    val acknowledgedCallNotifiedIds = androidx.compose.runtime.remember { mutableStateSetOf<String>() }
    val acknowledgedAlertIds = androidx.compose.runtime.remember { mutableStateSetOf<String>() }
    val context = LocalContext.current
    val notifier = androidx.compose.runtime.remember(context) {
        EmergencyAlertNotifier(context.applicationContext)
    }

    LaunchedEffect(alerts) {
        alerts.forEach { alert ->
            if (notifiedAlertIds.add(alert.id)) {
                if (alert.type != EmergencyAlertType.CALL_REQUEST) {
                    notifier.show(alert)
                }
            }
        }
    }

    LaunchedEffect(acknowledgedCallRequests) {
        acknowledgedCallRequests.forEach { alert ->
            if (acknowledgedCallNotifiedIds.add(alert.id)) {
                notifier.showGuardianAcknowledgement(alert)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        SpeakEZNavHost(navController = navController)
        alerts
            .filter { it.type == EmergencyAlertType.CALL_REQUEST && it.id !in acknowledgedAlertIds }
            .maxByOrNull { it.createdAtMillis }
            ?.let { alert ->
                CallRequestFullScreenAlert(
                    alert = alert,
                    onConfirm = {
                        acknowledgedAlertIds.add(alert.id)
                        alertViewModel.markAlertRead(alert.id)
                    },
                )
            }
    }
}

@Composable
private fun CallRequestFullScreenAlert(
    alert: EmergencyAlert,
    onConfirm: () -> Unit,
) {
    LoopingSystemRingtone(alertId = alert.id)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.error,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.error)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Surface(
                    modifier = Modifier.size(96.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    color = Color.White.copy(alpha = 0.18f),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = null,
                        modifier = Modifier.padding(24.dp),
                        tint = MaterialTheme.colorScheme.onError,
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Cuộc gọi khẩn cấp",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onError,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "${alert.userDisplayName} cần người giám hộ phản hồi ngay.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onError,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                ) {
                    Text(
                        text = "Tôi đã nhận",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun LoopingSystemRingtone(alertId: String) {
    val context = LocalContext.current
    DisposableEffect(alertId, context) {
        val ringtone = runCatching {
            RingtoneManager.getRingtone(
                context.applicationContext,
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE),
            )?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                isLooping = true
                play()
            }
        }.getOrNull()

        onDispose {
            runCatching { ringtone?.stop() }
        }
    }
}
