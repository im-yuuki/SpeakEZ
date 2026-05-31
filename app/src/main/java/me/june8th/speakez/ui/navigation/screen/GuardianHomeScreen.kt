package me.june8th.speakez.ui.navigation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.june8th.speakez.domain.model.EmergencyAlert
import me.june8th.speakez.domain.model.EmergencyAlertType
import me.june8th.speakez.domain.model.GuardianConnection
import me.june8th.speakez.domain.model.GuardianInvitation
import me.june8th.speakez.ui.auth.ProfileViewModel

@Composable
fun GuardianHomeScreen(
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val dependents by viewModel.dependentConnections.collectAsStateWithLifecycle(initialValue = emptyList())
    val invitations by viewModel.incomingInvitations.collectAsStateWithLifecycle(initialValue = emptyList())
    val alerts by viewModel.unreadEmergencyAlerts.collectAsStateWithLifecycle(initialValue = emptyList())
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Mở menu",
                )
            }
            Text(
                text = "Xin chào, ${profile?.displayName ?: "người giám hộ"}",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        // Text(
        //     text = "Theo dõi lời mời, cảnh báo khẩn cấp và người dùng đã liên kết tại một nơi.",
        //     style = MaterialTheme.typography.bodyLarge,
        //     color = MaterialTheme.colorScheme.onSurfaceVariant,
        // )

        GuardianSummaryRow(
            alerts = alerts.size,
            invitations = invitations.size,
            dependents = dependents.size,
        )

        EmergencyAlertsCard(
            alerts = alerts,
            isBusy = actionState.isSaving,
            onMarkRead = viewModel::markAlertRead,
        )

        IncomingInvitationsCard(
            invitations = invitations,
            isBusy = actionState.isSaving,
            onAccept = { viewModel.respondToInvitation(it, accept = true) },
            onDecline = { viewModel.respondToInvitation(it, accept = false) },
        )

        DependentsCard(dependents = dependents)
    }
}

@Composable
private fun GuardianSummaryRow(
    alerts: Int,
    invitations: Int,
    dependents: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SummaryTile(
            title = "Cảnh báo",
            value = alerts.toString(),
            icon = Icons.Filled.NotificationsActive,
            modifier = Modifier.weight(1f),
        )
        SummaryTile(
            title = "Lời mời",
            value = invitations.toString(),
            icon = Icons.Filled.PersonAdd,
            modifier = Modifier.weight(1f),
        )
        SummaryTile(
            title = "Theo dõi",
            value = dependents.toString(),
            icon = Icons.Filled.People,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryTile(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(104.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun EmergencyAlertsCard(
    alerts: List<EmergencyAlert>,
    isBusy: Boolean,
    onMarkRead: (String) -> Unit,
) {
    DashboardCard(
        title = "Cảnh báo khẩn cấp",
        subtitle = if (alerts.isEmpty()) "Không có cảnh báo mới" else "${alerts.size} cảnh báo cần xử lý",
        icon = Icons.Filled.NotificationsActive,
        urgent = alerts.isNotEmpty(),
    ) {
        if (alerts.isEmpty()) {
            EmptyDashboardText("Bạn sẽ thấy cảnh báo mới tại đây khi người dùng cần hỗ trợ.")
        } else {
            alerts.forEach { alert ->
                val isCall = alert.type == EmergencyAlertType.CALL_REQUEST
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = if (isCall) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = if (isCall) "Cuộc gọi khẩn cấp" else alert.phraseText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isCall) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            text = "Từ ${alert.userDisplayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCall) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onErrorContainer,
                        )
                        Button(
                            onClick = { onMarkRead(alert.id) },
                            enabled = !isBusy,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (isCall) "Tôi đã nhận" else "Đã xử lý")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomingInvitationsCard(
    invitations: List<GuardianInvitation>,
    isBusy: Boolean,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
) {
    DashboardCard(
        title = "Lời mời giám hộ",
        subtitle = if (invitations.isEmpty()) "Không có lời mời đang chờ" else "${invitations.size} lời mời mới",
        icon = Icons.Filled.PersonAdd,
    ) {
        if (invitations.isEmpty()) {
            EmptyDashboardText("Khi người dùng mời bạn làm người giám hộ, lời mời sẽ xuất hiện tại đây.")
        } else {
            invitations.forEach { invitation ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = invitation.userDisplayName.ifBlank { "Người dùng" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Text(
                            text = "Muốn liên kết bạn làm người giám hộ.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onAccept(invitation.id) },
                                enabled = !isBusy,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Chấp nhận")
                            }
                            TextButton(
                                onClick = { onDecline(invitation.id) },
                                enabled = !isBusy,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Từ chối")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DependentsCard(dependents: List<GuardianConnection>) {
    DashboardCard(
        title = "Người dùng đang theo dõi",
        subtitle = if (dependents.isEmpty()) "Chưa liên kết người dùng nào" else "Đang theo dõi ${dependents.size} người dùng",
        icon = Icons.Filled.People,
    ) {
        if (dependents.isEmpty()) {
            EmptyDashboardText("Sau khi chấp nhận lời mời, người dùng sẽ xuất hiện trong danh sách này.")
        } else {
            dependents.forEach { dependent ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = dependent.displayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = dependent.email ?: dependent.uid,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    urgent: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (urgent) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(imageVector = icon, contentDescription = null)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            content()
        }
    }
}

@Composable
private fun EmptyDashboardText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
