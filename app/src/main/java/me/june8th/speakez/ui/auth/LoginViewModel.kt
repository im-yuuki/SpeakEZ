package me.june8th.speakez.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.june8th.speakez.domain.model.AccountType
import me.june8th.speakez.domain.repository.AuthRepository
import me.june8th.speakez.ui.common.toUserMessage
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    val profileState = authRepository.profileState

    fun setSignUp(isSignUp: Boolean) {
        _uiState.update {
            it.copy(
                isSignUp = isSignUp,
                isChoosingAccountType = isSignUp,
                errorMessage = null,
            )
        }
    }

    fun setDisplayName(value: String) {
        _uiState.update { it.copy(displayName = value) }
    }

    fun setEmail(value: String) {
        _uiState.update { it.copy(email = value.trim()) }
    }

    fun setPassword(value: String) {
        _uiState.update { it.copy(password = value) }
    }

    fun setAccountType(accountType: AccountType) {
        _uiState.update { it.copy(accountType = accountType, isChoosingAccountType = false, errorMessage = null) }
    }

    fun changeAccountType() {
        _uiState.update { it.copy(isChoosingAccountType = true, errorMessage = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun setError(message: String) {
        _uiState.update { it.copy(errorMessage = message, isLoading = false) }
    }

    fun submitEmailPassword() {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập email và mật khẩu") }
            return
        }
        if (state.isSignUp && state.password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Mật khẩu cần ít nhất 6 ký tự") }
            return
        }

        runAuthAction {
            if (state.isSignUp) {
                authRepository.signUpWithEmail(
                    email = state.email,
                    password = state.password,
                    displayName = state.displayName,
                    accountType = state.accountType,
                )
            } else {
                authRepository.signInWithEmail(
                    email = state.email,
                    password = state.password,
                )
            }
        }
    }

    fun signInWithGoogleIdToken(idToken: String?) {
        if (idToken.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "Không lấy được mã đăng nhập Google") }
            return
        }
        val state = _uiState.value
        val accountType = if (state.isSignUp) state.accountType else null
        runAuthAction { authRepository.signInWithGoogle(idToken, accountType) }
    }

    fun continueAsGuest() {
        val state = _uiState.value
        authRepository.continueAsGuest(
            displayName = state.displayName.ifBlank { "" },
        )
    }

    private fun runAuthAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { action() }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.toUserMessage("Đăng nhập thất bại"),
                        )
                    }
                }
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                }
        }
    }
}
