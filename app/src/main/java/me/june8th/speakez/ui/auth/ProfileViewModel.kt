package me.june8th.speakez.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.june8th.speakez.domain.model.AccountGender
import me.june8th.speakez.domain.repository.AuthRepository
import me.june8th.speakez.domain.repository.GuardianRepository
import me.june8th.speakez.ui.common.toUserMessage
import javax.inject.Inject

data class ProfileActionState(
    val isSaving: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val guardianRepository: GuardianRepository,
) : ViewModel() {
    val profileState = authRepository.profileState
    val guardianConnections = guardianRepository.observeGuardianConnections()
    val dependentConnections = guardianRepository.observeDependentConnections()
    val outgoingInvitations = guardianRepository.observeOutgoingInvitations()
    val incomingInvitations = guardianRepository.observeIncomingInvitations()
    val unreadEmergencyAlerts = guardianRepository.observeUnreadEmergencyAlerts()

    private val _actionState = MutableStateFlow(ProfileActionState())
    val actionState: StateFlow<ProfileActionState> = _actionState.asStateFlow()

    fun saveProfile(displayName: String, dateOfBirth: String, gender: AccountGender) {
        viewModelScope.launch {
            _actionState.update { it.copy(isSaving = true, message = null) }
            runCatching { authRepository.saveProfile(displayName, dateOfBirth, gender) }
                .onSuccess { _actionState.update { it.copy(isSaving = false, message = "Đã cập nhật hồ sơ") } }
                .onFailure { throwable ->
                    _actionState.update {
                        it.copy(
                            isSaving = false,
                            message = throwable.toUserMessage("Không thể cập nhật hồ sơ"),
                        )
                    }
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun inviteGuardian(email: String) {
        viewModelScope.launch {
            _actionState.update { it.copy(isSaving = true, message = null) }
            runCatching { guardianRepository.inviteGuardian(email) }
                .onSuccess { _actionState.update { it.copy(isSaving = false, message = "Đã gửi lời mời giám hộ") } }
                .onFailure { throwable ->
                    _actionState.update {
                        it.copy(
                            isSaving = false,
                            message = throwable.toUserMessage("Không thể gửi lời mời"),
                        )
                    }
                }
        }
    }

    fun respondToInvitation(invitationId: String, accept: Boolean) {
        viewModelScope.launch {
            _actionState.update { it.copy(isSaving = true, message = null) }
            runCatching { guardianRepository.respondToInvitation(invitationId, accept) }
                .onSuccess {
                    _actionState.update {
                        it.copy(
                            isSaving = false,
                            message = if (accept) "Đã chấp nhận lời mời" else "Đã từ chối lời mời",
                        )
                    }
                }
                .onFailure { throwable ->
                    _actionState.update {
                        it.copy(
                            isSaving = false,
                            message = throwable.toUserMessage("Không thể phản hồi lời mời"),
                        )
                    }
                }
        }
    }

    fun unlinkGuardian(userUid: String, guardianUid: String) {
        viewModelScope.launch {
            _actionState.update { it.copy(isSaving = true, message = null) }
            runCatching { guardianRepository.unlinkGuardian(userUid, guardianUid) }
                .onSuccess { _actionState.update { it.copy(isSaving = false, message = "Đã hủy liên kết") } }
                .onFailure { throwable ->
                    _actionState.update {
                        it.copy(
                            isSaving = false,
                            message = throwable.toUserMessage("Không thể hủy liên kết"),
                        )
                    }
                }
        }
    }

    fun markAlertRead(alertId: String) {
        viewModelScope.launch {
            runCatching { guardianRepository.markAlertRead(alertId) }
                .onFailure { throwable ->
                    _actionState.update {
                        it.copy(message = throwable.toUserMessage("Không thể đánh dấu cảnh báo đã đọc"))
                    }
                }
        }
    }

    fun startLoginFromGuest() {
        authRepository.startLoginFromGuest()
    }

    fun clearMessage() {
        _actionState.update { it.copy(message = null) }
    }
}
