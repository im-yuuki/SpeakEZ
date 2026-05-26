package me.june8th.speakez.data.local

import androidx.room.TypeConverter
import me.june8th.speakez.domain.model.ActionType

class ActionTypeConverter {
    @TypeConverter
    fun fromActionType(actionType: ActionType): String = actionType.name

    @TypeConverter
    fun toActionType(value: String): ActionType = ActionType.valueOf(value)
}
