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
    val snackbarHostState = remember { SnackbarHostState() }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var displayName by remember { mutableStateOf("") }
    var dateOfBirth by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(AccountGender.UNSPECIFIED) }

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

private val AccountGender.label: String
    get() = when (this) {
        AccountGender.UNSPECIFIED -> "Chưa chọn"
        AccountGender.MALE -> "Nam"
        AccountGender.FEMALE -> "Nữ"
        AccountGender.OTHER -> "Khác"
    }
