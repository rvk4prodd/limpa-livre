package com.limpalivre.optimizer.domain

import com.limpalivre.optimizer.data.explainer.ExplainerCatalog
import com.limpalivre.optimizer.data.model.CleanActionStatus
import com.limpalivre.optimizer.data.model.CleanReport
import com.limpalivre.optimizer.data.model.CleanReportItem
import com.limpalivre.optimizer.data.model.CleanableFileItem
import com.limpalivre.optimizer.data.model.DeepCategoryType
import com.limpalivre.optimizer.data.model.DeepCleanCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class DeepStorageScanner(
    private val storageRoot: File
) {

    companion object {
        const val LARGE_FILE_THRESHOLD_BYTES = 50L * 1024L * 1024L // 50 MB
    }

    suspend fun scan(onProgress: (String) -> Unit = {}): List<DeepCleanCategory> = withContext(Dispatchers.IO) {
        if (!storageRoot.exists() || !storageRoot.isDirectory) {
            return@withContext emptyList()
        }

        val tempLogItems = mutableListOf<CleanableFileItem>()
        val thumbnailItems = mutableListOf<CleanableFileItem>()
        val emptyDirItems = mutableListOf<CleanableFileItem>()
        val messengerItems = mutableListOf<CleanableFileItem>()
        val apkItems = mutableListOf<CleanableFileItem>()
        val largeItems = mutableListOf<CleanableFileItem>()
        val regularFilesForDuplicates = mutableListOf<File>()

        val allDirsBottomUp = mutableListOf<File>()
        val visitedDirs = mutableSetOf<String>()

        fun walk(dir: File, inThumbnailsFolder: Boolean) {
            if (!dir.exists() || !dir.isDirectory || SafeFileGuard.isSymlink(dir)) return
            val canonical = runCatching { dir.canonicalPath }.getOrDefault(dir.absolutePath)
            if (!visitedDirs.add(canonical)) return

            val rootPath = runCatching { storageRoot.canonicalPath }.getOrDefault(storageRoot.absolutePath)
            val relative = canonical.removePrefix(rootPath).trimStart(File.separatorChar, '/')
                .lowercase().replace('\\', '/')

            // Android 11+ protege Android/data e Android/obb; Android/media é acessível
            if (relative == "android/data" || relative == "android/obb") return

            onProgress(relative.ifEmpty { storageRoot.name })

            val children = dir.listFiles() ?: return
            val dirIsThumbnails = inThumbnailsFolder || dir.name.equals(".thumbnails", ignoreCase = true)

            for (child in children) {
                if (SafeFileGuard.isSymlink(child)) continue
                if (child.isDirectory) {
                    walk(child, dirIsThumbnails)
                } else if (child.isFile) {
                    val fileSize = child.length()
                    if (dirIsThumbnails && child.name != ".nomedia") {
                        thumbnailItems.add(
                            CleanableFileItem(
                                path = child.absolutePath,
                                name = child.name,
                                sizeBytes = fileSize,
                                lastModified = child.lastModified(),
                                isDirectory = false,
                                isSelected = true,
                                detailNote = "Miniatura em cache (${child.parentFile?.name ?: ".thumbnails"})"
                            )
                        )
                        continue
                    }

                    if (SafeFileGuard.isTempOrLogFile(child)) {
                        tempLogItems.add(
                            CleanableFileItem(
                                path = child.absolutePath,
                                name = child.name,
                                sizeBytes = fileSize,
                                lastModified = child.lastModified(),
                                isDirectory = false,
                                isSelected = true,
                                detailNote = "Temporário/Log • ${child.parentFile?.name ?: "raiz"}"
                            )
                        )
                    } else if (SafeFileGuard.isInstallerFile(child)) {
                        apkItems.add(
                            CleanableFileItem(
                                path = child.absolutePath,
                                name = child.name,
                                sizeBytes = fileSize,
                                lastModified = child.lastModified(),
                                isDirectory = false,
                                isSelected = false,
                                detailNote = "Instalador • ${child.parentFile?.name ?: "raiz"}"
                            )
                        )
                    } else if (fileSize >= LARGE_FILE_THRESHOLD_BYTES) {
                        largeItems.add(
                            CleanableFileItem(
                                path = child.absolutePath,
                                name = child.name,
                                sizeBytes = fileSize,
                                lastModified = child.lastModified(),
                                isDirectory = false,
                                isSelected = false,
                                detailNote = "Pasta: ${child.parentFile?.name ?: "raiz"}"
                            )
                        )
                    }

                    if (!dirIsThumbnails &&
                        !relative.startsWith("android/") &&
                        !SafeFileGuard.isProtectedFileForDeletion(child, storageRoot)
                    ) {
                        regularFilesForDuplicates.add(child)
                    }
                }
            }

            allDirsBottomUp.add(dir)
        }

        walk(storageRoot, inThumbnailsFolder = false)

        // Pastas vazias (bottom-up considerando se subpastas já são vazias)
        val detectedEmptySet = mutableSetOf<String>()
        for (dir in allDirsBottomUp) {
            if (SafeFileGuard.isProtectedDirectory(dir, storageRoot)) continue
            val children = dir.listFiles() ?: continue
            val allChildrenAreEmptyDirs = children.all { child ->
                child.isDirectory && detectedEmptySet.contains(child.absolutePath)
            }
            if (children.isEmpty() || allChildrenAreEmptyDirs) {
                detectedEmptySet.add(dir.absolutePath)
                emptyDirItems.add(
                    CleanableFileItem(
                        path = dir.absolutePath,
                        name = dir.name,
                        sizeBytes = 0L,
                        lastModified = dir.lastModified(),
                        isDirectory = true,
                        isSelected = true,
                        detailNote = dir.parentFile?.name ?: "Armazenamento"
                    )
                )
            }
        }

        // Cache profundo específico de mensageiros (WhatsApp / Telegram)
        scanMessengerDeepCaches(messengerItems, onProgress)

        // Duplicados exatos por tamanho + hash
        val duplicateItems = DuplicateDetector.findDuplicates(regularFilesForDuplicates, onProgress)

        val categories = listOf(
            buildCategory(DeepCategoryType.TEMP_LOGS, tempLogItems.sortedByDescending { it.sizeBytes }),
            buildCategory(DeepCategoryType.THUMBNAILS_CACHE, thumbnailItems.sortedByDescending { it.sizeBytes }),
            buildCategory(DeepCategoryType.EMPTY_RESIDUAL, emptyDirItems),
            buildCategory(DeepCategoryType.MESSENGER_CACHE, messengerItems.sortedByDescending { it.sizeBytes }),
            buildCategory(DeepCategoryType.DUPLICATES, duplicateItems),
            buildCategory(DeepCategoryType.APK_INSTALLERS, apkItems.sortedByDescending { it.sizeBytes }),
            buildCategory(DeepCategoryType.LARGE_FILES, largeItems.sortedByDescending { it.sizeBytes })
        )

        categories.filter { it.items.isNotEmpty() }
    }

    private fun scanMessengerDeepCaches(
        output: MutableList<CleanableFileItem>,
        onProgress: (String) -> Unit
    ) {
        val whatsappRoots = listOf(
            File(storageRoot, "WhatsApp"),
            File(storageRoot, "Android/media/com.whatsapp/WhatsApp"),
            File(storageRoot, "WhatsApp Business"),
            File(storageRoot, "Android/media/com.whatsapp.w4b/WhatsApp Business")
        )

        for (waRoot in whatsappRoots) {
            if (!waRoot.exists() || !waRoot.isDirectory) continue
            onProgress("Cache oculto ${waRoot.name}")

            // .Shared e .trash (arquivos temporários de compartilhamento e lixeira interna)
            listOf(".Shared", ".trash", "Media/.Statuses").forEach { sub ->
                val targetDir = File(waRoot, sub)
                if (targetDir.exists() && targetDir.isDirectory) {
                    targetDir.listFiles()?.forEach { file ->
                        if (file.isFile && !SafeFileGuard.isSymlink(file) && file.name != ".nomedia") {
                            output.add(
                                CleanableFileItem(
                                    path = file.absolutePath,
                                    name = file.name,
                                    sizeBytes = file.length(),
                                    lastModified = file.lastModified(),
                                    isDirectory = false,
                                    isSelected = false,
                                    detailNote = "${waRoot.name}/$sub"
                                )
                            )
                        }
                    }
                }
            }

            // Backups locais antigos de Databases (mantendo sempre o backup mais recente protegido!)
            val dbDir = File(waRoot, "Databases")
            if (dbDir.exists() && dbDir.isDirectory) {
                val backups = dbDir.listFiles()
                    ?.filter { it.isFile && !SafeFileGuard.isSymlink(it) && it.name.contains("msgstore", ignoreCase = true) }
                    ?.sortedByDescending { it.lastModified() }
                    .orEmpty()

                if (backups.size > 1) {
                    backups.drop(1).forEach { oldBackup ->
                        output.add(
                            CleanableFileItem(
                                path = oldBackup.absolutePath,
                                name = oldBackup.name,
                                sizeBytes = oldBackup.length(),
                                lastModified = oldBackup.lastModified(),
                                isDirectory = false,
                                isSelected = false,
                                detailNote = "Backup local antigo (o mais recente permanece intacto)"
                            )
                        )
                    }
                }
            }
        }

        // Telegram cache público (se existir na memória compartilhada)
        val telegramCache = File(storageRoot, "Telegram/Telegram Cache")
        if (telegramCache.exists() && telegramCache.isDirectory) {
            telegramCache.walkTopDown().forEach { file ->
                if (file.isFile && !SafeFileGuard.isSymlink(file) && file.name != ".nomedia") {
                    output.add(
                        CleanableFileItem(
                            path = file.absolutePath,
                            name = file.name,
                            sizeBytes = file.length(),
                            lastModified = file.lastModified(),
                            isDirectory = false,
                            isSelected = false,
                            detailNote = "Cache local do Telegram"
                        )
                    )
                }
            }
        }
    }

    private fun buildCategory(
        type: DeepCategoryType,
        items: List<CleanableFileItem>
    ): DeepCleanCategory {
        val copy = ExplainerCatalog.explainDeepCategory(type)
        return DeepCleanCategory(
            type = type,
            title = copy.title,
            subtitle = copy.subtitle,
            whatItIs = copy.whatItIs,
            willItCauseProblems = copy.willItCauseProblems,
            riskLevel = copy.riskLevel,
            defaultSelected = copy.defaultSelected,
            items = items
        )
    }

    suspend fun cleanSelected(categories: List<DeepCleanCategory>): CleanReport = withContext(Dispatchers.IO) {
        val details = mutableListOf<CleanReportItem>()
        var totalFreed = 0L
        var totalCleaned = 0
        var totalFailed = 0

        for (category in categories) {
            val selectedItems = category.items.filter { it.isSelected && !it.isProtectedOriginal }
            if (selectedItems.isEmpty()) continue

            var categoryBytesBefore = 0L
            var categoryBytesFreed = 0L
            var categoryDeletedCount = 0
            var categoryFailedCount = 0

            // Ordena pastas por caminho mais longo primeiro para remover de dentro para fora
            val ordered = if (category.type == DeepCategoryType.EMPTY_RESIDUAL) {
                selectedItems.sortedByDescending { it.path.length }
            } else {
                selectedItems
            }

            for (item in ordered) {
                val target = File(item.path)
                categoryBytesBefore += item.sizeBytes

                if (!target.exists()) {
                    continue
                }

                val isSafe = if (target.isDirectory) {
                    SafeFileGuard.isSafeEmptyFolderToDelete(target, storageRoot)
                } else {
                    !SafeFileGuard.isProtectedFileForDeletion(target, storageRoot)
                }

                if (!isSafe) {
                    categoryFailedCount++
                    continue
                }

                val deleted = runCatching { target.delete() }.getOrDefault(false)
                if (deleted) {
                    categoryDeletedCount++
                    categoryBytesFreed += item.sizeBytes
                } else {
                    categoryFailedCount++
                }
            }

            totalFreed += categoryBytesFreed
            totalCleaned += categoryDeletedCount
            totalFailed += categoryFailedCount

            val status = when {
                categoryDeletedCount > 0 && categoryFailedCount == 0 -> CleanActionStatus.SUCCESS
                categoryDeletedCount > 0 && categoryFailedCount > 0 -> CleanActionStatus.PARTIAL
                categoryDeletedCount == 0 && categoryFailedCount == 0 -> CleanActionStatus.ALREADY_CLEAN
                else -> CleanActionStatus.FAILED
            }

            val freedLabel = ByteFormatter.format(categoryBytesFreed)
            val (whatWasDone, assessment) = if (category.type == DeepCategoryType.EMPTY_RESIDUAL) {
                Pair(
                    "$categoryDeletedCount pasta(s) vazia(s) removida(s).",
                    "Sem problemas: apenas diretórios sem nenhum arquivo foram removidos; pastas protegidas permaneceram intactas."
                )
            } else {
                ExplainerCatalog.buildPostCleanAssessment(status, category.title, freedLabel)
            }

            details.add(
                CleanReportItem(
                    title = category.title,
                    subtitle = "$categoryDeletedCount removido(s)" +
                        if (categoryFailedCount > 0) " • $categoryFailedCount bloqueado(s)" else "",
                    bytesBefore = categoryBytesBefore,
                    bytesFreed = categoryBytesFreed,
                    status = status,
                    whatWasDone = whatWasDone,
                    problemAssessment = assessment
                )
            )
        }

        val hadProblem = totalFailed > 0
        val summary = when {
            details.isEmpty() -> "Nenhum item selecionado para limpeza."
            !hadProblem -> "Limpeza profunda concluída sem erros. ${ByteFormatter.format(totalFreed)} liberados e todos os seus arquivos pessoais foram preservados."
            else -> "Limpeza concluída (${ByteFormatter.format(totalFreed)} liberados). $totalFailed item(ns) estavam bloqueados pelo sistema e foram pulados com segurança."
        }

        CleanReport(
            id = UUID.randomUUID().toString(),
            timestampMillis = System.currentTimeMillis(),
            operationType = "Limpeza Profunda",
            totalBytesFreed = totalFreed,
            itemsCleanedCount = totalCleaned,
            itemsFailedCount = totalFailed,
            summaryDiagnosis = summary,
            hadAnyProblem = hadProblem,
            details = details
        )
    }
}
