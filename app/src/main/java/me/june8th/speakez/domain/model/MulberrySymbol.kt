package me.june8th.speakez.domain.model

data class MulberrySymbol(
    val id: String,
    val categoryId: String,
    val grammar: String,
    val rated: Int,
    val tags: String,
    val symbolEn: String,
    val categoryEn: String,
    val categoryVi: String,
    val symbolVi: String,
    val assetPath: String,
    val isRepresentative: Boolean = false,
)

data class MulberryCategory(
    val id: String,
    val title: String,
    val symbolCount: Int,
)

