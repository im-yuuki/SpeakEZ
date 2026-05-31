package me.june8th.speakez.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import me.june8th.speakez.MainActivity
import me.june8th.speakez.R
import me.june8th.speakez.domain.model.EmergencyAlert
import me.june8th.speakez.domain.model.EmergencyAlertType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmergencyAlertNotifier @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannels(
                listOf(
                    NotificationChannel(
                        ALERT_CHANNEL_ID,
                        "Cảnh báo khẩn cấp",
                        NotificationManager.IMPORTANCE_HIGH,
                    ).apply {
                        description = "Cảnh báo realtime từ người dùng đã liên kết"
                        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                        enableVibration(true)
                    },
                    NotificationChannel(
                        CALL_CHANNEL_ID,
                        "Yêu cầu gọi khẩn cấp",
                        NotificationManager.IMPORTANCE_HIGH,
                    ).apply {
                        description = "Thông báo toàn màn hình khi người dùng cần gọi khẩn cấp"
                        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                        enableVibration(true)
                        setSound(
                            ringtoneUri,
                            AudioAttributes.Builder()
                                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                .build(),
                        )
                    },
                ),
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
        val isCallRequest = alert.type == EmergencyAlertType.CALL_REQUEST
        val channelId = if (isCallRequest) CALL_CHANNEL_ID else ALERT_CHANNEL_ID
        val title = if (isCallRequest) {
            "Cuộc gọi khẩn cấp từ ${alert.userDisplayName}"
        } else {
            "Cảnh báo từ ${alert.userDisplayName}"
        }
        val text = if (isCallRequest) {
            "${alert.userDisplayName} cần người giám hộ phản hồi ngay"
        } else {
            buildString {
                append(alert.phraseText)
                alert.actionPayload?.takeIf { it.isNotBlank() }?.let { append(" · ").append(it) }
            }
        }

        val notification = android.app.Notification.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(android.app.Notification.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(if (isCallRequest) android.app.Notification.CATEGORY_CALL else android.app.Notification.CATEGORY_ALARM)
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .setVisibility(android.app.Notification.VISIBILITY_PUBLIC)
            .setDefaults(if (isCallRequest) android.app.Notification.DEFAULT_VIBRATE else android.app.Notification.DEFAULT_ALL)
            .apply {
                if (isCallRequest) {
                    setSound(ringtoneUri)
                    setFullScreenIntent(pendingIntent, true)
                    setOngoing(true)
                    setTimeoutAfter(CALL_NOTIFICATION_TIMEOUT_MILLIS)
                }
            }
            .build()
            .apply {
                if (isCallRequest) {
                    flags = flags or android.app.Notification.FLAG_INSISTENT
                }
            }

        notificationManager.notify(alert.id.hashCode(), notification)
    }

    fun showGuardianAcknowledgement(alert: EmergencyAlert) {
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
            "ack_${alert.id}".hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val text = "Một người giám hộ đã nhận yêu cầu gọi khẩn cấp của bạn."

        val notification = android.app.Notification.Builder(context, ALERT_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Người giám hộ đã phản hồi")
            .setContentText(text)
            .setStyle(android.app.Notification.BigTextStyle().bigText(text))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(android.app.Notification.CATEGORY_STATUS)
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .setVisibility(android.app.Notification.VISIBILITY_PUBLIC)
            .setDefaults(android.app.Notification.DEFAULT_ALL)
            .build()

        notificationManager.notify("ack_${alert.id}".hashCode(), notification)
    }

    private companion object {
        const val ALERT_CHANNEL_ID = "emergency_alerts"
        const val CALL_CHANNEL_ID = "emergency_call_requests_v2"
        const val CALL_NOTIFICATION_TIMEOUT_MILLIS = 30_000L
    }
}
