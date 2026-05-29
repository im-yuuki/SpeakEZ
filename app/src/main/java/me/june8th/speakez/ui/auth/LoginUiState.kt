package me.june8th.speakez.ui.auth

import me.june8th.speakez.domain.model.AccountType

data class LoginUiState(
    val isSignUp: Boolean = false,
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val accountType: AccountType = AccountType.USER,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
