package me.june8th.speakez.domain.model

data class AccountProfile(
    val uid: String?,
    val email: String?,
    val displayName: String,
    val dateOfBirth: String,
    val gender: AccountGender,
    val accountType: AccountType,
    val isGuest: Boolean,
)
