package com.limpalivre.optimizer.domain

import com.limpalivre.optimizer.data.model.CleanableFileItem
import java.io.File
import java.io.RandomAccessFile
import java.security.MessageDigest

object DuplicateDetector {

    private const val MIN_DUPLICATE_BYTES = 10L * 1024L // 10 KB
    private const val PARTIAL_CHUNK_BYTES = 4096

    fun findDuplicates(files: List<File>, onProgress: (String) -> Unit = {}): List<CleanableFileItem> {
        val candidatesBySize = files
            .asSequence()
            .filter { it.isFile && !SafeFileGuard.isSymlink(it) && it.length() >= MIN_DUPLICATE_BYTES }
            .groupBy { it.length() }
            .filter { it.value.size > 1 }

        if (candidatesBySize.isEmpty()) return emptyList()

        val duplicateItems = mutableListOf<CleanableFileItem>()
        var groupCounter = 0

        for ((size, sameSizeFiles) in candidatesBySize) {
            onProgress("Comparando duplicados (${ByteFormatter.format(size)})")
            val byPartialHash = sameSizeFiles
                .mapNotNull { file ->
                    partialHash(file)?.let { hash -> hash to file }
                }
                .groupBy({ it.first }, { it.second })
                .filter { it.value.size > 1 }

            for (partialGroup in byPartialHash.values) {
                val byFullHash = partialGroup
                    .mapNotNull { file ->
                        fullSha256(file)?.let { hash -> hash to file }
                    }
                    .groupBy({ it.first }, { it.second })
                    .filter { it.value.size > 1 }

                for (exactGroup in byFullHash.values) {
                    groupCounter++
                    val groupId = "dup_$groupCounter"
                    val sorted = exactGroup.sortedWith(
                        compareBy<File> { it.lastModified() }.thenBy { it.absolutePath }
                    )
                    sorted.forEachIndexed { index, file ->
                        val isOriginal = index == 0
                        duplicateItems.add(
                            CleanableFileItem(
                                path = file.absolutePath,
                                name = file.name,
                                sizeBytes = file.length(),
                                lastModified = file.lastModified(),
                                isDirectory = false,
                                isSelected = !isOriginal,
                                isProtectedOriginal = isOriginal,
                                duplicateGroupId = groupId,
                                detailNote = if (isOriginal) {
                                    "Original mais antigo (protegido automaticamente)"
                                } else {
                                    "Cópia idêntica #${index} • pasta: ${file.parentFile?.name ?: "raiz"}"
                                }
                            )
                        )
                    }
                }
            }
        }

        return duplicateItems
    }

    private fun partialHash(file: File): String? {
        return try {
            val length = file.length()
            val digest = MessageDigest.getInstance("SHA-256")
            RandomAccessFile(file, "r").use { raf ->
                val headSize = minOf(PARTIAL_CHUNK_BYTES.toLong(), length).toInt()
                val head = ByteArray(headSize)
                raf.readFully(head)
                digest.update(head)

                if (length > PARTIAL_CHUNK_BYTES * 2L) {
                    raf.seek(length - PARTIAL_CHUNK_BYTES)
                    val tail = ByteArray(PARTIAL_CHUNK_BYTES)
                    raf.readFully(tail)
                    digest.update(tail)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            null
        }
    }

    private fun fullSha256(file: File): String? {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(16 * 1024)
            file.inputStream().use { input ->
                var read = input.read(buffer)
                while (read != -1) {
                    digest.update(buffer, 0, read)
                    read = input.read(buffer)
                }
            }
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            null
        }
    }
}
