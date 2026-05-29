package me.june8th.speakez.data.auth

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.june8th.speakez.domain.model.AccountGender
import me.june8th.speakez.domain.model.AccountType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSessionStore @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs = context.getSharedPreferences("SpeakEZ_Prefs", Context.MODE_PRIVATE)
    private val _version = MutableStateFlow(0)

    val version: StateFlow<Int> = _version.asStateFlow()

    val isOnboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    val isGuestMode: Boolean
        get() = prefs.getBoolean(KEY_GUEST_MODE, false)

    fun setGuestMode(displayName: String) {
        prefs.edit {
            putBoolean(KEY_GUEST_MODE, true)
            putString(KEY_GUEST_DISPLAY_NAME, displayName.ifBlank { DEFAULT_GUEST_NAME })
            putString(KEY_GUEST_ACCOUNT_TYPE, AccountType.USER.name)
        }
        bump()
    }

    fun clearGuestMode() {
        prefs.edit { putBoolean(KEY_GUEST_MODE, false) }
        bump()
    }

    fun getGuestDisplayName(): String = prefs.getString(KEY_GUEST_DISPLAY_NAME, DEFAULT_GUEST_NAME) ?: DEFAULT_GUEST_NAME

    fun getGuestAccountType(): AccountType = AccountType.USER

    fun getGuestDateOfBirth(): String = prefs.getString(KEY_GUEST_DATE_OF_BIRTH, "") ?: ""

    fun getGuestGender(): AccountGender = AccountGender.fromStored(prefs.getString(KEY_GUEST_GENDER, null))

    fun saveGuestProfile(displayName: String, dateOfBirth: String, gender: AccountGender) {
        prefs.edit {
            putString(KEY_GUEST_DISPLAY_NAME, displayName.ifBlank { DEFAULT_GUEST_NAME })
            putString(KEY_GUEST_ACCOUNT_TYPE, AccountType.USER.name)
            putString(KEY_GUEST_DATE_OF_BIRTH, dateOfBirth)
            putString(KEY_GUEST_GENDER, gender.name)
        }
        bump()
    }

    fun getDisplayName(uid: String, fallback: String): String {
        return prefs.getString(displayNameKey(uid), null)?.takeIf { it.isNotBlank() } ?: fallback
    }

    fun getAccountType(uid: String): AccountType = AccountType.fromStored(prefs.getString(accountTypeKey(uid), null))

    fun getDateOfBirth(uid: String): String = prefs.getString(dateOfBirthKey(uid), "") ?: ""

    fun getGender(uid: String): AccountGender = AccountGender.fromStored(prefs.getString(genderKey(uid), null))

    fun hasAccountType(uid: String): Boolean = prefs.contains(accountTypeKey(uid))

    fun saveFirebaseProfile(
        uid: String,
        displayName: String,
        accountType: AccountType,
        dateOfBirth: String = getDateOfBirth(uid),
        gender: AccountGender = getGender(uid),
    ) {
        prefs.edit {
            putString(displayNameKey(uid), displayName)
            putString(accountTypeKey(uid), accountType.name)
            putString(dateOfBirthKey(uid), dateOfBirth)
            putString(genderKey(uid), gender.name)
        }
        bump()
    }

    private fun bump() {
        _version.value += 1
    }

    private fun displayNameKey(uid: String) = "display_name_$uid"

    private fun accountTypeKey(uid: String) = "account_type_$uid"

    private fun dateOfBirthKey(uid: String) = "date_of_birth_$uid"

    private fun genderKey(uid: String) = "gender_$uid"

    private companion object {
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        const val KEY_GUEST_MODE = "guest_mode_enabled"
        const val KEY_GUEST_ACCOUNT_TYPE = "guest_account_type"
        const val KEY_GUEST_DISPLAY_NAME = "guest_display_name"
        const val KEY_GUEST_DATE_OF_BIRTH = "guest_date_of_birth"
        const val KEY_GUEST_GENDER = "guest_gender"
        const val DEFAULT_GUEST_NAME = "Người dùng"
    }
}
