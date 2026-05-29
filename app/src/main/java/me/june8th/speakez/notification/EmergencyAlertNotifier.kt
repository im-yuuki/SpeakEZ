package me.june8th.speakez.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import me.june8th.speakez.MainActivity
import me.june8th.speakez.R
import me.june8th.speakez.domain.model.EmergencyAlert
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyAlertNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Cảnh báo khẩn cấp",
                    NotificationManager.IMPORTANCE_HIGH,
                ).apply {
                    description = "Cảnh báo realtime từ người dùng đã liên kết"
                },
            )
        }
    }

    fun show(alert: EmergencyAlert) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            alert.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = buildString {
            append(alert.phraseText)
            alert.actionPayload?.takeIf { it.isNotBlank() }?.let { append(" · ").append(it) }
        }

        val notification = android.app.Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Cảnh báo từ ${alert.userDisplayName}")
            .setContentText(text)
            .setStyle(android.app.Notification.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(android.app.Notification.CATEGORY_ALARM)
            .build()

        notificationManager.notify(alert.id.hashCode(), notification)
    }

    private companion object {
        const val CHANNEL_ID = "emergency_alerts"
    }
}
