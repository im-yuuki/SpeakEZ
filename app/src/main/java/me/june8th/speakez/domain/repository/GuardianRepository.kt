package me.june8th.speakez.domain.repository

import kotlinx.coroutines.flow.Flow
import me.june8th.speakez.domain.model.EmergencyAlert
import me.june8th.speakez.domain.model.EmergencyAlertType
import me.june8th.speakez.domain.model.GuardianConnection
import me.june8th.speakez.domain.model.GuardianInvitation

interface GuardianRepository {
    fun observeGuardianConnections(): Flow<List<GuardianConnection>>
    fun observeDependentConnections(): Flow<List<GuardianConnection>>
    fun observeOutgoingInvitations(): Flow<List<GuardianInvitation>>
    fun observeIncomingInvitations(): Flow<List<GuardianInvitation>>
    fun observeUnreadEmergencyAlerts(): Flow<List<EmergencyAlert>>
    fun observeAcknowledgedCallRequests(): Flow<List<EmergencyAlert>>

    suspend fun inviteGuardian(email: String)
    suspend fun respondToInvitation(invitationId: String, accept: Boolean)
    suspend fun unlinkGuardian(userUid: String, guardianUid: String)
    suspend fun sendEmergencyAlert(
        phraseText: String,
        actionPayload: String?,
        type: EmergencyAlertType = EmergencyAlertType.NOTIFICATION,
    ): List<String>
    suspend fun hasAnyAlertRead(alertIds: List<String>): Boolean
    suspend fun markAlertRead(alertId: String)
    suspend fun cleanupEmergencyAlerts()
}
