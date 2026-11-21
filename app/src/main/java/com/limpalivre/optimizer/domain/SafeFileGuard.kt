package com.limpalivre.optimizer.domain

import java.io.File
import java.nio.file.Files

object SafeFileGuard {

    private val protectedRootFolderNames = setOf(
        "dcim",
        "pictures",
        "movies",
        "music",
        "download",
        "downloads",
        "documents",
        "podcasts",
        "ringtones",
        "alarms",
        "notifications",
        "audiobooks",
        "recordings",
        "android"
    )

    private val tempExtensions = setOf(
        "tmp", "temp", "log", "chk", "dmp", "crash"
    )

    private val safeBakParentHints = setOf(
        "cache", "temp", "tmp", "logs", "log", "crash", "analytics"
    )

    private val installerExtensions = setOf(
        "apk", "apks", "xapk", "apkm"
    )

    fun isSymlink(file: File): Boolean {
        return try {
            Files.isSymbolicLink(file.toPath())
        } catch (_: Exception) {
            false
        }
    }

    fun isProtectedDirectory(dir: File, storageRoot: File): Boolean {
        val rootPath = runCatching { storageRoot.canonicalPath }.getOrDefault(storageRoot.absolutePath)
        val dirPath = runCatching { dir.canonicalPath }.getOrDefault(dir.absolutePath)
        if (dirPath == rootPath) return true

        val relative = dirPath.removePrefix(rootPath).trimStart(File.separatorChar, '/')
        if (relative.isEmpty()) return true

        val segments = relative.split(File.separatorChar, '/').filter { it.isNotEmpty() }
        if (segments.size == 1 && segments[0].lowercase() in protectedRootFolderNames) {
            return true
        }
        val lowerRel = relative.lowercase().replace('\\', '/')
        if (lowerRel == "dcim/camera" || lowerRel.startsWith("dcim/camera/")) {
            return true
        }
        if (lowerRel.startsWith("android/obb") || lowerRel.startsWith("android/data")) {
            return true
        }
        return false
    }

    fun isProtectedFileForDeletion(file: File, storageRoot: File): Boolean {
        if (isSymlink(file)) return true
        val ext = file.extension.lowercase()
        if (ext == "obb") return true
        if (file.name == ".nomedia") return true

        val rootPath = runCatching { storageRoot.canonicalPath }.getOrDefault(storageRoot.absolutePath)
        val filePath = runCatching { file.canonicalPath }.getOrDefault(file.absolutePath)
        val relative = filePath.removePrefix(rootPath).trimStart(File.separatorChar, '/')
            .lowercase().replace('\\', '/')

        if (relative.startsWith("android/obb/") || relative.startsWith("android/data/")) {
            return true
        }
        return false
    }

    fun isTempOrLogFile(file: File): Boolean {
        val nameLower = file.name.lowercase()
        val ext = file.extension.lowercase()
        if (ext in tempExtensions) return true
        if (nameLower.endsWith(".log.gz") || nameLower.endsWith(".txt.log")) return true
        if (ext == "bak" || ext == "old") {
            val parentLower = file.parentFile?.name?.lowercase().orEmpty()
            return parentLower in safeBakParentHints
        }
        return false
    }

    fun isInstallerFile(file: File): Boolean {
        return file.extension.lowercase() in installerExtensions
    }

    fun isSafeEmptyFolderToDelete(dir: File, storageRoot: File): Boolean {
        if (!dir.isDirectory || isSymlink(dir)) return false
        if (isProtectedDirectory(dir, storageRoot)) return false
        val children = dir.listFiles() ?: return false
        return children.isEmpty()
    }
}
