package me.june8th.speakez.domain.model

enum class EmergencyAlertType {
    NOTIFICATION,
    CALL_REQUEST;

    companion object {
        fun fromStored(value: String?): EmergencyAlertType {
            return entries.firstOrNull { it.name == value } ?: NOTIFICATION
        }
    }
}
