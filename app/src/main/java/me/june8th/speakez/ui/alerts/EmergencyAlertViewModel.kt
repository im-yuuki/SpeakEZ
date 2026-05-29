package me.june8th.speakez.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import me.june8th.speakez.domain.repository.GuardianRepository
import javax.inject.Inject

@HiltViewModel
class EmergencyAlertViewModel @Inject constructor(
    guardianRepository: GuardianRepository,
) : ViewModel() {
    val unreadAlerts = guardianRepository.observeUnreadEmergencyAlerts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )
}
