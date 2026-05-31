package me.june8th.speakez.ui.navigation.screen

import android.app.Activity
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import me.june8th.speakez.R
import me.june8th.speakez.domain.model.AccountType
import me.june8th.speakez.ui.auth.LoginViewModel

@Composable
fun LoginScreen(
    onAuthComplete: (isGuest: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current
    val webClientId = stringResource(R.string.default_web_client_id)

    val googleSignInClient = remember(webClientId) {
        GoogleSignIn.getClient(
            context,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build(),
        )
    }
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
            viewModel.signInWithGoogleIdToken(account.idToken)
        } catch (exception: ApiException) {
            viewModel.setError("Không thể đăng nhập Google: ${exception.statusCode}")
        }
    }

    LaunchedEffect(profile) {
        profile?.let { onAuthComplete(it.isGuest) }
    }

    val content: @Composable () -> Unit = {
        LoginPanel(
            isLandscape = isLandscape,
            isSignUp = uiState.isSignUp,
            isChoosingAccountType = uiState.isChoosingAccountType,
            displayName = uiState.displayName,
            email = uiState.email,
            password = uiState.password,
            accountType = uiState.accountType,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onDisplayNameChange = viewModel::setDisplayName,
            onEmailChange = viewModel::setEmail,
            onPasswordChange = viewModel::setPassword,
            onAccountTypeChange = viewModel::setAccountType,
            onChangeAccountType = viewModel::changeAccountType,
            onToggleMode = { viewModel.setSignUp(!uiState.isSignUp) },
            onSubmit = viewModel::submitEmailPassword,
            onGoogleClick = { googleLauncher.launch(googleSignInClient.signInIntent) },
            onGuestClick = viewModel::continueAsGuest,
        )
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background,
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 36.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LoginHero(modifier = Modifier.weight(0.9f))
                Column(
                    modifier = Modifier
                        .weight(1.1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    content()
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                LoginHero(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(24.dp))
                content()
            }
        }
    }
}

@Composable
private fun LoginHero(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "SpeakEZ",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Đăng nhập để đồng bộ hồ sơ, hoặc tiếp tục offline nếu chỉ cần dùng trên thiết bị này.",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoginPanel(
    isLandscape: Boolean,
    isSignUp: Boolean,
    isChoosingAccountType: Boolean,
    displayName: String,
    email: String,
    password: String,
    accountType: AccountType,
    isLoading: Boolean,
    errorMessage: String?,
    onDisplayNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onAccountTypeChange: (AccountType) -> Unit,
    onChangeAccountType: () -> Unit,
    onToggleMode: () -> Unit,
    onSubmit: () -> Unit,
    onGoogleClick: () -> Unit,
    onGuestClick: () -> Unit,
) {
    Card(
        modifier = Modifier.widthIn(max = if (isLandscape) 560.dp else 520.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (isSignUp && isChoosingAccountType) {
                AccountTypeChoicePage(
                    onAccountTypeChange = onAccountTypeChange,
                    onToggleMode = onToggleMode,
                )
                return@Column
            }

            Text(
                text = if (isSignUp) "Tạo tài khoản" else "Đăng nhập",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (isSignUp) {
                    "Chọn loại tài khoản cho hồ sơ mới."
                } else {
                    "Đăng nhập bằng tài khoản đã có hoặc tiếp tục dùng offline."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (isSignUp) {
                SelectedAccountTypeSummary(
                    accountType = accountType,
                    onChangeAccountType = onChangeAccountType,
                )
                OutlinedTextField(
                    value = displayName,
                    onValueChange = onDisplayNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Tên hồ sơ") },
                    singleLine = true,
                )
            }
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Mật khẩu") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
            )
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Button(
                onClick = onSubmit,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSignUp) "Đăng ký" else "Đăng nhập")
            }
            OutlinedButton(
                onClick = onGoogleClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_google),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isSignUp) "Đăng ký với Google" else "Tiếp tục với Google")
            }
            TextButton(
                onClick = onToggleMode,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isSignUp) "Đã có tài khoản? Đăng nhập" else "Chưa có tài khoản? Đăng ký")
            }
            TextButton(
                onClick = onGuestClick,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Dùng không cần tài khoản (offline)")
            }
            Text(
                text = "Chế độ offline chỉ lưu dữ liệu trên thiết bị này.",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AccountTypeChoicePage(
    onAccountTypeChange: (AccountType) -> Unit,
    onToggleMode: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Bạn muốn tạo tài khoản nào?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Chọn đúng vai trò để SpeakEZ bật các tính năng phù hợp ngay từ đầu.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AccountTypeChoiceCard(
            icon = Icons.Filled.AccountCircle,
            title = AccountType.USER.label,
            description = "Tạo câu nhanh, dùng giọng đọc và gửi cảnh báo khẩn cấp cho người giám hộ.",
            onClick = { onAccountTypeChange(AccountType.USER) },
        )
        AccountTypeChoiceCard(
            icon = Icons.Filled.VerifiedUser,
            title = AccountType.GUARDIAN.label,
            description = "Nhận lời mời giám hộ, theo dõi người dùng đã liên kết và nhận cảnh báo realtime.",
            onClick = { onAccountTypeChange(AccountType.GUARDIAN) },
        )
        TextButton(
            onClick = onToggleMode,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Đã có tài khoản? Đăng nhập")
        }
    }
}

@Composable
private fun AccountTypeChoiceCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp),
                    tint = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun SelectedAccountTypeSummary(
    accountType: AccountType,
    onChangeAccountType: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (accountType == AccountType.USER) Icons.Filled.AccountCircle else Icons.Filled.VerifiedUser,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Loại tài khoản",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Text(
                    text = accountType.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            TextButton(onClick = onChangeAccountType) {
                Text("Đổi")
            }
        }
    }
}

private val AccountType.label: String
    get() = when (this) {
        AccountType.USER -> "Người dùng"
        AccountType.GUARDIAN -> "Người giám hộ"
    }
