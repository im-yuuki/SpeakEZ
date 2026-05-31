package me.june8th.speakez.data.guardian

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.Date
import me.june8th.speakez.domain.model.AccountType
import me.june8th.speakez.domain.model.EmergencyAlert
import me.june8th.speakez.domain.model.EmergencyAlertType
import me.june8th.speakez.domain.model.GuardianConnection
import me.june8th.speakez.domain.model.GuardianInvitation
import me.june8th.speakez.domain.model.GuardianInvitationStatus
import me.june8th.speakez.domain.repository.AuthRepository
import me.june8th.speakez.domain.repository.GuardianRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseGuardianRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    authRepository: AuthRepository,
) : GuardianRepository {
    private val profileState = authRepository.profileState

    override fun observeGuardianConnections(): Flow<List<GuardianConnection>> {
        return profileState.flatMapLatest { profile ->
            val uid = profile?.uid
            if (uid == null || profile.isGuest) {
                flowOf(emptyList())
            } else {
                combine(
                    observeConnections(uid, FIELD_GUARDIAN_IDS),
                    observeAcceptedOutgoingConnections(uid),
                ) { storedConnections, invitationConnections ->
                    (storedConnections + invitationConnections).distinctBy { it.uid }
                }
            }
        }
    }

    override fun observeDependentConnections(): Flow<List<GuardianConnection>> {
        return profileState.flatMapLatest { profile ->
            val uid = profile?.uid
            if (uid == null || profile.isGuest) {
                flowOf(emptyList())
            } else {
                combine(
                    observeConnections(uid, FIELD_DEPENDENT_IDS),
                    observeAcceptedIncomingConnections(uid, profile.email.normalizedEmailOrBlank()),
                ) { storedConnections, invitationConnections ->
                    (storedConnections + invitationConnections).distinctBy { it.uid }
                }
            }
        }
    }

    override fun observeOutgoingInvitations(): Flow<List<GuardianInvitation>> {
        return profileState.flatMapLatest { profile ->
            val uid = profile?.uid
            if (uid == null || profile.isGuest) {
                flowOf(emptyList())
            } else {
                observeInvitations(FIELD_USER_UID, uid)
            }
        }
    }

    override fun observeIncomingInvitations(): Flow<List<GuardianInvitation>> {
        return profileState.flatMapLatest { profile ->
            val uid = profile?.uid
            if (uid == null || profile.isGuest) {
                flowOf(emptyList())
            } else {
                val normalizedEmail = profile.email.normalizedEmailOrBlank()
                if (normalizedEmail.isBlank()) {
                    observeInvitations(FIELD_GUARDIAN_UID, uid, GuardianInvitationStatus.PENDING)
                } else {
                    combine(
                        observeInvitations(FIELD_GUARDIAN_UID, uid, GuardianInvitationStatus.PENDING),
                        observeInvitations(
                            FIELD_GUARDIAN_NORMALIZED_EMAIL,
                            normalizedEmail,
                            GuardianInvitationStatus.PENDING,
                        ),
                    ) { byUid, byEmail ->
                        (byUid + byEmail).distinctBy { it.id }
                    }
                }
            }
        }
    }

    override fun observeUnreadEmergencyAlerts(): Flow<List<EmergencyAlert>> {
        return profileState.flatMapLatest { profile ->
            val uid = profile?.uid
            if (uid == null || profile.isGuest || profile.accountType != AccountType.GUARDIAN) {
                flowOf(emptyList())
            } else {
                callbackFlow {
                    launch {
                        runCatching { cleanupEmergencyAlerts() }
                            .onFailure { Timber.w(it, "Failed to cleanup emergency alerts while observing") }
                    }
                    val registration = firestore.collection(COLLECTION_EMERGENCY_ALERTS)
                        .whereEqualTo(FIELD_GUARDIAN_UID, uid)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Timber.w(error, "Failed to observe unread emergency alerts")
                                trySend(emptyList())
                            } else {
                                trySend(
                                    snapshot?.documents.orEmpty()
                                        .mapNotNull { it.toEmergencyAlert() }
                                        .filter { it.readAtMillis == null }
                                        .sortedByDescending { it.createdAtMillis },
                                )
                            }
                        }
                    awaitClose { registration.remove() }
                }
            }
        }
    }

    override fun observeAcknowledgedCallRequests(): Flow<List<EmergencyAlert>> {
        return profileState.flatMapLatest { profile ->
            val uid = profile?.uid
            if (uid == null || profile.isGuest || profile.accountType != AccountType.USER) {
                flowOf(emptyList())
            } else {
                callbackFlow {
                    val registration = firestore.collection(COLLECTION_EMERGENCY_ALERTS)
                        .whereEqualTo(FIELD_USER_UID, uid)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Timber.w(error, "Failed to observe acknowledged call requests")
                                trySend(emptyList())
                            } else {
                                val recentThreshold = System.currentTimeMillis() - ACKNOWLEDGED_CALL_VISIBLE_MILLIS
                                trySend(
                                    snapshot?.documents.orEmpty()
                                        .mapNotNull { it.toEmergencyAlert() }
                                        .filter { alert ->
                                            alert.type == EmergencyAlertType.CALL_REQUEST &&
                                                alert.readAtMillis != null &&
                                                alert.readAtMillis >= recentThreshold
                                        }
                                        .sortedByDescending { it.readAtMillis },
                                )
                            }
                        }
                    awaitClose { registration.remove() }
                }
            }
        }
    }

    override suspend fun inviteGuardian(email: String) {
        val user = requireCurrentUser()
        val requester = profileState.value ?: error("Vui lòng đăng nhập để gửi lời mời")
        if (requester.accountType != AccountType.USER) error("Chỉ tài khoản người dùng mới gửi lời mời giám hộ")

        val trimmedEmail = email.trim()
        val normalizedEmail = trimmedEmail.normalizedEmail()
        if (normalizedEmail.isBlank()) error("Vui lòng nhập email người giám hộ")
        if (normalizedEmail == user.email.normalizedEmailOrBlank()) error("Không thể tự mời chính tài khoản này")

        val userSnapshot = accountDocument(user.uid).get().await()
        val currentGuardianIds = userSnapshot.getStringList(FIELD_GUARDIAN_IDS)
        if (currentGuardianIds.size >= MAX_GUARDIANS) error("Mỗi người dùng chỉ liên kết tối đa 3 người giám hộ")

        val invitationRef = firestore.collection(COLLECTION_GUARDIAN_INVITATIONS)
            .document("${user.uid}_${normalizedEmail.sha256()}")

        invitationRef.set(
            mapOf(
                FIELD_USER_UID to user.uid,
                FIELD_USER_DISPLAY_NAME to requester.displayName,
                FIELD_GUARDIAN_UID to null,
                FIELD_GUARDIAN_DISPLAY_NAME to "",
                FIELD_GUARDIAN_EMAIL to trimmedEmail,
                FIELD_GUARDIAN_NORMALIZED_EMAIL to normalizedEmail,
                FIELD_STATUS to GuardianInvitationStatus.PENDING.name,
                FIELD_CREATED_AT to FieldValue.serverTimestamp(),
                FIELD_RESPONDED_AT to null,
            ),
        ).await()
    }

    override suspend fun respondToInvitation(invitationId: String, accept: Boolean) {
        val guardian = requireCurrentUser()
        val guardianProfile = profileState.value ?: error("Vui lòng đăng nhập")
        if (guardianProfile.accountType != AccountType.GUARDIAN) error("Chỉ tài khoản người giám hộ mới phản hồi lời mời")
        val invitationRef = firestore.collection(COLLECTION_GUARDIAN_INVITATIONS).document(invitationId)

        firestore.runTransaction { transaction ->
            val invitation = transaction.get(invitationRef)
            if (!invitation.exists()) error("Lời mời không còn tồn tại")
            val invitedGuardianUid = invitation.getString(FIELD_GUARDIAN_UID).orEmpty()
            val invitedGuardianEmail = invitation.getString(FIELD_GUARDIAN_NORMALIZED_EMAIL).orEmpty()
            if (invitedGuardianUid.isNotBlank() && invitedGuardianUid != guardian.uid) {
                error("Bạn không có quyền phản hồi lời mời này")
            }
            if (invitedGuardianUid.isBlank() && invitedGuardianEmail != guardian.email.normalizedEmailOrBlank()) {
                error("Bạn không có quyền phản hồi lời mời này")
            }
            if (GuardianInvitationStatus.fromStored(invitation.getString(FIELD_STATUS)) != GuardianInvitationStatus.PENDING) {
                error("Lời mời đã được xử lý")
            }

            val userUid = invitation.getString(FIELD_USER_UID).orEmpty()
            if (accept) {
                val guardianRef = accountDocument(guardian.uid)

                transaction.set(
                    guardianRef,
                    mapOf(
                        FIELD_DISPLAY_NAME to guardianProfile.displayName,
                        FIELD_EMAIL to guardian.email,
                        FIELD_NORMALIZED_EMAIL to guardian.email.normalizedEmailOrBlank(),
                        FIELD_ACCOUNT_TYPE to AccountType.GUARDIAN.name,
                        FIELD_DEPENDENT_IDS to FieldValue.arrayUnion(userUid),
                    ),
                    SetOptions.merge(),
                )
                transaction.update(
                    invitationRef,
                    mapOf(
                        FIELD_GUARDIAN_UID to guardian.uid,
                        FIELD_GUARDIAN_DISPLAY_NAME to guardianProfile.displayName,
                        FIELD_GUARDIAN_EMAIL to guardian.email,
                        FIELD_GUARDIAN_NORMALIZED_EMAIL to guardian.email.normalizedEmailOrBlank(),
                        FIELD_STATUS to GuardianInvitationStatus.ACCEPTED.name,
                        FIELD_RESPONDED_AT to FieldValue.serverTimestamp(),
                    ),
                )
            } else {
                transaction.update(
                    invitationRef,
                    mapOf(
                        FIELD_GUARDIAN_UID to guardian.uid,
                        FIELD_GUARDIAN_DISPLAY_NAME to guardianProfile.displayName,
                        FIELD_GUARDIAN_EMAIL to guardian.email,
                        FIELD_GUARDIAN_NORMALIZED_EMAIL to guardian.email.normalizedEmailOrBlank(),
                        FIELD_STATUS to GuardianInvitationStatus.DECLINED.name,
                        FIELD_RESPONDED_AT to FieldValue.serverTimestamp(),
                    ),
                )
            }
        }.await()
    }

    override suspend fun unlinkGuardian(userUid: String, guardianUid: String) {
        val currentUser = requireCurrentUser()
        if (currentUser.uid != userUid && currentUser.uid != guardianUid) {
            error("Bạn không có quyền hủy liên kết này")
        }

        val batch = firestore.batch()
        batch.update(accountDocument(userUid), FIELD_GUARDIAN_IDS, FieldValue.arrayRemove(guardianUid))
        batch.update(accountDocument(guardianUid), FIELD_DEPENDENT_IDS, FieldValue.arrayRemove(userUid))
        batch.commit().await()
    }

    override suspend fun sendEmergencyAlert(
        phraseText: String,
        actionPayload: String?,
        type: EmergencyAlertType,
    ): List<String> {
        val profile = profileState.value ?: error("Vui lòng đăng nhập để gửi cảnh báo")
        val uid = profile.uid ?: error("Tài khoản offline không thể gửi cảnh báo")
        if (profile.accountType != AccountType.USER) return emptyList()

        val guardianIds = getLinkedGuardianIds(uid)
        if (guardianIds.isEmpty()) return emptyList()

        runCatching { cleanupEmergencyAlerts() }
            .onFailure { Timber.w(it, "Failed to cleanup emergency alerts before sending") }

        val batch = firestore.batch()
        val alertIds = mutableListOf<String>()
        val expiresAt = Timestamp(Date(System.currentTimeMillis() + ALERT_TTL_MILLIS))
        guardianIds.take(MAX_GUARDIANS).forEach { guardianUid ->
            val alertRef = firestore.collection(COLLECTION_EMERGENCY_ALERTS).document()
            alertIds += alertRef.id
            batch.set(
                alertRef,
                mapOf(
                    FIELD_USER_UID to uid,
                    FIELD_USER_DISPLAY_NAME to profile.displayName,
                    FIELD_GUARDIAN_UID to guardianUid,
                    FIELD_PHRASE_TEXT to phraseText,
                    FIELD_TYPE to type.name,
                    FIELD_ACTION_PAYLOAD to actionPayload,
                    FIELD_CREATED_AT to FieldValue.serverTimestamp(),
                    FIELD_EXPIRES_AT to expiresAt,
                    FIELD_READ_AT to null,
                ),
            )
        }
        batch.commit().await()
        return alertIds
    }

    override suspend fun hasAnyAlertRead(alertIds: List<String>): Boolean {
        if (alertIds.isEmpty()) return false
        return alertIds.any { alertId ->
            firestore.collection(COLLECTION_EMERGENCY_ALERTS)
                .document(alertId)
                .get()
                .await()
                .getTimestamp(FIELD_READ_AT) != null
        }
    }

    override suspend fun markAlertRead(alertId: String) {
        val user = requireCurrentUser()
        val alertRef = firestore.collection(COLLECTION_EMERGENCY_ALERTS).document(alertId)
        firestore.runTransaction { transaction ->
            val alert = transaction.get(alertRef)
            if (!alert.exists()) return@runTransaction
            if (alert.getString(FIELD_GUARDIAN_UID) != user.uid) error("Bạn không có quyền cập nhật cảnh báo này")
            transaction.update(alertRef, FIELD_READ_AT, FieldValue.serverTimestamp())
        }.await()
    }

    override suspend fun cleanupEmergencyAlerts() {
        val profile = profileState.value ?: return
        val uid = profile.uid ?: return
        if (profile.isGuest) return

        val field = if (profile.accountType == AccountType.GUARDIAN) FIELD_GUARDIAN_UID else FIELD_USER_UID
        val snapshots = firestore.collection(COLLECTION_EMERGENCY_ALERTS)
            .whereEqualTo(field, uid)
            .get()
            .await()
            .documents

        val nowMillis = System.currentTimeMillis()
        val deletable = snapshots.filter { it.shouldDeleteEmergencyAlert(nowMillis) }
        if (deletable.isEmpty()) return

        deletable.chunked(MAX_BATCH_WRITES).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { batch.delete(it.reference) }
            batch.commit().await()
        }
    }

    private fun observeConnections(uid: String, field: String): Flow<List<GuardianConnection>> = callbackFlow {
        val registration = accountDocument(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.w(error, "Failed to observe guardian connections")
                trySend(emptyList())
                return@addSnapshotListener
            }

            val ids = snapshot?.getStringList(field).orEmpty()
            if (ids.isEmpty()) {
                trySend(emptyList())
            } else {
                firestore.collection(COLLECTION_ACCOUNT_PROFILES)
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), ids)
                    .get()
                    .addOnSuccessListener { profiles ->
                        trySend(profiles.documents.map { it.toGuardianConnection() })
                    }
                    .addOnFailureListener {
                        Timber.w(it, "Failed to load guardian connection profiles")
                        trySend(emptyList())
                    }
            }
        }
        awaitClose { registration.remove() }
    }

    private fun observeAcceptedOutgoingConnections(uid: String): Flow<List<GuardianConnection>> {
        return observeInvitations(FIELD_USER_UID, uid, GuardianInvitationStatus.ACCEPTED)
            .map { invitations ->
                invitations.mapNotNull { invitation ->
                    invitation.guardianUid.takeIf { it.isNotBlank() }?.let { guardianUid ->
                        GuardianConnection(
                            uid = guardianUid,
                            email = invitation.guardianEmail,
                            displayName = invitation.guardianDisplayName.ifBlank {
                                invitation.guardianEmail ?: "Người giám hộ"
                            },
                        )
                    }
                }
            }
    }

    private fun observeAcceptedIncomingConnections(uid: String, normalizedEmail: String): Flow<List<GuardianConnection>> {
        val byUid = observeInvitations(FIELD_GUARDIAN_UID, uid, GuardianInvitationStatus.ACCEPTED)
            .map { invitations -> invitations.map(GuardianInvitation::toDependentConnection) }
        if (normalizedEmail.isBlank()) return byUid

        return combine(
            byUid,
            observeInvitations(FIELD_GUARDIAN_NORMALIZED_EMAIL, normalizedEmail, GuardianInvitationStatus.ACCEPTED)
                .map { invitations -> invitations.map(GuardianInvitation::toDependentConnection) },
        ) { byUidConnections, byEmailConnections ->
            (byUidConnections + byEmailConnections).distinctBy { it.uid }
        }
    }

    private suspend fun getLinkedGuardianIds(uid: String): List<String> {
        val storedGuardianIds = accountDocument(uid).get().await().getStringList(FIELD_GUARDIAN_IDS)
        val acceptedInvitationGuardianIds = firestore.collection(COLLECTION_GUARDIAN_INVITATIONS)
            .whereEqualTo(FIELD_USER_UID, uid)
            .get()
            .await()
            .documents
            .filter { GuardianInvitationStatus.fromStored(it.getString(FIELD_STATUS)) == GuardianInvitationStatus.ACCEPTED }
            .mapNotNull { it.getString(FIELD_GUARDIAN_UID)?.takeIf(String::isNotBlank) }

        return (storedGuardianIds + acceptedInvitationGuardianIds).distinct()
    }

    private fun observeInvitations(
        field: String,
        uid: String,
        status: GuardianInvitationStatus? = null,
    ): Flow<List<GuardianInvitation>> = callbackFlow {
        val query: Query = firestore.collection(COLLECTION_GUARDIAN_INVITATIONS).whereEqualTo(field, uid)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.w(error, "Failed to observe guardian invitations")
                trySend(emptyList())
            } else {
                val invitations = snapshot?.documents.orEmpty()
                    .mapNotNull { it.toGuardianInvitation() }
                    .filter { status == null || it.status == status }
                trySend(invitations)
            }
        }
        awaitClose { registration.remove() }
    }

    private fun DocumentSnapshot.shouldDeleteEmergencyAlert(nowMillis: Long): Boolean {
        val expiresAtMillis = getTimestamp(FIELD_EXPIRES_AT)?.toDate()?.time
        if (expiresAtMillis != null && expiresAtMillis <= nowMillis) return true

        val createdAtMillis = getTimestamp(FIELD_CREATED_AT)?.toDate()?.time ?: return false
        if (createdAtMillis + ALERT_TTL_MILLIS <= nowMillis) return true

        val readAtMillis = getTimestamp(FIELD_READ_AT)?.toDate()?.time ?: return false
        return readAtMillis + READ_ALERT_RETENTION_MILLIS <= nowMillis
    }

    private fun requireCurrentUser() = firebaseAuth.currentUser ?: error("Vui lòng đăng nhập")

    private fun accountDocument(uid: String) = firestore.collection(COLLECTION_ACCOUNT_PROFILES).document(uid)

    private companion object {
        const val MAX_GUARDIANS = 3
        const val COLLECTION_ACCOUNT_PROFILES = "account_profiles"
        const val COLLECTION_GUARDIAN_INVITATIONS = "guardian_invitations"
        const val COLLECTION_EMERGENCY_ALERTS = "emergency_alerts"
        const val ALERT_TTL_MILLIS = 7L * 24 * 60 * 60 * 1000
        const val READ_ALERT_RETENTION_MILLIS = 24L * 60 * 60 * 1000
        const val ACKNOWLEDGED_CALL_VISIBLE_MILLIS = 10L * 60 * 1000
        const val MAX_BATCH_WRITES = 450
        const val FIELD_ACCOUNT_TYPE = "accountType"
        const val FIELD_ACTION_PAYLOAD = "actionPayload"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_DEPENDENT_IDS = "dependentIds"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_EMAIL = "email"
        const val FIELD_EXPIRES_AT = "expiresAt"
        const val FIELD_GUARDIAN_DISPLAY_NAME = "guardianDisplayName"
        const val FIELD_GUARDIAN_EMAIL = "guardianEmail"
        const val FIELD_GUARDIAN_IDS = "guardianIds"
        const val FIELD_GUARDIAN_NORMALIZED_EMAIL = "guardianNormalizedEmail"
        const val FIELD_GUARDIAN_UID = "guardianUid"
        const val FIELD_NORMALIZED_EMAIL = "normalizedEmail"
        const val FIELD_PHRASE_TEXT = "phraseText"
        const val FIELD_READ_AT = "readAt"
        const val FIELD_RESPONDED_AT = "respondedAt"
        const val FIELD_STATUS = "status"
        const val FIELD_TYPE = "type"
        const val FIELD_USER_DISPLAY_NAME = "userDisplayName"
        const val FIELD_USER_UID = "userUid"
    }
}

private fun DocumentSnapshot.toGuardianConnection(): GuardianConnection {
    return GuardianConnection(
        uid = id,
        email = getString("email"),
        displayName = getString("displayName")?.takeIf { it.isNotBlank() } ?: getString("email") ?: "Tài khoản",
    )
}

private fun DocumentSnapshot.toGuardianInvitation(): GuardianInvitation? {
    if (!exists()) return null
    return GuardianInvitation(
        id = id,
        userUid = getString("userUid").orEmpty(),
        userDisplayName = getString("userDisplayName").orEmpty(),
        guardianUid = getString("guardianUid").orEmpty(),
        guardianDisplayName = getString("guardianDisplayName").orEmpty(),
        guardianEmail = getString("guardianEmail"),
        status = GuardianInvitationStatus.fromStored(getString("status")),
    )
}

private fun GuardianInvitation.toDependentConnection(): GuardianConnection {
    return GuardianConnection(
        uid = userUid,
        email = null,
        displayName = userDisplayName.ifBlank { "Người dùng" },
    )
}

private fun DocumentSnapshot.toEmergencyAlert(): EmergencyAlert? {
    if (!exists()) return null
    return EmergencyAlert(
        id = id,
        userUid = getString("userUid").orEmpty(),
        userDisplayName = getString("userDisplayName").orEmpty(),
        guardianUid = getString("guardianUid").orEmpty(),
        phraseText = getString("phraseText").orEmpty(),
        type = EmergencyAlertType.fromStored(getString("type")),
        actionPayload = getString("actionPayload"),
        createdAtMillis = getTimestamp("createdAt")?.toDate()?.time ?: 0L,
        readAtMillis = getTimestamp("readAt")?.toDate()?.time,
    )
}

private fun DocumentSnapshot.getStringList(field: String): List<String> {
    return get(field)?.let { value ->
        @Suppress("UNCHECKED_CAST")
        value as? List<String>
    }.orEmpty()
}

private fun String.normalizedEmail(): String = trim().lowercase()

private fun String?.normalizedEmailOrBlank(): String = this?.trim()?.lowercase().orEmpty()

private fun String.sha256(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray())
    return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
}
