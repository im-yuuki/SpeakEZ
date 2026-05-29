package me.june8th.speakez.domain.model

enum class AccountGender {
    UNSPECIFIED,
    MALE,
    FEMALE,
    OTHER;

    companion object {
        fun fromStored(value: String?): AccountGender {
            return entries.firstOrNull { it.name == value } ?: UNSPECIFIED
        }
    }
}
