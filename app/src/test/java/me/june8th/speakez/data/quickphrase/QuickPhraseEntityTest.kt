package me.june8th.speakez.data.quickphrase

import me.june8th.speakez.domain.model.ActionType
import me.june8th.speakez.domain.model.QuickPhrase
import org.junit.Assert.assertEquals
import org.junit.Test

class QuickPhraseEntityTest {
    @Test
    fun toDomain_mapsAllFields() {
        val entity = QuickPhraseEntity(
            id = "phrase-1",
            text = "Gọi người thân",
            actionType = ActionType.CALL,
            actionPayload = "0900000000",
        )

        assertEquals(
            QuickPhrase(
                id = "phrase-1",
                text = "Gọi người thân",
                actionType = ActionType.CALL,
                actionPayload = "0900000000",
            ),
            entity.toDomain(),
        )
    }

    @Test
    fun toEntity_mapsAllFields() {
        val quickPhrase = QuickPhrase(
            id = "phrase-2",
            text = "Con bị đau",
            actionType = ActionType.NONE,
            actionPayload = null,
        )

        assertEquals(
            QuickPhraseEntity(
                id = "phrase-2",
                text = "Con bị đau",
                actionType = ActionType.NONE,
                actionPayload = null,
            ),
            quickPhrase.toEntity(),
        )
    }
}
