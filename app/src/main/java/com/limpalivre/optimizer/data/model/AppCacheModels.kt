package com.limpalivre.optimizer.data.model

enum class RiskLevel {
    SAFE,
    CAUTION,
    MANUAL
}

enum class AppCategory(val label: String) {
    SOCIAL("Redes Sociais"),
    BROWSER("Navegadores"),
    STREAMING("Vídeo e Música"),
    MESSAGING("Mensageiros"),
    GAMING("Jogos"),
    SHOPPING("Compras"),
    BANKING("Bancos e Pagamentos"),
    PRODUCTIVITY("Produtividade"),
    UTILITY("Utilitários")
}

data class AppExplanation(
    val whatIsInCache: String,
    val willItCauseProblems: String,
    val riskLevel: RiskLevel,
    val safetyTip: String
)

data class AppStorageItem(
    val packageName: String,
    val appName: String,
    val category: AppCategory,
    val cacheBytes: Long,
    val dataBytes: Long,
    val appBytes: Long,
    val lastTimeUsed: Long,
    val isSystemApp: Boolean,
    val isWhitelisted: Boolean,
    val isSelected: Boolean,
    val sensitivePermissions: List<String>,
    val explanation: AppExplanation
) {
    val totalBytes: Long
        get() = cacheBytes + dataBytes + appBytes
}
