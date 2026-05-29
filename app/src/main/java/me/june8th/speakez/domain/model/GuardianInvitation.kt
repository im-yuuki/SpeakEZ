package me.june8th.speakez.domain.model

data class GuardianInvitation(
    val id: String,
    val userUid: String,
    val userDisplayName: String,
    val guardianUid: String,
    val guardianDisplayName: String,
    val guardianEmail: String?,
    val status: GuardianInvitationStatus,
)

enum class GuardianInvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED;

    companion object {
        fun fromStored(value: String?): GuardianInvitationStatus {
            return entries.firstOrNull { it.name == value } ?: PENDING
        }
    }
}
