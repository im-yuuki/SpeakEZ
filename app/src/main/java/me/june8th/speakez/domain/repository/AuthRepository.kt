package me.june8th.speakez.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import me.june8th.speakez.domain.model.AccountProfile
import me.june8th.speakez.domain.model.AccountType
import me.june8th.speakez.domain.model.AuthUser

interface AuthRepository {
    val authState: Flow<AuthUser?>
    val profileState: StateFlow<AccountProfile?>

    suspend fun signInWithEmail(email: String, password: String)
    suspend fun signUpWithEmail(email: String, password: String, displayName: String, accountType: AccountType)
    suspend fun signInWithGoogle(idToken: String, accountType: AccountType?)
    suspend fun saveProfile(displayName: String)
    fun continueAsGuest(displayName: String)
    fun startLoginFromGuest()
    fun signOut()
}
