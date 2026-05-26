package me.june8th.speakez.data.quickphrase

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.june8th.speakez.domain.model.ActionType
import me.june8th.speakez.domain.model.QuickPhrase

@Entity(tableName = "quick_phrases")
data class QuickPhraseEntity(
    @PrimaryKey val id: String,
    val text: String,
    val actionType: ActionType,
    val actionPayload: String?,
)

fun QuickPhraseEntity.toDomain(): QuickPhrase {
    return QuickPhrase(
        id = id,
        text = text,
        actionType = actionType,
        actionPayload = actionPayload,
    )
}

fun QuickPhrase.toEntity(): QuickPhraseEntity {
    return QuickPhraseEntity(
        id = id,
        text = text,
        actionType = actionType,
        actionPayload = actionPayload,
    )
}
