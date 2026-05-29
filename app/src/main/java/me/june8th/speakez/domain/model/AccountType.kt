package me.june8th.speakez.domain.model

enum class AccountType {
    USER,
    GUARDIAN;

    companion object {
        fun fromStored(value: String?): AccountType {
            return entries.firstOrNull { it.name == value } ?: USER
        }
    }
}
