package me.june8th.speakez.data.mulberry

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.june8th.speakez.domain.model.MulberryCategory
import me.june8th.speakez.domain.model.MulberrySymbol
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MulberrySymbolRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private var cachedSymbols: List<MulberrySymbol>? = null

    suspend fun getSymbols(): List<MulberrySymbol> = withContext(Dispatchers.IO) {
        cachedSymbols ?: loadSymbols().also { cachedSymbols = it }
    }

    fun getCategories(symbols: List<MulberrySymbol>): List<MulberryCategory> {
        return symbols
            .groupBy { it.categoryId }
            .mapNotNull { (categoryId, categorySymbols) ->
                val title = categorySymbols.firstOrNull()?.categoryVi ?: return@mapNotNull null
                MulberryCategory(
                    id = categoryId,
                    title = title,
                    symbolCount = categorySymbols.size,
                )
            }
            .sortedBy { it.title.lowercase(VIETNAMESE_LOCALE) }
    }

    fun filterSymbols(
        symbols: List<MulberrySymbol>,
        query: String,
        categoryId: String?,
    ): List<MulberrySymbol> {
        val normalizedQuery = query.trim().lowercase(VIETNAMESE_LOCALE)
        return symbols.asSequence()
            .filter { symbol -> categoryId == null || symbol.categoryId == categoryId }
            .filter { symbol ->
                normalizedQuery.isBlank() ||
                    symbol.symbolVi.lowercase(VIETNAMESE_LOCALE).contains(normalizedQuery) ||
                    symbol.symbolEn.lowercase(Locale.US).contains(normalizedQuery) ||
                    symbol.tags.lowercase(Locale.US).contains(normalizedQuery)
            }
            .sortedWith(compareBy<MulberrySymbol> { it.symbolVi.lowercase(VIETNAMESE_LOCALE) }.thenBy { it.id.toIntOrNull() ?: Int.MAX_VALUE })
            .toList()
    }

    private fun loadSymbols(): List<MulberrySymbol> {
        return context.assets.open(SYMBOL_INFO_PATH).use { input ->
            input.bufferedReader(StandardCharsets.UTF_8).useLines { lines ->
                lines.drop(1)
                    .mapNotNull(::parseSymbol)
                    .toList()
            }
        }
    }

    private fun parseSymbol(line: String): MulberrySymbol? {
        val fields = parseCsvLine(line)
        if (fields.size < 9) return null

        val symbolEn = fields[5]
        val isRep = fields.getOrNull(9) == "1"
        return MulberrySymbol(
            id = fields[0],
            categoryId = fields[1],
            grammar = fields[2],
            rated = fields[3].toIntOrNull() ?: 0,
            tags = fields[4],
            symbolEn = symbolEn,
            categoryEn = fields[6],
            categoryVi = fields[7],
            symbolVi = fields[8],
            assetPath = "$SYMBOL_ASSET_BASE/$symbolEn.svg",
            isRepresentative = isRep,
        )
    }

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val field = StringBuilder()
        var inQuotes = false
        var index = 0

        while (index < line.length) {
            val char = line[index]
            when {
                char == '"' && inQuotes && index + 1 < line.length && line[index + 1] == '"' -> {
                    field.append('"')
                    index++
                }
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    fields += field.toString()
                    field.clear()
                }
                else -> field.append(char)
            }
            index++
        }

        fields += field.toString()
        return fields
    }

    private companion object {
        const val SYMBOL_INFO_PATH = "mulberry/symbol-info-vi-final.csv"
        const val SYMBOL_ASSET_BASE = "file:///android_asset/mulberry/EN-symbols"
        val VIETNAMESE_LOCALE: Locale = Locale.forLanguageTag("vi-VN")
    }
}
