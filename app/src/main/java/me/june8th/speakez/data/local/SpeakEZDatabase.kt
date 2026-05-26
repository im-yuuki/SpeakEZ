package me.june8th.speakez.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.june8th.speakez.data.quickphrase.QuickPhraseDao
import me.june8th.speakez.data.quickphrase.QuickPhraseEntity

@Database(
    entities = [
        QuickPhraseEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
@TypeConverters(ActionTypeConverter::class)
abstract class SpeakEZDatabase : RoomDatabase() {
    abstract fun quickPhraseDao(): QuickPhraseDao
}
