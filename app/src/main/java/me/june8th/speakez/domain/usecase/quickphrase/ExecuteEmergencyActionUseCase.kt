package me.june8th.speakez.domain.usecase.quickphrase

import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import me.june8th.speakez.domain.model.ActionType
import me.june8th.speakez.domain.model.EmergencyAlertType
import me.june8th.speakez.domain.repository.GuardianRepository
import timber.log.Timber
import javax.inject.Inject

open class ExecuteEmergencyActionUseCase @Inject constructor(
    private val guardianRepository: GuardianRepository,
    @param:ApplicationContext private val context: Context,
) {
    open suspend operator fun invoke(phraseText: String, actionType: ActionType, actionPayload: String?) {
        Timber.d("Triggering action: $actionType with payload: $actionPayload")
        when (actionType) {
            ActionType.NONE -> Unit
            ActionType.PUSH_NOTI -> guardianRepository.sendEmergencyAlert(
                phraseText = phraseText,
                actionPayload = null,
            )
            ActionType.CALL -> requestGuardianCallThenFallback(phraseText, actionPayload)
        }
    }

    private suspend fun requestGuardianCallThenFallback(phraseText: String, phoneNumber: String?) {
        val normalizedPhoneNumber = phoneNumber?.trim().orEmpty()
        require(normalizedPhoneNumber.isNotBlank()) { "Vui lòng nhập số điện thoại để gọi" }

        val alertIds = runCatching {
            guardianRepository.sendEmergencyAlert(
                phraseText = phraseText,
                actionPayload = null,
                type = EmergencyAlertType.CALL_REQUEST,
            )
        }.getOrElse { throwable ->
            Timber.w(throwable, "Failed to send guardian call request, falling back to dialer")
            openDialer(normalizedPhoneNumber)
            return
        }
        if (alertIds.isEmpty()) {
            openDialer(normalizedPhoneNumber)
            return
        }

        delay(CALL_REQUEST_TIMEOUT_MILLIS)
        val hasGuardianResponse = runCatching { guardianRepository.hasAnyAlertRead(alertIds) }
            .getOrElse { throwable ->
                Timber.w(throwable, "Failed to check guardian call response")
                false
            }
        if (!hasGuardianResponse) {
            openDialer(normalizedPhoneNumber)
        }
    }

    private fun openDialer(phoneNumber: String?) {
        val normalizedPhoneNumber = phoneNumber?.trim().orEmpty()
        require(normalizedPhoneNumber.isNotBlank()) { "Vui lòng nhập số điện thoại để gọi" }

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.fromParts("tel", normalizedPhoneNumber, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private companion object {
        const val CALL_REQUEST_TIMEOUT_MILLIS = 30_000L
    }
}
