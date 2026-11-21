package com.limpalivre.optimizer.service

import java.text.Normalizer
import java.util.Locale

enum class ButtonMatch {
    CLEAR_CACHE,
    STORAGE_SECTION,
    FORBIDDEN_DATA_WIPE,
    NONE
}

object CacheAutomationMatcher {

    private val forbiddenPhrases = listOf(
        "limpar dados",
        "apagar dados",
        "limpar armazenamento",
        "apagar armazenamento",
        "excluir dados",
        "gerenciar espaco",
        "gerenciar armazenamento",
        "clear data",
        "clear storage",
        "delete data",
        "manage space",
        "manage storage",
        "borrar datos",
        "eliminar datos",
        "borrar almacenamiento",
        "desinstalar",
        "uninstall",
        "forcar parada",
        "force stop"
    )

    private val clearCachePhrases = listOf(
        "limpar cache",
        "apagar cache",
        "esvaziar cache",
        "limpar o cache",
        "clear cache",
        "clear cached",
        "borrar cache",
        "eliminar cache",
        "limpiar cache"
    )

    private val storageSectionPhrases = listOf(
        "armazenamento e cache",
        "armazenamento",
        "uso de armazenamento",
        "storage & cache",
        "storage and cache",
        "storage usage",
        "storage",
        "almacenamiento y cache",
        "almacenamiento"
    )

    fun classifyLabel(rawText: String?): ButtonMatch {
        if (rawText.isNullOrBlank()) return ButtonMatch.NONE
        val normalized = normalize(rawText)
        if (normalized.isBlank()) return ButtonMatch.NONE

        if (forbiddenPhrases.any { normalized.contains(it) }) {
            return ButtonMatch.FORBIDDEN_DATA_WIPE
        }

        if (clearCachePhrases.any { normalized == it || normalized.contains(it) }) {
            return ButtonMatch.CLEAR_CACHE
        }

        if (storageSectionPhrases.any { normalized == it || normalized.contains(it) }) {
            return ButtonMatch.STORAGE_SECTION
        }

        return ButtonMatch.NONE
    }

    internal fun normalize(input: String): String {
        val decomposed = Normalizer.normalize(input.trim(), Normalizer.Form.NFD)
        val withoutAccents = decomposed.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        return withoutAccents
            .lowercase(Locale.ROOT)
            .replace(Regex("\\s+"), " ")
            .trim()
    }
}
