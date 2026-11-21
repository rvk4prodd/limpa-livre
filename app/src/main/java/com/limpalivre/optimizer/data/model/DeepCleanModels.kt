package com.limpalivre.optimizer.data.model

enum class DeepCategoryType {
    TEMP_LOGS,
    THUMBNAILS_CACHE,
    EMPTY_RESIDUAL,
    MESSENGER_CACHE,
    DUPLICATES,
    APK_INSTALLERS,
    LARGE_FILES
}

data class CleanableFileItem(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val isSelected: Boolean,
    val isProtectedOriginal: Boolean = false,
    val duplicateGroupId: String? = null,
    val detailNote: String = ""
)

data class DeepCleanCategory(
    val type: DeepCategoryType,
    val title: String,
    val subtitle: String,
    val whatItIs: String,
    val willItCauseProblems: String,
    val riskLevel: RiskLevel,
    val defaultSelected: Boolean,
    val items: List<CleanableFileItem>
) {
    val totalBytes: Long
        get() = items.sumOf { it.sizeBytes }

    val selectedBytes: Long
        get() = items.filter { it.isSelected && !it.isProtectedOriginal }.sumOf { it.sizeBytes }

    val selectedCount: Int
        get() = items.count { it.isSelected && !it.isProtectedOriginal }
}
