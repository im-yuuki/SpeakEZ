package me.june8th.speakez.data.guardian

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import me.june8th.speakez.domain.model.AccountType
import me.june8th.speakez.domain.model.EmergencyAlert
import me.june8th.speakez.domain.model.GuardianConnection
import me.june8th.speakez.domain.model.GuardianInvitation
import me.june8th.speakez.domain.model.GuardianInvitationStatus
import me.june8th.speakez.domain.repository.AuthRepository
import me.june8th.speakez.domain.repository.GuardianRepository
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
            if (uid == null || profile.isGuest) flowOf(emptyList()) else observeConnections(uid, FIELD_GUARDIAN_IDS)
        }
    }

    override fun observeDependentConnections(): Flow<List<GuardianConnection>> {
        return profileState.flatMapLatest { profile ->
            val uid = profile?.uid
            if (uid == null || profile.isGuest) flowOf(emptyList()) else observeConnections(uid, FIELD_DEPENDENT_IDS)
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
                observeInvitations(FIELD_GUARDIAN_UID, uid, GuardianInvitationStatus.PENDING)
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
                    val registration = firestore.collection(COLLECTION_EMERGENCY_ALERTS)
                        .whereEqualTo(FIELD_GUARDIAN_UID, uid)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                close(error)
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

    override suspend fun inviteGuardian(email: String) {
        val user = requireCurrentUser()
        val requester = profileState.value ?: error("Vui lòng đăng nhập để gửi lời mời")
        if (requester.accountType != AccountType.USER) error("Chỉ tài khoản người dùng mới gửi lời mời giám hộ")

        val normalizedEmail = email.normalizedEmail()
        if (normalizedEmail.isBlank()) error("Vui lòng nhập email người giám hộ")

        val userSnapshot = accountDocument(user.uid).get().await()
        val currentGuardianIds = userSnapshot.getStringList(FIELD_GUARDIAN_IDS)
        if (currentGuardianIds.size >= MAX_GUARDIANS) error("Mỗi người dùng chỉ liên kết tối đa 3 người giám hộ")

        val guardianSnapshot = firestore.collection(COLLECTION_ACCOUNT_PROFILES)
            .whereEqualTo(FIELD_NORMALIZED_EMAIL, normalizedEmail)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
            ?: error("Không tìm thấy tài khoản người giám hộ với email này")

        val guardianUid = guardianSnapshot.id
        if (guardianUid == user.uid) error("Không thể tự mời chính tài khoản này")
        if (AccountType.fromStored(guardianSnapshot.getString(FIELD_ACCOUNT_TYPE)) != AccountType.GUARDIAN) {
            error("Email này không thuộc tài khoản người giám hộ")
        }
        if (currentGuardianIds.contains(guardianUid)) error("Người giám hộ này đã được liên kết")

        val invitationRef = firestore.collection(COLLECTION_GUARDIAN_INVITATIONS).document("${user.uid}_$guardianUid")
        val currentInvitation = invitationRef.get().await()
        if (currentInvitation.exists() &&
            GuardianInvitationStatus.fromStored(currentInvitation.getString(FIELD_STATUS)) == GuardianInvitationStatus.PENDING
        ) {
            error("Đã có lời mời đang chờ người giám hộ này phản hồi")
        }

        invitationRef.set(
            mapOf(
                FIELD_USER_UID to user.uid,
                FIELD_USER_DISPLAY_NAME to requester.displayName,
                FIELD_GUARDIAN_UID to guardianUid,
                FIELD_GUARDIAN_DISPLAY_NAME to guardianSnapshot.getString(FIELD_DISPLAY_NAME).orEmpty(),
                FIELD_GUARDIAN_EMAIL to guardianSnapshot.getString(FIELD_EMAIL),
                FIELD_STATUS to GuardianInvitationStatus.PENDING.name,
                FIELD_CREATED_AT to FieldValue.serverTimestamp(),
                FIELD_RESPONDED_AT to null,
            ),
        ).await()
    }

    override suspend fun respondToInvitation(invitationId: String, accept: Boolean) {
        val guardian = requireCurrentUser()
        val invitationRef = firestore.collection(COLLECTION_GUARDIAN_INVITATIONS).document(invitationId)

        firestore.runTransaction { transaction ->
            val invitation = transaction.get(invitationRef)
            if (!invitation.exists()) error("Lời mời không còn tồn tại")
            if (invitation.getString(FIELD_GUARDIAN_UID) != guardian.uid) error("Bạn không có quyền phản hồi lời mời này")
            if (GuardianInvitationStatus.fromStored(invitation.getString(FIELD_STATUS)) != GuardianInvitationStatus.PENDING) {
                error("Lời mời đã được xử lý")
            }

            val userUid = invitation.getString(FIELD_USER_UID).orEmpty()
            if (accept) {
                val userRef = accountDocument(userUid)
                val guardianRef = accountDocument(guardian.uid)
                val userSnapshot = transaction.get(userRef)
                val guardianIds = userSnapshot.getStringList(FIELD_GUARDIAN_IDS)
                if (!guardianIds.contains(guardian.uid) && guardianIds.size >= MAX_GUARDIANS) {
                    error("Người dùng này đã có đủ 3 người giám hộ")
                }

                transaction.update(userRef, FIELD_GUARDIAN_IDS, FieldValue.arrayUnion(guardian.uid))
                transaction.update(guardianRef, FIELD_DEPENDENT_IDS, FieldValue.arrayUnion(userUid))
                transaction.update(
                    invitationRef,
                    mapOf(
                        FIELD_STATUS to GuardianInvitationStatus.ACCEPTED.name,
                        FIELD_RESPONDED_AT to FieldValue.serverTimestamp(),
                    ),
                )
            } else {
                transaction.update(
                    invitationRef,
                    mapOf(
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

    override suspend fun sendEmergencyAlert(phraseText: String, actionPayload: String?) {
        val profile = profileState.value ?: error("Vui lòng đăng nhập để gửi cảnh báo")
        val uid = profile.uid ?: error("Tài khoản offline không thể gửi cảnh báo")
        if (profile.accountType != AccountType.USER) return

        val guardianIds = accountDocument(uid).get().await().getStringList(FIELD_GUARDIAN_IDS)
        if (guardianIds.isEmpty()) return

        val batch = firestore.batch()
        guardianIds.take(MAX_GUARDIANS).forEach { guardianUid ->
            val alertRef = firestore.collection(COLLECTION_EMERGENCY_ALERTS).document()
            batch.set(
                alertRef,
                mapOf(
                    FIELD_USER_UID to uid,
                    FIELD_USER_DISPLAY_NAME to profile.displayName,
                    FIELD_GUARDIAN_UID to guardianUid,
                    FIELD_PHRASE_TEXT to phraseText,
                    FIELD_ACTION_PAYLOAD to actionPayload,
                    FIELD_CREATED_AT to FieldValue.serverTimestamp(),
                    FIELD_READ_AT to null,
                ),
            )
        }
        batch.commit().await()
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

    private fun observeConnections(uid: String, field: String): Flow<List<GuardianConnection>> = callbackFlow {
        val registration = accountDocument(uid).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
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
                    .addOnFailureListener { close(it) }
            }
        }
        awaitClose { registration.remove() }
    }

    private fun observeInvitations(
        field: String,
        uid: String,
        status: GuardianInvitationStatus? = null,
    ): Flow<List<GuardianInvitation>> = callbackFlow {
        val query: Query = firestore.collection(COLLECTION_GUARDIAN_INVITATIONS).whereEqualTo(field, uid)
        val registration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
            } else {
                val invitations = snapshot?.documents.orEmpty()
                    .mapNotNull { it.toGuardianInvitation() }
                    .filter { status == null || it.status == status }
                trySend(invitations)
            }
        }
        awaitClose { registration.remove() }
    }

    private fun requireCurrentUser() = firebaseAuth.currentUser ?: error("Vui lòng đăng nhập")

    private fun accountDocument(uid: String) = firestore.collection(COLLECTION_ACCOUNT_PROFILES).document(uid)

    private companion object {
        const val MAX_GUARDIANS = 3
        const val COLLECTION_ACCOUNT_PROFILES = "account_profiles"
        const val COLLECTION_GUARDIAN_INVITATIONS = "guardian_invitations"
        const val COLLECTION_EMERGENCY_ALERTS = "emergency_alerts"
        const val FIELD_ACCOUNT_TYPE = "accountType"
        const val FIELD_ACTION_PAYLOAD = "actionPayload"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_DEPENDENT_IDS = "dependentIds"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_EMAIL = "email"
        const val FIELD_GUARDIAN_DISPLAY_NAME = "guardianDisplayName"
        const val FIELD_GUARDIAN_EMAIL = "guardianEmail"
        const val FIELD_GUARDIAN_IDS = "guardianIds"
        const val FIELD_GUARDIAN_UID = "guardianUid"
        const val FIELD_NORMALIZED_EMAIL = "normalizedEmail"
        const val FIELD_PHRASE_TEXT = "phraseText"
        const val FIELD_READ_AT = "readAt"
        const val FIELD_RESPONDED_AT = "respondedAt"
        const val FIELD_STATUS = "status"
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

private fun DocumentSnapshot.toEmergencyAlert(): EmergencyAlert? {
    if (!exists()) return null
    return EmergencyAlert(
        id = id,
        userUid = getString("userUid").orEmpty(),
        userDisplayName = getString("userDisplayName").orEmpty(),
        guardianUid = getString("guardianUid").orEmpty(),
        phraseText = getString("phraseText").orEmpty(),
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
