package me.june8th.speakez.data.quickphrase

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface QuickPhraseDao {
    @Query("SELECT * FROM quick_phrases ORDER BY text COLLATE NOCASE ASC")
    fun observeQuickPhrases(): Flow<List<QuickPhraseEntity>>

    @Query("SELECT COUNT(*) FROM quick_phrases")
    suspend fun countQuickPhrases(): Int

    @Upsert
    suspend fun upsertQuickPhrase(quickPhrase: QuickPhraseEntity)

    @Upsert
    suspend fun upsertQuickPhrases(quickPhrases: List<QuickPhraseEntity>)

    @Query("DELETE FROM quick_phrases WHERE id = :id")
    suspend fun deleteQuickPhrase(id: String)
}
