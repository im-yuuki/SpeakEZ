package me.june8th.speakez.ui.auth

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.june8th.speakez.data.auth.LocalSessionStore
import me.june8th.speakez.domain.repository.AuthRepository
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    localSessionStore: LocalSessionStore,
    authRepository: AuthRepository,
) : ViewModel() {
    val onboardingComplete = localSessionStore.isOnboardingComplete
    val profileState = authRepository.profileState
}
