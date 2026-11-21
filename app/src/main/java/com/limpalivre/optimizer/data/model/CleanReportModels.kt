package com.limpalivre.optimizer.data.model

enum class CleanActionStatus(val label: String) {
    SUCCESS("Limpo"),
    ALREADY_CLEAN("Já estava limpo"),
    PARTIAL("Parcial"),
    SKIPPED("Pulado"),
    MANUAL_REQUIRED("Requer toque manual"),
    FAILED("Não concluído")
}

data class CleanReportItem(
    val title: String,
    val subtitle: String,
    val bytesBefore: Long,
    val bytesFreed: Long,
    val status: CleanActionStatus,
    val whatWasDone: String,
    val problemAssessment: String
)

data class CleanReport(
    val id: String,
    val timestampMillis: Long,
    val operationType: String,
    val totalBytesFreed: Long,
    val itemsCleanedCount: Int,
    val itemsFailedCount: Int,
    val summaryDiagnosis: String,
    val hadAnyProblem: Boolean,
    val details: List<CleanReportItem>
)
