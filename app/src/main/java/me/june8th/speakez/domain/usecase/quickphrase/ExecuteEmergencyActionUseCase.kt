package me.june8th.speakez.domain.usecase.quickphrase

import me.june8th.speakez.domain.model.ActionType
import me.june8th.speakez.domain.repository.GuardianRepository
import timber.log.Timber
import javax.inject.Inject

open class ExecuteEmergencyActionUseCase @Inject constructor(
    private val guardianRepository: GuardianRepository,
) {
    open suspend operator fun invoke(phraseText: String, actionType: ActionType, actionPayload: String?) {
        Timber.d("Triggering action: $actionType with payload: $actionPayload")
        if (actionType == ActionType.PUSH_NOTI) {
            guardianRepository.sendEmergencyAlert(
                phraseText = phraseText,
                actionPayload = actionPayload,
            )
        }
    }
}
