package me.june8th.speakez.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.june8th.speakez.domain.repository.AuthRepository
import javax.inject.Inject

data class ProfileActionState(
    val isSaving: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {
    val profileState = authRepository.profileState

    private val _actionState = MutableStateFlow(ProfileActionState())
    val actionState: StateFlow<ProfileActionState> = _actionState.asStateFlow()

    fun saveProfile(displayName: String) {
        viewModelScope.launch {
            _actionState.update { it.copy(isSaving = true, message = null) }
            runCatching { authRepository.saveProfile(displayName) }
                .onSuccess { _actionState.update { it.copy(isSaving = false, message = "Đã cập nhật hồ sơ") } }
                .onFailure { throwable ->
                    _actionState.update {
                        it.copy(
                            isSaving = false,
                            message = throwable.message ?: "Không thể cập nhật hồ sơ",
                        )
                    }
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun startLoginFromGuest() {
        authRepository.startLoginFromGuest()
    }

    fun clearMessage() {
        _actionState.update { it.copy(message = null) }
    }
}
