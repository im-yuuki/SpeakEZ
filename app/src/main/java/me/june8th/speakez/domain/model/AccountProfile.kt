package me.june8th.speakez.domain.model

data class AccountProfile(
    val uid: String?,
    val email: String?,
    val displayName: String,
    val accountType: AccountType,
    val isGuest: Boolean,
)
