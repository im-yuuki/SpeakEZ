package me.june8th.speakez.domain.model

data class EmergencyAlert(
    val id: String,
    val userUid: String,
    val userDisplayName: String,
    val guardianUid: String,
    val phraseText: String,
    val type: EmergencyAlertType,
    val actionPayload: String?,
    val createdAtMillis: Long,
    val readAtMillis: Long?,
)
