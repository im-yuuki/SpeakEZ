package me.june8th.speakez.data.mock

import me.june8th.speakez.domain.model.VocabularyItem

object MockVocabularyRepository {
    fun getCategories(): List<VocabularyItem> = listOf(
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

    /**
     * Get all vocabulary items with subcategories
     */
    fun getAllVocabulary(): List<VocabularyItem> = listOf(
        // Food category
        VocabularyItem(
            id = "food_1",
            title = "Cơm",
            iconColorHex = "0xFF0B7A75",
            containerColorHex = "0xFFDDF7F4",
            category = "Food"
        ),
        VocabularyItem(
            id = "food_2",
            title = "Canh",
            iconColorHex = "0xFF0B7A75",
            containerColorHex = "0xFFDDF7F4",
            category = "Food"
        ),
        VocabularyItem(
            id = "food_3",
            title = "Trái cây",
            iconColorHex = "0xFF0B7A75",
            containerColorHex = "0xFFDDF7F4",
            category = "Food"
        ),
        VocabularyItem(
            id = "food_4",
            title = "Nước",
            iconColorHex = "0xFF0B7A75",
            containerColorHex = "0xFFDDF7F4",
            category = "Food"
        ),
        VocabularyItem(
            id = "food_5",
            title = "Bánh",
            iconColorHex = "0xFF0B7A75",
            containerColorHex = "0xFFDDF7F4",
            category = "Food"
        ),
        // Health category
        VocabularyItem(
            id = "health_1",
            title = "Bác sĩ",
            iconColorHex = "0xFFB54708",
            containerColorHex = "0xFFFFE8D6",
            category = "Health"
        ),
        VocabularyItem(
            id = "health_2",
            title = "Đau",
            iconColorHex = "0xFFB54708",
            containerColorHex = "0xFFFFE8D6",
            category = "Health"
        ),
        VocabularyItem(
            id = "health_3",
            title = "Thuốc",
            iconColorHex = "0xFFB54708",
            containerColorHex = "0xFFFFE8D6",
            category = "Health"
        ),
        VocabularyItem(
            id = "health_4",
            title = "Bệnh",
            iconColorHex = "0xFFB54708",
            containerColorHex = "0xFFFFE8D6",
            category = "Health"
        ),
        VocabularyItem(
            id = "health_5",
            title = "Sốt",
            iconColorHex = "0xFFB54708",
            containerColorHex = "0xFFFFE8D6",
            category = "Health"
        ),
        // Activity category
        VocabularyItem(
            id = "activity_1",
            title = "Chơi",
            iconColorHex = "0xFF2F5AA8",
            containerColorHex = "0xFFDDE8FF",
            category = "Activity"
        ),
        VocabularyItem(
            id = "activity_2",
            title = "Đi",
            iconColorHex = "0xFF2F5AA8",
            containerColorHex = "0xFFDDE8FF",
            category = "Activity"
        ),
        VocabularyItem(
            id = "activity_3",
            title = "Ngủ",
            iconColorHex = "0xFF2F5AA8",
            containerColorHex = "0xFFDDE8FF",
            category = "Activity"
        ),
        VocabularyItem(
            id = "activity_4",
            title = "Lạy",
            iconColorHex = "0xFF2F5AA8",
            containerColorHex = "0xFFDDE8FF",
            category = "Activity"
        ),
        VocabularyItem(
            id = "activity_5",
            title = "Tắm",
            iconColorHex = "0xFF2F5AA8",
            containerColorHex = "0xFFDDE8FF",
            category = "Activity"
        ),
        // Emotion category
        VocabularyItem(
            id = "emotion_1",
            title = "Vui",
            iconColorHex = "0xFF8E3B9E",
            containerColorHex = "0xFFF3E0F8",
            category = "Emotion"
        ),
        VocabularyItem(
            id = "emotion_2",
            title = "Buồn",
            iconColorHex = "0xFF8E3B9E",
            containerColorHex = "0xFFF3E0F8",
            category = "Emotion"
        ),
        VocabularyItem(
            id = "emotion_3",
            title = "Sợ",
            iconColorHex = "0xFF8E3B9E",
            containerColorHex = "0xFFF3E0F8",
            category = "Emotion"
        ),
        VocabularyItem(
            id = "emotion_4",
            title = "Tức",
            iconColorHex = "0xFF8E3B9E",
            containerColorHex = "0xFFF3E0F8",
            category = "Emotion"
        ),
        VocabularyItem(
            id = "emotion_5",
            title = "Yêu",
            iconColorHex = "0xFF8E3B9E",
            containerColorHex = "0xFFF3E0F8",
            category = "Emotion"
        ),
        // Body category
        VocabularyItem(
            id = "body_1",
            title = "Tay",
            iconColorHex = "0xFF7A5B00",
            containerColorHex = "0xFFFFF0C2",
            category = "Body"
        ),
        VocabularyItem(
            id = "body_2",
            title = "Chân",
            iconColorHex = "0xFF7A5B00",
            containerColorHex = "0xFFFFF0C2",
            category = "Body"
        ),
        VocabularyItem(
            id = "body_3",
            title = "Mặt",
            iconColorHex = "0xFF7A5B00",
            containerColorHex = "0xFFFFF0C2",
            category = "Body"
        ),
        VocabularyItem(
            id = "body_4",
            title = "Mắt",
            iconColorHex = "0xFF7A5B00",
            containerColorHex = "0xFFFFF0C2",
            category = "Body"
        ),
        VocabularyItem(
            id = "body_5",
            title = "Tai",
            iconColorHex = "0xFF7A5B00",
            containerColorHex = "0xFFFFF0C2",
            category = "Body"
        ),
    )
}

