package me.june8th.speakez.domain.model

data class VocabularyItem(
    val id: String,
    val title: String,
    val iconColorHex: String,
    val containerColorHex: String,
    val category: String,
    val isVisible: Boolean = true,
    val customImageUri: String? = null,
)
