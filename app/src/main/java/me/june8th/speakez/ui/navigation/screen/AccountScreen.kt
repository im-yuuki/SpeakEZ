package me.june8th.speakez.ui.navigation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.june8th.speakez.domain.model.AccountGender
import me.june8th.speakez.domain.model.AccountProfile
import me.june8th.speakez.domain.model.AccountType
import me.june8th.speakez.domain.model.EmergencyAlert
import me.june8th.speakez.domain.model.EmergencyAlertType
import me.june8th.speakez.domain.model.GuardianConnection
import me.june8th.speakez.domain.model.GuardianInvitation
import me.june8th.speakez.domain.model.GuardianInvitationStatus
import me.june8th.speakez.ui.auth.ProfileViewModel
import me.june8th.speakez.ui.common.DateOfBirthField

@Composable
fun AccountScreen(
    onBackClick: () -> Unit,
    onLoginRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val guardianConnections by viewModel.guardianConnections.collectAsStateWithLifecycle(initialValue = emptyList())
    val dependentConnections by viewModel.dependentConnections.collectAsStateWithLifecycle(initialValue = emptyList())
    val outgoingInvitations by viewModel.outgoingInvitations.collectAsStateWithLifecycle(initialValue = emptyList())
    val incomingInvitations by viewModel.incomingInvitations.collectAsStateWithLifecycle(initialValue = emptyList())
    val unreadEmergencyAlerts by viewModel.unreadEmergencyAlerts.collectAsStateWithLifecycle(initialValue = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var displayName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(AccountGender.UNSPECIFIED) }
    var guardianEmail by remember { mutableStateOf("") }

    LaunchedEffect(profile?.uid, profile?.isGuest, profile?.displayName, profile?.dateOfBirth, profile?.gender) {
        profile?.let {
            displayName = it.displayName
            dateOfBirth = it.dateOfBirth
            gender = it.gender
        }
    }

    LaunchedEffect(actionState.message) {
        val message = actionState.message ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.clearMessage()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (isLandscape) {
                AccountLandscapeTopBar(onBackClick = onBackClick)
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                        )
                        Column {
                            Text(
                                text = "Hồ sơ",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "Xem và chỉnh sửa thông tin tài khoản",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    AccountProfileContent(
                        profile = profile,
                        displayName = displayName,
                        dateOfBirth = dateOfBirth,
                        gender = gender,
                        isSaving = actionState.isSaving,
                        onDisplayNameChange = { displayName = it },
                        onDateOfBirthChange = { dateOfBirth = it },
                        onGenderChange = { gender = it },
                        onSave = { viewModel.saveProfile(displayName, dateOfBirth, gender) },
                        onSignOut = {
                            viewModel.signOut()
                            onLoginRequested()
                        },
                        onLoginRequested = {
                            viewModel.startLoginFromGuest()
                            onLoginRequested()
                        },
                    )
                }
            }

            val currentProfile = profile
            if (currentProfile != null && !currentProfile.isGuest) {
                GuardianManagementCard(
                    profile = currentProfile,
                    guardianConnections = guardianConnections,
                    dependentConnections = dependentConnections,
                    outgoingInvitations = outgoingInvitations,
                    incomingInvitations = incomingInvitations,
                    unreadEmergencyAlerts = unreadEmergencyAlerts,
                    inviteEmail = guardianEmail,
                    isBusy = actionState.isSaving,
                    onInviteEmailChange = { guardianEmail = it },
                    onInvite = {
                        viewModel.inviteGuardian(guardianEmail)
                        guardianEmail = ""
                    },
                    onAcceptInvitation = { invitationId ->
                        viewModel.respondToInvitation(invitationId, accept = true)
                    },
                    onDeclineInvitation = { invitationId ->
                        viewModel.respondToInvitation(invitationId, accept = false)
                    },
                    onUnlink = { userUid, guardianUid ->
                        viewModel.unlinkGuardian(userUid, guardianUid)
                    },
                    onMarkAlertRead = { alertId ->
                        viewModel.markAlertRead(alertId)
                    },
                )
            }
        }
    }
}

@Composable
private fun AccountLandscapeTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Surface(
            onClick = onBackClick,
            modifier = Modifier.height(56.dp).widthIn(min = 104.dp),
            color = MaterialTheme.colorScheme.primary,
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Quay lại",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            text = "Tài khoản",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.width(104.dp))
    }
}

@Composable
private fun AccountProfileContent(
    profile: AccountProfile?,
    displayName: String,
    dateOfBirth: String,
    gender: AccountGender,
    isSaving: Boolean,
    onDisplayNameChange: (String) -> Unit,
    onDateOfBirthChange: (String) -> Unit,
    onGenderChange: (AccountGender) -> Unit,
    onSave: () -> Unit,
    onSignOut: () -> Unit,
    onLoginRequested: () -> Unit,
) {
    if (profile == null) {
        Text(
            text = "Chưa có hồ sơ đăng nhập.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(onClick = onLoginRequested) { Text("Đăng nhập / Đăng ký") }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (profile.isGuest) "Đang dùng offline" else "Đã đăng nhập",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = profile.email ?: "Dữ liệu chỉ lưu trên thiết bị này",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        OutlinedTextField(
            value = displayName,
            onValueChange = onDisplayNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Tên hồ sơ") },
            singleLine = true,
        )
        DateOfBirthField(
            value = dateOfBirth,
            onValueChange = onDateOfBirthChange,
            modifier = Modifier.fillMaxWidth(),
        )
        GenderDropdown(
            selected = gender,
            onSelected = onGenderChange,
        )
        AccountTypeReadOnly(accountType = profile.accountType)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onSave,
                enabled = !isSaving && displayName.isNotBlank(),
                modifier = Modifier.weight(1f),
            ) {
                Text(if (isSaving) "Đang lưu..." else "Lưu hồ sơ")
            }
            if (profile.isGuest) {
                TextButton(onClick = onLoginRequested, modifier = Modifier.weight(1f)) {
                    Text("Đăng nhập")
                }
            } else {
                TextButton(onClick = onSignOut, modifier = Modifier.weight(1f)) {
                    Text("Đăng xuất")
                }
            }
        }
    }
}

@Composable
private fun GuardianManagementCard(
    profile: AccountProfile,
    guardianConnections: List<GuardianConnection>,
    dependentConnections: List<GuardianConnection>,
    outgoingInvitations: List<GuardianInvitation>,
    incomingInvitations: List<GuardianInvitation>,
    unreadEmergencyAlerts: List<EmergencyAlert>,
    inviteEmail: String,
    isBusy: Boolean,
    onInviteEmailChange: (String) -> Unit,
    onInvite: () -> Unit,
    onAcceptInvitation: (String) -> Unit,
    onDeclineInvitation: (String) -> Unit,
    onUnlink: (userUid: String, guardianUid: String) -> Unit,
    onMarkAlertRead: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (profile.accountType == AccountType.USER) "Người giám hộ" else "Người dùng đang theo dõi",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            if (profile.accountType == AccountType.USER) {
                UserGuardianSection(
                    profile = profile,
                    guardianConnections = guardianConnections,
                    outgoingInvitations = outgoingInvitations,
                    inviteEmail = inviteEmail,
                    isBusy = isBusy,
                    onInviteEmailChange = onInviteEmailChange,
                    onInvite = onInvite,
                    onUnlink = onUnlink,
                )
            } else {
                GuardianDependentSection(
                    profile = profile,
                    dependentConnections = dependentConnections,
                    incomingInvitations = incomingInvitations,
                    unreadEmergencyAlerts = unreadEmergencyAlerts,
                    isBusy = isBusy,
                    onAcceptInvitation = onAcceptInvitation,
                    onDeclineInvitation = onDeclineInvitation,
                    onUnlink = onUnlink,
                    onMarkAlertRead = onMarkAlertRead,
                )
            }
        }
    }
}

@Composable
private fun UserGuardianSection(
    profile: AccountProfile,
    guardianConnections: List<GuardianConnection>,
    outgoingInvitations: List<GuardianInvitation>,
    inviteEmail: String,
    isBusy: Boolean,
    onInviteEmailChange: (String) -> Unit,
    onInvite: () -> Unit,
    onUnlink: (userUid: String, guardianUid: String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Đã liên kết ${guardianConnections.size}/3 người giám hộ. Cảnh báo khẩn cấp sẽ được gửi cho các tài khoản đã chấp nhận lời mời.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            value = inviteEmail,
            onValueChange = onInviteEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email người giám hộ") },
            singleLine = true,
            enabled = guardianConnections.size < 3,
        )
        Button(
            onClick = onInvite,
            enabled = !isBusy && inviteEmail.isNotBlank() && guardianConnections.size < 3,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (isBusy) "Đang xử lý..." else "Gửi lời mời")
        }

        ConnectionList(
            title = "Đang liên kết",
            emptyText = "Chưa có người giám hộ nào chấp nhận.",
            connections = guardianConnections,
            actionText = "Hủy liên kết",
            isBusy = isBusy,
            onAction = { guardian ->
                val userUid = profile.uid ?: return@ConnectionList
                onUnlink(userUid, guardian.uid)
            },
        )

        InvitationList(
            title = "Lời mời đã gửi",
            invitations = outgoingInvitations.filter { it.status == GuardianInvitationStatus.PENDING },
            emptyText = "Không có lời mời đang chờ.",
        )
    }
}

@Composable
private fun GuardianDependentSection(
    profile: AccountProfile,
    dependentConnections: List<GuardianConnection>,
    incomingInvitations: List<GuardianInvitation>,
    unreadEmergencyAlerts: List<EmergencyAlert>,
    isBusy: Boolean,
    onAcceptInvitation: (String) -> Unit,
    onDeclineInvitation: (String) -> Unit,
    onUnlink: (userUid: String, guardianUid: String) -> Unit,
    onMarkAlertRead: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        EmergencyAlertList(
            alerts = unreadEmergencyAlerts,
            isBusy = isBusy,
            onMarkAlertRead = onMarkAlertRead,
        )
        IncomingInvitationList(
            invitations = incomingInvitations,
            isBusy = isBusy,
            onAcceptInvitation = onAcceptInvitation,
            onDeclineInvitation = onDeclineInvitation,
        )
        ConnectionList(
            title = "Đang theo dõi",
            emptyText = "Chưa theo dõi người dùng nào.",
            connections = dependentConnections,
            actionText = "Dừng theo dõi",
            isBusy = isBusy,
            onAction = { user ->
                val guardianUid = profile.uid ?: return@ConnectionList
                onUnlink(user.uid, guardianUid)
            },
        )
    }
}

@Composable
private fun EmergencyAlertList(
    alerts: List<EmergencyAlert>,
    isBusy: Boolean,
    onMarkAlertRead: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Cảnh báo chưa xử lý",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (alerts.isEmpty()) {
            Text(
                text = "Chưa có cảnh báo khẩn cấp mới.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            alerts.forEach { alert ->
                val isCallRequest = alert.type == EmergencyAlertType.CALL_REQUEST
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = if (isCallRequest) {
                                "Cuộc gọi khẩn cấp từ ${alert.userDisplayName}"
                            } else {
                                "${alert.userDisplayName}: ${alert.phraseText}"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                        if (isCallRequest) {
                            Text(
                                text = "Người dùng đang chờ người giám hộ phản hồi. Bấm xác nhận để dừng fallback gọi điện thoại.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                        alert.actionPayload?.takeIf { it.isNotBlank() }?.let { payload ->
                            Text(
                                text = payload,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                        }
                        TextButton(
                            onClick = { onMarkAlertRead(alert.id) },
                            enabled = !isBusy,
                        ) {
                            Text(if (isCallRequest) "Tôi đã nhận" else "Đã xử lý")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionList(
    title: String,
    emptyText: String,
    connections: List<GuardianConnection>,
    actionText: String,
    isBusy: Boolean,
    onAction: (GuardianConnection) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (connections.isEmpty()) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            connections.forEach { connection ->
                GuardianConnectionRow(
                    connection = connection,
                    actionText = actionText,
                    enabled = !isBusy,
                    onAction = { onAction(connection) },
                )
            }
        }
    }
}

@Composable
private fun GuardianConnectionRow(
    connection: GuardianConnection,
    actionText: String,
    enabled: Boolean,
    onAction: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = connection.displayName, fontWeight = FontWeight.SemiBold)
                Text(
                    text = connection.email ?: connection.uid,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = onAction, enabled = enabled) {
                Text(actionText)
            }
        }
    }
}

@Composable
private fun InvitationList(
    title: String,
    invitations: List<GuardianInvitation>,
    emptyText: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (invitations.isEmpty()) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            invitations.forEach { invitation ->
                Text(
                    text = "${invitation.guardianDisplayName.ifBlank { invitation.guardianEmail ?: "Người giám hộ" }} · ${invitation.status.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun IncomingInvitationList(
    invitations: List<GuardianInvitation>,
    isBusy: Boolean,
    onAcceptInvitation: (String) -> Unit,
    onDeclineInvitation: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Lời mời giám hộ",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        if (invitations.isEmpty()) {
            Text(
                text = "Không có lời mời đang chờ.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            invitations.forEach { invitation ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = invitation.userDisplayName.ifBlank { "Người dùng" },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "Muốn liên kết bạn làm người giám hộ.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onAcceptInvitation(invitation.id) },
                                enabled = !isBusy,
                                modifier = Modifier.weight(1f),
                            ) {
                                Text("Chấp nhận")
                            }
                            TextButton(
                                onClick = { onDeclineInvitation(invitation.id) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderDropdown(
    selected: AccountGender,
    onSelected: (AccountGender) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selected.label,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true,
                ),
            label = { Text("Giới tính") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AccountGender.entries.forEach { gender ->
                DropdownMenuItem(
                    text = { Text(gender.label) },
                    onClick = {
                        onSelected(gender)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun AccountTypeReadOnly(accountType: AccountType) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Loại tài khoản",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = accountType.label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val AccountType.label: String
    get() = when (this) {
        AccountType.USER -> "Người dùng"
        AccountType.GUARDIAN -> "Người giám hộ"
    }

private val GuardianInvitationStatus.label: String
    get() = when (this) {
        GuardianInvitationStatus.PENDING -> "Đang chờ"
        GuardianInvitationStatus.ACCEPTED -> "Đã chấp nhận"
        GuardianInvitationStatus.DECLINED -> "Đã từ chối"
    }

private val AccountGender.label: String
    get() = when (this) {
        AccountGender.UNSPECIFIED -> "Chưa chọn"
        AccountGender.MALE -> "Nam"
        AccountGender.FEMALE -> "Nữ"
        AccountGender.OTHER -> "Khác"
    }
