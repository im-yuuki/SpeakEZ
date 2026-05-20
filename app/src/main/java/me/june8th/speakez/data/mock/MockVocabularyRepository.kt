package me.june8th.speakez.data.mock

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.june8th.speakez.domain.model.VocabularyItem

object MockVocabularyRepository {
    private val categoriesData = listOf(
        VocabularyItem(
            id = "cat_1",
            title = "Ăn uống",
            iconColorHex = "0xFF0B7A75",
            containerColorHex = "0xFFDDF7F4",
            category = "Food"
        ),
        VocabularyItem(
            id = "cat_2",
            title = "Y tế",
            iconColorHex = "0xFFB54708",
            containerColorHex = "0xFFFFE8D6",
            category = "Health"
        ),
        VocabularyItem(
            id = "cat_3",
            title = "Hoạt động",
            iconColorHex = "0xFF2F5AA8",
            containerColorHex = "0xFFDDE8FF",
            category = "Activity"
        ),
        VocabularyItem(
            id = "cat_4",
            title = "Cảm xúc",
            iconColorHex = "0xFF8E3B9E",
            containerColorHex = "0xFFF3E0F8",
            category = "Emotion"
        ),
        VocabularyItem(
            id = "cat_5",
            title = "Cơ thể",
            iconColorHex = "0xFF7A5B00",
            containerColorHex = "0xFFFFF0C2",
            category = "Body"
        ),
    )

    private val _allVocabulary = MutableStateFlow(
        listOf(
            VocabularyItem("food_1", "Cơm", "0xFF0B7A75", "0xFFDDF7F4", "Food"),
            VocabularyItem("food_2", "Canh", "0xFF0B7A75", "0xFFDDF7F4", "Food"),
            VocabularyItem("food_3", "Trái cây", "0xFF0B7A75", "0xFFDDF7F4", "Food"),
            VocabularyItem("food_4", "Nước", "0xFF0B7A75", "0xFFDDF7F4", "Food"),
            VocabularyItem("food_5", "Bánh", "0xFF0B7A75", "0xFFDDF7F4", "Food"),
            VocabularyItem("health_1", "Bác sĩ", "0xFFB54708", "0xFFFFE8D6", "Health"),
            VocabularyItem("health_2", "Đau", "0xFFB54708", "0xFFFFE8D6", "Health"),
            VocabularyItem("health_3", "Thuốc", "0xFFB54708", "0xFFFFE8D6", "Health"),
            VocabularyItem("health_4", "Bệnh", "0xFFB54708", "0xFFFFE8D6", "Health"),
            VocabularyItem("health_5", "Sốt", "0xFFB54708", "0xFFFFE8D6", "Health"),
            VocabularyItem("activity_1", "Chơi", "0xFF2F5AA8", "0xFFDDE8FF", "Activity"),
            VocabularyItem("activity_2", "Đi", "0xFF2F5AA8", "0xFFDDE8FF", "Activity"),
            VocabularyItem("activity_3", "Ngủ", "0xFF2F5AA8", "0xFFDDE8FF", "Activity"),
            VocabularyItem("activity_4", "Lạy", "0xFF2F5AA8", "0xFFDDE8FF", "Activity"),
            VocabularyItem("activity_5", "Tắm", "0xFF2F5AA8", "0xFFDDE8FF", "Activity"),
            VocabularyItem("emotion_1", "Vui", "0xFF8E3B9E", "0xFFF3E0F8", "Emotion"),
            VocabularyItem("emotion_2", "Buồn", "0xFF8E3B9E", "0xFFF3E0F8", "Emotion"),
            VocabularyItem("emotion_3", "Sợ", "0xFF8E3B9E", "0xFFF3E0F8", "Emotion"),
            VocabularyItem("emotion_4", "Tức", "0xFF8E3B9E", "0xFFF3E0F8", "Emotion"),
            VocabularyItem("emotion_5", "Yêu", "0xFF8E3B9E", "0xFFF3E0F8", "Emotion"),
            VocabularyItem("body_1", "Tay", "0xFF7A5B00", "0xFFFFF0C2", "Body"),
            VocabularyItem("body_2", "Chân", "0xFF7A5B00", "0xFFFFF0C2", "Body"),
            VocabularyItem("body_3", "Mặt", "0xFF7A5B00", "0xFFFFF0C2", "Body"),
            VocabularyItem("body_4", "Mắt", "0xFF7A5B00", "0xFFFFF0C2", "Body"),
            VocabularyItem("body_5", "Tai", "0xFF7A5B00", "0xFFFFF0C2", "Body"),
        )
    )
    val allVocabulary: StateFlow<List<VocabularyItem>> = _allVocabulary.asStateFlow()

    fun getCategories(): List<VocabularyItem> = categoriesData

    fun toggleVisibility(itemId: String) {
        _allVocabulary.value = _allVocabulary.value.map { item ->
            if (item.id == itemId) item.copy(isVisible = !item.isVisible) else item
        }
    }

    fun addVocabulary(label: String, emoji: String) {
        if (label.isBlank()) return
        val normalized = label.trim()
        val category = categoriesData.firstOrNull()?.category ?: "Food"
        val template = categoriesData.firstOrNull()
        _allVocabulary.value = _allVocabulary.value + VocabularyItem(
            id = "custom_${System.currentTimeMillis()}",
            title = "$emoji $normalized",
            iconColorHex = template?.iconColorHex ?: "0xFF0B7A75",
            containerColorHex = template?.containerColorHex ?: "0xFFDDF7F4",
            category = category,
            isVisible = true,
        )
    }

    fun updateCustomImage(itemId: String, imageUri: String) {
        _allVocabulary.value = _allVocabulary.value.map { item ->
            if (item.id == itemId) item.copy(customImageUri = imageUri) else item
        }
    }
}
