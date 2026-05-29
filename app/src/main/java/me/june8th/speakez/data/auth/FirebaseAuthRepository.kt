package me.june8th.speakez.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import me.june8th.speakez.di.ApplicationScope
import me.june8th.speakez.domain.model.AccountGender
import me.june8th.speakez.domain.model.AccountProfile
import me.june8th.speakez.domain.model.AccountType
import me.june8th.speakez.domain.model.AuthUser
import me.june8th.speakez.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val localSessionStore: LocalSessionStore,
    @ApplicationScope applicationScope: CoroutineScope,
) : AuthRepository {
    override val authState: Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.toAuthUser())
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    private val remoteProfileState: Flow<RemoteAccountProfile?> = authState.flatMapLatest { user ->
        if (user == null) {
            flowOf(null)
        } else {
            callbackFlow {
                val registration = accountDocument(user.uid).addSnapshotListener { snapshot, _ ->
                    trySend(snapshot?.toRemoteAccountProfile())
                }
                awaitClose { registration.remove() }
            }
        }
    }

    override val profileState: StateFlow<AccountProfile?> = combine(
        authState,
        localSessionStore.version,
        remoteProfileState,
    ) { user, _, remoteProfile ->
        when {
            user != null -> user.toProfile(remoteProfile)
            localSessionStore.isGuestMode -> AccountProfile(
                uid = null,
                email = null,
                displayName = localSessionStore.getGuestDisplayName(),
                dateOfBirth = localSessionStore.getGuestDateOfBirth(),
                gender = localSessionStore.getGuestGender(),
                accountType = localSessionStore.getGuestAccountType(),
                isGuest = true,
            )
            else -> null
        }
    }.stateIn(
        scope = applicationScope,
        started = SharingStarted.Eagerly,
        initialValue = firebaseAuth.currentUser?.toAuthUser()?.toProfile(remoteProfile = null)
            ?: if (localSessionStore.isGuestMode) {
                AccountProfile(
                    uid = null,
                    email = null,
                    displayName = localSessionStore.getGuestDisplayName(),
                    dateOfBirth = localSessionStore.getGuestDateOfBirth(),
                    gender = localSessionStore.getGuestGender(),
                    accountType = localSessionStore.getGuestAccountType(),
                    isGuest = true,
                )
            } else {
                null
            },
    )

    override suspend fun signInWithEmail(email: String, password: String) {
        val user = firebaseAuth.signInWithEmailAndPassword(email, password).await().user
            ?: error("Không thể đọc thông tin tài khoản sau khi đăng nhập")
        saveAccountLookup(user.uid, user.email, user.displayName)
        localSessionStore.clearGuestMode()
    }

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
        displayName: String,
        accountType: AccountType,
    ) {
        val user = firebaseAuth.createUserWithEmailAndPassword(email, password).await().user
            ?: error("Không thể tạo tài khoản")
        val resolvedName = displayName.ifBlank { email }
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(resolvedName).build()).await()
        localSessionStore.clearGuestMode()
        localSessionStore.saveFirebaseProfile(user.uid, resolvedName, accountType)
        saveRemoteProfile(
            uid = user.uid,
            email = user.email,
            displayName = resolvedName,
            dateOfBirth = "",
            gender = AccountGender.UNSPECIFIED,
            accountType = accountType,
        )
    }

    override suspend fun signInWithGoogle(idToken: String, accountType: AccountType?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val user = result.user
            ?: error("Không thể đăng nhập bằng Google")
        localSessionStore.clearGuestMode()
        saveAccountLookup(user.uid, user.email, user.displayName)
        if (accountType != null && result.additionalUserInfo?.isNewUser == true && !localSessionStore.hasAccountType(user.uid)) {
            val displayName = user.displayName?.takeIf { it.isNotBlank() } ?: user.email ?: "Tài khoản Google"
            localSessionStore.saveFirebaseProfile(user.uid, displayName, accountType)
            saveRemoteProfile(
                uid = user.uid,
                email = user.email,
                displayName = displayName,
                dateOfBirth = "",
                gender = AccountGender.UNSPECIFIED,
                accountType = accountType,
            )
        }
    }

    override suspend fun saveProfile(displayName: String, dateOfBirth: String, gender: AccountGender) {
        val profile = profileState.value ?: return
        if (profile.isGuest) {
            localSessionStore.saveGuestProfile(displayName, dateOfBirth, gender)
            return
        }

        val user = firebaseAuth.currentUser ?: return
        val resolvedName = displayName.ifBlank { user.email ?: "Tài khoản" }
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(resolvedName).build()).await()
        localSessionStore.saveFirebaseProfile(user.uid, resolvedName, profile.accountType, dateOfBirth, gender)
        saveRemoteProfile(
            uid = user.uid,
            email = user.email,
            displayName = resolvedName,
            dateOfBirth = dateOfBirth,
            gender = gender,
            accountType = profile.accountType,
        )
    }

    override fun continueAsGuest(displayName: String) {
        firebaseAuth.signOut()
        localSessionStore.setGuestMode(displayName)
    }

    override fun startLoginFromGuest() {
        localSessionStore.clearGuestMode()
    }

    override fun signOut() {
        firebaseAuth.signOut()
        localSessionStore.clearGuestMode()
    }

    private suspend fun saveRemoteProfile(
        uid: String,
        email: String?,
        displayName: String,
        dateOfBirth: String,
        gender: AccountGender,
        accountType: AccountType,
    ) {
        accountDocument(uid).set(
            mapOf(
                FIELD_DISPLAY_NAME to displayName,
                FIELD_EMAIL to email,
                FIELD_NORMALIZED_EMAIL to email.normalizedEmail(),
                FIELD_DATE_OF_BIRTH to dateOfBirth,
                FIELD_GENDER to gender.name,
                FIELD_ACCOUNT_TYPE to accountType.name,
            ),
            com.google.firebase.firestore.SetOptions.merge(),
        ).await()
    }

    private suspend fun saveAccountLookup(uid: String, email: String?, displayName: String?) {
        val fields = buildMap {
            email?.let {
                put(FIELD_EMAIL, it)
                put(FIELD_NORMALIZED_EMAIL, it.normalizedEmail())
            }
            displayName?.takeIf { it.isNotBlank() }?.let { put(FIELD_DISPLAY_NAME, it) }
        }
        if (fields.isEmpty()) return
        accountDocument(uid).set(
            fields,
            com.google.firebase.firestore.SetOptions.merge(),
        ).await()
    }

    private fun accountDocument(uid: String) = firestore.collection(COLLECTION_ACCOUNT_PROFILES).document(uid)

    private fun AuthUser.toProfile(remoteProfile: RemoteAccountProfile?): AccountProfile {
        val fallbackName = displayName?.takeIf { it.isNotBlank() } ?: email ?: "Tài khoản"
        return AccountProfile(
            uid = uid,
            email = email,
            displayName = remoteProfile?.displayName?.takeIf { it.isNotBlank() }
                ?: localSessionStore.getDisplayName(uid, fallbackName),
            dateOfBirth = remoteProfile?.dateOfBirth ?: localSessionStore.getDateOfBirth(uid),
            gender = remoteProfile?.gender ?: localSessionStore.getGender(uid),
            accountType = remoteProfile?.accountType ?: localSessionStore.getAccountType(uid),
            isGuest = false,
        )
    }

    private companion object {
        const val COLLECTION_ACCOUNT_PROFILES = "account_profiles"
        const val FIELD_DISPLAY_NAME = "displayName"
        const val FIELD_EMAIL = "email"
        const val FIELD_NORMALIZED_EMAIL = "normalizedEmail"
        const val FIELD_DATE_OF_BIRTH = "dateOfBirth"
        const val FIELD_GENDER = "gender"
        const val FIELD_ACCOUNT_TYPE = "accountType"
    }
}

private data class RemoteAccountProfile(
    val displayName: String,
    val dateOfBirth: String,
    val gender: AccountGender,
    val accountType: AccountType?,
)

private fun DocumentSnapshot.toRemoteAccountProfile(): RemoteAccountProfile? {
    if (!exists()) return null
    return RemoteAccountProfile(
        displayName = getString("displayName").orEmpty(),
        dateOfBirth = getString("dateOfBirth").orEmpty(),
        gender = AccountGender.fromStored(getString("gender")),
        accountType = getString("accountType")?.let(AccountType::fromStored),
    )
}

private fun com.google.firebase.auth.FirebaseUser.toAuthUser(): AuthUser {
    return AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
    )
}

private fun String?.normalizedEmail(): String? = this?.trim()?.lowercase()
