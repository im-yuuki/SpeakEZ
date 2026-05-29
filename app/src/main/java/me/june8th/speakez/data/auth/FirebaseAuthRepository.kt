package me.june8th.speakez.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await
import me.june8th.speakez.di.ApplicationScope
import me.june8th.speakez.domain.model.AccountProfile
import me.june8th.speakez.domain.model.AccountType
import me.june8th.speakez.domain.model.AuthUser
import me.june8th.speakez.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
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

    override val profileState: StateFlow<AccountProfile?> = combine(
        authState,
        localSessionStore.version,
    ) { user, _ ->
        when {
            user != null -> user.toProfile()
            localSessionStore.isGuestMode -> AccountProfile(
                uid = null,
                email = null,
                displayName = localSessionStore.getGuestDisplayName(),
                accountType = localSessionStore.getGuestAccountType(),
                isGuest = true,
            )
            else -> null
        }
    }.stateIn(
        scope = applicationScope,
        started = SharingStarted.Eagerly,
        initialValue = firebaseAuth.currentUser?.toAuthUser()?.toProfile()
            ?: if (localSessionStore.isGuestMode) {
                AccountProfile(
                    uid = null,
                    email = null,
                    displayName = localSessionStore.getGuestDisplayName(),
                    accountType = localSessionStore.getGuestAccountType(),
                    isGuest = true,
                )
            } else {
                null
            },
    )

    override suspend fun signInWithEmail(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await().user
            ?: error("Không thể đọc thông tin tài khoản sau khi đăng nhập")
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
    }

    override suspend fun signInWithGoogle(idToken: String, accountType: AccountType?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val user = result.user
            ?: error("Không thể đăng nhập bằng Google")
        localSessionStore.clearGuestMode()
        if (accountType != null && result.additionalUserInfo?.isNewUser == true && !localSessionStore.hasAccountType(user.uid)) {
            val displayName = user.displayName?.takeIf { it.isNotBlank() } ?: user.email ?: "Tài khoản Google"
            localSessionStore.saveFirebaseProfile(user.uid, displayName, accountType)
        }
    }

    override suspend fun saveProfile(displayName: String) {
        val profile = profileState.value ?: return
        if (profile.isGuest) {
            localSessionStore.saveGuestProfile(displayName)
            return
        }

        val user = firebaseAuth.currentUser ?: return
        val resolvedName = displayName.ifBlank { user.email ?: "Tài khoản" }
        user.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(resolvedName).build()).await()
        localSessionStore.saveFirebaseProfile(user.uid, resolvedName, profile.accountType)
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

    private fun AuthUser.toProfile(): AccountProfile {
        val fallbackName = displayName?.takeIf { it.isNotBlank() } ?: email ?: "Tài khoản"
        return AccountProfile(
            uid = uid,
            email = email,
            displayName = localSessionStore.getDisplayName(uid, fallbackName),
            accountType = localSessionStore.getAccountType(uid),
            isGuest = false,
        )
    }
}

private fun com.google.firebase.auth.FirebaseUser.toAuthUser(): AuthUser {
    return AuthUser(
        uid = uid,
        email = email,
        displayName = displayName,
        photoUrl = photoUrl?.toString(),
    )
}
