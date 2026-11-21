package com.limpalivre.optimizer.service

import com.limpalivre.optimizer.data.explainer.ExplainerCatalog
import com.limpalivre.optimizer.data.model.AppStorageItem
import com.limpalivre.optimizer.data.model.CleanActionStatus
import com.limpalivre.optimizer.data.model.CleanReport
import com.limpalivre.optimizer.data.model.CleanReportItem
import com.limpalivre.optimizer.domain.AppCacheScanner
import com.limpalivre.optimizer.domain.ByteFormatter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class QueueAppEntry(
    val packageName: String,
    val appName: String,
    val bytesBefore: Long,
    val outcome: CleanActionStatus? = null
)

data class AutomationState(
    val isRunning: Boolean = false,
    val isManualGuidedMode: Boolean = false,
    val queue: List<QueueAppEntry> = emptyList(),
    val currentIndex: Int = 0,
    val openedStorageForCurrent: Boolean = false,
    val lastActionTimestampMillis: Long = 0L,
    val awaitingReportGeneration: Boolean = false,
    val statusMessage: String = ""
) {
    val currentApp: QueueAppEntry?
        get() = queue.getOrNull(currentIndex)

    val progressFraction: Float
        get() = if (queue.isEmpty()) 0f else (currentIndex.toFloat() / queue.size.toFloat()).coerceIn(0f, 1f)
}

object CleanAutomationCoordinator {

    private val _state = MutableStateFlow(AutomationState())
    val state: StateFlow<AutomationState> = _state.asStateFlow()

    fun startAutomatedQueue(apps: List<AppStorageItem>) {
        val entries = apps.map {
            QueueAppEntry(
                packageName = it.packageName,
                appName = it.appName,
                bytesBefore = it.cacheBytes
            )
        }
        _state.value = AutomationState(
            isRunning = entries.isNotEmpty(),
            isManualGuidedMode = false,
            queue = entries,
            currentIndex = 0,
            openedStorageForCurrent = false,
            lastActionTimestampMillis = System.currentTimeMillis(),
            awaitingReportGeneration = false,
            statusMessage = entries.firstOrNull()?.let { "Abrindo ${it.appName}..." } ?: ""
        )
    }

    fun startManualGuidedQueue(apps: List<AppStorageItem>) {
        val entries = apps.map {
            QueueAppEntry(
                packageName = it.packageName,
                appName = it.appName,
                bytesBefore = it.cacheBytes
            )
        }
        _state.value = AutomationState(
            isRunning = false,
            isManualGuidedMode = entries.isNotEmpty(),
            queue = entries,
            currentIndex = 0,
            openedStorageForCurrent = false,
            lastActionTimestampMillis = System.currentTimeMillis(),
            awaitingReportGeneration = false,
            statusMessage = entries.firstOrNull()?.let { "Limpe o cache de ${it.appName} e volte para o app" } ?: ""
        )
    }

    fun markStorageSectionOpened() {
        _state.update { current ->
            current.copy(
                openedStorageForCurrent = true,
                lastActionTimestampMillis = System.currentTimeMillis(),
                statusMessage = current.currentApp?.let { "Procurando Limpar cache em ${it.appName}..." } ?: ""
            )
        }
    }

    fun recordOutcomeAndAdvance(status: CleanActionStatus): QueueAppEntry? {
        var nextApp: QueueAppEntry? = null
        _state.update { current ->
            if (!current.isRunning && !current.isManualGuidedMode) return@update current
            val updatedQueue = current.queue.toMutableList()
            val idx = current.currentIndex
            if (idx in updatedQueue.indices) {
                updatedQueue[idx] = updatedQueue[idx].copy(outcome = status)
            }
            val nextIdx = idx + 1
            val hasMore = nextIdx < updatedQueue.size
            nextApp = if (hasMore) updatedQueue[nextIdx] else null
            current.copy(
                isRunning = if (current.isRunning) hasMore else false,
                isManualGuidedMode = if (current.isManualGuidedMode) hasMore else false,
                queue = updatedQueue,
                currentIndex = nextIdx,
                openedStorageForCurrent = false,
                lastActionTimestampMillis = System.currentTimeMillis(),
                awaitingReportGeneration = !hasMore,
                statusMessage = nextApp?.let { "Processando ${it.appName}..." } ?: "Finalizando relatório..."
            )
        }
        return nextApp
    }

    fun cancelRun() {
        _state.update { current ->
            if (!current.isRunning && !current.isManualGuidedMode) return@update current
            val updated = current.queue.mapIndexed { idx, entry ->
                if (idx >= current.currentIndex && entry.outcome == null) {
                    entry.copy(outcome = CleanActionStatus.SKIPPED)
                } else {
                    entry
                }
            }
            current.copy(
                isRunning = false,
                isManualGuidedMode = false,
                queue = updated,
                awaitingReportGeneration = updated.isNotEmpty(),
                statusMessage = "Limpeza interrompida pelo usuário."
            )
        }
    }

    suspend fun consumeAndBuildReport(scanner: AppCacheScanner): CleanReport? {
        val snapshot = _state.value
        if (!snapshot.awaitingReportGeneration || snapshot.queue.isEmpty()) return null

        val afterMap = scanner.measureCacheBytes(snapshot.queue.map { it.packageName })
        var totalFreed = 0L
        var cleanedCount = 0
        var manualCount = 0
        val reportItems = mutableListOf<CleanReportItem>()

        for (entry in snapshot.queue) {
            val bytesAfter = afterMap[entry.packageName] ?: entry.bytesBefore
            val diff = (entry.bytesBefore - bytesAfter).coerceAtLeast(0L)

            val resolvedStatus = when (entry.outcome) {
                CleanActionStatus.SKIPPED -> CleanActionStatus.SKIPPED
                CleanActionStatus.MANUAL_REQUIRED -> {
                    if (diff > 1024L) CleanActionStatus.SUCCESS else CleanActionStatus.MANUAL_REQUIRED
                }
                CleanActionStatus.ALREADY_CLEAN -> CleanActionStatus.ALREADY_CLEAN
                CleanActionStatus.SUCCESS, null -> {
                    when {
                        diff > 1024L -> CleanActionStatus.SUCCESS
                        bytesAfter <= 32L * 1024L -> CleanActionStatus.ALREADY_CLEAN
                        else -> CleanActionStatus.MANUAL_REQUIRED
                    }
                }
                else -> entry.outcome
            }

            if (resolvedStatus == CleanActionStatus.SUCCESS || resolvedStatus == CleanActionStatus.ALREADY_CLEAN) {
                cleanedCount++
            }
            if (resolvedStatus == CleanActionStatus.MANUAL_REQUIRED) {
                manualCount++
            }
            totalFreed += diff

            val freedText = ByteFormatter.format(diff)
            val (whatDone, assessment) = ExplainerCatalog.buildPostCleanAssessment(
                status = resolvedStatus,
                title = entry.appName,
                freedText = freedText
            )

            reportItems.add(
                CleanReportItem(
                    title = entry.appName,
                    subtitle = "Antes: ${ByteFormatter.format(entry.bytesBefore)} • Depois: ${ByteFormatter.format(bytesAfter)}",
                    bytesBefore = entry.bytesBefore,
                    bytesFreed = diff,
                    status = resolvedStatus,
                    whatWasDone = whatDone,
                    problemAssessment = assessment
                )
            )
        }

        _state.value = AutomationState()

        val summary = when {
            manualCount == 0 ->
                "Limpeza concluída sem nenhum problema! ${ByteFormatter.format(totalFreed)} de cache foram liberados em $cleanedCount app(s). Logins, conversas e arquivos continuam intactos."
            else ->
                "${ByteFormatter.format(totalFreed)} liberados em $cleanedCount app(s). $manualCount app(s) precisaram de toque manual por layout diferente do fabricante."
        }

        return CleanReport(
            id = UUID.randomUUID().toString(),
            timestampMillis = System.currentTimeMillis(),
            operationType = "Limpeza de Cache de Apps",
            totalBytesFreed = totalFreed,
            itemsCleanedCount = cleanedCount,
            itemsFailedCount = manualCount,
            summaryDiagnosis = summary,
            hadAnyProblem = manualCount > 0,
            details = reportItems
        )
    }
}
