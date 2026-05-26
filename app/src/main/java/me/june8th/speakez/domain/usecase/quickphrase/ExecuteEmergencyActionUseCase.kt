package me.june8th.speakez.domain.usecase.quickphrase

import me.june8th.speakez.domain.model.ActionType
import timber.log.Timber
import javax.inject.Inject

open class ExecuteEmergencyActionUseCase @Inject constructor() {
    open operator fun invoke(actionType: ActionType, actionPayload: String?) {
        Timber.d("Triggering action: $actionType with payload: $actionPayload")
    }
}
