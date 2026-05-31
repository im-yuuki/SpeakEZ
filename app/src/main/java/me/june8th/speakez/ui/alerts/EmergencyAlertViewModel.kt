package me.june8th.speakez.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.june8th.speakez.domain.repository.GuardianRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EmergencyAlertViewModel @Inject constructor(
    private val guardianRepository: GuardianRepository,
) : ViewModel() {
    val unreadAlerts = guardianRepository.observeUnreadEmergencyAlerts()
        .catch { throwable ->
            Timber.w(throwable, "Failed to collect unread emergency alerts")
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val acknowledgedCallRequests = guardianRepository.observeAcknowledgedCallRequests()
        .catch { throwable ->
            Timber.w(throwable, "Failed to collect acknowledged call requests")
            emit(emptyList())
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun markAlertRead(alertId: String) {
        viewModelScope.launch {
            runCatching { guardianRepository.markAlertRead(alertId) }
                .onFailure { throwable ->
                    Timber.w(throwable, "Failed to mark emergency alert read")
                }
        }
    }
}
